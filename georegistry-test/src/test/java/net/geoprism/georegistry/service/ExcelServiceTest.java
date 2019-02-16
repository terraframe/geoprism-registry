package net.geoprism.georegistry.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.SessionFacade;
import com.runwaysdk.system.gis.geo.LocatedIn;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdTermRelationship;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import net.geoprism.data.importer.BasicColumnFunction;
import net.geoprism.data.importer.FeatureRow;
import net.geoprism.data.importer.ShapefileFunction;
import net.geoprism.georegistry.excel.GeoObjectExcelExporter;
import net.geoprism.georegistry.io.DelegateShapefileFunction;
import net.geoprism.georegistry.io.GeoObjectConfiguration;
import net.geoprism.georegistry.io.Location;
import net.geoprism.georegistry.io.LocationBuilder;
import net.geoprism.georegistry.io.PostalCodeFactory;
import net.geoprism.georegistry.query.CodeRestriction;
import net.geoprism.georegistry.query.GeoObjectIterator;
import net.geoprism.georegistry.query.GeoObjectQuery;
import net.geoprism.registry.test.USATestData;

public class ExcelServiceTest
{
  protected USATestData        testData;

  protected ClientRequestIF    adminCR;

  private AttributeTermType    testTerm;

  private AttributeIntegerType testInteger;

  private AttributeDateType    testDate;

  private AttributeBooleanType testBoolean;

  @Before
  public void setUp()
  {
    this.testData = USATestData.newTestData(GeometryType.POINT, false);

    this.adminCR = testData.adminClientRequest;

    AttributeTermType testTerm = (AttributeTermType) AttributeType.factory("testTerm", "testTermLocalName", "testTermLocalDescrip", AttributeTermType.TYPE);
    this.testTerm = (AttributeTermType) ServiceFactory.getRegistryService().createAttributeType(this.adminCR.getSessionId(), this.testData.STATE.getCode(), testTerm.toJSON().toString());

    AttributeBooleanType testBoolean = (AttributeBooleanType) AttributeType.factory("testBoolean", "testBooleanLocalName", "testBooleanLocalDescrip", AttributeBooleanType.TYPE);
    this.testBoolean = (AttributeBooleanType) ServiceFactory.getRegistryService().createAttributeType(this.adminCR.getSessionId(), this.testData.STATE.getCode(), testBoolean.toJSON().toString());

    AttributeDateType testDate = (AttributeDateType) AttributeType.factory("testDate", "testDateLocalName", "testDateLocalDescrip", AttributeDateType.TYPE);
    this.testDate = (AttributeDateType) ServiceFactory.getRegistryService().createAttributeType(this.adminCR.getSessionId(), this.testData.STATE.getCode(), testDate.toJSON().toString());

    AttributeIntegerType testInteger = (AttributeIntegerType) AttributeType.factory("testInteger", "testIntegerLocalName", "testIntegerLocalDescrip", AttributeIntegerType.TYPE);
    this.testInteger = (AttributeIntegerType) ServiceFactory.getRegistryService().createAttributeType(this.adminCR.getSessionId(), this.testData.STATE.getCode(), testInteger.toJSON().toString());

    reload();
  }

  @Request
  public void reload()
  {
    /*
     * Reload permissions for the new attributes
     */
    SessionFacade.getSessionForRequest(this.adminCR.getSessionId()).reloadPermissions();
  }

  @After
  public void tearDown()
  {
    testData.cleanUp();
  }

  @Test
  public void testGetAttributeInformation()
  {
    PostalCodeFactory.remove(testData.STATE.getGeoObjectType(GeometryType.MULTIPOLYGON));

    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();
    JsonObject result = service.getExcelConfiguration(this.adminCR.getSessionId(), testData.STATE.getCode(), "test-spreadsheet.xlsx", istream);

    Assert.assertFalse(result.get(GeoObjectConfiguration.HAS_POSTAL_CODE).getAsBoolean());

    Assert.assertNotNull(result.getAsJsonObject(GeoObjectConfiguration.TYPE));

    JsonArray hierarchies = result.get(GeoObjectConfiguration.HIERARCHIES).getAsJsonArray();

    Assert.assertEquals(1, hierarchies.size());

    JsonObject hierarchy = hierarchies.get(0).getAsJsonObject();

    Assert.assertNotNull(hierarchy.get("label").getAsString());

    JsonObject sheet = result.getAsJsonObject(GeoObjectConfiguration.SHEET);

    Assert.assertNotNull(sheet);
    Assert.assertEquals("Objects", sheet.get("name").getAsString());

    JsonObject attributes = sheet.get(GeoObjectType.JSON_ATTRIBUTES).getAsJsonObject();

    Assert.assertNotNull(attributes);

    JsonArray fields = attributes.get(GeoObjectConfiguration.TEXT).getAsJsonArray();

    Assert.assertEquals(4, fields.size());
    Assert.assertEquals("Name", fields.get(0).getAsString());

    Assert.assertEquals(4, attributes.get(GeoObjectConfiguration.NUMERIC).getAsJsonArray().size());
    Assert.assertEquals(1, attributes.get(AttributeBooleanType.TYPE).getAsJsonArray().size());
    Assert.assertEquals(1, attributes.get(AttributeDateType.TYPE).getAsJsonArray().size());
  }

  @Test
  public void testGetAttributeInformationPostalCode()
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    GeoObjectType type = testData.STATE.getGeoObjectType(GeometryType.MULTIPOLYGON);

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
    JsonObject result = service.getExcelConfiguration(this.adminCR.getSessionId(), testData.STATE.getCode(), "test-spreadsheet.xlsx", istream);

    Assert.assertTrue(result.get(GeoObjectConfiguration.HAS_POSTAL_CODE).getAsBoolean());
  }

  @Test
  @Request
  public void testImportSpreadsheet()
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();

    JsonObject json = this.getTestConfiguration(istream, service, null);

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), true);

    service.importExcelFile(this.adminCR.getSessionId(), configuration.toJson().toString());

    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(this.testData.adminClientRequest.getSessionId(), "0001", testData.STATE.getCode());

    Assert.assertNotNull(object);
    Assert.assertNotNull(object.getGeometry());
    Assert.assertEquals("Test", object.getValue(GeoObject.LOCALIZED_DISPLAY_LABEL));
  }

  @Test
  @Request
  public void testImportSpreadsheetInteger()
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();

    JsonObject json = this.getTestConfiguration(istream, service, null);

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), true);
    configuration.setFunction(this.testInteger.getName(), new BasicColumnFunction("Test Integer"));

    service.importExcelFile(this.adminCR.getSessionId(), configuration.toJson().toString());

    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(this.testData.adminClientRequest.getSessionId(), "0001", testData.STATE.getCode());

    Assert.assertNotNull(object);
    Assert.assertEquals(new Long(123), object.getValue(this.testInteger.getName()));
  }

  @Test
  @Request
  public void testImportSpreadsheetDate()
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();

    JsonObject json = this.getTestConfiguration(istream, service, null);

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), true);
    configuration.setFunction(this.testDate.getName(), new BasicColumnFunction("Test Date"));

    service.importExcelFile(this.adminCR.getSessionId(), configuration.toJson().toString());

    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(this.testData.adminClientRequest.getSessionId(), "0001", testData.STATE.getCode());

    Calendar calendar = Calendar.getInstance();
    calendar.clear();
    calendar.set(2018, Calendar.FEBRUARY, 12, 0, 0, 0);

    Assert.assertNotNull(object);
    Assert.assertEquals(calendar.getTime(), object.getValue(this.testDate.getName()));
  }

  @Test
  @Request
  public void testImportSpreadsheetBoolean()
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();

    JsonObject json = this.getTestConfiguration(istream, service, null);

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), true);
    configuration.setFunction(this.testBoolean.getName(), new BasicColumnFunction("Test Boolean"));

    service.importExcelFile(this.adminCR.getSessionId(), configuration.toJson().toString());

    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(this.testData.adminClientRequest.getSessionId(), "0001", testData.STATE.getCode());

    Assert.assertNotNull(object);
    Assert.assertEquals(new Boolean(true), object.getValue(this.testBoolean.getName()));
  }

  @Test
  @Request
  public void testCreateWorkbook() throws IOException
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();

    JsonObject json = this.getTestConfiguration(istream, service, null);

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), true);

    service.importExcelFile(this.adminCR.getSessionId(), configuration.toJson().toString());

    GeoObjectType type = testData.STATE.getGeoObjectType(GeometryType.POINT);
    HierarchyType hierarchyType = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(LocatedIn.class.getSimpleName()).get();

    GeoObjectIterator objects = new GeoObjectQuery(type, testData.STATE.getUniversal()).getIterator();

    try
    {
      GeoObjectExcelExporter exporter = new GeoObjectExcelExporter(type, hierarchyType, objects);
      Workbook workbook = exporter.createWorkbook();

      Assert.assertEquals(1, workbook.getNumberOfSheets());

      Sheet sheet = workbook.getSheetAt(0);

      Assert.assertEquals(WorkbookUtil.createSafeSheetName(type.getLocalizedLabel()), sheet.getSheetName());
    }
    finally
    {
      objects.close();
    }
  }

  @Test
  @Request
  public void testExport() throws IOException
  {
    Term term = ServiceFactory.getRegistryService().createTerm(this.adminCR.getSessionId(), testTerm.getRootTerm().getCode(), new Term("Test Term", "Test Term", "").toJSON().toString());

    try
    {
      this.testData.refreshTerms(testTerm);

      Calendar calendar = Calendar.getInstance();
      calendar.clear();
      calendar.set(2018, Calendar.FEBRUARY, 12, 0, 0, 0);

      GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
      Point point = geometryFactory.createPoint(new Coordinate(-104.991531, 39.742043));

      GeoObject geoObj = ServiceFactory.getRegistryService().newGeoObjectInstance(this.testData.adminClientRequest.getSessionId(), this.testData.STATE.getCode());
      geoObj.setCode("00");
      geoObj.setLocalizedDisplayLabel("Test Label");
      geoObj.setUid(ServiceFactory.getIdService().getUids(1)[0]);
      geoObj.setGeometry(point);
      geoObj.setValue(this.testTerm.getName(), term.getCode());
      geoObj.setValue(this.testInteger.getName(), 23L);
      geoObj.setValue(this.testDate.getName(), calendar.getTime());
      geoObj.setValue(this.testBoolean.getName(), true);

      geoObj = ServiceFactory.getUtilities().applyGeoObject(geoObj, true);

      InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

      Assert.assertNotNull(istream);

      GeoObjectType type = testData.STATE.getGeoObjectType(GeometryType.POINT);
      HierarchyType hierarchyType = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(LocatedIn.class.getSimpleName()).get();

      GeoObjectIterator objects = new GeoObjectQuery(type, testData.STATE.getUniversal()).getIterator();

      try
      {
        GeoObjectExcelExporter exporter = new GeoObjectExcelExporter(type, hierarchyType, objects);
        InputStream export = exporter.export();

        Assert.assertNotNull(export);

        IOUtils.copy(export, new NullOutputStream());
      }
      finally
      {
        objects.close();
      }
    }
    finally
    {
      ServiceFactory.getRegistryService().deleteTerm(this.adminCR.getSessionId(), term.getCode());
    }
  }

  @Test
  @Request
  public void testImportExcelWithParent()
  {
    GeoObject geoObj = ServiceFactory.getRegistryService().newGeoObjectInstance(this.testData.adminClientRequest.getSessionId(), this.testData.COUNTRY.getCode());
    geoObj.setCode("00");
    geoObj.setLocalizedDisplayLabel("Test Label");
    geoObj.setUid(ServiceFactory.getIdService().getUids(1)[0]);

    ServiceFactory.getUtilities().applyGeoObject(geoObj, true);

    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();

    JsonObject json = this.getTestConfiguration(istream, service, null);

    HierarchyType hierarchyType = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(LocatedIn.class.getSimpleName()).get();
    MdTermRelationship mdRelationship = ServiceFactory.getConversionService().existingHierarchyToGeoEntityMdTermRelationiship(hierarchyType);

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), true);
    configuration.setHierarchy(hierarchyType);
    configuration.setHierarchyRelationship(mdRelationship);
    configuration.addParent(new Location(this.testData.COUNTRY.getGeoObjectType(GeometryType.POLYGON), this.testData.COUNTRY.getUniversal(), new BasicColumnFunction("Parent")));

    service.importExcelFile(this.adminCR.getSessionId(), configuration.toJson().toString());

    String sessionId = this.testData.adminClientRequest.getSessionId();
    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(sessionId, "0001", testData.STATE.getCode());

    Assert.assertEquals("0001", object.getCode());

    ParentTreeNode nodes = ServiceFactory.getRegistryService().getParentGeoObjects(sessionId, object.getUid(), configuration.getType().getCode(), new String[] { this.testData.COUNTRY.getCode() }, false);

    List<ParentTreeNode> parents = nodes.getParents();

    Assert.assertEquals(1, parents.size());
  }

  @Test
  @Request
  public void testImportExcelWithPostalCode()
  {
    GeoObjectType type = testData.STATE.getGeoObjectType(GeometryType.POINT);

    PostalCodeFactory.addPostalCode(type, new LocationBuilder()
    {
      @Override
      public Location build(ShapefileFunction function)
      {
        GeoObjectType type = testData.COUNTRY.getGeoObjectType(GeometryType.POINT);
        Universal universal = ServiceFactory.getConversionService().geoObjectTypeToUniversal(type);

        DelegateShapefileFunction delegate = new DelegateShapefileFunction(function)
        {
          @Override
          public Object getValue(FeatureRow feature)
          {
            String code = (String) super.getValue(feature);

            return code.substring(0, 2);
          }
        };

        return new Location(type, universal, delegate);
      }
    });

    GeoObject geoObj = ServiceFactory.getRegistryService().newGeoObjectInstance(this.testData.adminClientRequest.getSessionId(), this.testData.COUNTRY.getCode());
    geoObj.setCode("00");
    geoObj.setLocalizedDisplayLabel("Test Label");
    geoObj.setUid(ServiceFactory.getIdService().getUids(1)[0]);

    ServiceFactory.getUtilities().applyGeoObject(geoObj, true);

    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();

    JsonObject json = this.getTestConfiguration(istream, service, null);

    HierarchyType hierarchyType = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(LocatedIn.class.getSimpleName()).get();
    MdTermRelationship mdRelationship = ServiceFactory.getConversionService().existingHierarchyToGeoEntityMdTermRelationiship(hierarchyType);

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), true);
    configuration.setHierarchy(hierarchyType);
    configuration.setHierarchyRelationship(mdRelationship);
    configuration.addParent(new Location(this.testData.COUNTRY.getGeoObjectType(GeometryType.POLYGON), this.testData.COUNTRY.getUniversal(), new BasicColumnFunction("Parent")));

    service.importExcelFile(this.adminCR.getSessionId(), configuration.toJson().toString());

    String sessionId = this.testData.adminClientRequest.getSessionId();
    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(sessionId, "0001", testData.STATE.getCode());

    Assert.assertEquals("0001", object.getCode());

    ParentTreeNode nodes = ServiceFactory.getRegistryService().getParentGeoObjects(sessionId, object.getUid(), configuration.getType().getCode(), new String[] { this.testData.COUNTRY.getCode() }, false);

    List<ParentTreeNode> parents = nodes.getParents();

    Assert.assertEquals(1, parents.size());
  }

  @Test
  @Request
  public void testImportExcelExcludeParent()
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();

    JsonObject json = this.getTestConfiguration(istream, service, null);

    HierarchyType hierarchyType = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(LocatedIn.class.getSimpleName()).get();
    MdTermRelationship mdRelationship = ServiceFactory.getConversionService().existingHierarchyToGeoEntityMdTermRelationiship(hierarchyType);

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), true);
    configuration.setHierarchy(hierarchyType);
    configuration.setHierarchyRelationship(mdRelationship);
    configuration.addParent(new Location(this.testData.COUNTRY.getGeoObjectType(GeometryType.POLYGON), this.testData.COUNTRY.getUniversal(), new BasicColumnFunction("Parent")));
    configuration.addExclusion(GeoObjectConfiguration.PARENT_EXCLUSION, "00");

    JsonObject result = service.importExcelFile(this.adminCR.getSessionId(), configuration.toJson().toString());

    Assert.assertFalse(result.has(GeoObjectConfiguration.LOCATION_PROBLEMS));

    // Ensure the geo objects were not created
    GeoObjectQuery query = new GeoObjectQuery(testData.STATE.getGeoObjectType(GeometryType.POLYGON), testData.STATE.getUniversal());
    query.setRestriction(new CodeRestriction("0001"));

    Assert.assertNull(query.getSingleResult());
  }

  @Test
  @Request
  public void testImportExcelWithBadParent()
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();

    JsonObject json = this.getTestConfiguration(istream, service, null);

    HierarchyType hierarchyType = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(LocatedIn.class.getSimpleName()).get();
    MdTermRelationship mdRelationship = ServiceFactory.getConversionService().existingHierarchyToGeoEntityMdTermRelationiship(hierarchyType);

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), true);
    configuration.setHierarchy(hierarchyType);
    configuration.setHierarchyRelationship(mdRelationship);
    configuration.addParent(new Location(this.testData.COUNTRY.getGeoObjectType(GeometryType.POLYGON), this.testData.COUNTRY.getUniversal(), new BasicColumnFunction("Parent")));

    JsonObject result = service.importExcelFile(this.adminCR.getSessionId(), configuration.toJson().toString());

    Assert.assertTrue(result.has(GeoObjectConfiguration.LOCATION_PROBLEMS));

    JsonArray problems = result.get(GeoObjectConfiguration.LOCATION_PROBLEMS).getAsJsonArray();

    Assert.assertEquals(1, problems.size());

    // Ensure the geo objects were not created
    GeoObjectQuery query = new GeoObjectQuery(testData.STATE.getGeoObjectType(GeometryType.POLYGON), testData.STATE.getUniversal());
    query.setRestriction(new CodeRestriction("0001"));

    Assert.assertNull(query.getSingleResult());
  }

  @Test
  @Request
  public void testImportExcelWithTerm()
  {
    Term term = ServiceFactory.getRegistryService().createTerm(this.adminCR.getSessionId(), testTerm.getRootTerm().getCode(), new Term("Test Term", "Test Term", "").toJSON().toString());

    try
    {
      this.testData.refreshTerms(testTerm);

      InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

      Assert.assertNotNull(istream);

      ExcelService service = new ExcelService();

      JsonObject json = this.getTestConfiguration(istream, service, testTerm);

      HierarchyType hierarchyType = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(LocatedIn.class.getSimpleName()).get();
      MdTermRelationship mdRelationship = ServiceFactory.getConversionService().existingHierarchyToGeoEntityMdTermRelationiship(hierarchyType);

      GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), true);
      configuration.setHierarchy(hierarchyType);
      configuration.setHierarchyRelationship(mdRelationship);

      service.importExcelFile(this.adminCR.getSessionId(), configuration.toJson().toString());

      String sessionId = this.testData.adminClientRequest.getSessionId();
      GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(sessionId, "0001", testData.STATE.getCode());

      Assert.assertEquals("0001", object.getCode());
    }
    finally
    {
      ServiceFactory.getRegistryService().deleteTerm(this.adminCR.getSessionId(), term.getCode());

      this.testData.refreshTerms(testTerm);
    }
  }

  @Test
  @Request
  public void testImportExcelWithBadTerm()
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();

    JsonObject json = this.getTestConfiguration(istream, service, testTerm);

    HierarchyType hierarchyType = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(LocatedIn.class.getSimpleName()).get();
    MdTermRelationship mdRelationship = ServiceFactory.getConversionService().existingHierarchyToGeoEntityMdTermRelationiship(hierarchyType);

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), true);
    configuration.setHierarchy(hierarchyType);
    configuration.setHierarchyRelationship(mdRelationship);

    JsonObject result = service.importExcelFile(this.adminCR.getSessionId(), configuration.toJson().toString());

    Assert.assertTrue(result.has(GeoObjectConfiguration.TERM_PROBLEMS));

    JsonArray problems = result.get(GeoObjectConfiguration.TERM_PROBLEMS).getAsJsonArray();

    Assert.assertEquals(1, problems.size());

    // Assert the values of the problem
    JsonObject problem = problems.get(0).getAsJsonObject();

    Assert.assertEquals("Test Term", problem.get("label").getAsString());
    Assert.assertEquals(this.testTerm.getRootTerm().getCode(), problem.get("parentCode").getAsString());
    Assert.assertEquals(this.testTerm.getName(), problem.get("attributeCode").getAsString());
    Assert.assertEquals(this.testTerm.getLocalizedLabel(), problem.get("attributeLabel").getAsString());

    // Ensure the geo objects were not created
    GeoObjectQuery query = new GeoObjectQuery(testData.STATE.getGeoObjectType(GeometryType.POINT), testData.STATE.getUniversal());
    query.setRestriction(new CodeRestriction("0001"));

    Assert.assertNull(query.getSingleResult());
  }

  private JsonObject getTestConfiguration(InputStream istream, ExcelService service, AttributeTermType attributeTerm)
  {
    JsonObject result = service.getExcelConfiguration(this.adminCR.getSessionId(), testData.STATE.getCode(), "test-spreadsheet.xlsx", istream);
    JsonObject type = result.getAsJsonObject(GeoObjectConfiguration.TYPE);
    JsonArray attributes = type.get(GeoObjectType.JSON_ATTRIBUTES).getAsJsonArray();

    for (int i = 0; i < attributes.size(); i++)
    {
      JsonObject attribute = attributes.get(i).getAsJsonObject();

      String attributeName = attribute.get(AttributeType.JSON_CODE).getAsString();

      if (attributeName.equals(GeoObject.LOCALIZED_DISPLAY_LABEL))
      {
        attribute.addProperty(GeoObjectConfiguration.TARGET, "Name");
      }
      else if (attributeName.equals(GeoObject.CODE))
      {
        attribute.addProperty(GeoObjectConfiguration.TARGET, "Code");
      }
      else if (attributeName.equals(GeoObjectConfiguration.LATITUDE))
      {
        attribute.addProperty(GeoObjectConfiguration.TARGET, "Latitude");
      }
      else if (attributeName.equals(GeoObjectConfiguration.LONGITUDE))
      {
        attribute.addProperty(GeoObjectConfiguration.TARGET, "Longitude");
      }
      else if (attributeTerm != null && attributeName.equals(attributeTerm.getName()))
      {
        attribute.addProperty(GeoObjectConfiguration.TARGET, "Term");
      }
    }

    return result;
  }
}
