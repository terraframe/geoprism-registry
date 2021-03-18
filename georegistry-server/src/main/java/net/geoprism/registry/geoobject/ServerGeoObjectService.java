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

import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.DuplicateDataException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.InvalidRegistryIdException;
import net.geoprism.registry.MasterListVersion;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.action.AllGovernanceStatus;
import net.geoprism.registry.action.ChangeRequest;
import net.geoprism.registry.action.geoobject.CreateGeoObjectAction;
import net.geoprism.registry.action.geoobject.SetParentAction;
import net.geoprism.registry.action.geoobject.UpdateGeoObjectAction;
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
import net.geoprism.registry.query.ServerGeoObjectQuery;
import net.geoprism.registry.query.graph.VertexGeoObjectQuery;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.view.GeoObjectSplitView;
import net.geoprism.registry.view.ServerParentTreeNodeOverTime;

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
  public ServerGeoObjectIF apply(GeoObject object, boolean isNew, boolean isImport)
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

    if (!isNew)
    {
      geoObject.lock();
    }

    geoObject.populate(object);

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
    target.populate(source.toGeoObject());
    target.setCode(view.getTargetCode());
    target.setDisplayLabel(view.getLabel());
    target.apply(false);

    final ServerParentTreeNode sNode = source.getParentGeoObjects(null, false);

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
  public GeoObjectOverTime masterListEdit(String sessionId, String ptn, String sGO, Boolean isNew, String masterListId, String notes)
  {
    return this.masterListEditTrans(sessionId, ptn, sGO, isNew, masterListId, notes);
  }
  
  @Transaction
  public GeoObjectOverTime masterListEditTrans(String sessionId, String sPtn, String sGO, Boolean isNew, String masterListId, String notes)
  {
    GeoObjectOverTime timeGO = GeoObjectOverTime.fromJSON(ServiceFactory.getAdapter(), sGO);
    
    ServerGeoObjectType serverGOT = ServerGeoObjectType.get(timeGO.getType());

    if (ServiceFactory.getRolePermissionService().isRC(serverGOT))
    {
      Instant base = Instant.now();
      int sequence = 0;

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

      if (sPtn != null && (isNew || this.hasChanged(timeGO, sPtn)))
      {
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
    }
    else
    {
      ServerGeoObjectService service = new ServerGeoObjectService();

      ServerGeoObjectIF serverGO = service.apply(timeGO, isNew, false);
      final ServerGeoObjectType type = serverGO.getType();

      if (sPtn != null)
      {
        ServerParentTreeNodeOverTime ptnOt = ServerParentTreeNodeOverTime.fromJSON(type, sPtn);

        serverGO.setParents(ptnOt);
      }

      // Update the master list record
      if (masterListId != null)
      {
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
  
  private boolean hasChanged(GeoObjectOverTime timeGO, String sPtn)
  {
    ServerGeoObjectService service = new ServerGeoObjectService();
    ServerGeoObjectIF sGO = service.getGeoObjectByCode(timeGO.getCode(), timeGO.getType().getCode());

    ServerParentTreeNodeOverTime sPto = ServerParentTreeNodeOverTime.fromJSON(sGO.getType(), sPtn);
    ServerParentTreeNodeOverTime ePto = sGO.getParentsOverTime(null, false);

    return !ePto.isSame(sPto, sGO);
  }
}
