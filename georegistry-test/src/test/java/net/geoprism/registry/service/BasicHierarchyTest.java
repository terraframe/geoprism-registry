/**
 *
 */
package net.geoprism.registry.service;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.session.Request;

import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.model.RootGeoObjectType;
import net.geoprism.registry.model.ServerChildTreeNode;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.service.business.HierarchyTypeBusinessServiceIF;
import net.geoprism.registry.service.business.SourceBusinessServiceIF;
import net.geoprism.registry.test.USATestData;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
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
    USATestData.SOURCE.apply();

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

    USATestData.SOURCE.delete();
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
        this.service.addChild(parent, child, hierarchyType, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE, UUID.randomUUID().toString(), false);

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
