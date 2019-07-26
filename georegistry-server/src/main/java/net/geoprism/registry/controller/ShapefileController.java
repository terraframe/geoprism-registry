/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
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

import net.geoprism.DataUploaderDTO;
import net.geoprism.registry.service.ShapefileService;

@Controller(url = "shapefile")
public class ShapefileController
{
  private ShapefileService service;

  public ShapefileController()
  {
    this.service = new ShapefileService();
  }

  @Endpoint(url = "get-shapefile-configuration", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF getShapefileConfiguration(ClientRequestIF request, @RequestParamter(name = "type") String type, @RequestParamter(name = "file") MultipartFileParameter file) throws IOException, JSONException
  {
    try (InputStream stream = file.getInputStream())
    {
      String fileName = file.getFilename();

      JsonObject configuration = service.getShapefileConfiguration(request.getSessionId(), type, fileName, stream);

      // object.add("options", service.getOptions(request.getSessionId()));
      // object.put("classifiers", new
      // JSONArray(ClassifierDTO.getCategoryClassifiersAsJSON(request)));

      return new RestBodyResponse(configuration);
    }
  }

  @Endpoint(url = "import-shapefile", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF importShapefile(ClientRequestIF request, @RequestParamter(name = "configuration") String configuration) throws JSONException
  {
    JsonObject response = service.importShapefile(request.getSessionId(), configuration);

    return new RestBodyResponse(response);
  }

  @Endpoint(url = "cancel-import", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF cancelImport(ClientRequestIF request, @RequestParamter(name = "configuration") String configuration)
  {
    DataUploaderDTO.cancelImport(request, configuration);

    return new RestBodyResponse("");
  }

  @Endpoint(url = "export-shapefile", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF exportShapefile(ClientRequestIF request, @RequestParamter(name = "type") String type, @RequestParamter(name = "hierarchyType") String hierarchyType) throws JSONException
  {
    return new InputStreamResponse(service.exportShapefile(request.getSessionId(), type, hierarchyType), "application/zip", "shapefile.zip");
  }
}
