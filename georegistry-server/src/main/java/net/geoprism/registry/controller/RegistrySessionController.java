/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.Cookie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.RunwayException;
import com.runwaysdk.constants.ClientConstants;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.CookieResponse;
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
import net.geoprism.SessionEvent;
import net.geoprism.SessionEvent.EventType;
import net.geoprism.account.LocaleSerializer;
import net.geoprism.account.OauthServerIF;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.session.RegistrySessionServiceDTO;

@Controller(url = "cgrsession")
public class RegistrySessionController
{
  public static final long serialVersionUID = 1234283350799L;
  
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF login(ServletRequestIF req, @RequestParamter(name = "username") String username, @RequestParamter(name = "password") String password) throws JSONException
  {
    if (username != null)
    {
      username = username.trim();
    }

    Locale[] locales = this.getLocales(req);
    
//    Locale sessionLocale = req.getLocale();
//
//    JSONArray installedLocalesArr = new JSONArray();
//    Collection<Locale> installedLocales = LocalizationFacade.getInstalledLocales();
//    for (Locale loc : installedLocales)
//    {
//      JSONObject locObj = new JSONObject();
//      locObj.put("language", loc.getDisplayLanguage(sessionLocale));
//      locObj.put("country", loc.getDisplayCountry(sessionLocale));
//      locObj.put("name", loc.getDisplayName(sessionLocale));
//      locObj.put("variant", loc.getDisplayVariant(sessionLocale));
//
//      installedLocalesArr.put(locObj);
//    }
    
    ClientRequestIF clientRequest = loginWithLocales(req, username, password, locales);
    
    JSONArray jaLocales = new JSONArray(ServiceFactory.getRegistryService().getLocales(clientRequest.getSessionId()).toString());

    JsonArray roles = JsonParser.parseString(RoleViewDTO.getCurrentRoles(clientRequest)).getAsJsonArray();
    JsonArray roleDisplayLabels = JsonParser.parseString(RoleViewDTO.getCurrentRoleDisplayLabels(clientRequest)).getAsJsonArray();
    
    JsonObject cookieValue = new JsonObject();
    cookieValue.addProperty("loggedIn", clientRequest.isLoggedIn());
    cookieValue.add("roles", roles);
    cookieValue.add("roleDisplayLabels", roleDisplayLabels);
    cookieValue.addProperty("userName", username);
    cookieValue.addProperty("version", ClientConfigurationService.getServerVersion());

    CookieResponse response = new CookieResponse("user", -1, cookieValue.toString());
    response.set("installedLocales", jaLocales);

    return response;
  }

  public ClientRequestIF loginWithLocales(ServletRequestIF req, String username, String password, Locale[] locales)
  {
    try
    {
      WebClientSession clientSession = WebClientSession.createUserSession(username, password, locales);
      ClientRequestIF clientRequest = clientSession.getRequest();

      req.getSession().setMaxInactiveInterval(CommonProperties.getSessionTime());
      req.getSession().setAttribute(ClientConstants.CLIENTSESSION, clientSession);
      req.setAttribute(ClientConstants.CLIENTREQUEST, clientRequest);

      ClientConfigurationService.handleSessionEvent(new SessionEvent(EventType.LOGIN_SUCCESS, clientRequest, username));

      return clientRequest;
    }
    catch (RuntimeException e)
    {
      ClientConfigurationService.handleSessionEvent(new SessionEvent(EventType.LOGIN_FAILURE, null, username));

      throw e;
    }
  }

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
      
//      final Locale sessionLocale = Session.getCurrentLocale();
//      
//      JsonArray installedLocalesArr = new JsonArray();
//      Set<SupportedLocaleIF> installedLocales = LocalizationFacade.getSupportedLocales();
//      for (SupportedLocaleIF supportedLocale : installedLocales)
//      {
//        Locale locale = supportedLocale.getLocale();
//        
//        JsonObject locObj = new JsonObject();
//        locObj.addProperty("language", locale.getDisplayLanguage(sessionLocale));
//        locObj.addProperty("country", locale.getDisplayCountry(sessionLocale));
//        locObj.addProperty("name", locale.getDisplayName(sessionLocale));
//        locObj.addProperty("variant", locale.getDisplayVariant(sessionLocale));
//
//        installedLocalesArr.add(locObj);
//      }
      
      JsonArray jaLocales = ServiceFactory.getRegistryService().getLocales(clientRequest.getSessionId());
      cookieJson.add("installedLocales", jaLocales);
      
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
  
  public Locale[] getLocales(ServletRequestIF req)
  {
    Enumeration<Locale> enumeration = req.getLocales();
    List<Locale> locales = new LinkedList<Locale>();

    while (enumeration.hasMoreElements())
    {
      locales.add(enumeration.nextElement());
    }

    return locales.toArray(new Locale[locales.size()]);
  }
}
