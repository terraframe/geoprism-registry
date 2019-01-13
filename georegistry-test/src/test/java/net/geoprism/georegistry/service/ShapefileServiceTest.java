package net.geoprism.georegistry.service;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.constants.ClientRequestIF;

import net.geoprism.georegistry.io.GeoObjectConfiguration;
import net.geoprism.georegistry.shapefile.GeoObjectShapefileExporter;
import net.geoprism.registry.testframework.USATestData;

public class ShapefileServiceTest
{
  protected USATestData     tutil;

  protected ClientRequestIF adminCR;

  @Before
  public void setUp()
  {
    this.tutil = USATestData.newTestData();

    this.adminCR = tutil.adminClientRequest;
  }

  @After
  public void tearDown()
  {
    tutil.cleanUp();
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

    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(json.toString());

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

    Assert.assertEquals("geom", featureType.getGeometryDescriptor().getLocalName());

    List<AttributeDescriptor> attributes = featureType.getAttributeDescriptors();

    Assert.assertEquals( ( type.getAttributeMap().size() + 1 ), attributes.size());
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
