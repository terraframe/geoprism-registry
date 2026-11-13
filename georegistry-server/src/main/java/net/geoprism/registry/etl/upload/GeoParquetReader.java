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
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.CloseableFile;

import net.geoprism.data.importer.FeatureRow;
import net.geoprism.registry.InvalidProjectionException;
import net.geoprism.registry.UnableToReadProjectionException;
import net.geoprism.registry.etl.ImportFileFormatException;

/**
 * Reads a GeoParquet file (https://geoparquet.org) using an embedded, in-memory
 * DuckDB database.
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

    public Row(Map<String, Object> values, Geometry geometry, Long rowNumber)
    {
      this.values = values;
      this.geometry = geometry;
      this.rowNumber = rowNumber;
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

    public Geometry getGeometry()
    {
      return geometry;
    }
  }

  private final String       path;

  private final Connection   connection;

  private final String       geometryColumn;

  private final JsonObject   geometryMetadata;

  private final List<Column> columns;

  public GeoParquetReader(File file)
  {
    try
    {
      this.path = file.getCanonicalPath();
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }

    try
    {
      this.connection = openConnection(this.path);
    }
    catch (SQLException e)
    {
      throw new ProgrammingErrorException(e);
    }

    try
    {
      JsonObject metadata = this.readGeoMetadata();

      this.geometryColumn = metadata.get("primary_column").getAsString();
      this.geometryMetadata = metadata.getAsJsonObject("columns").getAsJsonObject(this.geometryColumn);

      if (this.geometryMetadata == null)
      {
        throw new ImportFileFormatException("GeoParquet metadata does not describe the primary column [" + this.geometryColumn + "]");
      }

      String encoding = this.geometryMetadata.has("encoding") ? this.geometryMetadata.get("encoding").getAsString() : null;

      if (!WKB_ENCODING.equalsIgnoreCase(encoding))
      {
        throw new ImportFileFormatException("Unsupported GeoParquet geometry encoding [" + encoding + "]. Only WKB is supported.");
      }

      this.columns = this.readColumns(metadata.getAsJsonObject("columns").keySet());
    }
    catch (SQLException | RuntimeException e)
    {
      this.close();

      if (e instanceof ImportFileFormatException)
      {
        throw (ImportFileFormatException) e;
      }

      // Not a parquet file, or the GeoParquet metadata is malformed
      throw new ImportFileFormatException(e);
    }
  }

  /**
   * Opens an in-memory DuckDB database which can only read the given file.
   */
  private static Connection openConnection(String path) throws SQLException
  {
    Properties properties = new Properties();
    properties.setProperty("autoinstall_known_extensions", "false");
    properties.setProperty("autoload_known_extensions", "false");
    properties.setProperty("allow_community_extensions", "false");
    properties.setProperty("memory_limit", MEMORY_LIMIT);
    properties.setProperty("threads", THREADS);

    // Stream result sets instead of materializing the whole file in memory
    properties.setProperty("jdbc_stream_results", "true");

    Connection connection = DriverManager.getConnection("jdbc:duckdb:", properties);

    try (Statement statement = connection.createStatement())
    {
      statement.execute("SET TimeZone = 'UTC'");

      // Always return geometry columns as raw WKB
      statement.execute("SET enable_geoparquet_conversion = false");

      // Only the file being imported may be read. This also blocks
      // network access and installing or loading extensions.
      statement.execute("SET allowed_paths = [" + literal(path) + "]");
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

  private JsonObject readGeoMetadata() throws SQLException
  {
    String sql = "SELECT decode(value) FROM parquet_kv_metadata(?) WHERE decode(key) = ?";

    try (PreparedStatement statement = this.connection.prepareStatement(sql))
    {
      statement.setString(1, this.path);
      statement.setString(2, GEO_METADATA_KEY);

      try (ResultSet results = statement.executeQuery())
      {
        if (results.next())
        {
          return JsonParser.parseString(results.getString(1)).getAsJsonObject();
        }
      }
    }

    throw new ImportFileFormatException("Parquet file does not contain GeoParquet metadata");
  }

  private List<Column> readColumns(Set<String> geometryColumns) throws SQLException
  {
    List<Column> columns = new ArrayList<>();
    boolean hasGeometry = false;

    try (PreparedStatement statement = this.connection.prepareStatement("DESCRIBE SELECT * FROM read_parquet(?)"))
    {
      statement.setString(1, this.path);

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
              throw new ImportFileFormatException("Unsupported type [" + type + "] for geometry column [" + name + "]");
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
      throw new ImportFileFormatException("GeoParquet primary column [" + this.geometryColumn + "] does not exist");
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

  public String getPath()
  {
    return path;
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

  public long count()
  {
    try (PreparedStatement statement = this.connection.prepareStatement("SELECT count(*) FROM read_parquet(?)"))
    {
      statement.setString(1, this.path);

      try (ResultSet results = statement.executeQuery())
      {
        results.next();

        return results.getLong(1);
      }
    }
    catch (SQLException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  /**
   * Streams the rows of the file, in file order.
   *
   * @param startIndex
   *          Zero based index of the first row to read
   * @param includeGeometry
   *          If false the geometry column is not read or parsed
   * @param limit
   *          Maximum number of rows to read, or null for no limit
   * @param handler
   *          Receives each row, and returns false to stop reading
   */
  public void read(long startIndex, boolean includeGeometry, Long limit, RowHandler handler) throws Exception
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

    WKBReader wkbReader = new WKBReader();

    try (PreparedStatement statement = this.connection.prepareStatement(sql.toString()))
    {
      statement.setString(1, this.path);
      statement.setLong(2, startIndex);

      try (ResultSet results = statement.executeQuery())
      {
        while (results.next())
        {
          long rowNumber = results.getLong(1) + 1;

          Map<String, Object> values = new HashMap<>();

          for (int i = 0; i < this.columns.size(); i++)
          {
            Column column = this.columns.get(i);

            values.put(column.getName(), convert(column, results.getObject(i + 2)));
          }

          Geometry geometry = null;

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
                throw new ImportFileFormatException("Unable to parse the geometry on row [" + rowNumber + "]", e);
              }
            }
          }

          if (!handler.handle(new Row(values, geometry, rowNumber)))
          {
            return;
          }
        }
      }
    }
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
   * Locates the parquet file in an uploaded resource. Supports a standalone
   * .parquet file, or a .zip containing one (the first match is used).
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
      return ShapefileImporter.getShapefileFromResource(res, EXTENSION);
    }

    throw new ImportFileFormatException();
  }
}
