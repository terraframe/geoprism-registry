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
import net.geoprism.registry.model.RootGeoObjectType;
import net.geoprism.registry.model.ServerChildTreeNode;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.service.business.HierarchyTypeBusinessServiceIF;
import net.geoprism.registry.test.USATestData;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class BasicHierarchyTest implements InstanceTestClassListener
{
  private static ServerGeoObjectType     parent;

  private static ServerGeoObjectType     child;

  private static ServerHierarchyType     hierarchyType;

  @Autowired
  private GeoObjectTypeBusinessServiceIF gTypeService;

  @Autowired
  private HierarchyTypeBusinessServiceIF hTypeService;

  @Autowired
  private GeoObjectBusinessServiceIF     service;

  @Override
  @Request
  public void beforeClassSetup() throws Exception
  {
    USATestData.ORG_NPS.apply();

    parent = this.gTypeService.create(USATestData.COUNTRY.toDTO());
    child = this.gTypeService.create(USATestData.STATE.toDTO());
    hierarchyType = this.hTypeService.createHierarchyType(USATestData.HIER_ADMIN.toDTO());
    
    this.hTypeService.addToHierarchy(hierarchyType, RootGeoObjectType.INSTANCE, parent);
    this.hTypeService.addToHierarchy(hierarchyType, parent, child);
  }

  @Override
  @Request
  public void afterClassSetup() throws Exception
  {
    this.hTypeService.delete(hierarchyType);
    this.gTypeService.deleteGeoObjectType(child.getCode());
    this.gTypeService.deleteGeoObjectType(parent.getCode());

    USATestData.ORG_NPS.delete();
  }

  @Test
  @Request
  public void testAddChild()
  {
    ServerGeoObjectIF parent = USATestData.USA.apply();

    try
    {
      ServerGeoObjectIF child = USATestData.COLORADO.apply();

      try
      {
        this.service.addChild(parent, child, hierarchyType, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);

        ServerChildTreeNode objects = this.service.getChildGeoObjects(parent, hierarchyType, null, false, USATestData.DEFAULT_OVER_TIME_DATE);

        Assert.assertEquals(1, objects.getChildren().size());
      }
      finally
      {
        child.delete();
      }

    }
    finally
    {
      parent.delete();
    }
  }
}
