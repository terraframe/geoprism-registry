package net.geoprism.georegistry.service;

import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.commongeoregistry.adapter.RegistryAdapter;
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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.business.SmartExceptionDTO;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.GeoEntityQuery;
import com.runwaysdk.system.gis.geo.LocatedIn;

import net.geoprism.georegistry.RegistryController;
import net.geoprism.georegistry.service.RegistryIdService;
import net.geoprism.georegistry.service.ServiceFactory;
import net.geoprism.georegistry.testframework.USATestData;
import net.geoprism.georegistry.testframework.USATestData.TestGeoObjectInfo;
import net.geoprism.georegistry.testframework.USATestData.TestGeoObjectTypeInfo;

public class RegistryServiceTest
{
  protected RegistryAdapter    adapter;

  protected USATestData        tutil;

  protected RegistryController controller;

  protected ClientRequestIF    adminCR;

  @Before
  public void setUp()
  {
    this.controller = new RegistryController();

    this.tutil = USATestData.newTestData();

    this.adminCR = tutil.adminClientRequest;

    this.adapter = ServiceFactory.getAdapter();
  }

  @After
  public void tearDown()
  {
    if (this.tutil != null)
    {
      tutil.cleanUp();
    }
  }

  @Test
  @Request
  public void testGetGeoObject()
  {
    GeoObject geoObj = tutil.responseToGeoObject(this.controller.getGeoObject(this.adminCR, tutil.COLORADO.getRegistryId(), tutil.COLORADO.getUniversal().getCode()));

    Assert.assertEquals(geoObj.toJSON().toString(), GeoObject.fromJSON(tutil.adapter, geoObj.toJSON().toString()).toJSON().toString());
    tutil.COLORADO.assertEquals(geoObj, DefaultTerms.GeoObjectStatusTerm.ACTIVE);
  }

  @Test
  @Request
  public void testGetGeoObjectByCode()
  {
    GeoObject geoObj = tutil.responseToGeoObject(this.controller.getGeoObjectByCode(this.adminCR, tutil.COLORADO.getGeoId(), tutil.COLORADO.getUniversal().getCode()));

    Assert.assertEquals(geoObj.toJSON().toString(), GeoObject.fromJSON(tutil.adapter, geoObj.toJSON().toString()).toJSON().toString());
    tutil.COLORADO.assertEquals(geoObj, DefaultTerms.GeoObjectStatusTerm.ACTIVE);
  }

  @Test
  @Request
  public void testUpdateGeoObject()
  {
    tutil.WASHINGTON.delete();

    // 1. Test creating a new one
    GeoObject geoObj = tutil.adapter.newGeoObjectInstance(tutil.STATE.getCode());
    geoObj.setWKTGeometry(tutil.WASHINGTON.getWkt());
    geoObj.setCode(tutil.WASHINGTON.getGeoId());
    geoObj.setLocalizedDisplayLabel(tutil.WASHINGTON.getDisplayLabel());
    this.controller.createGeoObject(this.adminCR, geoObj.toJSON().toString());

    GeoEntity waGeo = GeoEntity.getByKey(tutil.WASHINGTON.getGeoId());
    Assert.assertEquals(StringUtils.deleteWhitespace(tutil.WASHINGTON.getWkt()), StringUtils.deleteWhitespace(waGeo.getWkt()));
    Assert.assertEquals(tutil.WASHINGTON.getGeoId(), waGeo.getGeoId());
    Assert.assertEquals(tutil.WASHINGTON.getDisplayLabel(), waGeo.getDisplayLabel().getValue());

    // 2. Test updating the one we created earlier
    GeoObject waGeoObj = tutil.responseToGeoObject(this.controller.getGeoObject(this.adminCR, geoObj.getUid(), waGeo.getUniversal().getUniversalId()));
    waGeoObj.setWKTGeometry(tutil.COLORADO.getWkt());
    waGeoObj.setLocalizedDisplayLabel(tutil.COLORADO.getDisplayLabel());
    this.controller.updateGeoObject(this.adminCR, waGeoObj.toJSON().toString());

    GeoEntity waGeo2 = GeoEntity.getByKey(tutil.WASHINGTON.getGeoId());
    Assert.assertEquals(StringUtils.deleteWhitespace(tutil.COLORADO.getWkt()), StringUtils.deleteWhitespace(waGeo2.getWkt()));
    Assert.assertEquals(tutil.COLORADO.getDisplayLabel(), waGeo2.getDisplayLabel().getValue());
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
  @Request
  public void testGetUIDS()
  {
    String[] ids = tutil.responseToStringArray(this.controller.getUIDs(this.adminCR, 100));

    Assert.assertEquals(100, ids.length);

    for (String id : ids)
    {
      Assert.assertTrue(RegistryIdService.getInstance().isIssuedId(id));
    }
  }

  @Test(expected = SmartExceptionDTO.class)
  @Request
  public void testUnissuedIdCreate()
  {
    // Test to make sure we can't just provide random ids, they actually have to
    // be issued by our id service

    // Create
    GeoObject geoObj = tutil.adapter.newGeoObjectInstance(tutil.STATE.getCode());
    geoObj.setWKTGeometry(tutil.WASHINGTON.getWkt());
    geoObj.setCode(tutil.WASHINGTON.getGeoId());
    geoObj.setLocalizedDisplayLabel(tutil.WASHINGTON.getDisplayLabel());
    geoObj.setUid(UUID.randomUUID().toString());
    this.controller.createGeoObject(this.adminCR, geoObj.toJSON().toString());
  }

  @Test(expected = SmartExceptionDTO.class)
  @Request
  public void testUnissuedIdUpdate()
  {
    // Test to make sure we can't just provide random ids, they actually have to
    // be issued by our id service

    // Update
    GeoObject waGeoObj = tutil.responseToGeoObject(this.controller.getGeoObject(this.adminCR, tutil.WASHINGTON.getRegistryId(), tutil.WASHINGTON.getUniversal().getCode()));
    waGeoObj.setWKTGeometry(tutil.COLORADO.getWkt());
    waGeoObj.setLocalizedDisplayLabel(tutil.COLORADO.getDisplayLabel());
    waGeoObj.setUid(UUID.randomUUID().toString());
    this.controller.updateGeoObject(this.adminCR, waGeoObj.toJSON().toString());
  }

  @Test
  public void testGetGeoObjectTypes()
  {
    String[] codes = new String[] { tutil.STATE.getCode(), tutil.DISTRICT.getCode() };
    String saCodes = tutil.serialize(codes);

    GeoObjectType[] gots = tutil.responseToGeoObjectTypes(this.controller.getGeoObjectTypes(this.adminCR, saCodes));

    Assert.assertEquals(codes.length, gots.length);

    GeoObjectType state = gots[0];
    Assert.assertEquals(state.toJSON().toString(), GeoObjectType.fromJSON(state.toJSON().toString(), tutil.adapter).toJSON().toString());
    tutil.STATE.assertEquals(state);

    GeoObjectType district = gots[1];
    Assert.assertEquals(district.toJSON().toString(), GeoObjectType.fromJSON(district.toJSON().toString(), tutil.adapter).toJSON().toString());
    tutil.DISTRICT.assertEquals(district);

    // Test to make sure we can provide none
    GeoObjectType[] gots2 = tutil.responseToGeoObjectTypes(this.controller.getGeoObjectTypes(this.adminCR, "[]"));
    Assert.assertTrue(gots2.length > 0);

    GeoObjectType[] gots3 = tutil.responseToGeoObjectTypes(this.controller.getGeoObjectTypes(this.adminCR, null));
    Assert.assertTrue(gots3.length > 0);
  }

  @Test
  public void testListGeoObjectTypes()
  {
    RestBodyResponse response = (RestBodyResponse) this.controller.listGeoObjectTypes(this.tutil.adminClientRequest);
    JsonArray types = (JsonArray) response.serialize();

    Assert.assertEquals(8, types.size());

    JsonObject object = types.get(1).getAsJsonObject();

    Assert.assertEquals("Commune", object.get("label").getAsString());
    Assert.assertEquals("Cambodia_Commune", object.get("code").getAsString());
  }

  @Test
  @Request
  public void testGetChildGeoObjects()
  {
    String parentId = tutil.USA.getRegistryId();
    String parentTypeCode = tutil.USA.getUniversal().getCode();
    String[] childrenTypes = new String[] { tutil.STATE.getCode(), tutil.DISTRICT.getCode() };
    String saChildrenTypes = tutil.serialize(childrenTypes);

    // Recursive
    ChildTreeNode tn = tutil.responseToChildTreeNode(this.controller.getChildGeoObjects(this.adminCR, parentId, parentTypeCode, saChildrenTypes, true));
    tutil.USA.assertEquals(tn, childrenTypes, true);
    Assert.assertEquals(tn.toJSON().toString(), ChildTreeNode.fromJSON(tn.toJSON().toString(), tutil.adapter).toJSON().toString());

    // Not recursive
    ChildTreeNode tn2 = tutil.responseToChildTreeNode(this.controller.getChildGeoObjects(this.adminCR, parentId, parentTypeCode, saChildrenTypes, false));
    tutil.USA.assertEquals(tn2, childrenTypes, false);
    Assert.assertEquals(tn2.toJSON().toString(), ChildTreeNode.fromJSON(tn2.toJSON().toString(), tutil.adapter).toJSON().toString());

    // Test only getting districts
    String[] distArr = new String[] { tutil.DISTRICT.getCode() };
    String saDistArr = tutil.serialize(distArr);
    ChildTreeNode tn3 = tutil.responseToChildTreeNode(this.controller.getChildGeoObjects(this.adminCR, parentId, parentTypeCode, saDistArr, true));
    tutil.USA.assertEquals(tn3, distArr, true);
    Assert.assertEquals(tn3.toJSON().toString(), ChildTreeNode.fromJSON(tn3.toJSON().toString(), tutil.adapter).toJSON().toString());
  }

  @Test
  @Request
  public void testGetParentGeoObjects()
  {
    String childId = tutil.CO_D_TWO.getRegistryId();
    String childTypeCode = tutil.DISTRICT.getCode();
    String[] childrenTypes = new String[] { tutil.COUNTRY.getCode(), tutil.STATE.getCode() };
    String saChildrenTypes = tutil.serialize(childrenTypes);

    // Recursive
    ParentTreeNode tn = tutil.responseToParentTreeNode(this.controller.getParentGeoObjects(this.adminCR, childId, childTypeCode, saChildrenTypes, true));
    tutil.CO_D_TWO.assertEquals(tn, childrenTypes, true);
    Assert.assertEquals(tn.toJSON().toString(), ParentTreeNode.fromJSON(tn.toJSON().toString(), tutil.adapter).toJSON().toString());

    // Not recursive
    ParentTreeNode tn2 = tutil.responseToParentTreeNode(this.controller.getParentGeoObjects(this.adminCR, childId, childTypeCode, saChildrenTypes, false));
    tutil.CO_D_TWO.assertEquals(tn2, childrenTypes, false);
    Assert.assertEquals(tn2.toJSON().toString(), ParentTreeNode.fromJSON(tn2.toJSON().toString(), tutil.adapter).toJSON().toString());

    // Test only getting countries
    String[] countryArr = new String[] { tutil.COUNTRY.getCode() };
    String saCountryArr = tutil.serialize(countryArr);
    ParentTreeNode tn3 = tutil.responseToParentTreeNode(this.controller.getParentGeoObjects(this.adminCR, childId, childTypeCode, saCountryArr, true));
    tutil.CO_D_TWO.assertEquals(tn3, countryArr, true);
    Assert.assertEquals(tn3.toJSON().toString(), ParentTreeNode.fromJSON(tn3.toJSON().toString(), tutil.adapter).toJSON().toString());
  }

  @Test
  @Request
  public void testGetHierarchyTypes()
  {
    String[] types = new String[] { LocatedIn.class.getSimpleName() };

    HierarchyType[] hts = tutil.responseToHierarchyTypes(this.controller.getHierarchyTypes(this.adminCR, tutil.serialize(types)));

    Assert.assertEquals(types.length, hts.length);

    HierarchyType locatedIn = hts[0];
    USATestData.assertEqualsHierarchyType(LocatedIn.CLASS, locatedIn);
    Assert.assertEquals(locatedIn.toJSON().toString(), HierarchyType.fromJSON(locatedIn.toJSON().toString(), tutil.adapter).toJSON().toString());

    // Test to make sure we can provide no types and get everything back
    HierarchyType[] hts2 = tutil.responseToHierarchyTypes(this.controller.getHierarchyTypes(this.adminCR, tutil.serialize(new String[] {})));
    Assert.assertTrue(hts2.length > 0);

    HierarchyType[] hts3 = tutil.responseToHierarchyTypes(this.controller.getHierarchyTypes(this.adminCR, null));
    Assert.assertTrue(hts3.length > 0);
  }

  @Test
  @Request
  public void testAddChild()
  {
    TestGeoObjectInfo testAddChild = tutil.newTestGeoObjectInfo("TEST_ADD_CHILD", tutil.STATE);
    testAddChild.apply();

    ParentTreeNode ptnTestState = tutil.responseToParentTreeNode(this.controller.addChild(this.adminCR, tutil.USA.getRegistryId(), tutil.USA.getUniversal().getCode(), testAddChild.getRegistryId(), testAddChild.getUniversal().getCode(), LocatedIn.class.getSimpleName()));

    boolean found = false;
    for (ParentTreeNode ptnUSA : ptnTestState.getParents())
    {
      if (ptnUSA.getGeoObject().getCode().equals(tutil.USA.getGeoId()))
      {
        found = true;
        break;
      }
    }
    Assert.assertTrue("Did not find our test object in the list of returned children", found);
    testAddChild.assertEquals(ptnTestState.getGeoObject());

    ChildTreeNode ctnUSA2 = tutil.responseToChildTreeNode(this.controller.getChildGeoObjects(this.adminCR, tutil.USA.getRegistryId(), tutil.USA.getUniversal().getCode(), tutil.serialize(new String[] { tutil.STATE.getCode() }), false));

    found = false;
    for (ChildTreeNode ctnState : ctnUSA2.getChildren())
    {
      if (ctnState.getGeoObject().getCode().equals(testAddChild.getGeoId()))
      {
        found = true;
        break;
      }
    }
    Assert.assertTrue("Did not find our test object in the list of returned children", found);
  }

  @Test
  @Request
  public void testActions()
  {
    TestGeoObjectInfo testAddChildParent = tutil.newTestGeoObjectInfo("TEST_ACTIONS_ADD_CHILD_PARENT", tutil.STATE);
    testAddChildParent.apply();

    TestGeoObjectInfo testAddChild = tutil.newTestGeoObjectInfo("TEST_ACTIONS_ADD_CHILD", tutil.DISTRICT);
    testAddChild.apply();

    TestGeoObjectInfo testDelete = tutil.newTestGeoObjectInfo("TEST_ACTIONS_DELETE_CHILD", tutil.STATE);
    testDelete.apply();

    TestGeoObjectInfo testNew = tutil.newTestGeoObjectInfo("TEST_ACTIONS_NEW_CHILD", tutil.STATE);
    GeoObject goNewChild = testNew.getGeoObject();

    TestGeoObjectTypeInfo testDeleteUni = tutil.newTestGeoObjectTypeInfo("TEST_ACTIONS_DELETE_UNI");
    testDeleteUni.apply(GeometryType.POLYGON);
    GeoObjectType gotDelete = testDeleteUni.getGeoObjectType(GeometryType.POLYGON);
    tutil.adapter.getMetadataCache().addGeoObjectType(gotDelete);

    AbstractAction[] actions = new AbstractAction[3];
    int i = 0;

    // Add Child
    AddChildAction addChild = new AddChildAction(testAddChild.getRegistryId(), testAddChild.getUniversal().getCode(), testAddChildParent.getRegistryId(), testAddChildParent.getUniversal().getCode(), LocatedIn.class.getSimpleName());
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
    this.controller.executeActions(this.adminCR, sActions);

    // Make sure that the database has been modified correctly
    Assert.assertEquals(1, testAddChildParent.getChildren(LocatedIn.CLASS).getAll().size());

    // GeoEntityQuery delGEQ = new GeoEntityQuery(new QueryFactory());
    // delGEQ.WHERE(delGEQ.getOid().EQ(testDelete.getUid()));
    // Assert.assertEquals(0, delGEQ.getCount());

    GeoEntityQuery createGEQ = new GeoEntityQuery(new QueryFactory());
    createGEQ.WHERE(createGEQ.getGeoId().EQ(testNew.getGeoId()));
    Assert.assertEquals(1, createGEQ.getCount());
    Assert.assertEquals(NEW_DISPLAY_LABEL, createGEQ.getIterator().getAll().get(0).getDisplayLabel().getValue());

    // UniversalQuery delUQ = new UniversalQuery(new QueryFactory());
    // delUQ.WHERE(delUQ.getOid().EQ(testDeleteUni.getUid()));
    // Assert.assertEquals(0, delUQ.getCount());

    // TODO : Response architecture
  }
}
