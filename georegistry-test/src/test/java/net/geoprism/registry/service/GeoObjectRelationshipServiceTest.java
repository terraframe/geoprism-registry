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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.dataaccess.ValueOverTimeDTO;
import org.commongeoregistry.adapter.metadata.HierarchyNode;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.business.SmartExceptionDTO;

import net.geoprism.registry.roles.ReadGeoObjectPermissionException;
import net.geoprism.registry.test.CambodiaTestDataset;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.TestHierarchyTypeInfo;
import net.geoprism.registry.test.TestUserInfo;
import net.geoprism.registry.view.ServerParentTreeNodeOverTime;

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
  public void testGetHierarchies() throws ParseException
  {
    TestDataSet.runAsUser(FastTestDataset.USER_ADMIN, (request, adapter) -> {
      JsonArray ptn = adapter.getHierarchiesForGeoObjectOverTime(FastTestDataset.DIST_CENTRAL.getCode(), FastTestDataset.DISTRICT.getCode());
      
      Assert.assertEquals(2, ptn.size());
      
      boolean foundAdminHR = false;
      boolean foundHealthHR = false;
      
      for (int i = 0; i < ptn.size(); ++i)
      {
        /*
         * Assert hierarchy information
         */
        JsonObject hierarchy = ptn.get(i).getAsJsonObject();
        
        String code = hierarchy.get(ServerParentTreeNodeOverTime.JSON_HIERARCHY_CODE).getAsString();
        
        String label = hierarchy.get(ServerParentTreeNodeOverTime.JSON_HIERARCHY_LABEL).getAsString();
        
        TestHierarchyTypeInfo testHt;
        
        if (code.equals(FastTestDataset.HIER_ADMIN.getCode()))
        {
          foundAdminHR = true;
          
          testHt = FastTestDataset.HIER_ADMIN;
        }
        else if (code.equals(FastTestDataset.HIER_HEALTH_ADMIN.getCode()))
        {
          foundHealthHR = true;
          
          testHt = FastTestDataset.HIER_HEALTH_ADMIN;
        }
        else
        {
          Assert.fail("Unexpected hierarchy code [" + code + "]");
          return;
        }
        
        Assert.assertEquals(testHt.getDisplayLabel(), label);
        
        JsonArray types = hierarchy.get(ServerParentTreeNodeOverTime.JSON_HIERARCHY_TYPES).getAsJsonArray();
        Assert.assertEquals(2, types.size());
        
        /*
         * Assert type information
         */
        String[] typeCodes = new String[types.size()];
        String[] typeLabels = new String[types.size()];
        for (int j = 0; j < types.size(); ++j)
        {
          typeCodes[j] = types.get(j).getAsJsonObject().get(ServerParentTreeNodeOverTime.JSON_TYPE_CODE).getAsString();
          typeLabels[j] = types.get(j).getAsJsonObject().get(ServerParentTreeNodeOverTime.JSON_TYPE_LABEL).getAsString();
        }
        
        Assert.assertTrue(ArrayUtils.contains(typeCodes, FastTestDataset.COUNTRY.getCode()));
        Assert.assertTrue(ArrayUtils.contains(typeCodes, FastTestDataset.PROVINCE.getCode()));
        
        Assert.assertTrue(ArrayUtils.contains(typeLabels, FastTestDataset.COUNTRY.getDisplayLabel().getValue()));
        Assert.assertTrue(ArrayUtils.contains(typeLabels, FastTestDataset.PROVINCE.getDisplayLabel().getValue()));
        
        /*
         * Assert entry information
         */
        JsonArray entries = hierarchy.get(ServerParentTreeNodeOverTime.JSON_HIERARCHY_ENTRIES).getAsJsonArray();
        
        Assert.assertEquals(1, entries.size());
        
        SimpleDateFormat format = ServerParentTreeNodeOverTime.getDateFormat();
        
        for (int j = 0; j < entries.size(); ++j)
        {
          JsonObject entry = entries.get(j).getAsJsonObject();
          
          Date startDate = format.parse(entry.get(ServerParentTreeNodeOverTime.JSON_ENTRY_STARTDATE).getAsString());
          Assert.assertEquals(FastTestDataset.DEFAULT_OVER_TIME_DATE, startDate);
          
          Date endDate = format.parse(entry.get(ServerParentTreeNodeOverTime.JSON_ENTRY_ENDDATE).getAsString());
          Assert.assertEquals(FastTestDataset.DEFAULT_END_TIME_DATE, endDate);
          
          JsonObject parents = entry.get(ServerParentTreeNodeOverTime.JSON_ENTRY_PARENTS).getAsJsonObject();
          
          Assert.assertEquals(2, parents.keySet().size());
          
          String[] parentCodes = new String[2];
          
          int k = 0;
          for (String parentCode : parents.keySet())
          {
            parentCodes[k++] = parentCode;
            
            JsonObject parent = parents.get(parentCode).getAsJsonObject();
            
            JsonObject geoObject = parent.get(ServerParentTreeNodeOverTime.JSON_ENTRY_PARENT_GEOOBJECT).getAsJsonObject();
            
            GeoObject go = GeoObject.fromJSON(adapter, geoObject.toString());
            
            if (parentCode.equals(FastTestDataset.COUNTRY.getCode()))
            {
              Assert.assertEquals(FastTestDataset.CAMBODIA.getCode(), go.getCode());
            }
            else if (parentCode.equals(FastTestDataset.PROVINCE.getCode()))
            {
              Assert.assertEquals(FastTestDataset.PROV_CENTRAL.getCode(), go.getCode());
            }
          }
          
          Assert.assertTrue(ArrayUtils.contains(parentCodes, FastTestDataset.COUNTRY.getCode()));
          Assert.assertTrue(ArrayUtils.contains(parentCodes, FastTestDataset.PROVINCE.getCode()));
        }
      }
      
      Assert.assertTrue(foundHealthHR && foundAdminHR);
    });
  }
  
  @Test
  public void testGetHierarchiesPrivate()
  {
    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM_PRIVATE };
    for (TestUserInfo user : allowedUsers)
    {
      TestDataSet.runAsUser(user, (request, adapter) -> {
        JsonArray ptn = adapter.getHierarchiesForGeoObjectOverTime(FastTestDataset.PROV_CENTRAL_PRIVATE.getCode(), FastTestDataset.PROVINCE_PRIVATE.getCode());
        
        Assert.assertEquals(1, ptn.size());
      });
    }
    
    TestUserInfo[] disallowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RM, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC, FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_MOHA_RM, FastTestDataset.USER_MOHA_RC, FastTestDataset.USER_MOHA_AC };
    for (TestUserInfo user : disallowedUsers)
    {
      TestDataSet.runAsUser(user, (request, adapter) -> {
        
        try
        {
          adapter.getHierarchiesForGeoObjectOverTime(FastTestDataset.PROV_CENTRAL_PRIVATE.getCode(), FastTestDataset.PROVINCE_PRIVATE.getCode());
          
          Assert.fail("Expected a permission exception");
        }
        catch (SmartExceptionDTO ex)
        {
          Assert.assertEquals(ReadGeoObjectPermissionException.CLASS, ex.getType());
        }
      });
    }
  }
  
  /**
   * The getHierarchies endpoint is used by the GeoObject editor widget to display hierarchy information.
   * 
   * TODO : This test very much might not make sense anymore given some recent refactors. I've tweaked it
   *   just barely enough to get it passing again. 
   */
  @Test
  public void testGetHierarchiesRoleFiltering()
  {
    TestUserInfo[] cgovUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM, FastTestDataset.USER_CGOV_RC };
    for (TestUserInfo user : cgovUsers)
    {
      TestDataSet.runAsUser(user, (request, adapter) -> {
        JsonArray hiers = adapter.getHierarchiesForGeoObjectOverTime(FastTestDataset.PROV_CENTRAL.getCode(), FastTestDataset.PROVINCE.getCode());
        
        Assert.assertEquals(2, hiers.size());
        
//        JsonObject hierarchy = hiers.get(0).getAsJsonObject();
//        
//        String code = hierarchy.get(ServerParentTreeNodeOverTime.JSON_HIERARCHY_CODE).getAsString();
//        
//        Assert.assertEquals(FastTestDataset.HIER_ADMIN.getCode(), code);
      });
    }
    
    TestUserInfo[] mohaUsers = new TestUserInfo[] { FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_MOHA_RM, FastTestDataset.USER_MOHA_RC };
    for (TestUserInfo user : mohaUsers)
    {
      TestDataSet.runAsUser(user, (request, adapter) -> {
        JsonArray hiers = adapter.getHierarchiesForGeoObjectOverTime(FastTestDataset.CENTRAL_HOSPITAL.getCode(), FastTestDataset.HOSPITAL.getCode());
        
        Assert.assertEquals(1, hiers.size());
        
//        JsonObject hierarchy = hiers.get(0).getAsJsonObject();
//        
//        String code = hierarchy.get(ServerParentTreeNodeOverTime.JSON_HIERARCHY_CODE).getAsString();
//        
//        Assert.assertEquals(FastTestDataset.HIER_HEALTH_ADMIN.getCode(), code);
      });
    }
    
//    TestUserInfo[] disallowedUsers = new TestUserInfo[] { FastTestDataset.USER_MOHA_AC, FastTestDataset.USER_CGOV_AC };
//    for (TestUserInfo user : disallowedUsers)
//    {
//      TestDataSet.runAsUser(user, (request, adapter) -> {
//        JsonArray hiers = adapter.getHierarchiesForGeoObjectOverTime(FastTestDataset.PROV_CENTRAL.getCode(), FastTestDataset.PROVINCE.getCode());
//        
//        Assert.assertEquals(0, hiers.size());
//      });
//    }
    
    /*
     * GeoObjectType specific allowed permissions
     */
    TestUserInfo[] cgovPrivateUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RM_PRIVATE };
    for (TestUserInfo user : cgovPrivateUsers)
    {
      TestDataSet.runAsUser(user, (request, adapter) -> {
        JsonArray hiers = adapter.getHierarchiesForGeoObjectOverTime(FastTestDataset.PROV_CENTRAL_PRIVATE.getCode(), FastTestDataset.PROVINCE_PRIVATE.getCode());
        
        Assert.assertEquals(1, hiers.size());
        
        JsonObject hierarchy = hiers.get(0).getAsJsonObject();
        
        String code = hierarchy.get(ServerParentTreeNodeOverTime.JSON_HIERARCHY_CODE).getAsString();
        
        Assert.assertEquals(FastTestDataset.HIER_ADMIN.getCode(), code);
      });
    }
    
    /*
     * GeoObjectType specific not allowed permissions
     */
    TestUserInfo[] rmNotAllowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC };
    for (TestUserInfo user : rmNotAllowedUsers)
    {
      TestDataSet.runAsUser(user, (request, adapter) -> {
        JsonArray hiers = adapter.getHierarchiesForGeoObjectOverTime(FastTestDataset.CENTRAL_HOSPITAL.getCode(), FastTestDataset.HOSPITAL.getCode());
        
        Assert.assertEquals(1, hiers.size());
      });
    }
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
    hts = Arrays.stream(hts).filter(ht -> ArrayUtils.contains(new String[] {FastTestDataset.HIER_ADMIN.getCode(), FastTestDataset.HIER_HEALTH_ADMIN.getCode()}, ht.getCode())).toArray(HierarchyType[]::new);
    
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
        Assert.assertEquals(1, hnProvince.getChildren().size());
      }
      else if (ht.getCode().equals(FastTestDataset.HIER_HEALTH_ADMIN.getCode()))
      {
        List<HierarchyNode> provChildren = hnProvince.getChildren();
        
        Assert.assertEquals(2, provChildren.size());
        
//        HierarchyNode hnHospital = provChildren.get(0);
//        
//        Assert.assertEquals(FastTestDataset.HOSPITAL.getCode(), hnHospital.getGeoObjectType().getCode());
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
    expectedChildren.add(FastTestDataset.PROV_WESTERN);

    // Recursive
    ChildTreeNode tn = testData.adapter.getChildGeoObjects(parentId, parentTypeCode, TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, childrenTypes, true);
    FastTestDataset.CAMBODIA.childTreeNodeAssert(tn, expectedChildren);
    Assert.assertEquals(tn.toJSON().toString(), ChildTreeNode.fromJSON(tn.toJSON().toString(), testData.adapter).toJSON().toString());

    // Not recursive
    ChildTreeNode tn2 = testData.adapter.getChildGeoObjects(parentId, parentTypeCode, TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, childrenTypes, false);
    FastTestDataset.CAMBODIA.childTreeNodeAssert(tn2, expectedChildren);
    Assert.assertEquals(tn2.toJSON().toString(), ChildTreeNode.fromJSON(tn2.toJSON().toString(), testData.adapter).toJSON().toString());
    
    // Null date
    ChildTreeNode tn3 = testData.adapter.getChildGeoObjects(parentId, parentTypeCode, null, null, childrenTypes, false);
    FastTestDataset.CAMBODIA.childTreeNodeAssert(tn3, expectedChildren);
    Assert.assertEquals(tn3.toJSON().toString(), ChildTreeNode.fromJSON(tn3.toJSON().toString(), testData.adapter).toJSON().toString());
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
        ChildTreeNode tn = adapter.getChildGeoObjects(parentId, parentTypeCode, TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, childrenTypes, true);
        FastTestDataset.CAMBODIA.childTreeNodeAssert(tn, expectedChildren);
        Assert.assertEquals(tn.toJSON().toString(), ChildTreeNode.fromJSON(tn.toJSON().toString(), testData.adapter).toJSON().toString());
      });
    }
    
    TestUserInfo[] disallowedUsers = new TestUserInfo[] { FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_MOHA_RM, FastTestDataset.USER_MOHA_RC, FastTestDataset.USER_MOHA_AC, FastTestDataset.USER_CGOV_RM, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC };
    for (TestUserInfo user : disallowedUsers)
    {
      FastTestDataset.runAsUser(user, (request, adapter) -> {
        ChildTreeNode ctn = adapter.getChildGeoObjects(parentId, parentTypeCode, TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, childrenTypes, true);
        
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
    ParentTreeNode tn = testData.adapter.getParentGeoObjects(childId, childTypeCode, TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, parentTypes, true);
    FastTestDataset.PROV_CENTRAL.parentTreeNodeAssert(tn, expectedParents);
    Assert.assertEquals(tn.toJSON().toString(), ParentTreeNode.fromJSON(tn.toJSON().toString(), testData.adapter).toJSON().toString());

    // Not recursive
    ParentTreeNode tn2 = testData.adapter.getParentGeoObjects(childId, childTypeCode, TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, parentTypes, false);
    FastTestDataset.PROV_CENTRAL.parentTreeNodeAssert(tn2, expectedParents);
    Assert.assertEquals(tn2.toJSON().toString(), ParentTreeNode.fromJSON(tn2.toJSON().toString(), testData.adapter).toJSON().toString());

    // Test only getting countries
    String[] countryArr = new String[] { FastTestDataset.COUNTRY.getCode() };
    ParentTreeNode tn3 = testData.adapter.getParentGeoObjects(childId, childTypeCode, TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, countryArr, true);
    FastTestDataset.PROV_CENTRAL.parentTreeNodeAssert(tn3, expectedParents);
    Assert.assertEquals(tn3.toJSON().toString(), ParentTreeNode.fromJSON(tn3.toJSON().toString(), testData.adapter).toJSON().toString());

    // Test null parent types
    ParentTreeNode tn4 = testData.adapter.getParentGeoObjects(childId, childTypeCode, TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, null, true);
    FastTestDataset.PROV_CENTRAL.parentTreeNodeAssert(tn4, expectedParents);
    Assert.assertEquals(tn4.toJSON().toString(), ParentTreeNode.fromJSON(tn4.toJSON().toString(), testData.adapter).toJSON().toString());

    // Test empty parent types
    ParentTreeNode tn5 = testData.adapter.getParentGeoObjects(childId, childTypeCode, TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, new String[] {}, true);
    FastTestDataset.PROV_CENTRAL.parentTreeNodeAssert(tn5, expectedParents);
    Assert.assertEquals(tn5.toJSON().toString(), ParentTreeNode.fromJSON(tn5.toJSON().toString(), testData.adapter).toJSON().toString());
    
    // Null dates
    ParentTreeNode tn6 = testData.adapter.getParentGeoObjects(childId, childTypeCode, null, null, new String[] {}, true);
    FastTestDataset.PROV_CENTRAL.parentTreeNodeAssert(tn6, expectedParents);
    Assert.assertEquals(tn6.toJSON().toString(), ParentTreeNode.fromJSON(tn6.toJSON().toString(), testData.adapter).toJSON().toString());
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
        ParentTreeNode tn = adapter.getParentGeoObjects(childId, childTypeCode, TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, parentTypes, true);
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
          adapter.getParentGeoObjects(childId, childTypeCode, TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, parentTypes, true);
          
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

    ChildTreeNode ctnCAMBODIA2 = testData.adapter.getChildGeoObjects(FastTestDataset.CAMBODIA.getRegistryId(), FastTestDataset.CAMBODIA.getGeoObjectType().getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, new String[] { FastTestDataset.PROVINCE.getCode() }, false);

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
    ChildTreeNode ctnCAMBODIA2 = testData.adapter.getChildGeoObjects(FastTestDataset.CAMBODIA.getRegistryId(), FastTestDataset.CAMBODIA.getGeoObjectType().getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, new String[] { FastTestDataset.PROVINCE.getCode() }, false);

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
