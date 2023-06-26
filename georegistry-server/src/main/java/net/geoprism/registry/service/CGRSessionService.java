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
package net.geoprism.registry.service;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import net.geoprism.registry.RegistryVersionProperties;
import net.geoprism.session.SessionService;

@Component
public class CGRSessionService extends SessionService
{
  @Override
  public JsonArray getInstalledLocales(String sessionId)
  {
    return JsonParser.parseString(ServiceFactory.getRegistryService().getLocales(sessionId).toString()).getAsJsonArray();
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
    
    return endpoints;
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
