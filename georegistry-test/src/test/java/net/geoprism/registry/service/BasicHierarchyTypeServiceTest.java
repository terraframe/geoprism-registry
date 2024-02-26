/**
 *
 */
package net.geoprism.registry.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.runwaysdk.session.Request;

import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.service.business.HierarchyTypeBusinessServiceIF;
import net.geoprism.registry.test.USATestData;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class BasicHierarchyTypeServiceTest implements InstanceTestClassListener
{
  private static ServerGeoObjectType     parent;

  private static ServerGeoObjectType     child;

  private static ServerGeoObjectType     grandChild;

  @Autowired
  private GeoObjectTypeBusinessServiceIF gTypeService;

  @Autowired
  private HierarchyTypeBusinessServiceIF service;

  @Override
  @Request
  public void beforeClassSetup() throws Exception
  {
    USATestData.ORG_NPS.apply();

    parent = this.gTypeService.create(USATestData.COUNTRY.toDTO());
    child = this.gTypeService.create(USATestData.STATE.toDTO());
    grandChild = this.gTypeService.create(USATestData.DISTRICT.toDTO());

  }

  @Override
  @Request
  public void afterClassSetup() throws Exception
  {
    this.gTypeService.deleteGeoObjectType(grandChild.getCode());
    this.gTypeService.deleteGeoObjectType(child.getCode());
    this.gTypeService.deleteGeoObjectType(parent.getCode());

    USATestData.ORG_NPS.delete();
  }

  @Test
  @Request
  public void testCreateDeleteHierarchyType()
  {
    ServerHierarchyType hierarchyType = this.service.createHierarchyType(USATestData.HIER_ADMIN.toDTO());

    try
    {
      ServerHierarchyType test = this.service.get(hierarchyType.getCode());

      Assert.assertNotNull(test);
    }
    finally
    {
      this.service.delete(hierarchyType);
    }
  }
}
