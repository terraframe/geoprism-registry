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

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
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
import net.geoprism.georegistry.service.RegistryService;

/**
 * This controller is used by the change request table widget.
 * 
 * @author rrowlands
 *
 */
@Controller(url = "changerequest")
public class ChangeRequestController 
{
  
  
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
      
      actions.put(buildAction(action));
    }
//    changeRequest.put("actions", actions);
//    
//    return changeRequest.toString();
    
    return actions.toString();
  }
  
  private JSONObject buildAction(AbstractAction action)
  {
    JSONObject jo = new JSONObject();
    
    jo.put(AbstractAction.APPROVALSTATUS, action.getApprovalStatus().get(0));
    
    jo.put(AbstractAction.CREATEACTIONDATE, action.getCreateActionDate());
    
    jo.put("label", action.getMdClass().getDisplayLabel(Session.getCurrentLocale()));
    
//    if (action instanceof CreateGeoObjectAction)
//    {
//      String json = ( (CreateGeoObjectAction) action ).getGeoObjectJson();
//      
//      jo.put(CreateGeoObjectAction.GEOOBJECTJSON, new JSONObject(json));
//    }
//    else if (action instanceof UpdateGeoObjectAction)
//    {
//      String json = ( (UpdateGeoObjectAction) action ).getGeoObjectJson();
//      
//      jo.put(UpdateGeoObjectAction.GEOOBJECTJSON, new JSONObject(json));
//    }
//    else if (action instanceof AddChildAction)
//    {
//      AddChildAction aca = (AddChildAction) action;
//      
//      GeoObject child = RegistryService.getInstance().getGeoObject(Session.getCurrentSession().getOid(), aca.getChildId(), aca.getChildTypeCode());
//      GeoObject parent = RegistryService.getInstance().getGeoObject(Session.getCurrentSession().getOid(), aca.getParentId(), aca.getParentTypeCode());
//      
//      jo.put("childCode", child.getCode());
//      jo.put("parentCode", parent.getCode());
//      jo.put("hierarchyCode", aca.getHierarchyCode());
//    }
//    else if (action instanceof RemoveChildAction)
//    {
//      RemoveChildAction rca = (RemoveChildAction) action;
//      
//      GeoObject child = RegistryService.getInstance().getGeoObject(Session.getCurrentSession().getOid(), rca.getChildId(), rca.getChildTypeCode());
//      GeoObject parent = RegistryService.getInstance().getGeoObject(Session.getCurrentSession().getOid(), rca.getParentId(), rca.getParentTypeCode());
//      
//      jo.put("childCode", child.getCode());
//      jo.put("parentCode", parent.getCode());
//      jo.put("hierarchyCode", rca.getHierarchyCode());
//    }
    
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
