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

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

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
  private ServerGeoObjectService service = new ServerGeoObjectService();

  @Request(RequestType.SESSION)
  public List<GeoObject> search(String sessionId, String text, Date date)
  {
    final GeoObjectPermissionService pService = new GeoObjectPermissionService();

    List<ServerGeoObjectIF> results = new SearchService().search(text, date, 20L);

    return results.stream().collect(() -> new LinkedList<GeoObject>(), (list, element) -> {
      ServerGeoObjectType type = element.getType();

      GeoObject geoObject = element.toGeoObject();
      geoObject.setWritable(pService.canCreateCR(type.getOrganization().getCode(), type));

      list.add(geoObject);
    }, (listA, listB) -> {
    });
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

    List<ServerGeoObjectType> nodes = hierarchy.getDirectRootNodes();

    if (nodes.size() > 0)
    {
      /*
       * If a typeCode is given and it is an option based on the hierarchy than
       * use that type otherwise use the first type code
       */
      ServerGeoObjectType type = nodes.get(0);

      if (typeCode != null && typeCode.length() > 0)
      {
        for (ServerGeoObjectType node : nodes)
        {
          if (node.getCode().equals(typeCode))
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
    information.setChildTypes(nodes);

    return information;
  }

//  @Request(RequestType.SESSION)
//  public LocationInformation getLocationInformation(String sessionId, String code, String typeCode, Date date, String childTypeCode, String hierarchyCode)
//  {
//    LocationInformation information = new LocationInformation();
//
//    ServerGeoObjectIF go = this.service.getGeoObjectByCode(code, typeCode);
//    go.setDate(date);
//
//    ServerGeoObjectType type = go.getType();
//    List<ServerHierarchyType> hierarchies = type.getHierarchies();
//
//    ServerHierarchyType hierarchy = null;
//
//    if (hierarchyCode == null || hierarchyCode.length() == 0)
//    {
//      hierarchy = hierarchies.get(0);
//    }
//    else
//    {
//      hierarchy = ServerHierarchyType.get(hierarchyCode);
//    }
//
//    List<ServerGeoObjectType> childTypes = type.getChildren(hierarchy);
//    ServerGeoObjectType childType = null;
//
//    if (childTypes.size() > 0)
//    {
//      /*
//       * If a typeCode is given and it is an option based on the hierarchy than
//       * use that type otherwise use the first type code
//       */
//      childType = childTypes.get(0);
//
//      if (childTypeCode != null && childTypeCode.length() > 0)
//      {
//        for (ServerGeoObjectType child : childTypes)
//        {
//          if (child.getCode().equals(childTypeCode))
//          {
//            childType = child;
//          }
//        }
//      }
//    }
//
//    if (childType != null)
//    {
//      information.setChildType(childType.getType());
//
//      List<VertexServerGeoObject> children = this.getGeoObjects(go.getCode(), childType.getCode(), hierarchy.getCode(), date);
//
//      for (VertexServerGeoObject child : children)
//      {
//        information.addChild(child.toGeoObject());
//      }
//    }
//
//    information.setChildTypes(childTypes);
//    information.setHierarchies(hierarchies);
//    information.setHierarchy(hierarchy.getCode());
//    information.setEntity(go.toGeoObject());
//
//    return information;
//  }

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

    // SELECT expand(outE('hierarchy10')[DATE('2021-01-01 00:00:00') BETWEEN
    // startDate AND endDate].inV('district0')
    // from #309:0
    // order by code
    // StringBuilder statement = new StringBuilder();
    // statement.append("SELECT EXPAND(in) FROM " + mdEdge.getDBClassName());
    // statement.append(" WHERE in.@class = '" + mdVertex.getDBClassName() +
    // "'");
    // statement.append(" AND out.code = :parent");
    // statement.append(" AND :date BETWEEN startDate AND endDate");
    // statement.append(" ORDER BY in.code");

    // SELECT
    // FROM health_facility0
    // WHERE in('around0')[0].code = '2202'
    // AND DATE('2021-01-01 00:00:00') BETWEEN inE('around0')[0].startDate AND
    // inE('around0')[0].endDate
    // ORDER BY code
    //

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());
    statement.append(" WHERE in('" + mdEdge.getDBClassName() + "')[0].code = :parent");
    statement.append(" AND :date BETWEEN inE('" + mdEdge.getDBClassName() + "')[0].startDate");
    statement.append(" AND inE('" + mdEdge.getDBClassName() + "')[0].endDate");
    statement.append(" ORDER BY code");

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
