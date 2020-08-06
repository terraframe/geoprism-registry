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

import java.util.Set;
import java.util.UUID;

import org.commongeoregistry.adapter.constants.DefaultTerms;
import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.geotools.geometry.jts.GeometryBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.business.SmartExceptionDTO;
import com.runwaysdk.session.Request;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import net.geoprism.registry.GeometryTypeException;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.TestRegistryAdapterClient;
import net.geoprism.registry.test.TestUserInfo;

public class GeoObjectServiceTest
{
  protected static FastTestDataset testData;
  
  public static final TestGeoObjectInfo TEST_GO = new TestGeoObjectInfo("GOSERV_TEST_GO", FastTestDataset.COUNTRY);

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
    
    TEST_GO.delete();
    
    testData.logIn(FastTestDataset.USER_CGOV_RA);
  }

  @After
  public void tearDown()
  {
    testData.logOut();
    
    testData.tearDownInstanceData();
  }

  @Test
  public void testGetGeoObject()
  {
    // Allowed Users
    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC };

    for (TestUserInfo user : allowedUsers)
    {
      FastTestDataset.runAsUser(user, (request, adapter) -> {
        GeoObject geoObj = adapter.getGeoObject(FastTestDataset.CAMBODIA.getRegistryId(), FastTestDataset.CAMBODIA.getGeoObjectType().getCode());

        FastTestDataset.CAMBODIA.assertEquals(geoObj);
        
        Assert.assertEquals(DefaultTerms.GeoObjectStatusTerm.ACTIVE.code, geoObj.getStatus().getCode());
      });
    }
    
    // Disallowed Users
    TestUserInfo[] disllowedUsers = new TestUserInfo[] { FastTestDataset.USER_MOHA_RA };

    for (TestUserInfo user : disllowedUsers)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request, adapter) -> {
          adapter.getGeoObject(FastTestDataset.CAMBODIA.getRegistryId(), FastTestDataset.CAMBODIA.getGeoObjectType().getCode());

          Assert.fail();
        });
      }
      catch (SmartExceptionDTO e)
      {
        // Expected
      }
    }
  }
  
  @Test
  public void testGetGeoObjectByCode()
  {
    // Allowed Users
    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC };

    for (TestUserInfo user : allowedUsers)
    {
      FastTestDataset.runAsUser(user, (request, adapter) -> {
        GeoObject geoObj = adapter.getGeoObjectByCode(FastTestDataset.CAMBODIA.getCode(), FastTestDataset.CAMBODIA.getGeoObjectType().getCode());

        Assert.assertEquals(geoObj.toJSON().toString(), GeoObject.fromJSON(adapter, geoObj.toJSON().toString()).toJSON().toString());
        Assert.assertEquals(DefaultTerms.GeoObjectStatusTerm.ACTIVE.code, geoObj.getStatus().getCode());
      });
    }
    
    // Disallowed Users
    TestUserInfo[] disllowedUsers = new TestUserInfo[] { FastTestDataset.USER_MOHA_RA };

    for (TestUserInfo user : disllowedUsers)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request, adapter) -> {
          adapter.getGeoObjectByCode(FastTestDataset.CAMBODIA.getCode(), FastTestDataset.CAMBODIA.getGeoObjectType().getCode());

          Assert.fail();
        });
      }
      catch (SmartExceptionDTO e)
      {
        // This is expected
      }
    }
  }

  @Test
  public void testCreateGeoObjectBadGeometry()
  {
    GeometryBuilder builder = new GeometryBuilder(new GeometryFactory());
    Point point = builder.point(48.44, -123.37);

    GeoObject geoObj = testData.adapter.newGeoObjectInstance(FastTestDataset.PROVINCE.getCode());
    geoObj.setGeometry(point);
    geoObj.setCode(FastTestDataset.CAMBODIA.getCode());
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, FastTestDataset.CAMBODIA.getDisplayLabel());

    try
    {
      testData.adapter.createGeoObject(geoObj.toJSON().toString());

      Assert.fail("Able to create a GeoObject with a wrong geometry type");
    }
    catch (SmartExceptionDTO e)
    {
      // This is expected
      Assert.assertEquals(GeometryTypeException.CLASS, e.getType());
    }
  }
  
  @Test
  public void testCreateGeoObject()
  {
    TestUserInfo[] allowedUsers = new TestUserInfo[] {FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM};
    
    for (TestUserInfo user : allowedUsers)
    {
      TestDataSet.runAsUser(user, (request, adapter) -> {
        GeoObject returned = adapter.createGeoObject(TEST_GO.newGeoObject(adapter).toJSON().toString());
        
        TEST_GO.assertEquals(returned);
        
        Assert.assertEquals(DefaultTerms.GeoObjectStatusTerm.PENDING.code, returned.getStatus().getCode());
        
        TEST_GO.assertApplied();
        TEST_GO.delete();
      });
    }
    
    
    TestUserInfo[] disallowedUsers = new TestUserInfo[] {FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC, FastTestDataset.USER_MOHA_RA};
    
    for (TestUserInfo user : disallowedUsers)
    {
      TestDataSet.runAsUser(user, (request, adapter) -> {
        try
        {
          adapter.createGeoObject(TEST_GO.newGeoObject(ServiceFactory.getAdapter()).toJSON().toString());
          
          Assert.fail();
        }
        catch (SmartExceptionDTO ex)
        {
          // expected
        }
      });
    }
  }
  
  private void updateGO(TestRegistryAdapterClient adapter, TestGeoObjectInfo go)
  {
    go.setWkt(TestDataSet.WKT_POLYGON_2);
    go.setDisplayLabel("Some new value");
    
    GeoObject update = go.fetchGeoObject();
    go.populate(update);
    
    GeoObject returnedUpdate = adapter.updateGeoObject(update.toJSON().toString());

    go.assertEquals(returnedUpdate);
    
    go.assertApplied();
  }

  @Test
  public void testUpdateGeoObject()
  {
    // Allowed Users
    TestUserInfo[] allowedUsers = new TestUserInfo[] {FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM};
    
    for (TestUserInfo user : allowedUsers)
    {
      TestGeoObjectInfo go = testData.newTestGeoObjectInfo("UpdateTest", FastTestDataset.COUNTRY);
      go.apply();
      
      TestDataSet.runAsUser(user, (request, adapter) -> {
        updateGO(adapter, go);
      });
      
      go.delete();
    }
    
    
    // Disallowed Users
    TestUserInfo[] disallowedUsers = new TestUserInfo[] {FastTestDataset.USER_CGOV_AC, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_MOHA_RA};
    
    for (TestUserInfo user : disallowedUsers)
    {
      TestGeoObjectInfo go = testData.newTestGeoObjectInfo("UpdateTest", FastTestDataset.COUNTRY);
      go.apply();
      
      TestDataSet.runAsUser(user, (request, adapter) -> {
        try
        {
          updateGO(adapter, go);
          
          Assert.fail();
        }
        catch (SmartExceptionDTO ex)
        {
          // expected
        }
      });
    }
  }

  @Test
  public void testGetUIDS()
  {
    Set<String> ids = testData.adapter.getGeoObjectUids(100);

    Assert.assertEquals(100, ids.size());

    assertIdIssued(ids);
  }

  @Request
  private void assertIdIssued(Set<String> ids)
  {
    for (String id : ids)
    {
      Assert.assertTrue(RegistryIdService.getInstance().isIssuedId(id));
    }
  }

  @Test
  public void testGetGeoObjectSuggestions()
  {
    JSONArray results = testData.adapter.getGeoObjectSuggestions(FastTestDataset.PROV_CENTRAL.getDisplayLabel().substring(0, 3), FastTestDataset.PROVINCE.getCode(), FastTestDataset.CAMBODIA.getCode(), FastTestDataset.HIER_ADMIN.getCode(), null);

    Assert.assertEquals(1, results.length());

    JSONObject result = results.getJSONObject(0);

    Assert.assertEquals(FastTestDataset.PROV_CENTRAL.getDisplayLabel(), result.getString("name"));
//    Assert.assertEquals(testData.CAMBODIA.getOid(), result.getString("id")); // This is commented out because the ids are different due to postgres + orientdb inconsistencies
    Assert.assertEquals(FastTestDataset.PROV_CENTRAL.getCode(), result.getString(GeoObject.CODE));
  }

  @Test
  public void testGetGeoObjectSuggestionsNoParent()
  {
    JSONArray results = testData.adapter.getGeoObjectSuggestions(FastTestDataset.PROV_CENTRAL.getDisplayLabel().substring(0, 3), FastTestDataset.PROVINCE.getCode(), null, null, null);

    Assert.assertEquals(1, results.length());

    JSONObject result = results.getJSONObject(0);

    Assert.assertEquals(FastTestDataset.PROV_CENTRAL.getDisplayLabel(), result.getString("name"));
//    Assert.assertEquals(testData.CAMBODIA.getOid(), result.getString("id")); // This is commented out because the ids are different due to postgres + orientdb inconsistencies
    Assert.assertEquals(FastTestDataset.PROV_CENTRAL.getCode(), result.getString(GeoObject.CODE));
  }

  /**
   * Test to make sure we can't just provide random ids, they actually have to
   * be issued by our id service
   */
  @Test(expected = SmartExceptionDTO.class)
  public void testUnissuedIdCreate()
  {
    // Create
    GeoObject geoObj = testData.adapter.newGeoObjectInstance(FastTestDataset.PROVINCE.getCode());
    geoObj.setWKTGeometry(FastTestDataset.CAMBODIA.getWkt());
    geoObj.setCode(FastTestDataset.CAMBODIA.getCode());
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, FastTestDataset.CAMBODIA.getDisplayLabel());
    geoObj.setUid(UUID.randomUUID().toString());
    testData.adapter.createGeoObject(geoObj.toJSON().toString());
  }

  /**
   * Test to make sure we can't just provide random ids, they actually have to
   * be issued by our id service
   */
  @Test(expected = SmartExceptionDTO.class)
  public void testUnissuedIdUpdate()
  {
    // Update
    GeoObject waGeoObj = testData.adapter.getGeoObject(FastTestDataset.CAMBODIA.getRegistryId(), FastTestDataset.CAMBODIA.getGeoObjectType().getCode());
    waGeoObj.setWKTGeometry(FastTestDataset.CAMBODIA.getWkt());
    waGeoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, FastTestDataset.CAMBODIA.getDisplayLabel());
    waGeoObj.setUid(UUID.randomUUID().toString());
    testData.adapter.updateGeoObject(waGeoObj.toJSON().toString());
  }

  @Test
  public void testGetChildGeoObjects()
  {
    String parentId = FastTestDataset.CAMBODIA.getRegistryId();
    String parentTypeCode = FastTestDataset.CAMBODIA.getGeoObjectType().getCode();
    String[] childrenTypes = new String[] { FastTestDataset.PROVINCE.getCode() };

    // Recursive
    ChildTreeNode tn = testData.adapter.getChildGeoObjects(parentId, parentTypeCode, childrenTypes, true);
    FastTestDataset.CAMBODIA.assertEquals(tn, childrenTypes, true);
    Assert.assertEquals(tn.toJSON().toString(), ChildTreeNode.fromJSON(tn.toJSON().toString(), testData.adapter).toJSON().toString());

    // Not recursive
    ChildTreeNode tn2 = testData.adapter.getChildGeoObjects(parentId, parentTypeCode, childrenTypes, false);
    FastTestDataset.CAMBODIA.assertEquals(tn2, childrenTypes, false);
    Assert.assertEquals(tn2.toJSON().toString(), ChildTreeNode.fromJSON(tn2.toJSON().toString(), testData.adapter).toJSON().toString());

    // Test only getting districts
    String[] distArr = new String[] { FastTestDataset.CAMBODIA.getCode() };
    ChildTreeNode tn3 = testData.adapter.getChildGeoObjects(parentId, parentTypeCode, distArr, true);
    FastTestDataset.CAMBODIA.assertEquals(tn3, distArr, true);
    Assert.assertEquals(tn3.toJSON().toString(), ChildTreeNode.fromJSON(tn3.toJSON().toString(), testData.adapter).toJSON().toString());

    // Test null children types. We're using Mexico because it has no leaf
    // nodes.
    ChildTreeNode tn4 = testData.adapter.getChildGeoObjects(FastTestDataset.CAMBODIA.getRegistryId(), FastTestDataset.CAMBODIA.getGeoObjectType().getCode(), null, true);
    FastTestDataset.CAMBODIA.assertEquals(tn4, null, true);
    Assert.assertEquals(tn4.toJSON().toString(), ChildTreeNode.fromJSON(tn4.toJSON().toString(), testData.adapter).toJSON().toString());

    // Test empty children types. We're using Mexico because it has no leaf
    // nodes.
    ChildTreeNode tn5 = testData.adapter.getChildGeoObjects(FastTestDataset.CAMBODIA.getRegistryId(), FastTestDataset.CAMBODIA.getGeoObjectType().getCode(), new String[] {}, true);
    FastTestDataset.CAMBODIA.assertEquals(tn5, null, true);
    Assert.assertEquals(tn5.toJSON().toString(), ChildTreeNode.fromJSON(tn5.toJSON().toString(), testData.adapter).toJSON().toString());
  }
  
  @Test
  public void testGetParentGeoObjects()
  {
    String childId = FastTestDataset.PROV_CENTRAL.getRegistryId();
    String childTypeCode = FastTestDataset.PROVINCE.getCode();
    String[] parentTypes = new String[] { FastTestDataset.COUNTRY.getCode() };

    // Recursive
    ParentTreeNode tn = testData.adapter.getParentGeoObjects(childId, childTypeCode, parentTypes, true, null);
    FastTestDataset.PROV_CENTRAL.assertEquals(tn, parentTypes, true);
    Assert.assertEquals(tn.toJSON().toString(), ParentTreeNode.fromJSON(tn.toJSON().toString(), testData.adapter).toJSON().toString());

    // Not recursive
    ParentTreeNode tn2 = testData.adapter.getParentGeoObjects(childId, childTypeCode, parentTypes, false, null);
    FastTestDataset.PROV_CENTRAL.assertEquals(tn2, parentTypes, false);
    Assert.assertEquals(tn2.toJSON().toString(), ParentTreeNode.fromJSON(tn2.toJSON().toString(), testData.adapter).toJSON().toString());

    // Test only getting countries
    String[] countryArr = new String[] { FastTestDataset.COUNTRY.getCode() };
    ParentTreeNode tn3 = testData.adapter.getParentGeoObjects(childId, childTypeCode, countryArr, true, null);
    FastTestDataset.PROV_CENTRAL.assertEquals(tn3, countryArr, true);
    Assert.assertEquals(tn3.toJSON().toString(), ParentTreeNode.fromJSON(tn3.toJSON().toString(), testData.adapter).toJSON().toString());

    // Test null parent types
    ParentTreeNode tn4 = testData.adapter.getParentGeoObjects(childId, childTypeCode, null, true, null);
    FastTestDataset.PROV_CENTRAL.assertEquals(tn4, null, true);
    Assert.assertEquals(tn4.toJSON().toString(), ParentTreeNode.fromJSON(tn4.toJSON().toString(), testData.adapter).toJSON().toString());

    // Test empty parent types
    ParentTreeNode tn5 = testData.adapter.getParentGeoObjects(childId, childTypeCode, new String[] {}, true, null);
    FastTestDataset.PROV_CENTRAL.assertEquals(tn5, null, true);
    Assert.assertEquals(tn5.toJSON().toString(), ParentTreeNode.fromJSON(tn5.toJSON().toString(), testData.adapter).toJSON().toString());
  }

  @Test
  public void testGetHierarchyTypes()
  {
    String[] types = new String[] { FastTestDataset.HIER_ADMIN.getCode() };

    HierarchyType[] hts = testData.adapter.getHierarchyTypes(types);

    Assert.assertEquals(types.length, hts.length);

    HierarchyType locatedIn = hts[0];
//    CAMBODIATestData.assertEqualsHierarchyType(LocatedIn.CLASS, locatedIn);
    Assert.assertEquals(locatedIn.toJSON().toString(), HierarchyType.fromJSON(locatedIn.toJSON().toString(), testData.adapter).toJSON().toString());

    // Test to make sure we can provide no types and get everything back
    HierarchyType[] hts2 = testData.adapter.getHierarchyTypes(new String[] {});
    Assert.assertTrue(hts2.length > 0);

    HierarchyType[] hts3 = testData.adapter.getHierarchyTypes(null);
    Assert.assertTrue(hts3.length > 0);
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
