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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;

import net.geoprism.registry.service.ClassificationTypeService;

@Controller(url = "classification-type")
public class ClassificationTypeController
{
  private ClassificationTypeService service;

  public ClassificationTypeController()
  {
    this.service = new ClassificationTypeService();
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "page")
  public ResponseIF page(ClientRequestIF request, @RequestParamter(name = "criteria") String criteria)
  {
    return new RestBodyResponse(this.service.page(request.getSessionId(), criteria));
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "apply")
  public ResponseIF apply(ClientRequestIF request, @RequestParamter(name = "classificationType") String classificationType)
  {
    JsonObject response = this.service.apply(request.getSessionId(), classificationType);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "remove")
  public ResponseIF remove(ClientRequestIF request, @RequestParamter(name = "oid") String oid)
  {
    this.service.remove(request.getSessionId(), oid);

    return new RestResponse();
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get")
  public ResponseIF get(ClientRequestIF request, @RequestParamter(name = "classificationCode") String classificationCode)
  {
    JsonObject response = this.service.get(request.getSessionId(), classificationCode);

    return new RestBodyResponse(response);
  }
}
