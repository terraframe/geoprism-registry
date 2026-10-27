/**
 *
 */
package net.geoprism.registry.io;

import java.util.List;
import java.util.Map;

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
import net.geoprism.registry.model.LocationInfo;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.test.USATestData;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@ExtendWith(InstanceTestClassExtension.class)
public class GeoObjectUtilTest extends USADatasetTest implements InstanceTestClassListener
{
  @Autowired
  private GeoObjectBusinessServiceIF     objectService;

  @Autowired
  private GeoObjectTypeBusinessServiceIF typeService;

  @Test
  @Request
  public void testGetAncestorMapForTreeType()
  {
    ServerGeoObjectType type = USATestData.AREA.getServerObject();
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    ServerGeoObjectIF object = this.objectService.getGeoObjectByCode(USATestData.CO_A_ONE.getCode(), type);

    List<ServerGeoObjectType> ancestors = this.typeService.getTypeAncestors(type, hierarchyType, true);

    Map<String, LocationInfo> map = this.objectService.getAncestorMap(object, hierarchyType, ancestors);

    Assertions.assertEquals(3, map.size());

    // Validate the county values
    Assertions.assertTrue(map.containsKey(USATestData.COUNTY.getCode()));

    LocationInfo vObject = map.get(USATestData.COUNTY.getCode());

    Assertions.assertEquals(USATestData.CO_C_ONE.getCode(), vObject.getCode());
    Assertions.assertEquals(USATestData.CO_C_ONE.getDisplayLabel(), vObject.getLabel());

    // Validate the state values
    Assertions.assertTrue(map.containsKey(USATestData.STATE.getCode()));

    vObject = map.get(USATestData.STATE.getCode());

    Assertions.assertEquals(USATestData.COLORADO.getCode(), vObject.getCode());
    Assertions.assertEquals(USATestData.COLORADO.getDisplayLabel(), vObject.getLabel());

    // Validate the country values
    Assertions.assertTrue(map.containsKey(USATestData.COUNTRY.getCode()));

    vObject = map.get(USATestData.COUNTRY.getCode());

    Assertions.assertEquals(USATestData.USA.getCode(), vObject.getCode());
    Assertions.assertEquals(USATestData.USA.getDisplayLabel(), vObject.getLabel());
  }
}
