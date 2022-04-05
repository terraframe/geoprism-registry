/**
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
import net.geoprism.registry.service.DirectedAcyclicGraphService;

@Controller(url = "dag")
public class DirectedAcyclicGraphController
{
  private DirectedAcyclicGraphService service;

  public DirectedAcyclicGraphController()
  {
    super();
    
    this.service = new DirectedAcyclicGraphService();
  }
  
  @Endpoint(url = "get-children", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getChildren(ClientRequestIF request, 
      @RequestParamter(name="parentCode", required = true) String parentCode,
      @RequestParamter(name="parentTypeCode", required = true) String parentTypeCode,
      @RequestParamter(name="directedGraphCode", required = true) String directedGraphCode,
      @RequestParamter(name="recursive", required = true) Boolean recursive,
      @RequestParamter(name="date", required = true) String sDate)
  {
    Date date = GeoRegistryUtil.parseDate(sDate, true);
    
    JsonObject response = this.service.getChildren(request.getSessionId(), parentCode, parentTypeCode, directedGraphCode, recursive, date);
    
    return new RestBodyResponse(response);
  }

  @Endpoint(url = "get-parents", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getParents(ClientRequestIF request, 
      @RequestParamter(name="childCode", required = true) String childCode,
      @RequestParamter(name="childTypeCode", required = true) String childTypeCode,
      @RequestParamter(name="directedGraphCode", required = true) String directedGraphCode,
      @RequestParamter(name="recursive", required = true) Boolean recursive,
      @RequestParamter(name="date", required = true) String sDate)
  {
    Date date = GeoRegistryUtil.parseDate(sDate, true);
    
    JsonObject response = this.service.getParents(request.getSessionId(), childCode, childTypeCode, directedGraphCode, recursive, date);
    
    return new RestBodyResponse(response);
  }

  @Endpoint(url = "add-child", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF addChild(ClientRequestIF request, 
      @RequestParamter(name="parentCode", required = true) String parentCode,
      @RequestParamter(name="parentTypeCode", required = true) String parentTypeCode,
      @RequestParamter(name="childCode", required = true) String childCode,
      @RequestParamter(name="childTypeCode", required = true) String childTypeCode,
      @RequestParamter(name="directedGraphCode", required = true) String directedGraphCode,
      @RequestParamter(name="startDate", required = true) String startDateStr, 
      @RequestParamter(name="endDate", required = true) String endDateStr)
  {
    Date startDate = GeoRegistryUtil.parseDate(startDateStr, true);
    Date endDate = GeoRegistryUtil.parseDate(endDateStr, true);
    
    JsonObject response = this.service.addChild(request.getSessionId(), parentCode, parentTypeCode, childCode, childTypeCode, directedGraphCode, startDate, endDate);

    return new RestBodyResponse(response);
  }

  @Endpoint(url = "remove-child", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF removeChild(ClientRequestIF request, 
      @RequestParamter(name="parentCode", required = true) String parentCode,
      @RequestParamter(name="parentTypeCode", required = true) String parentTypeCode,
      @RequestParamter(name="childCode", required = true) String childCode,
      @RequestParamter(name="childTypeCode", required = true) String childTypeCode,
      @RequestParamter(name="directedGraphCode", required = true) String directedGraphCode,
      @RequestParamter(name="startDate", required = true) String startDateStr, 
      @RequestParamter(name="endDate", required = true) String endDateStr)
  {
    Date startDate = GeoRegistryUtil.parseDate(startDateStr, true);
    Date endDate = GeoRegistryUtil.parseDate(endDateStr, true);
    
    this.service.removeChild(request.getSessionId(), parentCode, parentTypeCode, childCode, childTypeCode, directedGraphCode, startDate, endDate);

    return new RestResponse();
  }
}
