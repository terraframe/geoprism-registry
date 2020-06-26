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

import net.geoprism.registry.service.ServiceFactory;

@Controller(url = "geoobject")
public class GeoObjectController
{
  /**
   * Returns a paginated response of all GeoObjects matching the provided criteria.
   **/
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get-all")
  public ResponseIF getAll(ClientRequestIF request, @RequestParamter(name = "typeCode") String typeCode,
      @RequestParamter(name = "hierarchyCode") String hierarchyCode, @RequestParamter(name = "updatedSince") Long updatedSince,
      @RequestParamter(name = "includeGeographicLevel") Boolean includeLevel, @RequestParamter(name = "format") String format,
      @RequestParamter(name = "pageNumber") Integer pageNumber, @RequestParamter(name = "pageSize") Integer pageSize,
      @RequestParamter(name = "externalSystemId") String externalSystemId)
  {
    Date dUpdatedSince = null;
    if (updatedSince != null)
    {
      dUpdatedSince = new Date(updatedSince);
    }
    
    JsonObject jo = ServiceFactory.getGeoObjectService().getAll(request.getSessionId(), typeCode, hierarchyCode, dUpdatedSince, includeLevel, format, externalSystemId, pageNumber, pageSize);
    
//    return new InputStreamResponse(is, "application/json", "get-all.json");
    
    return new RestBodyResponse(jo.toString());
  }
}
