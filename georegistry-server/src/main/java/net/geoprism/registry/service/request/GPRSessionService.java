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
package net.geoprism.registry.service.request;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import net.geoprism.registry.RegistryVersionProperties;

@Service
@Primary
public class GPRSessionService extends SessionService implements SessionServiceIF
{
  @Autowired
  private RegistryComponentService service;

  @Override
  public JsonArray getInstalledLocales(String sessionId)
  {
    return JsonParser.parseString(service.getLocales(sessionId).toString()).getAsJsonArray();
  }

  @Override
  public Set<String> getPublicEndpoints()
  {
    Set<String> endpoints = super.getPublicEndpoints();
    endpoints.add("cgr/manage");
    endpoints.add("api/session/ologin");
    endpoints.add("api/session/login");
    endpoints.add("api/cgr/localization-map");
    endpoints.add("api/cgr/current-locale");
    endpoints.add("api/cgr/locales");
    endpoints.add("api/cgr/configuration");
    endpoints.add("api/asset/view");
    endpoints.add("api/oauth/get-public");
    endpoints.add("api/invite-user/initiate");
    endpoints.add("api/invite-user/complete");
    endpoints.add("api/registryaccount/newUserInstance");
    endpoints.add("api/forgotpassword/initiate");
    endpoints.add("api/forgotpassword/complete");
    endpoints.add("api/master-list/tile");

    // Public list management support
    endpoints.add("api/cgr/init");
    endpoints.add("api/list-type/entries");
    endpoints.add("api/list-type/list-for-type");

    // Public list support
    endpoints.add("api/list-type/version");
    endpoints.add("api/list-type/data");
    endpoints.add("api/list-type/export-shapefile");
    endpoints.add("api/list-type/export-spreadsheet");
    endpoints.add("api/geoobjecttype/get-all");
    endpoints.add("websocket/notify");
    endpoints.add("websocket/progress");

    // Public explorer support
    endpoints.add("api/list-type/fetchVersionsAsListVersion");
    endpoints.add("api/list-type/record");
    endpoints.add("api/list-type/tile");
    endpoints.add("api/list-type/bounds");
    endpoints.add("api/list-type/get-geospatial-versions");
    endpoints.add("glyphs/NotoSansRegular");

    // Public labeled property graph support
    endpoints.add("api/labeled-property-graph-type/get-all");
    endpoints.add("api/labeled-property-graph-type/get");
    endpoints.add("api/labeled-property-graph-type/entries");
    endpoints.add("api/labeled-property-graph-type/entry");
    endpoints.add("api/labeled-property-graph-type/versions");
    endpoints.add("api/labeled-property-graph-type/version");
    endpoints.add("api/labeled-property-graph-type/data");
    endpoints.add("api/labeled-property-graph-type/geo-objects");
    endpoints.add("api/labeled-property-graph-type/edges");

    // Public endpoint for changing locale
    endpoints.add("api/localization/set-locale");

    // Public endpoint for organization exports
    endpoints.add("api/organization/export-tree");
    
    return endpoints;
  }

  @Override
  public boolean pathAllowed(HttpServletRequest req)
  {
    String uri = req.getRequestURI();

    List<String> endpoints = new LinkedList<String>();

    // They're allowed to hit the login view page, otherwise its a redirect loop
    endpoints.add("loginRedirect");

    // They can also invoke the login action on SessionController @
    // session/form and session/login
    endpoints.add("session/form");
    endpoints.add("session/login");
    endpoints.add("session/ologin");

    for (String endpoint : endpoints)
    {
      if (uri.equals(req.getContextPath() + "/" + endpoint))
      {
        return true;
      }
    }

    List<String> directories = new LinkedList<String>();
    directories.add("jquery");
    directories.add("font-awesome");
    directories.add("fontawesome");
    directories.add("3rd-party");

    // Allow direct hitting of all page resources in login directories.
    directories.add("/net/geoprism/login");

    // Directory of uploaded images
    directories.add("uploaded_images/");

    for (String directory : directories)
    {
      if (uri.contains(directory))
      {
        return true;
      }
    }

    List<String> extensions = new LinkedList<String>();
    extensions.add(".sld");
    extensions.add(".css");
    extensions.add(".png");
    extensions.add(".jpg");
    extensions.add(".bmp");
    extensions.add(".jpeg");
    extensions.add(".gif");
    extensions.add(".svg");
    extensions.add(".pdf");
    extensions.add(".otf");
    extensions.add(".mp4");
    extensions.add(".ico");
    extensions.add(".woff2");
    extensions.add(".woff");
    extensions.add(".ttf");

    extensions.add(".js");

    for (String extension : extensions)
    {
      if (uri.endsWith(extension))
      {
        return true;
      }
    }

    return false;
  }

  @Override
  public String getHomeUrl()
  {
    return "/cgr/manage";
  }

  @Override
  public String getLoginUrl()
  {
    return "/cgr/manage#login";
  }

  @Override
  public String getServerVersion()
  {
    return RegistryVersionProperties.getInstance().getVersion();
  }
}
