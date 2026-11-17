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
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import net.geoprism.registry.etl.upload.GeoParquetReader.Column;
import net.geoprism.registry.etl.upload.GeoParquetReader.Row;

/**
 * Unit tests for {@link GeoParquetReader}. These don't need a database: the
 * fixtures are generated with DuckDB. Error cases are covered by
 * GeoParquetServiceTest, because Runway exceptions can only be created with a
 * running Runway.
 */
public class GeoParquetReaderTest
{
  /**
   * WKB for POINT (1 2), little endian
   */
  private static final String POINT_WKB = "from_hex('0101000000000000000000F03F0000000000000040')";

  private static final String WKB_METADATA = "{\"version\":\"1.1.0\",\"primary_column\":\"geometry\",\"columns\":{\"geometry\":{\"encoding\":\"WKB\",\"geometry_types\":[\"Point\"]}}}";

  @TempDir
  File directory;

  /**
   * Writes the result of a query to a GeoParquet file
   */
  private static File write(File file, String select, String geo, String options) throws SQLException
  {
    file.getParentFile().mkdirs();

    try (Connection connection = GeoParquetReader.connect(new Properties()); Statement statement = connection.createStatement())
    {
      // Write naive timestamps in a time zone other than UTC, to show that the
      // values don't depend on the writer's time zone
      statement.execute("SET TimeZone = 'America/Denver'");

      String metadata = geo == null ? "" : ", KV_METADATA {geo: '" + geo.replace("'", "''") + "'}";

      statement.execute("COPY (" + select + ") TO '" + file.getAbsolutePath() + "' (FORMAT parquet" + metadata + options + ")");
    }

    return file;
  }

  private static File write(File file, String select) throws SQLException
  {
    return write(file, select, WKB_METADATA, "");
  }

  private static List<Row> readAll(GeoParquetReader reader, long startIndex, boolean includeGeometry, Long limit) throws Exception
  {
    List<Row> rows = new ArrayList<>();

    reader.read(startIndex, includeGeometry, limit, row -> {
      rows.add(row);

      return true;
    });

    return rows;
  }

  @Test
  public void testColumnTypesAndValues() throws Exception
  {
    File file = write(new File(this.directory, "types.parquet"), "SELECT * FROM (VALUES " //
        + "('text', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'::UUID, true, 42::INTEGER, 9000000000::BIGINT, 7::UBIGINT, 18446744073709551615::UBIGINT, 1.5::DOUBLE, 2.5::FLOAT, 12.50::DECIMAL(10,2), " //
        + "DATE '2020-01-15', TIMESTAMP '2020-01-15 23:30:00', '2020-01-15 23:30:00'::TIMESTAMP_NS, TIMESTAMPTZ '2020-01-15 23:30:00+00', " //
        + "TIME '12:00:00', [1, 2], {'a': 1}, " + POINT_WKB + "), " //
        + "(NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL)" //
        + ") t(s, u, b, i, bi, ub, ubmax, d, f, dec, dt, ts, tsns, tstz, tm, list, struct, geometry)");

    // Read in a JVM time zone far from UTC, to show that dates don't depend on
    // the server's time zone
    TimeZone original = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Auckland"));

    try (GeoParquetReader reader = new GeoParquetReader(file))
    {
      // TIME, LIST and STRUCT columns can't be imported, and the geometry
      // column isn't an attribute
      Map<String, Class<?>> bindings = reader.getColumns().stream().collect(Collectors.toMap(Column::getName, Column::getBinding, (a, b) -> a, java.util.LinkedHashMap::new));

      Assertions.assertEquals(List.of("s", "u", "b", "i", "bi", "ub", "ubmax", "d", "f", "dec", "dt", "ts", "tsns", "tstz"), new ArrayList<>(bindings.keySet()));
      Assertions.assertEquals(String.class, bindings.get("s"));
      Assertions.assertEquals(String.class, bindings.get("u"));
      Assertions.assertEquals(Boolean.class, bindings.get("b"));
      Assertions.assertEquals(Long.class, bindings.get("i"));
      Assertions.assertEquals(Long.class, bindings.get("bi"));
      Assertions.assertEquals(BigInteger.class, bindings.get("ub"));
      Assertions.assertEquals(Double.class, bindings.get("d"));
      Assertions.assertEquals(Double.class, bindings.get("f"));
      Assertions.assertEquals(Double.class, bindings.get("dec"));

      for (String date : new String[] { "dt", "ts", "tsns", "tstz" })
      {
        Assertions.assertEquals(Date.class, bindings.get(date), date);
      }

      Assertions.assertEquals("geometry", reader.getGeometryColumn());
      Assertions.assertEquals(2, reader.count());

      List<Row> rows = readAll(reader, 0, true, null);

      Assertions.assertEquals(2, rows.size());

      Row row = rows.get(0);

      Assertions.assertEquals(Long.valueOf(1), row.getRowNumber());
      Assertions.assertEquals("text", row.getValue("s"));
      Assertions.assertEquals("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11", row.getValue("u"));
      Assertions.assertEquals(Boolean.TRUE, row.getValue("b"));
      Assertions.assertEquals(Long.valueOf(42), row.getValue("i"));
      Assertions.assertEquals(Long.valueOf(9000000000L), row.getValue("bi"));
      Assertions.assertEquals(BigInteger.valueOf(7), row.getValue("ub"));
      Assertions.assertEquals(new BigInteger("18446744073709551615"), row.getValue("ubmax"));
      Assertions.assertEquals(1.5, row.getValue("d"));
      Assertions.assertEquals(2.5, row.getValue("f"));
      Assertions.assertEquals(12.5, row.getValue("dec"));

      // Dates are midnight UTC, and timestamps are read as UTC
      Assertions.assertEquals(new Date(1579046400000L), row.getValue("dt")); // 2020-01-15T00:00Z
      Assertions.assertEquals(new Date(1579131000000L), row.getValue("ts")); // 2020-01-15T23:30Z
      Assertions.assertEquals(new Date(1579131000000L), row.getValue("tsns"));
      Assertions.assertEquals(new Date(1579131000000L), row.getValue("tstz"));

      Assertions.assertNull(row.getValue("tm"));

      Geometry geometry = row.getGeometry();

      Assertions.assertTrue(geometry instanceof Point);
      Assertions.assertEquals(1.0, ( (Point) geometry ).getX());
      Assertions.assertEquals(2.0, ( (Point) geometry ).getY());

      // getValues() keeps the column order
      Assertions.assertEquals(new ArrayList<>(bindings.keySet()), new ArrayList<>(row.getValues().keySet()));

      // Nulls, including a null geometry
      Row nulls = rows.get(1);

      Assertions.assertEquals(Long.valueOf(2), nulls.getRowNumber());

      for (String column : bindings.keySet())
      {
        Assertions.assertNull(nulls.getValue(column), column);
      }

      Assertions.assertNull(nulls.getGeometry());
    }
    finally
    {
      TimeZone.setDefault(original);
    }
  }

  @Test
  public void testWithoutGeometry() throws Exception
  {
    File file = write(new File(this.directory, "point.parquet"), "SELECT 1 AS id, " + POINT_WKB + " AS geometry");

    try (GeoParquetReader reader = new GeoParquetReader(file))
    {
      Row row = readAll(reader, 0, false, null).get(0);

      Assertions.assertNull(row.getGeometry());
      Assertions.assertEquals(Long.valueOf(1), row.getValue("id"));
    }
  }

  /**
   * A geometry which can't be parsed doesn't stop reading. The error is kept
   * with its row, and reported when the importer asks for the geometry.
   */
  @Test
  public void testCorruptGeometry() throws Exception
  {
    File file = write(new File(this.directory, "corrupt.parquet"), "SELECT i AS id, CASE WHEN i = 1 THEN from_hex('0102') ELSE " + POINT_WKB + " END AS geometry FROM range(0, 3) t(i)");

    try (GeoParquetReader reader = new GeoParquetReader(file))
    {
      List<Row> rows = readAll(reader, 0, true, null);

      Assertions.assertEquals(3, rows.size());

      Assertions.assertNull(rows.get(0).getGeometryError());
      Assertions.assertNotNull(rows.get(0).getGeometry());

      Assertions.assertNotNull(rows.get(1).getGeometryError());
      Assertions.assertEquals(Long.valueOf(2), rows.get(1).getRowNumber());

      Assertions.assertNull(rows.get(2).getGeometryError());
      Assertions.assertNotNull(rows.get(2).getGeometry());

      // Without geometries there is nothing to parse
      Assertions.assertTrue(readAll(reader, 0, false, null).stream().allMatch(row -> row.getGeometryError() == null));
    }
  }

  /**
   * Other geometry columns described by the metadata are not attributes, and
   * neither are bounding box covering columns
   */
  @Test
  public void testGeometryColumnsAreNotAttributes() throws Exception
  {
    String geo = "{\"version\":\"1.1.0\",\"primary_column\":\"geometry\",\"columns\":{" //
        + "\"geometry\":{\"encoding\":\"WKB\",\"geometry_types\":[],\"covering\":{\"bbox\":{\"xmin\":[\"bbox\",\"xmin\"],\"ymin\":[\"bbox\",\"ymin\"],\"xmax\":[\"bbox\",\"xmax\"],\"ymax\":[\"bbox\",\"ymax\"]}}}," //
        + "\"centroid\":{\"encoding\":\"WKB\",\"geometry_types\":[]}}}";

    File file = write(new File(this.directory, "columns.parquet"), "SELECT 'A' AS name, " + POINT_WKB + " AS geometry, " + POINT_WKB + " AS centroid, {'xmin': 1.0, 'ymin': 2.0, 'xmax': 1.0, 'ymax': 2.0} AS bbox", geo, "");

    try (GeoParquetReader reader = new GeoParquetReader(file))
    {
      Assertions.assertEquals(List.of("name"), reader.getColumns().stream().map(Column::getName).toList());
    }
  }

  /**
   * Rows are read in file order across many row groups, and reading can resume
   * from any row
   */
  @Test
  public void testRowOrderAndResume() throws Exception
  {
    int total = 50_000;

    File file = write(new File(this.directory, "rows.parquet"), "SELECT i AS id, " + POINT_WKB + " AS geometry FROM range(0, " + total + ") t(i)", WKB_METADATA, ", ROW_GROUP_SIZE 2048");

    try (GeoParquetReader reader = new GeoParquetReader(file))
    {
      Assertions.assertEquals(total, reader.count());

      List<Row> rows = readAll(reader, 0, false, null);

      Assertions.assertEquals(total, rows.size());

      for (int i = 0; i < total; i++)
      {
        Assertions.assertEquals(Long.valueOf(i + 1), rows.get(i).getRowNumber());
        Assertions.assertEquals(Long.valueOf(i), rows.get(i).getValue("id"));
      }

      // Resume part way through a row group
      List<Row> resumed = readAll(reader, 12_345, false, null);

      Assertions.assertEquals(total - 12_345, resumed.size());
      Assertions.assertEquals(Long.valueOf(12_346), resumed.get(0).getRowNumber());
      Assertions.assertEquals(Long.valueOf(12_345), resumed.get(0).getValue("id"));

      // Past the end
      Assertions.assertTrue(readAll(reader, total, false, null).isEmpty());
    }
  }

  @Test
  public void testLimitAndStop() throws Exception
  {
    File file = write(new File(this.directory, "rows.parquet"), "SELECT i AS id, " + POINT_WKB + " AS geometry FROM range(0, 100) t(i)");

    try (GeoParquetReader reader = new GeoParquetReader(file))
    {
      List<Row> limited = readAll(reader, 10, false, 5L);

      Assertions.assertEquals(List.of(11L, 12L, 13L, 14L, 15L), limited.stream().map(Row::getRowNumber).toList());

      List<Long> stopped = new ArrayList<>();

      reader.read(0, false, null, row -> {
        stopped.add(row.getRowNumber());

        return row.getRowNumber() < 3;
      });

      Assertions.assertEquals(List.of(1L, 2L, 3L), stopped);
    }
  }

  /**
   * A directory, e.g. an extracted zip, is read as one dataset made of every
   * .parquet file under it, in path order. Files added by macOS are ignored.
   */
  @Test
  public void testMultipleParts() throws Exception
  {
    File dataset = new File(this.directory, "dataset");

    // Written out of order, and in a sub folder
    write(new File(dataset, "states/part-1.parquet"), "SELECT i AS id, " + POINT_WKB + " AS geometry FROM range(10, 25) t(i)");
    write(new File(dataset, "states/part-0.parquet"), "SELECT i AS id, " + POINT_WKB + " AS geometry FROM range(0, 10) t(i)");
    write(new File(dataset, "states/part-2.PARQUET"), "SELECT i AS id, " + POINT_WKB + " AS geometry FROM range(25, 30) t(i)");
    write(new File(dataset, "__MACOSX/states/part-3.parquet"), "SELECT i AS id, " + POINT_WKB + " AS geometry FROM range(100, 101) t(i)");
    write(new File(dataset, "states/._part-4.parquet"), "SELECT i AS id, " + POINT_WKB + " AS geometry FROM range(200, 201) t(i)");

    try (GeoParquetReader reader = new GeoParquetReader(dataset))
    {
      Assertions.assertEquals(List.of("part-0.parquet", "part-1.parquet", "part-2.PARQUET"), reader.getPaths().stream().map(p -> new File(p).getName()).toList());
      Assertions.assertEquals(30, reader.count());

      List<Row> rows = readAll(reader, 0, true, null);

      Assertions.assertEquals(30, rows.size());

      for (int i = 0; i < 30; i++)
      {
        // Row numbers continue across the parts
        Assertions.assertEquals(Long.valueOf(i + 1), rows.get(i).getRowNumber());
        Assertions.assertEquals(Long.valueOf(i), rows.get(i).getValue("id"));
        Assertions.assertNotNull(rows.get(i).getGeometry());
      }

      // Resuming and limits cross the boundaries between parts
      Assertions.assertEquals(List.of(9L, 10L, 11L, 12L), readAll(reader, 8, false, 4L).stream().map(Row::getRowNumber).toList());
      Assertions.assertEquals(List.of(26L, 27L, 28L, 29L, 30L), readAll(reader, 25, false, null).stream().map(Row::getRowNumber).toList());
    }
  }

  /**
   * Per the GeoParquet specification, an absent CRS is OGC:CRS84. EPSG:4326 and
   * OGC:CRS84 written as PROJJSON are also accepted. (Rejected projections are
   * covered by GeoParquetServiceTest.)
   */
  @Test
  public void testAcceptedProjections() throws Exception
  {
    String[] crss = new String[] { //
        null, //
        "{\"type\":\"GeographicCRS\",\"name\":\"WGS 84\",\"id\":{\"authority\":\"EPSG\",\"code\":4326}}", //
        "{\"type\":\"GeographicCRS\",\"name\":\"WGS 84 (CRS84)\",\"id\":{\"authority\":\"OGC\",\"code\":\"CRS84\"}}" };

    for (int i = 0; i < crss.length; i++)
    {
      String crs = crss[i] == null ? "" : ",\"crs\":" + crss[i];
      String geo = "{\"version\":\"1.1.0\",\"primary_column\":\"geometry\",\"columns\":{\"geometry\":{\"encoding\":\"WKB\",\"geometry_types\":[]" + crs + "}}}";

      File file = write(new File(this.directory, "crs-" + i + ".parquet"), "SELECT 1 AS id, " + POINT_WKB + " AS geometry", geo, "");

      try (GeoParquetReader reader = new GeoParquetReader(file))
      {
        Assertions.assertDoesNotThrow(reader::validateProjection, String.valueOf(crss[i]));
      }
    }
  }

  /**
   * The reader doesn't depend on DriverManager knowing about DuckDB, which it
   * may not in a web application (or when another JDBC driver was used first).
   * Previously this failed with "No suitable driver found for jdbc:duckdb:".
   */
  @Test
  public void testDoesNotNeedDriverManager() throws Exception
  {
    File file = write(new File(this.directory, "driver.parquet"), "SELECT 1 AS id, " + POINT_WKB + " AS geometry");

    for (Driver driver : Collections.list(DriverManager.getDrivers()))
    {
      if (driver.getClass().getName().startsWith("org.duckdb"))
      {
        DriverManager.deregisterDriver(driver);
      }
    }

    Assertions.assertThrows(SQLException.class, () -> DriverManager.getConnection("jdbc:duckdb:"));

    try (GeoParquetReader reader = new GeoParquetReader(file))
    {
      Assertions.assertEquals(1, reader.count());
    }
  }

  /**
   * The embedded database can only read the files being imported. It can't
   * read other files, change its configuration, or install or load extensions.
   */
  @Test
  public void testDatabaseIsLockedDown() throws Exception
  {
    File allowed = write(new File(this.directory, "allowed.parquet"), "SELECT 1 AS id, " + POINT_WKB + " AS geometry");
    File other = write(new File(this.directory, "other.parquet"), "SELECT 2 AS id, " + POINT_WKB + " AS geometry");

    try (Connection connection = GeoParquetReader.openConnection(List.of(allowed.getCanonicalPath())))
    {
      try (Statement statement = connection.createStatement(); ResultSet results = statement.executeQuery("SELECT count(*) FROM read_parquet('" + allowed.getCanonicalPath() + "')"))
      {
        results.next();

        Assertions.assertEquals(1, results.getLong(1));
      }

      String[] blocked = new String[] { //
          "SELECT count(*) FROM read_parquet('" + other.getCanonicalPath() + "')", //
          "SELECT count(*) FROM read_csv('/etc/passwd')", //
          "SELECT count(*) FROM read_csv('https://example.com/data.csv')", //
          "SET enable_external_access = true", //
          "SET allowed_paths = ['/']", //
          "INSTALL spatial", //
          "LOAD spatial" };

      for (String sql : blocked)
      {
        // Prepared statements report DuckDB's actual error, so check that the
        // statement was blocked by the configuration rather than failing for
        // another reason (e.g. no network access)
        SQLException e = Assertions.assertThrows(SQLException.class, () -> {
          try (var statement = connection.prepareStatement(sql))
          {
            statement.execute();
          }
        }, sql);

        String message = e.getMessage();

        Assertions.assertTrue(message.contains("disabled by configuration") || message.contains("disabled through configuration") || message.contains("configuration has been locked"), sql + ": " + message);
      }
    }
  }
}
