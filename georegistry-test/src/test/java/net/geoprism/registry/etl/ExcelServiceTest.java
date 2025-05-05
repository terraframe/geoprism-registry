/**
 *
 */
package net.geoprism.registry.etl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutionContext;
import com.runwaysdk.system.scheduler.JobHistoryRecord;
import com.runwaysdk.system.scheduler.SchedulerManager;

import net.geoprism.data.importer.BasicColumnFunction;
import net.geoprism.data.importer.FeatureRow;
import net.geoprism.data.importer.ShapefileFunction;
import net.geoprism.ontology.Classifier;
import net.geoprism.registry.DataNotFoundException;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.USADatasetTest;
import net.geoprism.registry.classification.ClassificationTypeTest;
import net.geoprism.registry.etl.FormatSpecificImporterFactory.FormatImporterType;
import net.geoprism.registry.etl.ObjectImporterFactory.ObjectImportType;
import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.etl.upload.ImportHistoryProgressScribe.Range;
import net.geoprism.registry.excel.GeoObjectExcelExporter;
import net.geoprism.registry.io.DelegateShapefileFunction;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.io.Location;
import net.geoprism.registry.io.LocationBuilder;
import net.geoprism.registry.io.ParentMatchStrategy;
import net.geoprism.registry.io.PostalCodeFactory;
import net.geoprism.registry.model.Classification;
import net.geoprism.registry.model.ClassificationType;
import net.geoprism.registry.model.EdgeConstant;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerParentTreeNode;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.query.ServerCodeRestriction;
import net.geoprism.registry.query.ServerGeoObjectQuery;
import net.geoprism.registry.service.business.ClassificationBusinessServiceIF;
import net.geoprism.registry.service.business.ClassificationTypeBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.service.business.ServiceFactory;
import net.geoprism.registry.service.business.TermBusinessServiceIF;
import net.geoprism.registry.service.request.ETLService;
import net.geoprism.registry.service.request.ExcelService;
import net.geoprism.registry.test.SchedulerTestUtils;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.USATestData;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class ExcelServiceTest extends USADatasetTest implements InstanceTestClassListener
{
  private static ClassificationType           type;

  protected static String                     CODE      = "Test Term";

  private static AttributeTermType            testTerm;

  private static AttributeIntegerType         testInteger;

  private static AttributeDateType            testDate;

  private static AttributeBooleanType         testBoolean;

  private static AttributeClassificationType  testClassification;

  private final Integer                       ROW_COUNT = 2;

  @Autowired
  private GeoObjectTypeBusinessServiceIF      typeService;

  @Autowired
  private GeoObjectBusinessServiceIF          objectService;

  @Autowired
  private TermBusinessServiceIF               termService;

  @Autowired
  private ExcelService                        excelService;

  @Autowired
  private ETLService                          etlService;

  @Autowired
  private ClassificationTypeBusinessServiceIF cTypeService;

  @Autowired
  private ClassificationBusinessServiceIF     cService;

  @Override
  @Request
  public void beforeClassSetup() throws Exception
  {
    type = this.cTypeService.apply(ClassificationTypeTest.createMock());

    Classification root = this.cService.newInstance(type);
    root.setCode(CODE);
    root.setDisplayLabel(new LocalizedValue("Test Classification"));

    this.cService.apply(root, null);

    TestDataSet.deleteAllSchedulerData();

    super.beforeClassSetup();

    testTerm = (AttributeTermType) TestDataSet.createTermAttribute("testTerm", "testTermLocalName", USATestData.DISTRICT, null).fetchDTO();
    testBoolean = (AttributeBooleanType) TestDataSet.createAttribute("testBoolean", "testBooleanLocalName", USATestData.DISTRICT, AttributeBooleanType.TYPE).fetchDTO();
    testDate = (AttributeDateType) TestDataSet.createAttribute("testDate", "testDateLocalName", USATestData.DISTRICT, AttributeDateType.TYPE).fetchDTO();
    testInteger = (AttributeIntegerType) TestDataSet.createAttribute("testInteger", "testIntegerLocalName", USATestData.DISTRICT, AttributeIntegerType.TYPE).fetchDTO();

    if (!SchedulerManager.initialized())
    {
      SchedulerManager.start();
    }

    testClassification = (AttributeClassificationType) AttributeType.factory("testClassification", new LocalizedValue("testClassificationLocalName"), new LocalizedValue("testClassificationLocalDescrip"), AttributeClassificationType.TYPE, false, false, false);
    testClassification.setClassificationType(type.getCode());
    testClassification.setRootTerm(root.toTerm());

    ServerGeoObjectType got = ServerGeoObjectType.get(USATestData.DISTRICT.getCode());
    testClassification = (AttributeClassificationType) this.typeService.createAttributeType(got, testClassification.toJSON().toString());
  }

  @Override
  @Request
  public void afterClassSetup() throws Exception
  {
    super.afterClassSetup();

    this.cTypeService.delete(type);
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
  }

  @Test
  @Request
  public void testGetAttributeInformation()
  {
    PostalCodeFactory.remove(USATestData.DISTRICT.getServerObject());

    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    JSONObject result = this.excelService.getExcelConfiguration(testData.clientRequest.getSessionId(), USATestData.DISTRICT.getCode(), null, null, "test-spreadsheet.xlsx", istream, ImportStrategy.NEW_AND_UPDATE, false);

    Assert.assertFalse(result.getBoolean(GeoObjectImportConfiguration.HAS_POSTAL_CODE));

    Assert.assertNotNull(result.getJSONObject(GeoObjectImportConfiguration.TYPE));

    JSONObject sheet = result.getJSONObject(GeoObjectImportConfiguration.SHEET);

    Assert.assertNotNull(sheet);
    Assert.assertEquals("Objects", sheet.getString("name"));

    JSONObject attributes = sheet.getJSONObject(GeoObjectType.JSON_ATTRIBUTES);

    Assert.assertNotNull(attributes);

    JSONArray fields = attributes.getJSONArray(GeoObjectImportConfiguration.TEXT);

    Assert.assertEquals(8, fields.length());
    Assert.assertEquals("Longitude", fields.getString(0));

    Assert.assertEquals(4, attributes.getJSONArray(GeoObjectImportConfiguration.NUMERIC).length());
    Assert.assertEquals(1, attributes.getJSONArray(AttributeBooleanType.TYPE).length());
    Assert.assertEquals(1, attributes.getJSONArray(AttributeDateType.TYPE).length());
  }

  @Test
  @Request
  public void testGetAttributeInformationPostalCode()
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    ServerGeoObjectType type = USATestData.DISTRICT.getServerObject();

    PostalCodeFactory.addPostalCode(type, new LocationBuilder()
    {
      @Override
      public Location build(ShapefileFunction function)
      {
        return null;
      }
    });

    Assert.assertNotNull(istream);

    JSONObject result = this.excelService.getExcelConfiguration(testData.clientRequest.getSessionId(), USATestData.DISTRICT.getCode(), null, null, "test-spreadsheet.xlsx", istream, ImportStrategy.NEW_AND_UPDATE, false);

    Assert.assertTrue(result.getBoolean(GeoObjectImportConfiguration.HAS_POSTAL_CODE));
  }

  private ImportHistory importExcelFile(String sessionId, String config) throws InterruptedException
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
  public void testImportSpreadsheet() throws Throwable
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    JSONObject json = this.getTestConfiguration(istream, null, ImportStrategy.NEW_AND_UPDATE);
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    config.setHierarchy(hierarchyType);

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    ServerGeoObjectIF object = this.objectService.getGeoObjectByCode("0001", USATestData.DISTRICT.getCode());

    Assert.assertNotNull(object);
    Assert.assertEquals("Test", object.getDisplayLabel().getValue());

    Geometry geometry = object.getGeometry();

    Assert.assertNotNull(geometry);

    Double lat = Double.valueOf(2.232343);
    Double lon = Double.valueOf(1.134232);

    GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
    MultiPoint expected = factory.createMultiPoint(new Point[] { factory.createPoint(new Coordinate(lon, lat)) });

    Assert.assertEquals(expected, geometry);
  }

  @Test
  @Request
  public void testImportSpreadsheetInteger() throws Throwable
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    JSONObject json = this.getTestConfiguration(istream, null, ImportStrategy.NEW_AND_UPDATE);
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    config.setFunction(testInteger.getName(), new BasicColumnFunction("Test Integer"));
    config.setHierarchy(hierarchyType);

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    ServerGeoObjectIF object = this.objectService.getGeoObjectByCode("0001", USATestData.DISTRICT.getCode());

    Assert.assertNotNull(object);
    Assert.assertEquals(Long.valueOf(123), object.getValue(testInteger.getName()));
  }

  @Test
  @Request
  public void testImportSpreadsheetIntegerOverflow() throws Throwable
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet-integer-overflow.xlsx");

    Assert.assertNotNull(istream);

    JSONObject json = this.getTestConfiguration(istream, null, ImportStrategy.NEW_AND_UPDATE);
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    config.setFunction(testInteger.getName(), new BasicColumnFunction("Test Integer"));
    config.setHierarchy(hierarchyType);

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.FEEDBACK));

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(Long.valueOf(1), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(1), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(0), hist.getImportedRecords());

    Assert.assertEquals(ImportStage.VALIDATION_RESOLVE, hist.getStage().get(0));

    // Ensure the geo objects were not created
    ServerGeoObjectQuery query = this.objectService.createQuery(USATestData.DISTRICT.getServerObject(), config.getStartDate());
    query.setRestriction(new ServerCodeRestriction(USATestData.DISTRICT.getServerObject(), "0001"));

    Assert.assertNull(query.getSingleResult());
  }

  @Test
  @Request
  public void testImportSpreadsheetBadInteger() throws Throwable
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet-bad-integer.xlsx");

    Assert.assertNotNull(istream);

    JSONObject json = this.getTestConfiguration(istream, null, ImportStrategy.NEW_AND_UPDATE);
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    config.setFunction(testInteger.getName(), new BasicColumnFunction("Test Integer"));
    config.setHierarchy(hierarchyType);

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.FEEDBACK));

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(Long.valueOf(1), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(1), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(0), hist.getImportedRecords());

    Assert.assertEquals(ImportStage.VALIDATION_RESOLVE, hist.getStage().get(0));

    // Ensure the geo objects were not created
    ServerGeoObjectQuery query = this.objectService.createQuery(USATestData.DISTRICT.getServerObject(), config.getStartDate());
    query.setRestriction(new ServerCodeRestriction(USATestData.DISTRICT.getServerObject(), "0001"));

    Assert.assertNull(query.getSingleResult());
  }

  @Test
  @Request
  public void testImportSpreadsheetDate() throws Throwable
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    JSONObject json = this.getTestConfiguration(istream, null, ImportStrategy.NEW_AND_UPDATE);
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    config.setFunction(testDate.getName(), new BasicColumnFunction("Test Date"));
    config.setHierarchy(hierarchyType);

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    ServerGeoObjectIF object = this.objectService.getGeoObjectByCode("0001", USATestData.DISTRICT.getCode());

    Calendar calendar = Calendar.getInstance();
    calendar.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);
    calendar.clear();
    calendar.set(2018, Calendar.FEBRUARY, 12, 0, 0, 0);

    Assert.assertNotNull(object);
    Assert.assertEquals(calendar.getTime(), object.getValue(testDate.getName(), TestDataSet.DEFAULT_OVER_TIME_DATE));
  }

  @Test
  @Request
  public void testImportSpreadsheetBoolean() throws Throwable
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    JSONObject json = this.getTestConfiguration(istream, null, ImportStrategy.NEW_AND_UPDATE);
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    config.setFunction(testBoolean.getName(), new BasicColumnFunction("Test Boolean"));
    config.setHierarchy(hierarchyType);

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    ServerGeoObjectIF object = this.objectService.getGeoObjectByCode("0001", USATestData.DISTRICT.getCode());

    Assert.assertNotNull(object);
    Assert.assertEquals(Boolean.valueOf(true), object.getValue(testBoolean.getName(), TestDataSet.DEFAULT_OVER_TIME_DATE));
  }

  @Test
  @Request
  public void testCreateWorkbook() throws Throwable
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    JSONObject json = this.getTestConfiguration(istream, null, ImportStrategy.NEW_AND_UPDATE);
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    config.setHierarchy(hierarchyType);

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    ServerGeoObjectType type = USATestData.DISTRICT.getServerObject();

    List<ServerGeoObjectIF> objects = getObjects(type, config.getStartDate());

    GeoObjectExcelExporter exporter = new GeoObjectExcelExporter(type, hierarchyType, objects, USATestData.DEFAULT_OVER_TIME_DATE);
    Workbook workbook = exporter.createWorkbook();

    Assert.assertEquals(1, workbook.getNumberOfSheets());

    Sheet sheet = workbook.getSheetAt(0);

    Assert.assertEquals(WorkbookUtil.createSafeSheetName(type.getLabel().getValue()), sheet.getSheetName());
  }

  @Test
  @Request
  public void testExport() throws IOException
  {
    Term term = this.termService.createTerm(testTerm.getRootTerm().getCode(), new Term("Test Term", new LocalizedValue("Test Term"), new LocalizedValue("")));
    Classifier classy = Classifier.getByKey(RegistryConstants.REGISTRY_PACKAGE + "." + testTerm.getRootTerm().getCode() + "." + term.getCode());

    try
    {
      TestDataSet.refreshTerms(testTerm);

      Calendar calendar = Calendar.getInstance();
      calendar.clear();
      calendar.set(2018, Calendar.FEBRUARY, 12, 0, 0, 0);

      GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
      MultiPoint point = geometryFactory.createMultiPoint(new Point[] { geometryFactory.createPoint(new Coordinate(-104.991531, 39.742043)) });

      ServerGeoObjectIF geoObj = this.objectService.newInstance(USATestData.DISTRICT.getServerObject());
      geoObj.setCode("00");
      geoObj.setDisplayLabel(new LocalizedValue("Test Label"));
      geoObj.setUid(ServiceFactory.getIdService().getUids(1)[0]);
      geoObj.setGeometry(point);
      geoObj.setValue(testTerm.getName(), classy.getOid(), USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
      geoObj.setValue(testInteger.getName(), 23L, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
      geoObj.setValue(testDate.getName(), calendar.getTime(), USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
      geoObj.setValue(testBoolean.getName(), true, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);

      this.objectService.apply(geoObj, false);

      geoObj = this.objectService.getGeoObjectByCode(geoObj.getCode(), geoObj.getType().getCode());

      InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

      Assert.assertNotNull(istream);

      ServerGeoObjectType type = USATestData.DISTRICT.getServerObject();
      ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

      // Ensure the geo objects were not created
      List<ServerGeoObjectIF> objects = getObjects(type, USATestData.DEFAULT_OVER_TIME_DATE);

      GeoObjectExcelExporter exporter = new GeoObjectExcelExporter(type, hierarchyType, objects, USATestData.DEFAULT_OVER_TIME_DATE);
      InputStream export = exporter.export();

      Assert.assertNotNull(export);

      IOUtils.copy(export, NullOutputStream.NULL_OUTPUT_STREAM);
    }
    finally
    {
      this.termService.deleteTerm(testTerm.getRootTerm(), term.getCode());
    }
  }

  @Test
  @Request
  public void testImportExcelWithParent() throws Throwable
  {
    ServerGeoObjectIF geoObj = this.objectService.newInstance(USATestData.STATE.getServerObject());
    geoObj.setCode("00");
    geoObj.setDisplayLabel(new LocalizedValue("Test Label"));
    geoObj.setUid(ServiceFactory.getIdService().getUids(1)[0]);

    this.objectService.apply(geoObj, true);
    geoObj = this.objectService.getGeoObjectByCode(geoObj.getCode(), geoObj.getType().getCode());

    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    JSONObject json = this.getTestConfiguration(istream, null, ImportStrategy.NEW_AND_UPDATE);

    ServerHierarchyType hierarchyType = USATestData.HIER_ADMIN.getServerObject();

    GeoObjectImportConfiguration config = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    config.setStartDate(USATestData.DEFAULT_OVER_TIME_DATE);
    config.setEndDate(USATestData.DEFAULT_OVER_TIME_DATE);
    config.setHierarchy(hierarchyType);

    config.addParent(new Location(USATestData.STATE.getServerObject(), hierarchyType, new BasicColumnFunction("Parent"), ParentMatchStrategy.ALL));

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    ServerGeoObjectIF object = this.objectService.getGeoObjectByCode("0001", USATestData.DISTRICT.getCode());

    Assert.assertEquals("0001", object.getCode());

    ServerParentTreeNode nodes = this.objectService.getParentGeoObjects(object, config.getHierarchy(), new String[] { USATestData.STATE.getCode() }, false, false, TestDataSet.DEFAULT_OVER_TIME_DATE);

    List<ServerParentTreeNode> parents = nodes.getParents();

    Assert.assertEquals(1, parents.size());
  }

  @Test
  @Request
  public void testImportExcelWithPostalCode() throws Throwable
  {
    ServerGeoObjectType type = USATestData.DISTRICT.getServerObject();

    PostalCodeFactory.addPostalCode(type, new LocationBuilder()
    {
      @Override
      public Location build(ShapefileFunction function)
      {
        ServerGeoObjectType type = USATestData.STATE.getServerObject();

        DelegateShapefileFunction delegate = new DelegateShapefileFunction(function)
        {
          @Override
          public Object getValue(FeatureRow feature)
          {
            String code = (String) super.getValue(feature);

            return code.substring(0, 2);
          }
        };

        return new Location(type, USATestData.HIER_ADMIN.getServerObject(), delegate, ParentMatchStrategy.ALL);
      }
    });

    ServerGeoObjectIF geoObj = this.objectService.newInstance(USATestData.STATE.getServerObject());
    geoObj.setCode("00");
    geoObj.setDisplayLabel(new LocalizedValue("Test Label"));
    geoObj.setUid(ServiceFactory.getIdService().getUids(1)[0]);

    this.objectService.apply(geoObj, true);
    geoObj = this.objectService.getGeoObjectByCode(geoObj.getCode(), geoObj.getType().getCode());

    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    JSONObject json = this.getTestConfiguration(istream, null, ImportStrategy.NEW_AND_UPDATE);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    config.setStartDate(USATestData.DEFAULT_OVER_TIME_DATE);
    config.setEndDate(USATestData.DEFAULT_END_TIME_DATE);
    config.setHierarchy(hierarchyType);

    config.addParent(new Location(USATestData.STATE.getServerObject(), hierarchyType, new BasicColumnFunction("Parent"), ParentMatchStrategy.ALL));

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    ServerGeoObjectIF object = this.objectService.getGeoObjectByCode("0001", USATestData.DISTRICT.getCode());

    Assert.assertEquals("0001", object.getCode());

    ServerParentTreeNode nodes = this.objectService.getParentGeoObjects(object, config.getHierarchy(), new String[] { USATestData.STATE.getCode() }, false, false, TestDataSet.DEFAULT_OVER_TIME_DATE);

    List<ServerParentTreeNode> parents = nodes.getParents();

    Assert.assertEquals(1, parents.size());
  }

  @Test
  @Request
  public void testImportExcelExcludeParent() throws Throwable
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    JSONObject json = this.getTestConfiguration(istream, null, ImportStrategy.NEW_AND_UPDATE);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    config.setHierarchy(hierarchyType);

    config.addParent(new Location(USATestData.COUNTRY.getServerObject(), hierarchyType, new BasicColumnFunction("Parent"), ParentMatchStrategy.ALL));
    config.addExclusion(GeoObjectImportConfiguration.PARENT_EXCLUSION, "00");

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(1), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    // Ensure the geo objects were not created
    ServerGeoObjectQuery query = this.objectService.createQuery(USATestData.DISTRICT.getServerObject(), config.getStartDate());
    query.setRestriction(new ServerCodeRestriction(USATestData.DISTRICT.getServerObject(), "0001"));

    Assert.assertNull(query.getSingleResult());
  }

  @Test
  @Request
  public void testImportExcelWithBadParent() throws Throwable
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    JSONObject json = this.getTestConfiguration(istream, null, ImportStrategy.NEW_AND_UPDATE);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    config.setHierarchy(hierarchyType);

    config.addParent(new Location(USATestData.COUNTRY.getServerObject(), hierarchyType, new BasicColumnFunction("Parent"), ParentMatchStrategy.ALL));

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.FEEDBACK));

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(0), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.VALIDATION_RESOLVE, hist.getStage().get(0));

    JSONObject page = new JSONObject(this.etlService.getValidationProblems(testData.clientRequest.getSessionId(), hist.getOid(), false, 100, 1).toString());
    JSONArray results = page.getJSONArray("resultSet");
    Assert.assertEquals(1, results.length());

    // Ensure the geo objects were not created
    ServerGeoObjectQuery query = this.objectService.createQuery(USATestData.DISTRICT.getServerObject(), config.getStartDate());
    query.setRestriction(new ServerCodeRestriction(USATestData.DISTRICT.getServerObject(), "0001"));

    Assert.assertNull(query.getSingleResult());
  }

  @Test
  @Request
  public void testImportExcelWithTerm() throws Throwable
  {
    Term term = this.termService.createTerm(testTerm.getRootTerm().getCode(), new Term("Test Term", new LocalizedValue("Test Term"), new LocalizedValue("")));

    try
    {
      TestDataSet.refreshTerms(testTerm);

      InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

      Assert.assertNotNull(istream);

      JSONObject json = this.getTestConfiguration(istream, testTerm, ImportStrategy.NEW_AND_UPDATE);

      ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

      GeoObjectImportConfiguration config = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
      config.setHierarchy(hierarchyType);

      ImportHistory hist = mockImport(config);
      Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

      hist = ImportHistory.get(hist.getOid());
      Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getWorkTotal());
      Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getWorkProgress());
      Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getImportedRecords());
      Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

      ServerGeoObjectIF object = this.objectService.getGeoObjectByCode("0001", USATestData.DISTRICT.getCode());

      Assert.assertEquals("0001", object.getCode());
    }
    finally
    {
      this.termService.deleteTerm(testTerm.getRootTerm(), term.getCode());

      TestDataSet.refreshTerms(testTerm);
    }
  }

  @Test
  @Request
  public void testImportExcelWithClassification() throws InterruptedException
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    JSONObject json = this.getTestConfiguration(istream, testClassification, ImportStrategy.NEW_AND_UPDATE);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration configuration = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    configuration.setHierarchy(hierarchyType);

    ImportHistory hist = importExcelFile(testData.clientRequest.getSessionId(), configuration.toJSON().toString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.SUCCESS);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    ServerGeoObjectIF object = this.objectService.getGeoObjectByCode("0001", USATestData.DISTRICT.getCode());

    Assert.assertEquals("0001", object.getCode());
  }

  @Test
  @Request
  public void testImportExcelWithBadTerm() throws Throwable
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    JSONObject json = this.getTestConfiguration(istream, testTerm, ImportStrategy.NEW_AND_UPDATE);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    config.setHierarchy(hierarchyType);

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.FEEDBACK));

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(ROW_COUNT), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(0), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.VALIDATION_RESOLVE, hist.getStage().get(0));

    JSONObject page = new JSONObject(this.etlService.getValidationProblems(testData.clientRequest.getSessionId(), hist.getOid(), false, 100, 1).toString());
    JSONArray results = page.getJSONArray("resultSet");
    Assert.assertEquals(1, results.length());

    // Assert the values of the problem
    JSONObject problem = results.getJSONObject(0);

    Assert.assertEquals("Test Term", problem.getString("label"));
    Assert.assertEquals(testTerm.getRootTerm().getCode(), problem.getString("parentCode"));
    Assert.assertEquals(testTerm.getName(), problem.getString("attributeCode"));
    Assert.assertEquals(testTerm.getLabel().getValue(), problem.getString("attributeLabel"));

    // Ensure the geo objects were not created
    ServerGeoObjectQuery query = this.objectService.createQuery(USATestData.DISTRICT.getServerObject(), config.getStartDate());
    query.setRestriction(new ServerCodeRestriction(USATestData.DISTRICT.getServerObject(), "0001"));

    Assert.assertNull(query.getSingleResult());
  }

  /**
   * In the case when the server fails mid import, when the server reboots it's
   * supposed to restart any jobs that were running. When we restart the job, we
   * want to make sure that it picks up from where it left off.
   */
  @Test
  @Request
  public void testResumeImport() throws InterruptedException
  {
    DataImportJob job = new DataImportJob();
    job.setRunAsUserId(testData.clientRequest.getSessionUser().getOid());
    job.apply();

    ImportHistory fakeImportHistory = new ImportHistory();
    fakeImportHistory.setStartTime(new Date());
    fakeImportHistory.addStatus(AllJobStatus.RUNNING);
    fakeImportHistory.addStage(ImportStage.IMPORT);
    fakeImportHistory.setWorkProgress(2L);
    fakeImportHistory.setCompletedRowsJson(Range.serialize(new TreeSet<>(Arrays.asList(new Range(1, 2)))));
    fakeImportHistory.setImportedRecords(0L);
    fakeImportHistory.apply();

    JobHistoryRecord record = new JobHistoryRecord(job, fakeImportHistory);
    record.apply();

    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet-resume.xlsx");

    Assert.assertNotNull(istream);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = (GeoObjectImportConfiguration) ImportConfiguration.build(this.getTestConfiguration(istream, null, ImportStrategy.NEW_ONLY).toString(), true);
    config.setHierarchy(hierarchyType);
    config.setHistoryId(fakeImportHistory.getOid());
    config.setJobId(job.getOid());

    fakeImportHistory.appLock();
    fakeImportHistory.setConfigJson(config.toJSON().toString());
    fakeImportHistory.setImportFileId(config.getVaultFileId());
    fakeImportHistory.apply();

    ImportHistory hist = importExcelFile(testData.clientRequest.getSessionId(), config.toJSON().toString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.SUCCESS);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(Long.valueOf(10), hist.getWorkTotal());
    Assert.assertEquals(Long.valueOf(10), hist.getWorkProgress());
    Assert.assertEquals(Long.valueOf(8), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    for (int i = 1; i < 3; ++i)
    {
      try
      {
        this.objectService.getGeoObjectByCode("000" + i, USATestData.DISTRICT.getCode());

        Assert.fail("Was able to fectch GeoObject with code [000" + i + "], which should not have been imported.");
      }
      catch (DataNotFoundException ex)
      {
        // Expected
      }
    }

    for (int i = 3; i < 11; ++i)
    {
      ServerGeoObjectIF object = this.objectService.getGeoObjectByCode("000" + i, USATestData.DISTRICT.getCode());

      Assert.assertNotNull(object);
      Assert.assertEquals("Test", object.getDisplayLabel().getValue());

      Geometry geometry = object.getGeometry(TestDataSet.DEFAULT_OVER_TIME_DATE);

      Assert.assertNotNull(geometry);
    }

    JSONObject json = new JSONObject(this.etlService.getImportErrors(testData.clientRequest.getSessionId(), hist.getOid(), false, 100, 1).toString());

    Assert.assertEquals(0, json.getJSONArray("resultSet").length());
  }

  private JSONObject getTestConfiguration(InputStream istream, AttributeType attributeTerm, ImportStrategy strategy)
  {
    JSONObject result = this.excelService.getExcelConfiguration(testData.clientRequest.getSessionId(), USATestData.DISTRICT.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, "test-spreadsheet.xlsx", istream, strategy, false);
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
    result.put(ImportConfiguration.IMPORT_STRATEGY, strategy);

    return result;
  }

  protected List<ServerGeoObjectIF> getObjects(ServerGeoObjectType type, Date date)
  {
    // Ensure the geo objects were not created
    StringBuilder statement = new StringBuilder();
    statement.append("TRAVERSE out('" + EdgeConstant.HAS_VALUE.getDBClassName() + "', '" + EdgeConstant.HAS_GEOMETRY.getDBClassName() + "') FROM (");
    statement.append(" SELECT FROM " + type.getDBClassName());
    statement.append(" WHERE out('" + EdgeConstant.HAS_VALUE.getDBClassName() + "')[attributeName = 'exists' AND value = true AND :date BETWEEN startDate AND endDate].size() > 0");
    statement.append(")");

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());
    query.setParameter("date", date);

    return VertexServerGeoObject.processTraverseResults(query.getResults(), date);
  }
}
