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
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import org.commongeoregistry.adapter.constants.DefaultTerms;
import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.business.SmartExceptionDTO;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.LocatedIn;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import net.geoprism.registry.GeometryTypeException;
import net.geoprism.registry.RegistryController;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.TestGeoObjectTypeInfo;
import net.geoprism.registry.test.USATestData;

public class RegistryServiceTest
{
  protected static USATestData testData;

  @BeforeClass
  public static void setUpClass()
  {
    testData = USATestData.newTestDataForClass();
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
  public void testGetGeoObject()
  {
    GeoObject geoObj = testData.adapter.getGeoObject(testData.COLORADO.getRegistryId(), testData.COLORADO.getGeoObjectType().getCode());

    testData.COLORADO.assertEquals(geoObj, DefaultTerms.GeoObjectStatusTerm.ACTIVE);
  }

  @Test
  public void testCreateGeoObjectBadGeometry()
  {
    GeometryBuilder builder = new GeometryBuilder(new GeometryFactory());
    Point point = builder.point(48.44, -123.37);

    // 1. Test creating a new one
    GeoObject geoObj = testData.adapter.newGeoObjectInstance(testData.STATE.getCode());
    geoObj.setGeometry(point);
    geoObj.setCode(testData.COLORADO.getCode());
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, testData.COLORADO.getDisplayLabel());

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
  public void testGetGeoObjectByCode()
  {
    GeoObject geoObj = testData.adapter.getGeoObjectByCode(testData.COLORADO.getCode(), testData.COLORADO.getGeoObjectType().getCode());

    Assert.assertEquals(geoObj.toJSON().toString(), GeoObject.fromJSON(testData.adapter, geoObj.toJSON().toString()).toJSON().toString());
    testData.assertGeoObjectStatus(geoObj, DefaultTerms.GeoObjectStatusTerm.ACTIVE);
  }

  @Test
  public void testUpdateGeoObject()
  {
    TestGeoObjectInfo testUpdateGO = testData.newTestGeoObjectInfo("TEST_UPDATE_GO", testData.STATE);

    // 1. Test creating a new one
    GeoObject geoObj = testUpdateGO.asGeoObject();
    testData.adapter.createGeoObject(geoObj.toJSON().toString());
    testUpdateGO.assertApplied();

    // 2. Test updating the one we created earlier
    GeoObject waGeoObj = testData.adapter.getGeoObject(geoObj.getUid(), testUpdateGO.getGeoObjectType().getCode());
    LocalizedValue displayLabel = waGeoObj.getDisplayLabel();
    displayLabel.setValue(MdAttributeLocalInfo.DEFAULT_LOCALE, testData.COLORADO.getDisplayLabel());

    waGeoObj.setWKTGeometry(testData.COLORADO.getWkt());
    waGeoObj.setDisplayLabel(displayLabel);
    waGeoObj.setStatus(DefaultTerms.GeoObjectStatusTerm.INACTIVE.code);

    testUpdateGO.setWkt(testData.COLORADO.getWkt());
    testUpdateGO.setDisplayLabel(testData.COLORADO.getDisplayLabel());

    GeoObject returnedUpdate = testData.adapter.updateGeoObject(waGeoObj.toJSON().toString());
    testUpdateGO.setRegistryId(returnedUpdate.getUid());

    // Assert that the database is applied correctly
    testUpdateGO.assertApplied();

    // Assert the GeoObject they returned to us is correct
    testUpdateGO.assertEquals(returnedUpdate);
    testData.assertGeoObjectStatus(returnedUpdate, DefaultTerms.GeoObjectStatusTerm.INACTIVE);

    // Assert when we fetch our own GeoObject its also correct
    GeoObject freshFetched = testData.adapter.getGeoObject(geoObj.getUid(), testUpdateGO.getGeoObjectType().getCode());
    testUpdateGO.assertEquals(returnedUpdate);
    testData.assertGeoObjectStatus(freshFetched, DefaultTerms.GeoObjectStatusTerm.INACTIVE);
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
    JSONArray results = testData.adapter.getGeoObjectSuggestions("Co", testData.STATE.getCode(), testData.USA.getCode(), LocatedIn.class.getSimpleName(), null);

    Assert.assertEquals(1, results.length());

    JSONObject result = results.getJSONObject(0);

    Assert.assertEquals(testData.COLORADO.getDisplayLabel(), result.getString("name"));
    Assert.assertEquals(testData.COLORADO.getOid(), result.getString("id"));
    Assert.assertEquals(testData.COLORADO.getCode(), result.getString(GeoObject.CODE));
  }

  @Test
  public void testGetGeoObjectSuggestionsNoParent()
  {
    JSONArray results = testData.adapter.getGeoObjectSuggestions("Co", testData.STATE.getCode(), null, null, null);

    Assert.assertEquals(1, results.length());

    JSONObject result = results.getJSONObject(0);

    Assert.assertEquals(testData.COLORADO.getDisplayLabel(), result.getString("name"));
    Assert.assertEquals(testData.COLORADO.getOid(), result.getString("id"));
    Assert.assertEquals(testData.COLORADO.getCode(), result.getString(GeoObject.CODE));
  }

  /**
   * Test to make sure we can't just provide random ids, they actually have to
   * be issued by our id service
   */
  @Test(expected = SmartExceptionDTO.class)
  public void testUnissuedIdCreate()
  {
    // Create
    GeoObject geoObj = testData.adapter.newGeoObjectInstance(testData.STATE.getCode());
    geoObj.setWKTGeometry(testData.COLORADO.getWkt());
    geoObj.setCode(testData.COLORADO.getCode());
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, testData.COLORADO.getDisplayLabel());
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
    GeoObject waGeoObj = testData.adapter.getGeoObject(testData.COLORADO.getRegistryId(), testData.COLORADO.getGeoObjectType().getCode());
    waGeoObj.setWKTGeometry(testData.COLORADO.getWkt());
    waGeoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, testData.COLORADO.getDisplayLabel());
    waGeoObj.setUid(UUID.randomUUID().toString());
    testData.adapter.updateGeoObject(waGeoObj.toJSON().toString());
  }

  @Test
  public void testGetGeoObjectTypes()
  {
    String[] codes = new String[] { testData.STATE.getCode(), testData.DISTRICT.getCode() };

    GeoObjectType[] gots = testData.adapter.getGeoObjectTypes(codes);

    Assert.assertEquals(codes.length, gots.length);

    GeoObjectType state = gots[0];
    Assert.assertEquals(state.toJSON().toString(), GeoObjectType.fromJSON(state.toJSON().toString(), testData.adapter).toJSON().toString());
    testData.STATE.assertEquals(state);

    GeoObjectType district = gots[1];
    Assert.assertEquals(district.toJSON().toString(), GeoObjectType.fromJSON(district.toJSON().toString(), testData.adapter).toJSON().toString());
    testData.DISTRICT.assertEquals(district);

    // Test to make sure we can provide none
    GeoObjectType[] gots2 = testData.adapter.getGeoObjectTypes(new String[] {});
    Assert.assertTrue(gots2.length > 0);

    GeoObjectType[] gots3 = testData.adapter.getGeoObjectTypes(null);
    Assert.assertTrue(gots3.length > 0);
  }

  @Test
  public void testListGeoObjectTypes()
  {
    JsonArray types = testData.adapter.listGeoObjectTypes();

    ArrayList<TestGeoObjectTypeInfo> expectedGots = testData.getManagedGeoObjectTypes();
    for (TestGeoObjectTypeInfo got : expectedGots)
    {
      boolean found = false;

      for (int i = 0; i < types.size(); ++i)
      {
        JsonObject jo = types.get(i).getAsJsonObject();

        if (jo.get("label").getAsString().equals(got.getDisplayLabel().getValue()) && jo.get("code").getAsString().equals(got.getCode()))
        {
          found = true;
        }
      }

      Assert.assertTrue(found);
    }
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

  @Test
  public void testGetHierarchyTypes()
  {
    String[] types = new String[] { LocatedIn.class.getSimpleName() };

    HierarchyType[] hts = testData.adapter.getHierarchyTypes(types);

    Assert.assertEquals(types.length, hts.length);

    HierarchyType locatedIn = hts[0];
    USATestData.assertEqualsHierarchyType(LocatedIn.CLASS, locatedIn);
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
    TestGeoObjectInfo testAddChild = testData.newTestGeoObjectInfo("TEST_ADD_CHILD", testData.STATE);
    testAddChild.apply(null);

    ParentTreeNode ptnTestState = testData.adapter.addChild(testData.USA.getRegistryId(), testData.USA.getGeoObjectType().getCode(), testAddChild.getRegistryId(), testAddChild.getGeoObjectType().getCode(), LocatedIn.class.getSimpleName());

    boolean found = false;
    for (ParentTreeNode ptnUSA : ptnTestState.getParents())
    {
      if (ptnUSA.getGeoObject().getCode().equals(testData.USA.getCode()))
      {
        found = true;
        break;
      }
    }
    Assert.assertTrue("Did not find our test object in the list of returned children", found);
    testAddChild.assertEquals(ptnTestState.getGeoObject());

    ChildTreeNode ctnUSA2 = testData.adapter.getChildGeoObjects(testData.USA.getRegistryId(), testData.USA.getGeoObjectType().getCode(), new String[] { testData.STATE.getCode() }, false);

    found = false;
    for (ChildTreeNode ctnState : ctnUSA2.getChildren())
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
    testData.adapter.removeChild(testData.USA.getRegistryId(), testData.USA.getGeoObjectType().getCode(), testData.COLORADO.getRegistryId(), testData.COLORADO.getGeoObjectType().getCode(), LocatedIn.class.getSimpleName());

    /*
     * Fetch the children and validate ours was removed
     */
    ChildTreeNode ctnUSA2 = testData.adapter.getChildGeoObjects(testData.USA.getRegistryId(), testData.USA.getGeoObjectType().getCode(), new String[] { testData.STATE.getCode() }, false);

    boolean found = false;
    for (ChildTreeNode ctnState : ctnUSA2.getChildren())
    {
      if (ctnState.getGeoObject().getCode().equals(testData.COLORADO.getCode()))
      {
        found = true;
        break;
      }
    }
    Assert.assertFalse("Did not expect Colorado to be a child of USA (because we deleted it earlier).", found);
  }
}
