/**
 *
 */
package net.geoprism.registry.io;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.runwaysdk.session.Request;

import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.USADatasetTest;
import net.geoprism.registry.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.model.LocationInfo;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.test.USATestData;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class GeoObjectUtilTest extends USADatasetTest implements InstanceTestClassListener
{
  @Autowired
  private GeoObjectBusinessServiceIF     objectService;

  @Autowired
  private GeoObjectTypeBusinessServiceIF typeService;

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

    ServerGeoObjectIF object = this.objectService.getGeoObjectByCode(USATestData.CO_A_ONE.getCode(), type);

    List<ServerGeoObjectType> ancestors = this.typeService.getTypeAncestors(type, hierarchyType, true);

    Map<String, LocationInfo> map = this.objectService.getAncestorMap(object, hierarchyType, ancestors);

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
