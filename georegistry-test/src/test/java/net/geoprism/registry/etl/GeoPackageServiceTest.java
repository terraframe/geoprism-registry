/**
 *
 */
package net.geoprism.registry.etl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
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
import org.geotools.api.data.DataStore;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.feature.FeatureIterator;
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
import net.geoprism.registry.USADatasetTest;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.etl.FormatSpecificImporterFactory.FormatImporterType;
import net.geoprism.registry.etl.ObjectImporterFactory.JobHistoryType;
import net.geoprism.registry.etl.upload.GeoPackageImporter;
import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.excel.SheetDTO;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.io.PostalCodeFactory;
import net.geoprism.registry.io.view.GeoObjectImportConfigurationDTO;
import net.geoprism.registry.jobs.ImportHistory;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.service.business.GeoPackageBusinessService;
import net.geoprism.registry.service.business.ServiceFactory;
import net.geoprism.registry.service.request.GeoPackageService;
import net.geoprism.registry.service.request.RegistryComponentService;
import net.geoprism.registry.test.SchedulerTestUtils;
import net.geoprism.registry.test.TestAttributeTypeInfo;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.USATestData;
import net.geoprism.registry.view.ImportConfigurationView;

/**
 * Tests the GeoPackage import end to end. The fixtures were made with GDAL from
 * the states shapefile: states.gpkg (with added DATE, DATETIME and BOOLEAN
 * columns) and mercator.gpkg (two states in EPSG:3857). GeoPackages with other
 * layers are derived from states.gpkg with SQL. Reading the columns is also
 * covered, without a database, by GeoPackageSampleTest.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@ExtendWith(InstanceTestClassExtension.class)
public class GeoPackageServiceTest extends USADatasetTest implements InstanceTestClassListener
{
  private static final String                STATES_RESOURCE   = "/geopackage/states.gpkg.test";

  private static final String                MERCATOR_RESOURCE = "/geopackage/mercator.gpkg.test";

  private static final int                   STATE_COUNT       = 56;

  private static final TestAttributeTypeInfo testInteger       = new TestAttributeTypeInfo("gpkgInteger", "gpkgIntegerLocalName", USATestData.STATE, AttributeIntegerType.TYPE);

  private static final TestAttributeTypeInfo testBoolean       = new TestAttributeTypeInfo("gpkgBoolean", "gpkgBooleanLocalName", USATestData.STATE, AttributeBooleanType.TYPE);

  private static final TestAttributeTypeInfo testDate          = new TestAttributeTypeInfo("gpkgDate", "gpkgDateLocalName", USATestData.STATE, AttributeDateType.TYPE);

  @Autowired
  private RegistryComponentService           service;

  @Autowired
  private GeoObjectBusinessServiceIF         objectService;

  @Autowired
  private GeoPackageService                  geoPackageService;

  @Autowired
  private GeoPackageBusinessService          geoPackageBusinessService;

  /**
   * Scratch directory for fixtures, deleted after each test
   */
  private File                               directory;

  /**
   * Copy of states.gpkg
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

    this.directory = Files.createTempDirectory("geopackage").toFile();
    this.states = this.copy(STATES_RESOURCE, "states.gpkg");
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
    try (InputStream istream = new FileInputStream(this.states))
    {
      GeoObjectImportConfigurationDTO dto = this.geoPackageService.getConfiguration(testData.clientRequest.getSessionId(), this.states.getName(), istream, getView());

      Assertions.assertEquals(FormatImporterType.GEOPACKAGE, dto.getFormatType());
      Assertions.assertFalse(dto.getPostalCode());

      SheetDTO sheet = dto.getSheet();

      Assertions.assertEquals("states", sheet.getName());

      Map<String, List<String>> attributes = sheet.getAttributes();

      Assertions.assertEquals(9, attributes.get(GeoObjectImportConfiguration.TEXT).size());
      Assertions.assertEquals(List.of("ALAND", "AWATER"), attributes.get(GeoObjectImportConfiguration.NUMERIC));
      Assertions.assertEquals(List.of("even"), attributes.get(AttributeBooleanType.TYPE));
      Assertions.assertEquals(List.of("founded", "updated"), attributes.get(AttributeDateType.TYPE));

      Assertions.assertTrue(sheet.getNonUniqueAttributes().contains("LSAD"));
      Assertions.assertFalse(sheet.getNonUniqueAttributes().contains("GEOID"));

      Assertions.assertEquals(SheetDTO.MAX_SAMPLE_ROWS, sheet.getSample().size());
      Assertions.assertEquals("West Virginia", sheet.getSample().get(0).get("NAME"));
    }
  }

  @Test
  @Request
  public void testGetConfigurationZip() throws Exception
  {
    File zip = new File(this.directory, "states.zip");

    try (ZipOutputStream ostream = new ZipOutputStream(new FileOutputStream(zip)))
    {
      ostream.putNextEntry(new ZipEntry("states.gpkg"));
      Files.copy(this.states.toPath(), ostream);
      ostream.closeEntry();
    }

    SheetDTO sheet = this.getConfiguration(zip).getSheet();

    Assertions.assertEquals(9, sheet.getAttributes().get(GeoObjectImportConfiguration.TEXT).size());
  }

  @Test
  @Request
  public void testGetConfigurationZipWithoutGeoPackage() throws Exception
  {
    File text = new File(this.directory, "readme.txt");
    Files.writeString(text.toPath(), "Not a GeoPackage");

    File zip = new File(this.directory, "readme.zip");

    try (ZipOutputStream ostream = new ZipOutputStream(new FileOutputStream(zip)))
    {
      ostream.putNextEntry(new ZipEntry("readme.txt"));
      Files.copy(text.toPath(), ostream);
      ostream.closeEntry();
    }

    ImportFileFormatException ex = Assertions.assertThrows(ImportFileFormatException.class, () -> this.getConfiguration(zip));

    Assertions.assertTrue(ex.getDetails().contains(".gpkg"), ex.getDetails());
  }

  /**
   * Zips made by compressing a folder hold the GeoPackage inside the folder.
   * Zips made on macOS also hold a __MACOSX folder and ._ resource fork files,
   * which must be ignored even though their names end in .gpkg.
   */
  @Test
  @Request
  public void testGetConfigurationZipWithFolder() throws Exception
  {
    File zip = new File(this.directory, "STATES.ZIP");

    try (ZipOutputStream ostream = new ZipOutputStream(new FileOutputStream(zip)))
    {
      for (String folder : new String[] { "data/", "__MACOSX/", "__MACOSX/data/" })
      {
        ostream.putNextEntry(new ZipEntry(folder));
        ostream.closeEntry();
      }

      // Resource forks sort before the GeoPackage, so they would be picked if
      // they weren't ignored
      for (String fork : new String[] { "__MACOSX/data/._STATES.GPKG", "data/._STATES.GPKG" })
      {
        ostream.putNextEntry(new ZipEntry(fork));
        ostream.write(new byte[] { 0, 5, 22, 7 });
        ostream.closeEntry();
      }

      ostream.putNextEntry(new ZipEntry("data/STATES.GPKG"));
      Files.copy(this.states.toPath(), ostream);
      ostream.closeEntry();
    }

    SheetDTO sheet = this.getConfiguration(zip).getSheet();

    Assertions.assertEquals(9, sheet.getAttributes().get(GeoObjectImportConfiguration.TEXT).size());
  }

  @Test
  @Request
  public void testInvalidProjection() throws Exception
  {
    File mercator = this.copy(MERCATOR_RESOURCE, "mercator.gpkg");

    Assertions.assertThrows(InvalidProjectionException.class, () -> this.getConfiguration(mercator));
  }

  @Test
  @Request
  public void testIgnoreProjection() throws Exception
  {
    File mercator = this.copy(MERCATOR_RESOURCE, "mercator.gpkg");

    try (InputStream istream = new FileInputStream(mercator))
    {
      GeoObjectImportConfigurationDTO dto = this.geoPackageBusinessService.getConfiguration(mercator.getName(), istream, getView(), true);

      Assertions.assertTrue(dto.getIgnoreProjection());
      Assertions.assertNotNull(dto.getSheet());
    }
  }

  /**
   * Only GeoPackages with exactly one layer of features can be imported. The
   * message names the layers.
   */
  @Test
  @Request
  public void testMultipleLayers() throws Exception
  {
    this.sql(this.states, //
        "CREATE TABLE counties (fid INTEGER PRIMARY KEY AUTOINCREMENT, geom MULTIPOLYGON, NAME TEXT)", //
        "INSERT INTO counties (geom, NAME) SELECT geom, NAME FROM states WHERE GEOID = '01'", //
        "INSERT INTO gpkg_contents (table_name, data_type, identifier, srs_id) VALUES ('counties', 'features', 'counties', 4326)", //
        "INSERT INTO gpkg_geometry_columns (table_name, column_name, geometry_type_name, srs_id, z, m) VALUES ('counties', 'geom', 'MULTIPOLYGON', 4326, 0, 0)");

    GeoPackageLayerException ex = Assertions.assertThrows(GeoPackageLayerException.class, () -> this.getConfiguration(this.states));

    Assertions.assertEquals("counties, states", ex.getLayers());
    Assertions.assertTrue(ex.getLocalizedMessage().contains("[counties, states]"), ex.getLocalizedMessage());
  }

  /**
   * A GeoPackage whose only table has no geometries has nothing to import
   */
  @Test
  @Request
  public void testNoFeatureLayers() throws Exception
  {
    this.sql(this.states, //
        "UPDATE gpkg_contents SET data_type = 'attributes' WHERE table_name = 'states'", //
        "DELETE FROM gpkg_geometry_columns WHERE table_name = 'states'");

    GeoPackageLayerException ex = Assertions.assertThrows(GeoPackageLayerException.class, () -> this.getConfiguration(this.states));

    Assertions.assertEquals("", ex.getLayers());
  }

  @Test
  @Request
  public void testNotAGeoPackage() throws Exception
  {
    File file = new File(this.directory, "fake.gpkg");
    Files.writeString(file.toPath(), "This is not a GeoPackage");

    ImportFileFormatException ex = Assertions.assertThrows(ImportFileFormatException.class, () -> this.getConfiguration(file));

    // SQLite's explanation is shown, without the server's file path
    Assertions.assertTrue(ex.getDetails().contains("[fake.gpkg] is not a valid GeoPackage"), ex.getDetails());
    Assertions.assertFalse(ex.getDetails().contains(File.separator + "fake.gpkg"), ex.getDetails());
    Assertions.assertTrue(ex.getLocalizedMessage().contains(ex.getDetails()), ex.getLocalizedMessage());
  }

  @Test
  @Request
  public void testWrongExtension() throws Exception
  {
    File file = new File(this.directory, "states.sqlite");
    Files.copy(this.states.toPath(), file.toPath());

    ImportFileFormatException ex = Assertions.assertThrows(ImportFileFormatException.class, () -> this.getConfiguration(file));

    Assertions.assertTrue(ex.getDetails().contains("[states.sqlite]"), ex.getDetails());
  }

  /*
   * Imports
   */

  @Test
  @Request
  public void testImport() throws Throwable
  {
    ImportHistory hist = this.importFile(this.states, Map.of());

    Assertions.assertEquals(AllJobStatus.SUCCESS, hist.getStatus().get(0));
    Assertions.assertEquals(Long.valueOf(STATE_COUNT), hist.getWorkTotal());
    Assertions.assertEquals(Long.valueOf(STATE_COUNT), hist.getWorkProgress());
    Assertions.assertEquals(Long.valueOf(STATE_COUNT), hist.getImportedRecords());
    Assertions.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    GeoObject object = this.service.getGeoObjectByCode(testData.clientRequest.getSessionId(), "01", USATestData.STATE.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assertions.assertNotNull(object);
    Assertions.assertEquals("Alabama", object.getLocalizedDisplayLabel());
    Assertions.assertEquals(true, object.getExists());
  }

  /**
   * The imported geometries match the file's
   */
  @Test
  @Request
  public void testImportGeometry() throws Throwable
  {
    this.importFile(this.states, Map.of());

    for (String code : new String[] { "54", "01", "72" })
    {
      Geometry expected = this.readGeometry(code);
      Geometry actual = this.getGeoObject(code).getGeometry();

      Assertions.assertEquals("MultiPolygon", actual.getGeometryType(), code);
      Assertions.assertEquals(expected.getNumGeometries(), actual.getNumGeometries(), code);
      // Stored coordinates are rounded to 7 decimal places (about 1 cm)
      Assertions.assertEquals(expected.getArea(), actual.getArea(), expected.getArea() * 1e-6, code);
      Assertions.assertEquals(expected.getEnvelopeInternal().getMinX(), actual.getEnvelopeInternal().getMinX(), 1e-7, code);
      Assertions.assertEquals(expected.getEnvelopeInternal().getMaxY(), actual.getEnvelopeInternal().getMaxY(), 1e-7, code);
    }
  }

  /**
   * Integer, boolean and date columns are imported into attributes of those
   * types. Dates are stored at midnight UTC, whatever the server's time zone.
   */
  @Test
  @Request
  public void testImportAttributeValues() throws Throwable
  {
    this.importFile(this.states, Map.of(testInteger.getName(), "ALAND", testBoolean.getName(), "even", testDate.getName(), "founded"));

    ServerGeoObjectIF alabama = this.getGeoObject("01");

    Assertions.assertEquals(Long.valueOf(131174431216L), alabama.getValue(testInteger.getName(), TestDataSet.DEFAULT_OVER_TIME_DATE));
    Assertions.assertEquals(Boolean.FALSE, alabama.getValue(testBoolean.getName(), TestDataSet.DEFAULT_OVER_TIME_DATE));

    // 2020-01-16 at midnight UTC
    Assertions.assertEquals(new Date(1579132800000L), alabama.getValue(testDate.getName(), TestDataSet.DEFAULT_OVER_TIME_DATE));

    Assertions.assertEquals(Boolean.TRUE, this.getGeoObject("02").getValue(testBoolean.getName(), TestDataSet.DEFAULT_OVER_TIME_DATE));
  }

  /**
   * Importing a zipped GeoPackage
   */
  @Test
  @Request
  public void testImportZip() throws Throwable
  {
    File zip = new File(this.directory, "states.zip");

    try (ZipOutputStream ostream = new ZipOutputStream(new FileOutputStream(zip)))
    {
      ostream.putNextEntry(new ZipEntry("states.gpkg"));
      Files.copy(this.states.toPath(), ostream);
      ostream.closeEntry();
    }

    ImportHistory hist = this.importFile(zip, Map.of());

    Assertions.assertEquals(AllJobStatus.SUCCESS, hist.getStatus().get(0));
    Assertions.assertEquals(Long.valueOf(STATE_COUNT), hist.getImportedRecords());
  }

  /**
   * Windows' built in zip tool writes non-ASCII file names in the DOS code page
   * (CP437) without marking them as such. Java reads unmarked names as UTF-8,
   * which used to fail with "invalid LOC header (bad entry name)".
   */
  @Test
  @Request
  public void testImportWindowsZip() throws Throwable
  {
    File zip = new File(this.directory, "windows.zip");

    // Like Windows, write the name in CP437 without the UTF-8 flag
    try (ZipOutputStream ostream = new ZipOutputStream(new FileOutputStream(zip), Charset.forName("IBM437")))
    {
      ostream.putNextEntry(new ZipEntry("Données.gpkg"));
      Files.copy(this.states.toPath(), ostream);
      ostream.closeEntry();
    }

    ImportHistory hist = this.importFile(zip, Map.of());

    Assertions.assertEquals(AllJobStatus.SUCCESS, hist.getStatus().get(0));
    Assertions.assertEquals(Long.valueOf(STATE_COUNT), hist.getImportedRecords());
  }

  /**
   * Codes are trimmed, so whitespace (e.g. the '\r' left by Windows line
   * endings) doesn't create objects with codes that don't match
   */
  @Test
  @Request
  public void testImportTrimsCodes() throws Throwable
  {
    // The GeoPackage's R-tree triggers call GDAL's SQL functions, which plain
    // SQLite doesn't have. They aren't needed to read the file.
    try (Connection connection = new org.sqlite.JDBC().connect("jdbc:sqlite:" + this.states.getAbsolutePath(), new Properties()); Statement statement = connection.createStatement())
    {
      List<String> triggers = new ArrayList<>();

      try (ResultSet results = statement.executeQuery("SELECT name FROM sqlite_master WHERE type = 'trigger' AND tbl_name = 'states'"))
      {
        while (results.next())
        {
          triggers.add(results.getString(1));
        }
      }

      for (String trigger : triggers)
      {
        statement.execute("DROP TRIGGER \"" + trigger + "\"");
      }

      statement.execute("UPDATE states SET GEOID = GEOID || char(13) WHERE GEOID = '01'");
      statement.execute("UPDATE states SET GEOID = ' ' || GEOID || ' ' WHERE GEOID = '02'");
    }

    ImportHistory hist = this.importFile(this.states, Map.of());

    Assertions.assertEquals(AllJobStatus.SUCCESS, hist.getStatus().get(0));
    Assertions.assertEquals(Long.valueOf(STATE_COUNT), hist.getImportedRecords());

    Assertions.assertEquals("01", this.getGeoObject("01").getCode());
    Assertions.assertEquals("02", this.getGeoObject("02").getCode());
  }

  /*
   * Helpers
   */

  private File copy(String resource, String name) throws Exception
  {
    File file = new File(this.directory, name);

    try (InputStream istream = this.getClass().getResourceAsStream(resource))
    {
      Assertions.assertNotNull(istream, resource);

      Files.copy(istream, file.toPath());
    }

    return file;
  }

  /**
   * Runs SQL against a GeoPackage, which is a SQLite database
   */
  private void sql(File file, String... statements) throws Exception
  {
    try (Connection connection = new org.sqlite.JDBC().connect("jdbc:sqlite:" + file.getAbsolutePath(), new Properties()); Statement statement = connection.createStatement())
    {
      for (String sql : statements)
      {
        statement.execute(sql);
      }
    }
  }

  /**
   * @return The geometry of the state in the original file
   */
  private Geometry readGeometry(String code) throws Exception
  {
    DataStore store = GeoPackageImporter.open(this.states);

    try (FeatureIterator<SimpleFeature> iterator = store.getFeatureSource("states").getFeatures().features())
    {
      while (iterator.hasNext())
      {
        SimpleFeature feature = iterator.next();

        if (code.equals(feature.getAttribute("GEOID")))
        {
          return (Geometry) feature.getDefaultGeometry();
        }
      }
    }
    finally
    {
      store.dispose();
    }

    throw new AssertionError("No state [" + code + "] in the file");
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

  private GeoObjectImportConfigurationDTO getConfiguration(File file) throws Exception
  {
    try (InputStream istream = new FileInputStream(file))
    {
      return this.geoPackageBusinessService.getConfiguration(file.getName(), istream, getView(), false);
    }
  }

  /**
   * Imports a file, matching NAME to the display label and GEOID to the code
   *
   * @param columns
   *          Additional attribute name to column matches
   * @return The refreshed history of the import
   */
  private ImportHistory importFile(File file, Map<String, String> columns) throws Throwable
  {
    GeoObjectImportConfigurationDTO dto;

    try (InputStream istream = new FileInputStream(file))
    {
      dto = this.geoPackageService.getConfiguration(testData.clientRequest.getSessionId(), file.getName(), istream, getView());
    }

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
    config.setHierarchy(ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode()));

    columns.forEach((attribute, column) -> config.setFunction(attribute, new BasicColumnFunction(column)));

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

    ExecutionContext context = job.startSynchronously(hist);

    return ImportHistory.get( ( (ImportHistory) context.getHistory() ).getOid());
  }
}
