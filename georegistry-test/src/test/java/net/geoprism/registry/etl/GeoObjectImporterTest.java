/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.jaitools.jts.CoordinateSequence2D;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.business.SmartExceptionDTO;
import com.runwaysdk.configuration.CommonsConfigurationResolver;
import com.runwaysdk.constants.VaultProperties;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.SchedulerManager;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

import net.geoprism.data.importer.BasicColumnFunction;
import net.geoprism.registry.DataNotFoundException;
import net.geoprism.registry.DuplicateGeoObjectCodeException;
import net.geoprism.registry.GeoregistryProperties;
import net.geoprism.registry.etl.FormatSpecificImporterFactory.FormatImporterType;
import net.geoprism.registry.etl.ImportError.ErrorResolution;
import net.geoprism.registry.etl.ObjectImporterFactory.ObjectImportType;
import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.io.Location;
import net.geoprism.registry.io.ParentMatchStrategy;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerParentTreeNode;
import net.geoprism.registry.service.ExcelService;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.test.SchedulerTestUtils;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.USATestData;
import net.geoprism.registry.view.ServerParentTreeNodeOverTime;

public class GeoObjectImporterTest
{
  protected static USATestData testData;

  private final Integer        ROW_COUNT = 3;

  @BeforeClass
  public static void setUpClass()
  {
    testData = USATestData.newTestData();
    testData.setUpMetadata();

    if (!SchedulerManager.initialized())
    {
      SchedulerManager.start();
    }
  }

  @AfterClass
  public static void cleanUpClass()
  {
    testData.tearDownMetadata();
  }

  @Before
  public void setUp()
  {
    testData.setUpInstanceData();

    clearData();

    testData.logIn(testData.USER_NPS_RA);
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
        TestGeoObjectInfo one = testData.newTestGeoObjectInfo("000" + i, testData.DISTRICT);
        one.setCode("000" + i);
        one.delete();
    }
  }

  private ImportHistory importExcelFile(String sessionId, String config) throws InterruptedException
  {
    String retConfig = new ETLService().doImport(sessionId, config).toString();

    GeoObjectImportConfiguration configuration = (GeoObjectImportConfiguration) ImportConfiguration.build(retConfig, true);

    String historyId = configuration.getHistoryId();

    Thread.sleep(100);

    return ImportHistory.get(historyId);
  }
  
  @Test
  public void testSessionExpire() throws InterruptedException
  {
    CommonsConfigurationResolver.getInMemoryConfigurator().setProperty("import.refreshSessionRecordCount", "1");
    
    Assert.assertEquals(1, GeoregistryProperties.getRefreshSessionRecordCount());
    
    GeoObjectImportConfiguration config = testSessionSetup();
    
    Date startTime = new Date();
    
    long oldSessionTime = Session.getSessionTime();
    
    final long sessionTimeMs = 8000;
    Session.setSessionTime(sessionTimeMs / (1000));
    
    ImportHistory hist;
    try
    {
      hist = testSessionExpireInReq(config);
    }
    finally
    {
      Session.setSessionTime(oldSessionTime);
    }
    
    sessionTestValidateInRequest(hist, startTime, sessionTimeMs);
  }
  
  @Request
  private void sessionTestValidateInRequest(ImportHistory hist, Date startTime, long sessionTimeMs) throws InterruptedException
  {
    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.SUCCESS);
    
    Date endTime = new Date();
    
    System.out.println("Session expiration test took " + (endTime.getTime() - startTime.getTime()) + " miliseconds to complete.");
    
    if ((endTime.getTime() - startTime.getTime()) < sessionTimeMs)
    {
      Assert.fail("The test completed before the session had a chance to expire. Try setting the 'sessionTimeMs' lower.");
    }
  }
  
  @Request
  private GeoObjectImportConfiguration testSessionSetup()
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet-500records.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_AND_UPDATE);
    config.setHierarchy(hierarchyType);
    return config;
  }
  
  @Request
  public ImportHistory testSessionExpireInReq(GeoObjectImportConfiguration config) throws InterruptedException
  {
    ImportHistory hist = importExcelFile(testData.clientRequest.getSessionId(), config.toJSON().toString());
    
    // We have to wait until the job is running so that it will run with the session time.
    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.RUNNING);
    
    return hist;
  }
  
//  @Test
//  public void testSessionExpire() throws InterruptedException
//  {
//    long beforeTime = Session.getSessionTime();
//    
//    Session.setSessionTime(1);
//    
//    try
//    {
//      testSessionExpireInReq();
//    }
//    finally
//    {
//      Session.setSessionTime(beforeTime);
//    }
//  }
//  
//  @Request
//  public void testSessionExpireInReq() throws InterruptedException
//  {
//    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet-50records.xlsx");
//
//    Assert.assertNotNull(istream);
//
//    ExcelService service = new ExcelService();
//    ServerHierarchyType hierarchyType = ServerHierarchyType.get(testData.HIER_ADMIN.getCode());
//
//    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_AND_UPDATE);
//    config.setHierarchy(hierarchyType);
//
//    ImportHistory hist = importExcelFile(testData.clientRequest.getSessionId(), config.toJSON().toString());
//
//    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.FAILURE);
//
//    hist = ImportHistory.get(hist.getOid());
////    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkTotal());
////    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkProgress());
////    Assert.assertEquals(new Long(3), hist.getImportedRecords());
////    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));
////
////    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(testData.clientRequest.getSessionId(), "0001", testData.DISTRICT.getCode());
////
////    Assert.assertNotNull(object);
////    Assert.assertEquals("Test", object.getLocalizedDisplayLabel());
//  }

  @Test
  @Request
  public void testNewAndUpdate() throws InterruptedException
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet2.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(testData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_AND_UPDATE);
    config.setHierarchy(hierarchyType);

    ImportHistory hist = importExcelFile(testData.clientRequest.getSessionId(), config.toJSON().toString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.SUCCESS);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkTotal());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkProgress());
    Assert.assertEquals(new Long(3), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(testData.clientRequest.getSessionId(), "0001", testData.DISTRICT.getCode());

    Assert.assertNotNull(object);
    Assert.assertEquals("Test", object.getLocalizedDisplayLabel());

    Geometry geometry = object.getGeometry();

    Assert.assertNotNull(geometry);

    Double lat = new Double(2.232343);
    Double lon = new Double(1.134232);

    GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
    Point expected = new Point(new CoordinateSequence2D(lon, lat), factory);

    Assert.assertEquals(expected, geometry);

    GeoObject coloradoDistOne = ServiceFactory.getRegistryService().getGeoObjectByCode(testData.clientRequest.getSessionId(), testData.CO_D_ONE.getCode(), testData.DISTRICT.getCode());

    Double cd1_lat = new Double(4.3333);
    Double cd1_lon = new Double(1.222);

    GeometryFactory cd1_factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
    Point cd1_expected = new Point(new CoordinateSequence2D(cd1_lon, cd1_lat), cd1_factory);

    Geometry cd1_geometry = coloradoDistOne.getGeometry();
    Assert.assertEquals(cd1_expected, cd1_geometry);

    JSONObject json = new JSONObject(new ETLService().getImportErrors(testData.clientRequest.getSessionId(), hist.getOid(), false, 100, 1).toString());

    Assert.assertEquals(0, json.getJSONArray("results").length());
  }

  @Test
  @Request
  public void testUpdateOnly() throws InterruptedException
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet2.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(testData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null, ImportStrategy.UPDATE_ONLY);
    config.setHierarchy(hierarchyType);
    
    ImportHistory hist = importExcelFile(testData.clientRequest.getSessionId(), config.toJSON().toString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.FEEDBACK);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkTotal());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkProgress());
    Assert.assertEquals(new Long(1), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.IMPORT_RESOLVE, hist.getStage().get(0));

    try
    {
      ServiceFactory.getRegistryService().getGeoObjectByCode(testData.clientRequest.getSessionId(), "0001", testData.DISTRICT.getCode());

      Assert.fail();
    }
    catch (SmartExceptionDTO e)
    {
      // Expected
      if (!e.getType().equals(DataNotFoundException.CLASS))
      {
        throw new RuntimeException(e);
      }
    }

    GeoObject coloradoDistOne = ServiceFactory.getRegistryService().getGeoObjectByCode(testData.clientRequest.getSessionId(), testData.CO_D_ONE.getCode(), testData.DISTRICT.getCode());

    Double cd1_lat = new Double(4.3333);
    Double cd1_lon = new Double(1.222);

    GeometryFactory cd1_factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
    Point cd1_expected = new Point(new CoordinateSequence2D(cd1_lon, cd1_lat), cd1_factory);

    Geometry cd1_geometry = coloradoDistOne.getGeometry();
    Assert.assertEquals(cd1_expected, cd1_geometry);

    JSONObject json = new JSONObject(new ETLService().getImportErrors(testData.clientRequest.getSessionId(), hist.getOid(), false, 100, 1).toString());

    Assert.assertEquals(2, json.getJSONArray("results").length());
  }
  
  /**
   * Tests to make sure that we are recording the correct amount of import errors.
   * 
   * @throws InterruptedException
   */
  @Test
  @Request
  public void testUpdateErrorCount() throws InterruptedException
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet3.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(testData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_ONLY);
    config.setHierarchy(hierarchyType);
    
    // First, import the spreadsheet. It should be succesful
    ImportHistory hist = importExcelFile(testData.clientRequest.getSessionId(), config.toJSON().toString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.SUCCESS);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(10), hist.getWorkTotal());
    Assert.assertEquals(new Long(10), hist.getWorkProgress());
    Assert.assertEquals(new Long(10), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));
    
    // Import a second spreadsheet, which should have a few duplicate records.
    InputStream istream2 = this.getClass().getResourceAsStream("/test-spreadsheet4.xlsx");
    GeoObjectImportConfiguration config2 = this.getTestConfiguration(istream2, service, null, ImportStrategy.NEW_ONLY);
    config2.setHierarchy(hierarchyType);
    ImportHistory hist2 = importExcelFile(testData.clientRequest.getSessionId(), config2.toJSON().toString());

    SchedulerTestUtils.waitUntilStatus(hist2.getOid(), AllJobStatus.FEEDBACK);

    hist2 = ImportHistory.get(hist2.getOid());
    Assert.assertEquals(new Long(17), hist2.getWorkTotal());
    Assert.assertEquals(new Long(17), hist2.getWorkProgress());
    Assert.assertEquals(new Long(7), hist2.getImportedRecords());
    Assert.assertEquals(ImportStage.IMPORT_RESOLVE, hist2.getStage().get(0));

    JSONObject json = new JSONObject(new ETLService().getImportErrors(testData.clientRequest.getSessionId(), hist2.getOid(), false, 100, 1).toString());

    Assert.assertEquals(10, json.getJSONArray("results").length());
  }

  @Test
  @Request
  public void testCreateOnly() throws InterruptedException
  {
    // USATestData.CO_D_ONE.delete();

    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet2.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_ONLY);
    config.setHierarchy(hierarchyType);

    ImportHistory hist = importExcelFile(testData.clientRequest.getSessionId(), config.toJSON().toString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.FEEDBACK);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkTotal());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkProgress());
    Assert.assertEquals(new Long(2), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.IMPORT_RESOLVE, hist.getStage().get(0));

    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(testData.clientRequest.getSessionId(), "0001", USATestData.DISTRICT.getCode());

    Assert.assertNotNull(object);
    Assert.assertEquals("Test", object.getLocalizedDisplayLabel());

    Geometry geometry = object.getGeometry();

    Assert.assertNotNull(geometry);

    Double lat = new Double(2.232343);
    Double lon = new Double(1.134232);

    GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
    Point expected = new Point(new CoordinateSequence2D(lon, lat), factory);

    Assert.assertEquals(expected, geometry);

    GeoObject coloradoDistOne = ServiceFactory.getRegistryService().getGeoObjectByCode(testData.clientRequest.getSessionId(), USATestData.CO_D_ONE.getCode(), USATestData.DISTRICT.getCode());

    Double cd1_lat = new Double(80);
    Double cd1_lon = new Double(110);

    GeometryFactory cd1_factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
    Point cd1_expected = new Point(new CoordinateSequence2D(cd1_lon, cd1_lat), cd1_factory);

    Geometry cd1_geometry = coloradoDistOne.getGeometry();
    Assert.assertEquals(cd1_expected, cd1_geometry);

    JSONObject json = new JSONObject(new ETLService().getImportErrors(testData.clientRequest.getSessionId(), hist.getOid(), false, 100, 1).toString());

    Assert.assertEquals(1, json.getJSONArray("results").length());
  }
  
  @Test
  @Request
  public void testErrorSerializeParents() throws InterruptedException
  {
    TestGeoObjectInfo state00 = testData.newTestGeoObjectInfo("00", testData.STATE);
    state00.setCode("00");
    state00.setDisplayLabel("Test Label");
    state00.setRegistryId(ServiceFactory.getIdService().getUids(1)[0]);
    state00.apply();
    testData.USA.addChild(state00, testData.HIER_ADMIN);

    InputStream istream = this.getClass().getResourceAsStream("/parent-test.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(testData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_ONLY);
    config.setHierarchy(hierarchyType);
    config.addParent(new Location(testData.COUNTRY.getServerObject(), hierarchyType, new BasicColumnFunction("Parent Country"), ParentMatchStrategy.ALL));
    config.addParent(new Location(testData.STATE.getServerObject(), hierarchyType, new BasicColumnFunction("Parent State"), ParentMatchStrategy.ALL));
    config.setStartDate(new Date());
    config.setEndDate(new Date());

    ImportHistory hist = importExcelFile(testData.clientRequest.getSessionId(), config.toJSON().toString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.FEEDBACK);

    JSONObject json = new JSONObject(new ETLService().getImportErrors(testData.clientRequest.getSessionId(), hist.getOid(), false, 100, 1).toString());
    JSONArray errors = json.getJSONArray("results");

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(2), hist.getWorkTotal());
    Assert.assertEquals(new Long(2), hist.getWorkProgress());
    Assert.assertEquals(new Long(1), hist.getErrorCount());
    Assert.assertEquals(new Long(0), hist.getErrorResolvedCount());
    Assert.assertEquals(new Long(1), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.IMPORT_RESOLVE, hist.getStage().get(0));

    Assert.assertEquals(1, errors.length());

    JSONObject error = errors.getJSONObject(0);

    Assert.assertTrue(error.has("id"));

    Assert.assertEquals(DuplicateGeoObjectCodeException.CLASS, error.getJSONObject("exception").getString("type"));

    JSONObject object = error.getJSONObject("object");
    Assert.assertTrue(object.has("geoObject"));
    Assert.assertTrue(object.has("parents"));
    Assert.assertTrue(object.getJSONArray("parents").length() > 0);

    ServerParentTreeNodeOverTime parentsOverTime = ServerParentTreeNodeOverTime.fromJSON(testData.DISTRICT.getServerObject(), object.getJSONArray("parents").toString());

    Assert.assertEquals(1, parentsOverTime.getHierarchies().size());

    List<ServerParentTreeNode> nodes = parentsOverTime.getEntries(testData.HIER_ADMIN.getServerObject());
    Assert.assertEquals(1, nodes.size());

    // TODO The fromJSON doesn't seem to be reading the json correctly...
    // List<ServerParentTreeNode> ptns = nodes.get(0).getParents();
    // Assert.assertEquals(2, ptns.size());
    //
    // Assert.assertEquals(testData.USA.getCode(),
    // ptns.get(0).getGeoObject().getCode());
    // Assert.assertEquals(testData.COLORADO.getCode(),
    // ptns.get(1).getGeoObject().getCode());

    // Test Resolving the error and then completing the import
    ImportErrorQuery ieq = new ImportErrorQuery(new QueryFactory());
    Assert.assertEquals(1, ieq.getCount());
    Assert.assertEquals(ErrorResolution.UNRESOLVED.name(), ieq.getIterator().next().getResolution());

    JSONObject resolution = new JSONObject();
    resolution.put("importErrorId", error.get("id"));
    resolution.put("resolution", ErrorResolution.IGNORE);
    resolution.put("historyId", hist.getOid());

    new ETLService().submitImportErrorResolution(testData.clientRequest.getSessionId(), resolution.toString());

    Assert.assertEquals(ErrorResolution.IGNORE.name(), ieq.getIterator().next().getResolution());

    new ETLService().resolveImport(testData.clientRequest.getSessionId(), hist.getOid());

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(2), hist.getWorkTotal());
    Assert.assertEquals(new Long(2), hist.getWorkProgress());
    Assert.assertEquals(new Long(1), hist.getErrorCount());
    Assert.assertEquals(new Long(0), hist.getErrorResolvedCount());
    Assert.assertEquals(new Long(1), hist.getImportedRecords());
    Assert.assertEquals(AllJobStatus.SUCCESS, hist.getStatus().get(0));
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    Assert.assertEquals(0, ieq.getCount());
  }

  @Test
  @Request
  public void testGetImportDetails() throws InterruptedException
  {
    TestGeoObjectInfo one = testData.newTestGeoObjectInfo("0001", testData.DISTRICT);
    one.setCode("0001");
    one.delete();

    TestGeoObjectInfo two = testData.newTestGeoObjectInfo("0002", testData.DISTRICT);
    two.setCode("0002");
    two.delete();

    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet2.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(testData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_ONLY);
    config.setHierarchy(hierarchyType);

    Date startDate = new Date();
    Date endDate = new Date();
    config.setStartDate(startDate);
    config.setEndDate(endDate);

    ImportHistory hist = importExcelFile(testData.clientRequest.getSessionId(), config.toJSON().toString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.FEEDBACK);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkTotal());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkProgress());
    Assert.assertEquals(new Long(2), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.IMPORT_RESOLVE, hist.getStage().get(0));

    JSONObject jo = new JSONObject(new ETLService().getImportDetails(testData.clientRequest.getSessionId(), hist.getOid(), false, 100, 1).toString());

    SimpleDateFormat format = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);
    format.setTimeZone(TimeZone.getTimeZone("GMT"));
    Assert.assertEquals(format.format(startDate), jo.getJSONObject("configuration").getString("startDate"));
    Assert.assertEquals(format.format(endDate), jo.getJSONObject("configuration").getString("endDate"));

    JSONObject importErrors = jo.getJSONObject("importErrors");
    JSONArray results = importErrors.getJSONArray("results");

    Assert.assertEquals(1, results.length());
  }

  private GeoObjectImportConfiguration getTestConfiguration(InputStream istream, ExcelService service, AttributeTermType attributeTerm, ImportStrategy strategy)
  {
    JSONObject result = service.getExcelConfiguration(testData.clientRequest.getSessionId(), testData.DISTRICT.getCode(), null, null, "test-spreadsheet.xlsx", istream, strategy);
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
