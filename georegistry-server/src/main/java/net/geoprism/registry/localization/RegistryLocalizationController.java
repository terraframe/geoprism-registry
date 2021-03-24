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
package net.geoprism.registry.localization;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletException;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.JsonArray;
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

import net.geoprism.localization.LocalizationFacade;
import net.geoprism.registry.service.ServiceFactory;

@Controller(url = "localization")
public class RegistryLocalizationController
{
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF importSpreadsheet(ClientRequestIF request, @RequestParamter(name = "file") MultipartFileParameter file)
  {
    new LocalizationService().importSpreadsheet(request.getSessionId(), file);

    return new RestResponse();
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "set-locale")
  public ResponseIF setLocale(ClientRequestIF request, @RequestParamter(name = "locale") String locale)
  {
    new LocalizationService().setLocale(request.getSessionId(), locale);

    return new RestResponse();
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

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get-locales")
  public ResponseIF getLocales(ClientRequestIF request) throws IOException, ServletException
  {
    final JsonArray locales = ServiceFactory.getRegistryService().getLocales(request.getSessionId());

    return new RestBodyResponse(locales);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF installLocale(ClientRequestIF request, @RequestParamter(name = "language") String language, @RequestParamter(name = "country") String country, @RequestParamter(name = "variant") String variant) throws IOException, ServletException
  {
    LocalizationService service = new LocalizationService();
    String locale = service.installLocaleInRequest(request.getSessionId(), language, country, variant);

    RestResponse response = new RestResponse();
    response.set("locale", locale);

    return response;
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF exportSpreadsheet(ClientRequestIF request)
  {
    return new LocalizationService().exportSpreadsheetInRequest(request.getSessionId());
  }
}
