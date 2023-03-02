/**
 *
 */
package net.geoprism.registry.io;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.session.Request;

import net.geoprism.registry.model.LocationInfo;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.ServerGeoObjectService;
import net.geoprism.registry.test.USATestData;

public class GeoObjectUtilTest
{
  private static USATestData testData;

  @BeforeClass
  public static void setUpClass()
  {
    testData = USATestData.newTestData();
    testData.setUpMetadata();
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
    testData.setUpInstanceData();

    testData.logIn();
  }

  @After
  public void tearDown()
  {
    testData.logOut();

    testData.tearDownInstanceData();
  }

  @Test
  @Request
  public void testGetAncestorMapForTreeType()
  {
    ServerGeoObjectType type = USATestData.AREA.getServerObject();
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    ServerGeoObjectIF object = new ServerGeoObjectService().getGeoObjectByCode(USATestData.CO_A_ONE.getCode(), type);
    
    List<ServerGeoObjectType> ancestors = type.getTypeAncestors(hierarchyType, true);

    Map<String, LocationInfo> map = object.getAncestorMap(hierarchyType, ancestors);

    Assert.assertEquals(3, map.size());

    // Validate the county values
    Assert.assertTrue(map.containsKey(USATestData.COUNTY.getCode()));

    LocationInfo vObject = map.get(USATestData.COUNTY.getCode());

    Assert.assertEquals(USATestData.CO_C_ONE.getCode(), vObject.getCode());
    Assert.assertEquals(USATestData.CO_C_ONE.getDisplayLabel(), vObject.getLabel());

    // Validate the state values
    Assert.assertTrue(map.containsKey(USATestData.STATE.getCode()));

    vObject = map.get(USATestData.STATE.getCode());

    Assert.assertEquals(USATestData.COLORADO.getCode(), vObject.getCode());
    Assert.assertEquals(USATestData.COLORADO.getDisplayLabel(), vObject.getLabel());

    // Validate the country values
    Assert.assertTrue(map.containsKey(USATestData.COUNTRY.getCode()));

    vObject = map.get(USATestData.COUNTRY.getCode());

    Assert.assertEquals(USATestData.USA.getCode(), vObject.getCode());
    Assert.assertEquals(USATestData.USA.getDisplayLabel(), vObject.getLabel());
  }
}
