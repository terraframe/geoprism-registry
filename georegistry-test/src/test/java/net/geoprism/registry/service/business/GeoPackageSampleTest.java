/**
 *
 */
package net.geoprism.registry.service.business;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import net.geoprism.registry.etl.upload.GeoPackageImporter;
import net.geoprism.registry.excel.SheetDTO;
import net.geoprism.registry.io.GeoObjectImportConfiguration;

/**
 * Tests describing a GeoPackage's columns for matching
 * ({@link GeoPackageBusinessService#getSheetInformation(File, boolean)}),
 * without a database. The fixture was made with GDAL from the states shapefile,
 * with added DATE, DATETIME and BOOLEAN columns. Errors are covered by
 * GeoPackageServiceTest, because Runway exceptions need a running Runway.
 */
public class GeoPackageSampleTest
{
  private static final String STATES_RESOURCE = "/geopackage/states.gpkg.test";

  @TempDir
  File                        directory;

  private File states() throws Exception
  {
    File file = new File(this.directory, "states.gpkg");

    try (InputStream istream = this.getClass().getResourceAsStream(STATES_RESOURCE))
    {
      Files.copy(istream, file.toPath());
    }

    return file;
  }

  private static SheetDTO read(File file) throws Exception
  {
    return new GeoPackageBusinessService().getSheetInformation(file, false);
  }

  @Test
  public void testColumns() throws Exception
  {
    SheetDTO sheet = read(this.states());
    Map<String, List<String>> attributes = sheet.getAttributes();

    Assertions.assertEquals("states", sheet.getName());

    // Numbers can also be matched to text attributes. The geometry column is
    // left out.
    Assertions.assertEquals(List.of("STATEFP", "STATENS", "AFFGEOID", "GEOID", "STUSPS", "NAME", "LSAD", "ALAND", "AWATER"), attributes.get(GeoObjectImportConfiguration.TEXT));
    Assertions.assertEquals(List.of("ALAND", "AWATER"), attributes.get(GeoObjectImportConfiguration.NUMERIC));
    Assertions.assertEquals(List.of("even"), attributes.get(AttributeBooleanType.TYPE));
    Assertions.assertEquals(List.of("founded", "updated"), attributes.get(AttributeDateType.TYPE));

    // Every state has LSAD "00", while GEOID is unique
    Assertions.assertTrue(sheet.getNonUniqueAttributes().contains("LSAD"));
    Assertions.assertFalse(sheet.getNonUniqueAttributes().contains("GEOID"));
  }

  @Test
  public void testSample() throws Exception
  {
    SheetDTO sheet = read(this.states());

    Assertions.assertEquals(SheetDTO.MAX_SAMPLE_ROWS, sheet.getSample().size());

    Map<String, String> row = sheet.getSample().get(0);

    Assertions.assertEquals("West Virginia", row.get("NAME"));
    Assertions.assertEquals("62265662566", row.get("ALAND"));
    Assertions.assertEquals("true", row.get("even"));
    Assertions.assertEquals("2020-03-09", row.get("founded"));
    Assertions.assertEquals("2020-01-15", row.get("updated"));
    Assertions.assertFalse(row.containsKey("geom"));
  }

  /**
   * GeoTools reads a GeoPackage DATE as midnight in the JVM's time zone. Dates
   * must not depend on the server's time zone, so they are converted to
   * midnight UTC, which is how GPR stores dates.
   */
  @Test
  public void testDatesDoNotDependOnTimeZone() throws Exception
  {
    TimeZone original = TimeZone.getDefault();

    try
    {
      for (String zone : new String[] { "UTC", "America/Denver", "Pacific/Auckland" })
      {
        TimeZone.setDefault(TimeZone.getTimeZone(zone));

        // 2020-01-16 at midnight in the JVM's time zone, as GeoTools reads it
        java.sql.Date date = java.sql.Date.valueOf("2020-01-16");

        Assertions.assertEquals(new Date(1579132800000L), GeoPackageImporter.convert(date), zone);

        // The preview shows the date in the file
        Assertions.assertEquals("2020-03-09", read(this.copy(zone)).getSample().get(0).get("founded"), zone);
      }

      // Timestamps are exact instants, and aren't changed
      java.sql.Timestamp timestamp = new java.sql.Timestamp(1579131000000L);

      Assertions.assertSame(timestamp, GeoPackageImporter.convert(timestamp));
    }
    finally
    {
      TimeZone.setDefault(original);
    }
  }

  private File copy(String name) throws Exception
  {
    File file = new File(this.directory, name.replace('/', '_') + ".gpkg");

    try (InputStream istream = this.getClass().getResourceAsStream(STATES_RESOURCE))
    {
      Files.copy(istream, file.toPath());
    }

    return file;
  }

  /**
   * Columns which can't be matched to an attribute, such as binary columns,
   * are left out instead of failing
   */
  @Test
  public void testUnsupportedColumnsAreSkipped() throws Exception
  {
    File file = this.states();

    try (Connection connection = new org.sqlite.JDBC().connect("jdbc:sqlite:" + file.getAbsolutePath(), new Properties()); Statement statement = connection.createStatement())
    {
      statement.execute("ALTER TABLE states ADD COLUMN thumbnail BLOB");
    }

    SheetDTO sheet = read(file);

    for (List<String> columns : sheet.getAttributes().values())
    {
      Assertions.assertFalse(columns.contains("thumbnail"));
    }

    Assertions.assertFalse(sheet.getSample().get(0).containsKey("thumbnail"));
  }

  /**
   * GeoPackages are SQLite databases. Reading one must not change it.
   */
  @Test
  public void testFileIsNotModified() throws Exception
  {
    File file = this.states();
    byte[] before = Files.readAllBytes(file.toPath());

    read(file);

    Assertions.assertArrayEquals(before, Files.readAllBytes(file.toPath()));

    // No journal files are left behind
    Assertions.assertEquals(List.of("states.gpkg"), List.of(this.directory.list()));
  }
}
