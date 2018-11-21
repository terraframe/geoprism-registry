package net.geoprism.registry;

import net.geoprism.georegistry.service.IdService;
import net.geoprism.georegistry.service.RegistryService;
import net.geoprism.registry.USATestData.TestGeoEntityInfo;
import net.geoprism.registry.USATestData.TestUniversalInfo;

import org.apache.commons.lang.StringUtils;
import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.action.AbstractAction;
import org.commongeoregistry.adapter.action.AddChildAction;
import org.commongeoregistry.adapter.action.DeleteAction;
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
import com.runwaysdk.system.gis.geo.AllowedIn;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.GeoEntityQuery;
import com.runwaysdk.system.gis.geo.LocatedIn;
import com.runwaysdk.system.gis.geo.UniversalQuery;

public class RegistryServiceTest
{
  protected static USATestData data;
  
  protected static RegistryAdapter adapter;
  
  @Before
  public void setUp()
  {
    data = new USATestData();
    
    data.setUp();
    
    adapter = RegistryService.getRegistryAdapter();
  }
  
  @After
  public void tearDown()
  {
    data.cleanUp();
  }
  
  @Test
  public void testGetGeoObject()
  {
    GeoObject geoObj = data.registryService.getGeoObject(data.systemSession.getSessionId(), USATestData.COLORADO.getUid());
    
    Assert.assertEquals(geoObj.toJSON().toString(), GeoObject.fromJSON(adapter, geoObj.toJSON().toString()).toJSON().toString());
    USATestData.COLORADO.assertEquals(geoObj);
  }
  
  @Test
  @Request
  public void testUpdateGeoObject()
  {
    USATestData.WASHINGTON.delete();
    
    // 1. Test creating a new one
    GeoObject geoObj = adapter.newGeoObjectInstance(USATestData.STATE.getCode());
    geoObj.setWKTGeometry(USATestData.WASHINGTON.getWkt());
    geoObj.setCode(USATestData.WASHINGTON.getGeoId());
//    geoObj.setUid(USATestData.WASHINGTON.getGeoId()); // TODO : This should be set by the API
    geoObj.setLocalizedDisplayLabel(USATestData.WASHINGTON.getDisplayLabel());
    data.registryService.updateGeoObject(data.systemSession.getSessionId(), geoObj.toJSON().toString());
    
    GeoEntity waGeo = GeoEntity.getByKey(USATestData.WASHINGTON.getGeoId());
    Assert.assertEquals(StringUtils.deleteWhitespace(USATestData.WASHINGTON.getWkt()), StringUtils.deleteWhitespace(waGeo.getWkt()));
    Assert.assertEquals(USATestData.WASHINGTON.getGeoId(), waGeo.getGeoId());
//    Assert.assertEquals(USATestData.WASHINGTON.getGeoId(), waGeo.getOid()); // TODO : check the uid?
    Assert.assertEquals(USATestData.WASHINGTON.getDisplayLabel(), waGeo.getDisplayLabel().getValue());
    
    // 2. Test updating the one we created earlier
    GeoObject waGeoObj = data.registryService.getGeoObject(data.systemSession.getSessionId(), waGeo.getOid());
    waGeoObj.setWKTGeometry(USATestData.COLORADO.getWkt());
    waGeoObj.setLocalizedDisplayLabel(USATestData.COLORADO.getDisplayLabel());
    data.registryService.updateGeoObject(data.systemSession.getSessionId(), waGeoObj.toJSON().toString());
    
    GeoEntity waGeo2 = GeoEntity.getByKey(USATestData.WASHINGTON.getGeoId());
    Assert.assertEquals(StringUtils.deleteWhitespace(USATestData.COLORADO.getWkt()), StringUtils.deleteWhitespace(waGeo2.getWkt()));
    Assert.assertEquals(USATestData.COLORADO.getDisplayLabel(), waGeo2.getDisplayLabel().getValue());
  }
  
  @Test
  public void testGetUIDS()
  {
    String[] ids = data.registryService.getUIDS(data.systemSession.getSessionId(), 100);
    
    Assert.assertEquals(100, ids.length);
    
    for (String id : ids)
    {
      Assert.assertTrue(IdService.getInstance(data.systemSession.getSessionId()).isIssuedId(id));
    }
  }
  
  @Test
  public void testGetGeoObjectTypes()
  {
    String[] codes = new String[]{ USATestData.STATE.getCode(), USATestData.DISTRICT.getCode() };
    
    GeoObjectType[] gots = data.registryService.getGeoObjectTypes(data.systemSession.getSessionId(), codes);
    
    Assert.assertEquals(codes.length, gots.length);
    
    GeoObjectType state = gots[0];
    Assert.assertEquals(state.toJSON().toString(), GeoObjectType.fromJSON(state.toJSON().toString(), adapter).toJSON().toString());
//    Assert.assertEquals(USATestData.STATE_UID, state.get); // TODO : GeoOBjectTypes don't have a uid?
    Assert.assertEquals(USATestData.STATE.getCode(), state.getCode());
    Assert.assertEquals(USATestData.STATE.getDisplayLabel(), state.getLocalizedLabel());
    Assert.assertEquals(USATestData.STATE.getDescription(), state.getLocalizedDescription());
    
    GeoObjectType district = gots[1];
    Assert.assertEquals(district.toJSON().toString(), GeoObjectType.fromJSON(district.toJSON().toString(), adapter).toJSON().toString());
//  Assert.assertEquals(USATestData.DISTRICT_UID, district.get); // TODO : GeoOBjectTypes don't have a uid?
    Assert.assertEquals(USATestData.DISTRICT.getCode(), district.getCode());
    Assert.assertEquals(USATestData.DISTRICT.getDisplayLabel(), district.getLocalizedLabel());
    Assert.assertEquals(USATestData.DISTRICT.getDescription(), district.getLocalizedDescription());
    
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
    String parentId = USATestData.USA.getUid();
    String[] childrenTypes = new String[]{USATestData.STATE.getCode(), USATestData.DISTRICT.getCode()};
    
    // Recursive
    ChildTreeNode tn = data.registryService.getChildGeoObjects(data.systemSession.getSessionId(), parentId, childrenTypes, true);
    USATestData.USA.assertEquals(tn, childrenTypes, true);
    Assert.assertEquals(tn.toJSON().toString(), ChildTreeNode.fromJSON(tn.toJSON().toString(), adapter).toJSON().toString());
    
    // Not recursive
    ChildTreeNode tn2 = data.registryService.getChildGeoObjects(data.systemSession.getSessionId(), parentId, childrenTypes, false);
    USATestData.USA.assertEquals(tn2, childrenTypes, false);
    Assert.assertEquals(tn2.toJSON().toString(), ChildTreeNode.fromJSON(tn2.toJSON().toString(), adapter).toJSON().toString());
    
    // Test only getting districts
    String[] distArr = new String[]{USATestData.DISTRICT.getCode()};
    ChildTreeNode tn3 = data.registryService.getChildGeoObjects(data.systemSession.getSessionId(), parentId, distArr, true);
    USATestData.USA.assertEquals(tn3, distArr, true);
    Assert.assertEquals(tn3.toJSON().toString(), ChildTreeNode.fromJSON(tn3.toJSON().toString(), adapter).toJSON().toString());
  }
  
  @Test
  @Request
  public void testGetParentGeoObjects()
  {
    String childId = USATestData.CO_D_TWO.getUid();
    String[] childrenTypes = new String[]{USATestData.COUNTRY.getCode(), USATestData.STATE.getCode()};
    
    // Recursive
    ParentTreeNode tn = data.registryService.getParentGeoObjects(data.systemSession.getSessionId(), childId, childrenTypes, true);
    USATestData.CO_D_TWO.assertEquals(tn, childrenTypes, true);
    Assert.assertEquals(tn.toJSON().toString(), ParentTreeNode.fromJSON(tn.toJSON().toString(), adapter).toJSON().toString());
    
    // Not recursive
    ParentTreeNode tn2 = data.registryService.getParentGeoObjects(data.systemSession.getSessionId(), childId, childrenTypes, false);
    USATestData.CO_D_TWO.assertEquals(tn2, childrenTypes, false);
    Assert.assertEquals(tn2.toJSON().toString(), ParentTreeNode.fromJSON(tn2.toJSON().toString(), adapter).toJSON().toString());
    
    // Test only getting countries
    String[] countryArr = new String[]{USATestData.COUNTRY.getCode()};
    ParentTreeNode tn3 = data.registryService.getParentGeoObjects(data.systemSession.getSessionId(), childId, countryArr, true);
    USATestData.CO_D_TWO.assertEquals(tn3, countryArr, true);
    Assert.assertEquals(tn3.toJSON().toString(), ParentTreeNode.fromJSON(tn3.toJSON().toString(), adapter).toJSON().toString());
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
    Assert.assertEquals(locatedIn.toJSON().toString(), HierarchyType.fromJSON(locatedIn.toJSON().toString(), adapter).toJSON().toString());
    
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
    TestGeoEntityInfo testAddChild = data.newTestGeoEntityInfo("TEST_ADD_CHILD", USATestData.STATE);
    testAddChild.apply();
    
    ParentTreeNode ptnTestState = data.registryService.addChild(data.systemSession.getSessionId(), USATestData.USA.getUid(), testAddChild.getUid(), LocatedIn.class.getSimpleName());
    
    boolean found = false;
    for (ParentTreeNode ptnUSA : ptnTestState.getParents())
    {
      if (ptnUSA.getGeoObject().getCode().equals(USATestData.USA.getGeoId()))
      {
        found = true;
        break;
      }
    }
    Assert.assertTrue("Did not find our test object in the list of returned children", found);
    testAddChild.assertEquals(ptnTestState.getGeoObject());
    
    ChildTreeNode ctnUSA2 = data.registryService.getChildGeoObjects(data.systemSession.getSessionId(), USATestData.USA.getUid(), new String[]{USATestData.STATE.getCode()}, false);
    
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
    TestGeoEntityInfo testAddChildParent = data.newTestGeoEntityInfo("TEST_ACTIONS_ADD_CHILD_PARENT", USATestData.STATE);
    testAddChildParent.apply();
    
    TestGeoEntityInfo testAddChild = data.newTestGeoEntityInfo("TEST_ACTIONS_ADD_CHILD", USATestData.DISTRICT);
    testAddChild.apply();
    
    TestGeoEntityInfo testDelete = data.newTestGeoEntityInfo("TEST_ACTIONS_DELETE_CHILD", USATestData.STATE);
    testDelete.apply();
    
    TestGeoEntityInfo testNew = data.newTestGeoEntityInfo("TEST_ACTIONS_NEW_CHILD", USATestData.STATE);
    GeoObject goNewChild = testNew.newGeoObject();
    
    TestUniversalInfo testDeleteUni = data.newTestUniversalInfo("TEST_ACTIONS_DELETE_UNI");
    testDeleteUni.apply();
    GeoObjectType gotDelete = testDeleteUni.newGeoObjectType();
    adapter.getMetadataCache().addGeoObjectType(gotDelete);
    
    AbstractAction[] actions = new AbstractAction[4];
    int i = 0;
    
    // Add Child
    AddChildAction addChild = new AddChildAction(testAddChild.getUid(), testAddChildParent.getUid(), LocatedIn.class.getSimpleName());
    String addChildJson = addChild.toJSON().toString();
    String addChildJson2 = AddChildAction.fromJSON(addChildJson).toJSON().toString();
    Assert.assertEquals(addChildJson, addChildJson2);
    actions[i++] = addChild;
    
    // Remove Child ??
    // TODO
    
    // Create a new GeoObject
    UpdateAction update = new UpdateAction(goNewChild);
    String updateJson = update.toJSON().toString();
    String updateJson2 = UpdateAction.fromJSON(updateJson).toJSON().toString();
    Assert.assertEquals(updateJson, updateJson2);
    actions[i++] = update;
    
    // Update a GeoObjectType
    // TODO : This hasn't been implemented yet in RegistryUpdateAction
//    UpdateAction createGOT = new UpdateAction(USATestData.STATE.getGeoObjectType());
//    String createGOTJson = createGOT.toJSON().toString();
//    String createGOTJson2 = UpdateAction.fromJSON(createGOTJson).toJSON().toString();
//    Assert.assertEquals(createGOTJson, createGOTJson2);
//    actions[i++] = createGOT;
    
    // Delete a GeoObject
    DeleteAction deleteGO = new DeleteAction(testDelete.newGeoObject());
    String deleteGOJson = deleteGO.toJSON().toString();
    String deleteGOJson2 = DeleteAction.fromJSON(deleteGOJson).toJSON().toString();
    Assert.assertEquals(deleteGOJson, deleteGOJson2);
    actions[i++] = deleteGO;
    
    // Delete a GeoObjectType
    DeleteAction deleteGOT = new DeleteAction(gotDelete);
    String deleteGOTJson = deleteGOT.toJSON().toString();
    String deleteGOTJson2 = DeleteAction.fromJSON(deleteGOTJson).toJSON().toString();
    Assert.assertEquals(deleteGOTJson, deleteGOTJson2);
    actions[i++] = deleteGOT;
    
    // Serialize the actions
    String sActions = AbstractAction.serializeActions(actions).toString();
    String sActions2 = AbstractAction.serializeActions(AbstractAction.parseActions(sActions)).toString();
    Assert.assertEquals(sActions, sActions2);
    
    // Execute the actions
    data.registryService.executeActions(data.systemSession.getSessionId(), sActions);
    
    // Make sure that the database has been modified correctly
    Assert.assertEquals(1, testAddChildParent.getGeoEntity().getChildren(LocatedIn.CLASS).getAll().size());
    
    GeoEntityQuery delGEQ = new GeoEntityQuery(new QueryFactory());
    delGEQ.WHERE(delGEQ.getOid().EQ(testDelete.getUid()));
    Assert.assertEquals(0, delGEQ.getCount());
    
    GeoEntityQuery createGEQ = new GeoEntityQuery(new QueryFactory());
    createGEQ.WHERE(createGEQ.getGeoId().EQ(testNew.getGeoId()));
    Assert.assertEquals(1, createGEQ.getCount());
    
    UniversalQuery delUQ = new UniversalQuery(new QueryFactory());
    delUQ.WHERE(delUQ.getOid().EQ(testDeleteUni.getUid()));
    Assert.assertEquals(0, delUQ.getCount());
    
    // TODO : Response architecture
  }
}
