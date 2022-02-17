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
package net.geoprism.registry.etl;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.MultipartFileParameter;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;
import com.runwaysdk.system.scheduler.JobHistory;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.io.GeoObjectImportConfiguration;

@Controller(url = "etl")
public class ETLController
{
  protected ETLService service;
  
  public ETLController()
  {
    this.service = new ETLService();
  }
  
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "reimport")
  public ResponseIF doReImport(ClientRequestIF request, @RequestParamter(name = "file") MultipartFileParameter file, @RequestParamter(name = "json") String json)
  {
    JsonObject config = this.service.reImport(request.getSessionId(), file, json);
    
    return new RestBodyResponse(config.toString());
  }
  
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "import")
  public ResponseIF doImport(ClientRequestIF request, @RequestParamter(name = "json") String json)
  {
    JsonObject config = this.service.doImport(request.getSessionId(), json);
    
    return new RestBodyResponse(config.toString());
  }
  
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "validation-resolve")
  public ResponseIF submitValidationProblemResolution(ClientRequestIF request, @RequestParamter(name = "config") String config)
  {
    this.service.submitValidationProblemResolution(request.getSessionId(), config);
    
    return new RestResponse();
  }
  
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "error-resolve")
  public ResponseIF submitImportErrorResolution(ClientRequestIF request, @RequestParamter(name = "config") String config)
  {
    this.service.submitImportErrorResolution(request.getSessionId(), config);
    
    return new RestResponse();
  }
  
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "import-resolve")
  public ResponseIF resolveImport(ClientRequestIF request, @RequestParamter(name = "historyId") String historyId)
  {
    this.service.resolveImport(request.getSessionId(), historyId);
    
    return new RestResponse();
  }
  
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get-active")
  public ResponseIF getActiveImports(ClientRequestIF request, @RequestParamter(name = "pageSize") Integer pageSize, @RequestParamter(name = "pageNumber") Integer pageNumber, @RequestParamter(name = "sortAttr") String sortAttr, @RequestParamter(name = "isAscending") Boolean isAscending)
  {
    if (sortAttr == null || sortAttr == "")
    {
      sortAttr = JobHistory.CREATEDATE;
    }
    
    if (isAscending == null)
    {
      isAscending = true;
    }
    
    JsonObject config = this.service.getActiveImports(request.getSessionId(), pageSize, pageNumber, sortAttr, isAscending);
    
    return new RestBodyResponse(config.toString());
  }
  
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get-completed")
  public ResponseIF getCompletedImports(ClientRequestIF request, @RequestParamter(name = "pageSize") Integer pageSize, @RequestParamter(name = "pageNumber") Integer pageNumber, @RequestParamter(name = "sortAttr") String sortAttr, @RequestParamter(name = "isAscending") Boolean isAscending)
  {
    if (sortAttr == null || sortAttr == "")
    {
      sortAttr = JobHistory.CREATEDATE;
    }
    
    if (isAscending == null)
    {
      isAscending = true;
    }
    
    JsonObject json = this.service.getCompletedImports(request.getSessionId(), pageSize, pageNumber, sortAttr, isAscending);
    
    return new RestBodyResponse(json.toString());
  }
  
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get-errors")
  public ResponseIF getImportErrors(ClientRequestIF request, @RequestParamter(name = "historyId") String historyId, @RequestParamter(name = "onlyUnresolved") Boolean onlyUnresolved, @RequestParamter(name = "pageSize") Integer pageSize, @RequestParamter(name = "pageNumber") Integer pageNumber)
  {
    JsonObject json = this.service.getImportErrors(request.getSessionId(), historyId, onlyUnresolved, pageSize, pageNumber);
    
    return new RestBodyResponse(json.toString());
  }
  
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get-import-details")
  public ResponseIF getImportDetails(ClientRequestIF request, @RequestParamter(name = "historyId") String historyId, @RequestParamter(name = "onlyUnresolved") Boolean onlyUnresolved, @RequestParamter(name = "pageSize") Integer pageSize, @RequestParamter(name = "pageNumber") Integer pageNumber)
  {
    JsonObject details = this.service.getImportDetails(request.getSessionId(), historyId, onlyUnresolved, pageSize, pageNumber);
    
    return new RestBodyResponse(details.toString());
  }
  
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "cancel-import")
  public ResponseIF cancelImport(ClientRequestIF request, @RequestParamter(name = "configuration") String config)
  {
    this.service.cancelImport(request.getSessionId(), config);
    
    return new RestResponse();
  }
  
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get-export-details")
  public ResponseIF getExportDetails(ClientRequestIF request, @RequestParamter(name = "historyId") String historyId, @RequestParamter(name = "pageSize") Integer pageSize, @RequestParamter(name = "pageNumber") Integer pageNumber)
  {
    JsonObject details = this.service.getExportDetails(request.getSessionId(), historyId, pageSize, pageNumber);
    
    return new RestBodyResponse(details.toString());
  }
  
  @Endpoint(url = "import-edge-json", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF importEdgeJson(ClientRequestIF request, @RequestParamter(name = "relationshipType") String relationshipType, @RequestParamter(name = "graphTypeCode") String graphTypeCode, @RequestParamter(name = "startDate") String startDate, @RequestParamter(name = "endDate") String endDate, @RequestParamter(name = "file") MultipartFileParameter file) throws IOException, JSONException, ParseException
  {
    try (InputStream stream = file.getInputStream())
    {
      Date sDate = startDate != null ? GeoRegistryUtil.parseDate(startDate) : null;
      Date eDate = endDate != null ? GeoRegistryUtil.parseDate(endDate) : null;

      service.importEdgeJson(request.getSessionId(), relationshipType, graphTypeCode, sDate, eDate, stream);

      return new RestResponse();
    }
  }

}
