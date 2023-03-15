package net.geoprism.registry.account;

import org.springframework.stereotype.Component;

import net.geoprism.GeoprismUser;
import net.geoprism.forgotpassword.business.ForgotPasswordBusinessService;
import net.geoprism.registry.UserInfo;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.session.ForgotPasswordOnOauthUser;

@Component
public class ForgotPasswordCGRService extends ForgotPasswordBusinessService
{
  @Override
  protected void validateInitiate(GeoprismUser user)
  {
    UserInfo userInfo = UserInfo.getByUser(user);
    
    if (userInfo.getExternalSystemOid() != null && userInfo.getExternalSystemOid().length() > 0)
    {
      ExternalSystem system = ExternalSystem.get(userInfo.getExternalSystemOid());
      
      ForgotPasswordOnOauthUser ex = new ForgotPasswordOnOauthUser();
      ex.setUsername(user.getUsername());
      ex.setOauthServer(system.getDisplayLabel().getValue());
      throw ex;
    }
  }
}
