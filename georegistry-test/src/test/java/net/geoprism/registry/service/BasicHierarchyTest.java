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
import net.geoprism.registry.model.ServerParentTreeNode;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.service.business.HierarchyTypeBusinessServiceIF;
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

  protected USATestData                  testData;

  @Override
  @Request
  public void beforeClassSetup() throws Exception
  {
    testData = USATestData.newTestData();

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
    testData.tearDownMetadata();
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
        String uid = UUID.randomUUID().toString();

        this.service.addChild(parent, child, hierarchyType, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE, uid, USATestData.SOURCE.getDataSource(), false);

        ServerChildTreeNode objects = this.service.getChildGeoObjects(parent, hierarchyType, null, false, USATestData.DEFAULT_OVER_TIME_DATE);

        Assert.assertEquals(1, objects.getChildren().size());

        ServerChildTreeNode node = objects.getChildren().get(0);

        Assert.assertNotNull(node.getSource());
        Assert.assertEquals(uid, node.getUid());
        Assert.assertEquals(child.getCode(), node.getGeoObject().getCode());        
      }
      finally
      {
        USATestData.COLORADO.delete();
      }
    }
    finally
    {
      USATestData.USA.delete();
    }
  }
  
  @Test
  @Request
  public void testAddParent()
  {
    ServerGeoObjectIF parent = USATestData.USA.apply();
    
    try
    {
      ServerGeoObjectIF child = USATestData.COLORADO.apply();
      
      try
      {
        String uid = UUID.randomUUID().toString();
        
        this.service.addParent(child, parent, hierarchyType, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE, uid, USATestData.SOURCE.getDataSource(), false);
        
        ServerParentTreeNode objects = this.service.getParentGeoObjects(child, hierarchyType, null, false, false, USATestData.DEFAULT_OVER_TIME_DATE);
        
        Assert.assertEquals(1, objects.getParents().size());
        
        ServerParentTreeNode node = objects.getParents().get(0);
        
        Assert.assertNotNull(node.getSource());
        Assert.assertEquals(uid, node.getUid());
        Assert.assertEquals(parent.getCode(), node.getGeoObject().getCode());
      }
      finally
      {
        USATestData.COLORADO.delete();
      }
    }
    finally
    {
      USATestData.USA.delete();
    }
  }
}
