/* 
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.controller;

import java.util.Date;

import com.google.gson.JsonObject;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.service.UndirectedGraphService;

@Controller(url = "undirected")
public class UndirectedGraphController
{
  private UndirectedGraphService service;

  public UndirectedGraphController()
  {
    super();
    
    this.service = new UndirectedGraphService();
  }
  
  @Endpoint(url = "get-related-geo-objects", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getChildren(ClientRequestIF request, 
      @RequestParamter(name="sourceCode", required = true) String sourceCode,
      @RequestParamter(name="sourceTypeCode", required = true) String sourceTypeCode,
      @RequestParamter(name="undirectedRelationshipCode", required = true) String undirectedRelationshipCode,
      @RequestParamter(name="recursive", required = true) Boolean recursive,
      @RequestParamter(name="date", required = true) String sDate)
  {
    Date date = GeoRegistryUtil.parseDate(sDate, true);
    
    JsonObject response = this.service.getChildren(request.getSessionId(), sourceCode, sourceTypeCode, undirectedRelationshipCode, recursive, date);
    
    return new RestBodyResponse(response);
  }

  @Endpoint(url = "add-target", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF addChild(ClientRequestIF request, 
      @RequestParamter(name="sourceCode", required = true) String sourceCode,
      @RequestParamter(name="sourceTypeCode", required = true) String sourceTypeCode,
      @RequestParamter(name="targetCode", required = true) String targetCode,
      @RequestParamter(name="targetTypeCode", required = true) String targetTypeCode,
      @RequestParamter(name="undirectedRelationshipCode", required = true) String undirectedRelationshipCode,
      @RequestParamter(name="startDate", required = true) String startDateStr, 
      @RequestParamter(name="endDate", required = true) String endDateStr)
  {
    Date startDate = GeoRegistryUtil.parseDate(startDateStr, true);
    Date endDate = GeoRegistryUtil.parseDate(endDateStr, true);
    
    JsonObject response = this.service.addChild(request.getSessionId(), sourceCode, sourceTypeCode, targetCode, targetTypeCode, undirectedRelationshipCode, startDate, endDate);

    return new RestBodyResponse(response);
  }

  @Endpoint(url = "remove-target", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF removeChild(ClientRequestIF request, 
      @RequestParamter(name="sourceCode", required = true) String sourceCode,
      @RequestParamter(name="sourceTypeCode", required = true) String sourceTypeCode,
      @RequestParamter(name="targetCode", required = true) String targetCode,
      @RequestParamter(name="targetTypeCode", required = true) String targetTypeCode,
      @RequestParamter(name="undirectedRelationshipCode", required = true) String undirectedRelationshipCode,
      @RequestParamter(name="startDate", required = true) String startDateStr, 
      @RequestParamter(name="endDate", required = true) String endDateStr)
  {
    Date startDate = GeoRegistryUtil.parseDate(startDateStr, true);
    Date endDate = GeoRegistryUtil.parseDate(endDateStr, true);
    
    this.service.removeChild(request.getSessionId(), sourceCode, sourceTypeCode, targetCode, targetTypeCode, undirectedRelationshipCode, startDate, endDate);

    return new RestResponse();
  }
}
