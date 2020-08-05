package net.geoprism.registry.service;

import java.util.Date;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.DefaultSerializer;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.view.LocationInformation;

public abstract class AbstractLocationServiceTest
{
  protected static FastTestDataset testData;

  @AfterClass
  public static void cleanUpClass()
  {
    testData.logOut();
    
    testData.tearDownMetadata();
  }

  @Test
  public void testGetLocationInformationNullTypeAndHierarchy()
  {
    LocationService service = new LocationService();
    LocationInformation information = service.getLocationInformation(testData.clientRequest.getSessionId(), new Date(), null, null);

    Assert.assertNotNull(information);
    Assert.assertEquals(FastTestDataset.HIER_ADMIN.getCode(), information.getHierarchy());
    Assert.assertEquals(FastTestDataset.COUNTRY.getCode(), information.getChildType().getCode());
    Assert.assertEquals(1, information.getChildren().size());
    Assert.assertNull(information.getEntity());
    Assert.assertEquals(1, information.getHierarchies().size());
    Assert.assertEquals(1, information.getChildTypes().size());
  }

  @Test
  public void testGetLocationInformationWithParent()
  {
    LocationService service = new LocationService();
    LocationInformation information = service.getLocationInformation(testData.clientRequest.getSessionId(), FastTestDataset.CAMBODIA.getCode(), FastTestDataset.COUNTRY.getCode(), new Date(), FastTestDataset.PROVINCE.getCode(), null);

    Assert.assertNotNull(information);
    Assert.assertEquals(FastTestDataset.HIER_ADMIN.getCode(), information.getHierarchy());
    Assert.assertEquals(FastTestDataset.PROVINCE.getCode(), information.getChildType().getCode());
    Assert.assertEquals(1, information.getChildren().size());
    Assert.assertEquals(FastTestDataset.CAMBODIA.getCode(), information.getEntity().getCode());
    Assert.assertEquals(1, information.getHierarchies().size());
    Assert.assertEquals(1, information.getChildTypes().size());
  }

  @Test
  public void testSerialize()
  {
    LocationService service = new LocationService();
    LocationInformation information = service.getLocationInformation(testData.clientRequest.getSessionId(), FastTestDataset.CAMBODIA.getCode(), FastTestDataset.COUNTRY.getCode(), new Date(), FastTestDataset.PROVINCE.getCode(), null);

    JsonObject response = (JsonObject) information.toJson(new DefaultSerializer());

    Assert.assertNotNull(response);
    Assert.assertEquals(FastTestDataset.HIER_ADMIN.getCode(), response.get("hierarchy").getAsString());
    Assert.assertEquals(FastTestDataset.PROVINCE.getCode(), response.get("childType").getAsString());
    Assert.assertEquals(1, response.get("hierarchies").getAsJsonArray().size());
    Assert.assertEquals(1, response.get("types").getAsJsonArray().size());

    JsonObject geojson = response.get("geojson").getAsJsonObject();
    JsonArray features = geojson.get("features").getAsJsonArray();

    Assert.assertEquals(1, features.size());

    JsonObject entity = response.get("entity").getAsJsonObject();

    Assert.assertNotNull(entity);
    Assert.assertEquals(FastTestDataset.CAMBODIA.getCode(), entity.get("properties").getAsJsonObject().get("code").getAsString());
  }

}
