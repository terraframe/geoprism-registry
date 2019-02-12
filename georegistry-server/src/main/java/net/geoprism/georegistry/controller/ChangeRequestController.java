/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Runway SDK(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.georegistry.controller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;

import net.geoprism.georegistry.service.ChangeRequestService;

/**
 * This controller is used by the change request table widget.
 * 
 * @author rrowlands
 *
 */
@Controller(url = "changerequest")
public class ChangeRequestController
{
  private ChangeRequestService service;

  public ChangeRequestController()
  {
    this.service = new ChangeRequestService();
  }

  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF acceptAction(ClientRequestIF request, @RequestParamter(name = "action") String action) throws JSONException
  {
    service.acceptAction(request.getSessionId(), action);

    return new RestResponse();
  }

  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF rejectAction(ClientRequestIF request, @RequestParamter(name = "action") String action) throws JSONException
  {
    service.rejectAction(request.getSessionId(), action);

    return new RestResponse();
  }

  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF getAllActions(ClientRequestIF request, @RequestParamter(name = "requestId") String requestId) throws JSONException
  {
    String json = service.getAllActions(request.getSessionId(), requestId);

    return new RestBodyResponse(json);
  }

  @Endpoint(error = ErrorSerialization.JSON, url = "get-all-requests", method = ServletMethod.GET)
  public ResponseIF getAllRequests(ClientRequestIF request) throws JSONException
  {
    JSONArray requests = service.getAllRequests(request.getSessionId());

    return new RestBodyResponse(requests);
  }

  @Endpoint(error = ErrorSerialization.JSON, url = "get-request-details", method = ServletMethod.GET)
  public ResponseIF getRequestDetails(ClientRequestIF request, @RequestParamter(name = "requestId") String requestId) throws JSONException
  {
    JSONObject response = service.getRequestDetails(request.getSessionId(), requestId);

    return new RestBodyResponse(response);
  }

  @Endpoint(error = ErrorSerialization.JSON, url = "approve-all-actions", method = ServletMethod.POST)
  public ResponseIF approveAllActions(ClientRequestIF request, @RequestParamter(name = "requestId") String requestId) throws JSONException
  {
    service.approveAllActions(request.getSessionId(), requestId);

    return new RestResponse();
  }

  @Endpoint(error = ErrorSerialization.JSON, url = "reject-all-actions", method = ServletMethod.POST)
  public ResponseIF rejectAllActions(ClientRequestIF request, @RequestParamter(name = "requestId") String requestId) throws JSONException
  {
    service.rejectAllActions(request.getSessionId(), requestId);

    return new RestResponse();
  }
}
