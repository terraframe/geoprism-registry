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

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.geotools.geometry.jts.JTSFactoryFinder;
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
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.constants.graph.MdClassificationInfo;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdClassificationDAO;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.AbstractClassification;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutionContext;
import com.runwaysdk.system.scheduler.JobHistoryRecord;
import com.runwaysdk.system.scheduler.MockScheduler;
import com.runwaysdk.system.scheduler.SchedulerManager;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

import net.geoprism.data.importer.BasicColumnFunction;
import net.geoprism.data.importer.FeatureRow;
import net.geoprism.data.importer.ShapefileFunction;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.etl.FormatSpecificImporterFactory.FormatImporterType;
import net.geoprism.registry.etl.ObjectImporterFactory.ObjectImportType;
import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.excel.GeoObjectExcelExporter;
import net.geoprism.registry.geoobject.ServerGeoObjectService;
import net.geoprism.registry.io.DelegateShapefileFunction;
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
import net.geoprism.registry.service.ExcelService;
import net.geoprism.registry.service.RegistryService;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.test.SchedulerTestUtils;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.USATestData;

public class ExcelServiceTest
{
  protected static String                    CLASSIFICATION_TYPE = "test.classification.TestClassification";

  protected static String                    CODE                = "Test Term";

  protected static USATestData               testData;

  private static AttributeTermType           testTerm;

  private static AttributeIntegerType        testInteger;

  private static AttributeDateType           testDate;

  private static AttributeBooleanType        testBoolean;

  private static AttributeClassificationType testClassification;

  private final Integer                      ROW_COUNT           = 2;

  @BeforeClass
  @Request
  public static void classSetUp()
  {
    MdClassificationDAO mdClassification = MdClassificationDAO.newInstance();
    mdClassification.setValue(MdClassificationInfo.PACKAGE, "test.classification");
    mdClassification.setValue(MdClassificationInfo.TYPE_NAME, "TestClassification");
    mdClassification.setStructValue(MdClassificationInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, "Test Classification");
    mdClassification.setValue(MdClassificationInfo.GENERATE_SOURCE, MdAttributeBooleanInfo.FALSE);
    mdClassification.apply();

    MdVertexDAOIF referenceMdVertexDAO = mdClassification.getReferenceMdVertexDAO();

    VertexObject root = new VertexObject(referenceMdVertexDAO.definesType());
    root.setValue(AbstractClassification.CODE, CODE);
    root.setEmbeddedValue(AbstractClassification.DISPLAYLABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, "Test Classification");
    root.apply();

    mdClassification.setValue(MdClassificationInfo.ROOT, root.getOid());
    mdClassification.apply();

    TestDataSet.deleteAllSchedulerData();

    testData = USATestData.newTestData();
    testData.setUpMetadata();

    testTerm = (AttributeTermType) TestDataSet.createTermAttribute("testTerm", "testTermLocalName", USATestData.DISTRICT, null).fetchDTO();
    testBoolean = (AttributeBooleanType) TestDataSet.createAttribute("testBoolean", "testBooleanLocalName", USATestData.DISTRICT, AttributeBooleanType.TYPE).fetchDTO();
    testDate = (AttributeDateType) TestDataSet.createAttribute("testDate", "testDateLocalName", USATestData.DISTRICT, AttributeDateType.TYPE).fetchDTO();
    testInteger = (AttributeIntegerType) TestDataSet.createAttribute("testInteger", "testIntegerLocalName", USATestData.DISTRICT, AttributeIntegerType.TYPE).fetchDTO();

    if (!SchedulerManager.initialized())
    {
      SchedulerManager.start();
    }

    testClassification = (AttributeClassificationType) AttributeType.factory("testClassification", new LocalizedValue("testClassificationLocalName"), new LocalizedValue("testClassificationLocalDescrip"), AttributeClassificationType.TYPE, false, false, false);
    testClassification.setClassificationType(CLASSIFICATION_TYPE);
    testClassification.setRootTerm(new Term(CODE, new LocalizedValue("Test Classification"), new LocalizedValue("Test Classification")));

    ServerGeoObjectType got = ServerGeoObjectType.get(USATestData.DISTRICT.getCode());
    testClassification = (AttributeClassificationType) got.createAttributeType(testClassification.toJSON().toString());
  }

  @AfterClass
  @Request
  public static void classTearDown()
  {
    testData.tearDownMetadata();

    try
    {
      MdClassificationDAO.getMdClassificationDAO(CLASSIFICATION_TYPE).getBusinessDAO().delete();
    }
    catch (Exception e)
    {
      // skip
    }
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

    ExcelService service = new ExcelService();
    JSONObject result = service.getExcelConfiguration(testData.clientRequest.getSessionId(), USATestData.DISTRICT.getCode(), null, null, "test-spreadsheet.xlsx", istream, ImportStrategy.NEW_AND_UPDATE, false);

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

    ExcelService service = new ExcelService();
    JSONObject result = service.getExcelConfiguration(testData.clientRequest.getSessionId(), USATestData.DISTRICT.getCode(), null, null, "test-spreadsheet.xlsx", istream, ImportStrategy.NEW_AND_UPDATE, false);

    Assert.assertTrue(result.getBoolean(GeoObjectImportConfiguration.HAS_POSTAL_CODE));
  }

  private ImportHistory importExcelFile(String sessionId, String config) throws InterruptedException
  {
    String retConfig = new ETLService().doImport(sessionId, config).toString();

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
    hist.setOrganization(type.getOrganization());
    hist.setGeoObjectTypeCode(type.getCode());
    hist.apply();

    ExecutionContext context = MockScheduler.executeJob(job, hist);

    hist = (ImportHistory) context.getHistory();
    return hist;
  }

  @Test
  @Request
  public void testImportSpreadsheet() throws Throwable
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();

    JSONObject json = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_AND_UPDATE);
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    config.setHierarchy(hierarchyType);

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkTotal());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkProgress());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(testData.clientRequest.getSessionId(), "0001", USATestData.DISTRICT.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assert.assertNotNull(object);
    Assert.assertEquals("Test", object.getLocalizedDisplayLabel());

    Geometry geometry = object.getGeometry();

    Assert.assertNotNull(geometry);

    Double lat = new Double(2.232343);
    Double lon = new Double(1.134232);

    GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
    Point expected = new Point(new CoordinateSequence2D(lon, lat), factory);

    Assert.assertEquals(expected, geometry);
  }

  @Test
  @Request
  public void testImportSpreadsheetInteger() throws Throwable
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();

    JSONObject json = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_AND_UPDATE);
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    config.setFunction(testInteger.getName(), new BasicColumnFunction("Test Integer"));
    config.setHierarchy(hierarchyType);

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkTotal());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkProgress());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(testData.clientRequest.getSessionId(), "0001", USATestData.DISTRICT.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assert.assertNotNull(object);
    Assert.assertEquals(new Long(123), object.getValue(testInteger.getName()));
  }

  @Test
  @Request
  public void testImportSpreadsheetIntegerOverflow() throws Throwable
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet-integer-overflow.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();

    JSONObject json = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_AND_UPDATE);
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    config.setFunction(testInteger.getName(), new BasicColumnFunction("Test Integer"));
    config.setHierarchy(hierarchyType);

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.FEEDBACK));

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(1), hist.getWorkTotal());
    Assert.assertEquals(new Long(1), hist.getWorkProgress());
    Assert.assertEquals(new Long(0), hist.getImportedRecords());

    Assert.assertEquals(ImportStage.VALIDATION_RESOLVE, hist.getStage().get(0));

    // Ensure the geo objects were not created
    ServerGeoObjectQuery query = new ServerGeoObjectService().createQuery(USATestData.DISTRICT.getServerObject(), config.getStartDate());
    query.setRestriction(new ServerCodeRestriction("0001"));

    Assert.assertNull(query.getSingleResult());
  }

  @Test
  @Request
  public void testImportSpreadsheetBadInteger() throws Throwable
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet-bad-integer.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();

    JSONObject json = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_AND_UPDATE);
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    config.setFunction(testInteger.getName(), new BasicColumnFunction("Test Integer"));
    config.setHierarchy(hierarchyType);

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.FEEDBACK));

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(1), hist.getWorkTotal());
    Assert.assertEquals(new Long(1), hist.getWorkProgress());
    Assert.assertEquals(new Long(0), hist.getImportedRecords());

    Assert.assertEquals(ImportStage.VALIDATION_RESOLVE, hist.getStage().get(0));

    // Ensure the geo objects were not created
    ServerGeoObjectQuery query = new ServerGeoObjectService().createQuery(USATestData.DISTRICT.getServerObject(), config.getStartDate());
    query.setRestriction(new ServerCodeRestriction("0001"));

    Assert.assertNull(query.getSingleResult());
  }

  @Test
  @Request
  public void testImportSpreadsheetDate() throws Throwable
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();

    JSONObject json = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_AND_UPDATE);
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    config.setFunction(testDate.getName(), new BasicColumnFunction("Test Date"));
    config.setHierarchy(hierarchyType);

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkTotal());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkProgress());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(testData.clientRequest.getSessionId(), "0001", USATestData.DISTRICT.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

    Calendar calendar = Calendar.getInstance();
    calendar.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);
    calendar.clear();
    calendar.set(2018, Calendar.FEBRUARY, 12, 0, 0, 0);

    Assert.assertNotNull(object);
    Assert.assertEquals(calendar.getTime(), object.getValue(testDate.getName()));
  }

  @Test
  @Request
  public void testImportSpreadsheetBoolean() throws Throwable
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();

    JSONObject json = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_AND_UPDATE);
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    config.setFunction(testBoolean.getName(), new BasicColumnFunction("Test Boolean"));
    config.setHierarchy(hierarchyType);

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkTotal());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkProgress());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(testData.clientRequest.getSessionId(), "0001", USATestData.DISTRICT.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assert.assertNotNull(object);
    Assert.assertEquals(new Boolean(true), object.getValue(testBoolean.getName()));
  }

  @Test
  @Request
  public void testCreateWorkbook() throws Throwable
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();

    JSONObject json = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_AND_UPDATE);
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    config.setHierarchy(hierarchyType);

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkTotal());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkProgress());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    ServerGeoObjectType type = USATestData.DISTRICT.getServerObject();

    // Ensure the geo objects were not created
    ServerGeoObjectQuery query = new ServerGeoObjectService().createQuery(type, config.getStartDate());
    List<ServerGeoObjectIF> objects = query.getResults();

    GeoObjectExcelExporter exporter = new GeoObjectExcelExporter(type, hierarchyType, objects);
    Workbook workbook = exporter.createWorkbook();

    Assert.assertEquals(1, workbook.getNumberOfSheets());

    Sheet sheet = workbook.getSheetAt(0);

    Assert.assertEquals(WorkbookUtil.createSafeSheetName(type.getLabel().getValue()), sheet.getSheetName());
  }

  @Test
  @Request
  public void testExport() throws IOException
  {
    Term term = ServiceFactory.getRegistryService().createTerm(testData.clientRequest.getSessionId(), testTerm.getRootTerm().getCode(), new Term("Test Term", new LocalizedValue("Test Term"), new LocalizedValue("")).toJSON().toString());

    try
    {
      TestDataSet.refreshTerms(testTerm);

      Calendar calendar = Calendar.getInstance();
      calendar.clear();
      calendar.set(2018, Calendar.FEBRUARY, 12, 0, 0, 0);

      GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
      Point point = geometryFactory.createPoint(new Coordinate(-104.991531, 39.742043));

      GeoObject geoObj = ServiceFactory.getRegistryService().newGeoObjectInstance(testData.clientRequest.getSessionId(), USATestData.DISTRICT.getCode());
      geoObj.setCode("00");
      geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "Test Label");
      geoObj.setUid(ServiceFactory.getIdService().getUids(1)[0]);
      geoObj.setGeometry(point);
      geoObj.setValue(testTerm.getName(), term.getCode());
      geoObj.setValue(testInteger.getName(), 23L);
      geoObj.setValue(testDate.getName(), calendar.getTime());
      geoObj.setValue(testBoolean.getName(), true);

      ServerGeoObjectIF serverGO = new ServerGeoObjectService(new AllowAllGeoObjectPermissionService()).apply(geoObj, TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, true, false);
      geoObj = RegistryService.getInstance().getGeoObjectByCode(Session.getCurrentSession().getOid(), serverGO.getCode(), serverGO.getType().getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

      InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

      Assert.assertNotNull(istream);

      ServerGeoObjectType type = USATestData.DISTRICT.getServerObject();
      ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

      // Ensure the geo objects were not created
      ServerGeoObjectQuery query = new ServerGeoObjectService().createQuery(type, null);
      List<ServerGeoObjectIF> objects = query.getResults();

      GeoObjectExcelExporter exporter = new GeoObjectExcelExporter(type, hierarchyType, objects);
      InputStream export = exporter.export();

      Assert.assertNotNull(export);

      IOUtils.copy(export, NullOutputStream.NULL_OUTPUT_STREAM);
    }
    finally

    {
      ServiceFactory.getRegistryService().deleteTerm(testData.clientRequest.getSessionId(), testTerm.getRootTerm().getCode(), term.getCode());
    }
  }

  @Test
  @Request
  public void testImportExcelWithParent() throws Throwable
  {
    GeoObject geoObj = ServiceFactory.getRegistryService().newGeoObjectInstance(testData.clientRequest.getSessionId(), USATestData.STATE.getCode());
    geoObj.setCode("00");
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "Test Label");
    geoObj.setUid(ServiceFactory.getIdService().getUids(1)[0]);

    ServerGeoObjectIF serverGO = new ServerGeoObjectService(new AllowAllGeoObjectPermissionService()).apply(geoObj, TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, true, false);
    geoObj = RegistryService.getInstance().getGeoObjectByCode(Session.getCurrentSession().getOid(), serverGO.getCode(), serverGO.getType().getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();

    JSONObject json = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_AND_UPDATE);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    config.setStartDate(USATestData.DEFAULT_OVER_TIME_DATE);
    config.setEndDate(USATestData.DEFAULT_OVER_TIME_DATE);
    config.setHierarchy(hierarchyType);

    config.addParent(new Location(USATestData.STATE.getServerObject(), hierarchyType, new BasicColumnFunction("Parent"), ParentMatchStrategy.ALL));

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkTotal());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkProgress());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    String sessionId = testData.clientRequest.getSessionId();
    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(sessionId, "0001", USATestData.DISTRICT.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assert.assertEquals("0001", object.getCode());

    ParentTreeNode nodes = ServiceFactory.getRegistryService().getParentGeoObjects(sessionId, object.getUid(), config.getType().getCode(), new String[] { USATestData.STATE.getCode() }, false, TestDataSet.DEFAULT_OVER_TIME_DATE);

    List<ParentTreeNode> parents = nodes.getParents();

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

    GeoObject geoObj = ServiceFactory.getRegistryService().newGeoObjectInstance(testData.clientRequest.getSessionId(), USATestData.STATE.getCode());
    geoObj.setCode("00");
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "Test Label");
    geoObj.setUid(ServiceFactory.getIdService().getUids(1)[0]);

    ServerGeoObjectIF serverGO = new ServerGeoObjectService(new AllowAllGeoObjectPermissionService()).apply(geoObj, TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, true, false);
    geoObj = RegistryService.getInstance().getGeoObjectByCode(Session.getCurrentSession().getOid(), serverGO.getCode(), serverGO.getType().getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();

    JSONObject json = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_AND_UPDATE);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    config.setStartDate(USATestData.DEFAULT_OVER_TIME_DATE);
    config.setEndDate(USATestData.DEFAULT_END_TIME_DATE);
    config.setHierarchy(hierarchyType);

    config.addParent(new Location(USATestData.STATE.getServerObject(), hierarchyType, new BasicColumnFunction("Parent"), ParentMatchStrategy.ALL));

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkTotal());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkProgress());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    String sessionId = testData.clientRequest.getSessionId();
    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(sessionId, "0001", USATestData.DISTRICT.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assert.assertEquals("0001", object.getCode());

    ParentTreeNode nodes = ServiceFactory.getRegistryService().getParentGeoObjects(sessionId, object.getUid(), config.getType().getCode(), new String[] { USATestData.STATE.getCode() }, false, TestDataSet.DEFAULT_OVER_TIME_DATE);

    List<ParentTreeNode> parents = nodes.getParents();

    Assert.assertEquals(1, parents.size());
  }

  @Test
  @Request
  public void testImportExcelExcludeParent() throws Throwable
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();

    JSONObject json = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_AND_UPDATE);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    config.setHierarchy(hierarchyType);

    config.addParent(new Location(USATestData.COUNTRY.getServerObject(), hierarchyType, new BasicColumnFunction("Parent"), ParentMatchStrategy.ALL));
    config.addExclusion(GeoObjectImportConfiguration.PARENT_EXCLUSION, "00");

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkTotal());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkProgress());
    Assert.assertEquals(new Long(1), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    // Ensure the geo objects were not created
    ServerGeoObjectQuery query = new ServerGeoObjectService().createQuery(USATestData.DISTRICT.getServerObject(), config.getStartDate());
    query.setRestriction(new ServerCodeRestriction("0001"));

    Assert.assertNull(query.getSingleResult());
  }

  @Test
  @Request
  public void testImportExcelWithBadParent() throws Throwable
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();

    JSONObject json = this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_AND_UPDATE);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    config.setHierarchy(hierarchyType);

    config.addParent(new Location(USATestData.COUNTRY.getServerObject(), hierarchyType, new BasicColumnFunction("Parent"), ParentMatchStrategy.ALL));

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.FEEDBACK));

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkTotal());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkProgress());
    Assert.assertEquals(new Long(0), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.VALIDATION_RESOLVE, hist.getStage().get(0));

    JSONObject page = new JSONObject(new ETLService().getValidationProblems(testData.clientRequest.getSessionId(), hist.getOid(), false, 100, 1).toString());
    JSONArray results = page.getJSONArray("results");
    Assert.assertEquals(1, results.length());

    // Ensure the geo objects were not created
    ServerGeoObjectQuery query = new ServerGeoObjectService().createQuery(USATestData.DISTRICT.getServerObject(), config.getStartDate());
    query.setRestriction(new ServerCodeRestriction("0001"));

    Assert.assertNull(query.getSingleResult());
  }

  @Test
  @Request
  public void testImportExcelWithTerm() throws Throwable
  {
    Term term = ServiceFactory.getRegistryService().createTerm(testData.clientRequest.getSessionId(), testTerm.getRootTerm().getCode(), new Term("Test Term", new LocalizedValue("Test Term"), new LocalizedValue("")).toJSON().toString());

    try
    {
      TestDataSet.refreshTerms(testTerm);

      InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

      Assert.assertNotNull(istream);

      ExcelService service = new ExcelService();

      JSONObject json = this.getTestConfiguration(istream, service, testTerm, ImportStrategy.NEW_AND_UPDATE);

      ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

      GeoObjectImportConfiguration config = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
      config.setHierarchy(hierarchyType);

      ImportHistory hist = mockImport(config);
      Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

      hist = ImportHistory.get(hist.getOid());
      Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkTotal());
      Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkProgress());
      Assert.assertEquals(new Long(ROW_COUNT), hist.getImportedRecords());
      Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

      String sessionId = testData.clientRequest.getSessionId();
      GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(sessionId, "0001", USATestData.DISTRICT.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

      Assert.assertEquals("0001", object.getCode());
    }
    finally
    {
      ServiceFactory.getRegistryService().deleteTerm(testData.clientRequest.getSessionId(), testTerm.getRootTerm().getCode(), term.getCode());

      TestDataSet.refreshTerms(testTerm);
    }
  }

  @Test
  @Request
  public void testImportExcelWithClassification() throws InterruptedException
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();

    JSONObject json = this.getTestConfiguration(istream, service, testClassification, ImportStrategy.NEW_AND_UPDATE);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration configuration = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    configuration.setHierarchy(hierarchyType);

    ImportHistory hist = importExcelFile(testData.clientRequest.getSessionId(), configuration.toJSON().toString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.SUCCESS);

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkTotal());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkProgress());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    String sessionId = testData.clientRequest.getSessionId();
    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(sessionId, "0001", USATestData.DISTRICT.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assert.assertEquals("0001", object.getCode());
  }

  @Test
  @Request
  public void testImportExcelWithBadTerm() throws Throwable
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();

    JSONObject json = this.getTestConfiguration(istream, service, testTerm, ImportStrategy.NEW_AND_UPDATE);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    config.setHierarchy(hierarchyType);

    ImportHistory hist = mockImport(config);
    Assert.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.FEEDBACK));

    hist = ImportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkTotal());
    Assert.assertEquals(new Long(ROW_COUNT), hist.getWorkProgress());
    Assert.assertEquals(new Long(0), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.VALIDATION_RESOLVE, hist.getStage().get(0));

    JSONObject page = new JSONObject(new ETLService().getValidationProblems(testData.clientRequest.getSessionId(), hist.getOid(), false, 100, 1).toString());
    JSONArray results = page.getJSONArray("results");
    Assert.assertEquals(1, results.length());

    // Assert the values of the problem
    JSONObject problem = results.getJSONObject(0);

    Assert.assertEquals("Test Term", problem.getString("label"));
    Assert.assertEquals(testTerm.getRootTerm().getCode(), problem.getString("parentCode"));
    Assert.assertEquals(testTerm.getName(), problem.getString("attributeCode"));
    Assert.assertEquals(testTerm.getLabel().getValue(), problem.getString("attributeLabel"));

    // Ensure the geo objects were not created
    ServerGeoObjectQuery query = new ServerGeoObjectService().createQuery(USATestData.DISTRICT.getServerObject(), config.getStartDate());
    query.setRestriction(new ServerCodeRestriction("0001"));

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
    fakeImportHistory.setImportedRecords(0L);
    fakeImportHistory.apply();

    JobHistoryRecord record = new JobHistoryRecord(job, fakeImportHistory);
    record.apply();

    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet-resume.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    GeoObjectImportConfiguration config = (GeoObjectImportConfiguration) ImportConfiguration.build(this.getTestConfiguration(istream, service, null, ImportStrategy.NEW_ONLY).toString(), true);
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
    Assert.assertEquals(new Long(10), hist.getWorkTotal());
    Assert.assertEquals(new Long(10), hist.getWorkProgress());
    Assert.assertEquals(new Long(8), hist.getImportedRecords());
    Assert.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

    for (int i = 1; i < 3; ++i)
    {
      try
      {
        ServiceFactory.getRegistryService().getGeoObjectByCode(testData.clientRequest.getSessionId(), "000" + i, USATestData.DISTRICT.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

        Assert.fail("Was able to fectch GeoObject with code [000" + i + "], which should not have been imported.");
      }
      catch (SmartExceptionDTO ex)
      {
        // Expected
      }
    }

    for (int i = 3; i < 11; ++i)
    {
      GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(testData.clientRequest.getSessionId(), "000" + i, USATestData.DISTRICT.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

      Assert.assertNotNull(object);
      Assert.assertEquals("Test", object.getLocalizedDisplayLabel());

      Geometry geometry = object.getGeometry();

      Assert.assertNotNull(geometry);
    }

    JSONObject json = new JSONObject(new ETLService().getImportErrors(testData.clientRequest.getSessionId(), hist.getOid(), false, 100, 1).toString());

    Assert.assertEquals(0, json.getJSONArray("results").length());
  }

  private JSONObject getTestConfiguration(InputStream istream, ExcelService service, AttributeType attributeTerm, ImportStrategy strategy)
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
    result.put(ImportConfiguration.IMPORT_STRATEGY, strategy);

    return result;
  }
}
