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
package net.geoprism.registry.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.business.SmartExceptionDTO;
import com.runwaysdk.constants.VaultProperties;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.gis.geo.LocatedIn;
import com.runwaysdk.system.gis.geo.Synonym;
import com.runwaysdk.system.gis.geo.SynonymQuery;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutableJob;
import com.runwaysdk.system.scheduler.JobHistory;
import com.runwaysdk.system.scheduler.JobHistoryRecord;
import com.runwaysdk.system.scheduler.JobHistoryRecordQuery;
import com.runwaysdk.system.scheduler.SchedulerManager;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

import net.geoprism.data.importer.BasicColumnFunction;
import net.geoprism.registry.DataNotFoundException;
import net.geoprism.registry.etl.ETLService;
import net.geoprism.registry.etl.FormatSpecificImporterFactory.FormatImporterType;
import net.geoprism.registry.etl.ImportConfiguration;
import net.geoprism.registry.etl.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.etl.ImportError;
import net.geoprism.registry.etl.ImportError.Resolution;
import net.geoprism.registry.etl.ImportErrorQuery;
import net.geoprism.registry.etl.ImportHistory;
import net.geoprism.registry.etl.ImportStage;
import net.geoprism.registry.etl.ObjectImporterFactory.ObjectImportType;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.io.Location;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerParentTreeNode;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.USATestData;
import net.geoprism.registry.view.ServerParentTreeNodeOverTime;

public class GeoObjectImporterTest
{
  protected static USATestData        testData;

  private final Integer ROW_COUNT = 3;
  
  
  @BeforeClass
  public static void setUpClass()
  {
    testData = USATestData.newTestDataForClass();
    testData.setUpMetadata();
    
    SchedulerManager.start();
  }
  
  @AfterClass
  public static void cleanUpClass()
  {
    if (testData != null)
    {
      testData.tearDownMetadata();
    }
    
    SchedulerManager.shutdown();
  }
  
  @Before
  public void setUp()
  {
    if (testData != null)
    {
      testData.setUpInstanceData();
    }
    
    clearData();
  }

  @After
  public void tearDown() throws IOException
  {
    if (testData != null)
    {
      testData.tearDownInstanceData();
    }
    
    FileUtils.deleteDirectory(new File(VaultProperties.getPath("vault.default"), "files"));
    
    clearData();
  }
  
  @Request
  private static void clearData()
  {
    ImportErrorQuery ieq = new ImportErrorQuery(new QueryFactory());
    OIterator<? extends ImportError> ieit = ieq.getIterator();
    
    while (ieit.hasNext())
    {
      ieit.next().delete();
    }
    
    
    JobHistoryRecordQuery query = new JobHistoryRecordQuery(new QueryFactory());
    OIterator<? extends JobHistoryRecord> jhrs = query.getIterator();

    while (jhrs.hasNext())
    {
      JobHistoryRecord jhr = jhrs.next();
      
      JobHistory hist = jhr.getChild();
      ExecutableJob job = jhr.getParent();
      jhr.delete();
//      hist.delete();
      job.delete();
    }
    
    
    SynonymQuery sq = new SynonymQuery(new QueryFactory());
    sq.WHERE(sq.getDisplayLabel().localize().EQ("00"));
    OIterator<? extends Synonym> it = sq.getIterator();
    
    while (it.hasNext())
    {
      it.next().delete();
    }
  }
  
  private void waitUntilStatus(JobHistory hist, AllJobStatus status) throws InterruptedException
  {
    int waitTime = 0;
    while (true)
    {
      hist = JobHistory.get(hist.getOid());
      if (hist.getStatus().get(0) == status)
      {
        break;
      }
      else if (hist.getStatus().get(0) == AllJobStatus.SUCCESS || hist.getStatus().get(0) == AllJobStatus.FAILURE)
      {
        Assert.fail("Job has a finished status [" + hist.getStatus().get(0) + "] which is not what we expected.");
      }
      
      Thread.sleep(10);

      waitTime += 10;
      if (waitTime > 2000000)
      {
        String extra = "";
        if (hist.getStatus().get(0).equals(AllJobStatus.FEEDBACK))
        {
          extra = new ETLService().getImportErrors(Session.getCurrentSession().getOid(), hist.getOid(), 100, 1).toString();
          
          extra = extra + " " + ((ImportHistory)hist).getValidationProblems();
        }
        
        Assert.fail("Job was never scheduled (status is " + hist.getStatus().get(0).getEnumName() + ") " + extra);
        return;
      }
    }
    
    Thread.sleep(100);
  }
  
  private ImportHistory importExcelFile(String sessionId, String config)
  {
    String retConfig = new ETLService().doImport(sessionId, config).toString();
    
    GeoObjectImportConfiguration configuration = (GeoObjectImportConfiguration) ImportConfiguration.build(retConfig, true);
    
    String historyId = configuration.getHistoryId();
    
    return ImportHistory.get(historyId);
  }
  
  @Test
  @Request
  public void testNewAndUpdate() throws InterruptedException
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
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());
    
    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_AND_UPDATE);
    config.setHierarchy(hierarchyType);
    
    ImportHistory hist = importExcelFile(testData.adminClientRequest.getSessionId(), config.toJSON().toString());
    
    this.waitUntilStatus(hist, AllJobStatus.SUCCESS);
    
    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkTotal());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkProgress());
    Assert.assertEquals(new Long(3), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));
    
    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(testData.adminClientRequest.getSessionId(), "0001", testData.DISTRICT.getCode());

    Assert.assertNotNull(object);
    Assert.assertEquals("Test", object.getLocalizedDisplayLabel());

    Geometry geometry = object.getGeometry();

    Assert.assertNotNull(geometry);

    Double lat = new Double(2.232343);
    Double lon = new Double(1.134232);

    GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
    Point expected = new Point(new CoordinateSequence2D(lon, lat), factory);

    Assert.assertEquals(expected, geometry);
    
    GeoObject coloradoDistOne = ServiceFactory.getRegistryService().getGeoObjectByCode(testData.adminClientRequest.getSessionId(), testData.CO_D_ONE.getCode(), testData.DISTRICT.getCode());
    
    Double cd1_lat = new Double(4.3333);
    Double cd1_lon = new Double(1.222);

    GeometryFactory cd1_factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
    Point cd1_expected = new Point(new CoordinateSequence2D(cd1_lon, cd1_lat), cd1_factory);

    Geometry cd1_geometry = coloradoDistOne.getGeometry();
    Assert.assertEquals(cd1_expected, cd1_geometry);
    
    JSONArray ja = new ETLService().getImportErrors(testData.adminClientRequest.getSessionId(), hist.getOid(), 100, 1);
    
    Assert.assertEquals(0, ja.length());
  }
  
  @Test
  @Request
  public void testUpdateOnly() throws InterruptedException
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
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());
    
    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null, ImportStrategy.UPDATE_ONLY);
    config.setHierarchy(hierarchyType);
    
    ImportHistory hist = importExcelFile(testData.adminClientRequest.getSessionId(), config.toJSON().toString());
    
    this.waitUntilStatus(hist, AllJobStatus.FEEDBACK);
    
    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkTotal());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkProgress());
    Assert.assertEquals(new Long(1), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.IMPORT_RESOLVE, hist.getStage().get(0));
    
    try
    {
      ServiceFactory.getRegistryService().getGeoObjectByCode(testData.adminClientRequest.getSessionId(), "0001", testData.DISTRICT.getCode());
      
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
    
    GeoObject coloradoDistOne = ServiceFactory.getRegistryService().getGeoObjectByCode(testData.adminClientRequest.getSessionId(), testData.CO_D_ONE.getCode(), testData.DISTRICT.getCode());
    
    Double cd1_lat = new Double(4.3333);
    Double cd1_lon = new Double(1.222);

    GeometryFactory cd1_factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
    Point cd1_expected = new Point(new CoordinateSequence2D(cd1_lon, cd1_lat), cd1_factory);

    Geometry cd1_geometry = coloradoDistOne.getGeometry();
    Assert.assertEquals(cd1_expected, cd1_geometry);
    
    JSONArray ja = new ETLService().getImportErrors(testData.adminClientRequest.getSessionId(), hist.getOid(), 100, 1);
    
    Assert.assertEquals(2, ja.length());
  }
  
  @Test
  @Request
  public void testCreateOnly() throws InterruptedException
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
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());
    
    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_ONLY);
    config.setHierarchy(hierarchyType);
    
    ImportHistory hist = importExcelFile(testData.adminClientRequest.getSessionId(), config.toJSON().toString());
    
    this.waitUntilStatus(hist, AllJobStatus.FEEDBACK);
    
    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkTotal());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkProgress());
    Assert.assertEquals(new Long(2), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.IMPORT_RESOLVE, hist.getStage().get(0));
    
    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(testData.adminClientRequest.getSessionId(), "0001", testData.DISTRICT.getCode());

    Assert.assertNotNull(object);
    Assert.assertEquals("Test", object.getLocalizedDisplayLabel());

    Geometry geometry = object.getGeometry();

    Assert.assertNotNull(geometry);

    Double lat = new Double(2.232343);
    Double lon = new Double(1.134232);

    GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
    Point expected = new Point(new CoordinateSequence2D(lon, lat), factory);

    Assert.assertEquals(expected, geometry);
    
    GeoObject coloradoDistOne = ServiceFactory.getRegistryService().getGeoObjectByCode(testData.adminClientRequest.getSessionId(), testData.CO_D_ONE.getCode(), testData.DISTRICT.getCode());
    
    Double cd1_lat = new Double(80);
    Double cd1_lon = new Double(110);

    GeometryFactory cd1_factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
    Point cd1_expected = new Point(new CoordinateSequence2D(cd1_lon, cd1_lat), cd1_factory);

    Geometry cd1_geometry = coloradoDistOne.getGeometry();
    Assert.assertEquals(cd1_expected, cd1_geometry);
    
    JSONArray ja = new ETLService().getImportErrors(testData.adminClientRequest.getSessionId(), hist.getOid(), 100, 1);
    
    Assert.assertEquals(1, ja.length());
  }
  
  @Test
  @Request
  public void testErrorSerializeParents() throws InterruptedException
  {
    TestGeoObjectInfo state00 = testData.newTestGeoObjectInfo("00", testData.STATE);
    state00.setCode("00");
    state00.setDisplayLabel("Test Label");
    state00.setRegistryId(ServiceFactory.getIdService().getUids(1)[0]);
    state00.apply(new Date());
    testData.USA.addChild(state00, testData.LocatedIn);
    
    TestGeoObjectInfo one = testData.newTestGeoObjectInfo("0001", testData.DISTRICT);
    one.setCode("0001");
    one.delete();

    TestGeoObjectInfo two = testData.newTestGeoObjectInfo("0002", testData.DISTRICT);
    two.setCode("0002");
    two.delete();
    
    InputStream istream = this.getClass().getResourceAsStream("/parent-test.xlsx");
    
    Assert.assertNotNull(istream);
    
    ExcelService service = new ExcelService();
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());
    
    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_ONLY);
    config.setHierarchy(hierarchyType);
    config.addParent(new Location(testData.COUNTRY.getServerObject(), new BasicColumnFunction("Parent Country")));
    config.addParent(new Location(testData.STATE.getServerObject(), new BasicColumnFunction("Parent State")));
    config.setStartDate(new Date());
    config.setEndDate(new Date());
    
    ImportHistory hist = importExcelFile(testData.adminClientRequest.getSessionId(), config.toJSON().toString());
    
    this.waitUntilStatus(hist, AllJobStatus.FEEDBACK);
    
    JSONArray errors = new ETLService().getImportErrors(testData.adminClientRequest.getSessionId(), hist.getOid(), 100, 1);
    
    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(2), hist.getWorkTotal());
    Assert.assertEquals(new Long(2), hist.getWorkProgress());
    Assert.assertEquals(new Long(1), hist.getErrorCount());
    Assert.assertEquals(new Long(0), hist.getErrorResolvedCount());
    Assert.assertEquals(new Long(1), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.IMPORT_RESOLVE, hist.getStage().get(0));
    
    Assert.assertEquals(1, errors.length());
    
    JSONObject error = errors.getJSONObject(0);
    System.out.println(error);
    
    Assert.assertTrue(error.has("importErrorId"));
    
    Assert.assertEquals("com.runwaysdk.dataaccess.DuplicateDataException", error.getJSONObject("exception").getString("type"));
    
    JSONObject object = error.getJSONObject("object");
    Assert.assertTrue(object.has("geoObject"));
    Assert.assertTrue(object.has("parents"));
    Assert.assertTrue(object.getJSONArray("parents").length() > 0);
    
    ServerParentTreeNodeOverTime parentsOverTime = ServerParentTreeNodeOverTime.fromJSON(testData.DISTRICT.getServerObject(), object.getJSONArray("parents").toString());
    
    Assert.assertEquals(1, parentsOverTime.getHierarchies().size());
    
    List<ServerParentTreeNode> nodes = parentsOverTime.getEntries(testData.LocatedIn.getServerObject());
    Assert.assertEquals(1, nodes.size());
    
    // TODO The fromJSON doesn't seem to be reading the json correctly...
//    List<ServerParentTreeNode> ptns = nodes.get(0).getParents();
//    Assert.assertEquals(2, ptns.size());
//    
//    Assert.assertEquals(testData.USA.getCode(), ptns.get(0).getGeoObject().getCode());
//    Assert.assertEquals(testData.COLORADO.getCode(), ptns.get(1).getGeoObject().getCode());
    
    
    // Test Resolving the error and then completing the import
    ImportErrorQuery ieq = new ImportErrorQuery(new QueryFactory());
    Assert.assertEquals(1, ieq.getCount());
    Assert.assertEquals(Resolution.UNRESOLVED.name(), ieq.getIterator().next().getResolution());
    
    JSONObject resolution = new JSONObject();
    resolution.put("importErrorId", error.get("importErrorId"));
    resolution.put("resolution", Resolution.IGNORE);
    resolution.put("historyId", hist.getOid());
    
    new ETLService().submitImportErrorResolution(testData.adminClientRequest.getSessionId(), resolution.toString());
    
    Assert.assertEquals(Resolution.IGNORE.name(), ieq.getIterator().next().getResolution());
    
    new ETLService().resolveImport(testData.adminClientRequest.getSessionId(), hist.getOid());
    
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
  
  private GeoObjectImportConfiguration getTestConfiguration(InputStream istream, ExcelService service, AttributeTermType attributeTerm, ImportStrategy strategy)
  {
    JSONObject result = service.getExcelConfiguration(testData.adminClientRequest.getSessionId(), testData.DISTRICT.getCode(), null, null, "test-spreadsheet.xlsx", istream, strategy);
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
