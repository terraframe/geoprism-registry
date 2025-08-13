/**
 *
 */
package net.geoprism.registry.etl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.commongeoregistry.adapter.Term;
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
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.business.SmartExceptionDTO;
import com.runwaysdk.constants.VaultProperties;
import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.resource.StreamResource;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutionContext;
import com.runwaysdk.system.scheduler.JobHistoryRecord;
import com.runwaysdk.system.scheduler.SchedulerManager;

import net.geoprism.data.importer.BasicColumnFunction;
import net.geoprism.data.importer.ShapefileFunction;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.USADatasetTest;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.etl.FormatSpecificImporterFactory.FormatImporterType;
import net.geoprism.registry.etl.ObjectImporterFactory.ObjectImportType;
import net.geoprism.registry.etl.upload.GeoObjectImporter;
import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.etl.upload.ImportHistoryProgressScribe.Range;
import net.geoprism.registry.etl.upload.ShapefileImporter;
import net.geoprism.registry.excel.MapFeatureRow;
import net.geoprism.registry.io.ConstantShapefileFunction;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.io.Location;
import net.geoprism.registry.io.LocationBuilder;
import net.geoprism.registry.io.ParentMatchStrategy;
import net.geoprism.registry.io.PostalCodeFactory;
import net.geoprism.registry.jobs.ImportHistory;
import net.geoprism.registry.jobs.ParentReferenceProblem;
import net.geoprism.registry.jobs.ValidationProblem;
import net.geoprism.registry.jobs.ValidationProblem.ValidationResolution;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.query.ServerCodeRestriction;
import net.geoprism.registry.query.ServerGeoObjectQuery;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.service.business.ServiceFactory;
import net.geoprism.registry.service.request.ETLService;
import net.geoprism.registry.service.request.RegistryComponentService;
import net.geoprism.registry.service.request.ShapefileService;
import net.geoprism.registry.test.SchedulerTestUtils;
import net.geoprism.registry.test.TestAttributeTermTypeInfo;
import net.geoprism.registry.test.TestAttributeTypeInfo;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.TestGeoObjectTypeInfo;
import net.geoprism.registry.test.USATestData;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class ShapefileServiceTest extends USADatasetTest implements InstanceTestClassListener
{
  private static TestAttributeTermTypeInfo testTerm    = new TestAttributeTermTypeInfo("testTerm", "testTermLocalName", USATestData.STATE);

  private static TestAttributeTypeInfo     testInteger = new TestAttributeTypeInfo("testInteger", "testIntegerLocalName", USATestData.STATE, AttributeIntegerType.TYPE);

  @Autowired
  private RegistryComponentService         service;

  @Autowired
  private GeoObjectBusinessServiceIF       objectService;

  @Autowired
  private ETLService                       etlService;

  @Autowired
  private ShapefileService                 shapefileService;

  @Override
  @Request
  public void beforeClassSetup() throws Exception
  {
    TestDataSet.deleteAllSchedulerData();

    super.beforeClassSetup();

    testTerm.apply();

    testInteger.apply();

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
  private void clearData()
  {
    SchedulerTestUtils.clearImportData();

    TestGeoObjectInfo parent = new TestGeoObjectInfo("00", USATestData.COUNTRY, USATestData.SOURCE);
    parent.delete();

    // Clear out the event table
    Database.deleteWhere("domainevententry", "true");
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
  // this.service.createAttributeType(this.adminCR.getSessionId(),
  // this.USATestData.STATE.getCode(), testTerm.toJSON().toString());
  //
  // AttributeIntegerType testInteger = (AttributeIntegerType)
  // AttributeType.factory("testInteger", new
  // LocalizedValue("testIntegerLocalName"), new
  // LocalizedValue("testIntegerLocalDescrip"), AttributeIntegerType.TYPE,
  // false, false, false);
  // this.testInteger = (AttributeIntegerType)
  // this.service.createAttributeType(this.adminCR.getSessionId(),
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
  public void testGetAttributeInformation() throws JSONException
  {
    PostalCodeFactory.remove(USATestData.STATE.getServerObject());

    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    JSONObject result = this.shapefileService.getShapefileConfiguration(testData.clientRequest.getSessionId(), USATestData.STATE.getCode(), null, null, USATestData.SOURCE.getCode(), "cb_2017_us_state_500k.zip", istream, ImportStrategy.NEW_AND_UPDATE, false);

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

    JSONObject sheet = result.getJSONObject("sheet");

    Assert.assertNotNull(sheet);
    Assert.assertEquals("cb_2017_us_state_500k_4326", sheet.getString("name"));

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
  public void testGetAttributeInformationPostalCode() throws JSONException
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

    JSONObject result = this.shapefileService.getShapefileConfiguration(testData.clientRequest.getSessionId(), USATestData.STATE.getCode(), null, null, USATestData.SOURCE.getCode(), "cb_2017_us_state_500k.zip", istream, ImportStrategy.NEW_AND_UPDATE, false);

    Assert.assertTrue(result.getBoolean(GeoObjectImportConfiguration.HAS_POSTAL_CODE));
  }

  @Test
  @Request
  public void testImportShapefileFrazer() throws Throwable
  {
    InputStream istream = Thread.currentThread().getContextClassLoader().getResourceAsStream("shapefile/ntd_zam_operational_28082020.zip.test");

    Assert.assertNotNull(istream);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    /*
     * Build Config
     */
    JSONObject result = this.shapefileService.getShapefileConfiguration(testData.clientRequest.getSessionId(), USATestData.STATE.getCode(), null, null, USATestData.SOURCE.getCode(), "ntd_zam_operational_28082020.zip", istream, ImportStrategy.NEW_AND_UPDATE, false);
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
    config.setHierarchy(hierarchyType);

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

    Assert.assertEquals(Long.valueOf(1011), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(1011), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(1011), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));
  }

  @Test
  @Request
  public void testImportShapefile() throws Throwable
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, null, ImportStrategy.NEW_AND_UPDATE);
    config.setHierarchy(hierarchyType);

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(Long.valueOf(56), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(56), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(56), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    GeoObject object = this.service.getGeoObjectByCode(testData.clientRequest.getSessionId(), "01", USATestData.STATE.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assert.assertNotNull(object);
    Assert.assertNotNull(object.getGeometry());
    Assert.assertEquals("Alabama", object.getLocalizedDisplayLabel());
    Assert.assertEquals(true, object.getExists());
  }

  @Test
  @Request
  public void testUpdateShapefile() throws Throwable
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, null, ImportStrategy.NEW_AND_UPDATE);
    config.setHierarchy(hierarchyType);

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(AllJobStatus.SUCCESS, hist.getStatus().get(0));
    Assert.assertEquals(Long.valueOf(56), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(56), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(56), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    /*
     * Import the shapefile twice
     */
    JSONObject config2 = new JSONObject(hist.getConfigJson());
    config2.remove("historyId");

    ImportHistory hist2 = mockImport((GeoObjectImportConfiguration) GeoObjectImportConfiguration.build(config2.toString()));
    Assert.assertTrue(hist2.getStatus().get(0).equals(AllJobStatus.SUCCESS));

    Assert.assertNotSame(hist.getOid(), hist2.getOid());

    hist2 = ImportHistory.get(hist2.getOid());
    Assert.assertEquals(AllJobStatus.SUCCESS, hist.getStatus().get(0));
    Assert.assertEquals(Long.valueOf(56), hist2.getWorkTotal());
    Assert.assertEquals(Long.valueOf(56), hist2.getWorkProgress());
    Assert.assertEquals(Long.valueOf(56), hist2.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist2.getStage().get(0));

    GeoObject object = this.service.getGeoObjectByCode(testData.clientRequest.getSessionId(), "01", USATestData.STATE.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assert.assertNotNull(object);
    Assert.assertNotNull(object.getGeometry());
    Assert.assertEquals("Alabama", object.getLocalizedDisplayLabel());
  }

  @Test
  @Request
  public void testImportShapefileInteger() throws Throwable
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, null, ImportStrategy.NEW_AND_UPDATE);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    config.setFunction(testInteger.getName(), new BasicColumnFunction("ALAND"));
    config.setHierarchy(hierarchyType);

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(Long.valueOf(56), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(56), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(56), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    GeoObject object = this.service.getGeoObjectByCode(testData.clientRequest.getSessionId(), "01", USATestData.STATE.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assert.assertNotNull(object);
    Assert.assertNotNull(object.getGeometry());
    Assert.assertEquals("Alabama", object.getLocalizedDisplayLabel());
    Assert.assertEquals(131174431216L, object.getValue(testInteger.getName()));
  }

  @Test
  @Request
  public void testImportShapefileNullInteger() throws Throwable
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, null, ImportStrategy.NEW_AND_UPDATE);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    config.setFunction(testInteger.getName(), new BasicColumnFunction("ALAND"));
    config.setHierarchy(hierarchyType);

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

    istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    config = this.getTestConfiguration(istream, null, ImportStrategy.NEW_AND_UPDATE);
    config.setFunction(testInteger.getName(), new BasicColumnFunction("ALAND"));
    config.setCopyBlank(true);

    HashMap<String, Object> row = new HashMap<String, Object>();
    row.put("ALAND", null);
    row.put("NAME", "Alabama2");
    row.put("GEOID", "01");

    try (GeoObjectImporter importer = new GeoObjectImporter(config, new NullImportProgressListener()))
    {
      importer.setFormatSpecificImporter(new NullFormatSpecificImporter());
      importer.importRow(new MapFeatureRow(row, 0L));
    }

    GeoObject object = this.service.getGeoObjectByCode(testData.clientRequest.getSessionId(), "01", USATestData.STATE.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assert.assertNotNull(object);
    Assert.assertNotNull(object.getGeometry());
    Assert.assertEquals("Alabama2", object.getLocalizedDisplayLabel());
    Assert.assertNull(object.getValue(testInteger.getName()));
  }

  @Test
  @Request
  public void testImportShapefileNullInteger_Ignore() throws Throwable
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, null, ImportStrategy.NEW_AND_UPDATE);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    config.setFunction(testInteger.getName(), new BasicColumnFunction("ALAND"));
    config.setHierarchy(hierarchyType);

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

    istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    config = this.getTestConfiguration(istream, null, ImportStrategy.NEW_AND_UPDATE);
    config.setFunction(testInteger.getName(), new BasicColumnFunction("ALAND"));
    config.setCopyBlank(false);

    HashMap<String, Object> row = new HashMap<String, Object>();
    row.put("ALAND", null);
    row.put("NAME", "Alabama2");
    row.put("GEOID", "01");

    try (GeoObjectImporter importer = new GeoObjectImporter(config, new NullImportProgressListener()))
    {
      importer.setFormatSpecificImporter(new NullFormatSpecificImporter());
      importer.importRow(new MapFeatureRow(row, 0L));
    }

    GeoObject object = this.service.getGeoObjectByCode(testData.clientRequest.getSessionId(), "01", USATestData.STATE.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assert.assertNotNull(object);
    Assert.assertNotNull(object.getGeometry());
    Assert.assertEquals("Alabama2", object.getLocalizedDisplayLabel());
    Assert.assertEquals(131174431216L, object.getValue(testInteger.getName()));
  }

  @Test
  @Request
  public void testImportShapefileWithParent() throws Throwable
  {
    GeoObject geoObj = this.service.newGeoObjectInstance(testData.clientRequest.getSessionId(), USATestData.COUNTRY.getCode());
    geoObj.setCode("00");
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "Test Label");
    geoObj.setUid(ServiceFactory.getIdService().getUids(1)[0]);

    ServerGeoObjectIF serverGO = this.objectService.apply(geoObj, TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, true, false, false);
    geoObj = this.service.getGeoObjectByCode(Session.getCurrentSession().getOid(), serverGO.getCode(), serverGO.getType().getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, null, ImportStrategy.NEW_AND_UPDATE);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    config.setHierarchy(hierarchyType);
    config.addParent(new Location(USATestData.COUNTRY.getServerObject(), hierarchyType, new BasicColumnFunction("LSAD"), ParentMatchStrategy.ALL));

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(Long.valueOf(56), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(56), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(56), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    String sessionId = testData.clientRequest.getSessionId();
    GeoObject object = this.service.getGeoObjectByCode(sessionId, "01", USATestData.STATE.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assert.assertEquals("01", object.getCode());

    ParentTreeNode nodes = this.service.getParentGeoObjects(sessionId, object.getCode(), config.getType().getCode(), null, new String[] { USATestData.COUNTRY.getCode() }, false, false, TestDataSet.DEFAULT_OVER_TIME_DATE);

    List<ParentTreeNode> parents = nodes.getParents();

    Assert.assertEquals(1, parents.size());
  }

  @Test
  @Request
  public void testImportShapefileWithParentCode() throws Throwable
  {
    GeoObject geoObj = this.service.newGeoObjectInstance(testData.clientRequest.getSessionId(), USATestData.COUNTRY.getCode());
    geoObj.setCode("00");
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "Test Label");
    geoObj.setUid(ServiceFactory.getIdService().getUids(1)[0]);

    ServerGeoObjectIF serverGO = this.objectService.apply(geoObj, TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, true, false, false);
    geoObj = this.service.getGeoObjectByCode(Session.getCurrentSession().getOid(), serverGO.getCode(), serverGO.getType().getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, null, ImportStrategy.NEW_AND_UPDATE);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    config.setHierarchy(hierarchyType);
    config.addParent(new Location(USATestData.COUNTRY.getServerObject(), hierarchyType, new BasicColumnFunction("LSAD"), ParentMatchStrategy.CODE));

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

    hist = ImportHistory.get(hist.getOid());

    Assert.assertEquals(Long.valueOf(56), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(56), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(56), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    final GeoObjectImportConfiguration test = new GeoObjectImportConfiguration();
    test.fromJSON(hist.getConfigJson(), false);

    // TODO
    // Assert.assertEquals(config.getParentLookupType(),
    // test.getParentLookupType());

    String sessionId = testData.clientRequest.getSessionId();
    GeoObject object = this.service.getGeoObjectByCode(sessionId, "01", USATestData.STATE.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assert.assertEquals("01", object.getCode());

    ParentTreeNode nodes = this.service.getParentGeoObjects(sessionId, object.getCode(), config.getType().getCode(), null, new String[] { USATestData.COUNTRY.getCode() }, false, false, TestDataSet.DEFAULT_OVER_TIME_DATE);

    List<ParentTreeNode> parents = nodes.getParents();

    Assert.assertEquals(1, parents.size());
  }

  @Test
  @Request
  public void testImportShapefileWithBadParentCode() throws Throwable
  {
    GeoObject geoObj = this.service.newGeoObjectInstance(testData.clientRequest.getSessionId(), USATestData.COUNTRY.getCode());
    geoObj.setCode("00");
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "Test Label");
    geoObj.setUid(ServiceFactory.getIdService().getUids(1)[0]);

    ServerGeoObjectIF serverGO = this.objectService.apply(geoObj, TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, true, false, false);
    geoObj = this.service.getGeoObjectByCode(Session.getCurrentSession().getOid(), serverGO.getCode(), serverGO.getType().getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, null, ImportStrategy.NEW_AND_UPDATE);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    config.setHierarchy(hierarchyType);
    config.addParent(new Location(USATestData.COUNTRY.getServerObject(), hierarchyType, new BasicColumnFunction("GEOID"), ParentMatchStrategy.CODE));

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.FEEDBACK));

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(Long.valueOf(56), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(56), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(0), hist.getImportedRecords());
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
    ServerGeoObjectQuery query = this.objectService.createQuery(USATestData.STATE.getServerObject(), config.getStartDate());
    query.setRestriction(new ServerCodeRestriction(USATestData.STATE.getServerObject(), "01"));

    Assert.assertNull(query.getSingleResult());
  }

  @Test
  @Request
  public void testImportShapefileExcludeParent() throws Throwable
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, null, ImportStrategy.NEW_AND_UPDATE);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    config.setHierarchy(hierarchyType);

    config.addParent(new Location(USATestData.COUNTRY.getServerObject(), hierarchyType, new BasicColumnFunction("LSAD"), ParentMatchStrategy.ALL));
    config.addExclusion(GeoObjectImportConfiguration.PARENT_EXCLUSION, "00");

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(Long.valueOf(56), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(56), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(0), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    // Ensure the geo objects were not created
    ServerGeoObjectQuery query = this.objectService.createQuery(USATestData.STATE.getServerObject(), config.getStartDate());
    query.setRestriction(new ServerCodeRestriction(USATestData.STATE.getServerObject(), "01"));

    Assert.assertNull(query.getSingleResult());
  }

  @Test
  @Request
  public void testImportShapefileWithBadParent() throws Throwable
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, null, ImportStrategy.NEW_AND_UPDATE);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    config.setHierarchy(hierarchyType);

    config.addParent(new Location(USATestData.COUNTRY.getServerObject(), hierarchyType, new BasicColumnFunction("LSAD"), ParentMatchStrategy.ALL));

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.FEEDBACK));

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(Long.valueOf(56), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(56), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(0), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.VALIDATION_RESOLVE, hist.getStage().get(0));

    JSONObject page = new JSONObject(this.etlService.getValidationProblems(testData.clientRequest.getSessionId(), hist.getOid(), false, 100, 1).toString());
    JSONArray results = page.getJSONArray("resultSet");
    Assert.assertEquals(1, results.length());

    // Ensure the geo objects were not created
    ServerGeoObjectQuery query = this.objectService.createQuery(USATestData.STATE.getServerObject(), config.getStartDate());
    query.setRestriction(new ServerCodeRestriction(USATestData.STATE.getServerObject(), "01"));

    Assert.assertNull(query.getSingleResult());
  }

  @Test
  @Request
  public void testImportShapefileWithTerm() throws Throwable
  {
    Term term = this.service.createTerm(testData.clientRequest.getSessionId(), testTerm.fetchRootAsTerm().getCode(), new Term("00", new LocalizedValue("00"), new LocalizedValue("")).toJSON().toString());

    try
    {
      TestDataSet.refreshTerms((AttributeTermType) testTerm.fetchDTO());

      InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

      Assert.assertNotNull(istream);

      GeoObjectImportConfiguration config = this.getTestConfiguration(istream, (AttributeTermType) testTerm.fetchDTO(), ImportStrategy.NEW_AND_UPDATE);

      ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

      config.setHierarchy(hierarchyType);

      ImportHistory hist = mockImport(config);
      Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

      hist = ImportHistory.get(hist.getOid());
      Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));
      Assert.assertEquals(Long.valueOf(56), hist.getWorkTotal());
      Assert.assertEquals(Long.valueOf(56), hist.getWorkProgress());
      Assert.assertEquals(Long.valueOf(56), hist.getImportedRecords());

      String sessionId = testData.clientRequest.getSessionId();
      GeoObject object = this.service.getGeoObjectByCode(sessionId, "01", USATestData.STATE.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

      Assert.assertEquals("01", object.getCode());
    }
    finally
    {
      this.service.deleteTerm(testData.clientRequest.getSessionId(), testTerm.fetchRootAsTerm().getCode(), term.getCode());

      TestDataSet.refreshTerms((AttributeTermType) testTerm.fetchDTO());
    }
  }

  @Test
  @Request
  public void testImportShapefileWithBadTerm() throws Throwable
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, (AttributeTermType) testTerm.fetchDTO(), ImportStrategy.NEW_AND_UPDATE);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    config.setHierarchy(hierarchyType);

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.FEEDBACK));

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(Long.valueOf(56), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(56), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(0), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.VALIDATION_RESOLVE, hist.getStage().get(0));

    JSONObject page = new JSONObject(this.etlService.getValidationProblems(testData.clientRequest.getSessionId(), hist.getOid(), false, 100, 1).toString());
    JSONArray results = page.getJSONArray("resultSet");
    Assert.assertEquals(1, results.length());

    // Assert the values of the problem
    JSONObject problem = results.getJSONObject(0);

    Assert.assertEquals("00", problem.getString("label"));
    Assert.assertEquals(testTerm.fetchRootAsTerm().getCode(), problem.getString("parentCode"));
    Assert.assertEquals(testTerm.getName(), problem.getString("attributeCode"));
    Assert.assertEquals(testTerm.getLabel(), problem.getString("attributeLabel"));

    // Ensure the geo objects were not created
    ServerGeoObjectQuery query = this.objectService.createQuery(USATestData.STATE.getServerObject(), config.getStartDate());
    query.setRestriction(new ServerCodeRestriction(USATestData.STATE.getServerObject(), "01"));

    Assert.assertNull(query.getSingleResult());
  }

  @Test
  @Request
  public void testQueueImports() throws InterruptedException, JSONException
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, null, ImportStrategy.NEW_AND_UPDATE);

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
    Assert.assertEquals(Long.valueOf(56), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(56), hist.getWorkProgress());

    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    GeoObject object = this.service.getGeoObjectByCode(testData.clientRequest.getSessionId(), "01", USATestData.STATE.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assert.assertNotNull(object);
    Assert.assertNotNull(object.getGeometry());
    Assert.assertEquals("Alabama", object.getLocalizedDisplayLabel());
    Assert.assertEquals(true, object.getExists());

    SchedulerTestUtils.waitUntilStatus(hist2.getOid(), AllJobStatus.SUCCESS);

    hist2 = ImportHistory.get(hist2.getOid());
    Assert.assertEquals(Long.valueOf(56), hist2.getWorkTotal());
    Assert.assertEquals(Long.valueOf(56), hist2.getWorkProgress());

    Assert.assertEquals(ImportStage.COMPLETE, hist2.getStage().get(0));
  }

  private ImportHistory importShapefile(String sessionId, String config) throws InterruptedException
  {
    String retConfig = this.etlService.doImport(sessionId, config).toString();

    GeoObjectImportConfiguration configuration = (GeoObjectImportConfiguration) ImportConfiguration.build(retConfig, true);

    String historyId = configuration.getHistoryId();

    Thread.sleep(100);

    return ImportHistory.get(historyId);
  }

  private ImportHistory mockImport(GeoObjectImportConfiguration config) throws Throwable
  {
    if (config.getStartDate() == null)
    {
      config.setStartDate(new Date());
    }

    if (config.getEndDate() == null)
    {
      config.setEndDate(new Date());
    }

    config.setImportStrategy(ImportStrategy.NEW_AND_UPDATE);

    DataImportJob job = new DataImportJob();
    job.apply();
    ImportHistory hist = (ImportHistory) job.createNewHistory();

    config.setHistoryId(hist.getOid());
    config.setJobId(job.getOid());

    ServerGeoObjectType type = config.getType();

    hist.appLock();
    hist.setImportFileId(config.getVaultFileId());
    hist.setConfigJson(config.toJSON().toString());
    hist.setOrganization(type.getOrganization().getOrganization());
    hist.setGeoObjectTypeCode(type.getCode());
    hist.apply();

    ExecutionContext context = job.startSynchronously(hist);

    hist = (ImportHistory) context.getHistory();
    return hist;
  }

  @Test
  @Request
  public void testBadParentSynonymAndResume() throws Throwable
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, null, ImportStrategy.NEW_AND_UPDATE);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    config.setHierarchy(hierarchyType);

    config.addParent(new Location(USATestData.COUNTRY.getServerObject(), hierarchyType, new BasicColumnFunction("LSAD"), ParentMatchStrategy.ALL));

    // ImportHistory hist = mockImport(config);
    // Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.FEEDBACK));

    ImportHistory hist = importShapefile(testData.clientRequest.getSessionId(), config.toJSON().toString());
    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.FEEDBACK);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(Long.valueOf(56), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(56), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(0), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.VALIDATION_RESOLVE, hist.getStage().get(0));

    JSONObject page = new JSONObject(this.etlService.getValidationProblems(testData.clientRequest.getSessionId(), hist.getOid(), false, 100, 1).toString());
    JSONArray results = page.getJSONArray("resultSet");
    Assert.assertEquals(1, results.length());

    // Ensure the geo objects were not created
    ServerGeoObjectQuery query = this.objectService.createQuery(USATestData.STATE.getServerObject(), config.getStartDate());
    query.setRestriction(new ServerCodeRestriction(USATestData.STATE.getServerObject(), "01"));

    Assert.assertNull(query.getSingleResult());

    // Resolve the import problem with a synonym
    GeoObject geoObj = this.service.newGeoObjectInstance(testData.clientRequest.getSessionId(), USATestData.COUNTRY.getCode());
    geoObj.setCode("99");
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "Test Label99");
    geoObj.setUid(ServiceFactory.getIdService().getUids(1)[0]);

    ServerGeoObjectIF serverGo = this.objectService.apply(geoObj, TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, true, false, false);

    JSONObject valRes = new JSONObject();
    valRes.put("validationProblemId", results.getJSONObject(0).getString("id"));
    valRes.put("resolution", ValidationResolution.SYNONYM);
    valRes.put("code", serverGo.getCode());
    valRes.put("typeCode", serverGo.getType().getCode());
    valRes.put("label", "00");

    this.etlService.submitValidationProblemResolution(testData.clientRequest.getSessionId(), valRes.toString());

    ValidationProblem vp = ValidationProblem.get(results.getJSONObject(0).getString("id"));
    Assert.assertEquals(ValidationResolution.SYNONYM.name(), vp.getResolution());
    Assert.assertEquals(ParentReferenceProblem.DEFAULT_SEVERITY, vp.getSeverity());

    ImportHistory hist2 = importShapefile(testData.clientRequest.getSessionId(), hist.getConfigJson());
    Assert.assertEquals(hist.getOid(), hist2.getOid());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.RUNNING, 2000);
    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.SUCCESS);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));
    Assert.assertEquals(Long.valueOf(56), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(56), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(56), hist.getImportedRecords());

    String sessionId = testData.clientRequest.getSessionId();
    GeoObject go = this.service.getGeoObjectByCode(sessionId, "01", USATestData.STATE.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assert.assertEquals("01", go.getCode());

    ParentTreeNode nodes = this.service.getParentGeoObjects(sessionId, go.getCode(), config.getType().getCode(), null, new String[] { USATestData.COUNTRY.getCode() }, false, false, TestDataSet.DEFAULT_OVER_TIME_DATE);

    List<ParentTreeNode> parents = nodes.getParents();

    Assert.assertEquals(1, parents.size());

    JSONObject page2 = new JSONObject(this.etlService.getValidationProblems(testData.clientRequest.getSessionId(), hist.getOid(), false, 100, 1).toString());
    JSONArray results2 = page2.getJSONArray("resultSet");
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
   * @throws Throwable
   */
  @Test
  @Request
  public void testResumeImport() throws Throwable
  {
    TestGeoObjectInfo parent = new TestGeoObjectInfo("00", USATestData.COUNTRY, USATestData.SOURCE);
    parent.apply();

    List<String> sortedGeoIds = shapefileSort();

    DataImportJob job = new DataImportJob();
    job.setRunAsUserId(testData.clientRequest.getSessionUser().getOid());
    job.apply();

    ImportHistory fakeImportHistory = new ImportHistory();
    fakeImportHistory.setStartTime(new Date());
    fakeImportHistory.addStatus(AllJobStatus.RUNNING);
    fakeImportHistory.addStage(ImportStage.IMPORT);
    fakeImportHistory.setWorkProgress(10L);
    fakeImportHistory.setImportedRecords(0L);
    fakeImportHistory.setCompletedRowsJson(Range.serialize(new TreeSet<>(Arrays.asList(new Range(1, 6)))));
    fakeImportHistory.apply();

    JobHistoryRecord record = new JobHistoryRecord(job, fakeImportHistory);
    record.apply();

    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, null, ImportStrategy.NEW_ONLY);
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
    Assert.assertEquals(Long.valueOf(56), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(56), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(50), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    for (int i = 0; i < 6; ++i)
    {
      String geoId = sortedGeoIds.get(i);

      try
      {
        this.service.getGeoObjectByCode(testData.clientRequest.getSessionId(), geoId, USATestData.STATE.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

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

      GeoObject object = this.service.getGeoObjectByCode(testData.clientRequest.getSessionId(), geoId, USATestData.STATE.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

      Assert.assertNotNull(object);
      Assert.assertEquals(geoId, object.getCode());

      Geometry geometry = object.getGeometry();

      Assert.assertNotNull(geometry);
    }

    JSONObject json = new JSONObject(this.etlService.getImportErrors(testData.clientRequest.getSessionId(), hist.getOid(), false, 100, 1).toString());

    Assert.assertEquals(0, json.getJSONArray("resultSet").length());
  }

  @Test
  @Request
  public void testImportSubtypeShapefile() throws Throwable
  {
    String parentCode = "ZZZZ000";

    GeoObject geoObj = this.service.newGeoObjectInstance(testData.clientRequest.getSessionId(), USATestData.DISTRICT.getCode());
    geoObj.setCode(parentCode);
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "Test Label");
    geoObj.setUid(ServiceFactory.getIdService().getUids(1)[0]);

    ServerGeoObjectIF serverGO = this.objectService.apply(geoObj, TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, true, false, false);

    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    GeoObjectImportConfiguration config = this.getTestConfiguration(istream, null, ImportStrategy.NEW_AND_UPDATE, USATestData.HEALTH_POST);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    config.setHierarchy(hierarchyType);
    config.addParent(new Location(USATestData.DISTRICT.getServerObject(), hierarchyType, new ConstantShapefileFunction(serverGO.getCode()), ParentMatchStrategy.CODE));

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(Long.valueOf(56), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(56), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(56), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    GeoObject object = this.service.getGeoObjectByCode(testData.clientRequest.getSessionId(), "01", USATestData.HEALTH_POST.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assert.assertNotNull(object);
    Assert.assertNotNull(object.getGeometry());
    Assert.assertEquals("Alabama", object.getLocalizedDisplayLabel());
  }

  private GeoObjectImportConfiguration getTestConfiguration(InputStream istream, AttributeTermType testTerm, ImportStrategy strategy) throws JSONException
  {
    return getTestConfiguration(istream, testTerm, strategy, USATestData.STATE);
  }

  private GeoObjectImportConfiguration getTestConfiguration(InputStream istream, AttributeTermType testTerm, ImportStrategy strategy, TestGeoObjectTypeInfo info) throws JSONException
  {
    JSONObject result = this.shapefileService.getShapefileConfiguration(testData.clientRequest.getSessionId(), info.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, USATestData.SOURCE.getCode(), "cb_2017_us_state_500k.zip", istream, strategy, false);
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
