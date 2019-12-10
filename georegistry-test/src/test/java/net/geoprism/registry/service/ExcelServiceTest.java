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

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.jaitools.jts.CoordinateSequence2D;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionFacade;
import com.runwaysdk.system.gis.geo.LocatedIn;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

import net.geoprism.data.importer.BasicColumnFunction;
import net.geoprism.data.importer.FeatureRow;
import net.geoprism.data.importer.ShapefileFunction;
import net.geoprism.registry.excel.GeoObjectExcelExporter;
import net.geoprism.registry.io.DelegateShapefileFunction;
import net.geoprism.registry.io.GeoObjectConfiguration;
import net.geoprism.registry.io.Location;
import net.geoprism.registry.io.LocationBuilder;
import net.geoprism.registry.io.PostalCodeFactory;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.query.postgres.CodeRestriction;
import net.geoprism.registry.query.postgres.GeoObjectIterator;
import net.geoprism.registry.query.postgres.GeoObjectQuery;
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
    this.testData = USATestData.newTestData(false);

    this.adminCR = testData.adminClientRequest;

    AttributeTermType testTerm = (AttributeTermType) AttributeType.factory("testTerm", new LocalizedValue("testTermLocalName"), new LocalizedValue("testTermLocalDescrip"), AttributeTermType.TYPE, false, false, false);
    this.testTerm = (AttributeTermType) ServiceFactory.getRegistryService().createAttributeType(this.adminCR.getSessionId(), this.testData.STATE.getCode(), testTerm.toJSON().toString());

    AttributeBooleanType testBoolean = (AttributeBooleanType) AttributeType.factory("testBoolean", new LocalizedValue("testBooleanLocalName"), new LocalizedValue("testBooleanLocalDescrip"), AttributeBooleanType.TYPE, false, false, false);
    this.testBoolean = (AttributeBooleanType) ServiceFactory.getRegistryService().createAttributeType(this.adminCR.getSessionId(), this.testData.STATE.getCode(), testBoolean.toJSON().toString());

    AttributeDateType testDate = (AttributeDateType) AttributeType.factory("testDate", new LocalizedValue("testDateLocalName"), new LocalizedValue("testDateLocalDescrip"), AttributeDateType.TYPE, false, false, false);
    this.testDate = (AttributeDateType) ServiceFactory.getRegistryService().createAttributeType(this.adminCR.getSessionId(), this.testData.STATE.getCode(), testDate.toJSON().toString());

    AttributeIntegerType testInteger = (AttributeIntegerType) AttributeType.factory("testInteger", new LocalizedValue("testIntegerLocalName"), new LocalizedValue("testIntegerLocalDescrip"), AttributeIntegerType.TYPE, false, false, false);
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
  @Request
  public void testGetAttributeInformation()
  {
    PostalCodeFactory.remove(testData.STATE.getGeoObjectType());

    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();
    JsonObject result = service.getExcelConfiguration(this.adminCR.getSessionId(), testData.STATE.getCode(), null, null, "test-spreadsheet.xlsx", istream);

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

    Assert.assertEquals(8, fields.size());
    Assert.assertEquals("Longitude", fields.get(0).getAsString());

    Assert.assertEquals(4, attributes.get(GeoObjectConfiguration.NUMERIC).getAsJsonArray().size());
    Assert.assertEquals(1, attributes.get(AttributeBooleanType.TYPE).getAsJsonArray().size());
    Assert.assertEquals(1, attributes.get(AttributeDateType.TYPE).getAsJsonArray().size());
  }

  @Test
  @Request
  public void testGetAttributeInformationPostalCode()
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    ServerGeoObjectType type = testData.STATE.getGeoObjectType();

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
    JsonObject result = service.getExcelConfiguration(this.adminCR.getSessionId(), testData.STATE.getCode(), null, null, "test-spreadsheet.xlsx", istream);

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
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), true);
    configuration.setHierarchy(hierarchyType);

    service.importExcelFile(this.adminCR.getSessionId(), configuration.toJson().toString());

    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(this.testData.adminClientRequest.getSessionId(), "0001", testData.STATE.getCode());

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
  public void testImportSpreadsheetInteger()
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();

    JsonObject json = this.getTestConfiguration(istream, service, null);
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), true);
    configuration.setFunction(this.testInteger.getName(), new BasicColumnFunction("Test Integer"));
    configuration.setHierarchy(hierarchyType);

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
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), true);
    configuration.setFunction(this.testDate.getName(), new BasicColumnFunction("Test Date"));
    configuration.setHierarchy(hierarchyType);

    service.importExcelFile(this.adminCR.getSessionId(), configuration.toJson().toString());

    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(this.testData.adminClientRequest.getSessionId(), "0001", testData.STATE.getCode());

    Calendar calendar = Calendar.getInstance();
    calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
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
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), true);
    configuration.setFunction(this.testBoolean.getName(), new BasicColumnFunction("Test Boolean"));
    configuration.setHierarchy(hierarchyType);

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
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), true);
    configuration.setHierarchy(hierarchyType);

    service.importExcelFile(this.adminCR.getSessionId(), configuration.toJson().toString());

    ServerGeoObjectType type = testData.STATE.getGeoObjectType();

    GeoObjectIterator objects = new GeoObjectQuery(type).getIterator();

    try
    {
      GeoObjectExcelExporter exporter = new GeoObjectExcelExporter(type, hierarchyType, objects);
      Workbook workbook = exporter.createWorkbook();

      Assert.assertEquals(1, workbook.getNumberOfSheets());

      Sheet sheet = workbook.getSheetAt(0);

      Assert.assertEquals(WorkbookUtil.createSafeSheetName(type.getLabel().getValue()), sheet.getSheetName());
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
    Term term = ServiceFactory.getRegistryService().createTerm(this.adminCR.getSessionId(), testTerm.getRootTerm().getCode(), new Term("Test Term", new LocalizedValue("Test Term"), new LocalizedValue("")).toJSON().toString());

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
      geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "Test Label");
      geoObj.setUid(ServiceFactory.getIdService().getUids(1)[0]);
      geoObj.setGeometry(point);
      geoObj.setValue(this.testTerm.getName(), term.getCode());
      geoObj.setValue(this.testInteger.getName(), 23L);
      geoObj.setValue(this.testDate.getName(), calendar.getTime());
      geoObj.setValue(this.testBoolean.getName(), true);

      ServerGeoObjectIF serverGO = new ServerGeoObjectService().apply(geoObj, true, false);
      geoObj = RegistryService.getInstance().getGeoObjectByCode(Session.getCurrentSession().getOid(), serverGO.getCode(), serverGO.getType().getCode());

      InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

      Assert.assertNotNull(istream);

      ServerGeoObjectType type = testData.STATE.getGeoObjectType();
      ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

      GeoObjectIterator objects = new GeoObjectQuery(type).getIterator();

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
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "Test Label");
    geoObj.setUid(ServiceFactory.getIdService().getUids(1)[0]);

    ServerGeoObjectIF serverGO = new ServerGeoObjectService().apply(geoObj, true, false);
    geoObj = RegistryService.getInstance().getGeoObjectByCode(Session.getCurrentSession().getOid(), serverGO.getCode(), serverGO.getType().getCode());

    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();

    JsonObject json = this.getTestConfiguration(istream, service, null);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), true);
    configuration.setHierarchy(hierarchyType);

    configuration.addParent(new Location(this.testData.COUNTRY.getGeoObjectType(), new BasicColumnFunction("Parent")));

    service.importExcelFile(this.adminCR.getSessionId(), configuration.toJson().toString());

    String sessionId = this.testData.adminClientRequest.getSessionId();
    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(sessionId, "0001", testData.STATE.getCode());

    Assert.assertEquals("0001", object.getCode());

    ParentTreeNode nodes = ServiceFactory.getRegistryService().getParentGeoObjects(sessionId, object.getUid(), configuration.getType().getCode(), new String[] { this.testData.COUNTRY.getCode() }, false, new Date());

    List<ParentTreeNode> parents = nodes.getParents();

    Assert.assertEquals(1, parents.size());
  }

  @Test
  @Request
  public void testImportExcelWithPostalCode()
  {
    ServerGeoObjectType type = testData.STATE.getGeoObjectType();

    PostalCodeFactory.addPostalCode(type, new LocationBuilder()
    {
      @Override
      public Location build(ShapefileFunction function)
      {
        ServerGeoObjectType type = testData.COUNTRY.getGeoObjectType();

        DelegateShapefileFunction delegate = new DelegateShapefileFunction(function)
        {
          @Override
          public Object getValue(FeatureRow feature)
          {
            String code = (String) super.getValue(feature);

            return code.substring(0, 2);
          }
        };

        return new Location(type, delegate);
      }
    });

    GeoObject geoObj = ServiceFactory.getRegistryService().newGeoObjectInstance(this.testData.adminClientRequest.getSessionId(), this.testData.COUNTRY.getCode());
    geoObj.setCode("00");
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "Test Label");
    geoObj.setUid(ServiceFactory.getIdService().getUids(1)[0]);

    ServerGeoObjectIF serverGO = new ServerGeoObjectService().apply(geoObj, true, false);
    geoObj = RegistryService.getInstance().getGeoObjectByCode(Session.getCurrentSession().getOid(), serverGO.getCode(), serverGO.getType().getCode());

    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();

    JsonObject json = this.getTestConfiguration(istream, service, null);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), true);
    configuration.setHierarchy(hierarchyType);

    configuration.addParent(new Location(this.testData.COUNTRY.getGeoObjectType(), new BasicColumnFunction("Parent")));

    service.importExcelFile(this.adminCR.getSessionId(), configuration.toJson().toString());

    String sessionId = this.testData.adminClientRequest.getSessionId();
    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(sessionId, "0001", testData.STATE.getCode());

    Assert.assertEquals("0001", object.getCode());

    ParentTreeNode nodes = ServiceFactory.getRegistryService().getParentGeoObjects(sessionId, object.getUid(), configuration.getType().getCode(), new String[] { this.testData.COUNTRY.getCode() }, false, new Date());

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

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), true);
    configuration.setHierarchy(hierarchyType);

    configuration.addParent(new Location(this.testData.COUNTRY.getGeoObjectType(), new BasicColumnFunction("Parent")));
    configuration.addExclusion(GeoObjectConfiguration.PARENT_EXCLUSION, "00");

    JsonObject result = service.importExcelFile(this.adminCR.getSessionId(), configuration.toJson().toString());

    Assert.assertFalse(result.has(GeoObjectConfiguration.LOCATION_PROBLEMS));

    // Ensure the geo objects were not created
    GeoObjectQuery query = new GeoObjectQuery(testData.STATE.getGeoObjectType());
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

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), true);
    configuration.setHierarchy(hierarchyType);

    configuration.addParent(new Location(this.testData.COUNTRY.getGeoObjectType(), new BasicColumnFunction("Parent")));

    JsonObject result = service.importExcelFile(this.adminCR.getSessionId(), configuration.toJson().toString());

    Assert.assertTrue(result.has(GeoObjectConfiguration.LOCATION_PROBLEMS));

    JsonArray problems = result.get(GeoObjectConfiguration.LOCATION_PROBLEMS).getAsJsonArray();

    Assert.assertEquals(1, problems.size());

    // Ensure the geo objects were not created
    GeoObjectQuery query = new GeoObjectQuery(testData.STATE.getGeoObjectType());
    query.setRestriction(new CodeRestriction("0001"));

    Assert.assertNull(query.getSingleResult());
  }

  @Test
  @Request
  public void testImportExcelWithTerm()
  {
    Term term = ServiceFactory.getRegistryService().createTerm(this.adminCR.getSessionId(), testTerm.getRootTerm().getCode(), new Term("Test Term", new LocalizedValue("Test Term"), new LocalizedValue("")).toJSON().toString());

    try
    {
      this.testData.refreshTerms(testTerm);

      InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

      Assert.assertNotNull(istream);

      ExcelService service = new ExcelService();

      JsonObject json = this.getTestConfiguration(istream, service, testTerm);

      ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

      GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), true);
      configuration.setHierarchy(hierarchyType);

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

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), true);
    configuration.setHierarchy(hierarchyType);

    JsonObject result = service.importExcelFile(this.adminCR.getSessionId(), configuration.toJson().toString());

    Assert.assertTrue(result.has(GeoObjectConfiguration.TERM_PROBLEMS));

    JsonArray problems = result.get(GeoObjectConfiguration.TERM_PROBLEMS).getAsJsonArray();

    Assert.assertEquals(1, problems.size());

    // Assert the values of the problem
    JsonObject problem = problems.get(0).getAsJsonObject();

    Assert.assertEquals("Test Term", problem.get("label").getAsString());
    Assert.assertEquals(this.testTerm.getRootTerm().getCode(), problem.get("parentCode").getAsString());
    Assert.assertEquals(this.testTerm.getName(), problem.get("attributeCode").getAsString());
    Assert.assertEquals(this.testTerm.getLabel().getValue(), problem.get("attributeLabel").getAsString());

    // Ensure the geo objects were not created
    GeoObjectQuery query = new GeoObjectQuery(testData.STATE.getGeoObjectType());
    query.setRestriction(new CodeRestriction("0001"));

    Assert.assertNull(query.getSingleResult());
  }

  private JsonObject getTestConfiguration(InputStream istream, ExcelService service, AttributeTermType attributeTerm)
  {
    JsonObject result = service.getExcelConfiguration(this.adminCR.getSessionId(), testData.STATE.getCode(), null, null, "test-spreadsheet.xlsx", istream);
    JsonObject type = result.getAsJsonObject(GeoObjectConfiguration.TYPE);
    JsonArray attributes = type.get(GeoObjectType.JSON_ATTRIBUTES).getAsJsonArray();

    for (int i = 0; i < attributes.size(); i++)
    {
      JsonObject attribute = attributes.get(i).getAsJsonObject();

      String attributeName = attribute.get(AttributeType.JSON_CODE).getAsString();

      if (attributeName.equals(GeoObject.DISPLAY_LABEL))
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
