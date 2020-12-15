/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.service;

import java.util.ArrayList;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.metadata.HierarchyNode;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.commongeoregistry.adapter.metadata.RegistryRole;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.business.SmartExceptionDTO;
import com.runwaysdk.session.Request;

import net.geoprism.registry.roles.ReadGeoObjectPermissionException;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.TestUserInfo;

public class GeoObjectRelationshipServiceTest
{
  protected static FastTestDataset               testData;
  
  @BeforeClass
  public static void setUpClass()
  {
    testData = FastTestDataset.newTestData();
    testData.setUpMetadata();
  }
  
  @AfterClass
  public static void cleanUpClass()
  {
    testData.tearDownMetadata();
  }
  
  @Before
  public void setUp()
  {
    testData.setUpInstanceData();
    
    testData.logIn(FastTestDataset.USER_CGOV_RA);
    
    TestDataSet.populateAdapterIds(null, testData.adapter);
  }

  @After
  public void tearDown()
  {
    testData.logOut();
    
    testData.tearDownInstanceData();
  }
  
  /**
   * TODO : The registry endpoint "init" is very poorly named. It's used to populate the hierarchy manager with initial data.
   *        Regardless of how poorly named it is, it's still vital that we test it because it's a critical component of the CGR.
   */
  @Test
  public void testInit()
  {
    JsonObject json = testData.adapter.hierarchyManagerInit();
    
    Assert.assertNotNull(json.get("types").getAsJsonArray());
    Assert.assertNotNull(json.get("hierarchies").getAsJsonArray());
    Assert.assertNotNull(json.get("organizations").getAsJsonArray());
    Assert.assertNotNull(json.get("locales").getAsJsonArray());
  }
  
  @Test
  public void testGetHierarchies()
  {
    // TODO : This endpoint returns a response for which there is literally no "DTO" representation. For this reason, we cannot
    // convert it to any sort of type-safe "ParentTreeNodeOverTime" object because one does not exist.
    JsonArray ptn = testData.adapter.getHierarchiesForGeoObjectOverTime(FastTestDataset.PROV_CENTRAL.getCode(), FastTestDataset.PROVINCE.getCode());
    
    Assert.assertEquals(2, ptn.size());
    
    for (int i = 0; i < ptn.size(); ++i)
    {
      JsonObject hierarchy = ptn.get(0).getAsJsonObject();
      
      Assert.assertEquals(1, hierarchy.get("entries").getAsJsonArray().size());
    }
  }
  
  // TODO : Test permissions stuff including private GeoObjects
  @Test
  public void testGetHierarchiesPrivate()
  {
    throw new UnsupportedOperationException();
  }
  
  @Test
  public void testGetHierarchyTypes()
  {
    final String[] types = new String[] { FastTestDataset.HIER_ADMIN.getCode() };

    HierarchyType[] hts = testData.adapter.getHierarchyTypes(types);

    Assert.assertEquals(types.length, hts.length);

    HierarchyType locatedIn = hts[0];
    Assert.assertEquals(locatedIn.toJSON().toString(), HierarchyType.fromJSON(locatedIn.toJSON().toString(), testData.adapter).toJSON().toString());

    // Test to make sure we can provide no types and get everything back
    HierarchyType[] hts2 = testData.adapter.getHierarchyTypes(new String[] {});
    checkHierarchyTypeResponse(hts2, true);

    HierarchyType[] hts3 = testData.adapter.getHierarchyTypes(null);
    checkHierarchyTypeResponse(hts3, true);
  }
  
  @Test
  public void testGetPrivateHierarchyTypes()
  {
    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM_PRIVATE, FastTestDataset.USER_CGOV_RC_PRIVATE, FastTestDataset.USER_CGOV_AC_PRIVATE };
    for (TestUserInfo user : allowedUsers)
    {
      TestDataSet.runAsUser(user, (request, adapter) -> {
        HierarchyType[] hts = adapter.getHierarchyTypes(null);
        
        checkHierarchyTypeResponse(hts, true);
      });
    }
    
    TestUserInfo[] disallowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RM, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC, FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_MOHA_RM, FastTestDataset.USER_MOHA_RC, FastTestDataset.USER_MOHA_AC };
    for (TestUserInfo user : disallowedUsers)
    {
      TestDataSet.runAsUser(user, (request, adapter) -> {
        HierarchyType[] hts = adapter.getHierarchyTypes(null);
        
        checkHierarchyTypeResponse(hts, false);
      });
    }
  }
  
  private void checkHierarchyTypeResponse(HierarchyType[] hts, boolean hasPrivate)
  {
    Assert.assertEquals(2, hts.length);
    
    for (HierarchyType ht : hts)
    {
      List<HierarchyNode> rootTypes = ht.getRootGeoObjectTypes();
      
      Assert.assertEquals(1, rootTypes.size());
      
      HierarchyNode hnRoot = rootTypes.get(0);
      
      Assert.assertEquals(FastTestDataset.COUNTRY.getCode(), hnRoot.getGeoObjectType().getCode());
      
      List<HierarchyNode> provinces = hnRoot.getChildren();
      
      HierarchyNode hnProvince = null;
      
      if (ht.getCode().equals(FastTestDataset.HIER_ADMIN.getCode()) && hasPrivate)
      {
        Assert.assertEquals(2, provinces.size());
        
        for (HierarchyNode province : provinces)
        {
          if (province.getGeoObjectType().getCode().equals(FastTestDataset.PROVINCE.getCode()))
          {
            hnProvince = province;
          }
          else
          {
            Assert.assertEquals(FastTestDataset.PROVINCE_PRIVATE.getCode(), province.getGeoObjectType().getCode());
          }
        }
        
        Assert.assertNotNull(hnProvince);
      }
      else
      {
        Assert.assertEquals(1, provinces.size());
        hnProvince = provinces.get(0);
      }
      
      Assert.assertEquals(FastTestDataset.PROVINCE.getCode(), hnProvince.getGeoObjectType().getCode());
      
      if (ht.getCode().equals(FastTestDataset.HIER_ADMIN.getCode()))
      {
        Assert.assertEquals(0, hnProvince.getChildren().size());
      }
      else if (ht.getCode().equals(FastTestDataset.HIER_HEALTH_ADMIN.getCode()))
      {
        List<HierarchyNode> hospitals = hnProvince.getChildren();
        
        Assert.assertEquals(1, hospitals.size());
        
        HierarchyNode hnHospital = hospitals.get(0);
        
        Assert.assertEquals(FastTestDataset.HOSPITAL.getCode(), hnHospital.getGeoObjectType().getCode());
      }
      else
      {
        Assert.fail("Unexpected Hierarchy [" + ht.getCode() + "]");
      }
    }
  }
  
  @Test
  public void testGetChildGeoObjects()
  {
    final String parentId = FastTestDataset.CAMBODIA.getRegistryId();
    final String parentTypeCode = FastTestDataset.CAMBODIA.getGeoObjectType().getCode();
    final String[] childrenTypes = new String[] { FastTestDataset.PROVINCE.getCode() };
    
    final List<TestGeoObjectInfo> expectedChildren = new ArrayList<TestGeoObjectInfo>();
    expectedChildren.add(FastTestDataset.PROV_CENTRAL);
    expectedChildren.add(FastTestDataset.PROV_CENTRAL);

    // Recursive
    ChildTreeNode tn = testData.adapter.getChildGeoObjects(parentId, parentTypeCode, childrenTypes, true);
    FastTestDataset.CAMBODIA.childTreeNodeAssert(tn, expectedChildren);
    Assert.assertEquals(tn.toJSON().toString(), ChildTreeNode.fromJSON(tn.toJSON().toString(), testData.adapter).toJSON().toString());

    // Not recursive
    ChildTreeNode tn2 = testData.adapter.getChildGeoObjects(parentId, parentTypeCode, childrenTypes, false);
    FastTestDataset.CAMBODIA.childTreeNodeAssert(tn2, expectedChildren);
    Assert.assertEquals(tn2.toJSON().toString(), ChildTreeNode.fromJSON(tn2.toJSON().toString(), testData.adapter).toJSON().toString());
  }
  
  @Test
  public void testGetPrivateChildGeoObjects()
  {
    // TODO : Attempt to get children of a parent which is private. This scenario should throw an error
    
    final String parentId = FastTestDataset.CAMBODIA.getRegistryId();
    final String parentTypeCode = FastTestDataset.CAMBODIA.getGeoObjectType().getCode();
    final String[] childrenTypes = new String[] { FastTestDataset.PROVINCE_PRIVATE.getCode() };
    
    final List<TestGeoObjectInfo> expectedChildren = new ArrayList<TestGeoObjectInfo>();
    expectedChildren.add(FastTestDataset.PROV_CENTRAL_PRIVATE);

    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM_PRIVATE, FastTestDataset.USER_CGOV_RC_PRIVATE, FastTestDataset.USER_CGOV_AC_PRIVATE };
    for (TestUserInfo user : allowedUsers)
    {
      FastTestDataset.runAsUser(user, (request, adapter) -> {
        ChildTreeNode tn = adapter.getChildGeoObjects(parentId, parentTypeCode, childrenTypes, true);
        FastTestDataset.CAMBODIA.childTreeNodeAssert(tn, expectedChildren);
        Assert.assertEquals(tn.toJSON().toString(), ChildTreeNode.fromJSON(tn.toJSON().toString(), testData.adapter).toJSON().toString());
      });
    }
    
    TestUserInfo[] disallowedUsers = new TestUserInfo[] { FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_MOHA_RM, FastTestDataset.USER_MOHA_RC, FastTestDataset.USER_MOHA_AC, FastTestDataset.USER_CGOV_RM, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC };
    for (TestUserInfo user : disallowedUsers)
    {
      FastTestDataset.runAsUser(user, (request, adapter) -> {
        ChildTreeNode ctn = adapter.getChildGeoObjects(parentId, parentTypeCode, childrenTypes, true);
        
        Assert.assertEquals(0, ctn.getChildren().size());
      });
    }
  }
  
  @Test
  public void testGetParentGeoObjects()
  {
    final String childId = FastTestDataset.PROV_CENTRAL.getRegistryId();
    final String childTypeCode = FastTestDataset.PROVINCE.getCode();
    final String[] parentTypes = new String[] { FastTestDataset.COUNTRY.getCode() };
    
    final List<TestGeoObjectInfo> expectedParents = new ArrayList<TestGeoObjectInfo>();
    expectedParents.add(FastTestDataset.CAMBODIA);
    expectedParents.add(FastTestDataset.CAMBODIA);

    // Recursive
    ParentTreeNode tn = testData.adapter.getParentGeoObjects(childId, childTypeCode, parentTypes, true, null);
    FastTestDataset.PROV_CENTRAL.parentTreeNodeAssert(tn, expectedParents);
    Assert.assertEquals(tn.toJSON().toString(), ParentTreeNode.fromJSON(tn.toJSON().toString(), testData.adapter).toJSON().toString());

    // Not recursive
    ParentTreeNode tn2 = testData.adapter.getParentGeoObjects(childId, childTypeCode, parentTypes, false, null);
    FastTestDataset.PROV_CENTRAL.parentTreeNodeAssert(tn2, expectedParents);
    Assert.assertEquals(tn2.toJSON().toString(), ParentTreeNode.fromJSON(tn2.toJSON().toString(), testData.adapter).toJSON().toString());

    // Test only getting countries
    String[] countryArr = new String[] { FastTestDataset.COUNTRY.getCode() };
    ParentTreeNode tn3 = testData.adapter.getParentGeoObjects(childId, childTypeCode, countryArr, true, null);
    FastTestDataset.PROV_CENTRAL.parentTreeNodeAssert(tn3, expectedParents);
    Assert.assertEquals(tn3.toJSON().toString(), ParentTreeNode.fromJSON(tn3.toJSON().toString(), testData.adapter).toJSON().toString());

    // Test null parent types
    ParentTreeNode tn4 = testData.adapter.getParentGeoObjects(childId, childTypeCode, null, true, null);
    FastTestDataset.PROV_CENTRAL.parentTreeNodeAssert(tn4, expectedParents);
    Assert.assertEquals(tn4.toJSON().toString(), ParentTreeNode.fromJSON(tn4.toJSON().toString(), testData.adapter).toJSON().toString());

    // Test empty parent types
    ParentTreeNode tn5 = testData.adapter.getParentGeoObjects(childId, childTypeCode, new String[] {}, true, null);
    FastTestDataset.PROV_CENTRAL.parentTreeNodeAssert(tn5, expectedParents);
    Assert.assertEquals(tn5.toJSON().toString(), ParentTreeNode.fromJSON(tn5.toJSON().toString(), testData.adapter).toJSON().toString());
  }
  
  @Test
  public void testGetPrivateParentGeoObjects()
  {
    final String childId = FastTestDataset.PROV_CENTRAL_PRIVATE.getRegistryId();
    final String childTypeCode = FastTestDataset.PROV_CENTRAL_PRIVATE.getGeoObjectType().getCode();
    final String[] parentTypes = new String[] { FastTestDataset.COUNTRY.getCode() };
    
    final List<TestGeoObjectInfo> expectedParents = new ArrayList<TestGeoObjectInfo>();
    expectedParents.add(FastTestDataset.CAMBODIA);

    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM_PRIVATE, FastTestDataset.USER_CGOV_RC_PRIVATE, FastTestDataset.USER_CGOV_AC_PRIVATE };
    for (TestUserInfo user : allowedUsers)
    {
      FastTestDataset.runAsUser(user, (request, adapter) -> {
        ParentTreeNode tn = adapter.getParentGeoObjects(childId, childTypeCode, parentTypes, true, null);
        FastTestDataset.PROV_CENTRAL_PRIVATE.parentTreeNodeAssert(tn, expectedParents);
        Assert.assertEquals(tn.toJSON().toString(), ParentTreeNode.fromJSON(tn.toJSON().toString(), testData.adapter).toJSON().toString());
      });
    }
    
    TestUserInfo[] disallowedUsers = new TestUserInfo[] { FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_MOHA_RM, FastTestDataset.USER_MOHA_RC, FastTestDataset.USER_MOHA_AC, FastTestDataset.USER_CGOV_RM, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC };
    for (TestUserInfo user : disallowedUsers)
    {
      FastTestDataset.runAsUser(user, (request, adapter) -> {
        try
        {
          adapter.getParentGeoObjects(childId, childTypeCode, parentTypes, true, null);
          
          Assert.fail("Expected a permissions error.");
        }
        catch (SmartExceptionDTO e)
        {
          Assert.assertEquals(ReadGeoObjectPermissionException.CLASS, e.getType());
        }
      });
    }
  }

  @Test
  public void testAddChild()
  {
    TestGeoObjectInfo testAddChild = testData.newTestGeoObjectInfo("TEST_ADD_CHILD", FastTestDataset.PROVINCE);
    testAddChild.apply();

    ParentTreeNode ptnTestState = testData.adapter.addChild(FastTestDataset.CAMBODIA.getRegistryId(), FastTestDataset.CAMBODIA.getGeoObjectType().getCode(), testAddChild.getRegistryId(), testAddChild.getGeoObjectType().getCode(), FastTestDataset.HIER_ADMIN.getCode());

    boolean found = false;
    for (ParentTreeNode ptnCAMBODIA : ptnTestState.getParents())
    {
      if (ptnCAMBODIA.getGeoObject().getCode().equals(FastTestDataset.CAMBODIA.getCode()))
      {
        found = true;
        break;
      }
    }
    Assert.assertTrue("Did not find our test object in the list of returned children", found);
    testAddChild.assertEquals(ptnTestState.getGeoObject());

    ChildTreeNode ctnCAMBODIA2 = testData.adapter.getChildGeoObjects(FastTestDataset.CAMBODIA.getRegistryId(), FastTestDataset.CAMBODIA.getGeoObjectType().getCode(), new String[] { FastTestDataset.PROVINCE.getCode() }, false);

    found = false;
    for (ChildTreeNode ctnState : ctnCAMBODIA2.getChildren())
    {
      if (ctnState.getGeoObject().getCode().equals(testAddChild.getCode()))
      {
        found = true;
        break;
      }
    }
    Assert.assertTrue("Did not find our test object in the list of returned children", found);
  }

  @Test
  public void testRemoveChild()
  {
    /*
     * Remove Child
     */
    testData.adapter.removeChild(FastTestDataset.CAMBODIA.getRegistryId(), FastTestDataset.CAMBODIA.getGeoObjectType().getCode(), FastTestDataset.PROV_CENTRAL.getRegistryId(), FastTestDataset.PROVINCE.getCode(), FastTestDataset.HIER_ADMIN.getCode());

    /*
     * Fetch the children and validate ours was removed
     */
    ChildTreeNode ctnCAMBODIA2 = testData.adapter.getChildGeoObjects(FastTestDataset.CAMBODIA.getRegistryId(), FastTestDataset.CAMBODIA.getGeoObjectType().getCode(), new String[] { FastTestDataset.PROVINCE.getCode() }, false);

    boolean found = false;
    for (ChildTreeNode ctnState : ctnCAMBODIA2.getChildren())
    {
      if (ctnState.getGeoObject().getCode().equals(FastTestDataset.CAMBODIA.getCode()))
      {
        found = true;
        break;
      }
    }
    Assert.assertFalse("Did not expect PROV_CENTRAL to be a child of CAMBODIA (because we deleted it earlier).", found);
  }
}
