/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism(tm).
 *
 * Geoprism(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.controller;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.request.ServletRequestIF;
import com.runwaysdk.web.WebClientSession;

import net.geoprism.CookieResponse;
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
    
    URL url = new URL(req.getScheme(), req.getServerName(), req.getServerPort(), req.getContextPath());

    String redirect = url.toString();

    JSONObject stateObject = new JSONObject(state);
    String serverId = stateObject.getString(OauthServerIF.SERVER_ID);

    Locale[] locales = geoprism.getLocales(req);

    WebClientSession clientSession = WebClientSession.createAnonymousSession(locales);

    try
    {
      ClientRequestIF clientRequest = clientSession.getRequest();

      String sessionId = RegistrySessionServiceDTO.ologin(clientRequest, serverId, code, LocaleSerializer.serialize(locales), redirect);

      geoprism.createSession(req, sessionId, locales);

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
}
