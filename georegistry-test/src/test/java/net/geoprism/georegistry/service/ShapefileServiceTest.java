package net.geoprism.georegistry.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
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
import org.geotools.feature.FeatureCollection;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.constants.VaultProperties;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.LocatedIn;
import com.runwaysdk.system.metadata.MdTermRelationship;

import net.geoprism.data.importer.BasicColumnFunction;
import net.geoprism.georegistry.CodeRestriction;
import net.geoprism.georegistry.GeoObjectQuery;
import net.geoprism.georegistry.io.GeoObjectConfiguration;
import net.geoprism.georegistry.io.GeoObjectUtil;
import net.geoprism.georegistry.io.Location;
import net.geoprism.georegistry.shapefile.GeoObjectShapefileExporter;
import net.geoprism.georegistry.testframework.USATestData;

public class ShapefileServiceTest
{
  protected USATestData        testData;

  protected ClientRequestIF    adminCR;

  private AttributeTermType    testTerm;

  private AttributeIntegerType testInteger;

  @Before
  public void setUp()
  {
    this.testData = USATestData.newTestData(GeometryType.MULTIPOLYGON, false);

    this.adminCR = testData.adminClientRequest;

    AttributeTermType testTerm = (AttributeTermType) AttributeType.factory("testTerm", "testTermLocalName", "testTermLocalDescrip", AttributeTermType.TYPE);
    this.testTerm = (AttributeTermType) ServiceFactory.getRegistryService().createAttributeType(this.adminCR.getSessionId(), this.testData.STATE.getCode(), testTerm.toJSON().toString());

    AttributeIntegerType testInteger = (AttributeIntegerType) AttributeType.factory("testInteger", "testIntegerLocalName", "testIntegerLocalDescrip", AttributeIntegerType.TYPE);
    this.testInteger = (AttributeIntegerType) ServiceFactory.getRegistryService().createAttributeType(this.adminCR.getSessionId(), this.testData.STATE.getCode(), testInteger.toJSON().toString());
  }

  @After
  public void tearDown() throws IOException
  {
    testData.cleanUp();

    FileUtils.deleteDirectory(new File(VaultProperties.getPath("vault.default"), "files"));
  }

  @Test
  public void testGetAttributeInformation()
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();
    JsonObject result = service.getShapefileConfiguration(this.adminCR.getSessionId(), testData.STATE.getCode(), "cb_2017_us_state_500k.zip", istream);

    JsonObject type = result.getAsJsonObject(GeoObjectConfiguration.TYPE);

    Assert.assertNotNull(type);

    JsonArray tAttributes = type.get(GeoObjectType.JSON_ATTRIBUTES).getAsJsonArray();

    Assert.assertEquals(4, tAttributes.size());

    JsonObject tAttribute = tAttributes.get(0).getAsJsonObject();

    Assert.assertTrue(tAttribute.has("required"));
    Assert.assertTrue(tAttribute.get("required").getAsBoolean());

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

    Assert.assertEquals(7, fields.size());
    Assert.assertEquals("STATEFP", fields.get(0).getAsString());

    Assert.assertEquals(2, attributes.get(GeoObjectConfiguration.NUMERIC).getAsJsonArray().size());
    Assert.assertEquals(0, attributes.get(AttributeBooleanType.TYPE).getAsJsonArray().size());
    Assert.assertEquals(0, attributes.get(AttributeDateType.TYPE).getAsJsonArray().size());
  }

  @Test
  public void testImportShapefile()
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();

    JsonObject json = this.getTestConfiguration(istream, service, null);

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), false);

    service.importShapefile(this.adminCR.getSessionId(), configuration.toJson().toString());

    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(this.testData.adminClientRequest.getSessionId(), "01", testData.STATE.getCode());

    Assert.assertNotNull(object);
    Assert.assertNotNull(object.getGeometry());
    Assert.assertEquals("Alabama", object.getValue(GeoObject.LOCALIZED_DISPLAY_LABEL));
  }

  @Test
  public void testImportShapefileInteger()
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();

    JsonObject json = this.getTestConfiguration(istream, service, null);

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), false);
    configuration.setFunction(this.testInteger.getName(), new BasicColumnFunction("ALAND"));

    service.importShapefile(this.adminCR.getSessionId(), configuration.toJson().toString());

    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(this.testData.adminClientRequest.getSessionId(), "01", testData.STATE.getCode());

    Assert.assertNotNull(object);
    Assert.assertNotNull(object.getGeometry());
    Assert.assertEquals("Alabama", object.getValue(GeoObject.LOCALIZED_DISPLAY_LABEL));
    Assert.assertEquals(4592944701L, object.getValue(this.testInteger.getName()));
  }

  @Test
  @Request
  public void testImportShapefileWithParent()
  {
    GeoObject geoObj = ServiceFactory.getRegistryService().newGeoObjectInstance(this.testData.adminClientRequest.getSessionId(), this.testData.COUNTRY.getCode());
    geoObj.setCode("00");
    geoObj.setLocalizedDisplayLabel("Test Label");
    geoObj.setUid(ServiceFactory.getIdService().getUids(1)[0]);

    ServiceFactory.getUtilities().applyGeoObject(geoObj, true);

    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();

    JsonObject json = this.getTestConfiguration(istream, service, null);

    HierarchyType hierarchyType = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(LocatedIn.class.getSimpleName()).get();
    MdTermRelationship mdRelationship = ServiceFactory.getConversionService().existingHierarchyToGeoEntityMdTermRelationiship(hierarchyType);

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), false);
    configuration.setHierarchy(hierarchyType);
    configuration.setHierarchyRelationship(mdRelationship);
    configuration.addParent(new Location(this.testData.COUNTRY.getGeoObjectType(GeometryType.POLYGON), this.testData.COUNTRY.getUniversal(), new BasicColumnFunction("LSAD")));

    service.importShapefile(this.adminCR.getSessionId(), configuration.toJson().toString());

    String sessionId = this.testData.adminClientRequest.getSessionId();
    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(sessionId, "01", testData.STATE.getCode());

    Assert.assertEquals("01", object.getCode());

    ParentTreeNode nodes = ServiceFactory.getRegistryService().getParentGeoObjects(sessionId, object.getUid(), configuration.getType().getCode(), new String[] { this.testData.COUNTRY.getCode() }, false);

    List<ParentTreeNode> parents = nodes.getParents();

    Assert.assertEquals(1, parents.size());
  }

  @Test
  @Request
  public void testImportShapefileExcludeParent()
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();

    JsonObject json = this.getTestConfiguration(istream, service, null);

    HierarchyType hierarchyType = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(LocatedIn.class.getSimpleName()).get();
    MdTermRelationship mdRelationship = ServiceFactory.getConversionService().existingHierarchyToGeoEntityMdTermRelationiship(hierarchyType);

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), false);
    configuration.setHierarchy(hierarchyType);
    configuration.setHierarchyRelationship(mdRelationship);
    configuration.addParent(new Location(this.testData.COUNTRY.getGeoObjectType(GeometryType.POLYGON), this.testData.COUNTRY.getUniversal(), new BasicColumnFunction("LSAD")));
    configuration.addExclusion(GeoObjectConfiguration.PARENT_EXCLUSION, "00");

    JsonObject result = service.importShapefile(this.adminCR.getSessionId(), configuration.toJson().toString());

    Assert.assertFalse(result.has(GeoObjectConfiguration.LOCATION_PROBLEMS));

    // Ensure the geo objects were not created
    GeoObjectQuery query = new GeoObjectQuery(testData.STATE.getGeoObjectType(GeometryType.POLYGON), testData.STATE.getUniversal());
    query.setRestriction(new CodeRestriction("01"));

    Assert.assertNull(query.getSingleResult());
  }

  @Test
  @Request
  public void testImportShapefileWithBadParent()
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();

    JsonObject json = this.getTestConfiguration(istream, service, null);

    HierarchyType hierarchyType = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(LocatedIn.class.getSimpleName()).get();
    MdTermRelationship mdRelationship = ServiceFactory.getConversionService().existingHierarchyToGeoEntityMdTermRelationiship(hierarchyType);

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), false);
    configuration.setHierarchy(hierarchyType);
    configuration.setHierarchyRelationship(mdRelationship);
    configuration.addParent(new Location(this.testData.COUNTRY.getGeoObjectType(GeometryType.POLYGON), this.testData.COUNTRY.getUniversal(), new BasicColumnFunction("LSAD")));

    JsonObject result = service.importShapefile(this.adminCR.getSessionId(), configuration.toJson().toString());

    Assert.assertTrue(result.has(GeoObjectConfiguration.LOCATION_PROBLEMS));

    JsonArray problems = result.get(GeoObjectConfiguration.LOCATION_PROBLEMS).getAsJsonArray();

    Assert.assertEquals(1, problems.size());

    JsonObject problem = problems.get(0).getAsJsonObject();
    JsonArray contex = problem.get("context").getAsJsonArray();

    Assert.assertEquals(1, contex.size());

    // Ensure the geo objects were not created
    GeoObjectQuery query = new GeoObjectQuery(testData.STATE.getGeoObjectType(GeometryType.POLYGON), testData.STATE.getUniversal());
    query.setRestriction(new CodeRestriction("01"));

    Assert.assertNull(query.getSingleResult());
  }

  @Test
  public void testCreateFeatureType()
  {
    GeoObjectType type = ServiceFactory.getAdapter().getMetadataCache().getGeoObjectType(testData.STATE.getCode()).get();

    GeoObjectShapefileExporter exporter = new GeoObjectShapefileExporter(type, new LinkedList<GeoObject>());
    SimpleFeatureType featureType = exporter.createFeatureType();

    Assert.assertNotNull(featureType);

    Assert.assertEquals(GeoObjectShapefileExporter.GEOM, featureType.getGeometryDescriptor().getLocalName());

    List<AttributeDescriptor> attributes = featureType.getAttributeDescriptors();

    Assert.assertEquals( ( type.getAttributeMap().size() + 1 ), attributes.size());
  }

  @Test
  @Request
  public void testCreateFeatures()
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();

    JsonObject json = this.getTestConfiguration(istream, service, null);

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), false);

    service.importShapefile(this.adminCR.getSessionId(), configuration.toJson().toString());

    GeoObjectType type = testData.STATE.getGeoObjectType(GeometryType.MULTIPOLYGON);

    List<GeoObject> objects = new GeoObjectQuery(type, testData.STATE.getUniversal()).getIterator().getAll();

    Assert.assertEquals(56, objects.size());

    GeoObjectShapefileExporter exporter = new GeoObjectShapefileExporter(type, objects);
    SimpleFeatureType featureType = exporter.createFeatureType();

    FeatureCollection<SimpleFeatureType, SimpleFeature> features = exporter.features(featureType);

    Assert.assertEquals(objects.size(), features.size());

    SimpleFeature feature = features.features().next();
    GeoObject object = objects.get(0);

    Assert.assertEquals("Attributes not equal [code]", object.getValue(GeoObject.CODE), feature.getAttribute(GeoObject.CODE));

    Object geometry = feature.getDefaultGeometry();
    Assert.assertNotNull(geometry);

    Map<String, AttributeType> attributes = type.getAttributeMap();
    Set<Entry<String, AttributeType>> entries = attributes.entrySet();

    for (Entry<String, AttributeType> entry : entries)
    {
      AttributeType attribute = entry.getValue();
      String attributeName = attribute.getName();

      Object oValue = object.getValue(attributeName);
      Object fValue = feature.getAttribute(GeoObjectShapefileExporter.format(attributeName));

      if (attribute instanceof AttributeTermType)
      {
        Assert.assertEquals("Attributes not equal [" + attributeName + "]", GeoObjectUtil.convertToTermString((AttributeTermType) attribute, oValue), fValue);
      }
      else
      {
        Assert.assertEquals("Attributes not equal [" + attributeName + "]", oValue, fValue);
      }

    }
  }

  @Test
  @Request
  public void testWriteToFile() throws IOException
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();

    JsonObject json = this.getTestConfiguration(istream, service, null);

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), false);

    service.importShapefile(this.adminCR.getSessionId(), configuration.toJson().toString());

    GeoObjectType type = testData.STATE.getGeoObjectType(GeometryType.MULTIPOLYGON);

    List<GeoObject> objects = new GeoObjectQuery(type, testData.STATE.getUniversal()).getIterator().getAll();

    Assert.assertEquals(56, objects.size());

    GeoObjectShapefileExporter exporter = new GeoObjectShapefileExporter(type, objects);
    File directory = exporter.writeToFile();

    Assert.assertTrue(directory.exists());

    File[] files = directory.listFiles();

    Assert.assertEquals(5, files.length);
  }

  @Test
  @Request
  public void testExport() throws IOException
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();

    JsonObject json = this.getTestConfiguration(istream, service, null);

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), false);

    service.importShapefile(this.adminCR.getSessionId(), configuration.toJson().toString());

    GeoObjectType type = testData.STATE.getGeoObjectType(GeometryType.MULTIPOLYGON);

    List<GeoObject> objects = new GeoObjectQuery(type, testData.STATE.getUniversal()).getIterator().getAll();

    Assert.assertEquals(56, objects.size());

    GeoObjectShapefileExporter exporter = new GeoObjectShapefileExporter(type, objects);
    InputStream export = exporter.export();

    Assert.assertNotNull(export);

    IOUtils.copy(export, new NullOutputStream());
  }

  @Test
  @Request
  public void testImportShapefileWithTerm()
  {
    Term term = ServiceFactory.getRegistryService().createTerm(this.adminCR.getSessionId(), testTerm.getRootTerm().getCode(), new Term("00", "00", "").toJSON().toString());

    try
    {
      InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip");

      Assert.assertNotNull(istream);

      ShapefileService service = new ShapefileService();

      JsonObject json = this.getTestConfiguration(istream, service, testTerm);

      HierarchyType hierarchyType = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(LocatedIn.class.getSimpleName()).get();
      MdTermRelationship mdRelationship = ServiceFactory.getConversionService().existingHierarchyToGeoEntityMdTermRelationiship(hierarchyType);

      GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), false);
      configuration.setHierarchy(hierarchyType);
      configuration.setHierarchyRelationship(mdRelationship);

      service.importShapefile(this.adminCR.getSessionId(), configuration.toJson().toString());

      String sessionId = this.testData.adminClientRequest.getSessionId();
      GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(sessionId, "01", testData.STATE.getCode());

      Assert.assertEquals("01", object.getCode());
    }
    finally
    {
      ServiceFactory.getRegistryService().deleteTerm(this.adminCR.getSessionId(), term.getCode());
    }
  }

  @Test
  @Request
  public void testImportShapefileWithBadTerm()
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();

    JsonObject json = this.getTestConfiguration(istream, service, testTerm);

    HierarchyType hierarchyType = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(LocatedIn.class.getSimpleName()).get();
    MdTermRelationship mdRelationship = ServiceFactory.getConversionService().existingHierarchyToGeoEntityMdTermRelationiship(hierarchyType);

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), false);
    configuration.setHierarchy(hierarchyType);
    configuration.setHierarchyRelationship(mdRelationship);

    JsonObject result = service.importShapefile(this.adminCR.getSessionId(), configuration.toJson().toString());

    Assert.assertTrue(result.has(GeoObjectConfiguration.TERM_PROBLEMS));

    JsonArray problems = result.get(GeoObjectConfiguration.TERM_PROBLEMS).getAsJsonArray();

    Assert.assertEquals(1, problems.size());

    // Ensure the geo objects were not created
    GeoObjectQuery query = new GeoObjectQuery(testData.STATE.getGeoObjectType(GeometryType.POLYGON), testData.STATE.getUniversal());
    query.setRestriction(new CodeRestriction("01"));

    Assert.assertNull(query.getSingleResult());
  }

  private JsonObject getTestConfiguration(InputStream istream, ShapefileService service, AttributeTermType testTerm)
  {
    JsonObject result = service.getShapefileConfiguration(this.adminCR.getSessionId(), testData.STATE.getCode(), "cb_2017_us_state_500k.zip", istream);
    JsonObject type = result.getAsJsonObject(GeoObjectConfiguration.TYPE);
    JsonArray attributes = type.get(GeoObjectType.JSON_ATTRIBUTES).getAsJsonArray();

    for (int i = 0; i < attributes.size(); i++)
    {
      JsonObject attribute = attributes.get(i).getAsJsonObject();

      String attributeName = attribute.get(AttributeType.JSON_CODE).getAsString();

      if (attributeName.equals(GeoObject.LOCALIZED_DISPLAY_LABEL))
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
