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

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import net.geoprism.data.importer.BasicColumnFunction;
import net.geoprism.data.importer.FeatureRow;
import net.geoprism.data.importer.ShapefileFunction;
import net.geoprism.registry.etl.DataImportJob;
import net.geoprism.registry.etl.ImportConfiguration;
import net.geoprism.registry.etl.FormatSpecificImporterFactory.FormatImporterType;
import net.geoprism.registry.etl.ObjectImporterFactory.ObjectImportType;
import net.geoprism.registry.excel.GeoObjectExcelExporter;
import net.geoprism.registry.io.DelegateShapefileFunction;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
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
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

public class ExcelServiceTest
{
  protected static USATestData        testData;

  protected ClientRequestIF    adminCR;

  private AttributeTermType    testTerm;

  private AttributeIntegerType testInteger;

  private AttributeDateType    testDate;

  private AttributeBooleanType testBoolean;
  
  @Before
  public void setUp()
  {
//    testData.setUpTest();
    
    testData = USATestData.newTestDataForClass();
    testData.setUpMetadata();
    
    AttributeTermType testTerm = (AttributeTermType) AttributeType.factory("testTerm", new LocalizedValue("testTermLocalName"), new LocalizedValue("testTermLocalDescrip"), AttributeTermType.TYPE, false, false, false);
    this.testTerm = (AttributeTermType) ServiceFactory.getRegistryService().createAttributeType(testData.adminClientRequest.getSessionId(), testData.DISTRICT.getCode(), testTerm.toJSON().toString());

    AttributeBooleanType testBoolean = (AttributeBooleanType) AttributeType.factory("testBoolean", new LocalizedValue("testBooleanLocalName"), new LocalizedValue("testBooleanLocalDescrip"), AttributeBooleanType.TYPE, false, false, false);
    this.testBoolean = (AttributeBooleanType) ServiceFactory.getRegistryService().createAttributeType(testData.adminClientRequest.getSessionId(), testData.DISTRICT.getCode(), testBoolean.toJSON().toString());

    AttributeDateType testDate = (AttributeDateType) AttributeType.factory("testDate", new LocalizedValue("testDateLocalName"), new LocalizedValue("testDateLocalDescrip"), AttributeDateType.TYPE, false, false, false);
    this.testDate = (AttributeDateType) ServiceFactory.getRegistryService().createAttributeType(testData.adminClientRequest.getSessionId(), testData.DISTRICT.getCode(), testDate.toJSON().toString());

    AttributeIntegerType testInteger = (AttributeIntegerType) AttributeType.factory("testInteger", new LocalizedValue("testIntegerLocalName"), new LocalizedValue("testIntegerLocalDescrip"), AttributeIntegerType.TYPE, false, false, false);
    this.testInteger = (AttributeIntegerType) ServiceFactory.getRegistryService().createAttributeType(testData.adminClientRequest.getSessionId(), testData.DISTRICT.getCode(), testInteger.toJSON().toString());

    reload();
  }
  
  @After
  public void tearDown()
  {
//    testData.cleanUpTest();
    
    if (testData != null)
    {
      testData.tearDownMetadata();
    }
  }

  @Request
  public static void reload()
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
    PostalCodeFactory.remove(testData.DISTRICT.getServerObject());

    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();
    JSONObject result = service.getExcelConfiguration(testData.adminClientRequest.getSessionId(), testData.DISTRICT.getCode(), null, null, "test-spreadsheet.xlsx", istream);

    Assert.assertFalse(result.getBoolean(GeoObjectImportConfiguration.HAS_POSTAL_CODE));

    Assert.assertNotNull(result.getJSONObject(GeoObjectImportConfiguration.TYPE));

    JSONArray hierarchies = result.getJSONArray(GeoObjectImportConfiguration.HIERARCHIES);

    Assert.assertEquals(1, hierarchies.length());

    JSONObject hierarchy = hierarchies.getJSONObject(0);

    Assert.assertNotNull(hierarchy.getString("label"));

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

    ServerGeoObjectType type = testData.DISTRICT.getServerObject();

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
    JSONObject result = service.getExcelConfiguration(testData.adminClientRequest.getSessionId(), testData.DISTRICT.getCode(), null, null, "test-spreadsheet.xlsx", istream);

    Assert.assertTrue(result.getBoolean(GeoObjectImportConfiguration.HAS_POSTAL_CODE));
  }

  @Test
  @Request
  public void testImportSpreadsheet()
  {
    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();

    JSONObject json = this.getTestConfiguration(istream, service, null);
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    GeoObjectImportConfiguration configuration = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    configuration.setHierarchy(hierarchyType);

    DataImportJob.importService(testData.adminClientRequest.getSessionId(), configuration.toJSON().toString());

    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(this.testData.adminClientRequest.getSessionId(), "0001", testData.DISTRICT.getCode());

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

    JSONObject json = this.getTestConfiguration(istream, service, null);
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    GeoObjectImportConfiguration configuration = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    configuration.setFunction(this.testInteger.getName(), new BasicColumnFunction("Test Integer"));
    configuration.setHierarchy(hierarchyType);

    DataImportJob.importService(testData.adminClientRequest.getSessionId(), configuration.toJSON().toString());

    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(this.testData.adminClientRequest.getSessionId(), "0001", testData.DISTRICT.getCode());

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

    JSONObject json = this.getTestConfiguration(istream, service, null);
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    GeoObjectImportConfiguration configuration = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    configuration.setFunction(this.testDate.getName(), new BasicColumnFunction("Test Date"));
    configuration.setHierarchy(hierarchyType);

    DataImportJob.importService(testData.adminClientRequest.getSessionId(), configuration.toJSON().toString());

    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(this.testData.adminClientRequest.getSessionId(), "0001", testData.DISTRICT.getCode());

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

    JSONObject json = this.getTestConfiguration(istream, service, null);
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    GeoObjectImportConfiguration configuration = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    configuration.setFunction(this.testBoolean.getName(), new BasicColumnFunction("Test Boolean"));
    configuration.setHierarchy(hierarchyType);

    DataImportJob.importService(testData.adminClientRequest.getSessionId(), configuration.toJSON().toString());

    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(this.testData.adminClientRequest.getSessionId(), "0001", testData.DISTRICT.getCode());

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

    JSONObject json = this.getTestConfiguration(istream, service, null);
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    GeoObjectImportConfiguration configuration = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    configuration.setHierarchy(hierarchyType);

    DataImportJob.importService(testData.adminClientRequest.getSessionId(), configuration.toJSON().toString());

    ServerGeoObjectType type = testData.DISTRICT.getServerObject();

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
    Term term = ServiceFactory.getRegistryService().createTerm(testData.adminClientRequest.getSessionId(), testTerm.getRootTerm().getCode(), new Term("Test Term", new LocalizedValue("Test Term"), new LocalizedValue("")).toJSON().toString());

    try
    {
      this.testData.refreshTerms(testTerm);

      Calendar calendar = Calendar.getInstance();
      calendar.clear();
      calendar.set(2018, Calendar.FEBRUARY, 12, 0, 0, 0);

      GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
      Point point = geometryFactory.createPoint(new Coordinate(-104.991531, 39.742043));

      GeoObject geoObj = ServiceFactory.getRegistryService().newGeoObjectInstance(this.testData.adminClientRequest.getSessionId(), this.testData.DISTRICT.getCode());
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

      ServerGeoObjectType type = testData.DISTRICT.getServerObject();
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
      ServiceFactory.getRegistryService().deleteTerm(testData.adminClientRequest.getSessionId(), term.getCode());
    }
  }

  @Test
  @Request
  public void testImportExcelWithParent()
  {
    GeoObject geoObj = ServiceFactory.getRegistryService().newGeoObjectInstance(testData.adminClientRequest.getSessionId(), testData.STATE.getCode());
    geoObj.setCode("00");
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "Test Label");
    geoObj.setUid(ServiceFactory.getIdService().getUids(1)[0]);

    ServerGeoObjectIF serverGO = new ServerGeoObjectService().apply(geoObj, true, false);
    geoObj = RegistryService.getInstance().getGeoObjectByCode(Session.getCurrentSession().getOid(), serverGO.getCode(), serverGO.getType().getCode());

    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();

    JSONObject json = this.getTestConfiguration(istream, service, null);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    GeoObjectImportConfiguration configuration = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    configuration.setStartDate(new Date());
    configuration.setEndDate(new Date());
    configuration.setHierarchy(hierarchyType);

    configuration.addParent(new Location(testData.STATE.getServerObject(), new BasicColumnFunction("Parent")));

    DataImportJob.importService(testData.adminClientRequest.getSessionId(), configuration.toJSON().toString());

    String sessionId = testData.adminClientRequest.getSessionId();
    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(sessionId, "0001", testData.DISTRICT.getCode());

    Assert.assertEquals("0001", object.getCode());

    ParentTreeNode nodes = ServiceFactory.getRegistryService().getParentGeoObjects(sessionId, object.getUid(), configuration.getType().getCode(), new String[] { testData.STATE.getCode() }, false, new Date());

    List<ParentTreeNode> parents = nodes.getParents();

    Assert.assertEquals(1, parents.size());
  }

  @Test
  @Request
  public void testImportExcelWithPostalCode()
  {
    ServerGeoObjectType type = testData.DISTRICT.getServerObject();

    PostalCodeFactory.addPostalCode(type, new LocationBuilder()
    {
      @Override
      public Location build(ShapefileFunction function)
      {
        ServerGeoObjectType type = testData.STATE.getServerObject();

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

    GeoObject geoObj = ServiceFactory.getRegistryService().newGeoObjectInstance(this.testData.adminClientRequest.getSessionId(), this.testData.STATE.getCode());
    geoObj.setCode("00");
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "Test Label");
    geoObj.setUid(ServiceFactory.getIdService().getUids(1)[0]);

    ServerGeoObjectIF serverGO = new ServerGeoObjectService().apply(geoObj, true, false);
    geoObj = RegistryService.getInstance().getGeoObjectByCode(Session.getCurrentSession().getOid(), serverGO.getCode(), serverGO.getType().getCode());

    InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

    Assert.assertNotNull(istream);

    ExcelService service = new ExcelService();

    JSONObject json = this.getTestConfiguration(istream, service, null);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    GeoObjectImportConfiguration configuration = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    configuration.setStartDate(new Date());
    configuration.setEndDate(new Date());
    configuration.setHierarchy(hierarchyType);

    configuration.addParent(new Location(this.testData.STATE.getServerObject(), new BasicColumnFunction("Parent")));

    DataImportJob.importService(testData.adminClientRequest.getSessionId(), configuration.toJSON().toString());

    String sessionId = this.testData.adminClientRequest.getSessionId();
    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(sessionId, "0001", testData.DISTRICT.getCode());

    Assert.assertEquals("0001", object.getCode());

    ParentTreeNode nodes = ServiceFactory.getRegistryService().getParentGeoObjects(sessionId, object.getUid(), configuration.getType().getCode(), new String[] { this.testData.STATE.getCode() }, false, new Date());

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

    JSONObject json = this.getTestConfiguration(istream, service, null);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    GeoObjectImportConfiguration configuration = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    configuration.setHierarchy(hierarchyType);

    configuration.addParent(new Location(this.testData.COUNTRY.getServerObject(), new BasicColumnFunction("Parent")));
    configuration.addExclusion(GeoObjectImportConfiguration.PARENT_EXCLUSION, "00");

    JSONObject result = new JSONObject(DataImportJob.importService(testData.adminClientRequest.getSessionId(), configuration.toJSON().toString()));

    Assert.assertFalse(result.has(GeoObjectImportConfiguration.LOCATION_PROBLEMS));

    // Ensure the geo objects were not created
    GeoObjectQuery query = new GeoObjectQuery(testData.DISTRICT.getServerObject());
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

    JSONObject json = this.getTestConfiguration(istream, service, null);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    GeoObjectImportConfiguration configuration = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    configuration.setHierarchy(hierarchyType);

    configuration.addParent(new Location(this.testData.COUNTRY.getServerObject(), new BasicColumnFunction("Parent")));

    JSONObject result = new JSONObject(DataImportJob.importService(testData.adminClientRequest.getSessionId(), configuration.toJSON().toString()));

    Assert.assertTrue(result.has(GeoObjectImportConfiguration.LOCATION_PROBLEMS));

    JSONArray problems = result.getJSONArray(GeoObjectImportConfiguration.LOCATION_PROBLEMS);

    Assert.assertEquals(1, problems.length());

    // Ensure the geo objects were not created
    GeoObjectQuery query = new GeoObjectQuery(testData.DISTRICT.getServerObject());
    query.setRestriction(new CodeRestriction("0001"));

    Assert.assertNull(query.getSingleResult());
  }

  @Test
  @Request
  public void testImportExcelWithTerm()
  {
    Term term = ServiceFactory.getRegistryService().createTerm(testData.adminClientRequest.getSessionId(), testTerm.getRootTerm().getCode(), new Term("Test Term", new LocalizedValue("Test Term"), new LocalizedValue("")).toJSON().toString());

    try
    {
      this.testData.refreshTerms(testTerm);

      InputStream istream = this.getClass().getResourceAsStream("/test-spreadsheet.xlsx");

      Assert.assertNotNull(istream);

      ExcelService service = new ExcelService();

      JSONObject json = this.getTestConfiguration(istream, service, testTerm);

      ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

      GeoObjectImportConfiguration configuration = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
      configuration.setHierarchy(hierarchyType);

      DataImportJob.importService(testData.adminClientRequest.getSessionId(), configuration.toJSON().toString());

      String sessionId = this.testData.adminClientRequest.getSessionId();
      GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(sessionId, "0001", testData.DISTRICT.getCode());

      Assert.assertEquals("0001", object.getCode());
    }
    finally
    {
      ServiceFactory.getRegistryService().deleteTerm(testData.adminClientRequest.getSessionId(), term.getCode());

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

    JSONObject json = this.getTestConfiguration(istream, service, testTerm);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    GeoObjectImportConfiguration configuration = (GeoObjectImportConfiguration) ImportConfiguration.build(json.toString(), true);
    configuration.setHierarchy(hierarchyType);

    JSONObject result = new JSONObject(DataImportJob.importService(testData.adminClientRequest.getSessionId(), configuration.toJSON().toString()));

    Assert.assertTrue(result.has(GeoObjectImportConfiguration.TERM_PROBLEMS));

    JSONArray problems = result.getJSONArray(GeoObjectImportConfiguration.TERM_PROBLEMS);

    Assert.assertEquals(1, problems.length());

    // Assert the values of the problem
    JSONObject problem = problems.getJSONObject(0);

    Assert.assertEquals("Test Term", problem.getString("label"));
    Assert.assertEquals(this.testTerm.getRootTerm().getCode(), problem.getString("parentCode"));
    Assert.assertEquals(this.testTerm.getName(), problem.getString("attributeCode"));
    Assert.assertEquals(this.testTerm.getLabel().getValue(), problem.getString("attributeLabel"));

    // Ensure the geo objects were not created
    GeoObjectQuery query = new GeoObjectQuery(testData.DISTRICT.getServerObject());
    query.setRestriction(new CodeRestriction("0001"));

    Assert.assertNull(query.getSingleResult());
  }

  private JSONObject getTestConfiguration(InputStream istream, ExcelService service, AttributeTermType attributeTerm)
  {
    JSONObject result = service.getExcelConfiguration(testData.adminClientRequest.getSessionId(), testData.DISTRICT.getCode(), null, null, "test-spreadsheet.xlsx", istream);
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

    return result;
  }
}
