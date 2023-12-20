/**
 *
 */
package net.geoprism.registry.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.commongeoregistry.adapter.action.AbstractActionDTO;
import org.commongeoregistry.adapter.action.geoobject.UpdateGeoObjectActionDTO;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.runwaysdk.session.Request;

import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.action.AbstractAction;
import net.geoprism.registry.action.geoobject.UpdateGeoObjectAction;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.TestRegistryClient;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class GovernanceTest extends FastDatasetTest implements InstanceTestClassListener
{
  @Autowired
  private TestRegistryClient            client;
  
  @Before
  public void setUp()
  {
    testData.setUpInstanceData();
    
    testData.logIn();
  }

  @After
  public void tearDown()
  {
    testData.logOut();
    
    testData.tearDownInstanceData();
  }

  /**
   * Tests serialization on the DTOs and also conversion to/from DTO
   */
  @Test
  public void testActionSerialization()
  {
    TestGeoObjectInfo testGo = FastTestDataset.CAMBODIA;
//    GeoObjectOverTime go = testGo.asGeoObject();
    GeoObjectOverTime goTime = client.getGeoObjectOverTimeByCode(FastTestDataset.CAMBODIA.getCode(), FastTestDataset.CAMBODIA.getGeoObjectType().getCode());
    testGo.setRegistryId(goTime.getUid());

    /*
     * UpdateGeoObject
     */
    UpdateGeoObjectActionDTO updateDTO = new UpdateGeoObjectActionDTO();
    updateDTO.setGeoObject(goTime.toJSON());
    updateDTO.setCreateActionDate(Date.from(Instant.now().minus(6, ChronoUnit.HOURS)));
    updateDTO.setContributorNotes("UPDATE_CONTRIB_NOTES");
    updateDTO.setMaintainerNotes("UPDATE_MAINTAIN_NOTES");

    String updateJson = updateDTO.toJSON().toString();
    String updateJson2 = AbstractActionDTO.parseAction(updateJson).toJSON().toString();
    Assert.assertEquals(updateJson, updateJson2);

    UpdateGeoObjectAction updateRA = (UpdateGeoObjectAction) AbstractAction.dtoToRegistry(updateDTO);
    GeoObjectOverTime updateRAGO = GeoObjectOverTime.fromJSON(client.getAdapter(), updateRA.getGeoObjectJson());
//    testGo.assertEquals(updateRAGO);
    Assert.assertEquals(updateDTO.getContributorNotes(), updateRA.getContributorNotes());
    Assert.assertEquals(updateDTO.getMaintainerNotes(), updateRA.getMaintainerNotes());
    Assert.assertEquals(updateDTO.getApiVersion(), updateRA.getApiVersion());
    // Assert.assertEquals(updateDTO.getCreateActionDate().getTime(),
    // updateRA.getCreateActionDate().getTime()); // TODO : Runway dates are
    // accurate to the second, but epoch is accurate to the milisecond.

    /*
     * TODO : The rest of the supported actions
     */
  }
  
  @Test
  public void testUpdateAction()
  {
    Date startDate = new Date();
    Date endDate = new Date();
    
    GeoObjectOverTime goTime = client.getGeoObjectOverTimeByCode(FastTestDataset.CAMBODIA.getCode(), FastTestDataset.CAMBODIA.getGeoObjectType().getCode());
    
    goTime.setDisplayLabel(new LocalizedValue("test 123"), startDate, endDate);
    
    UpdateGeoObjectActionDTO updateDTO = new UpdateGeoObjectActionDTO();
    updateDTO.setGeoObject(goTime.toJSON());
    updateDTO.setCreateActionDate(Date.from(Instant.now().minus(6, ChronoUnit.HOURS)));
    updateDTO.setContributorNotes("UPDATE_CONTRIB_NOTES");
    updateDTO.setMaintainerNotes("UPDATE_MAINTAIN_NOTES");
    
    UpdateGeoObjectAction updateRA = (UpdateGeoObjectAction) AbstractAction.dtoToRegistry(updateDTO);
    execute(updateRA);
    
    GeoObjectOverTime goTime2 = client.getGeoObjectOverTimeByCode(FastTestDataset.CAMBODIA.getCode(), FastTestDataset.CAMBODIA.getGeoObjectType().getCode());
    Assert.assertEquals("test 123", goTime2.getDisplayLabel(startDate).getValue());
  }
  
  @Request
  private void execute(AbstractAction action)
  {
    action.execute();
  }
  
//
//  @Test
//  public void testGovernance() throws InterruptedException
//  {
//    /*
//     * CR1 : Setup
//     */
//    TestGeoObjectInfo testAddChildParent = testData.newTestGeoObjectInfo("GOV_TEST_ACTIONS_PARENT_CR1", testData.STATE);
//    testAddChildParent.apply();
//
//    TestGeoObjectInfo testAddChild = testData.newTestGeoObjectInfo("GOV_TEST_ACTIONS_CHILD_CR1", testData.DISTRICT);
//    testAddChild.apply();
//
//    TestGeoObjectInfo testNew = testData.newTestGeoObjectInfo("GOV_TEST_ACTIONS_NEW_CR1", testData.STATE);
//    GeoObject goNewChild = testNew.asGeoObject();
//
//    List<AbstractActionDTO> actionsCR1 = new ArrayList<AbstractActionDTO>();
//
//    /*
//     * CR1 : Add Child
//     */
//    AddChildActionDTO addChild = new AddChildActionDTO();
//    addChild.setChildId(testAddChild.getRegistryId());
//    addChild.setChildTypeCode(testAddChild.getGeoObjectType().getCode());
//    addChild.setParentId(testAddChildParent.getRegistryId());
//    addChild.setParentTypeCode(testAddChildParent.getGeoObjectType().getCode());
//    addChild.setHierarchyCode(LocatedIn.class.getSimpleName());
//    addChild.setCreateActionDate(Date.from(Instant.now().minus(10, ChronoUnit.HOURS)));
//
//    String addChildJson = addChild.toJSON().toString();
//    String addChildJson2 = AbstractActionDTO.parseAction(addChildJson).toJSON().toString();
//    Assert.assertEquals(addChildJson, addChildJson2);
//    actionsCR1.add(addChild);
//
//    /*
//     * CR1 : Create Geo Object
//     */
//    CreateGeoObjectActionDTO create = new CreateGeoObjectActionDTO();
//    create.setGeoObject(goNewChild.toJSON());
//    create.setCreateActionDate(Date.from(Instant.now().minus(9, ChronoUnit.HOURS)));
//
//    String createJson = create.toJSON().toString();
//    String createJson2 = AbstractActionDTO.parseAction(createJson).toJSON().toString();
//    Assert.assertEquals(createJson, createJson2);
//    actionsCR1.add(create);
//
//    /*
//     * CR1 : Update the previously created GeoObject
//     */
//    final String NEW_DISPLAY_LABEL = "NEW_DISPLAY_LABEL";
//    goNewChild.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, NEW_DISPLAY_LABEL);
//
//    UpdateGeoObjectActionDTO update = new UpdateGeoObjectActionDTO();
//    update.setGeoObject(goNewChild.toJSON());
//    update.setCreateActionDate(Date.from(Instant.now().minus(8, ChronoUnit.HOURS)));
//
//    String updateJson = update.toJSON().toString();
//    String updateJson2 = AbstractActionDTO.parseAction(updateJson).toJSON().toString();
//    Assert.assertEquals(updateJson, updateJson2);
//    actionsCR1.add(update);
//
//    /*
//     * CR1 : Test Serialization
//     */
//    String sActions = AbstractActionDTO.serializeActions(actionsCR1).toString();
//    String sActions2 = AbstractActionDTO.serializeActions(AbstractActionDTO.parseActions(sActions)).toString();
//    Assert.assertEquals(sActions, sActions2);
//    System.out.println("CR1:\n" + sActions);
//
//    /*
//     * Submit CR1
//     */
//    this.adapter.submitChangeRequest(actionsCR1);
//
//    Thread.sleep(1500); // We need change requests to not have the same
//                        // createDate
//    // so the ordering in validation is predictable
//
//    /*
//     * CR2 : Setup
//     */
//    TestGeoObjectInfo testNewCR2 = testData.newTestGeoObjectInfo("GOV_TEST_ACTIONS_NEW_CR2", testData.STATE);
//    GeoObject goNewChildCR2 = testNewCR2.asGeoObject();
//
//    List<AbstractActionDTO> actionsCR2 = new ArrayList<AbstractActionDTO>();
//
//    /*
//     * CR2 : Create a GeoObject
//     */
//    CreateGeoObjectActionDTO cr2Create = new CreateGeoObjectActionDTO();
//    cr2Create.setGeoObject(goNewChildCR2.toJSON());
//    cr2Create.setCreateActionDate(Date.from(Instant.now().minus(7, ChronoUnit.HOURS)));
//
//    String createJsonCR2 = cr2Create.toJSON().toString();
//    String createJson2CR2 = AbstractActionDTO.parseAction(createJsonCR2).toJSON().toString();
//    Assert.assertEquals(createJsonCR2, createJson2CR2);
//    actionsCR2.add(cr2Create);
//
//    /*
//     * CR2 : Update a GeoObject
//     */
//    goNewChildCR2.setStatus(DefaultTerms.GeoObjectStatusTerm.INACTIVE.code);
//
//    UpdateGeoObjectActionDTO cr2Update = new UpdateGeoObjectActionDTO();
//    cr2Update.setGeoObject(goNewChildCR2.toJSON());
//    cr2Update.setCreateActionDate(Date.from(Instant.now().minus(6, ChronoUnit.HOURS)));
//
//    String updateJsonCR2 = cr2Update.toJSON().toString();
//    String updateJson2CR2 = AbstractActionDTO.parseAction(updateJsonCR2).toJSON().toString();
//    Assert.assertEquals(updateJsonCR2, updateJson2CR2);
//    actionsCR2.add(cr2Update);
//
//    /*
//     * CR2 : Remove Child
//     */
//    RemoveChildActionDTO removeChild = new RemoveChildActionDTO();
//    removeChild.setChildId(testAddChild.getRegistryId());
//    removeChild.setChildTypeCode(testAddChild.getGeoObjectType().getCode());
//    removeChild.setParentId(testAddChildParent.getRegistryId());
//    removeChild.setParentTypeCode(testAddChildParent.getGeoObjectType().getCode());
//    removeChild.setHierarchyCode(LocatedIn.class.getSimpleName());
//    removeChild.setCreateActionDate(Date.from(Instant.now().minus(5, ChronoUnit.HOURS)));
//
//    String removeChildJson = removeChild.toJSON().toString();
//    String removeChildJson2 = AbstractActionDTO.parseAction(removeChildJson).toJSON().toString();
//    Assert.assertEquals(removeChildJson, removeChildJson2);
//    actionsCR2.add(removeChild);
//
//    /*
//     * CR2 : Test Serialization
//     */
//    String sActionsCR2 = AbstractActionDTO.serializeActions(actionsCR2).toString();
//    String sActions2CR2 = AbstractActionDTO.serializeActions(AbstractActionDTO.parseActions(sActionsCR2)).toString();
//    Assert.assertEquals(sActionsCR2, sActions2CR2);
//    System.out.println("CR2:\n" + sActionsCR2);
//
//    /*
//     * Submit CR2
//     */
//    this.adapter.submitChangeRequest(actionsCR2);
//
//    /*
//     * Validation and execution
//     */
//    validateGovernance(testAddChildParent, actionsCR1, actionsCR2, testNew, testNewCR2, NEW_DISPLAY_LABEL);
//  }
//
//  @Request
//  private void validateGovernance(TestGeoObjectInfo testAddChildParent, List<AbstractActionDTO> actionsCR1, List<AbstractActionDTO> actionsCR2, TestGeoObjectInfo testNewCR1, TestGeoObjectInfo testNewCR2, final String NEW_DISPLAY_LABEL)
//  {
//    /*
//     * Pre-execution Validation
//     */
//    ChangeRequestQuery crq = new ChangeRequestQuery(new QueryFactory());
//    crq.ORDER_BY(crq.getCreateDate(), SortOrder.ASC);
//    Assert.assertEquals(2, crq.getCount());
//
//    AbstractActionQuery aaq = new AbstractActionQuery(new QueryFactory());
//    aaq.ORDER_BY(aaq.getCreateActionDate(), SortOrder.DESC);
//    Assert.assertEquals(actionsCR1.size() + actionsCR2.size(), aaq.getCount());
//
//    int crNum = 1;
//    OIterator<? extends ChangeRequest> it = crq.getIterator();
//    while (it.hasNext())
//    {
//      ChangeRequest cr = it.next();
//
//      Assert.assertEquals(1, cr.getApprovalStatus().size());
//      Assert.assertEquals(AllGovernanceStatus.PENDING.getEnumName(), cr.getApprovalStatus().get(0).getEnumName());
//
//      List<? extends AbstractAction> actions = cr.getOrderedActions();
//
//      List<AbstractActionDTO> actionsDTO = null;
//      if (crNum == 1)
//      {
//        actionsDTO = actionsCR1;
//      }
//      else if (crNum == 2)
//      {
//        actionsDTO = actionsCR2;
//      }
//      else
//      {
//        Assert.fail();
//      }
//      Assert.assertEquals(actionsDTO.size(), actions.size());
//
//      int actionIndex = 0;
//      for (AbstractActionDTO actionDTO : actionsDTO)
//      {
//        AbstractAction action = actions.get(actionIndex);
//
//        Assert.assertEquals(ActionFactory.newAction(actionDTO.getActionType()).getClass().getName(), action.getClass().getName());
//
//        Assert.assertEquals(actionDTO.getCreateActionDate(), actionDTO.getCreateActionDate());
//
//        Assert.assertEquals(action.getApiVersion(), actionDTO.getApiVersion());
//
//        actionIndex++;
//      }
//
//      crNum++;
//    }
//
//    /*
//     * Execute CR1
//     */
//    List<? extends ChangeRequest> requests = crq.getIterator().getAll();
//    ChangeRequest cr1 = requests.get(0);
//    cr1.setAllActionsStatus(AllGovernanceStatus.ACCEPTED);
//    cr1.execute(false);
//
//    /*
//     * Validate CR1
//     */
//    // AddChild : TODO : The leaf 'getChildren' mechanism doesn't work properly
//    // in our test framework
//    // Assert.assertEquals(1,
//    // testAddChildParent.getChildrenAsGeoEntity(LocatedIn.CLASS).getAll().size());
//
//    // CreateGeoObject and UpdateGeoObject
//    GeoEntityQuery createGEQ = new GeoEntityQuery(new QueryFactory());
//    createGEQ.WHERE(createGEQ.getGeoId().EQ(testNewCR1.getCode()));
//    Assert.assertEquals(1, createGEQ.getCount());
//    Assert.assertEquals(NEW_DISPLAY_LABEL, createGEQ.getIterator().getAll().get(0).getDisplayLabel().getValue());
//
//    /*
//     * Execute CR2
//     */
//    ChangeRequest cr2 = requests.get(1);
//    cr2.appLock();
//    cr2.clearApprovalStatus();
//    cr2.addApprovalStatus(AllGovernanceStatus.PENDING);
//    cr2.apply();
//
//    cr2.setAllActionsStatus(AllGovernanceStatus.ACCEPTED);
//    cr2.execute(false);
//
//    /*
//     * Validate CR2
//     */
//    // Test RemoveChild : TODO : The leaf 'getChildren' mechanism doesn't work
//    // properly in our test framework
//    // Assert.assertEquals(0,
//    // testAddChildParent.getChildrenAsGeoEntity(LocatedIn.CLASS).getAll().size());
//
//    // Test CreateGeoObject and UpdateGeoObject CR2
//    GeoObjectQuery createGEQCR2 = new GeoObjectQuery(testNewCR2.getGeoObjectType().getGeoObjectType());
//    createGEQCR2.setRestriction(new CodeRestriction(testNewCR2.getCode()));
//    List<GeoObject> createGEQCR2All = createGEQCR2.getIterator().getAll();
//    Assert.assertEquals(1, createGEQCR2All.size());
//    Assert.assertEquals(DefaultTerms.GeoObjectStatusTerm.INACTIVE.code, createGEQCR2All.get(0).getStatus().getCode());
//  }
}
