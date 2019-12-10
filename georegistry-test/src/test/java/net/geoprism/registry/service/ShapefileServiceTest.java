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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.constants.VaultProperties;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionFacade;
import com.runwaysdk.system.gis.geo.LocatedIn;

import net.geoprism.data.importer.BasicColumnFunction;
import net.geoprism.data.importer.ShapefileFunction;
import net.geoprism.registry.io.GeoObjectConfiguration;
import net.geoprism.registry.io.Location;
import net.geoprism.registry.io.LocationBuilder;
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

  protected ClientRequestIF    adminCR;

  private AttributeTermType    testTerm;

  private AttributeIntegerType testInteger;

  @Before
  public void setUp()
  {
    this.testData = USATestData.newTestData(false);

    this.adminCR = testData.adminClientRequest;

    AttributeTermType testTerm = (AttributeTermType) AttributeType.factory("testTerm", new LocalizedValue("testTermLocalName"), new LocalizedValue("testTermLocalDescrip"), AttributeTermType.TYPE, false, false, false);
    this.testTerm = (AttributeTermType) ServiceFactory.getRegistryService().createAttributeType(this.adminCR.getSessionId(), this.testData.STATE.getCode(), testTerm.toJSON().toString());

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
  public void tearDown() throws IOException
  {
    testData.cleanUp();

    FileUtils.deleteDirectory(new File(VaultProperties.getPath("vault.default"), "files"));
  }

  @Test
  @Request
  public void testGetAttributeInformation()
  {
    PostalCodeFactory.remove(testData.STATE.getGeoObjectType());

    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();
    JsonObject result = service.getShapefileConfiguration(this.adminCR.getSessionId(), testData.STATE.getCode(), null, null, "cb_2017_us_state_500k.zip.test", istream);

    Assert.assertFalse(result.get(GeoObjectConfiguration.HAS_POSTAL_CODE).getAsBoolean());

    JsonObject type = result.getAsJsonObject(GeoObjectConfiguration.TYPE);

    Assert.assertNotNull(type);

    JsonArray tAttributes = type.get(GeoObjectType.JSON_ATTRIBUTES).getAsJsonArray();

    Assert.assertEquals(4, tAttributes.size());

    boolean hasCode = false;

    for (int i = 0; i < tAttributes.size(); i++)
    {
      JsonObject tAttribute = tAttributes.get(i).getAsJsonObject();
      String code = tAttribute.get(AttributeType.JSON_CODE).getAsString();

      if (code.equals(GeoObjectType.JSON_CODE))
      {
        hasCode = true;
        Assert.assertTrue(tAttribute.has("required"));
        Assert.assertTrue(tAttribute.get("required").getAsBoolean());
      }
    }

    Assert.assertTrue(hasCode);

    JsonArray hierarchies = result.get(GeoObjectConfiguration.HIERARCHIES).getAsJsonArray();

    Assert.assertEquals(1, hierarchies.size());

    JsonObject hierarchy = hierarchies.get(0).getAsJsonObject();

    Assert.assertNotNull(hierarchy.get("label").getAsString());

    JsonObject sheet = result.getAsJsonObject("sheet");

    Assert.assertNotNull(sheet);
    Assert.assertEquals("cb_2017_us_state_500k", sheet.get("name").getAsString());

    JsonObject attributes = sheet.get("attributes").getAsJsonObject();

    Assert.assertNotNull(attributes);

    JsonArray fields = attributes.get(GeoObjectConfiguration.TEXT).getAsJsonArray();

    Assert.assertEquals(9, fields.size());
    Assert.assertEquals("STATEFP", fields.get(0).getAsString());

    Assert.assertEquals(2, attributes.get(GeoObjectConfiguration.NUMERIC).getAsJsonArray().size());
    Assert.assertEquals(0, attributes.get(AttributeBooleanType.TYPE).getAsJsonArray().size());
    Assert.assertEquals(0, attributes.get(AttributeDateType.TYPE).getAsJsonArray().size());
  }

  @Test
  @Request
  public void testGetAttributeInformationPostalCode()
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

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

    ShapefileService service = new ShapefileService();
    JsonObject result = service.getShapefileConfiguration(this.adminCR.getSessionId(), testData.STATE.getCode(), null, null, "cb_2017_us_state_500k.zip.test", istream);

    Assert.assertTrue(result.get(GeoObjectConfiguration.HAS_POSTAL_CODE).getAsBoolean());
  }

  @Test
  @Request
  public void testImportShapefile()
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    JsonObject json = this.getTestConfiguration(istream, service, null);

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), false);
    configuration.setHierarchy(hierarchyType);

    service.importShapefile(this.adminCR.getSessionId(), configuration.toJson().toString());

    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(this.testData.adminClientRequest.getSessionId(), "01", testData.STATE.getCode());

    Assert.assertNotNull(object);
    Assert.assertNotNull(object.getGeometry());
    Assert.assertEquals("Alabama", object.getLocalizedDisplayLabel());
    Assert.assertEquals(GeoObjectStatusTerm.ACTIVE.code, object.getStatus().getCode());
  }

  @Test
  @Request
  public void testUpdateShapefile()
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    JsonObject json = this.getTestConfiguration(istream, service, null);

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), false);
    configuration.setHierarchy(hierarchyType);

    service.importShapefile(this.adminCR.getSessionId(), configuration.toJson().toString());

    /*
     * IMport the shapefile twice
     */
    service.importShapefile(this.adminCR.getSessionId(), configuration.toJson().toString());

    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(this.testData.adminClientRequest.getSessionId(), "01", testData.STATE.getCode());

    Assert.assertNotNull(object);
    Assert.assertNotNull(object.getGeometry());
    Assert.assertEquals("Alabama", object.getLocalizedDisplayLabel());
  }

  @Test
  @Request
  public void testImportShapefileInteger()
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();

    JsonObject json = this.getTestConfiguration(istream, service, null);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), false);
    configuration.setFunction(this.testInteger.getName(), new BasicColumnFunction("ALAND"));
    configuration.setHierarchy(hierarchyType);

    service.importShapefile(this.adminCR.getSessionId(), configuration.toJson().toString());

    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(this.testData.adminClientRequest.getSessionId(), "01", testData.STATE.getCode());

    Assert.assertNotNull(object);
    Assert.assertNotNull(object.getGeometry());
    Assert.assertEquals("Alabama", object.getLocalizedDisplayLabel());
    Assert.assertEquals(131174431216L, object.getValue(this.testInteger.getName()));
  }

  @Test
  @Request
  public void testImportShapefileWithParent()
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

    JsonObject json = this.getTestConfiguration(istream, service, null);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), false);
    configuration.setHierarchy(hierarchyType);

    configuration.addParent(new Location(this.testData.COUNTRY.getGeoObjectType(), new BasicColumnFunction("LSAD")));

    service.importShapefile(this.adminCR.getSessionId(), configuration.toJson().toString());

    String sessionId = this.testData.adminClientRequest.getSessionId();
    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(sessionId, "01", testData.STATE.getCode());

    Assert.assertEquals("01", object.getCode());

    ParentTreeNode nodes = ServiceFactory.getRegistryService().getParentGeoObjects(sessionId, object.getUid(), configuration.getType().getCode(), new String[] { this.testData.COUNTRY.getCode() }, false, new Date());

    List<ParentTreeNode> parents = nodes.getParents();

    Assert.assertEquals(1, parents.size());
  }

  @Test
  @Request
  public void testImportShapefileExcludeParent()
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();

    JsonObject json = this.getTestConfiguration(istream, service, null);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), false);
    configuration.setHierarchy(hierarchyType);

    configuration.addParent(new Location(this.testData.COUNTRY.getGeoObjectType(), new BasicColumnFunction("LSAD")));
    configuration.addExclusion(GeoObjectConfiguration.PARENT_EXCLUSION, "00");

    JsonObject result = service.importShapefile(this.adminCR.getSessionId(), configuration.toJson().toString());

    Assert.assertFalse(result.has(GeoObjectConfiguration.LOCATION_PROBLEMS));

    // Ensure the geo objects were not created
    GeoObjectQuery query = new GeoObjectQuery(testData.STATE.getGeoObjectType());
    query.setRestriction(new CodeRestriction("01"));

    Assert.assertNull(query.getSingleResult());
  }

  @Test
  @Request
  public void testImportShapefileWithBadParent()
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();

    JsonObject json = this.getTestConfiguration(istream, service, null);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), false);
    configuration.setHierarchy(hierarchyType);

    configuration.addParent(new Location(this.testData.COUNTRY.getGeoObjectType(), new BasicColumnFunction("LSAD")));

    JsonObject result = service.importShapefile(this.adminCR.getSessionId(), configuration.toJson().toString());

    Assert.assertTrue(result.has(GeoObjectConfiguration.LOCATION_PROBLEMS));

    JsonArray problems = result.get(GeoObjectConfiguration.LOCATION_PROBLEMS).getAsJsonArray();

    Assert.assertEquals(1, problems.size());

    // Ensure the geo objects were not created
    GeoObjectQuery query = new GeoObjectQuery(testData.STATE.getGeoObjectType());
    query.setRestriction(new CodeRestriction("01"));

    Assert.assertNull(query.getSingleResult());
  }

  @Test
  @Request
  public void testImportShapefileWithTerm()
  {
    Term term = ServiceFactory.getRegistryService().createTerm(this.adminCR.getSessionId(), testTerm.getRootTerm().getCode(), new Term("00", new LocalizedValue("00"), new LocalizedValue("")).toJSON().toString());

    try
    {
      this.testData.refreshTerms(this.testTerm);

      InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

      Assert.assertNotNull(istream);

      ShapefileService service = new ShapefileService();

      JsonObject json = this.getTestConfiguration(istream, service, testTerm);

      ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

      GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), false);
      configuration.setHierarchy(hierarchyType);

      service.importShapefile(this.adminCR.getSessionId(), configuration.toJson().toString());

      String sessionId = this.testData.adminClientRequest.getSessionId();
      GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(sessionId, "01", testData.STATE.getCode());

      Assert.assertEquals("01", object.getCode());
    }
    finally
    {
      ServiceFactory.getRegistryService().deleteTerm(this.adminCR.getSessionId(), term.getCode());

      this.testData.refreshTerms(this.testTerm);
    }
  }

  @Test
  @Request
  public void testImportShapefileWithBadTerm()
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip.test");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();

    JsonObject json = this.getTestConfiguration(istream, service, testTerm);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), false);
    configuration.setHierarchy(hierarchyType);

    JsonObject result = service.importShapefile(this.adminCR.getSessionId(), configuration.toJson().toString());

    Assert.assertTrue(result.has(GeoObjectConfiguration.TERM_PROBLEMS));

    JsonArray problems = result.get(GeoObjectConfiguration.TERM_PROBLEMS).getAsJsonArray();

    Assert.assertEquals(1, problems.size());

    // Assert the values of the problem
    JsonObject problem = problems.get(0).getAsJsonObject();

    Assert.assertEquals("00", problem.get("label").getAsString());
    Assert.assertEquals(this.testTerm.getRootTerm().getCode(), problem.get("parentCode").getAsString());
    Assert.assertEquals(this.testTerm.getName(), problem.get("attributeCode").getAsString());
    Assert.assertEquals(this.testTerm.getLabel().getValue(), problem.get("attributeLabel").getAsString());

    // Ensure the geo objects were not created
    GeoObjectQuery query = new GeoObjectQuery(testData.STATE.getGeoObjectType());
    query.setRestriction(new CodeRestriction("01"));

    Assert.assertNull(query.getSingleResult());
  }

  private JsonObject getTestConfiguration(InputStream istream, ShapefileService service, AttributeTermType testTerm)
  {
    JsonObject result = service.getShapefileConfiguration(this.adminCR.getSessionId(), testData.STATE.getCode(), null, null, "cb_2017_us_state_500k.zip.test", istream);
    JsonObject type = result.getAsJsonObject(GeoObjectConfiguration.TYPE);
    JsonArray attributes = type.get(GeoObjectType.JSON_ATTRIBUTES).getAsJsonArray();

    for (int i = 0; i < attributes.size(); i++)
    {
      JsonObject attribute = attributes.get(i).getAsJsonObject();

      String attributeName = attribute.get(AttributeType.JSON_CODE).getAsString();

      if (attributeName.equals(GeoObject.DISPLAY_LABEL))
      {
        attribute.addProperty(GeoObjectConfiguration.TARGET, "NAME");
      }
      else if (attributeName.equals(GeoObject.CODE))
      {
        attribute.addProperty(GeoObjectConfiguration.TARGET, "GEOID");
      }
      else if (testTerm != null && attributeName.equals(testTerm.getName()))
      {
        attribute.addProperty(GeoObjectConfiguration.TARGET, "LSAD");
      }

    }

    return result;
  }
}
