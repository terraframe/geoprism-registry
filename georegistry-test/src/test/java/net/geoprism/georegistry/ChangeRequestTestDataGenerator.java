package net.geoprism.georegistry;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.commongeoregistry.adapter.action.AbstractActionDTO;
import org.commongeoregistry.adapter.action.geoobject.CreateGeoObjectActionDTO;
import org.commongeoregistry.adapter.action.geoobject.UpdateGeoObjectActionDTO;
import org.commongeoregistry.adapter.action.tree.AddChildActionDTO;
import org.commongeoregistry.adapter.action.tree.RemoveChildActionDTO;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.junit.Assert;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.LocatedIn;

import net.geoprism.georegistry.action.AbstractAction;
import net.geoprism.georegistry.action.AllGovernanceStatus;
import net.geoprism.georegistry.action.ChangeRequest;
import net.geoprism.georegistry.service.ServiceFactory;
import net.geoprism.georegistry.testframework.TestDataSet;

/**
 * This class generates some fake ChangeRequest data for demos and whatnot
 * 
 * @author rrowlands
 */
public class ChangeRequestTestDataGenerator
{
  public static void main(String[] args)
  {
    ChangeRequestTestDataGenerator.build();
  }
  
  @Request
  private static void build()
  {
    TestDataSet.deleteAllActions();
    TestDataSet.deleteAllChangeRequests();
    
    buildInTransaction();
  }
  @Transaction
  private static void buildInTransaction()
  {
    genChangeRequest("CR1", Instant.now().minus(3, ChronoUnit.DAYS));
    genChangeRequest("CR2", Instant.now().minus(2, ChronoUnit.DAYS));
    genChangeRequest("CR3", Instant.now().minus(1, ChronoUnit.DAYS));
  }
  
  private static void genChangeRequest(String genKey, Instant when)
  {
    GeoObject goNewChild = ServiceFactory.getAdapter().newGeoObjectInstance("Cambodia_District");
    goNewChild.setCode(genKey + "_CODE");
    goNewChild.setLocalizedDisplayLabel(genKey + "_LABEL");
    goNewChild.setWKTGeometry("POLYGON ((10000 10000, 12300 40000, 16800 50000, 12354 60000, 13354 60000, 17800 50000, 13300 40000, 11000 10000, 10000 10000))");
    
    GeoObject testAddChildParent = ServiceFactory.getUtilities().getGeoObjectByCode("855 01", "Cambodia_Province");
    GeoObject testAddChild = ServiceFactory.getUtilities().getGeoObjectByCode("855 0109", "Cambodia_District");

    List<AbstractActionDTO> actions = new ArrayList<AbstractActionDTO>();

    /*
     *  Add Child
     */
    AddChildActionDTO addChild = new AddChildActionDTO();
    addChild.setChildId(testAddChild.getUid());
    addChild.setChildTypeCode("Cambodia_Province");
    addChild.setParentId(testAddChildParent.getUid());
    addChild.setParentTypeCode("Cambodia");
    addChild.setHierarchyCode(LocatedIn.class.getSimpleName());
    addChild.setCreateActionDate(Date.from(when.minus(10, ChronoUnit.HOURS)));
    
    String addChildJson = addChild.toJSON().toString();
    String addChildJson2 = AbstractActionDTO.parseAction(addChildJson).toJSON().toString();
    Assert.assertEquals(addChildJson, addChildJson2);
    actions.add(addChild);
    
    /*
     * Remove Child
     */
    RemoveChildActionDTO removeChild = new RemoveChildActionDTO();
    removeChild.setChildId(testAddChild.getUid());
    removeChild.setChildTypeCode("Cambodia_Province");
    removeChild.setParentId(testAddChildParent.getUid());
    removeChild.setParentTypeCode("Cambodia");
    removeChild.setHierarchyCode(LocatedIn.class.getSimpleName());
    removeChild.setCreateActionDate(Date.from(when.minus(9, ChronoUnit.HOURS)));
    
    String removeChildJson = removeChild.toJSON().toString();
    String removeChildJson2 = AbstractActionDTO.parseAction(removeChildJson).toJSON().toString();
    Assert.assertEquals(removeChildJson, removeChildJson2);
    actions.add(removeChild);

    /*
     *  Create a new GeoObject
     */
    CreateGeoObjectActionDTO create = new CreateGeoObjectActionDTO();
    create.setGeoObject(goNewChild.toJSON());
    create.setCreateActionDate(Date.from(when.minus(8, ChronoUnit.HOURS)));
    
    String createJson = create.toJSON().toString();
    String createJson2 = AbstractActionDTO.parseAction(createJson).toJSON().toString();
    Assert.assertEquals(createJson, createJson2);
    actions.add(create);

    /*
     *  Update the previously created GeoObject
     */
    final String NEW_DISPLAY_LABEL = "NEW_DISPLAY_LABEL";
    goNewChild.setLocalizedDisplayLabel(NEW_DISPLAY_LABEL);
    
    UpdateGeoObjectActionDTO update = new UpdateGeoObjectActionDTO();
    update.setGeoObject(goNewChild.toJSON());
    update.setCreateActionDate(Date.from(when.minus(7, ChronoUnit.HOURS)));
    
    String updateJson = update.toJSON().toString();
    String updateJson2 = AbstractActionDTO.parseAction(updateJson).toJSON().toString();
    Assert.assertEquals(updateJson, updateJson2);
    actions.add(update);

    // Serialize the actions
    String sActions = AbstractActionDTO.serializeActions(actions).toString();

    submitChangeRequest(sActions);
  }
  
  private static void submitChangeRequest(String sJson)
  {
    ChangeRequest cr = new ChangeRequest();
    cr.addApprovalStatus(AllGovernanceStatus.PENDING);
    cr.apply();
    
    List<AbstractActionDTO> actionDTOs = AbstractActionDTO.parseActions(sJson);

    for (AbstractActionDTO actionDTO : actionDTOs)
    {
      AbstractAction ra = AbstractAction.dtoToRegistry(actionDTO);
      ra.addApprovalStatus(AllGovernanceStatus.PENDING);
      ra.apply();
      
      cr.addAction(ra).apply();
    }
  }
}
