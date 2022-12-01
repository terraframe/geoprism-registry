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
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.BusinessEdgeType;
import net.geoprism.registry.BusinessType;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.graph.VertexServerGeoObject;

@Component
public class BusinessObjectService
{
  @Request(RequestType.SESSION)
  public JsonObject get(String sessionId, String businessTypeCode, String code)
  {
    BusinessType type = BusinessType.getByCode(businessTypeCode);
    BusinessObject object = BusinessObject.getByCode(type, code);

    return object.toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject getTypeAndObject(String sessionId, String businessTypeCode, String code)
  {
    BusinessType type = BusinessType.getByCode(businessTypeCode);
    BusinessObject object = BusinessObject.getByCode(type, code);
    
    JsonObject response = new JsonObject();
    response.add("type", type.toJSON(true, false));
    response.add("object", object.toJSON());
    
    return response;
  }
  
  @Request(RequestType.SESSION)
  public JsonArray getParents(String sessionId, String businessTypeCode, String code, String businessEdgeTypeCode)
  {
    BusinessType type = BusinessType.getByCode(businessTypeCode);
    BusinessEdgeType relationshipType = BusinessEdgeType.getByCode(businessEdgeTypeCode);

    BusinessObject object = BusinessObject.getByCode(type, code);

    List<BusinessObject> parents = object.getParents(relationshipType);
    return parents.stream().map(parent -> parent.toJSON()).collect(() -> new JsonArray(), (array, element) -> array.add(element), (listA, listB) -> {
    });
  }

  @Request(RequestType.SESSION)
  public JsonArray getChildren(String sessionId, String businessTypeCode, String code, String businessEdgeTypeCode)
  {
    BusinessType type = BusinessType.getByCode(businessTypeCode);
    BusinessEdgeType relationshipType = BusinessEdgeType.getByCode(businessEdgeTypeCode);

    BusinessObject object = BusinessObject.getByCode(type, code);

    List<BusinessObject> children = object.getChildren(relationshipType);
    return children.stream().map(child -> child.toJSON()).collect(() -> new JsonArray(), (array, element) -> array.add(element), (listA, listB) -> {
    });
  }

  @Request(RequestType.SESSION)
  public JsonArray getGeoObjects(String sessionId, String businessTypeCode, String code, String dateStr)
  {
    BusinessType type = BusinessType.getByCode(businessTypeCode);
    BusinessObject object = BusinessObject.getByCode(type, code);
    Date date = GeoRegistryUtil.parseDate(dateStr, true);

    List<VertexServerGeoObject> geoObjects = object.getGeoObjects();
    return geoObjects.stream().map(child -> {
      GeoObject geoObject = child.toGeoObject(date);
      return geoObject.toJSON();
    }).collect(() -> new JsonArray(), (array, element) -> array.add(element), (listA, listB) -> {
    });
  }

}
