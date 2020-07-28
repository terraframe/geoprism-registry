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
package net.geoprism.registry.controller;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

import org.commongeoregistry.adapter.constants.CGRAdapterProperties;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.json.JSONException;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestResponse;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;

import net.geoprism.registry.MasterListVersion;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.action.AllGovernanceStatus;
import net.geoprism.registry.action.ChangeRequest;
import net.geoprism.registry.action.geoobject.CreateGeoObjectAction;
import net.geoprism.registry.action.geoobject.SetParentAction;
import net.geoprism.registry.action.geoobject.UpdateGeoObjectAction;
import net.geoprism.registry.geoobject.ServerGeoObjectService;
import net.geoprism.registry.model.CompositeServerGeoObject;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.permission.GeoObjectPermissionService;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.view.ServerParentTreeNodeOverTime;

@Controller(url = "geoobject-editor")
public class GeoObjectEditorController
{
  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF apply(ClientRequestIF request, @RequestParamter(name = "parentTreeNode") String parentTreeNode, @RequestParamter(name = "geoObject") String geoObject, @RequestParamter(name = "isNew") Boolean isNew, @RequestParamter(name = "masterListId") String masterListId, @RequestParamter(name = "notes") String notes) throws JSONException
  {
    applyInReq(request.getSessionId(), parentTreeNode, geoObject, isNew, masterListId, notes);

    return new RestResponse();
  }

  @Request(RequestType.SESSION)
  public GeoObjectOverTime applyInReq(String sessionId, String ptn, String sGO, Boolean isNew, String masterListId, String notes)
  {
    return applyInTransaction(sessionId, ptn, sGO, isNew, masterListId, notes);
  }

  @Transaction
  private GeoObjectOverTime applyInTransaction(String sessionId, String sPtn, String sGO, Boolean isNew, String masterListId, String notes)
  {
    Map<String, String> roles = Session.getCurrentSession().getUserRoles();

    if (roles.keySet().contains(RegistryConstants.REGISTRY_CONTRIBUTOR_ROLE))
    {
      Instant base = Instant.now();
      int sequence = 0;

      GeoObjectOverTime timeGO = GeoObjectOverTime.fromJSON(ServiceFactory.getAdapter(), sGO);

      ChangeRequest request = new ChangeRequest();
      request.addApprovalStatus(AllGovernanceStatus.PENDING);
      request.setContributorNotes(notes);
      request.apply();

      if (!isNew)
      {
        UpdateGeoObjectAction action = new UpdateGeoObjectAction();
        action.addApprovalStatus(AllGovernanceStatus.PENDING);
        action.setCreateActionDate(Date.from(base.plus(sequence++, ChronoUnit.MINUTES)));
        action.setGeoObjectJson(sGO);
        action.setApiVersion(CGRAdapterProperties.getApiVersion());
        action.setContributorNotes(notes);
        action.apply();
        request.addAction(action).apply();
      }
      else
      {
        CreateGeoObjectAction action = new CreateGeoObjectAction();
        action.addApprovalStatus(AllGovernanceStatus.PENDING);
        action.setCreateActionDate(Date.from(base.plus(sequence++, ChronoUnit.MINUTES)));
        action.setGeoObjectJson(sGO);
        action.setApiVersion(CGRAdapterProperties.getApiVersion());
        action.setContributorNotes(notes);
        action.apply();

        request.addAction(action).apply();
      }

      SetParentAction action = new SetParentAction();
      action.addApprovalStatus(AllGovernanceStatus.PENDING);
      action.setCreateActionDate(Date.from(base.plus(sequence++, ChronoUnit.MINUTES)));
      action.setChildCode(timeGO.getCode());
      action.setChildTypeCode(timeGO.getType().getCode());
      action.setJson(sPtn);
      action.setApiVersion(CGRAdapterProperties.getApiVersion());
      action.setContributorNotes(notes);
      action.apply();

      request.addAction(action).apply();
    }
    else
    {
      ServerGeoObjectService service = new ServerGeoObjectService(new GeoObjectPermissionService());

      GeoObjectOverTime timeGO = GeoObjectOverTime.fromJSON(ServiceFactory.getAdapter(), sGO);

      ServerGeoObjectIF serverGO = service.apply(timeGO, isNew, false);
      final ServerGeoObjectType type = serverGO.getType();

      ServerParentTreeNodeOverTime ptnOt = ServerParentTreeNodeOverTime.fromJSON(type, sPtn);

      serverGO.setParents(ptnOt);

      // Update the master list record
      if (masterListId != null)
      {
        if (serverGO instanceof CompositeServerGeoObject)
        {
          serverGO = ( (CompositeServerGeoObject) serverGO ).getVertexServerGeoObject();
        }

        if (!isNew)
        {
          MasterListVersion.get(masterListId).updateRecord(serverGO);
        }
        else
        {
          MasterListVersion.get(masterListId).publishRecord(serverGO);
        }
      }

      return serverGO.toGeoObjectOverTime();
    }

    return null;
  }
}
