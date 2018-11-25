package net.geoprism.registry;

import net.geoprism.georegistry.service.RegistryIdService;
import net.geoprism.registry.USATestData.TestGeoObjectInfo;
import net.geoprism.registry.USATestData.TestGeoObjectTypeInfo;

import org.apache.commons.lang.StringUtils;
import org.commongeoregistry.adapter.action.AbstractAction;
import org.commongeoregistry.adapter.action.AddChildAction;
import org.commongeoregistry.adapter.action.CreateAction;
import org.commongeoregistry.adapter.action.UpdateAction;
import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.GeoEntityQuery;
import com.runwaysdk.system.gis.geo.LocatedIn;

public class RegistryServiceTest
{
  protected static USATestData data;
  
  @Before
  public void setUp()
  {
    data = USATestData.newTestData();
  }
  
  @After
  public void tearDown()
  {
    data.cleanUp();
  }
  
  @Test
  @Request
  public void testGetGeoObject()
  {
    GeoObject geoObj = data.registryService.getGeoObject(data.systemSession.getSessionId(), data.COLORADO.getRegistryId(), data.COLORADO.getUniversal().getCode());
    
    Assert.assertEquals(geoObj.toJSON().toString(), GeoObject.fromJSON(data.adapter, geoObj.toJSON().toString()).toJSON().toString());
    data.COLORADO.assertEquals(geoObj);
  }
  
  @Test
  @Request
  public void testGetGeoObjectByCode()
  {
    GeoObject geoObj = data.registryService.getGeoObjectByCode(data.systemSession.getSessionId(), data.COLORADO.getGeoId(), data.COLORADO.getUniversal().getCode());
    
    Assert.assertEquals(geoObj.toJSON().toString(), GeoObject.fromJSON(data.adapter, geoObj.toJSON().toString()).toJSON().toString());
    data.COLORADO.assertEquals(geoObj);
  }
  
  @Test
  @Request
  public void testUpdateGeoObject()
  {
    data.WASHINGTON.delete();
    
    // 1. Test creating a new one
    GeoObject geoObj = data.adapter.newGeoObjectInstance(data.STATE.getCode());
    geoObj.setWKTGeometry(data.WASHINGTON.getWkt());
    geoObj.setCode(data.WASHINGTON.getGeoId());
    geoObj.setLocalizedDisplayLabel(data.WASHINGTON.getDisplayLabel());
    data.registryService.createGeoObject(data.systemSession.getSessionId(), geoObj.toJSON().toString());
    
    GeoEntity waGeo = GeoEntity.getByKey(data.WASHINGTON.getGeoId());
    Assert.assertEquals(StringUtils.deleteWhitespace(data.WASHINGTON.getWkt()), StringUtils.deleteWhitespace(waGeo.getWkt()));
    Assert.assertEquals(data.WASHINGTON.getGeoId(), waGeo.getGeoId());
    Assert.assertEquals(data.WASHINGTON.getDisplayLabel(), waGeo.getDisplayLabel().getValue());
    
    // 2. Test updating the one we created earlier
    GeoObject waGeoObj = data.registryService.getGeoObject(data.systemSession.getSessionId(), geoObj.getUid(), waGeo.getUniversal().getUniversalId());
    waGeoObj.setWKTGeometry(data.COLORADO.getWkt());
    waGeoObj.setLocalizedDisplayLabel(data.COLORADO.getDisplayLabel());
    data.registryService.updateGeoObject(data.systemSession.getSessionId(), waGeoObj.toJSON().toString());
    
    GeoEntity waGeo2 = GeoEntity.getByKey(data.WASHINGTON.getGeoId());
    Assert.assertEquals(StringUtils.deleteWhitespace(data.COLORADO.getWkt()), StringUtils.deleteWhitespace(waGeo2.getWkt()));
    Assert.assertEquals(data.COLORADO.getDisplayLabel(), waGeo2.getDisplayLabel().getValue());
  }
  
  @Test
  @Request
  public void testGetUIDS()
  {
    String[] ids = data.registryService.getUIDS(data.systemSession.getSessionId(), 100);
    
    Assert.assertEquals(100, ids.length);
    
    for (String id : ids)
    {
      Assert.assertTrue(RegistryIdService.getInstance().isIssuedId(id));
    }
  }
  
  @Test
  public void testGetGeoObjectTypes()
  {
    String[] codes = new String[]{ data.STATE.getCode(), data.DISTRICT.getCode() };
    
    GeoObjectType[] gots = data.registryService.getGeoObjectTypes(data.systemSession.getSessionId(), codes);
    
    Assert.assertEquals(codes.length, gots.length);
    
    GeoObjectType state = gots[0];
    Assert.assertEquals(state.toJSON().toString(), GeoObjectType.fromJSON(state.toJSON().toString(), data.adapter).toJSON().toString());
//    Assert.assertEquals(data.STATE_UID, state.get); // TODO : GeoOBjectTypes don't have a uid?
    Assert.assertEquals(data.STATE.getCode(), state.getCode());
    Assert.assertEquals(data.STATE.getDisplayLabel(), state.getLocalizedLabel());
    Assert.assertEquals(data.STATE.getDescription(), state.getLocalizedDescription());
    
    GeoObjectType district = gots[1];
    Assert.assertEquals(district.toJSON().toString(), GeoObjectType.fromJSON(district.toJSON().toString(), data.adapter).toJSON().toString());
//  Assert.assertEquals(data.DISTRICT_UID, district.get); // TODO : GeoOBjectTypes don't have a uid?
    Assert.assertEquals(data.DISTRICT.getCode(), district.getCode());
    Assert.assertEquals(data.DISTRICT.getDisplayLabel(), district.getLocalizedLabel());
    Assert.assertEquals(data.DISTRICT.getDescription(), district.getLocalizedDescription());
    
    // Test to make sure we can provide none
    GeoObjectType[] gots2 = data.registryService.getGeoObjectTypes(data.systemSession.getSessionId(), new String[]{});
    Assert.assertTrue(gots2.length > 0);
    
    GeoObjectType[] gots3 = data.registryService.getGeoObjectTypes(data.systemSession.getSessionId(), null);
    Assert.assertTrue(gots3.length > 0);
  }
  
  @Test
  @Request
  public void testGetChildGeoObjects()
  {
    String parentId = data.USA.getRegistryId();
    String parentTypeCode = data.USA.getUniversal().getCode();
    String[] childrenTypes = new String[]{data.STATE.getCode(), data.DISTRICT.getCode()};
    
    // Recursive
    ChildTreeNode tn = data.registryService.getChildGeoObjects(data.systemSession.getSessionId(), parentId, parentTypeCode, childrenTypes, true);
    data.USA.assertEquals(tn, childrenTypes, true);
    Assert.assertEquals(tn.toJSON().toString(), ChildTreeNode.fromJSON(tn.toJSON().toString(), data.adapter).toJSON().toString());
    
    // Not recursive
    ChildTreeNode tn2 = data.registryService.getChildGeoObjects(data.systemSession.getSessionId(), parentId, parentTypeCode, childrenTypes, false);
    data.USA.assertEquals(tn2, childrenTypes, false);
    Assert.assertEquals(tn2.toJSON().toString(), ChildTreeNode.fromJSON(tn2.toJSON().toString(), data.adapter).toJSON().toString());
    
    // Test only getting districts
    String[] distArr = new String[]{data.DISTRICT.getCode()};
    ChildTreeNode tn3 = data.registryService.getChildGeoObjects(data.systemSession.getSessionId(), parentId, parentTypeCode, distArr, true);
    data.USA.assertEquals(tn3, distArr, true);
    Assert.assertEquals(tn3.toJSON().toString(), ChildTreeNode.fromJSON(tn3.toJSON().toString(), data.adapter).toJSON().toString());
  }
  
  @Test
  @Request
  public void testGetParentGeoObjects()
  {
    String childId = data.CO_D_TWO.getRegistryId();
    String childTypeCode = data.DISTRICT.getCode();
    String[] childrenTypes = new String[]{data.COUNTRY.getCode(), data.STATE.getCode()};
    
    // Recursive
    ParentTreeNode tn = data.registryService.getParentGeoObjects(data.systemSession.getSessionId(), childId, childTypeCode, childrenTypes, true);
    data.CO_D_TWO.assertEquals(tn, childrenTypes, true);
    Assert.assertEquals(tn.toJSON().toString(), ParentTreeNode.fromJSON(tn.toJSON().toString(), data.adapter).toJSON().toString());
    
    // Not recursive
    ParentTreeNode tn2 = data.registryService.getParentGeoObjects(data.systemSession.getSessionId(), childId, childTypeCode, childrenTypes, false);
    data.CO_D_TWO.assertEquals(tn2, childrenTypes, false);
    Assert.assertEquals(tn2.toJSON().toString(), ParentTreeNode.fromJSON(tn2.toJSON().toString(), data.adapter).toJSON().toString());
    
    // Test only getting countries
    String[] countryArr = new String[]{data.COUNTRY.getCode()};
    ParentTreeNode tn3 = data.registryService.getParentGeoObjects(data.systemSession.getSessionId(), childId, childTypeCode, countryArr, true);
    data.CO_D_TWO.assertEquals(tn3, countryArr, true);
    Assert.assertEquals(tn3.toJSON().toString(), ParentTreeNode.fromJSON(tn3.toJSON().toString(), data.adapter).toJSON().toString());
  }
  
  @Test
  @Request
  public void testGetHierarchyTypes()
  {
    String[] types = new String[]{ LocatedIn.class.getSimpleName() };

    HierarchyType[] hts = data.registryService.getHierarchyTypes(data.systemSession.getSessionId(), types);
    
    Assert.assertEquals(types.length, hts.length);
    
    HierarchyType locatedIn = hts[0];
    USATestData.assertEqualsHierarchyType(LocatedIn.CLASS, locatedIn);
    Assert.assertEquals(locatedIn.toJSON().toString(), HierarchyType.fromJSON(locatedIn.toJSON().toString(), data.adapter).toJSON().toString());
    
    // Test to make sure we can provide no types and get everything back
    HierarchyType[] hts2 = data.registryService.getHierarchyTypes(data.systemSession.getSessionId(), new String[]{});
    Assert.assertTrue(hts2.length > 0);
    
    HierarchyType[] hts3 = data.registryService.getHierarchyTypes(data.systemSession.getSessionId(), null);
    Assert.assertTrue(hts3.length > 0);
  }
  
  @Test
  @Request
  public void testAddChild()
  {
    TestGeoObjectInfo testAddChild = data.newTestGeoObjectInfo("TEST_ADD_CHILD", data.STATE);
    testAddChild.apply();
    
    ParentTreeNode ptnTestState = data.registryService.addChild(data.systemSession.getSessionId(), data.USA.getRegistryId(), data.USA.getUniversal().getCode(), testAddChild.getRegistryId(), testAddChild.getUniversal().getCode(), LocatedIn.class.getSimpleName());
    
    boolean found = false;
    for (ParentTreeNode ptnUSA : ptnTestState.getParents())
    {
      if (ptnUSA.getGeoObject().getCode().equals(data.USA.getGeoId()))
      {
        found = true;
        break;
      }
    }
    Assert.assertTrue("Did not find our test object in the list of returned children", found);
    testAddChild.assertEquals(ptnTestState.getGeoObject());
    
    ChildTreeNode ctnUSA2 = data.registryService.getChildGeoObjects(data.systemSession.getSessionId(), data.USA.getRegistryId(), data.USA.getUniversal().getCode(), new String[]{data.STATE.getCode()}, false);
    
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
    TestGeoObjectInfo testAddChildParent = data.newTestGeoObjectInfo("TEST_ACTIONS_ADD_CHILD_PARENT", data.STATE);
    testAddChildParent.apply();
    
    TestGeoObjectInfo testAddChild = data.newTestGeoObjectInfo("TEST_ACTIONS_ADD_CHILD", data.DISTRICT);
    testAddChild.apply();
    
    TestGeoObjectInfo testDelete = data.newTestGeoObjectInfo("TEST_ACTIONS_DELETE_CHILD", data.STATE);
    testDelete.apply();
    
    TestGeoObjectInfo testNew = data.newTestGeoObjectInfo("TEST_ACTIONS_NEW_CHILD", data.STATE);
    GeoObject goNewChild = testNew.getGeoObject();
    
    TestGeoObjectTypeInfo testDeleteUni = data.newTestGeoObjectTypeInfo("TEST_ACTIONS_DELETE_UNI");
    testDeleteUni.apply();
    GeoObjectType gotDelete = testDeleteUni.getGeoObjectType();
    data.adapter.getMetadataCache().addGeoObjectType(gotDelete);
    
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
//    UpdateAction createGOT = new UpdateAction(data.STATE.getGeoObjectType());
//    String createGOTJson = createGOT.toJSON().toString();
//    String createGOTJson2 = UpdateAction.fromJSON(createGOTJson).toJSON().toString();
//    Assert.assertEquals(createGOTJson, createGOTJson2);
//    actions[i++] = createGOT;
    
    // Delete a GeoObject
//    DeleteAction deleteGO = new DeleteAction(testDelete.newGeoObject());
//    String deleteGOJson = deleteGO.toJSON().toString();
//    String deleteGOJson2 = DeleteAction.fromJSON(deleteGOJson).toJSON().toString();
//    Assert.assertEquals(deleteGOJson, deleteGOJson2);
//    actions[i++] = deleteGO;
    
    // Delete a GeoObjectType
//    DeleteAction deleteGOT = new DeleteAction(gotDelete);
//    String deleteGOTJson = deleteGOT.toJSON().toString();
//    String deleteGOTJson2 = DeleteAction.fromJSON(deleteGOTJson).toJSON().toString();
//    Assert.assertEquals(deleteGOTJson, deleteGOTJson2);
//    actions[i++] = deleteGOT;
    
    // Serialize the actions
    String sActions = AbstractAction.serializeActions(actions).toString();
    String sActions2 = AbstractAction.serializeActions(AbstractAction.parseActions(sActions)).toString();
    Assert.assertEquals(sActions, sActions2);
    
    // Execute the actions
    data.registryService.executeActions(data.systemSession.getSessionId(), sActions);
    
    // Make sure that the database has been modified correctly
    Assert.assertEquals(1, testAddChildParent.getGeoEntity().getChildren(LocatedIn.CLASS).getAll().size());
    
//    GeoEntityQuery delGEQ = new GeoEntityQuery(new QueryFactory());
//    delGEQ.WHERE(delGEQ.getOid().EQ(testDelete.getUid()));
//    Assert.assertEquals(0, delGEQ.getCount());
    
    GeoEntityQuery createGEQ = new GeoEntityQuery(new QueryFactory());
    createGEQ.WHERE(createGEQ.getGeoId().EQ(testNew.getGeoId()));
    Assert.assertEquals(1, createGEQ.getCount());
    Assert.assertEquals(NEW_DISPLAY_LABEL, createGEQ.getIterator().getAll().get(0).getDisplayLabel().getValue());
    
//    UniversalQuery delUQ = new UniversalQuery(new QueryFactory());
//    delUQ.WHERE(delUQ.getOid().EQ(testDeleteUni.getUid()));
//    Assert.assertEquals(0, delUQ.getCount());
    
    // TODO : Response architecture
  }
}
