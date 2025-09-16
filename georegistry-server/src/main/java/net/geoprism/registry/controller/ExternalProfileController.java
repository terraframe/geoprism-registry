/**
 * Copyright (c) 2023 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism(tm).
 *
 * Geoprism(tm) is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Geoprism(tm) is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

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

import net.geoprism.account.LocaleSerializer;
import net.geoprism.account.OauthServerIF;
import net.geoprism.rbac.RoleView;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.service.request.ExternalProfileServiceIF;
import net.geoprism.registry.service.request.RoleServiceIF;
import net.geoprism.registry.service.request.SessionServiceIF;

@Controller
@Validated
public class ExternalProfileController extends RunwaySpringController
{
  public static final String         API_PATH = RegistryConstants.CONTROLLER_ROOT + "session";

  @Autowired
  protected ExternalProfileServiceIF service;

  @Autowired
  protected RoleServiceIF            roleService;

  @Autowired
  protected SessionServiceIF         sessionService;

  @Autowired
  protected SessionController        sessionController;

  @GetMapping(API_PATH + "/ologout")
  public RedirectView ologout() throws IOException
  {
    return sessionController.logout();
  }

  @GetMapping(API_PATH + "/ologin")
  public RedirectView ologin(@RequestParam(required = true) String code, @RequestParam(required = true) String state)
  {
    final RequestDecorator req = new RequestDecorator(this.getRequest());
    final String contextPath = this.getRequest().getContextPath().contains("/") ? this.getRequest().getContextPath() + "/" : "/";

    String serverId = JsonParser.parseString(state).getAsJsonObject().get(OauthServerIF.SERVER_ID).getAsString();

    Locale[] locales = sessionController.getLocales();

    WebClientSession clientSession = WebClientSession.createAnonymousSession(locales);

    try
    {
      ClientRequestIF clientRequest = clientSession.getRequest();

      JsonObject ologinParams = new JsonObject();
      ologinParams.addProperty("serverId", serverId);
      ologinParams.addProperty("code", code);
      ologinParams.addProperty("locales", LocaleSerializer.serialize(locales));

      String sessionJsonString = this.service.ologin(clientRequest.getSessionId(), ologinParams.toString());

      JsonObject cgrSessionJson = (JsonObject) JsonParser.parseString(sessionJsonString);
      final String sessionId = cgrSessionJson.get("sessionId").getAsString();
      final String username = cgrSessionJson.get("username").getAsString();

      sessionController.createSession(req, sessionId, locales);
      clientRequest = (ClientRequestIF) req.getAttribute(ClientConstants.CLIENTREQUEST);

      Set<RoleView> roles = this.roleService.getCurrentRoles(clientRequest.getSessionId(), true);

      JsonArray roleNames = new JsonArray();
      roles.stream().forEach(role -> roleNames.add(role.getRoleName()));

      JsonArray roleDisplayLabels = new JsonArray();
      roles.stream().forEach(role -> roleDisplayLabels.add(role.getDisplayLabel()));

      JsonObject cookieJson = new JsonObject();
      cookieJson.addProperty("loggedIn", clientRequest.isSessionValid());
      cookieJson.add("roles", roleNames);
      cookieJson.add("roleDisplayLabels", roleDisplayLabels);
      cookieJson.addProperty("userName", username);
      cookieJson.addProperty("version", this.sessionService.getServerVersion());
      cookieJson.add("installedLocales", this.sessionService.getInstalledLocales(clientRequest.getSessionId()));

      sessionController.addCookie(cookieJson);

      return new RedirectView(contextPath);
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

      return new RedirectView(contextPath + "#/login/" + errorMessage);

    }
    finally
    {
      clientSession.logout();
    }
  }
}
