/**
 *
 */
package net.geoprism.registry.hierarchy;

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
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerParentTreeNode;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.service.business.HierarchyTypeBusinessServiceIF;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.USATestData;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class HierarchyChangeTest extends USADatasetTest implements InstanceTestClassListener
{
  @Autowired
  private GeoObjectBusinessServiceIF     objectService;

  @Autowired
  private HierarchyTypeBusinessServiceIF hierarchyService;

  @Before
  public void setUp()
  {
    testData.setUpInstanceData();

    testData.logIn(USATestData.USER_NPS_RA);
  }

  @After
  public void tearDown()
  {
    if (testData != null)
    {
      testData.logOut();

      testData.tearDownInstanceData();
    }
  }

  @Test
  @Request
  public void testRemoveFromHierarchyWithData()
  {
    ServerHierarchyType hierarchy = USATestData.HIER_ADMIN.getServerObject();
    ServerGeoObjectType country = USATestData.COUNTRY.getServerObject();
    ServerGeoObjectType state = USATestData.STATE.getServerObject();
    ServerGeoObjectType district = USATestData.DISTRICT.getServerObject();

    String cd1TypeCode = USATestData.CO_D_ONE.getCode();

    ServerGeoObjectIF geoobject = this.objectService.getGeoObjectByCode(cd1TypeCode, district);
    ServerParentTreeNode parents = this.objectService.getParentGeoObjects(geoobject, null, null, false, false, TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assert.assertEquals(1, parents.getParents().size());

    this.hierarchyService.removeChild(hierarchy, country, state, true);

    ServerGeoObjectIF test = this.objectService.getGeoObjectByCode(cd1TypeCode, district);
    ServerParentTreeNode tParents = this.objectService.getParentGeoObjects(test, null, null, false, false, TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assert.assertEquals(0, tParents.getParents().size());
  }
}
