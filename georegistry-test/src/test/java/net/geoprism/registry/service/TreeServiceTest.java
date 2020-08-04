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

import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.json.JSONArray;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.USATestData;

public class TreeServiceTest
{
  protected static USATestData               testData;

  @BeforeClass
  public static void setUpClass()
  {
    testData = USATestData.newTestData();
    testData.setSessionUser(testData.USER_NPS_RA);
    testData.setUpMetadata();
  }
  
  @AfterClass
  public static void cleanUpClass()
  {
    if (testData != null)
    {
      testData.tearDownMetadata();
    }
  }
  
  @Before
  public void setUp()
  {
    if (testData != null)
    {
      testData.setUpInstanceData();
    }
  }

  @After
  public void tearDown()
  {
    if (testData != null)
    {
      testData.tearDownInstanceData();
    }
  }
  
  @Test
  public void testGetHierarchies()
  {
    JSONArray ptn = testData.adapter.getHierarchiesForGeoObjectOverTime(testData.COLORADO.getCode(), testData.STATE.getCode());
    
    Assert.assertTrue(ptn.length() > 0);
  }
  
  @Test
  public void testGetChildGeoObjects()
  {
    String parentId = testData.USA.getRegistryId();
    String parentTypeCode = testData.USA.getGeoObjectType().getCode();
    String[] childrenTypes = new String[] { testData.STATE.getCode(), testData.DISTRICT.getCode() };

    // Recursive
    ChildTreeNode tn = testData.adapter.getChildGeoObjects(parentId, parentTypeCode, childrenTypes, true);
    testData.USA.assertEquals(tn, childrenTypes, true);
    Assert.assertEquals(tn.toJSON().toString(), ChildTreeNode.fromJSON(tn.toJSON().toString(), testData.adapter).toJSON().toString());

    // Not recursive
    ChildTreeNode tn2 = testData.adapter.getChildGeoObjects(parentId, parentTypeCode, childrenTypes, false);
    testData.USA.assertEquals(tn2, childrenTypes, false);
    Assert.assertEquals(tn2.toJSON().toString(), ChildTreeNode.fromJSON(tn2.toJSON().toString(), testData.adapter).toJSON().toString());

    // Test only getting districts
    String[] distArr = new String[] { testData.DISTRICT.getCode() };
    ChildTreeNode tn3 = testData.adapter.getChildGeoObjects(parentId, parentTypeCode, distArr, true);
    testData.USA.assertEquals(tn3, distArr, true);
    Assert.assertEquals(tn3.toJSON().toString(), ChildTreeNode.fromJSON(tn3.toJSON().toString(), testData.adapter).toJSON().toString());

    // Test null children types. We're using Mexico because it has no leaf
    // nodes.
    ChildTreeNode tn4 = testData.adapter.getChildGeoObjects(testData.MEXICO.getRegistryId(), testData.MEXICO.getGeoObjectType().getCode(), null, true);
    testData.MEXICO.assertEquals(tn4, null, true);
    Assert.assertEquals(tn4.toJSON().toString(), ChildTreeNode.fromJSON(tn4.toJSON().toString(), testData.adapter).toJSON().toString());

    // Test empty children types. We're using Mexico because it has no leaf
    // nodes.
    ChildTreeNode tn5 = testData.adapter.getChildGeoObjects(testData.MEXICO.getRegistryId(), testData.MEXICO.getGeoObjectType().getCode(), new String[] {}, true);
    testData.MEXICO.assertEquals(tn5, null, true);
    Assert.assertEquals(tn5.toJSON().toString(), ChildTreeNode.fromJSON(tn5.toJSON().toString(), testData.adapter).toJSON().toString());
  }
  
  @Test
  public void testGetParentGeoObjects()
  {
    String childId = testData.CO_D_TWO.getRegistryId();
    String childTypeCode = testData.DISTRICT.getCode();
    String[] parentTypes = new String[] { testData.COUNTRY.getCode(), testData.STATE.getCode() };

    // Recursive
    ParentTreeNode tn = testData.adapter.getParentGeoObjects(childId, childTypeCode, parentTypes, true, null);
    testData.CO_D_TWO.assertEquals(tn, parentTypes, true);
    Assert.assertEquals(tn.toJSON().toString(), ParentTreeNode.fromJSON(tn.toJSON().toString(), testData.adapter).toJSON().toString());

    // Not recursive
    ParentTreeNode tn2 = testData.adapter.getParentGeoObjects(childId, childTypeCode, parentTypes, false, null);
    testData.CO_D_TWO.assertEquals(tn2, parentTypes, false);
    Assert.assertEquals(tn2.toJSON().toString(), ParentTreeNode.fromJSON(tn2.toJSON().toString(), testData.adapter).toJSON().toString());

    // Test only getting countries
    String[] countryArr = new String[] { testData.COUNTRY.getCode() };
    ParentTreeNode tn3 = testData.adapter.getParentGeoObjects(childId, childTypeCode, countryArr, true, null);
    testData.CO_D_TWO.assertEquals(tn3, countryArr, true);
    Assert.assertEquals(tn3.toJSON().toString(), ParentTreeNode.fromJSON(tn3.toJSON().toString(), testData.adapter).toJSON().toString());

    // Test null parent types
    ParentTreeNode tn4 = testData.adapter.getParentGeoObjects(childId, childTypeCode, null, true, null);
    testData.CO_D_TWO.assertEquals(tn4, null, true);
    Assert.assertEquals(tn4.toJSON().toString(), ParentTreeNode.fromJSON(tn4.toJSON().toString(), testData.adapter).toJSON().toString());

    // Test empty parent types
    ParentTreeNode tn5 = testData.adapter.getParentGeoObjects(childId, childTypeCode, new String[] {}, true, null);
    testData.CO_D_TWO.assertEquals(tn5, null, true);
    Assert.assertEquals(tn5.toJSON().toString(), ParentTreeNode.fromJSON(tn5.toJSON().toString(), testData.adapter).toJSON().toString());
  }
}
