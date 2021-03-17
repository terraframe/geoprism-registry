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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

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

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
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
  public ResponseIF getConfiguration(ClientRequestIF request, @RequestParamter(name = "type") String type, @RequestParamter(name = "startDate") String startDate, @RequestParamter(name = "endDate") String endDate, @RequestParamter(name = "file") MultipartFileParameter file, @RequestParamter(name = "strategy") String sStrategy, @RequestParamter(name = "copyBlank") Boolean copyBlank) throws IOException, JSONException, ParseException
  {
    try (InputStream stream = file.getInputStream())
    {
      String fileName = file.getFilename();

      SimpleDateFormat format = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);
      format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

      Date sDate = startDate != null ? format.parse(startDate) : null;
      Date eDate = endDate != null ? format.parse(endDate) : null;

      ImportStrategy strategy = ImportStrategy.valueOf(sStrategy);

      JSONObject configuration = service.getExcelConfiguration(request.getSessionId(), type, sDate, eDate, fileName, stream, strategy, copyBlank);

      // object.add("options", service.getOptions(request.getSessionId()));
      // object.put("classifiers", new
      // JSONArray(ClassifierDTO.getCategoryClassifiersAsJSON(request)));

      return new RestBodyResponse(configuration);
    }
  }

  @Endpoint(url = "export-spreadsheet", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF exportShapefile(ClientRequestIF request, @RequestParamter(name = "type") String type, @RequestParamter(name = "hierarchyType") String hierarchyType) throws JSONException
  {
    return new InputStreamResponse(service.exportSpreadsheet(request.getSessionId(), type, hierarchyType), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "export.xlsx");
  }
}
