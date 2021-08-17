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

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;

import com.google.gson.JsonObject;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.MultipartFileParameter;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.InputStreamResponse;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;
import com.runwaysdk.resource.ApplicationResource;

import net.geoprism.registry.service.ChangeRequestService;

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

  /**
   * 
   * @param request
   * @param crOid
   * @param file
   * @return
   * @throws IOException
   */
  @Endpoint(url = "upload-file-cr", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF uploadFileCR(ClientRequestIF request, @RequestParamter(name = "crOid") String crOid, @RequestParamter(name = "file") MultipartFileParameter file) throws IOException
  {
    try (InputStream stream = file.getInputStream())
    {
      String fileName = file.getFilename();

      String vfOid = service.uploadFileCR(request.getSessionId(), crOid, fileName, stream);

      return new RestBodyResponse(vfOid);
    }
  }

  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF listDocumentsCR(ClientRequestIF request, @RequestParamter(name = "crOid") String crOid)
  {
    String json = service.listDocumentsCR(request.getSessionId(), crOid);

    return new RestBodyResponse(json);
  }

  @Endpoint(url = "download-file-cr", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF downloadDocumentCR(ClientRequestIF request, @RequestParamter(name = "crOid") String crOid, @RequestParamter(name = "vfOid") String vfOid)
  {
    ApplicationResource res = service.downloadDocumentCR(request.getSessionId(), crOid, vfOid);

    return new InputStreamResponse(res.openNewStream(), "application/octet-stream", res.getName());
  }

  @Endpoint(url = "delete-file-cr", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF deleteDocumentCR(ClientRequestIF request, @RequestParamter(name = "crOid") String crOid, @RequestParamter(name = "vfOid") String vfOid)
  {
    service.deleteDocumentCR(request.getSessionId(), crOid, vfOid);

    return new RestResponse();
  }
  
  @Endpoint(url = "reject", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF reject(ClientRequestIF request, @RequestParamter(name = "request") String cr)
  {
    service.reject(request.getSessionId(), cr);

    return new RestResponse();
  }

  /**
   * Returns a paginated response of all change requests that your user has
   * permission to view. Filter may be used to only return change requests that
   * are of a specific approval status.
   * 
   * @param pageSize
   *          The number of results to return in each page.
   * @param pageNumber
   *          The page number of results to return.
   * @param filter
   *          May be one of PENDING, REJECTED, ACCEPTED, INVALID
   */
  @Endpoint(error = ErrorSerialization.JSON, url = "get-all-requests", method = ServletMethod.GET)
  public ResponseIF getAllRequests(ClientRequestIF request, @RequestParamter(name = "pageSize") Integer pageSize, @RequestParamter(name = "pageNumber") Integer pageNumber, @RequestParamter(name = "filter") String filter, @RequestParamter(name = "oid") String oid)
  {
    JsonObject paginated = service.getAllRequestsSerialized(request.getSessionId(), pageSize, pageNumber, filter, oid);

    return new RestBodyResponse(paginated.toString());
  }

  /**
   * This endpoint can be used to set the status of an action. If your user does
   * not have permissions to set the status a
   * {@link net.geoprism.registry.CGRPermissionException} will be thrown.
   * 
   * @param actionOid
   *          The id of the action.
   * @param status
   *          The approval status. May be one of PENDING, REJECTED, ACCEPTED,
   *          INVALID
   * @throws net.geoprism.registry.CGRPermissionException
   * @return Empty response
   */
  @Endpoint(error = ErrorSerialization.JSON, url = "set-action-status", method = ServletMethod.POST)
  public ResponseIF setActionStatus(ClientRequestIF request, @RequestParamter(name = "actionOid") String actionOid, @RequestParamter(name = "status") String status)
  {
    service.setActionStatus(request.getSessionId(), actionOid, status);

    return new RestResponse();
  }

  /**
   * Implements all actions on the ChangeRequest. If your user does not have
   * permissions to implement the request a
   * {@link net.geoprism.registry.CGRPermissionException} will be thrown. If any
   * of the actions currently have PENDING status an
   * {@link net.geoprism.registry.action.ActionExecuteException} will be thrown.
   * If the request is implemented successfully a 200 response will be returned
   * and all actions as well as the change request will have an approval status
   * set to APPROVED.
   * 
   * @param requestId
   *          The id of the Change Request to implement.
   * @throws net.geoprism.registry.action.ActionExecuteException
   * @throws net.geoprism.registry.CGRPermissionException
   * @return Empty response
   */
  @Endpoint(error = ErrorSerialization.JSON, url = "implement-decisions", method = ServletMethod.POST)
  public ResponseIF implementDecisions(ClientRequestIF request, @RequestParamter(name = "request") String cr)
  {
    JsonObject details = service.implementDecisions(request.getSessionId(), cr);

    return new RestBodyResponse(details);
  }

  @Endpoint(error = ErrorSerialization.JSON, url = "update", method = ServletMethod.POST)
  public ResponseIF update(ClientRequestIF request, @RequestParamter(name = "request") String cr)
  {
    JsonObject details = service.update(request.getSessionId(), cr);
    
    return new RestBodyResponse(details);
  }
  
  @Endpoint(error = ErrorSerialization.JSON, url = "delete", method = ServletMethod.POST)
  public ResponseIF deleteChangeRequest(ClientRequestIF request, @RequestParamter(name = "requestId") String requestId) throws JSONException
  {
    service.deleteChangeRequest(request.getSessionId(), requestId);

    return new RestResponse();
  }
}
