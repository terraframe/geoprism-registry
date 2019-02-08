package net.geoprism.georegistry.controller;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

import javax.servlet.ServletException;

import org.apache.commons.lang3.LocaleUtils;
import org.json.JSONArray;
import org.json.JSONObject;

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
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.InputStreamResponse;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.system.metadata.MdAttributeLocal;
import com.runwaysdk.system.metadata.MdLocalizable;

import net.geoprism.georegistry.service.WMSService;
import net.geoprism.localization.LocalizationFacade;

@Controller(url = "localization")
public class RegistryLocalizationController
{
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
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

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getNewLocaleInformation(ClientRequestIF request) throws IOException, ServletException
  {
    JSONObject json = new JSONObject();

    JSONArray languages = new JSONArray();
    JSONArray countries = new JSONArray();

    for (Locale locale : LocalizationFacade.getAvailableLanguagesSorted())
    {
      JSONObject jobj = new JSONObject();
      jobj.put("key", locale.getLanguage());
      jobj.put("label", locale.getDisplayLanguage());
      languages.put(jobj);
    }

    for (Locale locale : LocalizationFacade.getAvailableCountriesSorted())
    {
      JSONObject jobj = new JSONObject();
      jobj.put("key", locale.getCountry());
      jobj.put("label", locale.getDisplayCountry());
      countries.put(jobj);
    }

    json.put("languages", languages);
    json.put("countries", countries);

    return new RestBodyResponse(json);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF installLocale(ClientRequestIF request, @RequestParamter(name = "language") String language, @RequestParamter(name = "country") String country, @RequestParamter(name = "variant") String variant) throws IOException, ServletException
  {
    installLocaleInRequest(request.getSessionId(), language, country, variant);

    return new RestResponse();
  }

  @Request(RequestType.SESSION)
  public void installLocaleInRequest(String sessionId, String language, String country, String variant)
  {
    String localeString = language;
    if (country != null)
    {
      localeString += "_" + country;
      if (variant != null)
      {
        localeString += "_" + variant;
      }
    }

    Locale locale = LocaleUtils.toLocale(localeString);

    com.runwaysdk.LocalizationFacade.install(locale);

    new WMSService().createAllWMSLayers(true);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
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

    builder.addLocalizedValueStoreTab("Core Exceptions", LocalizedValueStore.TAG_NAME_ALL_RUNWAY_EXCEPTIONS);

    builder.addLocalizedValueStoreTab("UI Text", Arrays.asList(LocalizedValueStore.TAG_NAME_UI_TEXT));

    return builder.build();
  }
}
