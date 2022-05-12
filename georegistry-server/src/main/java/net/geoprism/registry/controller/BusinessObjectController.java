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
package net.geoprism.registry.controller;

import com.google.gson.JsonArray;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;

import net.geoprism.registry.service.BusinessObjectService;

@Controller(url = "business-object")
public class BusinessObjectController
{
  private BusinessObjectService service;

  public BusinessObjectController()
  {
    this.service = new BusinessObjectService();
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get")
  public ResponseIF get(ClientRequestIF request, 
      @RequestParamter(name = "businessTypeCode", required = true) String businessTypeCode,
      @RequestParamter(name = "code", required = true) String code)
  {
    return new RestBodyResponse(service.get(request.getSessionId(), businessTypeCode, code));
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get-parents")
  public ResponseIF getParents(ClientRequestIF request, 
      @RequestParamter(name = "businessTypeCode", required = true) String businessTypeCode,
      @RequestParamter(name = "code", required = true) String code,
      @RequestParamter(name = "businessEdgeTypeCode", required = true) String businessEdgeTypeCode)
  {
    JsonArray parents = this.service.getParents(request.getSessionId(), businessTypeCode, code, businessEdgeTypeCode);
    
    return new RestBodyResponse(parents);
  }
  
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get-children")
  public ResponseIF getChildren(ClientRequestIF request, 
      @RequestParamter(name = "businessTypeCode", required = true) String businessTypeCode,
      @RequestParamter(name = "code", required = true) String code,
      @RequestParamter(name = "businessEdgeTypeCode", required = true) String businessEdgeTypeCode)
  {
    JsonArray parents = this.service.getChildren(request.getSessionId(), businessTypeCode, code, businessEdgeTypeCode);
    
    return new RestBodyResponse(parents);
  }
  
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get-geo-objects")
  public ResponseIF getGeoObjects(ClientRequestIF request, 
      @RequestParamter(name = "businessTypeCode", required = true) String businessTypeCode,
      @RequestParamter(name = "code", required = true) String code,
      @RequestParamter(name = "date", required = true) String date)
  {
    JsonArray geoObjects = this.service.getGeoObjects(request.getSessionId(), businessTypeCode, code, date);
    
    return new RestBodyResponse(geoObjects);
  }
  
}
