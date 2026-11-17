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
package net.geoprism.registry.etl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import com.runwaysdk.constants.VaultProperties;
import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutionContext;
import com.runwaysdk.system.scheduler.SchedulerManager;

import net.geoprism.data.importer.BasicColumnFunction;
import net.geoprism.registry.InstanceTestClassExtension;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.InvalidProjectionException;
import net.geoprism.registry.UnableToReadProjectionException;
import net.geoprism.registry.USADatasetTest;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.etl.FormatSpecificImporterFactory.FormatImporterType;
import net.geoprism.registry.etl.ObjectImporterFactory.JobHistoryType;
import net.geoprism.registry.etl.upload.GeoParquetReader;
import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.excel.SheetDTO;
import net.geoprism.registry.io.ColumnUniquenessTracker;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.io.InvalidGeometryException;
import net.geoprism.registry.io.PostalCodeFactory;
import net.geoprism.registry.io.view.GeoObjectImportConfigurationDTO;
import net.geoprism.registry.jobs.ImportHistory;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.query.ServerCodeRestriction;
import net.geoprism.registry.query.ServerGeoObjectQuery;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.service.business.GeoParquetBusinessService;
import net.geoprism.registry.service.business.ServiceFactory;
import net.geoprism.registry.service.request.ETLService;
import net.geoprism.registry.service.request.GeoParquetService;
import net.geoprism.registry.service.request.RegistryComponentService;
import net.geoprism.registry.test.SchedulerTestUtils;
import net.geoprism.registry.test.TestAttributeTypeInfo;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.USATestData;
import net.geoprism.registry.view.ImportConfigurationView;

/**
 * Tests the GeoParquet import end to end. Most fixtures are generated from the
 * states file with DuckDB (an unrestricted connection, separate from the
 * importer's locked down one). The reader itself is unit tested by
 * GeoParquetReaderTest.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@ExtendWith(InstanceTestClassExtension.class)
public class GeoParquetServiceTest extends USADatasetTest implements InstanceTestClassListener
{
  private static final String                STATES_RESOURCE   = "/geoparquet/cb_2017_us_state_500k.parquet.test";

  private static final String                STATES_FILE_NAME  = "cb_2017_us_state_500k.parquet";

  private static final String                MERCATOR_RESOURCE = "/geoparquet/mercator.parquet.test";

  /**
   * WKB for POINT (1 2)
   */
  private static final String                POINT_WKB         = "from_hex('0101000000000000000000F03F0000000000000040')";

  private static final int                   STATE_COUNT       = 56;

  private static final TestAttributeTypeInfo testInteger       = new TestAttributeTypeInfo("parquetInteger", "parquetIntegerLocalName", USATestData.STATE, AttributeIntegerType.TYPE);

  private static final TestAttributeTypeInfo testBoolean       = new TestAttributeTypeInfo("parquetBoolean", "parquetBooleanLocalName", USATestData.STATE, AttributeBooleanType.TYPE);

  private static final TestAttributeTypeInfo testDate          = new TestAttributeTypeInfo("parquetDate", "parquetDateLocalName", USATestData.STATE, AttributeDateType.TYPE);

  @Autowired
  private RegistryComponentService           service;

  @Autowired
  private GeoObjectBusinessServiceIF         objectService;

  @Autowired
  private ETLService                         etlService;

  @Autowired
  private GeoParquetService                  geoParquetService;

  @Autowired
  private GeoParquetBusinessService          geoParquetBusinessService;

  /**
   * Scratch directory for generated fixtures, deleted after each test
   */
  private File                               directory;

  /**
   * Copy of the states file, which generated fixtures are derived from
   */
  private File                               states;

  @Override
  @Request
  public void beforeClassSetup() throws Exception
  {
    TestDataSet.deleteAllSchedulerData();

    super.beforeClassSetup();

    testInteger.apply();
    testBoolean.apply();
    testDate.apply();

    if (!SchedulerManager.initialized())
    {
      SchedulerManager.start();
    }

    ServiceFactory.getMetadataCache().getAllGeoObjectTypes().forEach(t -> t.markAsDirty());
  }

  @Override
  public void afterClassSetup() throws Exception
  {
    super.afterClassSetup();
    FileUtils.deleteDirectory(new File(VaultProperties.getPath("vault.default"), "files"));
  }

  @BeforeEach
  public void setUp() throws Exception
  {
    super.setUp();

    clearData();

    this.directory = Files.createTempDirectory("geoparquet").toFile();
    this.states = new File(this.directory, STATES_FILE_NAME);

    try (InputStream istream = this.getClass().getResourceAsStream(STATES_RESOURCE))
    {
      Files.copy(istream, this.states.toPath());
    }
  }

  @AfterEach
  public void tearDown() throws Exception
  {
    PostalCodeFactory.clear();

    clearData();

    FileUtils.deleteQuietly(this.directory);

    super.tearDown();
  }

  @Request
  private void clearData()
  {
    SchedulerTestUtils.clearImportData();

    TestGeoObjectInfo parent = new TestGeoObjectInfo("00", USATestData.COUNTRY, USATestData.SOURCE);
    parent.delete();

    // Clear out the event table
    Database.deleteWhere("domainevententry", "true");
  }

  /*
   * Configuration
   */

  @Test
  @Request
  public void testGetConfiguration() throws Exception
  {
    try (InputStream istream = this.getClass().getResourceAsStream(STATES_RESOURCE))
    {
      Assertions.assertNotNull(istream);

      GeoObjectImportConfigurationDTO dto = this.geoParquetService.getConfiguration(testData.clientRequest.getSessionId(), STATES_FILE_NAME, istream, getView());

      Assertions.assertEquals(FormatImporterType.GEOPARQUET, dto.getFormatType());
      Assertions.assertFalse(dto.getPostalCode());

      SheetDTO sheet = dto.getSheet();

      Assertions.assertNotNull(sheet);
      Assertions.assertEquals("cb_2017_us_state_500k", sheet.getName());

      Map<String, List<String>> attributes = sheet.getAttributes();

      // 7 string columns plus the 2 numeric columns. The geometry column and
      // the bounding box struct column written by GDAL are excluded.
      List<String> fields = attributes.get(GeoObjectImportConfiguration.TEXT);

      Assertions.assertEquals(9, fields.size());
      Assertions.assertEquals("STATEFP", fields.get(0));
      Assertions.assertFalse(fields.contains("geometry"));
      Assertions.assertFalse(fields.contains("geometry_bbox"));

      Assertions.assertEquals(2, attributes.get(GeoObjectImportConfiguration.NUMERIC).size());
      Assertions.assertEquals(0, attributes.get(AttributeBooleanType.TYPE).size());
      Assertions.assertEquals(0, attributes.get(AttributeDateType.TYPE).size());

      // Every state has LSAD "00", while GEOID is unique
      Assertions.assertTrue(sheet.getNonUniqueAttributes().contains("LSAD"));
      Assertions.assertFalse(sheet.getNonUniqueAttributes().contains("GEOID"));

      // The preview sample holds the first rows of the file, without geometry
      List<Map<String, String>> sample = sheet.getSample();

      Assertions.assertEquals(SheetDTO.MAX_SAMPLE_ROWS, sample.size());
      Assertions.assertEquals("West Virginia", sample.get(0).get("NAME"));
      Assertions.assertEquals("54", sample.get(0).get("GEOID"));
      Assertions.assertEquals("62265662566", sample.get(0).get("ALAND"));
      Assertions.assertEquals("Illinois", sample.get(1).get("NAME"));
      Assertions.assertFalse(sample.get(0).containsKey("geometry"));
    }
  }

  /**
   * Every supported column type is offered for the matching attribute types,
   * and unsupported types (TIME, LIST, STRUCT) are left out
   */
  @Test
  @Request
  public void testGetConfigurationColumnTypes() throws Exception
  {
    File file = this.write("types.parquet", "SELECT 'text' AS s, true AS b, 42::INTEGER AS i, 9000000000::BIGINT AS bi, 7::UBIGINT AS ub, 1.5::DOUBLE AS d, 12.50::DECIMAL(10,2) AS dec, " //
        + "DATE '2020-01-15' AS dt, TIMESTAMP '2020-01-15 23:30:00' AS ts, '2020-01-15 23:30:00'::TIMESTAMP_NS AS tsns, TIMESTAMPTZ '2020-01-15 23:30:00+00' AS tstz, " //
        + "TIME '12:00:00' AS tm, [1, 2] AS list, {'a': 1} AS struct, " + POINT_WKB + " AS geometry");

    SheetDTO sheet = this.getConfiguration(file).getSheet();
    Map<String, List<String>> attributes = sheet.getAttributes();

    // Numbers can also be matched to text attributes
    Assertions.assertEquals(List.of("s", "i", "bi", "ub", "d", "dec"), attributes.get(GeoObjectImportConfiguration.TEXT));
    Assertions.assertEquals(List.of("i", "bi", "ub", "d", "dec"), attributes.get(GeoObjectImportConfiguration.NUMERIC));
    Assertions.assertEquals(List.of("b"), attributes.get(AttributeBooleanType.TYPE));
    Assertions.assertEquals(List.of("dt", "ts", "tsns", "tstz"), attributes.get(AttributeDateType.TYPE));

    for (List<String> columns : attributes.values())
    {
      Assertions.assertFalse(columns.contains("tm"));
      Assertions.assertFalse(columns.contains("list"));
      Assertions.assertFalse(columns.contains("struct"));
      Assertions.assertFalse(columns.contains("geometry"));
    }

    // Values are formatted for the preview. Dates are shown in UTC.
    Map<String, String> row = sheet.getSample().get(0);

    Assertions.assertEquals("text", row.get("s"));
    Assertions.assertEquals("true", row.get("b"));
    Assertions.assertEquals("12.5", row.get("dec"));
    Assertions.assertEquals("2020-01-15", row.get("dt"));
    Assertions.assertEquals("2020-01-15", row.get("tstz"));
  }

  /**
   * Duplicates are only looked for in the first
   * {@link ColumnUniquenessTracker#MAX_ROWS} rows
   */
  @Test
  @Request
  public void testGetConfigurationUniquenessLimit() throws Exception
  {
    int max = ColumnUniquenessTracker.MAX_ROWS;

    // "early" repeats a value within the scanned rows, "late" only repeats a
    // value in the first row after them, and "unique" never repeats
    File file = this.write("uniqueness.parquet", "SELECT i AS \"unique\", CASE WHEN i = 1 THEN 0 ELSE i END AS early, CASE WHEN i = " + max + " THEN 0 ELSE i END AS late, " + POINT_WKB + " AS geometry FROM range(0, " + ( max + 1 ) + ") t(i)");

    SheetDTO sheet = this.getConfiguration(file).getSheet();

    Assertions.assertTrue(sheet.getNonUniqueAttributes().contains("early"));
    Assertions.assertFalse(sheet.getNonUniqueAttributes().contains("late"));
    Assertions.assertFalse(sheet.getNonUniqueAttributes().contains("unique"));
  }

  /**
   * A zip of a dataset split into several files, inside a folder and with the
   * extra files macOS adds when zipping, is read as one dataset.
   */
  @Test
  @Request
  public void testGetConfigurationPartitionedZip() throws Exception
  {
    File zip = this.createPartitionedZip(false, null);

    SheetDTO sheet = this.getConfiguration(zip).getSheet();

    Assertions.assertEquals(9, sheet.getAttributes().get(GeoObjectImportConfiguration.TEXT).size());

    // Uniqueness is checked across all of the parts
    Assertions.assertTrue(sheet.getNonUniqueAttributes().contains("LSAD"));
    Assertions.assertFalse(sheet.getNonUniqueAttributes().contains("GEOID"));

    Assertions.assertEquals(SheetDTO.MAX_SAMPLE_ROWS, sheet.getSample().size());
    Assertions.assertEquals("West Virginia", sheet.getSample().get(0).get("NAME"));
  }

  /**
   * Extensions are matched without regard to case, as zips made on Windows
   * often use upper case names
   */
  @Test
  @Request
  public void testGetConfigurationUpperCaseZip() throws Exception
  {
    File zip = this.zip("STATES.ZIP", StandardCharsets.UTF_8, Map.of("STATES.PARQUET", this.states));

    SheetDTO sheet = this.getConfiguration(zip).getSheet();

    Assertions.assertEquals(9, sheet.getAttributes().get(GeoObjectImportConfiguration.TEXT).size());
  }

  /*
   * Projections
   */

  @Test
  @Request
  public void testInvalidProjection() throws Exception
  {
    try (InputStream istream = this.getClass().getResourceAsStream(MERCATOR_RESOURCE))
    {
      Assertions.assertNotNull(istream);

      Assertions.assertThrows(InvalidProjectionException.class, () -> {
        this.geoParquetBusinessService.getConfiguration("mercator.parquet", istream, getView(), false);
      });
    }
  }

  @Test
  @Request
  public void testIgnoreProjection() throws Exception
  {
    try (InputStream istream = this.getClass().getResourceAsStream(MERCATOR_RESOURCE))
    {
      GeoObjectImportConfigurationDTO dto = this.geoParquetBusinessService.getConfiguration("mercator.parquet", istream, getView(), true);

      Assertions.assertTrue(dto.getIgnoreProjection());
      Assertions.assertNotNull(dto.getSheet());
    }
  }

  /**
   * A null "crs" means the CRS is unknown, per the GeoParquet specification
   */
  @Test
  @Request
  public void testUnknownProjection() throws Exception
  {
    File file = this.write("unknown.parquet", "SELECT 1 AS id, " + POINT_WKB + " AS geometry", metadata("WKB", "geometry", ",\"crs\":null"));

    Assertions.assertThrows(UnableToReadProjectionException.class, () -> this.getConfiguration(file));
  }

  @Test
  @Request
  public void testWgs84Projection() throws Exception
  {
    String crs = ",\"crs\":{\"type\":\"GeographicCRS\",\"name\":\"WGS 84\",\"id\":{\"authority\":\"EPSG\",\"code\":4326}}";

    File file = this.write("wgs84.parquet", "SELECT 1 AS id, " + POINT_WKB + " AS geometry", metadata("WKB", "geometry", crs));

    Assertions.assertNotNull(this.getConfiguration(file).getSheet());
  }

  /*
   * Files which can't be imported. These are reported with an
   * ImportFileFormatException whose details say what is wrong.
   */

  @Test
  @Request
  public void testGetConfigurationMismatchedParts() throws Exception
  {
    File zip = this.createPartitionedZip(true, null);

    ImportFileFormatException ex = this.assertFileError(zip);

    // The message names both files, so the user knows which one differs
    Assertions.assertTrue(ex.getDetails().contains("[part-2.parquet]"), ex.getDetails());
    Assertions.assertTrue(ex.getDetails().contains("[part-0.parquet]"), ex.getDetails());
    Assertions.assertTrue(ex.getLocalizedMessage().contains(ex.getDetails()), ex.getLocalizedMessage());
  }

  /**
   * A Parquet file without GeoParquet metadata
   */
  @Test
  @Request
  public void testGetConfigurationPlainParquet() throws Exception
  {
    File file = this.write("plain.parquet", "SELECT 1 AS id, 'A' AS name", null);

    ImportFileFormatException ex = this.assertFileError(file);

    Assertions.assertTrue(ex.getDetails().contains("[plain.parquet]"), ex.getDetails());
    Assertions.assertTrue(ex.getDetails().contains("GeoParquet metadata"), ex.getDetails());
  }

  /**
   * GeoParquet 1.1 native (GeoArrow) encodings aren't supported
   */
  @Test
  @Request
  public void testGetConfigurationUnsupportedEncoding() throws Exception
  {
    File file = this.write("native.parquet", "SELECT 1 AS id, {'x': 1.0, 'y': 2.0} AS geometry", metadata("point", "geometry", ""));

    ImportFileFormatException ex = this.assertFileError(file);

    Assertions.assertTrue(ex.getDetails().contains("[point]"), ex.getDetails());
  }

  @Test
  @Request
  public void testGetConfigurationMissingPrimaryColumn() throws Exception
  {
    File file = this.write("missing.parquet", "SELECT 1 AS id, " + POINT_WKB + " AS geom", metadata("WKB", "geometry", ""));

    ImportFileFormatException ex = this.assertFileError(file);

    Assertions.assertTrue(ex.getDetails().contains("[geometry] does not exist"), ex.getDetails());
  }

  @Test
  @Request
  public void testGetConfigurationTextGeometryColumn() throws Exception
  {
    File file = this.write("wkt.parquet", "SELECT 1 AS id, 'POINT (1 2)' AS geometry", metadata("WKB", "geometry", ""));

    ImportFileFormatException ex = this.assertFileError(file);

    Assertions.assertTrue(ex.getDetails().contains("unsupported type [VARCHAR]"), ex.getDetails());
  }

  /**
   * A file which isn't Parquet at all. DuckDB's message is passed on, without
   * the server's temporary file paths.
   */
  @Test
  @Request
  public void testGetConfigurationNotParquet() throws Exception
  {
    File file = new File(this.directory, "fake.parquet");
    Files.writeString(file.toPath(), "This is not a parquet file");

    ImportFileFormatException ex = this.assertFileError(file);

    Assertions.assertTrue(ex.getDetails().contains("fake.parquet"), ex.getDetails());
    Assertions.assertFalse(ex.getDetails().contains(File.separator + "fake.parquet"), ex.getDetails());
  }

  /**
   * DuckDB can't add its row number column when the file already has a column
   * with the same name. The user sees DuckDB's explanation.
   */
  @Test
  @Request
  public void testGetConfigurationRowNumberColumn() throws Exception
  {
    File file = this.write("rownumber.parquet", "SELECT 'x' AS file_row_number, " + POINT_WKB + " AS geometry");

    ImportFileFormatException ex = this.assertFileError(file);

    Assertions.assertTrue(ex.getDetails().contains("file_row_number"), ex.getDetails());
  }

  @Test
  @Request
  public void testGetConfigurationZipWithoutParquet() throws Exception
  {
    File text = new File(this.directory, "readme.txt");
    Files.writeString(text.toPath(), "Not a parquet file");

    ImportFileFormatException ex = this.assertFileError(this.zip("readme.zip", StandardCharsets.UTF_8, Map.of("readme.txt", text)));

    Assertions.assertTrue(ex.getDetails().contains(".parquet"), ex.getDetails());
  }

  /**
   * Windows' built in zip tool writes non-ASCII file names in the OEM code page
   * (e.g. cp437) without marking them, which Java can't read. The upload fails,
   * but the user is told why.
   */
  @Test
  @Request
  public void testGetConfigurationWindowsZip() throws Exception
  {
    File zip = this.zip("windows.zip", Charset.forName("Cp437"), Map.of("Données.parquet", this.states));

    ImportFileFormatException ex = this.assertFileError(zip);

    Assertions.assertTrue(ex.getDetails().contains("invalid LOC header"), ex.getDetails());
  }

  @Test
  @Request
  public void testGetConfigurationWrongExtension() throws Exception
  {
    File file = new File(this.directory, "states.csv");
    Files.copy(this.states.toPath(), file.toPath());

    ImportFileFormatException ex = this.assertFileError(file);

    Assertions.assertTrue(ex.getDetails().contains("[states.csv]"), ex.getDetails());
  }

  /*
   * Imports
   */

  @Test
  @Request
  public void testImport() throws Throwable
  {
    try (InputStream istream = this.getClass().getResourceAsStream(STATES_RESOURCE))
    {
      Assertions.assertNotNull(istream);

      GeoObjectImportConfiguration config = this.getTestConfiguration(istream);
      config.setHierarchy(ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode()));

      ImportHistory hist = mockImport(config);
      Assertions.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

      hist = ImportHistory.get(hist.getOid());
      Assertions.assertEquals(Long.valueOf(STATE_COUNT), hist.getWorkTotal());
      Assertions.assertEquals(Long.valueOf(STATE_COUNT), hist.getWorkProgress());
      Assertions.assertEquals(Long.valueOf(STATE_COUNT), hist.getImportedRecords());
      Assertions.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

      GeoObject object = this.service.getGeoObjectByCode(testData.clientRequest.getSessionId(), "01", USATestData.STATE.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

      Assertions.assertNotNull(object);
      Assertions.assertNotNull(object.getGeometry());
      Assertions.assertEquals("Alabama", object.getLocalizedDisplayLabel());
      Assertions.assertEquals(true, object.getExists());
    }
  }

  /**
   * The imported geometry matches the file's, converted to the type's geometry
   * type (MultiPolygon)
   */
  @Test
  @Request
  public void testImportGeometry() throws Throwable
  {
    this.importFile(this.states, ImportStrategy.NEW_AND_UPDATE, Map.of());

    // West Virginia is a Polygon in the file, which is converted to a
    // MultiPolygon. Alabama and Puerto Rico are already MultiPolygons.
    for (String code : new String[] { "54", "01", "72" })
    {
      Geometry expected = this.readGeometry(code);
      Geometry actual = this.getGeoObject(code).getGeometry();

      Assertions.assertEquals("MultiPolygon", actual.getGeometryType(), code);
      Assertions.assertEquals(expected.getNumGeometries(), actual.getNumGeometries(), code);
      Assertions.assertEquals(expected.getArea(), actual.getArea(), expected.getArea() * 1e-9, code);
      // Allow for rounding when the geometry is stored
      Assertions.assertEquals(expected.getEnvelopeInternal().getMinX(), actual.getEnvelopeInternal().getMinX(), 1e-9, code);
      Assertions.assertEquals(expected.getEnvelopeInternal().getMinY(), actual.getEnvelopeInternal().getMinY(), 1e-9, code);
      Assertions.assertEquals(expected.getEnvelopeInternal().getMaxX(), actual.getEnvelopeInternal().getMaxX(), 1e-9, code);
      Assertions.assertEquals(expected.getEnvelopeInternal().getMaxY(), actual.getEnvelopeInternal().getMaxY(), 1e-9, code);
    }
  }

  /**
   * Integer, boolean and date columns are imported into attributes of those
   * types
   */
  @Test
  @Request
  public void testImportAttributeValues() throws Throwable
  {
    File file = this.writeFromStates("values.parquet", "SELECT *, ALAND::BIGINT AS land, (STATEFP::INTEGER % 2 = 0) AS even, DATE '2020-01-15' + STATEFP::INTEGER AS founded FROM " + this.statesTable());

    this.importFile(file, ImportStrategy.NEW_AND_UPDATE, Map.of(testInteger.getName(), "land", testBoolean.getName(), "even", testDate.getName(), "founded"));

    ServerGeoObjectIF alabama = this.getGeoObject("01");

    Assertions.assertEquals(Long.valueOf(131174431216L), alabama.getValue(testInteger.getName(), TestDataSet.DEFAULT_OVER_TIME_DATE));
    Assertions.assertEquals(Boolean.FALSE, alabama.getValue(testBoolean.getName(), TestDataSet.DEFAULT_OVER_TIME_DATE));

    // 2020-01-15 plus 1 day, at midnight UTC
    Assertions.assertEquals(new Date(1579132800000L), alabama.getValue(testDate.getName(), TestDataSet.DEFAULT_OVER_TIME_DATE));

    ServerGeoObjectIF alaska = this.getGeoObject("02");

    Assertions.assertEquals(Boolean.TRUE, alaska.getValue(testBoolean.getName(), TestDataSet.DEFAULT_OVER_TIME_DATE));
  }

  /**
   * A row without a geometry is imported without one
   */
  @Test
  @Request
  public void testImportNullGeometry() throws Throwable
  {
    File file = this.writeFromStates("nullgeometry.parquet", "SELECT * REPLACE (CASE WHEN GEOID = '01' THEN NULL ELSE geometry END AS geometry) FROM " + this.statesTable());

    ImportHistory hist = this.importFile(file, ImportStrategy.NEW_AND_UPDATE, Map.of());

    Assertions.assertEquals(Long.valueOf(STATE_COUNT), hist.getImportedRecords());
    Assertions.assertNull(this.getGeoObject("01").getGeometry());
    Assertions.assertNotNull(this.getGeoObject("54").getGeometry());
  }

  @Test
  @Request
  public void testImportPartitionedZip() throws Throwable
  {
    File zip = this.createPartitionedZip(false, null);

    ImportHistory hist = this.importFile(zip, ImportStrategy.NEW_AND_UPDATE, Map.of());

    // Every row of every part is imported
    Assertions.assertEquals(AllJobStatus.SUCCESS, hist.getStatus().get(0));
    Assertions.assertEquals(Long.valueOf(STATE_COUNT), hist.getWorkTotal());
    Assertions.assertEquals(Long.valueOf(STATE_COUNT), hist.getWorkProgress());
    Assertions.assertEquals(Long.valueOf(STATE_COUNT), hist.getImportedRecords());
    Assertions.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    // One state from each part: West Virginia (first row of the first part),
    // Rhode Island (second part) and Alabama (third part)
    for (String code : new String[] { "54", "44", "01" })
    {
      ServerGeoObjectIF object = this.getGeoObject(code);

      Assertions.assertNotNull(object.getGeometry(), code);
    }
  }

  /**
   * Validation problems report the row number of the bad row, counted across
   * all the parts of a dataset
   */
  @Test
  @Request
  public void testValidationProblemRowNumber() throws Throwable
  {
    // Row 25 is the 5th row of the second part
    File zip = this.createPartitionedZip(false, "CASE WHEN file_row_number = 24 THEN 'abc' ELSE CAST(file_row_number AS VARCHAR) END AS count");

    ImportHistory hist = this.importFile(zip, ImportStrategy.NEW_AND_UPDATE, Map.of(testInteger.getName(), "count"));

    Assertions.assertEquals(AllJobStatus.FEEDBACK, hist.getStatus().get(0));
    Assertions.assertEquals(ImportStage.VALIDATION_RESOLVE, hist.getStage().get(0));
    Assertions.assertEquals(Long.valueOf(0), hist.getImportedRecords());

    JSONObject page = new JSONObject(this.etlService.getValidationProblems(testData.clientRequest.getSessionId(), hist.getOid(), false, 100, 1).toString());
    JSONArray results = page.getJSONArray("resultSet");

    Assertions.assertEquals(1, results.length());
    Assertions.assertEquals("25", results.getJSONObject(0).getString("affectedRows"));
  }

  @Test
  @Request
  public void testImportNewOnly() throws Throwable
  {
    ImportHistory hist = this.importFile(this.states, ImportStrategy.NEW_ONLY, Map.of());

    Assertions.assertEquals(AllJobStatus.SUCCESS, hist.getStatus().get(0));
    Assertions.assertEquals(Long.valueOf(STATE_COUNT), hist.getImportedRecords());

    // The states now exist, so importing them again as new fails
    ImportHistory again = this.importFile(this.states, ImportStrategy.NEW_ONLY, Map.of());

    Assertions.assertEquals(AllJobStatus.FEEDBACK, again.getStatus().get(0));
    Assertions.assertEquals(Long.valueOf(0), again.getImportedRecords());
  }

  @Test
  @Request
  public void testImportUpdateOnly() throws Throwable
  {
    // Nothing exists to update yet
    ImportHistory hist = this.importFile(this.states, ImportStrategy.UPDATE_ONLY, Map.of());

    Assertions.assertEquals(AllJobStatus.FEEDBACK, hist.getStatus().get(0));
    Assertions.assertEquals(Long.valueOf(0), hist.getImportedRecords());
    Assertions.assertFalse(this.exists("01"));

    // Create the states, then update their names
    this.importFile(this.states, ImportStrategy.NEW_AND_UPDATE, Map.of());

    File renamed = this.writeFromStates("renamed.parquet", "SELECT * REPLACE (NAME || ' (updated)' AS NAME) FROM " + this.statesTable());

    ImportHistory update = this.importFile(renamed, ImportStrategy.UPDATE_ONLY, Map.of());

    Assertions.assertEquals(AllJobStatus.SUCCESS, update.getStatus().get(0));
    Assertions.assertEquals(Long.valueOf(STATE_COUNT), update.getImportedRecords());

    GeoObject object = this.service.getGeoObjectByCode(testData.clientRequest.getSessionId(), "01", USATestData.STATE.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assertions.assertEquals("Alabama (updated)", object.getLocalizedDisplayLabel());
  }

  /**
   * A geometry which can't be parsed is a problem with its row, like any other
   * invalid geometry. It doesn't stop the other rows from being validated.
   */
  @Test
  @Request
  public void testImportCorruptGeometry() throws Throwable
  {
    File file = this.writeFromStates("corrupt.parquet", "SELECT * REPLACE (CASE WHEN file_row_number = 2 THEN from_hex('0102') ELSE geometry END AS geometry) FROM " + this.statesTable());

    ImportHistory hist = this.importFile(file, ImportStrategy.NEW_AND_UPDATE, Map.of());

    Assertions.assertEquals(AllJobStatus.FEEDBACK, hist.getStatus().get(0));
    Assertions.assertEquals(ImportStage.VALIDATION_RESOLVE, hist.getStage().get(0));
    Assertions.assertEquals(Long.valueOf(STATE_COUNT), hist.getWorkProgress());
    Assertions.assertEquals(Long.valueOf(0), hist.getImportedRecords());

    JSONObject page = new JSONObject(this.etlService.getValidationProblems(testData.clientRequest.getSessionId(), hist.getOid(), false, 100, 1).toString());
    JSONArray results = page.getJSONArray("resultSet");

    Assertions.assertEquals(1, results.length());

    JSONObject problem = results.getJSONObject(0);

    Assertions.assertEquals("3", problem.getString("affectedRows"));
    Assertions.assertEquals(InvalidGeometryException.CLASS, problem.getJSONObject("exception").getString("type"));
  }

  /*
   * Helpers
   */

  /**
   * @return The states file as a DuckDB table expression, with a
   *         file_row_number column (zero based)
   */
  private String statesTable()
  {
    return "read_parquet('" + this.states.getAbsolutePath() + "', file_row_number = true)";
  }

  /**
   * @return GeoParquet metadata for a single geometry column
   */
  private static String metadata(String encoding, String column, String extra)
  {
    return "{\"version\":\"1.1.0\",\"primary_column\":\"" + column + "\",\"columns\":{\"" + column + "\":{\"encoding\":\"" + encoding + "\",\"geometry_types\":[]" + extra + "}}}";
  }

  /**
   * Writes a query over {@link #statesTable()} to a GeoParquet file, in the
   * states file's row order. The query must include the file_row_number
   * column, which is used for the order but not written.
   */
  private File writeFromStates(String name, String select) throws Exception
  {
    return this.write(name, "SELECT * EXCLUDE (file_row_number) FROM (" + select + ") ORDER BY file_row_number");
  }

  /**
   * Writes a query to a GeoParquet file, with the states file's GeoParquet
   * metadata
   */
  private File write(String name, String select) throws Exception
  {
    String geo;

    try (Connection connection = GeoParquetReader.connect(new Properties()); Statement statement = connection.createStatement(); ResultSet results = statement.executeQuery("SELECT decode(value) FROM parquet_kv_metadata('" + this.states.getAbsolutePath() + "') WHERE decode(key) = 'geo'"))
    {
      results.next();

      geo = results.getString(1);
    }

    return this.write(name, select, geo);
  }

  /**
   * Writes a query to a Parquet file
   *
   * @param geo
   *          GeoParquet metadata, or null for a plain Parquet file
   */
  private File write(String name, String select, String geo) throws Exception
  {
    File file = new File(this.directory, name);

    try (Connection connection = GeoParquetReader.connect(new Properties()); Statement statement = connection.createStatement())
    {
      statement.execute("SET enable_geoparquet_conversion = false");

      String metadata = geo == null ? "" : ", KV_METADATA {geo: '" + geo.replace("'", "''") + "'}";

      statement.execute("COPY (" + select + ") TO '" + file.getAbsolutePath() + "' (FORMAT parquet" + metadata + ")");
    }

    return file;
  }

  /**
   * Zips files, with the given encoding for the entry names
   */
  private File zip(String name, Charset charset, Map<String, File> entries) throws Exception
  {
    File zip = new File(this.directory, name);

    try (ZipOutputStream ostream = new ZipOutputStream(new FileOutputStream(zip), charset))
    {
      for (Map.Entry<String, File> entry : entries.entrySet())
      {
        ostream.putNextEntry(new ZipEntry(entry.getKey()));
        Files.copy(entry.getValue().toPath(), ostream);
        ostream.closeEntry();
      }
    }

    return zip;
  }

  /**
   * Splits the states file into three parts (rows 1-20, 21-40 and 41-56) and
   * zips them inside a "states" folder, along with the __MACOSX folder and ._
   * files that macOS adds to zips, which must be ignored.
   *
   * @param mismatched
   *          If true the last part has an extra column, so the parts don't
   *          share a schema
   * @param column
   *          An extra column expression to add to every part, or null
   */
  private File createPartitionedZip(boolean mismatched, String column) throws Exception
  {
    String[] filters = new String[] { "file_row_number < 20", "file_row_number >= 20 AND file_row_number < 40", "file_row_number >= 40" };
    String extraColumn = column == null ? "" : ", " + column;

    Map<String, File> entries = new LinkedHashMap<>();

    // Add the parts out of order: they are read in name order
    for (int i : new int[] { 1, 0, 2 })
    {
      String extra = ( mismatched && i == filters.length - 1 ) ? ", 1 AS extra" : "";

      File part = this.writeFromStates("part-" + i + ".parquet", "SELECT *" + extraColumn + extra + " FROM " + this.statesTable() + " WHERE " + filters[i]);

      entries.put("states/" + part.getName(), part);
    }

    // macOS resource forks, which are not parquet files
    File fork = new File(this.directory, "fork");
    Files.write(fork.toPath(), new byte[] { 0, 5, 22, 7 });

    entries.put("__MACOSX/states/._part-0.parquet", fork);
    entries.put("states/._part-0.parquet", fork);

    File zip = new File(this.directory, "states.zip");

    try (ZipOutputStream ostream = new ZipOutputStream(new FileOutputStream(zip)))
    {
      for (String folder : new String[] { "states/", "__MACOSX/", "__MACOSX/states/" })
      {
        ostream.putNextEntry(new ZipEntry(folder));
        ostream.closeEntry();
      }

      for (Map.Entry<String, File> entry : entries.entrySet())
      {
        ostream.putNextEntry(new ZipEntry(entry.getKey()));
        Files.copy(entry.getValue().toPath(), ostream);
        ostream.closeEntry();
      }
    }

    return zip;
  }

  private GeoObjectImportConfigurationDTO getConfiguration(File file) throws Exception
  {
    try (InputStream istream = new FileInputStream(file))
    {
      return this.geoParquetBusinessService.getConfiguration(file.getName(), istream, getView(), false);
    }
  }

  private ImportFileFormatException assertFileError(File file)
  {
    return Assertions.assertThrows(ImportFileFormatException.class, () -> this.getConfiguration(file));
  }

  /**
   * @return The geometry of the state in the original file
   */
  private Geometry readGeometry(String code) throws Exception
  {
    Geometry[] geometry = new Geometry[1];

    try (GeoParquetReader reader = new GeoParquetReader(this.states))
    {
      reader.read(0, true, null, row -> {
        if (code.equals(row.getValue("GEOID")))
        {
          geometry[0] = row.getGeometry();

          return false;
        }

        return true;
      });
    }

    Assertions.assertNotNull(geometry[0], code);

    return geometry[0];
  }

  private boolean exists(String code)
  {
    ServerGeoObjectQuery query = this.objectService.createQuery(USATestData.STATE.getServerObject(), TestDataSet.DEFAULT_OVER_TIME_DATE);
    query.setRestriction(new ServerCodeRestriction(USATestData.STATE.getServerObject(), code));

    return query.getSingleResult() != null;
  }

  private ServerGeoObjectIF getGeoObject(String code)
  {
    ServerGeoObjectIF object = this.objectService.getGeoObjectByCode(code, USATestData.STATE.getCode());

    Assertions.assertNotNull(object, code);

    return object;
  }

  private ImportConfigurationView getView()
  {
    return ImportConfigurationView.of(JobHistoryType.GEO_OBJECT, USATestData.STATE.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, USATestData.SOURCE.getCode(), ImportStrategy.NEW_AND_UPDATE, false);
  }

  /**
   * Imports a file, matching NAME to the display label and GEOID to the code
   *
   * @param columns
   *          Additional attribute name to column matches
   * @return The refreshed history of the import
   */
  private ImportHistory importFile(File file, ImportStrategy strategy, Map<String, String> columns) throws Throwable
  {
    GeoObjectImportConfiguration config = this.getTestConfiguration(file, strategy, columns);

    ImportHistory hist = this.mockImport(config);

    return ImportHistory.get(hist.getOid());
  }

  private GeoObjectImportConfiguration getTestConfiguration(File file, ImportStrategy strategy, Map<String, String> columns) throws Exception
  {
    try (InputStream istream = new FileInputStream(file))
    {
      GeoObjectImportConfiguration config = this.getTestConfiguration(istream, file.getName());
      config.setImportStrategy(strategy);
      config.setHierarchy(ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode()));

      columns.forEach((attribute, column) -> config.setFunction(attribute, new BasicColumnFunction(column)));

      return config;
    }
  }

  private GeoObjectImportConfiguration getTestConfiguration(InputStream istream)
  {
    return this.getTestConfiguration(istream, STATES_FILE_NAME);
  }

  private GeoObjectImportConfiguration getTestConfiguration(InputStream istream, String fileName)
  {
    GeoObjectImportConfigurationDTO dto = this.geoParquetService.getConfiguration(testData.clientRequest.getSessionId(), fileName, istream, getView());
    dto.setObjectType(JobHistoryType.GEO_OBJECT);
    dto.setPostalCode(false);

    dto.getType().getAttributes().forEach(attribute -> {
      String attributeName = attribute.getCode();

      if (attributeName.equals(GeoObject.DISPLAY_LABEL))
      {
        attribute.setTarget("NAME");
      }
      else if (attributeName.equals(GeoObject.CODE))
      {
        attribute.setTarget("GEOID");
      }
    });

    GeoObjectImportConfiguration config = (GeoObjectImportConfiguration) ImportConfiguration.build(dto, true);
    config.setStartDate(USATestData.DEFAULT_OVER_TIME_DATE);
    config.setEndDate(USATestData.DEFAULT_END_TIME_DATE);
    config.setImportStrategy(ImportStrategy.NEW_AND_UPDATE);

    return config;
  }

  private ImportHistory mockImport(GeoObjectImportConfiguration config) throws Throwable
  {
    ImportHistory hist = this.createHistory(config);

    return this.run(hist, config);
  }

  private ImportHistory createHistory(GeoObjectImportConfiguration config)
  {
    if (config.getStartDate() == null)
    {
      config.setStartDate(new Date());
    }

    if (config.getEndDate() == null)
    {
      config.setEndDate(new Date());
    }

    DataImportJob job = new DataImportJob();
    job.apply();

    ImportHistory hist = (ImportHistory) job.createNewHistory();

    config.setHistoryId(hist.getOid());
    config.setJobId(job.getOid());

    ServerGeoObjectType type = config.getType();

    hist.appLock();
    hist.setImportFileId(config.getVaultFileId());
    hist.setConfiguration(config.toDTO());
    hist.setOrganization(type.getOrganization().getOrganization());
    hist.setGeoObjectTypeCode(type.getCode());
    hist.apply();

    return hist;
  }

  private ImportHistory run(ImportHistory hist, GeoObjectImportConfiguration config) throws Throwable
  {
    DataImportJob job = DataImportJob.get(config.getJobId());

    ExecutionContext context = job.startSynchronously(hist);

    return (ImportHistory) context.getHistory();
  }
}
