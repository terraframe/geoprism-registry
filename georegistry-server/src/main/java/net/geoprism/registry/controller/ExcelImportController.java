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

import net.geoprism.registry.service.ExcelService;

@Controller(url = "excel")
public class ExcelImportController
{
  private ExcelService service;

  public ExcelImportController()
  {
    this.service = new ExcelService();
  }

  @Endpoint(url = "get-configuration", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF getConfiguration(ClientRequestIF request, @RequestParamter(name = "type") String type, @RequestParamter(name = "file") MultipartFileParameter file) throws IOException, JSONException
  {
    try (InputStream stream = file.getInputStream())
    {
      String fileName = file.getFilename();

      JsonObject configuration = service.getExcelConfiguration(request.getSessionId(), type, fileName, stream);

      // object.add("options", service.getOptions(request.getSessionId()));
      // object.put("classifiers", new
      // JSONArray(ClassifierDTO.getCategoryClassifiersAsJSON(request)));

      return new RestBodyResponse(configuration);
    }
  }

  @Endpoint(url = "import-spreadsheet", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF importSpreadsheet(ClientRequestIF request, @RequestParamter(name = "configuration") String configuration) throws JSONException
  {
    JsonObject response = service.importExcelFile(request.getSessionId(), configuration);

    return new RestBodyResponse(response);
  }

  @Endpoint(url = "cancel-import", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF cancelImport(ClientRequestIF request, @RequestParamter(name = "configuration") String configuration)
  {
    service.cancelImport(request.getSessionId(), configuration);

    return new RestBodyResponse("");
  }

  @Endpoint(url = "export-spreadsheet", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF exportShapefile(ClientRequestIF request, @RequestParamter(name = "type") String type, @RequestParamter(name = "hierarchyType") String hierarchyType) throws JSONException
  {
    return new InputStreamResponse(service.exportSpreadsheet(request.getSessionId(), type, hierarchyType), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "export.xlsx");
  }
}
