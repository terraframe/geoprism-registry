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

import com.google.gson.JsonArray;
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

import net.geoprism.registry.service.ClassificationService;

@Controller(url = "classification")
public class ClassificationController
{
  private ClassificationService service;

  public ClassificationController()
  {
    this.service = new ClassificationService();
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "apply")
  public ResponseIF apply(ClientRequestIF request, @RequestParamter(name = "classificationCode") String classificationCode, @RequestParamter(name = "parentCode") String parentCode, @RequestParamter(name = "classification") String classification, @RequestParamter(name = "isNew") Boolean isNew)
  {
    JsonObject object = JsonParser.parseString(classification).getAsJsonObject();

    JsonObject response = this.service.apply(request.getSessionId(), classificationCode, parentCode, object, isNew);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "remove")
  public ResponseIF remove(ClientRequestIF request, @RequestParamter(name = "classificationCode") String classificationCode, @RequestParamter(name = "code") String code)
  {
    this.service.remove(request.getSessionId(), classificationCode, code);

    return new RestResponse();
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get-children")
  public ResponseIF getChildren(ClientRequestIF request, @RequestParamter(name = "classificationCode") String classificationCode, @RequestParamter(name = "code") String code)
  {
    JsonArray children = this.service.getChildren(request.getSessionId(), classificationCode, code);

    return new RestBodyResponse(children);
  }
}
