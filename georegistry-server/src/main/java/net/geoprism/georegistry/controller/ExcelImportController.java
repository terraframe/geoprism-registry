package net.geoprism.georegistry.controller;

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

import net.geoprism.georegistry.service.ExcelService;

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

  @Endpoint(url = "export-spreadsheet", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF exportShapefile(ClientRequestIF request, @RequestParamter(name = "type") String type) throws JSONException
  {
    return new InputStreamResponse(service.exportSpreadsheet(request.getSessionId(), type), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "export.xlsx");
  }

  @Endpoint(url = "cancel-import", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF cancelImport(ClientRequestIF request, @RequestParamter(name = "configuration") String configuration)
  {
    service.cancelImport(request.getSessionId(), configuration);

    return new RestBodyResponse("");
  }
}
