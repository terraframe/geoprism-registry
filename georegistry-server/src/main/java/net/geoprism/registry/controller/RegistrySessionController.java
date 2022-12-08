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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.RunwayException;
import com.runwaysdk.constants.ClientConstants;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.request.RequestDecorator;
import com.runwaysdk.web.WebClientSession;

import net.geoprism.ClientConfigurationService;
import net.geoprism.RoleViewDTO;
import net.geoprism.SessionController;
import net.geoprism.SessionEvent;
import net.geoprism.SessionEvent.EventType;
import net.geoprism.account.LocaleSerializer;
import net.geoprism.account.OauthServerIF;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.spring.JsonObjectDeserializer;
import net.geoprism.session.RegistrySessionServiceDTO;

@RestController
@Validated
public class RegistrySessionController extends RunwaySpringController
{
  public static class LoginBody
  {
    @NotEmpty
    String username;

    @NotEmpty
    String password;

    public String getUsername()
    {
      return username;
    }

    public void setUsername(String username)
    {
      this.username = username;
    }

    public String getPassword()
    {
      return password;
    }

    public void setPassword(String password)
    {
      this.password = password;
    }
  }
  
  public static class OauthLoginBody
  {
    @NotEmpty
    String code;

    @NotNull
    @JsonDeserialize(using = JsonObjectDeserializer.class)    
    JsonObject state;

    public String getCode()
    {
      return code;
    }

    public void setCode(String code)
    {
      this.code = code;
    }
    
    public JsonObject getState()
    {
      return state;
    }
    
    public void setState(JsonObject state)
    {
      this.state = state;
    }
  }
  
  public static final String API_PATH = "cgrsession";

  
  @PostMapping(API_PATH + "/login")
  public ResponseEntity<String> login(HttpServletResponse response, HttpServletRequest req, @Valid @RequestBody LoginBody body) throws UnsupportedEncodingException 
  {
    if (body.username != null)
    {
      body.username = body.username.trim();
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
    
    ClientRequestIF clientRequest = loginWithLocales(req, body.username, body.password, locales);
    
    JSONArray jaLocales = new JSONArray(ServiceFactory.getRegistryService().getLocales(clientRequest.getSessionId()).toString());

    JsonArray roles = JsonParser.parseString(RoleViewDTO.getCurrentRoles(clientRequest)).getAsJsonArray();
    JsonArray roleDisplayLabels = JsonParser.parseString(RoleViewDTO.getCurrentRoleDisplayLabels(clientRequest)).getAsJsonArray();

    // Create the cookie
    JsonObject cookieValue = new JsonObject();
    cookieValue.addProperty("loggedIn", clientRequest.isLoggedIn());
    cookieValue.add("roles", roles);
    cookieValue.add("roleDisplayLabels", roleDisplayLabels);
    cookieValue.addProperty("userName", body.username);
    cookieValue.addProperty("version", ClientConfigurationService.getServerVersion());
    
    this.addCookie(response, req, cookieValue);

    JSONObject object = new JSONObject();
    object.put("installedLocales", jaLocales);

    return new ResponseEntity<String>(object.toString(), HttpStatus.OK);
  }

  public ClientRequestIF loginWithLocales(HttpServletRequest req, String username, String password, Locale[] locales)
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

  @PostMapping(API_PATH + "/ologin")  
  public String ologin(HttpServletResponse response, HttpServletRequest request, @Valid @RequestBody OauthLoginBody body) 
  {
    RequestDecorator req = new RequestDecorator(request);
    
    final SessionController geoprism = new SessionController();
    
//    URL url = new URL(req.getScheme(), req.getServerName(), req.getServerPort(), req.getContextPath());
//
//    String redirect = url.toString();

    String serverId = body.state.get(OauthServerIF.SERVER_ID).getAsString();

    Locale[] locales = geoprism.getLocales(req);

    WebClientSession clientSession = WebClientSession.createAnonymousSession(locales);

    try
    {
      ClientRequestIF clientRequest = clientSession.getRequest();

      String cgrSessionJsonString = RegistrySessionServiceDTO.ologin(clientRequest, serverId, body.code, LocaleSerializer.serialize(locales), null);
      
      JsonObject cgrSessionJson = (JsonObject) JsonParser.parseString(cgrSessionJsonString);
      final String sessionId = cgrSessionJson.get("sessionId").getAsString();
      final String username = cgrSessionJson.get("username").getAsString();
      
      geoprism.createSession(req, sessionId, locales);
      clientRequest = (ClientRequestIF) req.getAttribute(ClientConstants.CLIENTREQUEST);

      JsonArray roles = (JsonArray) JsonParser.parseString(RoleViewDTO.getCurrentRoles(clientRequest));
      JsonArray roleDisplayLabels = (JsonArray) JsonParser.parseString(RoleViewDTO.getCurrentRoleDisplayLabels(clientRequest));
      
      JsonArray jaLocales = ServiceFactory.getRegistryService().getLocales(clientRequest.getSessionId());
      
      JsonObject cookieJson = new JsonObject();
      cookieJson.addProperty("loggedIn", clientRequest.isLoggedIn());
      cookieJson.add("roles", roles);
      cookieJson.add("roleDisplayLabels", roleDisplayLabels);
      cookieJson.addProperty("userName", username);
      cookieJson.addProperty("version", ClientConfigurationService.getServerVersion());
      cookieJson.add("installedLocales", jaLocales);
            
      this.addCookie(response, request, cookieJson);
      
      return "redirect:/";
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
      
      return "redirect:/cgr/manage#/login/" + errorMessage;

    }
    finally
    {
      clientSession.logout();
    }
  }
  
  public Locale[] getLocales(HttpServletRequest req)
  {
    Enumeration<Locale> enumeration = req.getLocales();
    List<Locale> locales = new LinkedList<Locale>();

    while (enumeration.hasMoreElements())
    {
      locales.add(enumeration.nextElement());
    }

    return locales.toArray(new Locale[locales.size()]);
  }
  
  private void addCookie(HttpServletResponse response, HttpServletRequest req, JsonObject cookieValue) throws UnsupportedEncodingException
  {
    String path = req.getContextPath();

    if (path.equals("") || path.length() == 0)
    {
      path = "/";
    }

    final String value = URLEncoder.encode(cookieValue.toString(), "UTF-8");
    
    Cookie cookie = new Cookie("user", value);
    cookie.setMaxAge(-1);
    cookie.setPath(path);    
    
    response.addCookie(cookie);
  }

}
