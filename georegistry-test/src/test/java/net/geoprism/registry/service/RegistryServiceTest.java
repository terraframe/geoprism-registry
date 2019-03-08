package net.geoprism.registry.service;

import java.util.ArrayList;
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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.business.SmartExceptionDTO;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.LocatedIn;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import net.geoprism.registry.RegistryController;
import net.geoprism.registry.GeometryTypeException;
import net.geoprism.registry.test.TestDataSet.TestGeoObjectInfo;
import net.geoprism.registry.test.TestDataSet.TestGeoObjectTypeInfo;
import net.geoprism.registry.test.TestRegistryAdapterClient;
import net.geoprism.registry.test.USATestData;

public class RegistryServiceTest
{
  protected TestRegistryAdapterClient adapter;

  protected USATestData               testData;

  @Before
  public void setUp()
  {
    this.testData = USATestData.newTestData();

    this.adapter = this.testData.adapter;
  }

  @After
  public void tearDown()
  {
    if (this.testData != null)
    {
      testData.cleanUp();
    }
  }

  @Test
  public void testGetGeoObject()
  {
    GeoObject geoObj = this.adapter.getGeoObject(testData.COLORADO.getRegistryId(), testData.COLORADO.getGeoObjectType().getCode());

    testData.COLORADO.assertEquals(geoObj, DefaultTerms.GeoObjectStatusTerm.ACTIVE);
  }

  @Test
  public void testCreateGeoObjectBadGeometry()
  {
    GeometryBuilder builder = new GeometryBuilder(new GeometryFactory());
    Point point = builder.point(48.44, -123.37);

    // 1. Test creating a new one
    GeoObject geoObj = this.adapter.newGeoObjectInstance(testData.STATE.getCode());
    geoObj.setGeometry(point);
    geoObj.setCode(testData.WASHINGTON.getCode());
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, testData.WASHINGTON.getDisplayLabel());

    try
    {
      this.adapter.createGeoObject(geoObj.toJSON().toString());

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
    GeoObject geoObj = this.adapter.getGeoObjectByCode(testData.COLORADO.getCode(), testData.COLORADO.getGeoObjectType().getCode());

    Assert.assertEquals(geoObj.toJSON().toString(), GeoObject.fromJSON(testData.adapter, geoObj.toJSON().toString()).toJSON().toString());
    testData.assertGeoObjectStatus(geoObj, DefaultTerms.GeoObjectStatusTerm.ACTIVE);
  }

  @Test
  public void testUpdateGeoObject()
  {
    TestGeoObjectInfo testUpdateGO = testData.newTestGeoObjectInfo("TEST_UPDATE_GO", testData.STATE);

    // 1. Test creating a new one
    GeoObject geoObj = testUpdateGO.asGeoObject();
    this.adapter.createGeoObject(geoObj.toJSON().toString());
    testUpdateGO.assertApplied();

    // 2. Test updating the one we created earlier
    GeoObject waGeoObj = this.adapter.getGeoObject(geoObj.getUid(), testUpdateGO.getGeoObjectType().getCode());
    LocalizedValue displayLabel = waGeoObj.getDisplayLabel();
    displayLabel.setValue(MdAttributeLocalInfo.DEFAULT_LOCALE, testData.COLORADO.getDisplayLabel());

    waGeoObj.setWKTGeometry(testData.COLORADO.getWkt());
    waGeoObj.setDisplayLabel(displayLabel);
    waGeoObj.setStatus(DefaultTerms.GeoObjectStatusTerm.INACTIVE.code);
    
    testUpdateGO.setWkt(testData.COLORADO.getWkt());
    testUpdateGO.setDisplayLabel(testData.COLORADO.getDisplayLabel());

    GeoObject returnedUpdate = this.adapter.updateGeoObject(waGeoObj.toJSON().toString());
    testUpdateGO.setRegistryId(returnedUpdate.getUid());
    
    // Assert that the database is applied correctly
    testUpdateGO.assertApplied();
    
    // Assert the GeoObject they returned to us is correct
    testUpdateGO.assertEquals(returnedUpdate);
    testData.assertGeoObjectStatus(returnedUpdate, DefaultTerms.GeoObjectStatusTerm.INACTIVE);
    
    // Assert when we fetch our own GeoObject its also correct
    GeoObject freshFetched = this.adapter.getGeoObject(geoObj.getUid(), testUpdateGO.getGeoObjectType().getCode());
    testUpdateGO.assertEquals(returnedUpdate);
    testData.assertGeoObjectStatus(freshFetched, DefaultTerms.GeoObjectStatusTerm.INACTIVE);
  }

  // @Test
  // @Request
  // public void testUpdateUniversal()
  // {
  // TestGeoObjectInfo infoUniSwap =
  // tutil.newTestGeoObjectInfo("TEST_UPDATE_GEO_OBJECT_UNI_SWAP", tutil.STATE);
  // infoUniSwap.apply();
  //
  // // Most basic Universal swap
  // GeoObject goUniSwap =
  // tutil.responseToGeoObject(this.controller.getGeoObjectByCode(this.adminCR,
  // infoUniSwap.getGeoId(), infoUniSwap.getUniversal().getCode()));
  // goUniSwap.setType(this.adapter.getMetadataCache().getGeoObjectType(tutil.DISTRICT.getCode()).get());
  // GeoObject goUniSwap2 =
  // tutil.responseToGeoObject(this.controller.updateGeoObject(this.adminCR,
  // goUniSwap.toJSON().toString()));
  // Assert.assertEquals(tutil.DISTRICT.getCode(),
  // goUniSwap2.getType().getCode());
  //
  // // TODO : Make sure we throw an error if the GeoObject isn't allowed within
  // some other hierarchy
  //
  // // TODO : Validate based on universal tree? I.e. now that its a district
  // its not allowed within something else
  // }

  @Test
  public void testGetUIDS()
  {
    Set<String> ids = this.adapter.getGeoObjectUids(100);

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
    RegistryController controller = new RegistryController();

    RestBodyResponse response = (RestBodyResponse) controller.getGeoObjectSuggestions(testData.adminClientRequest, "Co", testData.STATE.getCode(), testData.USA.getCode(), LocatedIn.class.getSimpleName());
    JsonArray results = (JsonArray) response.serialize();

    Assert.assertEquals(1, results.size());

    JsonObject result = results.get(0).getAsJsonObject();

    Assert.assertEquals(testData.COLORADO.getDisplayLabel(), result.get("name").getAsString());
    Assert.assertEquals(testData.COLORADO.getOid(), result.get("id").getAsString());
    Assert.assertEquals(testData.COLORADO.getCode(), result.get(GeoObject.CODE).getAsString());    
  }

  @Test
  public void testGetGeoObjectSuggestionsNoParent()
  {
    RegistryController controller = new RegistryController();
    
    RestBodyResponse response = (RestBodyResponse) controller.getGeoObjectSuggestions(testData.adminClientRequest, "Co", testData.STATE.getCode(), null, null);
    JsonArray results = (JsonArray) response.serialize();
    
    Assert.assertEquals(1, results.size());
    
    JsonObject result = results.get(0).getAsJsonObject();
    
    Assert.assertEquals(testData.COLORADO.getDisplayLabel(), result.get("name").getAsString());
    Assert.assertEquals(testData.COLORADO.getOid(), result.get("id").getAsString());
    Assert.assertEquals(testData.COLORADO.getCode(), result.get(GeoObject.CODE).getAsString());
  }
  
  /**
   * Test to make sure we can't just provide random ids, they actually have to be issued by our id service
   */
  @Test(expected = SmartExceptionDTO.class)
  public void testUnissuedIdCreate()
  {
    // Create
    GeoObject geoObj = testData.adapter.newGeoObjectInstance(testData.STATE.getCode());
    geoObj.setWKTGeometry(testData.WASHINGTON.getWkt());
    geoObj.setCode(testData.WASHINGTON.getCode());
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, testData.WASHINGTON.getDisplayLabel());
    geoObj.setUid(UUID.randomUUID().toString());
    this.adapter.createGeoObject(geoObj.toJSON().toString());
  }

  /**
   * Test to make sure we can't just provide random ids, they actually have to be issued by our id service
   */
  @Test(expected = SmartExceptionDTO.class)
  public void testUnissuedIdUpdate()
  {
    // Update
    GeoObject waGeoObj = this.adapter.getGeoObject(testData.WASHINGTON.getRegistryId(), testData.WASHINGTON.getGeoObjectType().getCode());
    waGeoObj.setWKTGeometry(testData.COLORADO.getWkt());
    waGeoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, testData.COLORADO.getDisplayLabel());
    waGeoObj.setUid(UUID.randomUUID().toString());
    this.adapter.updateGeoObject(waGeoObj.toJSON().toString());
  }

  @Test
  public void testGetGeoObjectTypes()
  {
    String[] codes = new String[] { testData.STATE.getCode(), testData.DISTRICT.getCode() };

    GeoObjectType[] gots = this.adapter.getGeoObjectTypes(codes);

    Assert.assertEquals(codes.length, gots.length);

    GeoObjectType state = gots[0];
    Assert.assertEquals(state.toJSON().toString(), GeoObjectType.fromJSON(state.toJSON().toString(), testData.adapter).toJSON().toString());
    testData.STATE.assertEquals(state);

    GeoObjectType district = gots[1];
    Assert.assertEquals(district.toJSON().toString(), GeoObjectType.fromJSON(district.toJSON().toString(), testData.adapter).toJSON().toString());
    testData.DISTRICT.assertEquals(district);

    // Test to make sure we can provide none
    GeoObjectType[] gots2 = this.adapter.getGeoObjectTypes(new String[] {});
    Assert.assertTrue(gots2.length > 0);

    GeoObjectType[] gots3 = this.adapter.getGeoObjectTypes(null);
    Assert.assertTrue(gots3.length > 0);
  }

  @Test
  public void testListGeoObjectTypes()
  {
    JsonArray types = this.adapter.listGeoObjectTypes();

    ArrayList<TestGeoObjectTypeInfo> expectedGots = this.testData.getManagedGeoObjectTypes();
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
    ChildTreeNode tn = this.adapter.getChildGeoObjects(parentId, parentTypeCode, childrenTypes, true);
    testData.USA.assertEquals(tn, childrenTypes, true);
    Assert.assertEquals(tn.toJSON().toString(), ChildTreeNode.fromJSON(tn.toJSON().toString(), testData.adapter).toJSON().toString());

    // Not recursive
    ChildTreeNode tn2 = this.adapter.getChildGeoObjects(parentId, parentTypeCode, childrenTypes, false);
    testData.USA.assertEquals(tn2, childrenTypes, false);
    Assert.assertEquals(tn2.toJSON().toString(), ChildTreeNode.fromJSON(tn2.toJSON().toString(), testData.adapter).toJSON().toString());

    // Test only getting districts
    String[] distArr = new String[] { testData.DISTRICT.getCode() };
    ChildTreeNode tn3 = this.adapter.getChildGeoObjects(parentId, parentTypeCode, distArr, true);
    testData.USA.assertEquals(tn3, distArr, true);
    Assert.assertEquals(tn3.toJSON().toString(), ChildTreeNode.fromJSON(tn3.toJSON().toString(), testData.adapter).toJSON().toString());
  }

  @Test
  public void testGetParentGeoObjects()
  {
    String childId = testData.CO_D_TWO.getRegistryId();
    String childTypeCode = testData.DISTRICT.getCode();
    String[] parentTypes = new String[] { testData.COUNTRY.getCode(), testData.STATE.getCode() };

    // Recursive
    ParentTreeNode tn = this.adapter.getParentGeoObjects(childId, childTypeCode, parentTypes, true);
    testData.CO_D_TWO.assertEquals(tn, parentTypes, true);
    Assert.assertEquals(tn.toJSON().toString(), ParentTreeNode.fromJSON(tn.toJSON().toString(), testData.adapter).toJSON().toString());

    // Not recursive
    ParentTreeNode tn2 = this.adapter.getParentGeoObjects(childId, childTypeCode, parentTypes, false);
    testData.CO_D_TWO.assertEquals(tn2, parentTypes, false);
    Assert.assertEquals(tn2.toJSON().toString(), ParentTreeNode.fromJSON(tn2.toJSON().toString(), testData.adapter).toJSON().toString());

    // Test only getting countries
    String[] countryArr = new String[] { testData.COUNTRY.getCode() };
    ParentTreeNode tn3 = this.adapter.getParentGeoObjects(childId, childTypeCode, countryArr, true);
    testData.CO_D_TWO.assertEquals(tn3, countryArr, true);
    Assert.assertEquals(tn3.toJSON().toString(), ParentTreeNode.fromJSON(tn3.toJSON().toString(), testData.adapter).toJSON().toString());
  }

  @Test
  public void testGetHierarchyTypes()
  {
    String[] types = new String[] { LocatedIn.class.getSimpleName() };

    HierarchyType[] hts = this.adapter.getHierarchyTypes(types);

    Assert.assertEquals(types.length, hts.length);

    HierarchyType locatedIn = hts[0];
    USATestData.assertEqualsHierarchyType(LocatedIn.CLASS, locatedIn);
    Assert.assertEquals(locatedIn.toJSON().toString(), HierarchyType.fromJSON(locatedIn.toJSON().toString(), testData.adapter).toJSON().toString());

    // Test to make sure we can provide no types and get everything back
    HierarchyType[] hts2 = this.adapter.getHierarchyTypes(new String[] {});
    Assert.assertTrue(hts2.length > 0);

    HierarchyType[] hts3 = this.adapter.getHierarchyTypes(null);
    Assert.assertTrue(hts3.length > 0);
  }

  @Test
  public void testAddChild()
  {
    TestGeoObjectInfo testAddChild = testData.newTestGeoObjectInfo("TEST_ADD_CHILD", testData.STATE);
    testAddChild.apply();

    ParentTreeNode ptnTestState = this.adapter.addChild(testData.USA.getRegistryId(), testData.USA.getGeoObjectType().getCode(), testAddChild.getRegistryId(), testAddChild.getGeoObjectType().getCode(), LocatedIn.class.getSimpleName());

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

    ChildTreeNode ctnUSA2 = this.adapter.getChildGeoObjects(testData.USA.getRegistryId(), testData.USA.getGeoObjectType().getCode(), new String[] { testData.STATE.getCode() }, false);

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
    this.adapter.removeChild(testData.USA.getRegistryId(), testData.USA.getGeoObjectType().getCode(), testData.COLORADO.getRegistryId(), testData.COLORADO.getGeoObjectType().getCode(), LocatedIn.class.getSimpleName());
    
    /*
     * Fetch the children and validate ours was removed
     */
    ChildTreeNode ctnUSA2 = this.adapter.getChildGeoObjects(testData.USA.getRegistryId(), testData.USA.getGeoObjectType().getCode(), new String[] { testData.STATE.getCode() }, false);

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
