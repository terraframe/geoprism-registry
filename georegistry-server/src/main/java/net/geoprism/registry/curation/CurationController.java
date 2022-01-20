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
package net.geoprism.registry.curation;

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

@Controller(url = "curation")
public class CurationController
{
  protected CurationService service;

  public CurationController()
  {
    this.service = new CurationService();
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "details")
  public ResponseIF details(ClientRequestIF request, @RequestParamter(name = "historyId") String historyId, @RequestParamter(name = "onlyUnresolved") Boolean onlyUnresolved, @RequestParamter(name = "pageSize") Integer pageSize, @RequestParamter(name = "pageNumber") Integer pageNumber)
  {
    JsonObject details = this.service.details(request.getSessionId(), historyId, onlyUnresolved, pageSize, pageNumber);

    return new RestBodyResponse(details.toString());
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "page")
  public ResponseIF page(ClientRequestIF request, @RequestParamter(name = "historyId") String historyId, @RequestParamter(name = "onlyUnresolved") Boolean onlyUnresolved, @RequestParamter(name = "pageSize") Integer pageSize, @RequestParamter(name = "pageNumber") Integer pageNumber)
  {
    JsonObject page = this.service.page(request.getSessionId(), historyId, onlyUnresolved, pageSize, pageNumber);

    return new RestBodyResponse(page.toString());
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "curate")
  public ResponseIF curate(ClientRequestIF request, @RequestParamter(name = "listTypeVersionId") String listTypeVersionId)
  {
    JsonObject serializedHistory = this.service.curate(request.getSessionId(), listTypeVersionId);

    return new RestBodyResponse(serializedHistory.toString());
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "problem-resolve")
  public ResponseIF submitProblemResolution(ClientRequestIF request, @RequestParamter(name = "config") String config)
  {
    this.service.submitProblemResolution(request.getSessionId(), config);

    return new RestResponse();
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "set-resolution")
  public ResponseIF setResolution(ClientRequestIF request, @RequestParamter(name = "problemId") String problemId, @RequestParamter(name = "resolution") String resolution)
  {
    this.service.setResolution(request.getSessionId(), problemId, resolution);

    return new RestResponse();
  }

}
