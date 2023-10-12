/**
 *
 */
package net.geoprism.registry.hierarchy;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import com.runwaysdk.session.Request;

import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.conversion.VertexGeoObjectStrategy;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerParentTreeNode;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.USATestData;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class HierarchyChangeTest implements InstanceTestClassListener
{
  private static USATestData testData;

  @Before
  public void setUp()
  {
    testData = USATestData.newTestData();
    testData.setUpMetadata();

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

      testData.tearDownMetadata();
    }
  }

  @Test
  @Request
  public void testRemoveFromHierarchyWithData()
  {
    String sessionId = testData.clientSession.getSessionId();

    String hierarchyCode = USATestData.HIER_ADMIN.getCode();
    String countryTypeCode = USATestData.COUNTRY.getCode();
    String stateTypeCode = USATestData.STATE.getCode();

    String cd1TypeCode = USATestData.CO_D_ONE.getCode();

    ServerGeoObjectIF geoobject = new VertexGeoObjectStrategy(USATestData.DISTRICT.getServerObject()).getGeoObjectByCode(cd1TypeCode);
    ServerParentTreeNode parents = geoobject.getParentGeoObjects(null, null, false, false, TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assert.assertEquals(1, parents.getParents().size());

    ServiceFactory.getHierarchyService().removeFromHierarchy(sessionId, hierarchyCode, countryTypeCode, stateTypeCode, true);

    ServerGeoObjectIF test = new VertexGeoObjectStrategy(USATestData.DISTRICT.getServerObject()).getGeoObjectByCode(cd1TypeCode);
    ServerParentTreeNode tParents = test.getParentGeoObjects(null, null, false, false, TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assert.assertEquals(0, tParents.getParents().size());
  }
}
