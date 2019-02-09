/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.georegistry.controller;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;

import net.geoprism.georegistry.action.AbstractAction;
import net.geoprism.georegistry.action.AbstractActionQuery;
import net.geoprism.georegistry.action.AllGovernanceStatus;
import net.geoprism.georegistry.action.geoobject.CreateGeoObjectAction;
import net.geoprism.georegistry.action.geoobject.UpdateGeoObjectAction;
import net.geoprism.georegistry.action.tree.AddChildAction;
import net.geoprism.georegistry.action.tree.RemoveChildAction;

/**
 * This controller is used by the change request table widget.
 * 
 * @author rrowlands
 *
 */
@Controller(url = "changerequest")
public class ChangeRequestController 
{
  private void updateActionFromJson(AbstractAction action, JSONObject joAction)
  {
    if (action instanceof CreateGeoObjectAction)
    {
      ( (CreateGeoObjectAction) action ).setGeoObjectJson(joAction.getJSONObject(CreateGeoObjectAction.GEOOBJECTJSON).toString());
    }
    else if (action instanceof UpdateGeoObjectAction)
    {
      ( (UpdateGeoObjectAction) action ).setGeoObjectJson(joAction.getJSONObject(UpdateGeoObjectAction.GEOOBJECTJSON).toString());
    }
    else if (action instanceof AddChildAction)
    {
      AddChildAction aca = (AddChildAction) action;
      
      aca.setChildTypeCode(joAction.getString(AddChildAction.CHILDTYPECODE));
      aca.setChildId(joAction.getString(AddChildAction.CHILDID));
      aca.setParentId(joAction.getString(AddChildAction.PARENTID));
      aca.setParentTypeCode(joAction.getString(AddChildAction.PARENTTYPECODE));
      aca.setHierarchyTypeCode(joAction.getString(AddChildAction.HIERARCHYTYPECODE));
    }
    else if (action instanceof RemoveChildAction)
    {
      RemoveChildAction rca = (RemoveChildAction) action;
      
      rca.setChildTypeCode(joAction.getString(RemoveChildAction.CHILDTYPECODE));
      rca.setChildId(joAction.getString(RemoveChildAction.CHILDID));
      rca.setParentId(joAction.getString(RemoveChildAction.PARENTID));
      rca.setParentTypeCode(joAction.getString(RemoveChildAction.PARENTTYPECODE));
      rca.setHierarchyTypeCode(joAction.getString(RemoveChildAction.HIERARCHYTYPECODE));
    }
  }
  
  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF acceptAction(ClientRequestIF request, @RequestParamter(name = "action") String action) throws JSONException
  {
    acceptActionInRequest(request.getSessionId(), request, action);
    
    return new RestResponse();
  }
  @Request(RequestType.SESSION)
  private void acceptActionInRequest(String sessionId, ClientRequestIF request, String sAction)
  {
    acceptActionInTransaction(sAction);
  }
  @Transaction
  private void acceptActionInTransaction(String sAction)
  {
    JSONObject joAction = new JSONObject(sAction);
    
    AbstractAction action = AbstractAction.get(joAction.getString("oid"));
    
    action.appLock();
    
    updateActionFromJson(action, joAction);
    
    action.execute();
    
    action.clearApprovalStatus();
    action.addApprovalStatus(AllGovernanceStatus.ACCEPTED);
    action.apply();
  }
  
  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF rejectAction(ClientRequestIF request, @RequestParamter(name = "action") String action) throws JSONException
  {
    rejectActionInRequest(request.getSessionId(), request, action);
    
    return new RestResponse();
  }
  @Request(RequestType.SESSION)
  private void rejectActionInRequest(String sessionId, ClientRequestIF request, String sAction)
  {
    rejectActionInTransaction(sAction);
  }
  @Transaction
  private void rejectActionInTransaction(String sAction)
  {
    JSONObject joAction = new JSONObject(sAction);
    
    AbstractAction action = AbstractAction.get(joAction.getString("oid"));
    
    action.appLock();
    
    updateActionFromJson(action, joAction);
    
    action.clearApprovalStatus();
    action.addApprovalStatus(AllGovernanceStatus.REJECTED);
    action.apply();
  }
  
  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF getAllActions(ClientRequestIF request, @RequestParamter(name = "entityId") String entityId) throws JSONException
  {
    String json = getAllActionsInRequest(request.getSessionId(), request);
    
    return new RestBodyResponse(json);
  }
  
  @Request(RequestType.SESSION)
  private String getAllActionsInRequest(String sessionId, ClientRequestIF request)
  {
//    JSONObject changeRequest = new JSONObject();
//    
//    changeRequest.put(AbstractAction.APPROVALSTATUS, buildGovernanceStatus(AllGovernanceStatus.PENDING));
    
    JSONArray actions = new JSONArray();
    AbstractActionQuery query = new AbstractActionQuery(new QueryFactory());
    Iterator<? extends AbstractAction> it = query.getIterator();
    while (it.hasNext())
    {
      AbstractAction action = it.next();
      
      actions.put(serializeAction(action));
    }
//    changeRequest.put("actions", actions);
//    
//    return changeRequest.toString();
    
    return actions.toString();
  }
  
  private JSONObject serializeAction(AbstractAction action)
  {
    JSONObject jo = new JSONObject();
    
    jo.put(AbstractAction.OID, action.getOid());
    
    jo.put("actionType", action.getType());
    
    jo.put("actionLabel", action.getMdClass().getDisplayLabel(Session.getCurrentLocale()));
    
    jo.put(AbstractAction.APPROVALSTATUS, action.getApprovalStatus().get(0));
    
    jo.put(AbstractAction.CREATEACTIONDATE, action.getCreateActionDate());
    
    if (action instanceof CreateGeoObjectAction)
    {
      String json = ( (CreateGeoObjectAction) action ).getGeoObjectJson();
      
      jo.put(CreateGeoObjectAction.GEOOBJECTJSON, new JSONObject(json));
    }
    else if (action instanceof UpdateGeoObjectAction)
    {
      String json = ( (UpdateGeoObjectAction) action ).getGeoObjectJson();
      
      jo.put(UpdateGeoObjectAction.GEOOBJECTJSON, new JSONObject(json));
    }
    else if (action instanceof AddChildAction)
    {
      AddChildAction aca = (AddChildAction) action;
      
      jo.put(AddChildAction.CHILDID, aca.getChildId());
      jo.put(AddChildAction.CHILDTYPECODE, aca.getChildTypeCode());
      jo.put(AddChildAction.PARENTID, aca.getParentId());
      jo.put(AddChildAction.PARENTTYPECODE, aca.getParentTypeCode());
      jo.put(AddChildAction.HIERARCHYTYPECODE, aca.getHierarchyTypeCode());
    }
    else if (action instanceof RemoveChildAction)
    {
      RemoveChildAction rca = (RemoveChildAction) action;
      
      jo.put(RemoveChildAction.CHILDID, rca.getChildId());
      jo.put(RemoveChildAction.CHILDTYPECODE, rca.getChildTypeCode());
      jo.put(RemoveChildAction.PARENTID, rca.getParentId());
      jo.put(RemoveChildAction.PARENTTYPECODE, rca.getParentTypeCode());
      jo.put(RemoveChildAction.HIERARCHYTYPECODE, rca.getHierarchyTypeCode());
    }
    
    return jo;
  }
  
  private JSONObject buildGovernanceStatus(AllGovernanceStatus gs)
  {
    JSONObject governanceStatus = new JSONObject();
    governanceStatus.put("key", gs.getEnumName());
    governanceStatus.put("label", gs.getDisplayLabel());
    return governanceStatus;
  }
}
