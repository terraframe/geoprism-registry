/**
 *
 */
package net.geoprism.registry.etl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.jaitools.jts.CoordinateSequence2D;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.runwaysdk.constants.VaultProperties;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.SchedulerManager;

import net.geoprism.data.importer.BasicColumnFunction;
import net.geoprism.registry.DataNotFoundException;
import net.geoprism.registry.DuplicateGeoObjectCodeException;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.USADatasetTest;
import net.geoprism.registry.etl.FormatSpecificImporterFactory.FormatImporterType;
import net.geoprism.registry.etl.ImportError.ErrorResolution;
import net.geoprism.registry.etl.ObjectImporterFactory.ObjectImportType;
import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.io.Location;
import net.geoprism.registry.io.ParentMatchStrategy;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerParentTreeNode;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.service.request.ETLService;
import net.geoprism.registry.service.request.ExcelService;
import net.geoprism.registry.service.business.ServiceFactory;
import net.geoprism.registry.test.SchedulerTestUtils;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.USATestData;
import net.geoprism.registry.view.ServerParentTreeNodeOverTime;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class GeoObjectImporterTest extends USADatasetTest implements InstanceTestClassListener
{

  private final Integer              ROW_COUNT = 3;

  @Autowired
  private GeoObjectBusinessServiceIF objectService;

  @Autowired
  private ETLService                 etlService;

  @Autowired
  private ExcelService                 service;


  @Override
  public void beforeClassSetup() throws Exception
  {
    super.beforeClassSetup();

    if (!SchedulerManager.initialized())
    {
      SchedulerManager.start();
    }
  }

  @Before
  public void setUp()
  {
    testData.setUpInstanceData();

    clearData();

    testData.logIn(USATestData.USER_NPS_RA);
  }

  @After
  public void tearDown() throws IOException
  {
    testData.logOut();

    testData.tearDownInstanceData();

    FileUtils.deleteDirectory(new File(VaultProperties.getPath("vault.default"), "files"));

    clearData();
  }

  @Request
  private static void clearData()
  {
    SchedulerTestUtils.clearImportData();

    for (int i = 1; i < 11; ++i)
    {
      TestGeoObjectInfo one = testData.newTestGeoObjectInfo("000" + i, USATestData.DISTRICT);
      one.setCode("000" + i);
      one.delete();
    }
  }

  private ImportHistory importExcelFile(String sessionId, String config) throws InterruptedException
  {
    String retConfig = this.etlService.doImport(sessionId, config).toString();

    GeoObjectImportConfiguration configuration = (GeoObjectImportConfiguration) ImportConfiguration.build(retConfig, true);

    String historyId = configuration.getHistoryId();

    Thread.sleep(100);

    return ImportHistory.get(historyId);
  }

  /*
   * I simply could not get this test to work on the build machine :(. It works
   * locally for me but not on the build machine and I don't know why.
   */
  // @Test
  // public void testSessionExpire() throws InterruptedException
  // {
  // CommonsConfigurationResolver.getInMemoryConfigurator().setProperty("import.refreshSessionRecordCount",
  // "1");
  // Assert.assertEquals(1,
  // GeoregistryProperties.getRefreshSessionRecordCount());
  //
  // Date benchmarkStartTime = new Date();
  // testUpdateOnly();
  // Date benchmarkEndTime = new Date();
  // long benchmarkRuntime = benchmarkEndTime.getTime() -
  // benchmarkStartTime.getTime();
  // System.out.println("Benchmark time is " + benchmarkRuntime); // Find out
  // how long it takes on this computer to import one record
  //
  // GeoObjectImportConfiguration config = testSessionSetup();
  //
  // Date startTime = new Date();
  //
  // long oldSessionTime = Session.getSessionTime();
  //
  // // This value must be very finely tuned. It has to be short enough such
  // that it is less than the time a fast computer
  // // will take to import the entire spreadsheet, but small enough so that a
  // slow computer can import a single record
  // // before the session expires.
  // final long sessionTimeMs = benchmarkRuntime + 1500;
  // Session.setSessionTime(sessionTimeMs / (1000));
  //
  // ImportHistory hist;
  // try
  // {
  // hist = testSessionExpireInReq(config);
  // }
  // finally
  // {
  // Session.setSessionTime(oldSessionTime);
  // }
  //
  // sessionTestValidateInRequest(hist, startTime, sessionTimeMs);
  // }
  //
  // @Request
  // private void sessionTestValidateInRequest(ImportHistory hist, Date
  // startTime, long sessionTimeMs) throws InterruptedException
  // {
  // SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.SUCCESS);
  //
  // Date endTime = new Date();
  //
  // System.out.println("Session expiration test took " + (endTime.getTime() -
  // startTime.getTime()) + " miliseconds to complete.");
  //
  // if ((endTime.getTime() - startTime.getTime()) < sessionTimeMs)
  // {
  // Assert.fail("The test completed before the session had a chance to expire.
  // Try setting the 'sessionTimeMs' lower.");
  // }
  // }
  //
  // @Request
  // private GeoObjectImportConfiguration testSessionSetup()
  // {
  // InputStream istream =
  // this.getClass().getResourceAsStream("/test-spreadsheet-500records.xlsx");
  //
  // Assert.assertNotNull(istream);
  //
  // ExcelService service = new ExcelService();
  // ServerHierarchyType hierarchyType =
  // ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());
  //
  // GeoObjectImportConfiguration config = this.getTestConfiguration(istream,
  // service, null, ImportStrategy.NEW_AND_UPDATE);
  // config.setHierarchy(hierarchyType);
  // return config;
  // }
  //
  // @Request
  // public ImportHistory testSessionExpireInReq(GeoObjectImportConfiguration
  // config) throws InterruptedException
  // {
  // ImportHistory hist = importExcelFile(testData.clientRequest.getSessionId(),
  // config.toJSON().toString());
  //
  // // We have to wait until the job is running so that it will run with the
  // session time.
  // SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.RUNNING);
  //
  // return hist;
  // }

  @Test
  @Request
  public void testNewAndUpdate() throws InterruptedException
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet2.xlsx");

    Assert.assertNotNull(istream);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_AND_UPDATE);
    config.setHierarchy(hierarchyType);

    ImportHistory hist = importExcelFile(testData.clientRequest.getSessionId(), config.toJSON().toString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.SUCCESS);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(3), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    ServerGeoObjectIF object = this.objectService.getGeoObjectByCode("0001", USATestData.DISTRICT.getCode());

    Assert.assertNotNull(object);
    Assert.assertEquals("Test", object.getDisplayLabel(TestDataSet.DEFAULT_OVER_TIME_DATE).getValue());

    Geometry geometry = object.getGeometry();

    Assert.assertNotNull(geometry);

    Double lat = Double.valueOf(2.232343);
    Double lon = Double.valueOf(1.134232);

    GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
    MultiPoint expected = new MultiPoint(new Point[] { new Point(new CoordinateSequence2D(lon, lat), factory) }, factory);

    Assert.assertEquals(expected, geometry);

    ServerGeoObjectIF coloradoDistOne = this.objectService.getGeoObjectByCode(USATestData.CO_D_ONE.getCode(), USATestData.DISTRICT.getCode());

    Double cd1_lat = Double.valueOf(4.3333);
    Double cd1_lon = Double.valueOf(1.222);

    GeometryFactory cd1_factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
    MultiPoint cd1_expected = new MultiPoint(new Point[] { new Point(new CoordinateSequence2D(cd1_lon, cd1_lat), cd1_factory) }, cd1_factory);

    Geometry cd1_geometry = coloradoDistOne.getGeometry(TestDataSet.DEFAULT_OVER_TIME_DATE);
    Assert.assertEquals(cd1_expected, cd1_geometry);

    JSONObject json = new JSONObject(this.etlService.getImportErrors(testData.clientRequest.getSessionId(), hist.getOid(), false, 100, 1).toString());

    Assert.assertEquals(0, json.getJSONArray("resultSet").length());
  }

  @Test
  @Request
  public void testUpdateOnly() throws InterruptedException
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet2.xlsx");

    Assert.assertNotNull(istream);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null, ImportStrategy.UPDATE_ONLY);
    config.setHierarchy(hierarchyType);

    ImportHistory hist = importExcelFile(testData.clientRequest.getSessionId(), config.toJSON().toString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.FEEDBACK);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(1), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.IMPORT_RESOLVE, hist.getStage().get(0));

    try
    {
      this.objectService.getGeoObjectByCode("0001", USATestData.DISTRICT.getCode());

      Assert.fail();
    }
    catch (DataNotFoundException e)
    {
      // Expected
    }

    ServerGeoObjectIF coloradoDistOne = this.objectService.getGeoObjectByCode(USATestData.CO_D_ONE.getCode(), USATestData.DISTRICT.getCode());

    Double cd1_lat = Double.valueOf(4.3333);
    Double cd1_lon = Double.valueOf(1.222);

    GeometryFactory cd1_factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
    MultiPoint cd1_expected = new MultiPoint(new Point[] { new Point(new CoordinateSequence2D(cd1_lon, cd1_lat), cd1_factory) }, cd1_factory);

    Geometry cd1_geometry = coloradoDistOne.getGeometry(TestDataSet.DEFAULT_OVER_TIME_DATE);
    Assert.assertEquals(cd1_expected, cd1_geometry);

    JSONObject json = new JSONObject(this.etlService.getImportErrors(testData.clientRequest.getSessionId(), hist.getOid(), false, 100, 1).toString());

    Assert.assertEquals(2, json.getJSONArray("resultSet").length());
  }

  /**
   * Tests to make sure that we are recording the correct amount of import
   * errors.
   * 
   * @throws InterruptedException
   */
  @Test
  @Request
  public void testUpdateErrorCount() throws InterruptedException
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet3.xlsx");

    Assert.assertNotNull(istream);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_ONLY);
    config.setHierarchy(hierarchyType);

    // First, import the spreadsheet. It should be succesful
    ImportHistory hist = importExcelFile(testData.clientRequest.getSessionId(), config.toJSON().toString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.SUCCESS);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(Long.valueOf(10), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(10), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(10), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    // Import a second spreadsheet, which should have a few duplicate records.
    InputStream istream2 = this.getClass().getResourceAsStream("/test-spreadsheet4.xlsx");
    GeoObjectImportConfiguration config2 = this.getTestConfiguration(istream2, service, null, ImportStrategy.NEW_ONLY);
    config2.setHierarchy(hierarchyType);
    ImportHistory hist2 = importExcelFile(testData.clientRequest.getSessionId(), config2.toJSON().toString());

    SchedulerTestUtils.waitUntilStatus(hist2.getOid(), AllJobStatus.FEEDBACK);

    hist2 = ImportHistory.get(hist2.getOid());
    Assert.assertEquals(Long.valueOf(17), hist2.getWorkTotal());
    Assert.assertEquals(Long.valueOf(17), hist2.getWorkProgress());
    Assert.assertEquals(Long.valueOf(7), hist2.getImportedRecords());
    Assert.assertEquals(ImportStage.IMPORT_RESOLVE, hist2.getStage().get(0));

    JSONObject json = new JSONObject(this.etlService.getImportErrors(testData.clientRequest.getSessionId(), hist2.getOid(), false, 100, 1).toString());

    Assert.assertEquals(10, json.getJSONArray("resultSet").length());
  }

  @Test
  @Request
  public void testCreateOnly() throws InterruptedException
  {
    // USATestData.CO_D_ONE.delete();

    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet2.xlsx");

    Assert.assertNotNull(istream);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_ONLY);
    config.setHierarchy(hierarchyType);

    ImportHistory hist = importExcelFile(testData.clientRequest.getSessionId(), config.toJSON().toString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.FEEDBACK);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(2), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.IMPORT_RESOLVE, hist.getStage().get(0));

    ServerGeoObjectIF object = this.objectService.getGeoObjectByCode("0001", USATestData.DISTRICT.getCode());

    Assert.assertNotNull(object);
    Assert.assertEquals("Test", object.getDisplayLabel(TestDataSet.DEFAULT_OVER_TIME_DATE).getValue());

    Geometry geometry = object.getGeometry();

    Assert.assertNotNull(geometry);

    Double lat = Double.valueOf(2.232343);
    Double lon = Double.valueOf(1.134232);

    GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
    MultiPoint expected = new MultiPoint(new Point[] { new Point(new CoordinateSequence2D(lon, lat), factory) }, factory);

    Assert.assertEquals(expected, geometry);

    ServerGeoObjectIF coloradoDistOne = this.objectService.getGeoObjectByCode(USATestData.CO_D_ONE.getCode(), USATestData.DISTRICT.getCode());

    GeometryFactory cd1_factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
    MultiPoint cd1_expected = new MultiPoint(new Point[] {
        new Point(new CoordinateSequence2D(Double.valueOf(110), Double.valueOf(80)), cd1_factory),
        new Point(new CoordinateSequence2D(Double.valueOf(120), Double.valueOf(70)), cd1_factory)
    }, cd1_factory);

    Geometry cd1_geometry = coloradoDistOne.getGeometry();
    Assert.assertEquals(cd1_expected, cd1_geometry);

    JSONObject json = new JSONObject(this.etlService.getImportErrors(testData.clientRequest.getSessionId(), hist.getOid(), false, 100, 1).toString());

    Assert.assertEquals(1, json.getJSONArray("resultSet").length());
  }

  @Test
  @Request
  public void testErrorSerializeParents() throws InterruptedException
  {
    TestGeoObjectInfo state00 = testData.newTestGeoObjectInfo("00", USATestData.STATE);
    state00.setCode("00");
    state00.setDisplayLabel("Test Label");
    state00.setRegistryId(ServiceFactory.getIdService().getUids(1)[0]);
    state00.apply();
    USATestData.USA.addChild(state00, USATestData.HIER_ADMIN);

    InputStream istream = this.getClass().getResourceAsStream("/parent-test.xlsx");

    Assert.assertNotNull(istream);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_ONLY);
    config.setHierarchy(hierarchyType);
    config.addParent(new Location(USATestData.COUNTRY.getServerObject(), hierarchyType, new BasicColumnFunction("Parent Country"), ParentMatchStrategy.ALL));
    config.addParent(new Location(USATestData.STATE.getServerObject(), hierarchyType, new BasicColumnFunction("Parent State"), ParentMatchStrategy.ALL));
    config.setStartDate(new Date());
    config.setEndDate(new Date());

    ImportHistory hist = importExcelFile(testData.clientRequest.getSessionId(), config.toJSON().toString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.FEEDBACK);

    JSONObject json = new JSONObject(this.etlService.getImportErrors(testData.clientRequest.getSessionId(), hist.getOid(), false, 100, 1).toString());
    JSONArray errors = json.getJSONArray("resultSet");

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(Long.valueOf(2), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(2), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(1), hist.getErrorCount());
    Assert.assertEquals(Long.valueOf(0), hist.getErrorResolvedCount());
    Assert.assertEquals(Long.valueOf(1), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.IMPORT_RESOLVE, hist.getStage().get(0));

    Assert.assertEquals(1, errors.length());

    JSONObject error = errors.getJSONObject(0);

    Assert.assertTrue(error.has("id"));

    Assert.assertEquals(DuplicateGeoObjectCodeException.CLASS, error.getJSONObject("exception").getString("type"));

    JSONObject object = error.getJSONObject("object");
    Assert.assertTrue(object.has("geoObject"));
    Assert.assertTrue(object.has("parents"));
    Assert.assertTrue(object.getJSONArray("parents").length() > 0);

    ServerParentTreeNodeOverTime parentsOverTime = ServerParentTreeNodeOverTime.fromJSON(USATestData.DISTRICT.getServerObject(), object.getJSONArray("parents").toString());

    Assert.assertEquals(1, parentsOverTime.getHierarchies().size());

    List<ServerParentTreeNode> nodes = parentsOverTime.getEntries(USATestData.HIER_ADMIN.getServerObject());
    Assert.assertEquals(1, nodes.size());

    // TODO The fromJSON doesn't seem to be reading the json correctly...
    // List<ServerParentTreeNode> ptns = nodes.get(0).getParents();
    // Assert.assertEquals(2, ptns.size());
    //
    // Assert.assertEquals(USATestData.USA.getCode(),
    // ptns.get(0).getGeoObject().getCode());
    // Assert.assertEquals(USATestData.COLORADO.getCode(),
    // ptns.get(1).getGeoObject().getCode());

    // Test Resolving the error and then completing the import
    ImportErrorQuery ieq = new ImportErrorQuery(new QueryFactory());
    Assert.assertEquals(1, ieq.getCount());
    Assert.assertEquals(ErrorResolution.UNRESOLVED.name(), ieq.getIterator().next().getResolution());

    JSONObject resolution = new JSONObject();
    resolution.put("importErrorId", error.get("id"));
    resolution.put("resolution", ErrorResolution.IGNORE);
    resolution.put("historyId", hist.getOid());

    this.etlService.submitImportErrorResolution(testData.clientRequest.getSessionId(), resolution.toString());

    Assert.assertEquals(ErrorResolution.IGNORE.name(), ieq.getIterator().next().getResolution());

    this.etlService.resolveImport(testData.clientRequest.getSessionId(), hist.getOid());

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(Long.valueOf(2), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(2), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(1), hist.getErrorCount());
    Assert.assertEquals(Long.valueOf(0), hist.getErrorResolvedCount());
    Assert.assertEquals(Long.valueOf(1), hist.getImportedRecords());
    Assert.assertEquals(AllJobStatus.SUCCESS, hist.getStatus().get(0));
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    Assert.assertEquals(0, ieq.getCount());
  }

  @Test
  @Request
  public void testGetImportDetails() throws InterruptedException
  {
    TestGeoObjectInfo one = testData.newTestGeoObjectInfo("0001", USATestData.DISTRICT);
    one.setCode("0001");
    one.delete();

    TestGeoObjectInfo two = testData.newTestGeoObjectInfo("0002", USATestData.DISTRICT);
    two.setCode("0002");
    two.delete();

    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet2.xlsx");

    Assert.assertNotNull(istream);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_ONLY);
    config.setHierarchy(hierarchyType);

    Date startDate = new Date();
    Date endDate = new Date();
    config.setStartDate(startDate);
    config.setEndDate(endDate);

    ImportHistory hist = importExcelFile(testData.clientRequest.getSessionId(), config.toJSON().toString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.FEEDBACK);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(2), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.IMPORT_RESOLVE, hist.getStage().get(0));

    JSONObject jo = new JSONObject(this.etlService.getImportDetails(testData.clientRequest.getSessionId(), hist.getOid(), false, 100, 1).toString());

    SimpleDateFormat format = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);
    format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);
    Assert.assertEquals(format.format(startDate), jo.getJSONObject("configuration").getString("startDate"));
    Assert.assertEquals(format.format(endDate), jo.getJSONObject("configuration").getString("endDate"));

    JSONObject importErrors = jo.getJSONObject("importErrors");
    JSONArray results = importErrors.getJSONArray("resultSet");

    Assert.assertEquals(1, results.length());
  }

  private GeoObjectImportConfiguration getTestConfiguration(InputStream istream, ExcelService service, AttributeTermType attributeTerm, ImportStrategy strategy)
  {
    JSONObject result = service.getExcelConfiguration(testData.clientRequest.getSessionId(), USATestData.DISTRICT.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, "test-spreadsheet.xlsx", istream, strategy, false);
    JSONObject type = result.getJSONObject(GeoObjectImportConfiguration.TYPE);
    JSONArray attributes = type.getJSONArray(GeoObjectType.JSON_ATTRIBUTES);

    for (int i = 0; i < attributes.length(); i++)
    {
      JSONObject attribute = attributes.getJSONObject(i);

      String attributeName = attribute.getString(AttributeType.JSON_CODE);

      if (attributeName.equals(GeoObject.DISPLAY_LABEL))
      {
        attribute.put(GeoObjectImportConfiguration.TARGET, "Name");
      }
      else if (attributeName.equals(GeoObject.CODE))
      {
        attribute.put(GeoObjectImportConfiguration.TARGET, "Code");
      }
      else if (attributeName.equals(GeoObjectImportConfiguration.LATITUDE))
      {
        attribute.put(GeoObjectImportConfiguration.TARGET, "Latitude");
      }
      else if (attributeName.equals(GeoObjectImportConfiguration.LONGITUDE))
      {
        attribute.put(GeoObjectImportConfiguration.TARGET, "Longitude");
      }
      else if (attributeTerm != null && attributeName.equals(attributeTerm.getName()))
      {
        attribute.put(GeoObjectImportConfiguration.TARGET, "Term");
      }
    }

    result.put(ImportConfiguration.FORMAT_TYPE, FormatImporterType.EXCEL);
    result.put(ImportConfiguration.OBJECT_TYPE, ObjectImportType.GEO_OBJECT);

    GeoObjectImportConfiguration configuration = (GeoObjectImportConfiguration) ImportConfiguration.build(result.toString(), true);

    return configuration;
  }
}
