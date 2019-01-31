package net.geoprism.georegistry.service;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import org.commongeoregistry.adapter.action.AbstractAction;
import org.commongeoregistry.adapter.action.AddChildAction;
import org.commongeoregistry.adapter.action.CreateAction;
import org.commongeoregistry.adapter.action.UpdateAction;
import org.commongeoregistry.adapter.constants.DefaultTerms;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
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
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.GeoEntityQuery;
import com.runwaysdk.system.gis.geo.LocatedIn;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import net.geoprism.georegistry.testframework.TestDataSet.TestGeoObjectInfo;
import net.geoprism.georegistry.testframework.TestDataSet.TestGeoObjectTypeInfo;
import net.geoprism.georegistry.testframework.TestRegistryAdapterClient;
import net.geoprism.georegistry.testframework.USATestData;
import net.geoprism.registry.GeometryTypeException;

public class RegistryServiceTest
{
  protected TestRegistryAdapterClient adapter;

  protected USATestData        testData;

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
    geoObj.setLocalizedDisplayLabel(testData.WASHINGTON.getDisplayLabel());

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
    
    waGeoObj.setWKTGeometry(testData.COLORADO.getWkt());
    waGeoObj.setLocalizedDisplayLabel(testData.COLORADO.getDisplayLabel());
    testUpdateGO.setWkt(testData.COLORADO.getWkt());
    testUpdateGO.setDisplayLabel(testData.COLORADO.getDisplayLabel());
    
    this.adapter.updateGeoObject(waGeoObj.toJSON().toString());
    testUpdateGO.assertApplied();
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

  @Test(expected = SmartExceptionDTO.class)
  public void testUnissuedIdCreate()
  {
    // Test to make sure we can't just provide random ids, they actually have to
    // be issued by our id service

    // Create
    GeoObject geoObj = testData.adapter.newGeoObjectInstance(testData.STATE.getCode());
    geoObj.setWKTGeometry(testData.WASHINGTON.getWkt());
    geoObj.setCode(testData.WASHINGTON.getCode());
    geoObj.setLocalizedDisplayLabel(testData.WASHINGTON.getDisplayLabel());
    geoObj.setUid(UUID.randomUUID().toString());
    this.adapter.createGeoObject(geoObj.toJSON().toString());
  }

  @Test(expected = SmartExceptionDTO.class)
  public void testUnissuedIdUpdate()
  {
    // Test to make sure we can't just provide random ids, they actually have to
    // be issued by our id service

    // Update
    GeoObject waGeoObj = this.adapter.getGeoObject(testData.WASHINGTON.getRegistryId(), testData.WASHINGTON.getGeoObjectType().getCode());
    waGeoObj.setWKTGeometry(testData.COLORADO.getWkt());
    waGeoObj.setLocalizedDisplayLabel(testData.COLORADO.getDisplayLabel());
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
        
        if (jo.get("label").getAsString().equals(got.getDisplayLabel()) && jo.get("code").getAsString().equals(got.getCode()))
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
  public void testActions()
  {
    TestGeoObjectInfo testAddChildParent = testData.newTestGeoObjectInfo("TEST_ACTIONS_ADD_CHILD_PARENT", testData.STATE);
    testAddChildParent.apply();

    TestGeoObjectInfo testAddChild = testData.newTestGeoObjectInfo("TEST_ACTIONS_ADD_CHILD", testData.DISTRICT);
    testAddChild.apply();

    TestGeoObjectInfo testDelete = testData.newTestGeoObjectInfo("TEST_ACTIONS_DELETE_CHILD", testData.STATE);
    testDelete.apply();

    TestGeoObjectInfo testNew = testData.newTestGeoObjectInfo("TEST_ACTIONS_NEW_CHILD", testData.STATE);
    GeoObject goNewChild = testNew.asGeoObject();

    TestGeoObjectTypeInfo testDeleteUni = testData.newTestGeoObjectTypeInfo("TEST_ACTIONS_DELETE_UNI");
    testDeleteUni.apply(GeometryType.POLYGON);
    GeoObjectType gotDelete = testDeleteUni.getGeoObjectType(GeometryType.POLYGON);
    testData.adapter.getMetadataCache().addGeoObjectType(gotDelete);

    AbstractAction[] actions = new AbstractAction[3];
    int i = 0;

    // Add Child
    AddChildAction addChild = new AddChildAction(testAddChild.getRegistryId(), testAddChild.getGeoObjectType().getCode(), testAddChildParent.getRegistryId(), testAddChildParent.getGeoObjectType().getCode(), LocatedIn.class.getSimpleName());
    String addChildJson = addChild.toJSON().toString();
    String addChildJson2 = AddChildAction.fromJSON(addChildJson).toJSON().toString();
    Assert.assertEquals(addChildJson, addChildJson2);
    actions[i++] = addChild;

    // Remove Child ??
    // TODO

    // Create a new GeoObject
    CreateAction create = new CreateAction(goNewChild);
    String createJson = create.toJSON().toString();
    String createJson2 = CreateAction.fromJSON(createJson).toJSON().toString();
    Assert.assertEquals(createJson, createJson2);
    actions[i++] = create;

    // Update the previously created GeoObject
    final String NEW_DISPLAY_LABEL = "NEW_DISPLAY_LABEL";
    goNewChild.setLocalizedDisplayLabel(NEW_DISPLAY_LABEL);
    UpdateAction update = new UpdateAction(goNewChild);
    String updateJson = update.toJSON().toString();
    String updateJson2 = UpdateAction.fromJSON(updateJson).toJSON().toString();
    Assert.assertEquals(updateJson, updateJson2);
    actions[i++] = update;

    // Update a GeoObjectType
    // TODO : This hasn't been implemented yet in RegistryUpdateAction
    // UpdateAction createGOT = new UpdateAction(data.STATE.getGeoObjectType());
    // String createGOTJson = createGOT.toJSON().toString();
    // String createGOTJson2 =
    // UpdateAction.fromJSON(createGOTJson).toJSON().toString();
    // Assert.assertEquals(createGOTJson, createGOTJson2);
    // actions[i++] = createGOT;

    // Delete a GeoObject
    // DeleteAction deleteGO = new DeleteAction(testDelete.newGeoObject());
    // String deleteGOJson = deleteGO.toJSON().toString();
    // String deleteGOJson2 =
    // DeleteAction.fromJSON(deleteGOJson).toJSON().toString();
    // Assert.assertEquals(deleteGOJson, deleteGOJson2);
    // actions[i++] = deleteGO;

    // Delete a GeoObjectType
    // DeleteAction deleteGOT = new DeleteAction(gotDelete);
    // String deleteGOTJson = deleteGOT.toJSON().toString();
    // String deleteGOTJson2 =
    // DeleteAction.fromJSON(deleteGOTJson).toJSON().toString();
    // Assert.assertEquals(deleteGOTJson, deleteGOTJson2);
    // actions[i++] = deleteGOT;

    // Serialize the actions
    String sActions = AbstractAction.serializeActions(actions).toString();
    String sActions2 = AbstractAction.serializeActions(AbstractAction.parseActions(sActions)).toString();
    Assert.assertEquals(sActions, sActions2);

    // Execute the actions
    this.adapter.executeActions(actions);

    // Make sure that the database has been modified correctly
    Assert.assertEquals(1, testAddChildParent.getChildrenAsGeoEntity(LocatedIn.CLASS).getAll().size());

    // GeoEntityQuery delGEQ = new GeoEntityQuery(new QueryFactory());
    // delGEQ.WHERE(delGEQ.getOid().EQ(testDelete.getUid()));
    // Assert.assertEquals(0, delGEQ.getCount());

    assertActions(testNew, NEW_DISPLAY_LABEL);
  }
  @Request
  private void assertActions(TestGeoObjectInfo testNew, final String NEW_DISPLAY_LABEL)
  {
    GeoEntityQuery createGEQ = new GeoEntityQuery(new QueryFactory());
    createGEQ.WHERE(createGEQ.getGeoId().EQ(testNew.getCode()));
    Assert.assertEquals(1, createGEQ.getCount());
    Assert.assertEquals(NEW_DISPLAY_LABEL, createGEQ.getIterator().getAll().get(0).getDisplayLabel().getValue());

    // UniversalQuery delUQ = new UniversalQuery(new QueryFactory());
    // delUQ.WHERE(delUQ.getOid().EQ(testDeleteUni.getUid()));
    // Assert.assertEquals(0, delUQ.getCount());

    // TODO : Response architecture
  }
}
