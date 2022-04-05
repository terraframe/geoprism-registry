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
package net.geoprism.registry.service;

import java.util.Date;

import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.geoobject.ServerGeoObjectService;
import net.geoprism.registry.model.GraphType;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGraphNode;

public abstract class GraphService
{
  public abstract GraphType getGraphType(String code);

  @Request(RequestType.SESSION)
  public JsonObject getChildren(String sessionId, String parentCode, String parentGeoObjectTypeCode, String graphTypeCode, Boolean recursive, Date date)
  {
    ServerGeoObjectService service = ServiceFactory.getGeoObjectService();

    ServerGeoObjectIF parent = service.getGeoObjectByCode(parentCode, parentGeoObjectTypeCode, true);
    GraphType graphType = this.getGraphType(graphTypeCode);

    // ServiceFactory.getGeoObjectRelationshipPermissionService().enforceCanAddChild(ht.getOrganization().getCode(),
    // parent.getType(), child.getType());

    ServerGraphNode node = parent.getGraphChildren(graphType, recursive, date);
    return node.toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject getParents(String sessionId, String childCode, String childGeoObjectTypeCode, String graphTypeCode, Boolean recursive, Date date)
  {
    ServerGeoObjectService service = ServiceFactory.getGeoObjectService();

    ServerGeoObjectIF child = service.getGeoObjectByCode(childCode, childGeoObjectTypeCode, true);
    GraphType graphType = this.getGraphType(graphTypeCode);

    // ServiceFactory.getGeoObjectRelationshipPermissionService().enforceCanAddChild(ht.getOrganization().getCode(),
    // parent.getType(), child.getType());

    ServerGraphNode node = child.getGraphParents(graphType, recursive, date);
    return node.toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject addChild(String sessionId, String parentCode, String parentGeoObjectTypeCode, String childCode, String childGeoObjectTypeCode, String graphTypeCode, Date startDate, Date endDate)
  {
    ServerGeoObjectService service = ServiceFactory.getGeoObjectService();

    ServerGeoObjectIF parent = service.getGeoObjectByCode(parentCode, parentGeoObjectTypeCode, true);
    ServerGeoObjectIF child = service.getGeoObjectByCode(childCode, childGeoObjectTypeCode, true);
    GraphType graphType = this.getGraphType(graphTypeCode);

    // ServiceFactory.getGeoObjectRelationshipPermissionService().enforceCanAddChild(ht.getOrganization().getCode(),
    // parent.getType(), child.getType());

    ServerGraphNode node = parent.addGraphChild(child, graphType, startDate, endDate);
    return node.toJSON();
  }

  @Request(RequestType.SESSION)
  public void removeChild(String sessionId, String parentCode, String parentGeoObjectTypeCode, String childCode, String childGeoObjectTypeCode, String graphTypeCode, Date startDate, Date endDate)
  {
    ServerGeoObjectService service = ServiceFactory.getGeoObjectService();

    ServerGeoObjectIF parent = service.getGeoObjectByCode(parentCode, parentGeoObjectTypeCode, true);
    ServerGeoObjectIF child = service.getGeoObjectByCode(childCode, childGeoObjectTypeCode, true);
    GraphType graphType = this.getGraphType(graphTypeCode);

    // ServiceFactory.getGeoObjectRelationshipPermissionService().enforceCanAddChild(ht.getOrganization().getCode(),
    // parent.getType(), child.getType());

    parent.removeGraphChild(child, graphType, startDate, endDate);
  }

}
