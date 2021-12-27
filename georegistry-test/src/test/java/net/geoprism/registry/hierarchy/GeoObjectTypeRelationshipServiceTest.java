/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.hierarchy;

import java.util.List;

import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.metadata.HierarchyNode;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.commongeoregistry.adapter.metadata.RegistryRole;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.business.SmartExceptionDTO;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.Universal;

import net.geoprism.registry.AbstractParentException;
import net.geoprism.registry.model.RootGeoObjectType;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.roles.HierarchyRelationshipPermissionException;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectTypeInfo;
import net.geoprism.registry.test.TestHierarchyTypeInfo;
import net.geoprism.registry.test.TestRegistryAdapterClient;
import net.geoprism.registry.test.TestUserInfo;

public class GeoObjectTypeRelationshipServiceTest
{

  public static final String TEST_PREFIX = "GOTREL";
  
  public static final TestHierarchyTypeInfo TEST_HT  = new TestHierarchyTypeInfo(TEST_PREFIX + "_Hierarchy", FastTestDataset.ORG_CGOV);
  
  public static final TestGeoObjectTypeInfo TEST_PARENT = new TestGeoObjectTypeInfo(TEST_PREFIX + "_Parent", FastTestDataset.ORG_CGOV);
  
  public static final TestGeoObjectTypeInfo TEST_CHILD = new TestGeoObjectTypeInfo(TEST_PREFIX + "_Child", FastTestDataset.ORG_CGOV);

  public static final TestGeoObjectTypeInfo TEST_PRIVATE_CHILD = new TestGeoObjectTypeInfo("GOTRELTEST_Child_Private", GeometryType.MULTIPOLYGON, true, FastTestDataset.ORG_CGOV, null);
  
  public static final TestUserInfo USER_CHILD_GOT_RM = new TestUserInfo(FastTestDataset.TEST_DATA_KEY + "_" + TEST_PREFIX + "rmprivate", TEST_PREFIX + "rmprivate", FastTestDataset.TEST_DATA_KEY + TEST_PREFIX + "rmprivate@noreply.com", new String[] { RegistryRole.Type.getRM_RoleName(FastTestDataset.ORG_CGOV.getCode(), TEST_CHILD.getCode()) });

  public static final TestUserInfo USER_CHILD_GOT_RC = new TestUserInfo(FastTestDataset.TEST_DATA_KEY + "_" + TEST_PREFIX + "rcprivate", TEST_PREFIX + "rcprivate", FastTestDataset.TEST_DATA_KEY + TEST_PREFIX + "rcprivate@noreply.com", new String[] { RegistryRole.Type.getRC_RoleName(FastTestDataset.ORG_CGOV.getCode(), TEST_CHILD.getCode()) });

  public static final TestUserInfo USER_CHILD_GOT_AC = new TestUserInfo(FastTestDataset.TEST_DATA_KEY + "_" + TEST_PREFIX + "acprivate", TEST_PREFIX + "acprivate", FastTestDataset.TEST_DATA_KEY + TEST_PREFIX + "acprivate@noreply.com", new String[] { RegistryRole.Type.getAC_RoleName(FastTestDataset.ORG_CGOV.getCode(), TEST_CHILD.getCode()) });
  
  public static final TestUserInfo USER_PRIVATE_CHILD_GOT_RM = new TestUserInfo(FastTestDataset.TEST_DATA_KEY + "_" + TEST_PREFIX + "rmprivate", TEST_PREFIX + "rmprivate", FastTestDataset.TEST_DATA_KEY + TEST_PREFIX + "rmprivate@noreply.com", new String[] { RegistryRole.Type.getRM_RoleName(FastTestDataset.ORG_CGOV.getCode(), TEST_PRIVATE_CHILD.getCode()) });

  public static final TestUserInfo USER_PRIVATE_CHILD_GOT_RC = new TestUserInfo(FastTestDataset.TEST_DATA_KEY + "_" + TEST_PREFIX + "rcprivate", TEST_PREFIX + "rcprivate", FastTestDataset.TEST_DATA_KEY + TEST_PREFIX + "rcprivate@noreply.com", new String[] { RegistryRole.Type.getRC_RoleName(FastTestDataset.ORG_CGOV.getCode(), TEST_PRIVATE_CHILD.getCode()) });

  public static final TestUserInfo USER_PRIVATE_CHILD_GOT_AC = new TestUserInfo(FastTestDataset.TEST_DATA_KEY + "_" + TEST_PREFIX + "acprivate", TEST_PREFIX + "acprivate", FastTestDataset.TEST_DATA_KEY + TEST_PREFIX + "acprivate@noreply.com", new String[] { RegistryRole.Type.getAC_RoleName(FastTestDataset.ORG_CGOV.getCode(), TEST_PRIVATE_CHILD.getCode()) });

  protected static FastTestDataset          testData;

  @BeforeClass
  public static void setUpClass()
  {
    testData = FastTestDataset.newTestData();
    testData.setUpMetadata();
  }

  @AfterClass
  public static void cleanUpClass()
  {
    deleteExtraMetadata();
    
    testData.tearDownMetadata();
  }

  @Before
  public void setUp()
  {
    testData.setUpInstanceData();

    deleteExtraMetadata();
    
    TEST_HT.apply();

    testData.logIn(FastTestDataset.USER_CGOV_RA);
  }

  @After
  public void tearDown()
  {
    testData.logOut();

    deleteExtraMetadata();

    testData.tearDownInstanceData();
  }

  private static void deleteExtraMetadata()
  {
    USER_CHILD_GOT_RM.delete();
    USER_CHILD_GOT_RC.delete();
    USER_CHILD_GOT_AC.delete();
    
    USER_PRIVATE_CHILD_GOT_RM.delete();
    USER_PRIVATE_CHILD_GOT_RC.delete();
    USER_PRIVATE_CHILD_GOT_AC.delete();
    
    TEST_HT.delete();
    
    TEST_CHILD.delete();
    TEST_PRIVATE_CHILD.delete();
    TEST_PARENT.delete();
  }
  
  private void addChild(TestRegistryAdapterClient adapter, TestGeoObjectTypeInfo parent, TestGeoObjectTypeInfo child)
  {
    String parentCode = (parent == null) ? Universal.ROOT : parent.getCode();
    
    HierarchyType ht = adapter.addToHierarchy(TEST_HT.getCode(), parentCode, child.getCode());

    List<HierarchyNode> rootGots = ht.getRootGeoObjectTypes();

    for (HierarchyNode node : rootGots)
    {
      if (node.getGeoObjectType().getCode().equals(child.getCode()))
      {
        return;
      }
    }

    Assert.fail("We did not find the child we just added.");
  }

  @Test
  public void testAddChild()
  {
    TEST_CHILD.apply();
    
    USER_CHILD_GOT_RM.apply();
    USER_CHILD_GOT_RC.apply();
    USER_CHILD_GOT_AC.apply();

    // Allowed Users (with ROOT as parent)
    for (TestUserInfo user : new TestUserInfo[] { FastTestDataset.USER_CGOV_RA, USER_CHILD_GOT_RM })
    {
      TestDataSet.runAsUser(user, (request, adapter) -> {
        addChild(adapter, null, TEST_CHILD);
        
        TEST_HT.delete();
        TEST_HT.apply();
      });
    }
    
    // Disallowed Users (with ROOT as parent)
    for (TestUserInfo user : new TestUserInfo[] { FastTestDataset.USER_CGOV_RM, USER_CHILD_GOT_RC, USER_CHILD_GOT_AC, FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_MOHA_RM, FastTestDataset.USER_MOHA_RC, FastTestDataset.USER_MOHA_AC })
    {
      TestDataSet.runAsUser(user, (request, adapter) -> {
        try
        {
          addChild(adapter, null, TEST_CHILD);
          
          Assert.fail("Expected an error");
        }
        catch (SmartExceptionDTO ex)
        {
          Assert.assertEquals(HierarchyRelationshipPermissionException.CLASS, ex.getType());
        }
      });
    }
  }

  @Test
  @Request
  public void testAddChildAbstractType()
  {
    TEST_CHILD.apply();

    TestGeoObjectTypeInfo childGot = new TestGeoObjectTypeInfo("HMST_Abstract", FastTestDataset.ORG_CGOV);
    childGot.setAbstract(true);

    try
    {
      childGot.apply();

      ServerHierarchyType type = FastTestDataset.HIER_ADMIN.getServerObject();
      ServerGeoObjectType parentType = TEST_CHILD.getServerObject();
      ServerGeoObjectType childType = childGot.getServerObject();

      try
      {
        type.addToHierarchy(RootGeoObjectType.INSTANCE, parentType);
        type.addToHierarchy(parentType, childType);

        List<ServerGeoObjectType> children = parentType.getChildren(type);

        Assert.assertEquals(1, children.size());
        Assert.assertEquals(childType.getCode(), children.get(0).getCode());
      }
      finally
      {
        type.removeChild(RootGeoObjectType.INSTANCE, parentType, false);
        type.removeChild(parentType, childType, false);
      }
    }
    finally
    {
      childGot.delete();
    }
  }

  @Test(expected = AbstractParentException.class)
  @Request
  public void testAddChildOfAbstractType()
  {
    TestGeoObjectTypeInfo parentGot = new TestGeoObjectTypeInfo("HMST_Abstract", FastTestDataset.ORG_CGOV);
    parentGot.setAbstract(true);

    TestGeoObjectTypeInfo childGot = new TestGeoObjectTypeInfo("HMST_Child", FastTestDataset.ORG_CGOV);

    try
    {
      parentGot.apply();
      childGot.apply();

      ServerHierarchyType type = FastTestDataset.HIER_ADMIN.getServerObject();
      ServerGeoObjectType parentType = parentGot.getServerObject();
      ServerGeoObjectType childType = childGot.getServerObject();

      try
      {
        type.addToHierarchy(RootGeoObjectType.INSTANCE, parentType);
        type.addToHierarchy(parentType, childType);
      }
      finally
      {
        type.removeChild(RootGeoObjectType.INSTANCE, parentType, false);
      }
    }
    finally
    {
      parentGot.delete();
      childGot.delete();
    }
  }

}
