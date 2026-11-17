/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.etl.upload;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.duckdb.DuckDBDriver;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.business.SmartException;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.CloseableFile;

import net.geoprism.data.importer.FeatureRow;
import net.geoprism.registry.InvalidProjectionException;
import net.geoprism.registry.UnableToReadProjectionException;
import net.geoprism.registry.etl.ImportFileFormatException;
import net.geoprism.registry.io.InvalidGeometryException;

/**
 * Reads a GeoParquet file (https://geoparquet.org), or a dataset split across
 * several GeoParquet files, using an embedded, in-memory DuckDB database.
 * <p>
 * The parquet reader is statically linked into the DuckDB JDBC driver, so no
 * DuckDB extensions are required. The database is locked down so that it can
 * never install or load extensions, or touch any file or URL other than the
 * file being read. Geometries are decoded from WKB with JTS rather than with
 * the DuckDB spatial extension.
 */
public class GeoParquetReader implements AutoCloseable
{
  private static final Logger logger            = LoggerFactory.getLogger(GeoParquetReader.class);

  public static final String  EXTENSION         = "parquet";

  public static final String  GEO_METADATA_KEY  = "geo";

  public static final String  WKB_ENCODING      = "WKB";

  /**
   * Upper bound on the memory DuckDB may use. Reads are streamed, so this only
   * needs to hold a few row groups at a time.
   */
  private static final String MEMORY_LIMIT      = "256MB";

  private static final String THREADS           = "2";

  private static final String ROW_NUMBER_COLUMN = "file_row_number";

  /**
   * An in-memory database
   */
  private static final String JDBC_URL          = "jdbc:duckdb:";

  /**
   * Callback for {@link GeoParquetReader#read}. Return false to stop reading.
   */
  @FunctionalInterface
  public static interface RowHandler
  {
    public boolean handle(Row row) throws Exception;
  }

  public static class Column
  {
    private final String   name;

    private final String   sqlType;

    private final Class<?> binding;

    public Column(String name, String sqlType, Class<?> binding)
    {
      this.name = name;
      this.sqlType = sqlType;
      this.binding = binding;
    }

    public String getName()
    {
      return name;
    }

    public String getSqlType()
    {
      return sqlType;
    }

    /**
     * @return The Java type of the values in this column: {@link String},
     *         {@link Boolean}, {@link Long}, {@link BigInteger}, {@link Double}
     *         or {@link Date}.
     */
    public Class<?> getBinding()
    {
      return binding;
    }

    private boolean isDate()
    {
      return Date.class.equals(this.binding);
    }
  }

  public static class Row implements FeatureRow
  {
    private final Map<String, Object> values;

    private final Geometry            geometry;

    private final Long                rowNumber;

    /**
     * Why the row's geometry could not be parsed, or null if it was parsed (or
     * is empty)
     */
    private final ParseException      geometryError;

    public Row(Map<String, Object> values, Geometry geometry, Long rowNumber)
    {
      this(values, geometry, rowNumber, null);
    }

    public Row(Map<String, Object> values, Geometry geometry, Long rowNumber, ParseException geometryError)
    {
      this.values = values;
      this.geometry = geometry;
      this.rowNumber = rowNumber;
      this.geometryError = geometryError;
    }

    @Override
    public Object getValue(String attributeName)
    {
      return this.values.get(attributeName);
    }

    @Override
    public Long getRowNumber()
    {
      return this.rowNumber;
    }

    /**
     * @return The row's geometry, or null if it has none (or geometries were
     *         not read)
     * @throws InvalidGeometryException
     *           if the geometry could not be parsed. The importers call this
     *           for each row, so the problem is reported against the row,
     *           rather than stopping the import.
     */
    public Geometry getGeometry()
    {
      if (this.geometryError != null)
      {
        throw new InvalidGeometryException("Unable to parse the geometry on row [" + this.rowNumber + "]: " + this.geometryError.getMessage(), this.geometryError);
      }

      return geometry;
    }

    /**
     * @return Why the geometry could not be parsed, or null
     */
    public ParseException getGeometryError()
    {
      return geometryError;
    }

    /**
     * @return The values of the importable columns, in column order
     */
    public Map<String, Object> getValues()
    {
      return Collections.unmodifiableMap(this.values);
    }
  }

  /**
   * The parts of the dataset, in read order. A dataset is usually one file,
   * but can be split into several files with the same schema (e.g.
   * part-0.parquet, part-1.parquet, ...), as written by Spark, Sedona, DuckDB
   * and GDAL.
   */
  private final List<String> paths;

  /**
   * Number of rows in each part
   */
  private final long[]       counts;

  private final Connection   connection;

  private final String       geometryColumn;

  private final JsonObject   geometryMetadata;

  private final List<Column> columns;

  /**
   * @param file
   *          A GeoParquet file, or a directory containing the parts of a
   *          GeoParquet dataset (every .parquet file under it, recursively)
   */
  public GeoParquetReader(File file)
  {
    List<File> files = file.isDirectory() ? ImportFileResources.findFiles(file, EXTENSION) : List.of(file);

    if (files.isEmpty())
    {
      throw ImportFileResources.newMissingFileException(EXTENSION);
    }

    this.paths = new ArrayList<>();

    try
    {
      for (File f : files)
      {
        this.paths.add(f.getCanonicalPath());
      }
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }

    try
    {
      this.connection = openConnection(this.paths);
    }
    catch (SQLException e)
    {
      throw new ProgrammingErrorException(e);
    }

    try
    {
      String first = this.paths.get(0);

      JsonObject metadata = this.readGeoMetadata(first);

      this.geometryColumn = metadata.get("primary_column").getAsString();
      this.geometryMetadata = metadata.getAsJsonObject("columns").getAsJsonObject(this.geometryColumn);

      if (this.geometryMetadata == null)
      {
        throw invalid(first, "The GeoParquet metadata does not describe the primary geometry column [" + this.geometryColumn + "].");
      }

      String encoding = this.geometryMetadata.has("encoding") ? this.geometryMetadata.get("encoding").getAsString() : null;

      if (!WKB_ENCODING.equalsIgnoreCase(encoding))
      {
        throw ImportFileFormatException.withDetails("The GeoParquet geometry encoding [" + encoding + "] is not supported. Only WKB encoded geometries can be imported.");
      }

      this.columns = this.readColumns(first, metadata.getAsJsonObject("columns").keySet());

      for (int i = 1; i < this.paths.size(); i++)
      {
        this.validatePart(this.paths.get(i));
      }

      this.counts = new long[this.paths.size()];

      for (int i = 0; i < this.paths.size(); i++)
      {
        this.counts[i] = this.count(this.paths.get(i));
      }
    }
    catch (SQLException | RuntimeException e)
    {
      this.close();

      if (e instanceof SmartException)
      {
        throw (SmartException) e;
      }

      // Not a parquet file, or the GeoParquet metadata is malformed
      throw this.fileError("[" + new File(this.paths.get(0)).getName() + "] is not a valid GeoParquet file. ", e);
    }
  }

  /**
   * Creates an error for a file which DuckDB could not read, with DuckDB's
   * description of the problem, without the server's temporary file paths.
   */
  private ImportFileFormatException fileError(String prefix, Throwable e)
  {
    String details = ImportFileFormatException.describe(e);

    for (String path : this.paths)
    {
      details = details.replace(path, new File(path).getName());
    }

    return ImportFileFormatException.withDetails(prefix + details, e);
  }

  /**
   * @param path
   *          The file which can't be read as GeoParquet
   */
  private static ImportFileFormatException invalid(String path, String details)
  {
    return ImportFileFormatException.withDetails("[" + new File(path).getName() + "] is not a valid GeoParquet file. " + details);
  }

  /**
   * Ensures another part of the dataset matches the first: the same primary
   * geometry column, encoding, CRS and edges, and the same importable columns
   * with the same types. Per-file details such as the bounding box and
   * geometry types are allowed to differ.
   */
  private void validatePart(String path) throws SQLException
  {
    String name = new File(path).getName();
    String first = new File(this.paths.get(0)).getName();

    JsonObject metadata = this.readGeoMetadata(path);

    String primary = metadata.has("primary_column") ? metadata.get("primary_column").getAsString() : null;

    if (!this.geometryColumn.equals(primary))
    {
      throw ImportFileFormatException.withDetails("The GeoParquet file [" + name + "] has a different primary geometry column than [" + first + "]. Every file in the zip must have the same columns and geometry settings.");
    }

    JsonObject geometry = metadata.getAsJsonObject("columns").getAsJsonObject(primary);

    for (String key : new String[] { "encoding", "crs", "edges" })
    {
      if (!Objects.equals(this.geometryMetadata.get(key), geometry == null ? null : geometry.get(key)))
      {
        throw ImportFileFormatException.withDetails("The GeoParquet file [" + name + "] has a different geometry [" + key + "] than [" + first + "]. Every file in the zip must have the same columns and geometry settings.");
      }
    }

    List<Column> columns = this.readColumns(path, metadata.getAsJsonObject("columns").keySet());

    boolean matches = columns.size() == this.columns.size();

    for (int i = 0; matches && i < columns.size(); i++)
    {
      matches = columns.get(i).getName().equals(this.columns.get(i).getName()) && columns.get(i).getSqlType().equals(this.columns.get(i).getSqlType());
    }

    if (!matches)
    {
      throw ImportFileFormatException.withDetails("The GeoParquet file [" + name + "] does not have the same columns as [" + first + "]. Every file in the zip must have the same columns and geometry settings.");
    }
  }

  /**
   * Opens a new in-memory DuckDB database.
   * <p>
   * The driver is created directly instead of through
   * {@link java.sql.DriverManager}. DriverManager only discovers drivers once,
   * on the first JDBC call in the JVM, using the class loader of whichever
   * thread made it. In a web application (or when another library, such as
   * the Postgres driver, makes that first call) the DuckDB driver can be missed,
   * which fails with "No suitable driver found for jdbc:duckdb:".
   */
  public static Connection connect(Properties properties) throws SQLException
  {
    Connection connection = new DuckDBDriver().connect(JDBC_URL, properties);

    if (connection == null)
    {
      throw new SQLException("The DuckDB driver did not accept the URL [" + JDBC_URL + "]");
    }

    return connection;
  }

  /**
   * Opens an in-memory DuckDB database which can only read the given files.
   * Package visible so the lock down can be tested.
   */
  static Connection openConnection(List<String> paths) throws SQLException
  {
    Properties properties = new Properties();
    properties.setProperty("autoinstall_known_extensions", "false");
    properties.setProperty("autoload_known_extensions", "false");
    properties.setProperty("allow_community_extensions", "false");
    properties.setProperty("memory_limit", MEMORY_LIMIT);
    properties.setProperty("threads", THREADS);

    // Stream result sets instead of materializing the whole file in memory
    properties.setProperty("jdbc_stream_results", "true");

    Connection connection = connect(properties);

    try (Statement statement = connection.createStatement())
    {
      statement.execute("SET TimeZone = 'UTC'");

      // Always return geometry columns as raw WKB
      statement.execute("SET enable_geoparquet_conversion = false");

      // Only the file being imported may be read. This also blocks
      // network access and installing or loading extensions.
      statement.execute("SET allowed_paths = [" + paths.stream().map(GeoParquetReader::literal).collect(Collectors.joining(", ")) + "]");
      statement.execute("SET enable_external_access = false");
      statement.execute("SET lock_configuration = true");
    }
    catch (SQLException e)
    {
      connection.close();

      throw e;
    }

    return connection;
  }

  private JsonObject readGeoMetadata(String path) throws SQLException
  {
    String sql = "SELECT decode(value) FROM parquet_kv_metadata(?) WHERE decode(key) = ?";

    try (PreparedStatement statement = this.connection.prepareStatement(sql))
    {
      statement.setString(1, path);
      statement.setString(2, GEO_METADATA_KEY);

      try (ResultSet results = statement.executeQuery())
      {
        if (results.next())
        {
          return JsonParser.parseString(results.getString(1)).getAsJsonObject();
        }
      }
    }

    throw invalid(path, "It does not contain GeoParquet metadata. Plain Parquet files without geometry metadata are not supported.");
  }

  private List<Column> readColumns(String path, Set<String> geometryColumns) throws SQLException
  {
    List<Column> columns = new ArrayList<>();
    boolean hasGeometry = false;

    try (PreparedStatement statement = this.connection.prepareStatement("DESCRIBE SELECT * FROM read_parquet(?)"))
    {
      statement.setString(1, path);

      try (ResultSet results = statement.executeQuery())
      {
        while (results.next())
        {
          String name = results.getString("column_name");
          String type = results.getString("column_type");

          if (name.equals(this.geometryColumn))
          {
            if (!type.equals("BLOB"))
            {
              throw invalid(path, "The geometry column [" + name + "] has the unsupported type [" + type + "].");
            }

            hasGeometry = true;
          }
          else if (!geometryColumns.contains(name))
          {
            Class<?> binding = getBinding(type);

            if (binding != null)
            {
              columns.add(new Column(name, type, binding));
            }
            else
            {
              logger.debug("Skipping GeoParquet column [" + name + "] with unsupported type [" + type + "]");
            }
          }
        }
      }
    }

    if (!hasGeometry)
    {
      throw invalid(path, "The primary geometry column [" + this.geometryColumn + "] does not exist.");
    }

    return columns;
  }

  /**
   * @return The Java type which values of the given DuckDB type are converted
   *         to, or null if the column type is not supported for import (blobs,
   *         lists, structs, maps, etc).
   */
  private static Class<?> getBinding(String type)
  {
    String base = type.toUpperCase(Locale.ROOT);

    if (base.startsWith("DECIMAL"))
    {
      return Double.class;
    }

    switch (base)
    {
      case "VARCHAR":
      case "UUID":
        return String.class;
      case "BOOLEAN":
        return Boolean.class;
      case "TINYINT":
      case "SMALLINT":
      case "INTEGER":
      case "BIGINT":
      case "UTINYINT":
      case "USMALLINT":
      case "UINTEGER":
        return Long.class;
      case "UBIGINT":
      case "HUGEINT":
      case "UHUGEINT":
        return BigInteger.class;
      case "FLOAT":
      case "DOUBLE":
        return Double.class;
      case "DATE":
      case "TIMESTAMP":
      case "TIMESTAMP_S":
      case "TIMESTAMP_MS":
      case "TIMESTAMP_NS":
      case "TIMESTAMP WITH TIME ZONE":
        return Date.class;
      default:
        return null;
    }
  }

  /**
   * @return The files of the dataset, in read order
   */
  public List<String> getPaths()
  {
    return Collections.unmodifiableList(paths);
  }

  /**
   * @return The importable (non-geometry) columns in the file
   */
  public List<Column> getColumns()
  {
    return Collections.unmodifiableList(columns);
  }

  public String getGeometryColumn()
  {
    return geometryColumn;
  }

  /**
   * Ensures the geometries are in WGS 84 longitude/latitude. Per the GeoParquet
   * specification an absent "crs" means OGC:CRS84, and a null "crs" means the
   * CRS is unknown.
   *
   * @throws UnableToReadProjectionException
   *           if the CRS is unknown or can't be identified
   * @throws InvalidProjectionException
   *           if the CRS is not WGS 84
   */
  public void validateProjection()
  {
    if (!this.geometryMetadata.has("crs"))
    {
      return;
    }

    JsonElement crs = this.geometryMetadata.get("crs");

    if (!crs.isJsonObject() || !crs.getAsJsonObject().has("id"))
    {
      throw new UnableToReadProjectionException();
    }

    JsonObject id = crs.getAsJsonObject().getAsJsonObject("id");
    String authority = id.has("authority") ? id.get("authority").getAsString() : "";
    String code = id.has("code") ? id.get("code").getAsString() : "";

    boolean isWgs84 = ( authority.equalsIgnoreCase("EPSG") && code.equals("4326") ) || ( authority.equalsIgnoreCase("OGC") && code.equalsIgnoreCase("CRS84") );

    if (!isWgs84)
    {
      throw new InvalidProjectionException();
    }
  }

  /**
   * @return The total number of rows in the dataset
   */
  public long count()
  {
    long total = 0;

    for (long count : this.counts)
    {
      total += count;
    }

    return total;
  }

  private long count(String path) throws SQLException
  {
    try (PreparedStatement statement = this.connection.prepareStatement("SELECT count(*) FROM read_parquet(?)"))
    {
      statement.setString(1, path);

      try (ResultSet results = statement.executeQuery())
      {
        results.next();

        return results.getLong(1);
      }
    }
  }

  /**
   * Streams the rows of the dataset, in file order. When the dataset has
   * several parts they are read one after another, and rows are numbered
   * across all of them, so the first row of the second part follows the last
   * row of the first part.
   *
   * @param startIndex
   *          Zero based index of the first row to read, across all parts
   * @param includeGeometry
   *          If false the geometry column is not read or parsed
   * @param limit
   *          Maximum number of rows to read, or null for no limit
   * @param handler
   *          Receives each row, and returns false to stop reading
   */
  public void read(long startIndex, boolean includeGeometry, Long limit, RowHandler handler) throws Exception
  {
    long offset = 0;
    Long remaining = limit;

    for (int i = 0; i < this.paths.size(); i++)
    {
      long count = this.counts[i];

      if (startIndex < offset + count)
      {
        long start = Math.max(0, startIndex - offset);

        long read = this.readPart(this.paths.get(i), offset, start, includeGeometry, remaining, handler);

        if (read < 0)
        {
          // The handler asked to stop
          return;
        }

        if (remaining != null)
        {
          remaining = remaining - read;

          if (remaining <= 0)
          {
            return;
          }
        }
      }

      offset += count;
    }
  }

  /**
   * Reads the rows of one part of the dataset.
   *
   * @param offset
   *          Number of rows in the parts before this one
   * @param startIndex
   *          Zero based index, within this part, of the first row to read
   * @return The number of rows read, or -1 if the handler asked to stop
   */
  private long readPart(String path, long offset, long startIndex, boolean includeGeometry, Long limit, RowHandler handler) throws Exception
  {
    StringBuilder sql = new StringBuilder("SELECT " + ROW_NUMBER_COLUMN);

    for (Column column : this.columns)
    {
      // Convert dates and timestamps to epoch milliseconds in DuckDB so the
      // JDBC driver doesn't apply the JVM's time zone.
      if (column.isDate())
      {
        sql.append(", epoch_ms(" + identifier(column.getName()) + "::TIMESTAMP WITH TIME ZONE)");
      }
      else
      {
        sql.append(", " + identifier(column.getName()));
      }
    }

    if (includeGeometry)
    {
      sql.append(", " + identifier(this.geometryColumn));
    }

    sql.append(" FROM read_parquet(?, file_row_number = true) WHERE " + ROW_NUMBER_COLUMN + " >= ?");

    if (limit != null)
    {
      sql.append(" LIMIT " + limit);
    }

    try
    {
      return this.executeRead(path, offset, sql.toString(), startIndex, includeGeometry, handler);
    }
    catch (SQLException e)
    {
      throw this.fileError("[" + new File(path).getName() + "] could not be read. ", e);
    }
  }

  private long executeRead(String path, long offset, String sql, long startIndex, boolean includeGeometry, RowHandler handler) throws Exception
  {
    WKBReader wkbReader = new WKBReader();
    long read = 0;

    try (PreparedStatement statement = this.connection.prepareStatement(sql))
    {
      statement.setString(1, path);
      statement.setLong(2, startIndex);

      try (ResultSet results = statement.executeQuery())
      {
        while (results.next())
        {
          long rowNumber = offset + results.getLong(1) + 1;

          // Keep the values in column order
          Map<String, Object> values = new LinkedHashMap<>();

          for (int i = 0; i < this.columns.size(); i++)
          {
            Column column = this.columns.get(i);

            values.put(column.getName(), convert(column, results.getObject(i + 2)));
          }

          Geometry geometry = null;
          ParseException geometryError = null;

          if (includeGeometry)
          {
            byte[] wkb = results.getBytes(this.columns.size() + 2);

            if (wkb != null)
            {
              try
              {
                geometry = wkbReader.read(wkb);
              }
              catch (ParseException e)
              {
                // Don't stop reading: the problem is reported against this row
                // when its geometry is used (see Row.getGeometry)
                geometryError = e;
              }
            }
          }

          read++;

          if (!handler.handle(new Row(values, geometry, rowNumber, geometryError)))
          {
            return -1;
          }
        }
      }
    }

    return read;
  }

  private static Object convert(Column column, Object value)
  {
    if (value == null)
    {
      return null;
    }

    Class<?> binding = column.getBinding();

    if (binding.equals(String.class))
    {
      return value.toString();
    }
    else if (binding.equals(Long.class))
    {
      return ( (Number) value ).longValue();
    }
    else if (binding.equals(Double.class))
    {
      return ( (Number) value ).doubleValue();
    }
    else if (binding.equals(Date.class))
    {
      return new Date( ( (Number) value ).longValue());
    }

    return value;
  }

  @Override
  public void close()
  {
    try
    {
      this.connection.close();
    }
    catch (SQLException e)
    {
      logger.error("Unable to close GeoParquet reader", e);
    }
  }

  private static String identifier(String name)
  {
    return "\"" + name.replace("\"", "\"\"") + "\"";
  }

  private static String literal(String value)
  {
    return "'" + value.replace("'", "''") + "'";
  }

  /**
   * Locates the GeoParquet data in an uploaded resource: either a standalone
   * .parquet file, or a .zip containing one or more .parquet files (anywhere
   * in the zip). For a zip the extracted directory is returned, and every
   * .parquet file in it is read as a part of the same dataset.
   */
  public static CloseableFile getParquetFromResource(ApplicationResource res)
  {
    String extension = res.getNameExtension();

    if (EXTENSION.equalsIgnoreCase(extension))
    {
      return res.openNewFile();
    }
    else if ("zip".equalsIgnoreCase(extension))
    {
      CloseableFile directory = ImportFileResources.extractZip(res);

      if (ImportFileResources.findFiles(directory, EXTENSION).isEmpty())
      {
        directory.close();

        throw ImportFileResources.newMissingFileException(EXTENSION);
      }

      return directory;
    }

    throw ImportFileFormatException.withDetails("Expected a ." + EXTENSION + " or .zip file, but got [" + res.getName() + "].");
  }
}
