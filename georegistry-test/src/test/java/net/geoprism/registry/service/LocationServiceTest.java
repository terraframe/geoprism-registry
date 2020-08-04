package net.geoprism.registry.service;

import java.util.Date;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.SessionFacade;

import net.geoprism.registry.test.USATestData;
import net.geoprism.registry.view.LocationInformation;

public class LocationServiceTest
{
  private static USATestData testData;

  @BeforeClass
  public static void setUpClass()
  {
    testData = USATestData.newTestData();
    testData.setSessionUser(testData.USER_NPS_RA);
    testData.setUpMetadata();

    reload();
  }

  @AfterClass
  public static void cleanUpClass()
  {
    if (testData != null)
    {
      testData.tearDownMetadata();
    }
  }

  @Before
  public void setUp()
  {
    if (testData != null)
    {
      testData.setUpInstanceData();
    }
  }

  @After
  public void tearDown()
  {
    if (testData != null)
    {
      testData.tearDownInstanceData();
    }
  }

  @Request
  public static void reload()
  {
    /*
     * Reload permissions for the new attributes
     */
    SessionFacade.getSessionForRequest(testData.clientRequest.getSessionId()).reloadPermissions();
  }

  @Test
  public void testGetLocationInformationNullTypeAndHierarchy()
  {
    LocationService service = new LocationService();
    LocationInformation information = service.getLocationInformation(testData.clientRequest.getSessionId(), new Date(), null, null);

    Assert.assertNotNull(information);
    Assert.assertEquals(testData.HIER_ADMIN.getCode(), information.getHierarchy());
    Assert.assertEquals(testData.COUNTRY.getCode(), information.getChildType().getCode());
    Assert.assertEquals(3, information.getChildren().size());
    Assert.assertNull(information.getEntity());
    Assert.assertEquals(1, information.getHierarchies().size());
    Assert.assertEquals(1, information.getChildTypes().size());
  }

  @Test
  public void testGetLocationInformationWithParent()
  {
    LocationService service = new LocationService();
    LocationInformation information = service.getLocationInformation(testData.clientRequest.getSessionId(), testData.USA.getCode(), testData.COUNTRY.getCode(), new Date(), testData.STATE.getCode(), null);

    Assert.assertNotNull(information);
    Assert.assertEquals(testData.HIER_ADMIN.getCode(), information.getHierarchy());
    Assert.assertEquals(testData.STATE.getCode(), information.getChildType().getCode());
    Assert.assertEquals(2, information.getChildren().size());
    Assert.assertEquals(testData.USA.getCode(), information.getEntity().getCode());
    Assert.assertEquals(1, information.getHierarchies().size());
    Assert.assertEquals(2, information.getChildTypes().size());
  }

}
