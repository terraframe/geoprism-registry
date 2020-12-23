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

import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.Cookie;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.LocalizationFacade;
import com.runwaysdk.RunwayException;
import com.runwaysdk.constants.ClientConstants;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RedirectResponse;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.request.ServletRequestIF;
import com.runwaysdk.web.WebClientSession;

import net.geoprism.ClientConfigurationService;
import net.geoprism.RoleViewDTO;
import net.geoprism.SessionController;
import net.geoprism.account.LocaleSerializer;
import net.geoprism.account.OauthServerIF;
import net.geoprism.session.RegistrySessionServiceDTO;

@Controller(url = "cgrsession")
public class RegistrySessionController
{
  public static final long serialVersionUID = 1234283350799L;

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF ologin(ServletRequestIF req, @RequestParamter(name = "code") String code, @RequestParamter(name = "state") String state) throws MalformedURLException, JSONException
  {
    final SessionController geoprism = new SessionController();
    
//    URL url = new URL(req.getScheme(), req.getServerName(), req.getServerPort(), req.getContextPath());
//
//    String redirect = url.toString();

    JSONObject stateObject = new JSONObject(state);
    String serverId = stateObject.getString(OauthServerIF.SERVER_ID);

    Locale[] locales = geoprism.getLocales(req);

    WebClientSession clientSession = WebClientSession.createAnonymousSession(locales);

    try
    {
      ClientRequestIF clientRequest = clientSession.getRequest();

      String cgrSessionJsonString = RegistrySessionServiceDTO.ologin(clientRequest, serverId, code, LocaleSerializer.serialize(locales), null);
      
      JsonObject cgrSessionJson = (JsonObject) JsonParser.parseString(cgrSessionJsonString);
      final String sessionId = cgrSessionJson.get("sessionId").getAsString();
      final String username = cgrSessionJson.get("username").getAsString();
      
      geoprism.createSession(req, sessionId, locales);
      clientRequest = (ClientRequestIF) req.getAttribute(ClientConstants.CLIENTREQUEST);

      JsonArray roles = (JsonArray) JsonParser.parseString(RoleViewDTO.getCurrentRoles(clientRequest));
      JsonArray roleDisplayLabels = (JsonArray) JsonParser.parseString(RoleViewDTO.getCurrentRoleDisplayLabels(clientRequest));
      
      JsonObject cookieJson = new JsonObject();
      cookieJson.addProperty("loggedIn", clientRequest.isLoggedIn());
      cookieJson.add("roles", roles);
      cookieJson.add("roleDisplayLabels", roleDisplayLabels);
      cookieJson.addProperty("userName", username);
      cookieJson.addProperty("version", ClientConfigurationService.getServerVersion());
      
      JsonArray installedLocalesArr = new JsonArray();
      List<Locale> installedLocales = LocalizationFacade.getInstalledLocales();
      for (Locale loc : installedLocales)
      {
        JsonObject locObj = new JsonObject();
        locObj.addProperty("language", loc.getDisplayLanguage());
        locObj.addProperty("country", loc.getDisplayCountry());
        locObj.addProperty("name", loc.getDisplayName());
        locObj.addProperty("variant", loc.getDisplayVariant());

        installedLocalesArr.add(locObj);
      }
      cookieJson.add("installedLocales", installedLocalesArr);
      
      final String cookieValue = URLEncoder.encode(cookieJson.toString(), "UTF-8");
      
      Cookie cookie = new Cookie("user", cookieValue);
      cookie.setMaxAge(-1);
      
      RedirectResponse response = new RedirectResponse("/");
      response.addCookie(cookie);
      
      return response;
    }
    catch (Throwable t)
    {
      Locale locale = CommonProperties.getDefaultLocale();
      
      if (locales.length > 0)
      {
        locale = locales[0];
      }
      
      String errorMessage = RunwayException.localizeThrowable(t, locale);
      
      try
      {
        errorMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8.name());
      }
      catch (Throwable t2)
      {
        throw new ProgrammingErrorException(t2);
      }
      
      RedirectResponse response = new RedirectResponse("/cgr/manage#/login/" + errorMessage);
      return response;
    }
    finally
    {
      clientSession.logout();
    }
  }
}
