package net.geoprism.registry.session;

import org.springframework.stereotype.Component;

import com.runwaysdk.business.rbac.UserDAO;

import net.geoprism.GeoprismUser;
import net.geoprism.account.OauthServer;
import net.geoprism.configuration.GeoprismProperties;
import net.geoprism.externalprofile.business.ExternalProfileBusinessService;
import net.geoprism.registry.UserInfo;
import net.geoprism.registry.graph.DHIS2ExternalSystem;
import net.geoprism.registry.graph.ExternalSystem;

@Component
public class ExternalProfileCGRService extends ExternalProfileBusinessService
{
  @Override
  protected void validateUser(String username, UserDAO user, GeoprismUser geoprismUser, OauthServer server)
  {
    UserInfo userInfo = UserInfo.getByUser(geoprismUser);
    
    ExternalSystem system = ExternalSystem.get(userInfo.getExternalSystemOid());
    
    if (system instanceof DHIS2ExternalSystem)
    {
      DHIS2ExternalSystem dhis2System = (DHIS2ExternalSystem) system;
      
      if (dhis2System.getOauthServerOid().equals(server.getOid()))
      {
        return;
      }
    }
    
    UserNotOuathEnabledException ex = new UserNotOuathEnabledException();
    ex.setUsername(geoprismUser.getUsername());
    ex.setOauthServer(server.getDisplayLabel().getValue());
    throw ex;
  }
  
  @Override
  protected String buildRedirectURI()
  {
    return GeoprismProperties.getRemoteServerUrl() + "api/session/ologin";
  }
}
