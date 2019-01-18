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
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
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

import net.geoprism.georegistry.io.GeoObjectConfiguration;
import net.geoprism.georegistry.io.GeoObjectUtil;
import net.geoprism.georegistry.shapefile.GeoObjectShapefileExporter;
import net.geoprism.registry.testframework.USATestData;

public class ShapefileServiceTest
{
  protected USATestData     tutil;

  protected ClientRequestIF adminCR;

  @Before
  public void setUp()
  {
    this.tutil = USATestData.newTestData(GeometryType.MULTIPOLYGON, false);

    this.adminCR = tutil.adminClientRequest;
  }

  @After
  public void tearDown() throws IOException
  {
    tutil.cleanUp();

    FileUtils.deleteDirectory(new File(VaultProperties.getPath("vault.default"), "files"));
  }

  @Test
  public void testGetAttributeInformation()
  {
    InputStream istream = this.getClass().getResourceAsStream("/cb_2017_us_state_500k.zip");

    Assert.assertNotNull(istream);

    ShapefileService service = new ShapefileService();
    JsonObject result = service.getShapefileConfiguration(this.adminCR.getSessionId(), tutil.STATE.getCode(), "cb_2017_us_state_500k.zip", istream);

    System.out.println(result.toString());

    Assert.assertNotNull(result.getAsJsonObject("type"));

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

    JsonObject json = this.getTestConfiguration(istream, service);

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), false);

    service.importShapefile(this.adminCR.getSessionId(), configuration.toJson().toString());

    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(this.tutil.adminClientRequest.getSessionId(), "01", tutil.STATE.getCode());

    Assert.assertNotNull(object);
    Assert.assertNotNull(object.getGeometry());
    Assert.assertEquals("Alabama", object.getValue(GeoObject.LOCALIZED_DISPLAY_LABEL));
  }

  @Test
  public void testCreateFeatureType()
  {
    GeoObjectType type = ServiceFactory.getAdapter().getMetadataCache().getGeoObjectType(tutil.STATE.getCode()).get();

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

    JsonObject json = this.getTestConfiguration(istream, service);

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), false);

    service.importShapefile(this.adminCR.getSessionId(), configuration.toJson().toString());

    GeoObjectType type = ServiceFactory.getAdapter().getMetadataCache().getGeoObjectType(tutil.STATE.getCode()).get();

    List<GeoObject> objects = GeoObjectUtil.getObjects(type);

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
        Assert.assertEquals("Attributes not equal [" + attributeName + "]", GeoObjectUtil.convertToTermString(oValue), fValue);
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

    JsonObject json = this.getTestConfiguration(istream, service);

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), false);

    service.importShapefile(this.adminCR.getSessionId(), configuration.toJson().toString());

    GeoObjectType type = ServiceFactory.getAdapter().getMetadataCache().getGeoObjectType(tutil.STATE.getCode()).get();

    List<GeoObject> objects = GeoObjectUtil.getObjects(type);

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

    JsonObject json = this.getTestConfiguration(istream, service);

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString(), false);

    service.importShapefile(this.adminCR.getSessionId(), configuration.toJson().toString());

    GeoObjectType type = ServiceFactory.getAdapter().getMetadataCache().getGeoObjectType(tutil.STATE.getCode()).get();

    List<GeoObject> objects = GeoObjectUtil.getObjects(type);

    Assert.assertEquals(56, objects.size());

    GeoObjectShapefileExporter exporter = new GeoObjectShapefileExporter(type, objects);
    InputStream export = exporter.export();

    Assert.assertNotNull(export);

    IOUtils.copy(export, new NullOutputStream());
  }

  private JsonObject getTestConfiguration(InputStream istream, ShapefileService service)
  {
    JsonObject result = service.getShapefileConfiguration(this.adminCR.getSessionId(), tutil.STATE.getCode(), "cb_2017_us_state_500k.zip", istream);
    JsonObject type = result.getAsJsonObject("type");
    JsonArray attributes = type.get("attributes").getAsJsonArray();

    for (int i = 0; i < attributes.size(); i++)
    {
      JsonObject attribute = attributes.get(i).getAsJsonObject();

      String attributeName = attribute.get("name").getAsString();

      if (attributeName.equals(GeoObject.LOCALIZED_DISPLAY_LABEL))
      {
        attribute.addProperty("target", "NAME");
      }
      else if (attributeName.equals(GeoObject.CODE))
      {
        attribute.addProperty("target", "GEOID");
      }
    }

    return result;
  }
}
