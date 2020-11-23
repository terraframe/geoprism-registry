/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.service;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.metadata.CustomSerializer;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.commongeoregistry.adapter.metadata.HierarchyType.HierarchyNode;
import org.json.JSONObject;

import com.runwaysdk.LocalizationFacade;
import com.runwaysdk.business.ValueObjectDTO;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.system.gis.geo.GeoEntityDTO;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdTermRelationship;

import net.geoprism.ontology.GeoEntityUtil;
import net.geoprism.registry.controller.GeoObjectEditorControllerNoOverTime;
import net.geoprism.registry.geoobject.ServerGeoObjectService;
import net.geoprism.registry.hierarchy.HierarchyService;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.permission.GeoObjectPermissionService;
import net.geoprism.registry.permission.PermissionContext;
import net.geoprism.registry.view.LocationInformation;

public class LocationService
{
  private ServerGeoObjectService service = new ServerGeoObjectService(new GeoObjectPermissionService());

  @Request(RequestType.SESSION)
  public JSONObject addParentInfoToExistingGO(String sessionId, GeoObject child)
  {
    ParentTreeNode ptnChild = RegistryService.getInstance().getParentGeoObjects(sessionId, child.getUid(), child.getType().getCode(), null, false, null);

    return new JSONObject(ptnChild.toJSON().toString());
  }

  @Request(RequestType.SESSION)
  public String editNewGeoObjectInReq(String sessionId, String universalId, String sjsParent, String mdRelationshipId)
  {
    Universal uni = Universal.get(universalId);

    String gotCode = uni.getKey();

    GeoObject newGo = ServiceFactory.getAdapter().newGeoObjectInstance(gotCode);

    List<Locale> locales = LocalizationFacade.getInstalledLocales();
    for (Locale locale : locales)
    {
      newGo.setDisplayLabel(locale.toString(), "");
    }
    newGo.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "");

    JSONObject joResp = new JSONObject();

    // Add the GeoObject to the response
    joResp.put("newGeoObject", serializeGo(sessionId, newGo));
    joResp.put("geoObjectType", new JSONObject(newGo.getType().toJSON().toString()));
    joResp.put("parentTreeNode", addParentInfoToNewGO(sessionId, mdRelationshipId, sjsParent, newGo));

    return joResp.toString();
  }

  @Request(RequestType.SESSION)
  public JSONObject addParentInfoToNewGO(String sessionId, String mdRelationshipId, String sjsParent, GeoObject newGo)
  {
    ServerHierarchyType currentHt = ServerHierarchyType.get(MdTermRelationship.get(mdRelationshipId));
    JSONObject jsParent = new JSONObject(sjsParent);
    String oid = jsParent.getString("oid");

    ServerGeoObjectIF goParent = service.getGeoObjectByEntityId(oid);

    ParentTreeNode ptnChild = new ParentTreeNode(newGo, null);
    ptnChild.addParent(new ParentTreeNode(goParent.toGeoObject(), currentHt.getType()));

    return new JSONObject(ptnChild.toJSON().toString());
  }

  @Request(RequestType.SESSION)
  public GeoObject getGeoObject(String sessionId, String id)
  {
    ServerGeoObjectIF object = service.getGeoObjectByEntityId(id);

    return object.toGeoObject();
  }

  @Request(RequestType.SESSION)
  public ResponseIF applyInRequest(String sessionId, Boolean isNew, String sjsGO, String parentOid, String existingLayers, String sjsPTN)
  {
    return applyInTrans(sessionId, isNew, sjsGO, parentOid, existingLayers, sjsPTN);
  }

  @Transaction
  private ResponseIF applyInTrans(String sessionId, Boolean isNew, String sjsGO, String parentOid, String existingLayers, String sjsPTN)
  {
    CustomSerializer serializer = ServiceFactory.getRegistryService().serializer(sessionId);

    GeoObject go = GeoObject.fromJSON(ServiceFactory.getAdapter(), sjsGO);

    // TODO
    // if (entityDTO.getGeoId() == null || entityDTO.getGeoId().length() == 0)
    // {
    // entityDTO.setGeoId(IDGenerator.nextID());
    // }

    GeoEntityUtil.refreshViews(existingLayers);

    if (isNew)
    {
      go = RegistryService.getInstance().createGeoObject(sessionId, go.toJSON(serializer).toString());

      // GeoObject goParent = getGeoObject(request.getSessionId(), parentOid);
      // RegistryService.getInstance().addChild(request.getSessionId(),
      // goParent.getUid(), goParent.getType().getCode(), goChild.getUid(),
      // goChild.getType().getCode(), "LocatedIn");

      ParentTreeNode ptn = ParentTreeNode.fromJSON(sjsPTN, ServiceFactory.getAdapter());
      this.applyPtn(sessionId, ptn);
    }
    else
    {
      go = new GeoObjectEditorControllerNoOverTime().applyInReq(sessionId, sjsPTN, go.toJSON(serializer).toString(), false, null);
    }

    JSONObject object = new JSONObject();
    object.put(GeoEntityDTO.TYPE, ValueObjectDTO.CLASS);
    object.put(GeoEntityDTO.OID, go.getUid());
    object.put(GeoEntityDTO.DISPLAYLABEL, go.getDisplayLabel().getValue());
    object.put(GeoEntityDTO.GEOID, go.getCode());
    object.put(GeoEntityDTO.UNIVERSAL, go.getType().getLabel().getValue());

    object.put("geoObject", serializeGo(sessionId, go));

    return new RestBodyResponse(object);
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

  public JSONObject serializeGo(String sessionId, GeoObject go)
  {
    CustomSerializer serializer = ServiceFactory.getRegistryService().serializer(sessionId);

    JSONObject joGo = new JSONObject(go.toJSON(serializer).toString());
    joGo.remove("geometry");
    return joGo;
  }

  @Request(RequestType.SESSION)
  public LocationInformation getLocationInformation(String sessionId, Date date, String typeCode, String hierarchyCode)
  {
    LocationInformation information = new LocationInformation();

    HierarchyService hService = ServiceFactory.getHierarchyService();
    HierarchyType[] hierarchies = hService.getHierarchyTypes(sessionId, null, PermissionContext.READ);

    ServerHierarchyType hierarchy = null;

    if (hierarchyCode == null || hierarchyCode.length() == 0)
    {
      hierarchy = ServerHierarchyType.get(hierarchies[0]);
    }
    else
    {
      hierarchy = ServerHierarchyType.get(hierarchyCode);
    }

    List<HierarchyNode> nodes = hierarchy.getType().getRootGeoObjectTypes();

    if (nodes.size() > 0)
    {
      /*
       * If a typeCode is given and it is an option based on the hierarchy than
       * use that type otherwise use the first type code
       */

      HierarchyNode first = nodes.get(0);

      ServerGeoObjectType type = ServerGeoObjectType.get(first.getGeoObjectType());

      if (typeCode != null && typeCode.length() > 0)
      {
        for (HierarchyNode node : nodes)
        {
          if (node.getGeoObjectType().getCode().equals(typeCode))
          {
            type = ServerGeoObjectType.get(typeCode);
          }
        }
      }

      if (type != null)
      {
        ServiceFactory.getGeoObjectPermissionService().enforceCanRead(type.getOrganization().getCode(), type);

        information.setChildType(type.getType());

        List<VertexServerGeoObject> children = this.getGeoObjects(type.getCode(), date);

        for (VertexServerGeoObject child : children)
        {
          information.addChild(child.toGeoObject());
        }

      }
    }

    information.setHierarchies(hierarchies);
    information.setHierarchy(hierarchy.getCode());
    information.setChildTypesFromNodes(nodes);

    return information;
  }

  @Request(RequestType.SESSION)
  public LocationInformation getLocationInformation(String sessionId, String code, String typeCode, Date date, String childTypeCode, String hierarchyCode)
  {
    LocationInformation information = new LocationInformation();

    ServerGeoObjectIF go = this.service.getGeoObjectByCode(code, typeCode);
    go.setDate(date);

    ServerGeoObjectType type = go.getType();
    List<ServerHierarchyType> hierarchies = type.getHierarchies();

    ServerHierarchyType hierarchy = null;

    if (hierarchyCode == null || hierarchyCode.length() == 0)
    {
      hierarchy = hierarchies.get(0);
    }
    else
    {
      hierarchy = ServerHierarchyType.get(hierarchyCode);
    }

    List<ServerGeoObjectType> childTypes = type.getChildren(hierarchy);
    ServerGeoObjectType childType = null;

    if (childTypes.size() > 0)
    {
      /*
       * If a typeCode is given and it is an option based on the hierarchy than
       * use that type otherwise use the first type code
       */
      childType = childTypes.get(0);

      if (childTypeCode != null && childTypeCode.length() > 0)
      {
        for (ServerGeoObjectType child : childTypes)
        {
          if (child.getCode().equals(childTypeCode))
          {
            childType = child;
          }
        }
      }
    }

    if (childType != null)
    {
      information.setChildType(childType.getType());

      List<VertexServerGeoObject> children = this.getGeoObjects(go.getCode(), childType.getCode(), hierarchy.getCode(), date);

      for (VertexServerGeoObject child : children)
      {
        information.addChild(child.toGeoObject());
      }
    }

    information.setChildTypes(childTypes);
    information.setHierarchies(hierarchies);
    information.setHierarchy(hierarchy.getCode());
    information.setEntity(go.toGeoObject());

    return information;
  }

  // @Request(RequestType.SESSION)
  // public JsonObject getChildrenGeoJson(String sessionId, String typeCode,
  // String parentId, String hierarchyCode)
  // {
  // List<VertexServerGeoObject> children = parentId != null ?
  // this.getGeoObjects(typeCode, parentId, hierarchyCode) :
  // this.getGeoObjects(typeCode);
  //
  // return children;
  // }

  private List<VertexServerGeoObject> getGeoObjects(String typeCode, Date date)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(typeCode);

    MdVertexDAOIF mdVertex = type.getMdVertex();

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());
    statement.append(" ORDER BY code");

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());

    List<VertexObject> vObjects = query.getResults();

    List<VertexServerGeoObject> response = new LinkedList<VertexServerGeoObject>();

    for (VertexObject vObject : vObjects)
    {
      VertexServerGeoObject vSGO = new VertexServerGeoObject(type, vObject);
      vSGO.setDate(date);

      response.add(vSGO);
    }

    return response;
  }

  private List<VertexServerGeoObject> getGeoObjects(String parentCode, String typeCode, String hierarchyCode, Date date)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(typeCode);
    ServerHierarchyType hierachy = ServerHierarchyType.get(hierarchyCode);

    MdVertexDAOIF mdVertex = type.getMdVertex();
    MdEdgeDAOIF mdEdge = hierachy.getMdEdge();

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT EXPAND(in) FROM " + mdEdge.getDBClassName());
    statement.append(" WHERE in.@class = '" + mdVertex.getDBClassName() + "'");
    statement.append(" AND out.code = :parent");
    statement.append(" AND :date BETWEEN startDate AND endDate");
    statement.append(" ORDER BY in.code");

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());
    query.setParameter("parent", parentCode);
    query.setParameter("date", date);

    List<VertexObject> vObjects = query.getResults();

    List<VertexServerGeoObject> response = new LinkedList<VertexServerGeoObject>();

    for (VertexObject vObject : vObjects)
    {
      VertexServerGeoObject vSGO = new VertexServerGeoObject(type, vObject);
      vSGO.setDate(date);

      response.add(vSGO);
    }

    return response;
  }
}
