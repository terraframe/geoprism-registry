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
    
    // Public list support
//    endpoints.add("api/cgr/init");
    endpoints.add("api/list-type/version");
    endpoints.add("api/list-type/data");
    endpoints.add("api/list-type/export-shapefile");
    endpoints.add("api/list-type/export-spreadsheet");
    endpoints.add("api/geoobjecttype/get-all");
    endpoints.add("websocket/notify");
    endpoints.add("websocket/progress");
    
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
