/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.service.request;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.commongeoregistry.adapter.constants.CGRAdapterProperties;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;

import net.geoprism.registry.CGRPermissionException;
import net.geoprism.registry.ListType;
import net.geoprism.registry.ListTypeVersion;
import net.geoprism.registry.action.AbstractAction;
import net.geoprism.registry.action.AllGovernanceStatus;
import net.geoprism.registry.action.ChangeRequest;
import net.geoprism.registry.action.geoobject.CreateGeoObjectAction;
import net.geoprism.registry.action.geoobject.UpdateAttributeAction;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.service.permission.RolePermissionService;
import net.geoprism.registry.view.ServerParentTreeNodeOverTime;
import net.geoprism.registry.view.action.AbstractUpdateAttributeView;
import net.geoprism.registry.view.action.UpdateAttributeViewJsonAdapters;

@Service
public class GeoObjectEditorService implements GeoObjectEditorServiceIF
{
  @Autowired
  private RolePermissionService      permissions;

  @Autowired
  private GeoObjectBusinessServiceIF service;

//  @Override
//  @Transaction
//  public ServerGeoObjectIF apply(GeoObjectOverTime goTime, boolean isNew, boolean isImport)
//  {
//    try
//    {
//      ServerGeoObjectIF goServer = super.apply(goTime, isNew, isImport);
//
//      if (!isImport)
//      {
//        ( (GPRVertexServerGeoObject) goServer ).setAlternateIds(goTime.getAlternateIds());
//      }
//
//      return goServer;
//    }
//    catch (DuplicateDataException e)
//    {
//      handleDuplicateDataException(ServerGeoObjectType.get(goTime.getType()), e);
//
//      throw e;
//    }
//  }
//
//  @Override
//  @Transaction
//  public ServerGeoObjectIF apply(GeoObject object, Date startDate, Date endDate, boolean isNew, boolean isImport)
//  {
//    try
//    {
//      ServerGeoObjectIF geoObject = super.apply(object, startDate, endDate, isNew, isImport);
//
//      if (!isImport)
//      {
//        ( (GPRVertexServerGeoObject) geoObject ).setAlternateIds(object.getAlternateIds());
//      }
//
//      // Return the refreshed copy of the geoObject
//      return geoObject;
//    }
//    catch (DuplicateDataException e)
//    {
//      handleDuplicateDataException(ServerGeoObjectType.get(object.getType()), e);
//
//      throw e;
//    }
//  }
//
//  public void handleDuplicateDataException(ServerGeoObjectType type, DuplicateDataException e)
//  {
//    MdClassDAOIF mdClass = e.getMdClassDAOIF();
//
//    if (mdClass.definesType().equals(ExternalId.CLASS))
//    {
//      String key = e.getValues().get(0);
//      String externalId = key.split(ExternalId.KEY_SEPARATOR)[0];
//      String externalSystem = ExternalSystem.get(key.split(ExternalId.KEY_SEPARATOR)[1]).getDisplayLabel().getValue();
//
//      DuplicateExternalIdException ex = new DuplicateExternalIdException();
//      ex.setExternalId(externalId);
//      ex.setExternalSystem(externalSystem);
//      throw ex;
//    }
//    else
//    {
//      super.handleDuplicateDataException(type, e);
//    }
//  }

  @Override
  @Request(RequestType.SESSION)
  public JsonObject createGeoObject(String sessionId, String ptn, String sTimeGo, String masterListId, String notes)
  {
    return this.createGeoObjectInTrans(ptn, sTimeGo, masterListId, notes);
  }

  @Transaction
  public JsonObject createGeoObjectInTrans(String sPtn, String sTimeGo, String masterListId, String notes)
  {
    LocaleSerializer serializer = new LocaleSerializer(Session.getCurrentLocale());

    GeoObjectOverTime timeGO = GeoObjectOverTime.fromJSON(ServiceFactory.getAdapter(), sTimeGo);

    ServerGeoObjectType serverGOT = ServerGeoObjectType.get(timeGO.getType());

    final String orgCode = serverGOT.getOrganization().getCode();

    if (permissions.isSRA() || permissions.isRA(orgCode) || permissions.isRM(orgCode, serverGOT))
    {
      ServerGeoObjectIF serverGO = this.service.apply(timeGO, true, false);
      final ServerGeoObjectType type = serverGO.getType();

      if (sPtn != null)
      {
        ServerParentTreeNodeOverTime ptnOt = ServerParentTreeNodeOverTime.fromJSON(type, sPtn);

        this.service.setParents(serverGO, ptnOt);
      }

      // Update all of the working lists which have this record
      ListType.getForType(type).forEach(listType -> {
        listType.getWorkingVersions().forEach(version -> version.publishOrUpdateRecord(serverGO));
      });

      JsonObject resp = new JsonObject();

      resp.addProperty("isChangeRequest", false);
      resp.add("geoObject", this.service.toGeoObjectOverTime(serverGO).toJSON(serializer));

      return resp;
    }
    else if (permissions.isRC(orgCode, serverGOT))
    {
      Instant base = Instant.now();
      int sequence = 0;

      ChangeRequest request = new ChangeRequest();
      request.addApprovalStatus(AllGovernanceStatus.PENDING);
      request.setContributorNotes(notes);
      request.setGeoObjectCode(timeGO.getCode());
      request.setGeoObjectTypeCode(timeGO.getType().getCode());
      request.setOrganizationCode(orgCode);
      request.apply();

      CreateGeoObjectAction action = new CreateGeoObjectAction();
      action.addApprovalStatus(AllGovernanceStatus.PENDING);
      action.setCreateActionDate(Date.from(base.plus(sequence++, ChronoUnit.MINUTES)));
      action.setGeoObjectJson(sTimeGo);
      action.setParentJson(sPtn);
      action.setApiVersion(CGRAdapterProperties.getApiVersion());
      action.setContributorNotes(notes);
      action.apply();

      request.addAction(action).apply();

      JsonObject resp = new JsonObject();

      resp.addProperty("isChangeRequest", true);
      resp.addProperty("changeRequestId", request.getOid());

      return resp;
    }
    else
    {
      throw new CGRPermissionException();
    }
  }

  @Override
  @Request(RequestType.SESSION)
  public JsonObject updateGeoObject(String sessionId, String geoObjectCode, String geoObjectTypeCode, String actions, String masterListId, String notes)
  {
    return this.updateGeoObjectInTrans(geoObjectCode, geoObjectTypeCode, actions, masterListId, notes);
  }

  @Override
  @Transaction
  public JsonObject updateGeoObjectInTrans(String geoObjectCode, String geoObjectTypeCode, String actions, String masterListId, String notes)
  {
    LocaleSerializer serializer = new LocaleSerializer(Session.getCurrentLocale());

    final ServerGeoObjectType type = ServerGeoObjectType.get(geoObjectTypeCode);
    final String orgCode = type.getOrganization().getCode();
    final VertexServerGeoObject go = (VertexServerGeoObject) this.service.getGeoObjectByCode(geoObjectCode, geoObjectTypeCode);

    final JsonArray jaActions = JsonParser.parseString(actions).getAsJsonArray();

    if (permissions.isSRA() || permissions.isRA(orgCode) || permissions.isRM(orgCode, type))
    {
      this.executeActions(type, go, jaActions);

      if (masterListId != null)
      {
        ListTypeVersion.get(masterListId).updateRecord(go);
      }

      JsonObject resp = new JsonObject();

      resp.addProperty("isChangeRequest", false);
      resp.add("geoObject", this.service.toGeoObjectOverTime(go).toJSON(serializer));

      return resp;
    }
    else if (permissions.isRC(orgCode, type))
    {
      ChangeRequest request = createChangeRequest(geoObjectCode, geoObjectTypeCode, notes, orgCode, jaActions);

      JsonObject resp = new JsonObject();

      resp.addProperty("isChangeRequest", true);
      resp.addProperty("changeRequestId", request.getOid());

      return resp;
    }
    else
    {
      throw new CGRPermissionException();
    }
  }

  public ChangeRequest createChangeRequest(String geoObjectCode, String geoObjectTypeCode, String notes, final String orgCode, final JsonArray jaActions)
  {
    Instant base = Instant.now();
    int sequence = 0;

    ChangeRequest request = new ChangeRequest();
    request.addApprovalStatus(AllGovernanceStatus.PENDING);
    request.setContributorNotes(notes);
    request.setGeoObjectCode(geoObjectCode);
    request.setGeoObjectTypeCode(geoObjectTypeCode);
    request.setOrganizationCode(orgCode);
    request.apply();

    for (int i = 0; i < jaActions.size(); ++i)
    {
      JsonObject joAction = jaActions.get(i).getAsJsonObject();

      String attributeName = joAction.get("attributeName").getAsString();
      JsonObject attributeDiff = joAction.get("attributeDiff").getAsJsonObject();

      UpdateAttributeAction action = new UpdateAttributeAction();
      action.addApprovalStatus(AllGovernanceStatus.PENDING);
      action.setCreateActionDate(Date.from(base.plus(sequence++, ChronoUnit.MINUTES)));
      action.setAttributeName(attributeName);
      action.setJson(attributeDiff.toString());
      action.setApiVersion(CGRAdapterProperties.getApiVersion());
      action.setContributorNotes(notes);
      action.apply();

      request.addAction(action).apply();
    }

    return request;
  }

  @Override
  @Transaction
  public ChangeRequest updateChangeRequest(ChangeRequest request, String notes, final JsonArray jaActions)
  {
    Instant base = Instant.now();
    int sequence = 0;

    // Delete all existing actions
    try (OIterator<? extends AbstractAction> actions = request.getAllAction())
    {
      while (actions.hasNext())
      {
        AbstractAction action = actions.next();
        action.delete();
      }
    }

    // Create the new actions
    for (int i = 0; i < jaActions.size(); ++i)
    {
      JsonObject joAction = jaActions.get(i).getAsJsonObject();
      String actionType = joAction.get("actionType").getAsString();

      if (actionType.equals(CreateGeoObjectAction.class.getSimpleName()))
      {
        CreateGeoObjectAction action = new CreateGeoObjectAction();
        action.addApprovalStatus(AllGovernanceStatus.PENDING);
        action.setCreateActionDate(Date.from(base.plus(sequence++, ChronoUnit.MINUTES)));
        action.setApiVersion(CGRAdapterProperties.getApiVersion());
        action.setContributorNotes(notes);

        if (joAction.has(CreateGeoObjectAction.GEOOBJECTJSON) && !joAction.get(CreateGeoObjectAction.GEOOBJECTJSON).isJsonNull())
        {
          action.setGeoObjectJson(joAction.get(CreateGeoObjectAction.GEOOBJECTJSON).getAsJsonObject().toString());
        }

        if (joAction.has(CreateGeoObjectAction.PARENTJSON) && !joAction.get(CreateGeoObjectAction.PARENTJSON).isJsonNull())
        {
          action.setParentJson(joAction.get(CreateGeoObjectAction.PARENTJSON).getAsJsonArray().toString());
        }

        action.apply();

        request.addAction(action).apply();
      }
      else
      {
        String attributeName = joAction.get("attributeName").getAsString();
        JsonObject attributeDiff = joAction.get("attributeDiff").getAsJsonObject();

        UpdateAttributeAction action = new UpdateAttributeAction();
        action.addApprovalStatus(AllGovernanceStatus.PENDING);
        action.setCreateActionDate(Date.from(base.plus(sequence++, ChronoUnit.MINUTES)));
        action.setAttributeName(attributeName);
        action.setJson(attributeDiff.toString());
        action.setApiVersion(CGRAdapterProperties.getApiVersion());
        action.setContributorNotes(notes);
        action.apply();

        request.addAction(action).apply();
      }
    }

    request.appLock();
    request.setContributorNotes(notes);
    request.apply();

    return request;
  }

  public void executeActions(final ServerGeoObjectType type, final VertexServerGeoObject go, final JsonArray jaActions)
  {
    for (int i = 0; i < jaActions.size(); ++i)
    {
      JsonObject action = jaActions.get(i).getAsJsonObject();

      String attributeName = action.get("attributeName").getAsString();
      JsonObject attributeDiff = action.get("attributeDiff").getAsJsonObject();

      AbstractUpdateAttributeView view = UpdateAttributeViewJsonAdapters.deserialize(attributeDiff.toString(), attributeName, type);

      view.execute(go);
    }

    this.service.apply(go, false);
  }

}
