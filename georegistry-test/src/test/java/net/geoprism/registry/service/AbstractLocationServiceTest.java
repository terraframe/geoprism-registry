/**
 *
 */
package net.geoprism.registry.service;

import org.junit.AfterClass;

import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.test.FastTestDataset;

public abstract class AbstractLocationServiceTest implements InstanceTestClassListener
{
  protected static FastTestDataset testData;

  @AfterClass
  public static void cleanUpClass()
  {
    testData.logOut();
    
    testData.tearDownMetadata();
  }
  
  private int getNumProvinces()
  {
    return FastTestDataset.CAMBODIA.getChildren().size();
  }

  // As far as I can tell this isn't being used anywhere anymore
//  @Test
//  public void testGetLocationInformationNullTypeAndHierarchy()
//  {
//    LocationService service = new LocationService();
//    LocationInformation information = service.getLocationInformation(testData.clientRequest.getSessionId(), new Date(), null, null);
//
//    Assert.assertNotNull(information);
//    Assert.assertEquals(FastTestDataset.HIER_ADMIN.getCode(), information.getHierarchy());
//    Assert.assertEquals(FastTestDataset.COUNTRY.getCode(), information.getChildType().getCode());
//    Assert.assertEquals(1, information.getChildren().size());
//    Assert.assertNull(information.getEntity());
//    Assert.assertEquals(testData.getManagedHierarchyTypes().size(), information.getHierarchies().size());
//    Assert.assertEquals(1, information.getChildTypes().size());
//  }

//  @Test
//  public void testGetLocationInformationWithParent()
//  {
//    LocationService service = new LocationService();
//    LocationInformation information = service.getLocationInformation(testData.clientRequest.getSessionId(), FastTestDataset.CAMBODIA.getCode(), FastTestDataset.COUNTRY.getCode(), new Date(), FastTestDataset.PROVINCE.getCode(), null);
//
//    Assert.assertNotNull(information);
//    Assert.assertEquals(FastTestDataset.HIER_ADMIN.getCode(), information.getHierarchy());
//    Assert.assertEquals(FastTestDataset.PROVINCE.getCode(), information.getChildType().getCode());
//    Assert.assertEquals(1, information.getChildren().size());
//    Assert.assertEquals(FastTestDataset.CAMBODIA.getCode(), information.getEntity().getCode());
//    Assert.assertEquals(testData.getManagedHierarchyTypes().size(), information.getHierarchies().size());
//    Assert.assertEquals(getNumProvinces(), information.getChildTypes().size());
//  }

//  @Test
//  public void testSerialize()
//  {
//    LocationService service = new LocationService();
//    LocationInformation information = service.getLocationInformation(testData.clientRequest.getSessionId(), FastTestDataset.CAMBODIA.getCode(), FastTestDataset.COUNTRY.getCode(), new Date(), FastTestDataset.PROVINCE.getCode(), null);
//
//    JsonObject response = (JsonObject) information.toJson(new DefaultSerializer());
//
//    Assert.assertNotNull(response);
//    Assert.assertEquals(FastTestDataset.HIER_ADMIN.getCode(), response.get("hierarchy").getAsString());
//    Assert.assertEquals(FastTestDataset.PROVINCE.getCode(), response.get("childType").getAsString());
//    Assert.assertEquals(testData.getManagedHierarchyTypes().size(), response.get("hierarchies").getAsJsonArray().size());
//    Assert.assertEquals(getNumProvinces(), response.get("types").getAsJsonArray().size());
//
//    JsonObject geojson = response.get("geojson").getAsJsonObject();
//    JsonArray features = geojson.get("features").getAsJsonArray();
//
//    Assert.assertEquals(1, features.size());
//
//    JsonObject entity = response.get("entity").getAsJsonObject();
//
//    Assert.assertNotNull(entity);
//    Assert.assertEquals(FastTestDataset.CAMBODIA.getCode(), entity.get("properties").getAsJsonObject().get("code").getAsString());
//  }

}
