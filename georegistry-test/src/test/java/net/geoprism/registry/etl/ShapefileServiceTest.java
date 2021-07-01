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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
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
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.DelegateSimpleFeatureReader;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureReader;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.sort.SortedFeatureReader;
import org.geotools.factory.CommonFactoryFinder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;

import com.runwaysdk.business.SmartExceptionDTO;
import com.runwaysdk.constants.VaultProperties;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.resource.StreamResource;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.JobHistoryRecord;
import com.runwaysdk.system.scheduler.SchedulerManager;
import com.vividsolutions.jts.geom.Geometry;

import net.geoprism.data.importer.BasicColumnFunction;
import net.geoprism.data.importer.ShapefileFunction;
import net.geoprism.registry.etl.FormatSpecificImporterFactory.FormatImporterType;
import net.geoprism.registry.etl.ObjectImporterFactory.ObjectImportType;
import net.geoprism.registry.etl.ValidationProblem.ValidationResolution;
import net.geoprism.registry.etl.upload.GeoObjectImporter;
import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.etl.upload.ShapefileImporter;
import net.geoprism.registry.excel.MapFeatureRow;
import net.geoprism.registry.geoobject.ServerGeoObjectService;
import net.geoprism.registry.io.ConstantShapefileFunction;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.io.Location;
import net.geoprism.registry.io.LocationBuilder;
import net.geoprism.registry.io.ParentMatchStrategy;
import net.geoprism.registry.io.PostalCodeFactory;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.permission.AllowAllGeoObjectPermissionService;
import net.geoprism.registry.query.ServerCodeRestriction;
import net.geoprism.registry.query.ServerGeoObjectQuery;
import net.geoprism.registry.service.RegistryService;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.service.ShapefileService;
import net.geoprism.registry.test.SchedulerTestUtils;
import net.geoprism.registry.test.TestAttributeTypeInfo;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.TestGeoObjectTypeInfo;
import net.geoprism.registry.test.USATestData;

public class ShapefileServiceTest
{
  protected static USATestData         testData;

  private static TestAttributeTypeInfo testTerm;

  private static TestAttributeTypeInfo testInteger;

  @BeforeClass
  @Request
  public static void classSetUp()
  {
    testData = USATestData.newTestData();
    testData.setUpMetadata();

    testTerm = new TestAttributeTypeInfo("testTerm", "testTermLocalName", USATestData.STATE, AttributeTermType.TYPE);
    testTerm.apply();

    testInteger = new TestAttributeTypeInfo("testInteger", "testIntegerLocalName", USATestData.STATE, AttributeIntegerType.TYPE);
    testInteger.apply();

    if (!SchedulerManager.initialized())
    {
      SchedulerManager.start();
    }
  }

  @AfterClass
  public static void classTearDown() throws IOException
  {
    testData.tearDownMetadata();

    FileUtils.deleteDirectory(new File(VaultProperties.getPath("vault.default"), "files"));
  }

  @Before
  public void setUp()
  {
    clearData();

    testData.logIn(USATestData.USER_NPS_RA);
  }

  @After
  public void tearDown()
  {
    testData.logOut();

    testData.tearDownInstanceData();

    clearData();
  }

  @Request
  private static void clearData()
  {
    SchedulerTestUtils.clearImportData();

    TestGeoObjectInfo parent = new TestGeoObjectInfo("00", USATestData.COUNTRY);
    parent.delete();
  }

  // @Before
  // public void setUp()
  // {
  // testData = USATestData.newTestData(false);
  //
  // this.adminCR = testData.adminClientRequest;
  //
  // AttributeTermType testTerm = (AttributeTermType)
  // AttributeType.factory("testTerm", new LocalizedValue("testTermLocalName"),
  // new LocalizedValue("testTermLocalDescrip"), AttributeTermType.TYPE, false,
  // false, false);
  // this.testTerm = (AttributeTermType)
  // ServiceFactory.getRegistryService().createAttributeType(this.adminCR.getSessionId(),
  // this.USATestData.STATE.getCode(), testTerm.toJSON().toString());
  //
  // AttributeIntegerType testInteger = (AttributeIntegerType)
  // AttributeType.factory("testInteger", new
  // LocalizedValue("testIntegerLocalName"), new
  // LocalizedValue("testIntegerLocalDescrip"), AttributeIntegerType.TYPE,
  // false, false, false);
  // this.testInteger = (AttributeIntegerType)
  // ServiceFactory.getRegistryService().createAttributeType(this.adminCR.getSessionId(),
  // this.USATestData.STATE.getCode(), testInteger.toJSON().toString());
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

  @Test
  @Request
  public void testGetAttributeInformation()
  {
    PostalCodeFactory.remove(USATestData.STATE.getServerObject());

    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();
    JSONObject result = service.getShapefileConfiguration(testData.clientRequest.getSessionId(), USATestData.STATE.getCode(), null, null, "cb_2017_us_state_500k.zip", istream, ImportStrategy.NEW_AND_UPDATE, false);

    Assert.assertFalse(result.getBoolean(GeoObjectImportConfiguration.HAS_POSTAL_CODE));

    JSONObject type = result.getJSONObject(GeoObjectImportConfiguration.TYPE);

    Assert.assertNotNull(type);

    JSONArray tAttributes = type.getJSONArray(GeoObjectType.JSON_ATTRIBUTES);

    Assert.assertEquals(5, tAttributes.length());

    boolean hasCode = false;
    boolean hasStatus = false;

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
      else if (code.equals(DefaultAttribute.STATUS.getName()))
      {
        hasStatus = true;
        Assert.assertTrue(tAttribute.has("required"));
        Assert.assertFalse(tAttribute.getBoolean("required"));
      }
    }

    Assert.assertTrue(hasCode);
    Assert.assertTrue(hasStatus);

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

    ServerGeoObjectType type = USATestData.STATE.getServerObject();

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
    JSONObject result = service.getShapefileConfiguration(testData.clientRequest.getSessionId(), USATestData.STATE.getCode(), null, null, "cb_2017_us_state_500k.zip", istream, ImportStrategy.NEW_AND_UPDATE, false);

    Assert.assertTrue(result.getBoolean(GeoObjectImportConfiguration.HAS_POSTAL_CODE));
  }

  @Test
  @Request
  public void testImportShapefileFrazer() throws InterruptedException
  {
    InputStream istream = Thread.currentThread().getContextClassLoader().getResourceAsStream("shapefile/ntd_zam_operational_28082020.zip.test");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    /*
     * Build Config
     */
    JSONObject result = service.getShapefileConfiguration(testData.clientRequest.getSessionId(), USATestData.STATE.getCode(), null, null, "ntd_zam_operational_28082020.zip", istream, ImportStrategy.NEW_AND_UPDATE, false);
    JSONObject type = result.getJSONObject(GeoObjectImportConfiguration.TYPE);
    JSONArray attributes = type.getJSONArray(GeoObjectType.JSON_ATTRIBUTES);

    for (int i = 0; i < attributes.length(); i++)
    {
      JSONObject attribute = attributes.getJSONObject(i);

      String attributeName = attribute.getString(AttributeType.JSON_CODE);

      if (attributeName.equals(GeoObject.DISPLAY_LABEL))
      {
        attribute.put(GeoObjectImportConfiguration.TARGET, "name");
      }
      else if (attributeName.equals(GeoObject.CODE))
      {
        attribute.put(GeoObjectImportConfiguration.TARGET, "id");
      }

    }

    result.put(ImportConfiguration.FORMAT_TYPE, FormatImporterType.SHAPEFILE);
    result.put(ImportConfiguration.OBJECT_TYPE, ObjectImportType.GEO_OBJECT);

    GeoObjectImportConfiguration config = (GeoObjectImportConfiguration) ImportConfiguration.build(result.toString(), true);

    config.setStartDate(new Date());
    config.setEndDate(new Date());
    config.setImportStrategy(ImportStrategy.NEW_AND_UPDATE);
    config.setHierarchy(hierarchyType);

    ImportHistory hist = importShapefile(testData.clientRequest.getSessionId(), config.toJSON().toString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.SUCCESS);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(1011), hist.getWorkTotal());
    Assert.assertEquals(new Long(1011), hist.getWorkProgress());
    Assert.assertEquals(new Long(1011), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));
  }

  @Test
  @Request
  public void testImportShapefile() throws InterruptedException
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_AND_UPDATE);
    config.setHierarchy(hierarchyType);

    ImportHistory hist = importShapefile(testData.clientRequest.getSessionId(), config.toJSON().toString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.SUCCESS);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(56), hist.getWorkTotal());
    Assert.assertEquals(new Long(56), hist.getWorkProgress());
    Assert.assertEquals(new Long(56), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(testData.clientRequest.getSessionId(), "01", USATestData.STATE.getCode());

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
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_AND_UPDATE);
    config.setHierarchy(hierarchyType);

    ImportHistory hist = importShapefile(testData.clientRequest.getSessionId(), config.toJSON().toString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.SUCCESS);

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
    ImportHistory hist2 = importShapefile(testData.clientRequest.getSessionId(), config2.toString());
    Assert.assertNotSame(hist.getOid(), hist2.getOid());

    SchedulerTestUtils.waitUntilStatus(hist2.getOid(), AllJobStatus.SUCCESS);

    hist2 = ImportHistory.get(hist2.getOid());
    Assert.assertEquals(AllJobStatus.SUCCESS, hist.getStatus().get(0));
    Assert.assertEquals(new Long(56), hist2.getWorkTotal());
    Assert.assertEquals(new Long(56), hist2.getWorkProgress());
    Assert.assertEquals(new Long(56), hist2.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist2.getStage().get(0));

    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(testData.clientRequest.getSessionId(), "01", USATestData.STATE.getCode());

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

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_AND_UPDATE);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    config.setFunction(testInteger.getName(), new BasicColumnFunction("ALAND"));
    config.setHierarchy(hierarchyType);

    ImportHistory hist = importShapefile(testData.clientRequest.getSessionId(), config.toJSON().toString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.SUCCESS);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(56), hist.getWorkTotal());
    Assert.assertEquals(new Long(56), hist.getWorkProgress());
    Assert.assertEquals(new Long(56), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(testData.clientRequest.getSessionId(), "01", USATestData.STATE.getCode());

    Assert.assertNotNull(object);
    Assert.assertNotNull(object.getGeometry());
    Assert.assertEquals("Alabama", object.getLocalizedDisplayLabel());
    Assert.assertEquals(131174431216L, object.getValue(testInteger.getName()));
  }

  @Test
  @Request
  public void testImportShapefileNullInteger() throws InterruptedException
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_AND_UPDATE);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    config.setFunction(testInteger.getName(), new BasicColumnFunction("ALAND"));
    config.setHierarchy(hierarchyType);

    ImportHistory hist = importShapefile(testData.clientRequest.getSessionId(), config.toJSON().toString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.SUCCESS);

    istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    config = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_AND_UPDATE);
    config.setFunction(testInteger.getName(), new BasicColumnFunction("ALAND"));
    config.setCopyBlank(true);

    HashMap<String, Object> row = new HashMap<String, Object>();
    row.put("ALAND", null);
    row.put("NAME", "Alabama2");
    row.put("GEOID", "01");

    GeoObjectImporter importer = new GeoObjectImporter(config, new NullImportProgressListener());
    importer.setFormatSpecificImporter(new NullFormatSpecificImporter());
    importer.importRow(new MapFeatureRow(row));

    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(testData.clientRequest.getSessionId(), "01", USATestData.STATE.getCode());

    Assert.assertNotNull(object);
    Assert.assertNotNull(object.getGeometry());
    Assert.assertEquals("Alabama", object.getLocalizedDisplayLabel());
    Assert.assertNull(object.getValue(testInteger.getName()));
  }

  @Test
  @Request
  public void testImportShapefileNullInteger_Ignore() throws InterruptedException
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");
    
    Assert.assertNotNull(istream);
    
    ShapefileService service = new ShapefileService();
    
    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_AND_UPDATE);
    
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());
    
    config.setFunction(testInteger.getName(), new BasicColumnFunction("ALAND"));
    config.setHierarchy(hierarchyType);
    
    ImportHistory hist = importShapefile(testData.clientRequest.getSessionId(), config.toJSON().toString());
    
    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.SUCCESS);
    
    istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");
    
    config = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_AND_UPDATE);
    config.setFunction(testInteger.getName(), new BasicColumnFunction("ALAND"));
    config.setCopyBlank(false);
    
    HashMap<String, Object> row = new HashMap<String, Object>();
    row.put("ALAND", null);
    row.put("NAME", "Alabama2");
    row.put("GEOID", "01");
    
    GeoObjectImporter importer = new GeoObjectImporter(config, new NullImportProgressListener());
    importer.setFormatSpecificImporter(new NullFormatSpecificImporter());
    importer.importRow(new MapFeatureRow(row));
    
    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(testData.clientRequest.getSessionId(), "01", USATestData.STATE.getCode());
    
    Assert.assertNotNull(object);
    Assert.assertNotNull(object.getGeometry());
    Assert.assertEquals("Alabama", object.getLocalizedDisplayLabel());
    Assert.assertEquals(131174431216L, object.getValue(testInteger.getName()));
  }
  
  @Test
  @Request
  public void testImportShapefileWithParent() throws InterruptedException
  {
    GeoObject geoObj = ServiceFactory.getRegistryService().newGeoObjectInstance(testData.clientRequest.getSessionId(), USATestData.COUNTRY.getCode());
    geoObj.setCode("00");
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "Test Label");
    geoObj.setUid(ServiceFactory.getIdService().getUids(1)[0]);

    ServerGeoObjectIF serverGO = new ServerGeoObjectService(new AllowAllGeoObjectPermissionService()).apply(geoObj, true, false);
    geoObj = RegistryService.getInstance().getGeoObjectByCode(Session.getCurrentSession().getOid(), serverGO.getCode(), serverGO.getType().getCode());

    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_AND_UPDATE);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    config.setHierarchy(hierarchyType);
    config.addParent(new Location(USATestData.COUNTRY.getServerObject(), hierarchyType, new BasicColumnFunction("LSAD"), ParentMatchStrategy.ALL));

    ImportHistory hist = importShapefile(testData.clientRequest.getSessionId(), config.toJSON().toString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.SUCCESS);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(56), hist.getWorkTotal());
    Assert.assertEquals(new Long(56), hist.getWorkProgress());
    Assert.assertEquals(new Long(56), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    String sessionId = testData.clientRequest.getSessionId();
    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(sessionId, "01", USATestData.STATE.getCode());

    Assert.assertEquals("01", object.getCode());

    ParentTreeNode nodes = ServiceFactory.getRegistryService().getParentGeoObjects(sessionId, object.getUid(), config.getType().getCode(), new String[] { USATestData.COUNTRY.getCode() }, false, USATestData.DEFAULT_OVER_TIME_DATE);

    List<ParentTreeNode> parents = nodes.getParents();

    Assert.assertEquals(1, parents.size());
  }

  @Test
  @Request
  public void testImportShapefileWithParentCode() throws InterruptedException
  {
    GeoObject geoObj = ServiceFactory.getRegistryService().newGeoObjectInstance(testData.clientRequest.getSessionId(), USATestData.COUNTRY.getCode());
    geoObj.setCode("00");
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "Test Label");
    geoObj.setUid(ServiceFactory.getIdService().getUids(1)[0]);

    ServerGeoObjectIF serverGO = new ServerGeoObjectService(new AllowAllGeoObjectPermissionService()).apply(geoObj, true, false);
    geoObj = RegistryService.getInstance().getGeoObjectByCode(Session.getCurrentSession().getOid(), serverGO.getCode(), serverGO.getType().getCode());

    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_AND_UPDATE);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    config.setHierarchy(hierarchyType);
    config.addParent(new Location(USATestData.COUNTRY.getServerObject(), hierarchyType, new BasicColumnFunction("LSAD"), ParentMatchStrategy.CODE));

    ImportHistory hist = importShapefile(testData.clientRequest.getSessionId(), config.toJSON().toString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.SUCCESS);

    hist = ImportHistory.get(hist.getOid());

    Assert.assertEquals(new Long(56), hist.getWorkTotal());
    Assert.assertEquals(new Long(56), hist.getWorkProgress());
    Assert.assertEquals(new Long(56), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    final GeoObjectImportConfiguration test = new GeoObjectImportConfiguration();
    test.fromJSON(hist.getConfigJson(), false);

    // TODO
    // Assert.assertEquals(config.getParentLookupType(),
    // test.getParentLookupType());

    String sessionId = testData.clientRequest.getSessionId();
    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(sessionId, "01", USATestData.STATE.getCode());

    Assert.assertEquals("01", object.getCode());

    ParentTreeNode nodes = ServiceFactory.getRegistryService().getParentGeoObjects(sessionId, object.getUid(), config.getType().getCode(), new String[] { USATestData.COUNTRY.getCode() }, false, USATestData.DEFAULT_OVER_TIME_DATE);

    List<ParentTreeNode> parents = nodes.getParents();

    Assert.assertEquals(1, parents.size());
  }

  @Test
  @Request
  public void testImportShapefileWithBadParentCode() throws InterruptedException
  {
    GeoObject geoObj = ServiceFactory.getRegistryService().newGeoObjectInstance(testData.clientRequest.getSessionId(), USATestData.COUNTRY.getCode());
    geoObj.setCode("00");
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "Test Label");
    geoObj.setUid(ServiceFactory.getIdService().getUids(1)[0]);

    ServerGeoObjectIF serverGO = new ServerGeoObjectService(new AllowAllGeoObjectPermissionService()).apply(geoObj, true, false);
    geoObj = RegistryService.getInstance().getGeoObjectByCode(Session.getCurrentSession().getOid(), serverGO.getCode(), serverGO.getType().getCode());

    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_AND_UPDATE);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    config.setHierarchy(hierarchyType);
    config.addParent(new Location(USATestData.COUNTRY.getServerObject(), hierarchyType, new BasicColumnFunction("GEOID"), ParentMatchStrategy.CODE));

    ImportHistory hist = importShapefile(testData.clientRequest.getSessionId(), config.toJSON().toString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.FEEDBACK);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(56), hist.getWorkTotal());
    Assert.assertEquals(new Long(56), hist.getWorkProgress());
    Assert.assertEquals(new Long(0), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.VALIDATION_RESOLVE, hist.getStage().get(0));

    final GeoObjectImportConfiguration test = new GeoObjectImportConfiguration();
    test.fromJSON(hist.getConfigJson(), false);

    // TODO
    // Assert.assertEquals(config.getParentLookupType(),
    // test.getParentLookupType());

    // JSONArray errors = new JSONArray(hist.getErrorJson());
    //
    // Assert.assertEquals(0, errors.length());

    // Ensure the geo objects were not created
    ServerGeoObjectQuery query = new ServerGeoObjectService().createQuery(USATestData.STATE.getServerObject(), config.getStartDate());
    query.setRestriction(new ServerCodeRestriction("01"));

    Assert.assertNull(query.getSingleResult());
  }

  @Test
  @Request
  public void testImportShapefileExcludeParent() throws InterruptedException
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_AND_UPDATE);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    config.setHierarchy(hierarchyType);

    config.addParent(new Location(USATestData.COUNTRY.getServerObject(), hierarchyType, new BasicColumnFunction("LSAD"), ParentMatchStrategy.ALL));
    config.addExclusion(GeoObjectImportConfiguration.PARENT_EXCLUSION, "00");

    ImportHistory hist = importShapefile(testData.clientRequest.getSessionId(), config.toJSON().toString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.SUCCESS);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(56), hist.getWorkTotal());
    Assert.assertEquals(new Long(56), hist.getWorkProgress());
    Assert.assertEquals(new Long(0), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    // Ensure the geo objects were not created
    ServerGeoObjectQuery query = new ServerGeoObjectService().createQuery(USATestData.STATE.getServerObject(), config.getStartDate());
    query.setRestriction(new ServerCodeRestriction("01"));

    Assert.assertNull(query.getSingleResult());
  }

  @Test
  @Request
  public void testImportShapefileWithBadParent() throws InterruptedException
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_AND_UPDATE);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    config.setHierarchy(hierarchyType);

    config.addParent(new Location(USATestData.COUNTRY.getServerObject(), hierarchyType, new BasicColumnFunction("LSAD"), ParentMatchStrategy.ALL));

    ImportHistory hist = importShapefile(testData.clientRequest.getSessionId(), config.toJSON().toString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.FEEDBACK);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(56), hist.getWorkTotal());
    Assert.assertEquals(new Long(56), hist.getWorkProgress());
    Assert.assertEquals(new Long(0), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.VALIDATION_RESOLVE, hist.getStage().get(0));

    JSONObject page = new JSONObject(new ETLService().getValidationProblems(testData.clientRequest.getSessionId(), hist.getOid(), false, 100, 1).toString());
    JSONArray results = page.getJSONArray("results");
    Assert.assertEquals(1, results.length());

    // Ensure the geo objects were not created
    ServerGeoObjectQuery query = new ServerGeoObjectService().createQuery(USATestData.STATE.getServerObject(), config.getStartDate());
    query.setRestriction(new ServerCodeRestriction("01"));

    Assert.assertNull(query.getSingleResult());
  }

  @Test
  @Request
  public void testImportShapefileWithTerm() throws InterruptedException
  {
    Term term = ServiceFactory.getRegistryService().createTerm(testData.clientRequest.getSessionId(), testTerm.getRootTerm().getCode(), new Term("00", new LocalizedValue("00"), new LocalizedValue("")).toJSON().toString());

    try
    {
      TestDataSet.refreshTerms((AttributeTermType) testTerm.fetchDTO());

      InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

      Assert.assertNotNull(istream);

      ShapefileService service = new ShapefileService();

      GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, (AttributeTermType) testTerm.fetchDTO(), ImportStrategy.NEW_AND_UPDATE);

      ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

      config.setHierarchy(hierarchyType);

      ImportHistory hist = importShapefile(testData.clientRequest.getSessionId(), config.toJSON().toString());

      SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.SUCCESS);

      hist = ImportHistory.get(hist.getOid());
      Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));
      Assert.assertEquals(new Long(56), hist.getWorkTotal());
      Assert.assertEquals(new Long(56), hist.getWorkProgress());
      Assert.assertEquals(new Long(56), hist.getImportedRecords());

      String sessionId = testData.clientRequest.getSessionId();
      GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(sessionId, "01", USATestData.STATE.getCode());

      Assert.assertEquals("01", object.getCode());
    }
    finally
    {
      ServiceFactory.getRegistryService().deleteTerm(testData.clientRequest.getSessionId(), testTerm.getRootTerm().getCode(), term.getCode());

      TestDataSet.refreshTerms((AttributeTermType) testTerm.fetchDTO());
    }
  }

  @Test
  @Request
  public void testImportShapefileWithBadTerm() throws InterruptedException
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, (AttributeTermType) testTerm.fetchDTO(), ImportStrategy.NEW_AND_UPDATE);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    config.setHierarchy(hierarchyType);

    ImportHistory hist = importShapefile(testData.clientRequest.getSessionId(), config.toJSON().toString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.FEEDBACK);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(56), hist.getWorkTotal());
    Assert.assertEquals(new Long(56), hist.getWorkProgress());
    Assert.assertEquals(new Long(0), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.VALIDATION_RESOLVE, hist.getStage().get(0));

    JSONObject page = new JSONObject(new ETLService().getValidationProblems(testData.clientRequest.getSessionId(), hist.getOid(), false, 100, 1).toString());
    JSONArray results = page.getJSONArray("results");
    Assert.assertEquals(1, results.length());

    // Assert the values of the problem
    JSONObject problem = results.getJSONObject(0);

    Assert.assertEquals("00", problem.getString("label"));
    Assert.assertEquals(testTerm.getRootTerm().getCode(), problem.getString("parentCode"));
    Assert.assertEquals(testTerm.getName(), problem.getString("attributeCode"));
    Assert.assertEquals(testTerm.getLabel(), problem.getString("attributeLabel"));

    // Ensure the geo objects were not created
    ServerGeoObjectQuery query = new ServerGeoObjectService().createQuery(USATestData.STATE.getServerObject(), config.getStartDate());
    query.setRestriction(new ServerCodeRestriction("01"));

    Assert.assertNull(query.getSingleResult());
  }

  @Test
  @Request
  public void testQueueImports() throws InterruptedException
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_AND_UPDATE);

    config.setHierarchy(hierarchyType);

    ImportHistory hist = importShapefile(testData.clientRequest.getSessionId(), config.toJSON().toString());
    ImportHistory hist2 = importShapefile(testData.clientRequest.getSessionId(), config.toJSON().toString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.RUNNING);

    hist = ImportHistory.get(hist.getOid());
    hist2 = ImportHistory.get(hist2.getOid());
    Assert.assertTrue("Expected status new or queued, but was [" + hist2.getStatus().get(0) + "]", hist2.getStatus().get(0).equals(AllJobStatus.NEW) || hist2.getStatus().get(0).equals(AllJobStatus.QUEUED));

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.SUCCESS);

    hist2 = ImportHistory.get(hist2.getOid());
    Assert.assertTrue("Expected status running or queued, but was [" + hist2.getStatus().get(0) + "]", hist2.getStatus().get(0).equals(AllJobStatus.RUNNING) || hist2.getStatus().get(0).equals(AllJobStatus.QUEUED));

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(56), hist.getWorkTotal());
    Assert.assertEquals(new Long(56), hist.getWorkProgress());

    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(testData.clientRequest.getSessionId(), "01", USATestData.STATE.getCode());

    Assert.assertNotNull(object);
    Assert.assertNotNull(object.getGeometry());
    Assert.assertEquals("Alabama", object.getLocalizedDisplayLabel());
    Assert.assertEquals(GeoObjectStatusTerm.ACTIVE.code, object.getStatus().getCode());

    SchedulerTestUtils.waitUntilStatus(hist2.getOid(), AllJobStatus.SUCCESS);

    hist2 = ImportHistory.get(hist2.getOid());
    Assert.assertEquals(new Long(56), hist2.getWorkTotal());
    Assert.assertEquals(new Long(56), hist2.getWorkProgress());

    Assert.assertEquals(ImportStage.COMPLETE, hist2.getStage().get(0));
  }

  private ImportHistory importShapefile(String sessionId, String config) throws InterruptedException
  {
    String retConfig = new ETLService().doImport(sessionId, config).toString();

    GeoObjectImportConfiguration configuration = (GeoObjectImportConfiguration) ImportConfiguration.build(retConfig, true);

    String historyId = configuration.getHistoryId();

    Thread.sleep(100);

    return ImportHistory.get(historyId);
  }

  @Test
  @Request
  public void testBadParentSynonymAndResume() throws InterruptedException
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_AND_UPDATE);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    config.setHierarchy(hierarchyType);

    config.addParent(new Location(USATestData.COUNTRY.getServerObject(), hierarchyType, new BasicColumnFunction("LSAD"), ParentMatchStrategy.ALL));

    ImportHistory hist = importShapefile(testData.clientRequest.getSessionId(), config.toJSON().toString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.FEEDBACK);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(56), hist.getWorkTotal());
    Assert.assertEquals(new Long(56), hist.getWorkProgress());
    Assert.assertEquals(new Long(0), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.VALIDATION_RESOLVE, hist.getStage().get(0));

    JSONObject page = new JSONObject(new ETLService().getValidationProblems(testData.clientRequest.getSessionId(), hist.getOid(), false, 100, 1).toString());
    JSONArray results = page.getJSONArray("results");
    Assert.assertEquals(1, results.length());

    // Ensure the geo objects were not created
    ServerGeoObjectQuery query = new ServerGeoObjectService().createQuery(USATestData.STATE.getServerObject(), config.getStartDate());
    query.setRestriction(new ServerCodeRestriction("01"));

    Assert.assertNull(query.getSingleResult());

    // Resolve the import problem with a synonym
    GeoObject geoObj = ServiceFactory.getRegistryService().newGeoObjectInstance(testData.clientRequest.getSessionId(), USATestData.COUNTRY.getCode());
    geoObj.setCode("99");
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "Test Label99");
    geoObj.setUid(ServiceFactory.getIdService().getUids(1)[0]);

    ServerGeoObjectIF serverGo = new ServerGeoObjectService(new AllowAllGeoObjectPermissionService()).apply(geoObj, true, false);

    JSONObject valRes = new JSONObject();
    valRes.put("validationProblemId", results.getJSONObject(0).getString("id"));
    valRes.put("resolution", ValidationResolution.SYNONYM);
    valRes.put("code", serverGo.getCode());
    valRes.put("typeCode", serverGo.getType().getCode());
    valRes.put("label", "00");

    new ETLService().submitValidationProblemResolution(testData.clientRequest.getSessionId(), valRes.toString());

    ValidationProblem vp = ValidationProblem.get(results.getJSONObject(0).getString("id"));
    Assert.assertEquals(ValidationResolution.SYNONYM.name(), vp.getResolution());
    Assert.assertEquals(ParentReferenceProblem.DEFAULT_SEVERITY, vp.getSeverity());

    ImportHistory hist2 = importShapefile(testData.clientRequest.getSessionId(), hist.getConfigJson());
    Assert.assertEquals(hist.getOid(), hist2.getOid());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.SUCCESS);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));
    Assert.assertEquals(new Long(56), hist.getWorkTotal());
    Assert.assertEquals(new Long(56), hist.getWorkProgress());
    Assert.assertEquals(new Long(56), hist.getImportedRecords());

    String sessionId = testData.clientRequest.getSessionId();
    GeoObject go = ServiceFactory.getRegistryService().getGeoObjectByCode(sessionId, "01", USATestData.STATE.getCode());

    Assert.assertEquals("01", go.getCode());

    ParentTreeNode nodes = ServiceFactory.getRegistryService().getParentGeoObjects(sessionId, go.getUid(), config.getType().getCode(), new String[] { USATestData.COUNTRY.getCode() }, false, USATestData.DEFAULT_OVER_TIME_DATE);

    List<ParentTreeNode> parents = nodes.getParents();

    Assert.assertEquals(1, parents.size());

    JSONObject page2 = new JSONObject(new ETLService().getValidationProblems(testData.clientRequest.getSessionId(), hist.getOid(), false, 100, 1).toString());
    JSONArray results2 = page2.getJSONArray("results");
    Assert.assertEquals(0, results2.length());
    Assert.assertEquals(0, page2.getInt("count"));
  }

  public List<String> shapefileSort() throws IOException
  {
    try (CloseableFile shp = ShapefileImporter.getShapefileFromResource(new StreamResource(this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test"), "cb_2017_us_state_500k.zip"), "shp"))
    {
      FileDataStore myData = FileDataStoreFinder.getDataStore(shp);

      SimpleFeatureSource source = myData.getFeatureSource();

      SimpleFeatureCollection featCol = source.getFeatures();

      SimpleFeatureIterator featIt = featCol.features();

      SimpleFeatureReader fr = new DelegateSimpleFeatureReader(source.getSchema(), featIt);

      FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);

      List<SortBy> sortBy = new ArrayList<SortBy>();
      sortBy.add(SortBy.NATURAL_ORDER);
      sortBy.add(ff.sort("LSAD", SortOrder.ASCENDING));

      List<String> names = new ArrayList<String>();

      try (SimpleFeatureReader sr = new SortedFeatureReader(fr, sortBy.toArray(new SortBy[sortBy.size()]), 5000))
      {
        while (sr.hasNext())
        {
          SimpleFeature feature = sr.next();

          names.add(String.valueOf(feature.getAttribute("GEOID")));
        }
      }
      finally
      {
        myData.dispose();
      }

      // System.out.println(StringUtils.join(names, "\n"));

      return names;
    }
  }

  /**
   * In the case when the server fails mid import, when the server reboots it's
   * supposed to restart any jobs that were running. When we restart the job, we
   * want to make sure that it picks up from where it left off.
   * 
   * @throws IOException
   */
  @Test
  @Request
  public void testResumeImport() throws InterruptedException, IOException
  {
    TestGeoObjectInfo parent = new TestGeoObjectInfo("00", USATestData.COUNTRY);
    parent.apply();

    List<String> sortedGeoIds = shapefileSort();

    DataImportJob job = new DataImportJob();
    job.setRunAsUserId(testData.clientRequest.getSessionUser().getOid());
    job.apply();

    ImportHistory fakeImportHistory = new ImportHistory();
    fakeImportHistory.setStartTime(new Date());
    fakeImportHistory.addStatus(AllJobStatus.RUNNING);
    fakeImportHistory.addStage(ImportStage.IMPORT);
    fakeImportHistory.setWorkProgress(6L);
    fakeImportHistory.setImportedRecords(0L);
    fakeImportHistory.apply();

    JobHistoryRecord record = new JobHistoryRecord(job, fakeImportHistory);
    record.apply();

    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_ONLY);
    config.setHierarchy(hierarchyType);
    config.setHistoryId(fakeImportHistory.getOid());
    config.setJobId(job.getOid());
    config.addParent(new Location(USATestData.COUNTRY.getServerObject(), hierarchyType, new BasicColumnFunction("LSAD"), ParentMatchStrategy.ALL));

    fakeImportHistory.appLock();
    fakeImportHistory.setConfigJson(config.toJSON().toString());
    fakeImportHistory.setImportFileId(config.getVaultFileId());
    fakeImportHistory.apply();

    ImportHistory hist = importShapefile(testData.clientRequest.getSessionId(), config.toJSON().toString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.SUCCESS);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(56), hist.getWorkTotal());
    Assert.assertEquals(new Long(56), hist.getWorkProgress());
    Assert.assertEquals(new Long(50), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    for (int i = 0; i < 6; ++i)
    {
      String geoId = sortedGeoIds.get(i);

      try
      {
        ServiceFactory.getRegistryService().getGeoObjectByCode(testData.clientRequest.getSessionId(), geoId, USATestData.STATE.getCode());

        Assert.fail("Was able to fectch GeoObject with code [" + geoId + "], which should not have been imported.");
      }
      catch (SmartExceptionDTO ex)
      {
        // Expected
      }
    }

    for (int i = 6; i < sortedGeoIds.size(); ++i)
    {
      String geoId = sortedGeoIds.get(i);

      GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(testData.clientRequest.getSessionId(), geoId, USATestData.STATE.getCode());

      Assert.assertNotNull(object);
      Assert.assertEquals(geoId, object.getCode());

      Geometry geometry = object.getGeometry();

      Assert.assertNotNull(geometry);
    }

    JSONObject json = new JSONObject(new ETLService().getImportErrors(testData.clientRequest.getSessionId(), hist.getOid(), false, 100, 1).toString());

    Assert.assertEquals(0, json.getJSONArray("results").length());
  }

  @Test
  @Request
  public void testImportSubtypeShapefile() throws InterruptedException
  {
    String parentCode = "ZZZZ000";

    GeoObject geoObj = ServiceFactory.getRegistryService().newGeoObjectInstance(testData.clientRequest.getSessionId(), USATestData.DISTRICT.getCode());
    geoObj.setCode(parentCode);
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "Test Label");
    geoObj.setUid(ServiceFactory.getIdService().getUids(1)[0]);

    ServerGeoObjectIF serverGO = new ServerGeoObjectService(new AllowAllGeoObjectPermissionService()).apply(geoObj, true, false);

    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_AND_UPDATE, USATestData.HEALTH_POST);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    config.setHierarchy(hierarchyType);
    config.addParent(new Location(USATestData.DISTRICT.getServerObject(), hierarchyType, new ConstantShapefileFunction(serverGO.getCode()), ParentMatchStrategy.CODE));

    ImportHistory hist = importShapefile(testData.clientRequest.getSessionId(), config.toJSON().toString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.SUCCESS);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(56), hist.getWorkTotal());
    Assert.assertEquals(new Long(56), hist.getWorkProgress());
    Assert.assertEquals(new Long(56), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(testData.clientRequest.getSessionId(), "01", USATestData.HEALTH_POST.getCode());

    Assert.assertNotNull(object);
    Assert.assertNotNull(object.getGeometry());
    Assert.assertEquals("Alabama", object.getLocalizedDisplayLabel());
  }

  private GeoObjectImportConfiguration getTestConfiguration(InputStream istream, ShapefileService service, AttributeTermType testTerm, ImportStrategy strategy)
  {
    return getTestConfiguration(istream, service, testTerm, strategy, USATestData.STATE);
  }

  private GeoObjectImportConfiguration getTestConfiguration(InputStream istream, ShapefileService service, AttributeTermType testTerm, ImportStrategy strategy, TestGeoObjectTypeInfo info)
  {
    JSONObject result = service.getShapefileConfiguration(testData.clientRequest.getSessionId(), info.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, "cb_2017_us_state_500k.zip", istream, strategy, false);
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

    config.setStartDate(USATestData.DEFAULT_OVER_TIME_DATE);
    config.setEndDate(USATestData.DEFAULT_END_TIME_DATE);
    config.setImportStrategy(strategy);

    return config;
  }
}
