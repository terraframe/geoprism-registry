package net.geoprism.georegistry;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.MultipartFileParameter;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.localization.LocalizationExcelExporter;
import com.runwaysdk.localization.LocalizationExcelImporter;
import com.runwaysdk.localization.LocalizedValueStore;
import com.runwaysdk.localization.configuration.ConfigurationBuilder;
import com.runwaysdk.localization.configuration.SpreadsheetConfiguration;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.InputStreamResponse;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestResponse;
import com.runwaysdk.mvc.ViewResponse;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.system.metadata.MdAttributeLocal;
import com.runwaysdk.system.metadata.MdLocalizable;

@Controller(url = "localization")
public class RegistryLocalizationController
{
  @Endpoint(method = ServletMethod.POST)
  public ResponseIF importSpreadsheet(ClientRequestIF request, @RequestParamter(name = "file") MultipartFileParameter file)
  {
    importSpreadsheetInRequest(request.getSessionId(), file);
    
    return new RestResponse();
  }
  @Request(RequestType.SESSION)
  public void importSpreadsheetInRequest(String sessionId, MultipartFileParameter file)
  {
    try
    {
      LocalizationExcelImporter importer = new LocalizationExcelImporter(buildConfig(), file.getInputStream());
      importer.doImport();
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }
  
  @Endpoint(method = ServletMethod.GET)
  public ResponseIF exportSpreadsheet(ClientRequestIF request)
  {
    return exportSpreadsheetInRequest(request.getSessionId());
  }
  @Request(RequestType.SESSION)
  public InputStreamResponse exportSpreadsheetInRequest(String sessionId)
  {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    BufferedOutputStream buffer = new BufferedOutputStream(bytes);
    
    LocalizationExcelExporter exporter = new LocalizationExcelExporter(buildConfig(), buffer);
    exporter.export();
    
    ByteArrayInputStream is = new ByteArrayInputStream(bytes.toByteArray());
    
    return new InputStreamResponse(is, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "localization.xlsx");
  }
  
  private SpreadsheetConfiguration buildConfig()
  {
    ConfigurationBuilder builder = new ConfigurationBuilder();
    
    builder.addAttributeLocalTab("Exceptions", (MdAttributeLocal) BusinessFacade.get(MdLocalizable.getMessageMd()));
    
//    builder.addLocalizedValueStoreTab("Runway Exceptions", LocalizedValueStore.TAG_NAME_RUNWAY_EXCEPTION);
    
    builder.addLocalizedValueStoreTab("UI Text", LocalizedValueStore.TAG_NAME_UI_TEXT);
    
    return  builder.build();
  }
}
