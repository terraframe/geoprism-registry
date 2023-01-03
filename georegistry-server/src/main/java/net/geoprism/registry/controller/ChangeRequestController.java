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

import java.io.IOException;
import java.io.InputStream;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;
import com.runwaysdk.resource.ApplicationResource;

import net.geoprism.registry.service.ChangeRequestService;
import net.geoprism.registry.spring.JsonObjectDeserializer;

/**
 * This controller is used by the change request table widget.
 * 
 * @author rrowlands
 *
 */
@RestController
@Validated
public class ChangeRequestController extends RunwaySpringController
{
  public static class RequestObjectBody
  {
    @NotNull
    @JsonDeserialize(using = JsonObjectDeserializer.class)
    private JsonObject request;
    
    String newCode;

    public JsonObject getRequest()
    {
      return request;
    }

    public void setRequest(JsonObject request)
    {
      this.request = request;
    }
    
    public String getNewCode()
    {
      return newCode;
    }
    
    public void setNewCode(String newCode)
    {
      this.newCode = newCode;
    }
  }

  public static class ChangeRequestBody
  {
    @NotEmpty
    private String requestId;

    public String getRequestId()
    {
      return requestId;
    }

    public void setRequestId(String requestId)
    {
      this.requestId = requestId;
    }
  }

  public static class DocumentFileBody extends ChangeRequestBody
  {
    @NotEmpty
    private String fileId;

    public String getFileId()
    {
      return fileId;
    }

    public void setFileId(String fileId)
    {
      this.fileId = fileId;
    }
  }

  public static class UploadFileBody extends ChangeRequestBody
  {
    @NotNull
    private MultipartFile file;

    public MultipartFile getFile()
    {
      return file;
    }

    public void setFile(MultipartFile file)
    {
      this.file = file;
    }
  }

  public static class ActionStatusBody
  {
    @NotEmpty
    private String actionOid;

    @NotEmpty
    private String status;

    public String getActionOid()
    {
      return actionOid;
    }

    public void setActionOid(String actionOid)
    {
      this.actionOid = actionOid;
    }

    public String getStatus()
    {
      return status;
    }

    public void setStatus(String status)
    {
      this.status = status;
    }

  }

  public static final String API_PATH = "changerequest";

  @Autowired
  private ChangeRequestService service;

  /**
   * 
   * @param request
   * @param requestId
   * @param file
   * @return
   * @throws IOException
   */
  @PostMapping(API_PATH + "/upload-file-cr")
  public ResponseEntity<String> uploadFileCR(@Valid
  @ModelAttribute UploadFileBody body) throws IOException
  {
    try (InputStream stream = body.file.getInputStream())
    {
      String fileName = body.file.getOriginalFilename();

      String fileId = service.uploadFileCR(this.getSessionId(), body.getRequestId(), fileName, stream);

      return new ResponseEntity<String>(fileId, HttpStatus.OK);
    }
  }

  @GetMapping(API_PATH + "/list-documents-cr")
  public ResponseEntity<String> listDocumentsCR(@NotEmpty
  @RequestParam String requestId)
  {
    String json = service.listDocumentsCR(this.getSessionId(), requestId);

    return new ResponseEntity<String>(json, HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/download-file-cr")
  public ResponseEntity<InputStreamResource> downloadDocumentCR(@NotEmpty
  @RequestParam String requestId,
      @NotEmpty
      @RequestParam String fileId)
  {
    ApplicationResource res = service.downloadDocumentCR(this.getSessionId(), requestId, fileId);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + res.getName());

    return new ResponseEntity<InputStreamResource>(new InputStreamResource(res.openNewStream()), headers, HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/delete-file-cr")
  public ResponseEntity<Void> deleteDocumentCR(@Valid
  @RequestBody DocumentFileBody body)
  {
    service.deleteDocumentCR(this.getSessionId(), body.getRequestId(), body.getFileId());

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  }

  @PostMapping(API_PATH + "/reject")
  public ResponseEntity<Void> reject(@Valid
  @RequestBody ChangeRequestBody body)
  {
    service.reject(this.getSessionId(), body.getRequestId());

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
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
  @GetMapping(API_PATH + "/get-all-requests")
  public ResponseEntity<String> getAllRequests(@NotNull
  @RequestParam Integer pageSize,
      @NotNull
      @RequestParam Integer pageNumber, @RequestParam(required = false) String filter, @RequestParam(required = false) String sort, @RequestParam(required = false) String oid)
  {
    JsonObject paginated = service.getAllRequestsSerialized(this.getSessionId(), pageSize, pageNumber, filter, sort, oid);

    return new ResponseEntity<String>(paginated.toString(), HttpStatus.OK);
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
  @PostMapping(API_PATH + "/set-action-status")
  public ResponseEntity<Void> setActionStatus(@Valid
  @RequestBody ActionStatusBody body)
  {
    service.setActionStatus(this.getSessionId(), body.actionOid, body.status);

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
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
  @PostMapping(API_PATH + "/implement-decisions")
  public ResponseEntity<String> implementDecisions(@Valid
  @RequestBody RequestObjectBody body)
  {
    JsonObject details = service.implementDecisions(this.getSessionId(), body.request.toString(), body.newCode);

    return new ResponseEntity<String>(details.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/update")
  public ResponseEntity<String> update(@RequestBody RequestObjectBody body)
  {
    JsonObject details = service.update(this.getSessionId(), body.request);

    return new ResponseEntity<String>(details.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/delete")
  public ResponseEntity<Void> deleteChangeRequest(@Valid
  @RequestBody ChangeRequestBody body) throws JSONException
  {
    service.deleteChangeRequest(this.getSessionId(), body.requestId);

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  }
}
