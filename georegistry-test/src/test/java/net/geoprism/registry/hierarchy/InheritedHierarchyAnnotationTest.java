/**
 *
 */
package net.geoprism.registry.hierarchy;

import java.util.List;

import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyNode;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.runwaysdk.session.Request;

import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.HierarchyRootException;
import net.geoprism.registry.InheritedHierarchyAnnotation;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.TypeInUseException;
import net.geoprism.registry.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.business.HierarchyTypeBusinessServiceIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.GPRHierarchyTypeService;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestGeoObjectTypeInfo;
import net.geoprism.registry.test.TestHierarchyTypeInfo;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class InheritedHierarchyAnnotationTest extends FastDatasetTest implements InstanceTestClassListener
{
  public static final TestGeoObjectTypeInfo TEST_CHILD        = new TestGeoObjectTypeInfo("HMST_PROVINCE", FastTestDataset.ORG_CGOV);

  public static final TestHierarchyTypeInfo TEST_HT           = new TestHierarchyTypeInfo("INHAT", FastTestDataset.ORG_CGOV);

  public static final TestGeoObjectTypeInfo TEST_DELETE_CHILD = new TestGeoObjectTypeInfo("INHAT_CHILD_GOT", FastTestDataset.ORG_CGOV);

  public static final TestHierarchyTypeInfo TEST_CHILD_HT     = new TestHierarchyTypeInfo("IHAT_Child_HT", FastTestDataset.ORG_CGOV);

  public static final TestHierarchyTypeInfo TEST_PARENT_HT    = new TestHierarchyTypeInfo("IHAT_Parent_HT", FastTestDataset.ORG_CGOV);

  @Autowired
  private GPRHierarchyTypeService           service;

  @Autowired
  private HierarchyTypeBusinessServiceIF    hierarchyService;

  @Autowired
  private GeoObjectTypeBusinessServiceIF    typeService;

  @Override
  @Request
  public void beforeClassSetup() throws Exception
  {
    super.beforeClassSetup();

    TEST_CHILD.apply();
    TEST_HT.apply();

    TEST_HT.setRoot(FastTestDataset.PROVINCE);

    this.hierarchyService.addToHierarchy(TEST_HT.getServerObject(), FastTestDataset.PROVINCE.getServerObject(), TEST_CHILD.getServerObject());
  }

  @Override
  @Request
  public void afterClassSetup() throws Exception
  {
    this.hierarchyService.removeChild(TEST_HT.getServerObject(), FastTestDataset.PROVINCE.getServerObject(), TEST_CHILD.getServerObject(), true);
    TEST_HT.removeRoot(FastTestDataset.PROVINCE);

    TEST_HT.delete();
    TEST_CHILD.delete();

    super.afterClassSetup();
  }

  @After
  public void cleanUp()
  {
    TEST_CHILD_HT.delete();
    TEST_PARENT_HT.delete();
    TEST_DELETE_CHILD.delete();
  }

  @Test
  @Request
  public void testCreate()
  {
    ServerGeoObjectType sGOT = FastTestDataset.PROVINCE.getServerObject();
    InheritedHierarchyAnnotation annotation = this.typeService.setInheritedHierarchy(sGOT, TEST_HT.getServerObject(), FastTestDataset.HIER_ADMIN.getServerObject());

    try
    {
      Assert.assertNotNull(annotation);
    }
    finally
    {
      annotation.delete();
    }
  }

  @Test
  @Request
  public void testGetByUniversal()
  {
    ServerGeoObjectType sGOT = FastTestDataset.PROVINCE.getServerObject();
    InheritedHierarchyAnnotation annotation = this.typeService.setInheritedHierarchy(sGOT, TEST_HT.getServerObject(), FastTestDataset.HIER_ADMIN.getServerObject());

    try
    {
      List<? extends InheritedHierarchyAnnotation> annotations = InheritedHierarchyAnnotation.getByUniversal(sGOT.getUniversal());

      Assert.assertEquals(2, annotations.size());

      for (InheritedHierarchyAnnotation dbanno : annotations)
      {
        String forHierCode = dbanno.getForHierarchicalRelationshipType().getCode();

        if (forHierCode.equals(TEST_HT.getCode()))
        {
          Assert.assertEquals(dbanno.getOid(), annotation.getOid());
        }
        else if (forHierCode.equals(FastTestDataset.HIER_SPLIT_CHILD.getCode()))
        {
          continue;
        }
        else
        {
          throw new UnsupportedOperationException("Unexpected inherited hierarchy with code [" + forHierCode + "].");
        }
      }
    }
    finally
    {
      annotation.delete();
    }
  }

  @Test
  @Request
  public void testGetByForHierarchy()
  {
    ServerGeoObjectType sGOT = FastTestDataset.PROVINCE.getServerObject();
    ServerHierarchyType forHierarchy = TEST_HT.getServerObject();
    ServerHierarchyType inheritedHierarchy = FastTestDataset.HIER_ADMIN.getServerObject();

    InheritedHierarchyAnnotation annotation = this.typeService.setInheritedHierarchy(sGOT, forHierarchy, inheritedHierarchy);

    try
    {
      List<? extends InheritedHierarchyAnnotation> annotations = InheritedHierarchyAnnotation.getByRelationship(forHierarchy.getHierarchicalRelationshipType());

      Assert.assertEquals(1, annotations.size());

      InheritedHierarchyAnnotation test = annotations.get(0);

      Assert.assertEquals(test.getOid(), annotation.getOid());
    }
    finally
    {
      annotation.delete();
    }
  }

  @Test
  @Request
  public void testGetByInheritedHierarchy()
  {
    ServerGeoObjectType sGOT = FastTestDataset.PROVINCE.getServerObject();
    ServerHierarchyType forHierarchy = TEST_HT.getServerObject();
    ServerHierarchyType inheritedHierarchy = FastTestDataset.HIER_ADMIN.getServerObject();

    InheritedHierarchyAnnotation annotation = this.typeService.setInheritedHierarchy(sGOT, forHierarchy, inheritedHierarchy);

    try
    {
      List<? extends InheritedHierarchyAnnotation> annotations = InheritedHierarchyAnnotation.getByRelationship(inheritedHierarchy.getHierarchicalRelationshipType());

      Assert.assertEquals(1, annotations.size());

      InheritedHierarchyAnnotation test = annotations.get(0);

      Assert.assertEquals(test.getOid(), annotation.getOid());
    }
    finally
    {
      annotation.delete();
    }
  }

  @Test
  @Request
  public void testGetByUniversalAndHierarchy()
  {
    ServerGeoObjectType sGOT = FastTestDataset.PROVINCE.getServerObject();
    ServerHierarchyType forHierarchy = TEST_HT.getServerObject();
    ServerHierarchyType inheritedHierarchy = FastTestDataset.HIER_ADMIN.getServerObject();

    InheritedHierarchyAnnotation annotation = this.typeService.setInheritedHierarchy(sGOT, forHierarchy, inheritedHierarchy);

    try
    {
      InheritedHierarchyAnnotation test = InheritedHierarchyAnnotation.get(sGOT.getUniversal(), forHierarchy.getHierarchicalRelationshipType());

      Assert.assertNotNull(test);
      Assert.assertEquals(test.getOid(), annotation.getOid());
    }
    finally
    {
      annotation.delete();
    }
  }

  @Test
  @Request
  public void testRemove()
  {
    ServerGeoObjectType sGOT = FastTestDataset.PROVINCE.getServerObject();
    ServerHierarchyType forHierarchy = TEST_HT.getServerObject();
    ServerHierarchyType inheritedHierarchy = FastTestDataset.HIER_ADMIN.getServerObject();

    this.typeService.setInheritedHierarchy(sGOT, forHierarchy, inheritedHierarchy);
    this.typeService.removeInheritedHierarchy(forHierarchy);

    InheritedHierarchyAnnotation test = InheritedHierarchyAnnotation.get(sGOT.getUniversal(), forHierarchy.getHierarchicalRelationshipType());

    try
    {
      Assert.assertNull(test);
    }
    finally
    {
      if (test != null)
      {
        test.delete();
      }
    }
  }

  /**
   * Tests to make sure that if we have a hierarchy A which is inherited by
   * hierarchy B, if we delete A then the inherit relationship needs to also be
   * deleted.
   */
  @Test
  @Request
  public void testDeleteParentType()
  {
    TEST_DELETE_CHILD.apply();
    TEST_CHILD_HT.apply();
    TEST_PARENT_HT.apply();
    TEST_CHILD_HT.setRoot(TEST_DELETE_CHILD);

    ServerGeoObjectType sGOT = TEST_DELETE_CHILD.getServerObject();
    ServerHierarchyType forHierarchy = TEST_CHILD_HT.getServerObject();
    ServerHierarchyType inheritedHierarchy = TEST_PARENT_HT.getServerObject();

    this.typeService.setInheritedHierarchy(sGOT, forHierarchy, inheritedHierarchy);

    InheritedHierarchyAnnotation test = InheritedHierarchyAnnotation.get(sGOT.getUniversal(), forHierarchy.getHierarchicalRelationshipType());

    try
    {
      Assert.assertNotNull(test);

      TEST_PARENT_HT.delete();

      Assert.assertNull(InheritedHierarchyAnnotation.get(sGOT.getUniversal(), forHierarchy.getHierarchicalRelationshipType()));

      test = null;
    }
    finally
    {
      if (test != null)
      {
        test.delete();
      }
    }
  }

  /**
   * Tests to make sure that if we have a hierarchy A which is inherited by
   * hierarchy B, if we delete B then the inherit relationship needs to also be
   * deleted.
   */
  @Test
  @Request
  public void testDeleteChildType()
  {
    TEST_DELETE_CHILD.apply();
    TEST_CHILD_HT.apply();
    TEST_PARENT_HT.apply();
    TEST_CHILD_HT.setRoot(TEST_DELETE_CHILD);

    ServerGeoObjectType sGOT = TEST_DELETE_CHILD.getServerObject();
    ServerHierarchyType forHierarchy = TEST_CHILD_HT.getServerObject();
    ServerHierarchyType inheritedHierarchy = TEST_PARENT_HT.getServerObject();

    this.typeService.setInheritedHierarchy(sGOT, forHierarchy, inheritedHierarchy);

    List<? extends InheritedHierarchyAnnotation> test = InheritedHierarchyAnnotation.getByInheritedHierarchy(sGOT.getUniversal(), inheritedHierarchy.getHierarchicalRelationshipType());

    try
    {
      Assert.assertTrue(test != null && test.size() == 1);

      TEST_CHILD_HT.delete();

      test = InheritedHierarchyAnnotation.getByInheritedHierarchy(sGOT.getUniversal(), inheritedHierarchy.getHierarchicalRelationshipType());

      Assert.assertTrue(test != null && test.size() == 0);

      test = null;
    }
    finally
    {
      if (test != null)
      {
        test.stream().forEach(annot -> annot.delete());
      }
    }
  }

  /**
   * Tests to make sure that if we have a hierarchy A which is inherited by
   * hierarchy B, if we delete the GeoObjectType, then the annotation is also
   * deleted.
   */
  @Test(expected = TypeInUseException.class)
  @Request
  public void testDeleteGeoObjectType()
  {
    TEST_DELETE_CHILD.apply();
    TEST_CHILD_HT.apply();
    TEST_PARENT_HT.apply();
    TEST_CHILD_HT.setRoot(TEST_DELETE_CHILD);

    ServerGeoObjectType sGOT = TEST_DELETE_CHILD.getServerObject();
    ServerHierarchyType forHierarchy = TEST_CHILD_HT.getServerObject();
    ServerHierarchyType inheritedHierarchy = TEST_PARENT_HT.getServerObject();

    this.typeService.setInheritedHierarchy(sGOT, forHierarchy, inheritedHierarchy);

    List<? extends InheritedHierarchyAnnotation> test = InheritedHierarchyAnnotation.getAnnotationByHierarchies(forHierarchy.getHierarchicalRelationshipType(), inheritedHierarchy.getHierarchicalRelationshipType());

    try
    {
      Assert.assertTrue(test != null && test.size() == 1);

      TEST_DELETE_CHILD.delete();

      // test =
      // InheritedHierarchyAnnotation.getAnnotationByHierarchies(forHierarchy.getHierarchicalRelationshipType(),
      // inheritedHierarchy.getHierarchicalRelationshipType());
      //
      // Assert.assertTrue(test != null && test.size() == 0);
      //
      // test = null;
    }
    finally
    {
      if (test != null)
      {
        test.stream().forEach(annot -> annot.delete());
      }
    }
  }

  @Request
  @Test(expected = HierarchyRootException.class)
  public void testCreateOnNonRoot()
  {
    ServerGeoObjectType sGOT = TEST_CHILD.getServerObject();
    this.typeService.setInheritedHierarchy(sGOT, TEST_HT.getServerObject(), FastTestDataset.HIER_ADMIN.getServerObject());
  }

  @Test
  @Request
  public void testGetTypeAncestorsInherited()
  {
    ServerGeoObjectType sGOT = FastTestDataset.PROVINCE.getServerObject();
    ServerHierarchyType forHierarchy = TEST_HT.getServerObject();
    ServerHierarchyType inheritedHierarchy = FastTestDataset.HIER_ADMIN.getServerObject();

    InheritedHierarchyAnnotation annotation = this.typeService.setInheritedHierarchy(sGOT, forHierarchy, inheritedHierarchy);

    try
    {
      ServerGeoObjectType childType = TEST_CHILD.getServerObject();

      List<ServerGeoObjectType> results = this.typeService.getTypeAncestors(childType, TEST_HT.getServerObject(), true);

      Assert.assertEquals(2, results.size());
    }
    finally
    {
      annotation.delete();
    }
  }

  @Test
  @Request
  public void testGetTypeAncestors()
  {
    ServerGeoObjectType sGOT = FastTestDataset.PROVINCE.getServerObject();
    ServerHierarchyType forHierarchy = TEST_HT.getServerObject();
    ServerHierarchyType inheritedHierarchy = FastTestDataset.HIER_ADMIN.getServerObject();

    InheritedHierarchyAnnotation annotation = this.typeService.setInheritedHierarchy(sGOT, forHierarchy, inheritedHierarchy);

    try
    {
      ServerGeoObjectType childType = TEST_CHILD.getServerObject();

      List<ServerGeoObjectType> results = this.typeService.getTypeAncestors(childType, TEST_HT.getServerObject(), false);

      Assert.assertEquals(1, results.size());
    }
    finally
    {
      annotation.delete();
    }
  }

  @Test
  @Request
  public void testFindHierarchy()
  {
    ServerGeoObjectType sGOT = FastTestDataset.PROVINCE.getServerObject();
    ServerHierarchyType forHierarchy = TEST_HT.getServerObject();
    ServerHierarchyType inheritedHierarchy = FastTestDataset.HIER_ADMIN.getServerObject();

    InheritedHierarchyAnnotation annotation = this.typeService.setInheritedHierarchy(sGOT, forHierarchy, inheritedHierarchy);

    try
    {
      ServerGeoObjectType childType = TEST_CHILD.getServerObject();

      ServerHierarchyType hierarchy = this.typeService.findHierarchy(childType, forHierarchy, FastTestDataset.COUNTRY.getServerObject());

      Assert.assertEquals(FastTestDataset.HIER_ADMIN.getCode(), hierarchy.getCode());
    }
    finally
    {
      annotation.delete();
    }
  }

  @Test
  public void testSetInheritedHierarchy()
  {
    FastTestDataset.runAsUser(FastTestDataset.USER_CGOV_RA, (request) -> {

      try
      {
        HierarchyType ht = service.setInheritedHierarchy(request.getSessionId(), TEST_HT.getCode(), FastTestDataset.HIER_ADMIN.getCode(), FastTestDataset.PROVINCE.getCode());

        List<HierarchyNode> nodes = ht.getRootGeoObjectTypes();
        HierarchyNode node = nodes.get(0);
        GeoObjectType root = node.getGeoObjectType();

        Assert.assertEquals(FastTestDataset.COUNTRY.getCode(), root.getCode());
        Assert.assertEquals(FastTestDataset.HIER_ADMIN.getCode(), node.getInheritedHierarchyCode());
      }
      finally
      {
        HierarchyType ht = service.removeInheritedHierarchy(request.getSessionId(), TEST_HT.getCode(), FastTestDataset.PROVINCE.getCode());

        List<HierarchyNode> nodes = ht.getRootGeoObjectTypes();
        HierarchyNode node = nodes.get(0);
        GeoObjectType root = node.getGeoObjectType();

        Assert.assertEquals(FastTestDataset.PROVINCE.getCode(), root.getCode());
        Assert.assertNull(node.getInheritedHierarchyCode());
      }
    });
  }

}
