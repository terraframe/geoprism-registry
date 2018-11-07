/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
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
package net.geoprism;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.runwaysdk.ClientSession;
import com.runwaysdk.constants.ClientConstants;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.request.ServletRequestIF;
import com.runwaysdk.web.WebClientSession;

import net.geoprism.account.ExternalProfileDTO;
import net.geoprism.account.LocaleSerializer;
import net.geoprism.account.OauthServerIF;

@Controller(url = "session")
public class SessionController 
{
  public static final long   serialVersionUID = 1234283350799L;

  //
  // public ResponseIF form()
  // {
  // String errorMessage = this.req.getParameter("errorMessage");
  //
  // this.form(errorMessage);
  // }
  //
  // private ResponseIF form(String errorMessage)
  // {
  // Locale[] locales = ServletUtility.getLocales(req);
  //
  // CachedImageUtil.setBannerPath(this.req, this.resp);
  //
  // if (errorMessage != null)
  // {
  // req.setAttribute("errorMessage", errorMessage);
  // }
  //
  // WebClientSession clientSession = WebClientSession.createAnonymousSession(locales);
  //
  // try
  // {
  // ClientRequestIF clientRequest = clientSession.getRequest();
  //
  // OauthServerDTO[] servers = OauthServerDTO.getAll(clientRequest);
  //
  // req.setAttribute("servers", servers);
  // }
  // finally
  // {
  // clientSession.logout();
  // }
  //
  // req.getRequestDispatcher("/login.jsp").forward(req, resp);
  // }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF login(ServletRequestIF req, @RequestParamter(name = "username") String username, @RequestParamter(name = "password") String password) throws JSONException
  {
    if (username != null)
    {
      username = username.trim();
    }

    Locale[] locales = this.getLocales(req);

    WebClientSession clientSession = WebClientSession.createUserSession(username, password, locales);
    ClientRequestIF clientRequest = clientSession.getRequest();

    req.getSession().setMaxInactiveInterval(CommonProperties.getSessionTime());
    req.getSession().setAttribute(ClientConstants.CLIENTSESSION, clientSession);
    req.setAttribute(ClientConstants.CLIENTREQUEST, clientRequest);

    JSONArray roles = new JSONArray(RoleViewDTO.getCurrentRoles(clientRequest));

    CookieResponse response = new CookieResponse("user", -1);
    response.set("loggedIn", clientRequest.isLoggedIn());
    response.set("roles", roles);

    return response;
  }

  @Endpoint(method = ServletMethod.GET)
  public ResponseIF logout(ServletRequestIF req, ClientSession session)
  {
    // process which logs the user out.
    if (session != null)
    {
      session.logout();
    }

    req.getSession().removeAttribute(ClientConstants.CLIENTSESSION);
    req.getSession().invalidate();

    return new LogoutResponse(req.getContextPath() + "/", "user", 0);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF ologout(ServletRequestIF req, ClientSession session)
  {
    // process which logs the user out.
    if (session != null)
    {
      session.logout();
    }

    req.getSession().removeAttribute(ClientConstants.CLIENTSESSION);
    req.getSession().invalidate();

    return new CookieResponse("user", 0);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF ologin(ServletRequestIF req, @RequestParamter(name = "code") String code, @RequestParamter(name = "state") String state) throws MalformedURLException, JSONException
  {
    URL url = new URL(req.getScheme(), req.getServerName(), req.getServerPort(), req.getContextPath());

    String redirect = url.toString();

    JSONObject stateObject = new JSONObject(state);
    String serverId = stateObject.getString(OauthServerIF.SERVER_ID);

    Locale[] locales = this.getLocales(req);

    WebClientSession clientSession = WebClientSession.createAnonymousSession(locales);

    try
    {
      ClientRequestIF clientRequest = clientSession.getRequest();

      String sessionId = ExternalProfileDTO.login(clientRequest, serverId, code, LocaleSerializer.serialize(locales), redirect);

      this.createSession(req, sessionId, locales);

      JSONArray roles = new JSONArray(RoleViewDTO.getCurrentRoles(clientRequest));

      CookieResponse response = new CookieResponse("user", -1);
      response.set("loggedIn", clientRequest.isLoggedIn());
      response.set("roles", roles);

      return response;
    }
    finally
    {
      clientSession.logout();
    }
  }

  private void createSession(ServletRequestIF req, String sessionId, Locale[] locales)
  {
    WebClientSession clientSession = WebClientSession.getExistingSession(sessionId, locales);
    ClientRequestIF clientRequest = clientSession.getRequest();

    req.getSession().setMaxInactiveInterval(CommonProperties.getSessionTime());
    req.getSession().setAttribute(ClientConstants.CLIENTSESSION, clientSession);
    req.setAttribute(ClientConstants.CLIENTREQUEST, clientRequest);
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
