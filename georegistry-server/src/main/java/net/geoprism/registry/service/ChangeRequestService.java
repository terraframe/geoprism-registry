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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.action.AbstractActionDTO;
import org.commongeoregistry.adapter.metadata.RegistryRole;
import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.business.rbac.RoleDAOIF;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.query.Condition;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.VaultFile;

import net.geoprism.GeoprismUser;
import net.geoprism.registry.CGRPermissionException;
import net.geoprism.registry.Organization;
import net.geoprism.registry.action.AbstractAction;
import net.geoprism.registry.action.AllGovernanceStatus;
import net.geoprism.registry.action.ChangeRequest;
import net.geoprism.registry.action.ChangeRequestPermissionService;
import net.geoprism.registry.action.ChangeRequestPermissionService.ChangeRequestPermissionAction;
import net.geoprism.registry.action.ChangeRequestQuery;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.view.Page;

public class ChangeRequestService
{
  public ChangeRequestPermissionService permService = new ChangeRequestPermissionService();
  
  @Request(RequestType.SESSION)
  public void deleteDocumentCR(String sessionId, String crOid, String vfOid)
  {
    this.deleteDocumentInTransCR(crOid, vfOid);
  }
  
  @Transaction
  void deleteDocumentInTransCR(String crOid, String vfOid)
  {
    ChangeRequest request = ChangeRequest.get(crOid);
    
    if (!this.permService.getPermissions(request).contains(ChangeRequestPermissionAction.WRITE_DOCUMENTS))
    {
      throw new CGRPermissionException();
    }
    
    VaultFile vf = VaultFile.get(vfOid);
    
    vf.delete();
  }
  
  @Request(RequestType.SESSION)
  public void deleteDocumentAction(String sessionId, String actionOid, String vfOid)
  {
    this.deleteDocumentActionInTrans(actionOid, vfOid);
  }
  
  @Transaction
  void deleteDocumentActionInTrans(String actionOid, String vfOid)
  {
    AbstractAction action = AbstractAction.get(actionOid);
    
    if (!this.permService.getPermissions(action.getAllRequest().next()).contains(ChangeRequestPermissionAction.WRITE_DOCUMENTS))
    {
      throw new CGRPermissionException();
    }
    
    VaultFile vf = VaultFile.get(vfOid);
    
    vf.delete();
  }
  
  @Request(RequestType.SESSION)
  public ApplicationResource downloadDocumentCR(String sessionId, String crOid, String vfOid)
  {
    return this.downloadDocumentCR(crOid, vfOid);
  }
  
  ApplicationResource downloadDocumentCR(String crOid, String vfOid)
  {
    ChangeRequest request = ChangeRequest.get(crOid);
    
    if (!this.permService.getPermissions(request).contains(ChangeRequestPermissionAction.READ_DOCUMENTS))
    {
      throw new CGRPermissionException();
    }
    
    VaultFile vf = VaultFile.get(vfOid);
    
    return vf;
  }
  
  @Request(RequestType.SESSION)
  public ApplicationResource downloadDocumentAction(String sessionId, String actionOid, String vfOid)
  {
    return this.downloadDocumentAction(actionOid, vfOid);
  }
  
  ApplicationResource downloadDocumentAction(String actionOid, String vfOid)
  {
    AbstractAction action = AbstractAction.get(actionOid);
    
    if (!this.permService.getPermissions(action.getAllRequest().next()).contains(ChangeRequestPermissionAction.READ_DOCUMENTS))
    {
      throw new CGRPermissionException();
    }
    
    VaultFile vf = VaultFile.get(vfOid);
    
    return vf;
  }
  
  @Request(RequestType.SESSION)
  public String listDocumentsCR(String sessionId, String requestId)
  {
    return this.listDocumentsCR(requestId);
  }
  
  String listDocumentsCR(String requestId)
  {
    JsonArray ja = new JsonArray();
    
    ChangeRequest request = ChangeRequest.get(requestId);
    
    if (!this.permService.getPermissions(request).contains(ChangeRequestPermissionAction.READ_DOCUMENTS))
    {
      throw new CGRPermissionException();
    }
    
    OIterator<? extends VaultFile> it = request.getAllDocument();
    try
    {
      for (VaultFile vf : it)
      {
        JsonObject jo = new JsonObject();
        
        jo.addProperty("fileName", vf.getName());
        jo.addProperty("oid", vf.getOid());
        
        ja.add(jo);
      }
    }
    finally
    {
      it.close();
    }
    
    return ja.toString();
  }
  
  @Request(RequestType.SESSION)
  public String listDocumentsAction(String sessionId, String actionOid)
  {
    return this.listDocumentsAction(actionOid);
  }
  
  String listDocumentsAction(String actionOid)
  {
    JsonArray ja = new JsonArray();
    
    AbstractAction action = AbstractAction.get(actionOid);
    
    if (!this.permService.getPermissions(action.getAllRequest().next()).contains(ChangeRequestPermissionAction.READ_DOCUMENTS))
    {
      throw new CGRPermissionException();
    }
    
    OIterator<? extends VaultFile> it = action.getAllDocument();
    try
    {
      for (VaultFile vf : it)
      {
        JsonObject jo = new JsonObject();
        
        jo.addProperty("fileName", vf.getName());
        jo.addProperty("oid", vf.getOid());
        
        ja.add(jo);
      }
    }
    finally
    {
      it.close();
    }
    
    return ja.toString();
  }
  
  @Request(RequestType.SESSION)
  public String uploadFileCR(String sessionId, String requestId, String fileName, InputStream fileStream)
  {
    return uploadFileInTransactionCR(requestId, fileName, fileStream);
  }
  
  @Transaction
  String uploadFileInTransactionCR(String requestId, String fileName, InputStream fileStream)
  {
    ChangeRequest request = ChangeRequest.get(requestId);
    
    if (!this.permService.getPermissions(request).contains(ChangeRequestPermissionAction.WRITE_DOCUMENTS))
    {
      throw new CGRPermissionException();
    }
    
    VaultFile vf = VaultFile.createAndApply(fileName, fileStream);
    
    request.addDocument(vf).apply();
    
    JsonObject jo = new JsonObject();
    
    jo.addProperty("fileName", vf.getName());
    jo.addProperty("oid", vf.getOid());
    
    return jo.toString();
  }
  
  @Request(RequestType.SESSION)
  public String uploadFileAction(String sessionId, String actionOid, String fileName, InputStream fileStream)
  {
    return uploadFileActionInTransaction(actionOid, fileName, fileStream);
  }
  
  @Transaction
  String uploadFileActionInTransaction(String actionOid, String fileName, InputStream fileStream)
  {
    AbstractAction action = AbstractAction.get(actionOid);
    
    if (!this.permService.getPermissions(action.getAllRequest().next()).contains(ChangeRequestPermissionAction.WRITE_DOCUMENTS))
    {
      throw new CGRPermissionException();
    }
    
    VaultFile vf = VaultFile.createAndApply(fileName, fileStream);
    
    action.addDocument(vf).apply();
    
    JsonObject jo = new JsonObject();
    
    jo.addProperty("fileName", vf.getName());
    jo.addProperty("oid", vf.getOid());
    
    return jo.toString();
  }
  
//  @Request(RequestType.SESSION)
//  public void applyAction(String sessionId, String sAction)
//  {
//    applyActionInTransaction(sAction);
//  }
//
//  @Transaction
//  public void applyActionInTransaction(String sAction)
//  {
//    JSONObject joAction = new JSONObject(sAction);
//
//    AbstractAction action = AbstractAction.get(joAction.getString("oid"));
//    
//    Set<ChangeRequestPermissionAction> perms = this.permService.getPermissions(action.getAllRequest().next());
//    
//    if (!perms.containsAll(Arrays.asList(
//        ChangeRequestPermissionAction.WRITE
//      )))
//    {
//      throw new CGRPermissionException();
//    }
//
//    action.buildFromJson(joAction);
//
//    action.apply();
//  }

//  @Request(RequestType.SESSION)
//  public void applyActionStatusProperties(String sessionId, String sAction)
//  {
//    applyActionStatusPropertiesInTransaction(sAction);
//  }
//
//  @Transaction
//  public void applyActionStatusPropertiesInTransaction(String sAction)
//  {
//    JSONObject joAction = new JSONObject(sAction);
//
//    AbstractAction action = AbstractAction.get(joAction.getString("oid"));
//    
//    if (!this.permService.getPermissions(action.getAllRequest().next()).containsAll(Arrays.asList(
//        ChangeRequestPermissionAction.WRITE
//      )))
//    {
//      throw new CGRPermissionException();
//    }
//
//    action.lock();
//    action.buildFromJson(joAction);
//    action.setDecisionMaker(GeoprismUser.getCurrentUser());
//    action.apply();
//    action.unlock();
//  }

//  @Request(RequestType.SESSION)
//  public JsonArray getAllActions(String sessionId, String requestId)
//  {
//    ChangeRequest request = ChangeRequest.get(requestId);
//    
//    if (!this.permService.getPermissions(request).containsAll(Arrays.asList(
//        ChangeRequestPermissionAction.READ, ChangeRequestPermissionAction.READ_APPROVAL_STATUS
//      )))
//    {
//      throw new CGRPermissionException();
//    }
//    
//    JsonArray actions = new JsonArray();
//    QueryFactory factory = new QueryFactory();
//
//    AbstractActionQuery query = new AbstractActionQuery(factory);
//
//    ChangeRequestQuery rQuery = new ChangeRequestQuery(factory);
//    rQuery.WHERE(rQuery.getOid().EQ(requestId));
//
//    query.WHERE(query.request(rQuery));
//
//    query.ORDER_BY(query.getCreateActionDate(), SortOrder.ASC);
//
//    Iterator<? extends AbstractAction> it = query.getIterator();
//
//    while (it.hasNext())
//    {
//      AbstractAction action = it.next();
//
//      actions.add(action.toJson());
//    }
//
//    return actions;
//  }

  /**
   * ]
   * 
   * @param sessionId
   * @param requestId
   * @return
   * 
   *         Sets all PENDING actions to APPROVED and executes the change
   *         request to persist both the change request and actions.
   */
//  @Request(RequestType.SESSION)
//  public JsonObject confirmChangeRequest(String sessionId, String requestId)
//  {
//    return confirmChangeRequestTransaction(sessionId, requestId);
//  }
//
//  @Transaction
//  private JsonObject confirmChangeRequestTransaction(String sessionId, String requestId)
//  {
//    ChangeRequest request = ChangeRequest.get(requestId);
//    
//    if (!this.permService.getPermissions(request).containsAll(Arrays.asList(
//        ChangeRequestPermissionAction.EXECUTE, ChangeRequestPermissionAction.WRITE, ChangeRequestPermissionAction.WRITE_APPROVAL_STATUS, ChangeRequestPermissionAction.READ,
//        ChangeRequestPermissionAction.READ_DETAILS
//      )))
//    {
//      throw new CGRPermissionException();
//    }
//    
//    request.setAllActionsStatus(AllGovernanceStatus.ACCEPTED);
//
//    this.executeActions(sessionId, requestId);
//
//    return request.getDetails();
//  }
//
//  @Request(RequestType.SESSION)
//  public JsonArray approveAllActions(String sessionId, String requestId, String sActions)
//  {
//    return approveAllActionsInTransaction(sessionId, requestId, sActions);
//  }
//
//  @Transaction
//  public JsonArray approveAllActionsInTransaction(String sessionId, String requestId, String sActions)
//  {
//    ChangeRequest request = ChangeRequest.get(requestId);
//    
//    if (!this.permService.getPermissions(request).containsAll(Arrays.asList(
//        ChangeRequestPermissionAction.WRITE, ChangeRequestPermissionAction.WRITE_APPROVAL_STATUS, ChangeRequestPermissionAction.READ,
//        ChangeRequestPermissionAction.READ_DETAILS
//      )))
//    {
//      throw new CGRPermissionException();
//    }
//    
//    if (sActions != null && sActions.length() > 0)
//    {
//      JSONArray jaActions = new JSONArray(sActions);
//
//      for (int i = 0; i < jaActions.length(); ++i)
//      {
//        JSONObject joAction = jaActions.getJSONObject(i);
//
//        this.applyActionStatusPropertiesInTransaction(joAction.toString());
//      }
//    }
//
//    request.setAllActionsStatus(AllGovernanceStatus.ACCEPTED);
//
//    return this.getAllActions(sessionId, requestId);
//  }
//
//  @Request(RequestType.SESSION)
//  public JsonArray rejectAllActions(String sessionId, String requestId, String actions)
//  {
//    return rejectAllActionsInTransaction(sessionId, requestId, actions);
//  }
//
//  @Transaction
//  public JsonArray rejectAllActionsInTransaction(String sessionId, String requestId, String sActions)
//  {
//    ChangeRequest request = ChangeRequest.get(requestId);
//    
//    if (!this.permService.getPermissions(request).containsAll(Arrays.asList(
//        ChangeRequestPermissionAction.WRITE, ChangeRequestPermissionAction.WRITE_APPROVAL_STATUS, ChangeRequestPermissionAction.READ,
//        ChangeRequestPermissionAction.READ_DETAILS
//      )))
//    {
//      throw new CGRPermissionException();
//    }
//    
//    if (sActions != null && sActions.length() > 0)
//    {
//      JSONArray jaActions = new JSONArray(sActions);
//
//      for (int i = 0; i < jaActions.length(); ++i)
//      {
//        JSONObject joAction = jaActions.getJSONObject(i);
//
//        this.applyActionStatusPropertiesInTransaction(joAction.toString());
//      }
//    }
//
//    request.setAllActionsStatus(AllGovernanceStatus.REJECTED);
//
//    return this.getAllActions(sessionId, requestId);
//  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Request(RequestType.SESSION)
  public Page<ChangeRequest> getAllRequests(String sessionId, int pageSize, int pageNumber, String filter)
  {
    ChangeRequestQuery query = new ChangeRequestQuery(new QueryFactory());
    query.ORDER_BY_ASC(query.getCreateDate());

    if (filter != null && filter.equals("PENDING"))
    {
      query.WHERE(query.getApprovalStatus().containsAll(AllGovernanceStatus.PENDING));
    }
    else if (filter != null && filter.equals("REJECTED"))
    {
      query.WHERE(query.getApprovalStatus().containsAll(AllGovernanceStatus.REJECTED));
    }
    else if (filter != null && filter.equals("ACCEPTED"))
    {
      query.WHERE(query.getApprovalStatus().containsAll(AllGovernanceStatus.ACCEPTED));
    }
    
    query.restrictRows(pageSize, pageNumber);
    
    filterQueryBasedOnPermissions(query);
    
    List<? extends ChangeRequest> list = query.getIterator().getAll();
    
    for (ChangeRequest cr : list)
    {
      if (!ServiceFactory.getMetadataCache().getGeoObjectType(cr.getGeoObjectTypeCode()).isPresent())
      {
        cr.lock();
        cr.clearApprovalStatus();
        cr.addApprovalStatus(AllGovernanceStatus.INVALID);
        cr.apply();
      }
    }
    
    return new Page(query.getCount(), pageNumber, pageSize, list);
  }
  
  public void filterQueryBasedOnPermissions(ChangeRequestQuery crq)
  {
    List<String> raOrgs = new ArrayList<String>();
    List<String> goRoles = new ArrayList<String>();

    Condition cond = null;

    SingleActorDAOIF actor = Session.getCurrentSession().getUser();
    for (RoleDAOIF role : actor.authorizedRoles())
    {
      String roleName = role.getRoleName();

      if (RegistryRole.Type.isOrgRole(roleName) && !RegistryRole.Type.isRootOrgRole(roleName))
      {
        if (RegistryRole.Type.isRA_Role(roleName))
        {
          String roleOrgCode = RegistryRole.Type.parseOrgCode(roleName);
          raOrgs.add(roleOrgCode);
        }
        else if (RegistryRole.Type.isRM_Role(roleName) || RegistryRole.Type.isRC_Role(roleName) || RegistryRole.Type.isAC_Role(roleName))
        {
          goRoles.add(roleName);
        }
      }
    }

    for (String orgCode : raOrgs)
    {
      Organization org = Organization.getByCode(orgCode);

      Condition loopCond = crq.getOrganizationCode().EQ(org.getCode());

      if (cond == null)
      {
        cond = loopCond;
      }
      else
      {
        cond = cond.OR(loopCond);
      }
    }

    for (String roleName : goRoles)
    {
      String roleOrgCode = RegistryRole.Type.parseOrgCode(roleName);
      Organization org = Organization.getByCode(roleOrgCode);
      String gotCode = RegistryRole.Type.parseGotCode(roleName);

      Condition loopCond = crq.getGeoObjectTypeCode().EQ(gotCode).AND(crq.getOrganizationCode().EQ(org.getCode()));

      if (cond == null)
      {
        cond = loopCond;
      }
      else
      {
        cond = cond.OR(loopCond);
      }
      
      
      // If they have permission to an abstract parent type, then they also have permission to all its children.
      Optional<ServerGeoObjectType> op = ServiceFactory.getMetadataCache().getGeoObjectType(gotCode);
      
      if (op.isPresent() && op.get().getIsAbstract())
      {
        List<ServerGeoObjectType> subTypes = op.get().getSubtypes();
        
        for (ServerGeoObjectType subType : subTypes)
        {
          Condition superCond = crq.getGeoObjectTypeCode().EQ(subType.getCode()).AND(crq.getOrganizationCode().EQ(subType.getOrganization().getCode()));
          
          cond = cond.OR(superCond);
        }
      }
    }

    if (cond != null)
    {
      crq.AND(cond);
    }
  }
  
  @Request(RequestType.SESSION)
  public void setActionStatus(String sessionId, String actionOid, String status)
  {
    AbstractAction action = AbstractAction.lock(actionOid);
    action.clearApprovalStatus();
    action.addApprovalStatus(AllGovernanceStatus.valueOf(status));
    action.apply();
  }

//  /**
//   * 
//   * @param sessionId
//   * @param sJson
//   *          - serialized array of AbstractActions
//   */
//  @Request(RequestType.SESSION)
//  public void submitChangeRequest(String sessionId, String sJson)
//  {
//    new ChangeRequestService().submitChangeRequest(sJson);
//  }
//
//  @Transaction
//  public void submitChangeRequest(String sJson)
//  {
//    ChangeRequest cr = new ChangeRequest();
//    cr.addApprovalStatus(AllGovernanceStatus.PENDING);
//    cr.apply();
//
//    List<AbstractActionDTO> actionDTOs = AbstractActionDTO.parseActions(sJson);
//
//    for (AbstractActionDTO actionDTO : actionDTOs)
//    {
//      AbstractAction ra = AbstractAction.dtoToRegistry(actionDTO);
//      ra.addApprovalStatus(AllGovernanceStatus.PENDING);
//      ra.apply();
//
//      cr.addAction(ra).apply();
//    }
//    
//    if (!this.permService.getPermissions(cr).containsAll(Arrays.asList(
//        ChangeRequestPermissionAction.SUBMIT
//      )))
//    {
//      throw new CGRPermissionException();
//    }
//  }

//  @Request(RequestType.SESSION)
//  public JsonObject getRequestDetails(String sessionId, String requestId)
//  {
//    ChangeRequest request = ChangeRequest.get(requestId);
//
//    return request.getDetails();
//  }

  @Request(RequestType.SESSION)
  public void implementDecisions(String sessionId, String requestId)
  {
    ChangeRequest request = ChangeRequest.get(requestId);
    
    if (!this.permService.getPermissions(request).containsAll(Arrays.asList(
        ChangeRequestPermissionAction.EXECUTE, ChangeRequestPermissionAction.WRITE_APPROVAL_STATUS, ChangeRequestPermissionAction.WRITE
      )))
    {
      throw new CGRPermissionException();
    }
    
    request.execute(true);

//    return request.getDetails();
  }
  
  @Request(RequestType.SESSION)
  public String deleteChangeRequest(String sessionId, String requestId)
  {
    ChangeRequest request = ChangeRequest.get(requestId);
    
    if (!this.permService.getPermissions(request).containsAll(Arrays.asList(
        ChangeRequestPermissionAction.DELETE
      )))
    {
      throw new CGRPermissionException();
    }
    
    request.delete();

    return requestId;
  }

//  @Request(RequestType.SESSION)
//  public String lockAction(String sessionId, String actionId)
//  {
//    AbstractAction action = AbstractAction.get(actionId);
//    
//    if (!this.permService.getPermissions(action.getAllRequest().next()).containsAll(Arrays.asList(
//        ChangeRequestPermissionAction.WRITE
//      )))
//    {
//      throw new CGRPermissionException();
//    }
//
//    action.lock();
//
//    return action.toJson().toString();
//  }
//
//  @Request(RequestType.SESSION)
//  public String unlockAction(String sessionId, String actionId)
//  {
//    AbstractAction action = AbstractAction.get(actionId);
//
//    action.unlock();
//
//    return action.toJson().toString();
//  }
  
  @Transaction
  public void markAllAsInvalid(ServerGeoObjectType type)
  {
    String reason = LocalizationFacade.localize("changeRequest.invalidate.deleteReferencedGeoObjectType");
    
    ChangeRequestQuery crq = new ChangeRequestQuery(new QueryFactory());
    
    crq.WHERE(crq.getApprovalStatus().containsExactly(AllGovernanceStatus.PENDING));
    
    try (OIterator<? extends ChangeRequest> it = crq.getIterator())
    {
      for (ChangeRequest cr : it)
      {
        if (cr.getGeoObjectTypeCode().equals(type.getCode()))
        {
          cr.invalidate(reason);
        }
      }
    }
  }
}
