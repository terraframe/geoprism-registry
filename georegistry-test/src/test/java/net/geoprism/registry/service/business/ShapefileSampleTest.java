/**
 *
 */
package net.geoprism.registry.service.business;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.geotools.api.data.FeatureWriter;
import org.geotools.api.data.Transaction;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import net.geoprism.registry.etl.upload.ShapefileImporter;
import net.geoprism.registry.excel.SheetDTO;

/**
 * Tests the import preview sample read from a shapefile
 * ({@link ShapefileBusinessService#getSheetInformation(File, boolean)}). The
 * shapefiles are written to a temporary folder, and no database is needed.
 */
public class ShapefileSampleTest
{
  @TempDir
  Path folder;

  private int count = 0;

  /**
   * Writes a point shapefile with the given attribute types and rows, and
   * returns its .dbf file (which is what the service is given)
   */
  private File shapefile(Map<String, Class<?>> attributes, List<Map<String, Object>> rows) throws IOException
  {
    return shapefile(attributes, rows, StandardCharsets.UTF_8, "UTF-8");
  }

  /**
   * @param charset
   *          Encoding the text is written in
   * @param cpg
   *          Contents of the .cpg file which declares the encoding, as ArcGIS
   *          and GDAL write it, or null for no .cpg file
   */
  private File shapefile(Map<String, Class<?>> attributes, List<Map<String, Object>> rows, Charset charset, String cpg) throws IOException
  {
    String name = "test" + ( count++ );
    File shp = this.folder.resolve(name + ".shp").toFile();

    SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
    builder.setName(name);
    builder.setCRS(DefaultGeographicCRS.WGS84);
    builder.add("the_geom", Point.class);

    attributes.forEach((attribute, type) -> {
      if (type == String.class)
      {
        builder.length(254);
      }

      builder.add(attribute, type);
    });

    SimpleFeatureType type = builder.buildFeatureType();

    Map<String, Serializable> params = new HashMap<>();
    params.put(ShapefileDataStoreFactory.URLP.key, shp.toURI().toURL());

    ShapefileDataStore store = (ShapefileDataStore) new ShapefileDataStoreFactory().createNewDataStore(params);
    store.setCharset(charset);
    // Dates in the file are calendar dates; write them as UTC
    store.setTimeZone(TimeZone.getTimeZone("UTC"));
    store.createSchema(type);

    GeometryFactory factory = new GeometryFactory();

    try (FeatureWriter<SimpleFeatureType, SimpleFeature> writer = store.getFeatureWriterAppend(Transaction.AUTO_COMMIT))
    {
      int i = 0;

      for (Map<String, Object> row : rows)
      {
        SimpleFeature feature = writer.next();
        feature.setDefaultGeometry(factory.createPoint(new Coordinate(-100 + ( i++ ) * 0.01, 40)));
        row.forEach(feature::setAttribute);
        writer.write();
      }
    }
    finally
    {
      store.dispose();
    }

    File cpgFile = this.folder.resolve(name + ".cpg").toFile();
    cpgFile.delete();

    if (cpg != null)
    {
      Files.writeString(cpgFile.toPath(), cpg, StandardCharsets.US_ASCII);
    }

    return this.folder.resolve(name + ".dbf").toFile();
  }

  private static SheetDTO read(File dbf)
  {
    return new ShapefileBusinessService().getSheetInformation(dbf, true);
  }

  private static Map<String, Object> row(Object... keyValues)
  {
    Map<String, Object> row = new LinkedHashMap<>();

    for (int i = 0; i < keyValues.length; i += 2)
    {
      row.put((String) keyValues[i], keyValues[i + 1]);
    }

    return row;
  }

  private static Map<String, Class<?>> attributes(Object... nameTypes)
  {
    Map<String, Class<?>> attributes = new LinkedHashMap<>();

    for (int i = 0; i < nameTypes.length; i += 2)
    {
      attributes.put((String) nameTypes[i], (Class<?>) nameTypes[i + 1]);
    }

    return attributes;
  }

  @Test
  public void testSampleExcludesGeometryAndKeepsColumnOrder() throws IOException
  {
    File dbf = shapefile(attributes("code", String.class, "name", String.class, "pop", Long.class), List.of(row("code", "A", "name", "Alpha", "pop", 5L)));

    SheetDTO sheet = read(dbf);

    Assertions.assertEquals(List.of("code", "name", "pop"), new ArrayList<>(sheet.getSample().get(0).keySet()));
  }

  @Test
  public void testSampleIsCappedAtMaxRows() throws IOException
  {
    List<Map<String, Object>> rows = new ArrayList<>();

    for (int i = 1; i <= 500; i++)
    {
      rows.add(row("code", "F-" + i));
    }

    SheetDTO sheet = read(shapefile(attributes("code", String.class), rows));

    Assertions.assertEquals(SheetDTO.MAX_SAMPLE_ROWS, sheet.getSample().size());
    Assertions.assertEquals("F-1", sheet.getSample().get(0).get("code"));
  }

  @Test
  public void testNoFeatures() throws IOException
  {
    SheetDTO sheet = read(shapefile(attributes("code", String.class), List.of()));

    Assertions.assertTrue(sheet.getSample().isEmpty());
  }

  @Test
  public void testGeometryOnly() throws IOException
  {
    SheetDTO sheet = read(shapefile(attributes(), List.of(row(), row())));

    Assertions.assertTrue(sheet.getSample().isEmpty());
  }

  @Test
  public void testDecimalsUsePlainNotation() throws IOException
  {
    double[] values = { 1234.5, 9999999, 1e7, 12345678, 987654321.25, 0.001, 0.0009, 0.0000123, -45000000.5, 0 };
    String[] expected = { "1234.5", "9999999", "10000000", "12345678", "987654321.25", "0.001", "0.0009", "0.0000123", "-45000000.5", "0" };

    List<Map<String, Object>> rows = new ArrayList<>();

    for (double value : values)
    {
      rows.add(row("value", value));
    }

    SheetDTO sheet = read(shapefile(attributes("value", Double.class), rows));

    for (int i = 0; i < values.length; i++)
    {
      Assertions.assertEquals(expected[i], sheet.getSample().get(i).get("value"), "Value " + values[i]);
    }
  }

  @Test
  public void testNullAttributesAreOmitted() throws IOException
  {
    SheetDTO sheet = read(shapefile(attributes("code", String.class, "maybe", String.class), List.of(row("code", "A"))));

    Assertions.assertEquals(Map.of("code", "A"), sheet.getSample().get(0));
  }

  @Test
  public void testUnicodeAndEmojiAtCut() throws IOException
  {
    // 199 ASCII characters and an emoji fit in the 254 byte DBF field
    String value = "a".repeat(SheetDTO.MAX_SAMPLE_VALUE_LENGTH - 1) + "😀";

    SheetDTO sheet = read(shapefile(attributes("text", String.class, "accents", String.class), List.of(row("text", value, "accents", "Población 人口"))));

    Assertions.assertEquals("a".repeat(SheetDTO.MAX_SAMPLE_VALUE_LENGTH - 1) + "…", sheet.getSample().get(0).get("text"));
    Assertions.assertEquals("Población 人口", sheet.getSample().get(0).get("accents"));
  }

  @Test
  public void testJavaScriptPropertyNamesAsColumns() throws IOException
  {
    SheetDTO sheet = read(shapefile(attributes("toString", String.class, "valueOf", String.class, "__proto__", String.class), List.of(row("toString", "a", "valueOf", "b", "__proto__", "c"))));

    Assertions.assertEquals(Map.of("toString", "a", "valueOf", "b", "__proto__", "c"), sheet.getSample().get(0));
  }

  /**
   * Regression test: GeoTools ignores the .cpg file and reads text as
   * ISO-8859-1, which garbled UTF-8 shapefiles ("Población" became
   * "PoblaciÃ³n")
   */
  @Test
  public void testCpgEncodingIsUsed() throws IOException
  {
    for (String cpg : new String[] { "UTF-8", "utf-8\r\n", "UTF8", "65001" })
    {
      File dbf = shapefile(attributes("name", String.class), List.of(row("name", "Población 人口")), StandardCharsets.UTF_8, cpg);

      Assertions.assertEquals("Población 人口", read(dbf).getSample().get(0).get("name"), "cpg " + cpg);
    }
  }

  @Test
  public void testLatin1Shapefile() throws IOException
  {
    // Without a .cpg file the GeoTools default (ISO-8859-1) is used
    File dbf = shapefile(attributes("name", String.class), List.of(row("name", "Población")), StandardCharsets.ISO_8859_1, null);
    Assertions.assertEquals("Población", read(dbf).getSample().get(0).get("name"));

    dbf = shapefile(attributes("name", String.class), List.of(row("name", "Población")), StandardCharsets.ISO_8859_1, "ISO-8859-1");
    Assertions.assertEquals("Población", read(dbf).getSample().get(0).get("name"));

    dbf = shapefile(attributes("name", String.class), List.of(row("name", "Población")), StandardCharsets.ISO_8859_1, "88591");
    Assertions.assertEquals("Población", read(dbf).getSample().get(0).get("name"));
  }

  @Test
  public void testWindows1252Shapefile() throws IOException
  {
    // The euro sign is 0x80 in windows-1252 but not in ISO-8859-1
    File dbf = shapefile(attributes("price", String.class), List.of(row("price", "€5")), Charset.forName("windows-1252"), "1252");

    Assertions.assertEquals("€5", read(dbf).getSample().get(0).get("price"));
  }

  @Test
  public void testParseCharset()
  {
    Assertions.assertEquals(StandardCharsets.UTF_8, ShapefileImporter.parseCharset("UTF-8"));
    Assertions.assertEquals(StandardCharsets.UTF_8, ShapefileImporter.parseCharset(" utf-8 \n"));
    Assertions.assertEquals(StandardCharsets.UTF_8, ShapefileImporter.parseCharset("UTF8"));
    Assertions.assertEquals(StandardCharsets.UTF_8, ShapefileImporter.parseCharset("65001"));
    Assertions.assertEquals(Charset.forName("windows-1252"), ShapefileImporter.parseCharset("1252"));
    Assertions.assertEquals(Charset.forName("windows-1252"), ShapefileImporter.parseCharset("ANSI 1252"));
    Assertions.assertEquals(StandardCharsets.ISO_8859_1, ShapefileImporter.parseCharset("88591"));
    Assertions.assertEquals(Charset.forName("ISO-8859-15"), ShapefileImporter.parseCharset("885915"));
    Assertions.assertNull(ShapefileImporter.parseCharset(""));
    Assertions.assertNull(ShapefileImporter.parseCharset(null));
    Assertions.assertNull(ShapefileImporter.parseCharset("not a charset"));
  }

  /**
   * Regression test: DBF dates are calendar dates. They used to be read in the
   * JVM's time zone and then formatted in UTC, so on a server east of UTC they
   * showed (and imported) as the day before.
   */
  @Test
  public void testDatesDoNotShiftInOtherTimeZones() throws IOException
  {
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    calendar.clear();
    calendar.set(2020, Calendar.FEBRUARY, 18);

    File dbf = shapefile(attributes("date", java.util.Date.class), List.of(row("date", calendar.getTime())));

    TimeZone original = TimeZone.getDefault();

    for (String zone : new String[] { "Pacific/Auckland", "UTC", "America/Los_Angeles", "Asia/Kolkata" })
    {
      try
      {
        TimeZone.setDefault(TimeZone.getTimeZone(zone));

        Assertions.assertEquals("2020-02-18", read(dbf).getSample().get(0).get("date"), "Time zone " + zone);
      }
      finally
      {
        TimeZone.setDefault(original);
      }
    }
  }
}
