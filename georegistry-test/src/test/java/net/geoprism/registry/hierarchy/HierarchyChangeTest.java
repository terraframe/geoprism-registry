/**
 *
 */
package net.geoprism.registry.hierarchy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.session.Request;

import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.InstanceTestClassExtension;
import net.geoprism.registry.USADatasetTest;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerParentTreeNode;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.service.business.HierarchyTypeBusinessServiceIF;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.USATestData;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@ExtendWith(InstanceTestClassExtension.class)
public class HierarchyChangeTest extends USADatasetTest implements InstanceTestClassListener
{
  @Autowired
  private GeoObjectBusinessServiceIF     objectService;

  @Autowired
  private HierarchyTypeBusinessServiceIF hierarchyService;

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

    Assertions.assertEquals(1, parents.getParents().size());

    this.hierarchyService.removeChild(hierarchy, country, state, true);

    ServerGeoObjectIF test = this.objectService.getGeoObjectByCode(cd1TypeCode, district);
    ServerParentTreeNode tParents = this.objectService.getParentGeoObjects(test, null, null, false, false, TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assertions.assertEquals(0, tParents.getParents().size());
  }
}
