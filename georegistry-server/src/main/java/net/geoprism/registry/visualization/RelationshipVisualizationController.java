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
package net.geoprism.registry.visualization;

import com.google.gson.JsonElement;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;

import net.geoprism.registry.GeoRegistryUtil;

@Controller(url = "relationship-visualization")
public class RelationshipVisualizationController
{
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "tree")
  public ResponseIF tree(ClientRequestIF request, @RequestParamter(name = "relationshipType") String relationshipType, @RequestParamter(name = "graphTypeCode") String graphTypeCode, @RequestParamter(name = "sourceVertex") String sourceVertex, @RequestParamter(name = "date") String sDate, @RequestParamter(name = "boundsWKT") String boundsWKT)
  {
    JsonElement json = new RelationshipVisualizationService().tree(request.getSessionId(), GeoRegistryUtil.parseDate(sDate), relationshipType, graphTypeCode, sourceVertex, boundsWKT);

    return new RestBodyResponse(json);
  }
  
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "treeAsGeoJson")
  public ResponseIF treeAsGeoJson(ClientRequestIF request, @RequestParamter(name = "relationshipType") String relationshipType, @RequestParamter(name = "graphTypeCode") String graphTypeCode, @RequestParamter(name = "geoObjectCode") String geoObjectCode, @RequestParamter(name = "geoObjectTypeCode") String geoObjectTypeCode, @RequestParamter(name = "date") String sDate, @RequestParamter(name = "boundsWKT") String boundsWKT)
  {
    JsonElement json = new RelationshipVisualizationService().treeAsGeoJson(request.getSessionId(), GeoRegistryUtil.parseDate(sDate), relationshipType, graphTypeCode, geoObjectCode, geoObjectTypeCode, boundsWKT);

    return new RestBodyResponse(json);
  }
  
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "relationships")
  public ResponseIF relationships(ClientRequestIF request, @RequestParamter(name = "objectType") String objectType, @RequestParamter(name = "typeCode") String typeCode)
  {
    JsonElement json = new RelationshipVisualizationService().getRelationshipTypes(request.getSessionId(), VertexView.ObjectType.valueOf(objectType), typeCode);

    return new RestBodyResponse(json);
  }
}
