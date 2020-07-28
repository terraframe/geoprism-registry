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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.commongeoregistry.adapter.constants.CGRAdapterProperties;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.json.JSONException;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.dataaccess.transaction.Transaction;
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
import net.geoprism.registry.action.geoobject.UpdateGeoObjectAction;
import net.geoprism.registry.action.tree.AddChildAction;
import net.geoprism.registry.action.tree.RemoveChildAction;
import net.geoprism.registry.geoobject.ServerGeoObjectService;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.permission.GeoObjectPermissionService;
import net.geoprism.registry.service.RegistryService;
import net.geoprism.registry.service.ServiceFactory;

public class GeoObjectEditorControllerNoOverTime
{
  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF apply(ClientRequestIF request, @RequestParamter(name = "parentTreeNode") String parentTreeNode, @RequestParamter(name = "geoObject") String geoObject, @RequestParamter(name = "isNew") Boolean isNew, @RequestParamter(name = "masterListId") String masterListId) throws JSONException
  {
    applyInReq(request.getSessionId(), parentTreeNode, geoObject, isNew, masterListId);

    return new RestResponse();
  }

  @Request(RequestType.SESSION)
  public GeoObject applyInReq(String sessionId, String ptn, String go, Boolean isNew, String masterListId)
  {
    return applyInTransaction(sessionId, ptn, go, isNew, masterListId);
  }

  @Transaction
  private GeoObject applyInTransaction(String sessionId, String sPtn, String sGo, Boolean isNew, String masterListId)
  {
    GeoObject go;

    Map<String, String> roles = Session.getCurrentSession().getUserRoles();

    if (roles.keySet().contains(RegistryConstants.REGISTRY_CONTRIBUTOR_ROLE))
    {
      Instant base = Instant.now();
      int sequence = 0;

      ChangeRequest request = new ChangeRequest();
      request.addApprovalStatus(AllGovernanceStatus.PENDING);
      request.apply();

      if (!isNew)
      {
        UpdateGeoObjectAction action = new UpdateGeoObjectAction();
        action.addApprovalStatus(AllGovernanceStatus.PENDING);
        action.setCreateActionDate(Date.from(base.plus(sequence++, ChronoUnit.MINUTES)));
        action.setGeoObjectJson(sGo);
        action.setApiVersion(CGRAdapterProperties.getApiVersion());
        action.apply();
        request.addAction(action).apply();
      }
      else
      {
        CreateGeoObjectAction action = new CreateGeoObjectAction();
        action.addApprovalStatus(AllGovernanceStatus.PENDING);
        action.setCreateActionDate(Date.from(base.plus(sequence++, ChronoUnit.MINUTES)));
        action.setGeoObjectJson(sGo);
        action.setApiVersion(CGRAdapterProperties.getApiVersion());
        action.apply();

        request.addAction(action).apply();
      }

      ParentTreeNode ptn = ParentTreeNode.fromJSON(sPtn.toString(), ServiceFactory.getAdapter());

      applyChangeRequest(sessionId, request, ptn, isNew, base, sequence);
    }
    else
    {

      if (!isNew)
      {
        go = RegistryService.getInstance().updateGeoObject(sessionId, sGo.toString());
      }
      else
      {
        go = RegistryService.getInstance().createGeoObject(sessionId, sGo.toString());
      }

      ParentTreeNode ptn = ParentTreeNode.fromJSON(sPtn.toString(), ServiceFactory.getAdapter());

      applyPtn(sessionId, ptn);

      // Update the master list record
      if (masterListId != null)
      {
        ServerGeoObjectService service = new ServerGeoObjectService(new GeoObjectPermissionService());
        ServerGeoObjectIF geoObject = service.getGeoObject(go);

        if (!isNew)
        {
          MasterListVersion.get(masterListId).updateRecord(geoObject);
        }
        else
        {
          MasterListVersion.get(masterListId).publishRecord(geoObject);
        }
      }

      return go;
    }

    return null;
  }

  public void applyPtn(String sessionId, ParentTreeNode ptn)
  {
    GeoObject child = ptn.getGeoObject();
    List<ParentTreeNode> childDbParents = RegistryService.getInstance().getParentGeoObjects(sessionId, child.getUid(), child.getType().getCode(), null, false, null).getParents();

    // Remove all existing relationships which aren't what we're trying to
    // create
    for (ParentTreeNode ptnDbParent : childDbParents)
    {
      boolean shouldRemove = true;

      for (ParentTreeNode ptnParent : ptn.getParents())
      {
        if (ptnParent.getGeoObject().equals(ptnDbParent.getGeoObject()) && ptnParent.getHierachyType().getCode().equals(ptnDbParent.getHierachyType().getCode()))
        {
          shouldRemove = false;
        }
      }

      if (shouldRemove)
      {
        ServiceFactory.getGeoObjectService().removeChild(sessionId, ptnDbParent.getGeoObject().getUid(), ptnDbParent.getGeoObject().getType().getCode(), child.getUid(), child.getType().getCode(), ptnDbParent.getHierachyType().getCode());
      }
    }

    // Create new relationships that don't already exist
    for (ParentTreeNode ptnParent : ptn.getParents())
    {
      boolean alreadyExists = false;

      for (ParentTreeNode ptnDbParent : childDbParents)
      {
        if (ptnParent.getGeoObject().equals(ptnDbParent.getGeoObject()) && ptnParent.getHierachyType().getCode().equals(ptnDbParent.getHierachyType().getCode()))
        {
          alreadyExists = true;
        }
      }

      if (!alreadyExists)
      {
        GeoObject parent = ptnParent.getGeoObject();
        ServiceFactory.getGeoObjectService().addChild(sessionId, parent.getUid(), parent.getType().getCode(), child.getUid(), child.getType().getCode(), ptnParent.getHierachyType().getCode());
      }
    }
  }

  public void applyChangeRequest(String sessionId, ChangeRequest request, ParentTreeNode ptn, boolean isNew, Instant base, int sequence)
  {
    GeoObject child = ptn.getGeoObject();

    List<ParentTreeNode> childDbParents = new LinkedList<ParentTreeNode>();

    if (!isNew)
    {
      childDbParents = RegistryService.getInstance().getParentGeoObjects(sessionId, child.getUid(), child.getType().getCode(), null, false, null).getParents();

      // Remove all existing relationships which aren't what we're trying to
      // create
      for (ParentTreeNode ptnDbParent : childDbParents)
      {
        boolean shouldRemove = true;

        for (ParentTreeNode ptnParent : ptn.getParents())
        {
          if (ptnParent.getGeoObject().equals(ptnDbParent.getGeoObject()) && ptnParent.getHierachyType().getCode().equals(ptnDbParent.getHierachyType().getCode()))
          {
            shouldRemove = false;
          }
        }

        if (shouldRemove)
        {
          GeoObject parent = ptnDbParent.getGeoObject();

          RemoveChildAction action = new RemoveChildAction();
          action.addApprovalStatus(AllGovernanceStatus.PENDING);
          action.setChildId(child.getUid());
          action.setChildTypeCode(child.getType().getCode());
          action.setParentId(parent.getUid());
          action.setParentTypeCode(parent.getType().getCode());
          action.setHierarchyTypeCode(ptnDbParent.getHierachyType().getCode());
          action.setCreateActionDate(Date.from(base.plus(sequence++, ChronoUnit.MINUTES)));
          action.setApiVersion(CGRAdapterProperties.getApiVersion());
          action.apply();

          request.addAction(action).apply();

        }
      }
    }

    // Create new relationships that don't already exist
    for (ParentTreeNode ptnParent : ptn.getParents())
    {
      boolean alreadyExists = false;

      if (!isNew)
      {
        for (ParentTreeNode ptnDbParent : childDbParents)
        {
          if (ptnParent.getGeoObject().equals(ptnDbParent.getGeoObject()) && ptnParent.getHierachyType().getCode().equals(ptnDbParent.getHierachyType().getCode()))
          {
            alreadyExists = true;
          }
        }

      }

      if (!alreadyExists)
      {
        GeoObject parent = ptnParent.getGeoObject();

        AddChildAction action = new AddChildAction();
        action.addApprovalStatus(AllGovernanceStatus.PENDING);
        action.setChildId(child.getUid());
        action.setChildTypeCode(child.getType().getCode());
        action.setParentId(parent.getUid());
        action.setParentTypeCode(parent.getType().getCode());
        action.setHierarchyTypeCode(ptnParent.getHierachyType().getCode());
        action.setCreateActionDate(Date.from(base.plus(sequence++, ChronoUnit.MINUTES)));
        action.setApiVersion(CGRAdapterProperties.getApiVersion());
        action.apply();

        request.addAction(action).apply();
      }
    }
  }

}
