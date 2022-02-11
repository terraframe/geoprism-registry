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
package net.geoprism.registry.geoobject;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import org.commongeoregistry.adapter.constants.CGRAdapterProperties;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.DuplicateDataException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;

import net.geoprism.registry.CGRPermissionException;
import net.geoprism.registry.InvalidRegistryIdException;
import net.geoprism.registry.ListTypeVersion;
import net.geoprism.registry.action.AbstractAction;
import net.geoprism.registry.action.AllGovernanceStatus;
import net.geoprism.registry.action.ChangeRequest;
import net.geoprism.registry.action.geoobject.CreateGeoObjectAction;
import net.geoprism.registry.action.geoobject.UpdateAttributeAction;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.conversion.ServerGeoObjectStrategyIF;
import net.geoprism.registry.conversion.VertexGeoObjectStrategy;
import net.geoprism.registry.etl.export.GeoObjectExportFormat;
import net.geoprism.registry.etl.export.GeoObjectJsonExporter;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerParentTreeNode;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.permission.GeoObjectPermissionService;
import net.geoprism.registry.permission.GeoObjectPermissionServiceIF;
import net.geoprism.registry.permission.RolePermissionService;
import net.geoprism.registry.query.ServerGeoObjectQuery;
import net.geoprism.registry.query.graph.VertexGeoObjectQuery;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.view.GeoObjectSplitView;
import net.geoprism.registry.view.ServerParentTreeNodeOverTime;
import net.geoprism.registry.view.action.AbstractUpdateAttributeView;
import net.geoprism.registry.view.action.UpdateAttributeViewJsonAdapters;

public class ServerGeoObjectService extends LocalizedValueConverter
{
  private GeoObjectPermissionServiceIF permissionService;

  public ServerGeoObjectService()
  {
    this(new GeoObjectPermissionService());
  }

  public ServerGeoObjectService(GeoObjectPermissionServiceIF permissionService)
  {
    this.permissionService = permissionService;
  }

  @Request(RequestType.SESSION)
  public JsonObject getAll(String sessionId, String gotCode, String hierarchyCode, Date since, Boolean includeLevel, String format, String externalSystemId, Integer pageNumber, Integer pageSize)
  {
    GeoObjectExportFormat goef = null;
    if (format != null && format.length() > 0)
    {
      goef = GeoObjectExportFormat.valueOf(format);
    }

    GeoObjectJsonExporter exporter = new GeoObjectJsonExporter(gotCode, hierarchyCode, since, includeLevel, goef, externalSystemId, pageSize, pageNumber);

    try
    {
      return exporter.export();
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  @Request(RequestType.SESSION)
  public ParentTreeNode addChild(String sessionId, String parentId, String parentGeoObjectTypeCode, String childId, String childGeoObjectTypeCode, String hierarchyCode)
  {
    ServerGeoObjectIF parent = this.getGeoObject(parentId, parentGeoObjectTypeCode);
    ServerGeoObjectIF child = this.getGeoObject(childId, childGeoObjectTypeCode);
    ServerHierarchyType ht = ServerHierarchyType.get(hierarchyCode);

    ServiceFactory.getGeoObjectRelationshipPermissionService().enforceCanAddChild(ht.getOrganization().getCode(), parent.getType(), child.getType());

    return parent.addChild(child, ht).toNode(false);
  }

  @Request(RequestType.SESSION)
  public void removeChild(String sessionId, String parentId, String parentGeoObjectTypeCode, String childId, String childGeoObjectTypeCode, String hierarchyCode)
  {
    ServerGeoObjectIF parent = this.getGeoObject(parentId, parentGeoObjectTypeCode);
    ServerGeoObjectIF child = this.getGeoObject(childId, childGeoObjectTypeCode);
    ServerHierarchyType ht = ServerHierarchyType.get(hierarchyCode);

    ServiceFactory.getGeoObjectRelationshipPermissionService().enforceCanRemoveChild(ht.getOrganization().getCode(), parent.getType(), child.getType());

    parent.removeChild(child, hierarchyCode);
  }

  @Transaction
  public ServerGeoObjectIF apply(GeoObject object, Date startDate, Date endDate, boolean isNew, boolean isImport)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(object.getType());
    ServerGeoObjectStrategyIF strategy = this.getStrategy(type);

    if (isNew)
    {
      permissionService.enforceCanCreate(type.getOrganization().getCode(), type);
    }
    else
    {
      permissionService.enforceCanWrite(type.getOrganization().getCode(), type);
    }

    ServerGeoObjectIF geoObject = strategy.constructFromGeoObject(object, isNew);
    geoObject.setDate(startDate);

    if (!isNew)
    {
      geoObject.lock();
    }

    geoObject.populate(object, startDate, endDate);

    try
    {
      geoObject.apply(isImport);

      // Return the refreshed copy of the geoObject
      return this.build(type, geoObject.getRunwayId());
    }
    catch (DuplicateDataException e)
    {
      VertexServerGeoObject.handleDuplicateDataException(type, e);

      throw e;
    }
  }

  @Transaction
  public ServerGeoObjectIF apply(GeoObjectOverTime goTime, boolean isNew, boolean isImport)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(goTime.getType());
    ServerGeoObjectStrategyIF strategy = this.getStrategy(type);

    if (isNew)
    {
      permissionService.enforceCanCreate(type.getOrganization().getCode(), type);
    }
    else
    {
      permissionService.enforceCanWrite(type.getOrganization().getCode(), type);
    }

    ServerGeoObjectIF goServer = strategy.constructFromGeoObjectOverTime(goTime, isNew);

    if (!isNew)
    {
      goServer.lock();
    }

    goServer.populate(goTime);

    try
    {
      goServer.apply(isImport);

      // Return the refreshed copy of the geoObject
      return this.build(type, goServer.getRunwayId());
    }
    catch (DuplicateDataException e)
    {
      VertexServerGeoObject.handleDuplicateDataException(type, e);

      throw e;
    }
  }

  @Transaction
  public ServerGeoObjectIF split(GeoObjectSplitView view)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(view.getTypeCode());
    ServerGeoObjectStrategyIF strategy = this.getStrategy(type);

    final ServerGeoObjectIF source = strategy.getGeoObjectByCode(view.getSourceCode());
    source.setDate(view.getDate());

    ServerGeoObjectIF target = strategy.newInstance();
    target.setDate(view.getDate());
    target.populate(source.toGeoObject(view.getDate()), view.getDate(), view.getDate());
    target.setCode(view.getTargetCode());
    target.setDisplayLabel(view.getLabel());
    target.apply(false);

    final ServerParentTreeNode sNode = source.getParentGeoObjects(null, false, view.getDate());

    final List<ServerParentTreeNode> sParents = sNode.getParents();

    for (ServerParentTreeNode sParent : sParents)
    {
      final ServerGeoObjectIF parent = sParent.getGeoObject();
      final ServerHierarchyType hierarchyType = sParent.getHierarchyType();

      target.addParent(parent, hierarchyType, view.getDate(), null);
    }

    return target;
  }

  public ServerGeoObjectIF newInstance(ServerGeoObjectType type)
  {
    ServerGeoObjectStrategyIF strategy = this.getStrategy(type);

    return strategy.newInstance();
  }

  public ServerGeoObjectIF getGeoObject(GeoObject go)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(go.getType());

    ServerGeoObjectStrategyIF strategy = this.getStrategy(type);

    return strategy.constructFromGeoObject(go, false);
  }

  public ServerGeoObjectIF getGeoObject(GeoObjectOverTime timeGO)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(timeGO.getType());

    ServerGeoObjectStrategyIF strategy = this.getStrategy(type);

    return strategy.constructFromGeoObjectOverTime(timeGO, false);
  }

  public ServerGeoObjectIF getGeoObjectByCode(String code, String typeCode)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(typeCode);

    this.permissionService.enforceCanRead(type.getOrganization().getCode(), type);

    ServerGeoObjectStrategyIF strategy = this.getStrategy(type);

    return strategy.getGeoObjectByCode(code);
  }

  public ServerGeoObjectIF getGeoObjectByCode(String code, ServerGeoObjectType type)
  {
    this.permissionService.enforceCanRead(type.getOrganization().getCode(), type);

    ServerGeoObjectStrategyIF strategy = this.getStrategy(type);

    return strategy.getGeoObjectByCode(code);
  }

  public ServerGeoObjectIF getGeoObject(String uid, String typeCode)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(typeCode);

    this.permissionService.enforceCanRead(type.getOrganization().getCode(), type);

    ServerGeoObjectStrategyIF strategy = this.getStrategy(type);
    ServerGeoObjectIF object = strategy.getGeoObjectByUid(uid);

    if (object == null)
    {
      InvalidRegistryIdException ex = new InvalidRegistryIdException();
      ex.setRegistryId(uid);
      throw ex;
    }

    return object;
  }

  public ServerGeoObjectStrategyIF getStrategy(ServerGeoObjectType type)
  {
    return new VertexGeoObjectStrategy(type);
  }

  public ServerGeoObjectIF build(ServerGeoObjectType type, String runwayId)
  {
    ServerGeoObjectStrategyIF strategy = this.getStrategy(type);
    VertexObject vertex = VertexObject.get(type.getMdVertex(), runwayId);

    return strategy.constructFromDB(vertex);
  }

  public ServerGeoObjectIF build(ServerGeoObjectType type, Object dbObject)
  {
    ServerGeoObjectStrategyIF strategy = this.getStrategy(type);
    return strategy.constructFromDB(dbObject);
  }

  public ServerGeoObjectQuery createQuery(ServerGeoObjectType type, Date date)
  {
    return new VertexGeoObjectQuery(type, date);
  }

  public boolean hasData(ServerHierarchyType serverHierarchyType, ServerGeoObjectType childType)
  {
    return VertexServerGeoObject.hasData(serverHierarchyType, childType);
  }

  public void removeAllEdges(ServerHierarchyType hierarchyType, ServerGeoObjectType childType)
  {
    VertexServerGeoObject.removeAllEdges(hierarchyType, childType);
  }

  @Request(RequestType.SESSION)
  public JsonObject createGeoObject(String sessionId, String ptn, String sTimeGo, String masterListId, String notes)
  {
    return this.createGeoObjectInTrans(ptn, sTimeGo, masterListId, notes);
  }

  @Transaction
  public JsonObject createGeoObjectInTrans(String sPtn, String sTimeGo, String masterListId, String notes)
  {
    GeoObjectOverTime timeGO = GeoObjectOverTime.fromJSON(ServiceFactory.getAdapter(), sTimeGo);

    ServerGeoObjectType serverGOT = ServerGeoObjectType.get(timeGO.getType());

    RolePermissionService perms = ServiceFactory.getRolePermissionService();

    final String orgCode = serverGOT.getOrganization().getCode();

    if (perms.isSRA() || perms.isRA(orgCode) || perms.isRM(orgCode, serverGOT))
    {
      ServerGeoObjectService service = new ServerGeoObjectService();

      ServerGeoObjectIF serverGO = service.apply(timeGO, true, false);
      final ServerGeoObjectType type = serverGO.getType();

      if (sPtn != null)
      {
        ServerParentTreeNodeOverTime ptnOt = ServerParentTreeNodeOverTime.fromJSON(type, sPtn);

        serverGO.setParents(ptnOt);
      }

      // Update the master list record
      if (masterListId != null)
      {
        ListTypeVersion.get(masterListId).publishRecord(serverGO);
      }

      JsonObject resp = new JsonObject();

      resp.addProperty("isChangeRequest", false);
      resp.add("geoObject", serverGO.toGeoObjectOverTime().toJSON(ServiceFactory.getRegistryService().serializer(Session.getCurrentSession().getOid())));

      return resp;
    }
    else if (ServiceFactory.getRolePermissionService().isRC(orgCode, serverGOT))
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

  @Request(RequestType.SESSION)
  public JsonObject updateGeoObject(String sessionId, String geoObjectCode, String geoObjectTypeCode, String actions, String masterListId, String notes)
  {
    return this.updateGeoObjectInTrans(geoObjectCode, geoObjectTypeCode, actions, masterListId, notes);
  }

  @Transaction
  public JsonObject updateGeoObjectInTrans(String geoObjectCode, String geoObjectTypeCode, String actions, String masterListId, String notes)
  {
    final RolePermissionService perms = ServiceFactory.getRolePermissionService();
    final ServerGeoObjectType type = ServerGeoObjectType.get(geoObjectTypeCode);
    final String orgCode = type.getOrganization().getCode();
    final VertexServerGeoObject go = (VertexServerGeoObject) new ServerGeoObjectService().getGeoObjectByCode(geoObjectCode, geoObjectTypeCode);

    final JsonArray jaActions = JsonParser.parseString(actions).getAsJsonArray();

    if (perms.isSRA() || perms.isRA(orgCode) || perms.isRM(orgCode, type))
    {
      this.executeActions(type, go, jaActions);
      
      if (masterListId != null)
      {
        ListTypeVersion.get(masterListId).updateRecord(go);
      }

      JsonObject resp = new JsonObject();

      resp.addProperty("isChangeRequest", false);
      resp.add("geoObject", go.toGeoObjectOverTime().toJSON(ServiceFactory.getRegistryService().serializer(Session.getCurrentSession().getOid())));

      return resp;
    }
    else if (ServiceFactory.getRolePermissionService().isRC(orgCode, type))
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

    go.apply(false);
  }

  @Request(RequestType.SESSION)
  public JsonObject doesGeoObjectExistAtRange(String sessionId, Date startDate, Date endDate, String typeCode, String code)
  {
    VertexServerGeoObject vsgo = (VertexServerGeoObject) new ServerGeoObjectService().getGeoObjectByCode(code, typeCode);
    
    JsonObject jo = new JsonObject();
    
    jo.addProperty("exists", vsgo.existsAtRange(startDate, endDate));
    jo.addProperty("invalid", vsgo.getInvalid());
    
    return jo;
  }
}
