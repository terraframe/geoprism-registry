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
package net.geoprism.registry.controller;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;

import net.geoprism.registry.service.TransitionEventService;

@Controller(url = "transition-event")
public class TransitionEventController
{
  private TransitionEventService service;

  public TransitionEventController()
  {
    this.service = new TransitionEventService();
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "page")
  public ResponseIF page(ClientRequestIF request, @RequestParamter(name = "pageSize") Integer pageSize, @RequestParamter(name = "pageNumber") Integer pageNumber)
  {
    return new RestBodyResponse(service.page(request.getSessionId(), pageSize, pageNumber));
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get-details")
  public ResponseIF getDetails(ClientRequestIF request, @RequestParamter(name = "oid") String oid)
  {
    return new RestBodyResponse(service.getDetails(request.getSessionId(), oid));
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "apply")
  public ResponseIF apply(ClientRequestIF request, @RequestParamter(name = "event") String eventJSON)
  {
    return new RestBodyResponse(this.service.apply(request.getSessionId(), eventJSON));
  }
  
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "delete")
  public ResponseIF delete(ClientRequestIF request, @RequestParamter(name = "eventId") String eventId)
  {
    this.service.delete(request.getSessionId(), eventId);
    
    return new RestResponse();
  }
}
