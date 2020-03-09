/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultTerms.GeoObjectStatusTerm;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.constants.VaultProperties;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionFacade;
import com.runwaysdk.system.gis.geo.LocatedIn;
import com.runwaysdk.system.gis.geo.Synonym;
import com.runwaysdk.system.gis.geo.SynonymQuery;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutableJob;
import com.runwaysdk.system.scheduler.JobHistory;
import com.runwaysdk.system.scheduler.JobHistoryRecord;
import com.runwaysdk.system.scheduler.JobHistoryRecordQuery;
import com.runwaysdk.system.scheduler.SchedulerManager;

import net.geoprism.data.importer.BasicColumnFunction;
import net.geoprism.data.importer.ShapefileFunction;
import net.geoprism.registry.etl.ETLService;
import net.geoprism.registry.etl.FormatSpecificImporterFactory.FormatImporterType;
import net.geoprism.registry.etl.ImportConfiguration;
import net.geoprism.registry.etl.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.etl.ImportError;
import net.geoprism.registry.etl.ImportErrorQuery;
import net.geoprism.registry.etl.ImportHistory;
import net.geoprism.registry.etl.ImportStage;
import net.geoprism.registry.etl.ObjectImporterFactory.ObjectImportType;
import net.geoprism.registry.etl.ValidationProblem;
import net.geoprism.registry.etl.ValidationProblemQuery;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.io.Location;
import net.geoprism.registry.io.LocationBuilder;
import net.geoprism.registry.io.LookupType;
import net.geoprism.registry.io.PostalCodeFactory;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.query.postgres.CodeRestriction;
import net.geoprism.registry.query.postgres.GeoObjectQuery;
import net.geoprism.registry.test.USATestData;

public class ShapefileServiceTest
{
  protected USATestData        testData;

  private AttributeTermType    testTerm;

  private AttributeIntegerType testInteger;

  @Before
  public void setUp()
  {
    this.testData = USATestData.newTestData(false);

    AttributeTermType testTerm = (AttributeTermType) AttributeType.factory("testTerm", new LocalizedValue("testTermLocalName"), new LocalizedValue("testTermLocalDescrip"), AttributeTermType.TYPE, false, false, false);
    this.testTerm = (AttributeTermType) ServiceFactory.getRegistryService().createAttributeType(testData.adminClientRequest.getSessionId(), this.testData.STATE.getCode(), testTerm.toJSON().toString());

    AttributeIntegerType testInteger = (AttributeIntegerType) AttributeType.factory("testInteger", new LocalizedValue("testIntegerLocalName"), new LocalizedValue("testIntegerLocalDescrip"), AttributeIntegerType.TYPE, false, false, false);
    this.testInteger = (AttributeIntegerType) ServiceFactory.getRegistryService().createAttributeType(testData.adminClientRequest.getSessionId(), this.testData.STATE.getCode(), testInteger.toJSON().toString());

    reload();

    clearData();
  }

  @After
  public void tearDown() throws IOException
  {
    testData.cleanUp();

    FileUtils.deleteDirectory(new File(VaultProperties.getPath("vault.default"), "files"));

    clearData();
  }

  @BeforeClass
  @Request
  public static void classSetUp()
  {
    clearData();

    SchedulerManager.start();
  }

  @AfterClass
  @Request
  public static void classTearDown()
  {
    SchedulerManager.shutdown();
  }

  @Request
  private static void clearData()
  {
    ValidationProblemQuery vpq = new ValidationProblemQuery(new QueryFactory());
    OIterator<? extends ValidationProblem> vpit = vpq.getIterator();
    while (vpit.hasNext())
    {
      vpit.next().delete();
    }
    
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
      
      ExecutableJob job = jhr.getParent();
      jhr.delete();
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

  // @Before
  // public void setUp()
  // {
  // this.testData = USATestData.newTestData(false);
  //
  // this.adminCR = testData.adminClientRequest;
  //
  // AttributeTermType testTerm = (AttributeTermType)
  // AttributeType.factory("testTerm", new LocalizedValue("testTermLocalName"),
  // new LocalizedValue("testTermLocalDescrip"), AttributeTermType.TYPE, false,
  // false, false);
  // this.testTerm = (AttributeTermType)
  // ServiceFactory.getRegistryService().createAttributeType(this.adminCR.getSessionId(),
  // this.testData.STATE.getCode(), testTerm.toJSON().toString());
  //
  // AttributeIntegerType testInteger = (AttributeIntegerType)
  // AttributeType.factory("testInteger", new
  // LocalizedValue("testIntegerLocalName"), new
  // LocalizedValue("testIntegerLocalDescrip"), AttributeIntegerType.TYPE,
  // false, false, false);
  // this.testInteger = (AttributeIntegerType)
  // ServiceFactory.getRegistryService().createAttributeType(this.adminCR.getSessionId(),
  // this.testData.STATE.getCode(), testInteger.toJSON().toString());
  //
  // reload();
  // }
  //
  // @After
  // public void tearDown() throws IOException
  // {
  // testData.cleanUp();
  //
  // FileUtils.deleteDirectory(new
  // File(VaultProperties.getPath("vault.default"), "files"));
  // }

  @Request
  public void reload()
  {
    /*
     * Reload permissions for the new attributes
     */
    SessionFacade.getSessionForRequest(testData.adminClientRequest.getSessionId()).reloadPermissions();
  }

  @Test
  @Request
  public void testGetAttributeInformation()
  {
    PostalCodeFactory.remove(testData.STATE.getServerObject());

    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();
    JSONObject result = service.getShapefileConfiguration(this.testData.adminClientRequest.getSessionId(), testData.STATE.getCode(), null, null, "cb_2017_us_state_500k.zip", istream, ImportStrategy.NEW_AND_UPDATE);

    Assert.assertFalse(result.getBoolean(GeoObjectImportConfiguration.HAS_POSTAL_CODE));

    JSONObject type = result.getJSONObject(GeoObjectImportConfiguration.TYPE);

    Assert.assertNotNull(type);

    JSONArray tAttributes = type.getJSONArray(GeoObjectType.JSON_ATTRIBUTES);

    Assert.assertEquals(4, tAttributes.length());

    boolean hasCode = false;

    for (int i = 0; i < tAttributes.length(); i++)
    {
      JSONObject tAttribute = tAttributes.getJSONObject(i);
      String code = tAttribute.getString(AttributeType.JSON_CODE);

      if (code.equals(GeoObjectType.JSON_CODE))
      {
        hasCode = true;
        Assert.assertTrue(tAttribute.has("required"));
        Assert.assertTrue(tAttribute.getBoolean("required"));
      }
    }

    Assert.assertTrue(hasCode);

    JSONArray hierarchies = result.getJSONArray(GeoObjectImportConfiguration.HIERARCHIES);

    Assert.assertEquals(1, hierarchies.length());

    JSONObject hierarchy = hierarchies.getJSONObject(0);

    Assert.assertNotNull(hierarchy.getString("label"));

    JSONObject sheet = result.getJSONObject("sheet");

    Assert.assertNotNull(sheet);
    Assert.assertEquals("cb_2017_us_state_500k", sheet.getString("name"));

    JSONObject attributes = sheet.getJSONObject("attributes");

    Assert.assertNotNull(attributes);

    JSONArray fields = attributes.getJSONArray(GeoObjectImportConfiguration.TEXT);

    Assert.assertEquals(9, fields.length());
    Assert.assertEquals("STATEFP", fields.getString(0));

    Assert.assertEquals(2, attributes.getJSONArray(GeoObjectImportConfiguration.NUMERIC).length());
    Assert.assertEquals(0, attributes.getJSONArray(AttributeBooleanType.TYPE).length());
    Assert.assertEquals(0, attributes.getJSONArray(AttributeDateType.TYPE).length());
  }

  @Test
  @Request
  public void testGetAttributeInformationPostalCode()
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    ServerGeoObjectType type = testData.STATE.getServerObject();

    PostalCodeFactory.addPostalCode(type, new LocationBuilder()
    {
      @Override
      public Location build(ShapefileFunction function)
      {
        return null;
      }
    });

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();
    JSONObject result = service.getShapefileConfiguration(this.testData.adminClientRequest.getSessionId(), testData.STATE.getCode(), null, null, "cb_2017_us_state_500k.zip", istream, ImportStrategy.NEW_AND_UPDATE);

    Assert.assertTrue(result.getBoolean(GeoObjectImportConfiguration.HAS_POSTAL_CODE));
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
      if (waitTime > 20000000)
      {
//        String extra = "";
//        if (hist.getStatus().get(0).equals(AllJobStatus.FEEDBACK))
//        {
//          extra = new ETLService().getImportErrors(Session.getCurrentSession().getOid(), hist.getOid(), false, 100, 1).toString();
//
//          extra = extra + " " + ( (ImportHistory) hist ).getValidationProblems();
//        }

        Assert.fail("Job was never scheduled (status is " + hist.getStatus().get(0).getEnumName() + ") ");
        return;
      }
    }

    Thread.sleep(100);
  }

  @Test
  @Request
  public void testImportShapefile() throws InterruptedException
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null);
    config.setHierarchy(hierarchyType);

    ImportHistory hist = importShapefile(this.testData.adminClientRequest.getSessionId(), config.toJSON().toString());

    this.waitUntilStatus(hist, AllJobStatus.SUCCESS);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(56), hist.getWorkTotal());
    Assert.assertEquals(new Long(56), hist.getWorkProgress());
    Assert.assertEquals(new Long(56), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(this.testData.adminClientRequest.getSessionId(), "01", testData.STATE.getCode());

    Assert.assertNotNull(object);
    Assert.assertNotNull(object.getGeometry());
    Assert.assertEquals("Alabama", object.getLocalizedDisplayLabel());
    Assert.assertEquals(GeoObjectStatusTerm.ACTIVE.code, object.getStatus().getCode());
  }

  @Test
  @Request
  public void testUpdateShapefile() throws InterruptedException
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null);
    config.setHierarchy(hierarchyType);

    ImportHistory hist = importShapefile(this.testData.adminClientRequest.getSessionId(), config.toJSON().toString());

    this.waitUntilStatus(hist, AllJobStatus.SUCCESS);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(AllJobStatus.SUCCESS, hist.getStatus().get(0));
    Assert.assertEquals(new Long(56), hist.getWorkTotal());
    Assert.assertEquals(new Long(56), hist.getWorkProgress());
    Assert.assertEquals(new Long(56), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    /*
     * Import the shapefile twice
     */
    JSONObject config2 = new JSONObject(hist.getConfigJson());
    config2.remove("historyId");
    ImportHistory hist2 = importShapefile(this.testData.adminClientRequest.getSessionId(), config2.toString());
    Assert.assertNotSame(hist.getOid(), hist2.getOid());

    this.waitUntilStatus(hist2, AllJobStatus.SUCCESS);

    hist2 = ImportHistory.get(hist2.getOid());
    Assert.assertEquals(AllJobStatus.SUCCESS, hist.getStatus().get(0));
    Assert.assertEquals(new Long(56), hist2.getWorkTotal());
    Assert.assertEquals(new Long(56), hist2.getWorkProgress());
    Assert.assertEquals(new Long(56), hist2.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist2.getStage().get(0));

    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(this.testData.adminClientRequest.getSessionId(), "01", testData.STATE.getCode());

    Assert.assertNotNull(object);
    Assert.assertNotNull(object.getGeometry());
    Assert.assertEquals("Alabama", object.getLocalizedDisplayLabel());
  }

  @Test
  @Request
  public void testImportShapefileInteger() throws InterruptedException
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    config.setFunction(this.testInteger.getName(), new BasicColumnFunction("ALAND"));
    config.setHierarchy(hierarchyType);

    ImportHistory hist = importShapefile(this.testData.adminClientRequest.getSessionId(), config.toJSON().toString());

    this.waitUntilStatus(hist, AllJobStatus.SUCCESS);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(56), hist.getWorkTotal());
    Assert.assertEquals(new Long(56), hist.getWorkProgress());
    Assert.assertEquals(new Long(56), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(this.testData.adminClientRequest.getSessionId(), "01", testData.STATE.getCode());

    Assert.assertNotNull(object);
    Assert.assertNotNull(object.getGeometry());
    Assert.assertEquals("Alabama", object.getLocalizedDisplayLabel());
    Assert.assertEquals(131174431216L, object.getValue(this.testInteger.getName()));
  }

  @Test
  @Request
  public void testImportShapefileWithParent() throws InterruptedException
  {
    GeoObject geoObj = ServiceFactory.getRegistryService().newGeoObjectInstance(this.testData.adminClientRequest.getSessionId(), this.testData.COUNTRY.getCode());
    geoObj.setCode("00");
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "Test Label");
    geoObj.setUid(ServiceFactory.getIdService().getUids(1)[0]);

    ServerGeoObjectIF serverGO = new ServerGeoObjectService().apply(geoObj, true, false);
    geoObj = RegistryService.getInstance().getGeoObjectByCode(Session.getCurrentSession().getOid(), serverGO.getCode(), serverGO.getType().getCode());

    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    config.setHierarchy(hierarchyType);
    config.addParent(new Location(this.testData.COUNTRY.getServerObject(), new BasicColumnFunction("LSAD")));

    ImportHistory hist = importShapefile(this.testData.adminClientRequest.getSessionId(), config.toJSON().toString());

    this.waitUntilStatus(hist, AllJobStatus.SUCCESS);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(56), hist.getWorkTotal());
    Assert.assertEquals(new Long(56), hist.getWorkProgress());
    Assert.assertEquals(new Long(56), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    String sessionId = this.testData.adminClientRequest.getSessionId();
    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(sessionId, "01", testData.STATE.getCode());

    Assert.assertEquals("01", object.getCode());

    ParentTreeNode nodes = ServiceFactory.getRegistryService().getParentGeoObjects(sessionId, object.getUid(), config.getType().getCode(), new String[] { this.testData.COUNTRY.getCode() }, false, new Date());

    List<ParentTreeNode> parents = nodes.getParents();

    Assert.assertEquals(1, parents.size());
  }

  @Test
  @Request
  public void testImportShapefileWithParentCode() throws InterruptedException
  {
    GeoObject geoObj = ServiceFactory.getRegistryService().newGeoObjectInstance(this.testData.adminClientRequest.getSessionId(), this.testData.COUNTRY.getCode());
    geoObj.setCode("00");
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "Test Label");
    geoObj.setUid(ServiceFactory.getIdService().getUids(1)[0]);

    ServerGeoObjectIF serverGO = new ServerGeoObjectService().apply(geoObj, true, false);
    geoObj = RegistryService.getInstance().getGeoObjectByCode(Session.getCurrentSession().getOid(), serverGO.getCode(), serverGO.getType().getCode());

    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    config.setParentLookupType(LookupType.CODE);
    config.setHierarchy(hierarchyType);
    config.addParent(new Location(this.testData.COUNTRY.getServerObject(), new BasicColumnFunction("LSAD")));

    ImportHistory hist = importShapefile(this.testData.adminClientRequest.getSessionId(), config.toJSON().toString());

    this.waitUntilStatus(hist, AllJobStatus.SUCCESS);

    hist = ImportHistory.get(hist.getOid());

    Assert.assertEquals(new Long(56), hist.getWorkTotal());
    Assert.assertEquals(new Long(56), hist.getWorkProgress());
    Assert.assertEquals(new Long(56), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    final GeoObjectImportConfiguration test = new GeoObjectImportConfiguration();
    test.fromJSON(hist.getConfigJson(), false);

    Assert.assertEquals(config.getParentLookupType(), test.getParentLookupType());

    String sessionId = this.testData.adminClientRequest.getSessionId();
    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(sessionId, "01", testData.STATE.getCode());

    Assert.assertEquals("01", object.getCode());

    ParentTreeNode nodes = ServiceFactory.getRegistryService().getParentGeoObjects(sessionId, object.getUid(), config.getType().getCode(), new String[] { this.testData.COUNTRY.getCode() }, false, new Date());

    List<ParentTreeNode> parents = nodes.getParents();

    Assert.assertEquals(1, parents.size());
  }

  @Test
  @Request
  public void testImportShapefileWithBadParentCode() throws InterruptedException
  {
    GeoObject geoObj = ServiceFactory.getRegistryService().newGeoObjectInstance(this.testData.adminClientRequest.getSessionId(), this.testData.COUNTRY.getCode());
    geoObj.setCode("00");
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "Test Label");
    geoObj.setUid(ServiceFactory.getIdService().getUids(1)[0]);

    ServerGeoObjectIF serverGO = new ServerGeoObjectService().apply(geoObj, true, false);
    geoObj = RegistryService.getInstance().getGeoObjectByCode(Session.getCurrentSession().getOid(), serverGO.getCode(), serverGO.getType().getCode());

    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    config.setParentLookupType(LookupType.CODE);
    config.setHierarchy(hierarchyType);
    config.addParent(new Location(this.testData.COUNTRY.getServerObject(), new BasicColumnFunction("GEOID")));

    ImportHistory hist = importShapefile(this.testData.adminClientRequest.getSessionId(), config.toJSON().toString());

    this.waitUntilStatus(hist, AllJobStatus.FEEDBACK);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(56), hist.getWorkTotal());
    Assert.assertEquals(new Long(56), hist.getWorkProgress());
    Assert.assertEquals(new Long(0), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.IMPORT_RESOLVE, hist.getStage().get(0));

    final GeoObjectImportConfiguration test = new GeoObjectImportConfiguration();
    test.fromJSON(hist.getConfigJson(), false);

    Assert.assertEquals(config.getParentLookupType(), test.getParentLookupType());
    
//    JSONArray errors = new JSONArray(hist.getErrorJson());
//
//    Assert.assertEquals(0, errors.length());

    // Ensure the geo objects were not created
    GeoObjectQuery query = new GeoObjectQuery(testData.STATE.getServerObject());
    query.setRestriction(new CodeRestriction("01"));

    Assert.assertNull(query.getSingleResult());
  }

  @Test
  @Request
  public void testImportShapefileExcludeParent() throws InterruptedException
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    config.setHierarchy(hierarchyType);

    config.addParent(new Location(this.testData.COUNTRY.getServerObject(), new BasicColumnFunction("LSAD")));
    config.addExclusion(GeoObjectImportConfiguration.PARENT_EXCLUSION, "00");

    ImportHistory hist = importShapefile(this.testData.adminClientRequest.getSessionId(), config.toJSON().toString());

    this.waitUntilStatus(hist, AllJobStatus.SUCCESS);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(56), hist.getWorkTotal());
    Assert.assertEquals(new Long(56), hist.getWorkProgress());
    Assert.assertEquals(new Long(0), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));
    
    // Ensure the geo objects were not created
    GeoObjectQuery query = new GeoObjectQuery(testData.STATE.getServerObject());
    query.setRestriction(new CodeRestriction("01"));

    Assert.assertNull(query.getSingleResult());
  }

  @Test
  @Request
  public void testImportShapefileWithBadParent() throws InterruptedException
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    config.setHierarchy(hierarchyType);

    config.addParent(new Location(this.testData.COUNTRY.getServerObject(), new BasicColumnFunction("LSAD")));

    ImportHistory hist = importShapefile(this.testData.adminClientRequest.getSessionId(), config.toJSON().toString());

    this.waitUntilStatus(hist, AllJobStatus.FEEDBACK);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(56), hist.getWorkTotal());
    Assert.assertEquals(new Long(56), hist.getWorkProgress());
    Assert.assertEquals(new Long(0), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.VALIDATION_RESOLVE, hist.getStage().get(0));
    
    JSONObject page = new ETLService().getReferenceValidationProblems(testData.adminClientRequest.getSessionId(), hist.getOid(), 100, 1);
    JSONArray results = page.getJSONArray("results");
    Assert.assertEquals(1, results.length());

    // Ensure the geo objects were not created
    GeoObjectQuery query = new GeoObjectQuery(testData.STATE.getServerObject());
    query.setRestriction(new CodeRestriction("01"));

    Assert.assertNull(query.getSingleResult());
  }

  @Test
  @Request
  public void testImportShapefileWithTerm() throws InterruptedException
  {
    Term term = ServiceFactory.getRegistryService().createTerm(this.testData.adminClientRequest.getSessionId(), testTerm.getRootTerm().getCode(), new Term("00", new LocalizedValue("00"), new LocalizedValue("")).toJSON().toString());

    try
    {
      this.testData.refreshTerms(this.testTerm);

      InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

      Assert.assertNotNull(istream);

      ShapefileService service = new ShapefileService();

      GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, testTerm);

      ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

      config.setHierarchy(hierarchyType);

      ImportHistory hist = importShapefile(this.testData.adminClientRequest.getSessionId(), config.toJSON().toString());

      this.waitUntilStatus(hist, AllJobStatus.SUCCESS);

      hist = ImportHistory.get(hist.getOid());
      Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));
      Assert.assertEquals(new Long(56), hist.getWorkTotal());
      Assert.assertEquals(new Long(56), hist.getWorkProgress());
      Assert.assertEquals(new Long(56), hist.getImportedRecords());

      String sessionId = this.testData.adminClientRequest.getSessionId();
      GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(sessionId, "01", testData.STATE.getCode());

      Assert.assertEquals("01", object.getCode());
    }
    finally
    {
      ServiceFactory.getRegistryService().deleteTerm(this.testData.adminClientRequest.getSessionId(), term.getCode());

      this.testData.refreshTerms(this.testTerm);
    }
  }

  @Test
  @Request
  public void testImportShapefileWithBadTerm() throws InterruptedException
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, testTerm);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    config.setHierarchy(hierarchyType);

    ImportHistory hist = importShapefile(this.testData.adminClientRequest.getSessionId(), config.toJSON().toString());

    this.waitUntilStatus(hist, AllJobStatus.FEEDBACK);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(56), hist.getWorkTotal());
    Assert.assertEquals(new Long(56), hist.getWorkProgress());
    Assert.assertEquals(new Long(0), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.VALIDATION_RESOLVE, hist.getStage().get(0));

    JSONObject page = new ETLService().getReferenceValidationProblems(testData.adminClientRequest.getSessionId(), hist.getOid(), 100, 1);
    JSONArray results = page.getJSONArray("results");
    Assert.assertEquals(1, results.length());

    // Assert the values of the problem
    JSONObject problem = results.getJSONObject(0);

    Assert.assertEquals("00", problem.getString("label"));
    Assert.assertEquals(this.testTerm.getRootTerm().getCode(), problem.getString("parentCode"));
    Assert.assertEquals(this.testTerm.getName(), problem.getString("attributeCode"));
    Assert.assertEquals(this.testTerm.getLabel().getValue(), problem.getString("attributeLabel"));

    // Ensure the geo objects were not created
    GeoObjectQuery query = new GeoObjectQuery(testData.STATE.getServerObject());
    query.setRestriction(new CodeRestriction("01"));

    Assert.assertNull(query.getSingleResult());
  }

  @Test
  @Request
  public void testQueueImports() throws InterruptedException
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null);

    config.setHierarchy(hierarchyType);

    ImportHistory hist = importShapefile(this.testData.adminClientRequest.getSessionId(), config.toJSON().toString());
    ImportHistory hist2 = importShapefile(this.testData.adminClientRequest.getSessionId(), config.toJSON().toString());

    this.waitUntilStatus(hist, AllJobStatus.RUNNING);

    hist = ImportHistory.get(hist.getOid());
    hist2 = ImportHistory.get(hist2.getOid());
    Assert.assertTrue("Expected status new or queued, but was [" + hist2.getStatus().get(0) + "]", hist2.getStatus().get(0).equals(AllJobStatus.NEW) || hist2.getStatus().get(0).equals(AllJobStatus.QUEUED));

    this.waitUntilStatus(hist, AllJobStatus.SUCCESS);

    hist2 = ImportHistory.get(hist2.getOid());
    Assert.assertTrue("Expected status running or queued, but was [" + hist2.getStatus().get(0) + "]", hist2.getStatus().get(0).equals(AllJobStatus.RUNNING) || hist2.getStatus().get(0).equals(AllJobStatus.QUEUED));

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(56), hist.getWorkTotal());
    Assert.assertEquals(new Long(56), hist.getWorkProgress());

    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(this.testData.adminClientRequest.getSessionId(), "01", testData.STATE.getCode());

    Assert.assertNotNull(object);
    Assert.assertNotNull(object.getGeometry());
    Assert.assertEquals("Alabama", object.getLocalizedDisplayLabel());
    Assert.assertEquals(GeoObjectStatusTerm.ACTIVE.code, object.getStatus().getCode());

    this.waitUntilStatus(hist2, AllJobStatus.SUCCESS);

    hist2 = ImportHistory.get(hist2.getOid());
    Assert.assertEquals(new Long(56), hist2.getWorkTotal());
    Assert.assertEquals(new Long(56), hist2.getWorkProgress());

    Assert.assertEquals(ImportStage.COMPLETE, hist2.getStage().get(0));
  }

  private ImportHistory importShapefile(String sessionId, String config)
  {
    String retConfig = new ETLService().doImport(sessionId, config).toString();

    GeoObjectImportConfiguration configuration = (GeoObjectImportConfiguration) ImportConfiguration.build(retConfig, true);

    String historyId = configuration.getHistoryId();

    return ImportHistory.get(historyId);
  }

  @Test
  @Request
  public void testBadParentSynonymAndResume() throws InterruptedException
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    config.setHierarchy(hierarchyType);

    config.addParent(new Location(this.testData.COUNTRY.getServerObject(), new BasicColumnFunction("LSAD")));

    ImportHistory hist = importShapefile(this.testData.adminClientRequest.getSessionId(), config.toJSON().toString());

    this.waitUntilStatus(hist, AllJobStatus.FEEDBACK);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(56), hist.getWorkTotal());
    Assert.assertEquals(new Long(56), hist.getWorkProgress());
    Assert.assertEquals(new Long(0), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.VALIDATION_RESOLVE, hist.getStage().get(0));

    JSONObject page = new ETLService().getReferenceValidationProblems(testData.adminClientRequest.getSessionId(), hist.getOid(), 100, 1);
    JSONArray results = page.getJSONArray("results");
    Assert.assertEquals(1, results.length());

    // Ensure the geo objects were not created
    GeoObjectQuery query = new GeoObjectQuery(testData.STATE.getServerObject());
    query.setRestriction(new CodeRestriction("01"));

    Assert.assertNull(query.getSingleResult());

    // Resolve the import problem with a synonym
    GeoObject geoObj = ServiceFactory.getRegistryService().newGeoObjectInstance(this.testData.adminClientRequest.getSessionId(), this.testData.COUNTRY.getCode());
    geoObj.setCode("99");
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "Test Label99");
    geoObj.setUid(ServiceFactory.getIdService().getUids(1)[0]);

    ServerGeoObjectIF serverGo = new ServerGeoObjectService().apply(geoObj, true, false);

    new GeoSynonymService().createGeoEntitySynonym(this.testData.adminClientRequest.getSessionId(), serverGo.getRunwayId(), "00");

    ImportHistory hist2 = importShapefile(this.testData.adminClientRequest.getSessionId(), hist.getConfigJson());
    Assert.assertEquals(hist.getOid(), hist2.getOid());

    this.waitUntilStatus(hist, AllJobStatus.SUCCESS);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));
    Assert.assertEquals(new Long(56), hist.getWorkTotal());
    Assert.assertEquals(new Long(56), hist.getWorkProgress());
    Assert.assertEquals(new Long(56), hist.getImportedRecords());

    String sessionId = this.testData.adminClientRequest.getSessionId();
    GeoObject go = ServiceFactory.getRegistryService().getGeoObjectByCode(sessionId, "01", testData.STATE.getCode());

    Assert.assertEquals("01", go.getCode());

    ParentTreeNode nodes = ServiceFactory.getRegistryService().getParentGeoObjects(sessionId, go.getUid(), config.getType().getCode(), new String[] { this.testData.COUNTRY.getCode() }, false, new Date());

    List<ParentTreeNode> parents = nodes.getParents();

    Assert.assertEquals(1, parents.size());
  }

  private GeoObjectImportConfiguration getTestConfiguration(InputStream istream, ShapefileService service, AttributeTermType testTerm)
  {
    JSONObject result = service.getShapefileConfiguration(this.testData.adminClientRequest.getSessionId(), testData.STATE.getCode(), null, null, "cb_2017_us_state_500k.zip", istream, ImportStrategy.NEW_AND_UPDATE);
    JSONObject type = result.getJSONObject(GeoObjectImportConfiguration.TYPE);
    JSONArray attributes = type.getJSONArray(GeoObjectType.JSON_ATTRIBUTES);

    for (int i = 0; i < attributes.length(); i++)
    {
      JSONObject attribute = attributes.getJSONObject(i);

      String attributeName = attribute.getString(AttributeType.JSON_CODE);

      if (attributeName.equals(GeoObject.DISPLAY_LABEL))
      {
        attribute.put(GeoObjectImportConfiguration.TARGET, "NAME");
      }
      else if (attributeName.equals(GeoObject.CODE))
      {
        attribute.put(GeoObjectImportConfiguration.TARGET, "GEOID");
      }
      else if (testTerm != null && attributeName.equals(testTerm.getName()))
      {
        attribute.put(GeoObjectImportConfiguration.TARGET, "LSAD");
      }

    }

    result.put(ImportConfiguration.FORMAT_TYPE, FormatImporterType.SHAPEFILE);
    result.put(ImportConfiguration.OBJECT_TYPE, ObjectImportType.GEO_OBJECT);

    GeoObjectImportConfiguration config = (GeoObjectImportConfiguration) ImportConfiguration.build(result.toString(), true);

    config.setStartDate(new Date());
    config.setEndDate(new Date());
    config.setImportStrategy(ImportStrategy.NEW_AND_UPDATE);
    
    return config;
  }
}
