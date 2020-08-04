package net.geoprism.registry.service;

import java.util.Date;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

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
    Assert.assertEquals(testData.HIER_ADMIN.getCode(), information.getHierarchy());
    Assert.assertEquals(testData.COUNTRY.getCode(), information.getChildType().getCode());
    Assert.assertEquals(1, information.getChildren().size());
    Assert.assertNull(information.getEntity());
    Assert.assertEquals(1, information.getHierarchies().size());
    Assert.assertEquals(1, information.getChildTypes().size());
  }

  @Test
  public void testGetLocationInformationWithParent()
  {
    LocationService service = new LocationService();
    LocationInformation information = service.getLocationInformation(testData.clientRequest.getSessionId(), testData.CAMBODIA.getCode(), testData.COUNTRY.getCode(), new Date(), testData.PROVINCE.getCode(), null);

    Assert.assertNotNull(information);
    Assert.assertEquals(testData.HIER_ADMIN.getCode(), information.getHierarchy());
    Assert.assertEquals(testData.PROVINCE.getCode(), information.getChildType().getCode());
    Assert.assertEquals(1, information.getChildren().size());
    Assert.assertEquals(testData.CAMBODIA.getCode(), information.getEntity().getCode());
    Assert.assertEquals(1, information.getHierarchies().size());
    Assert.assertEquals(1, information.getChildTypes().size());
  }

}
