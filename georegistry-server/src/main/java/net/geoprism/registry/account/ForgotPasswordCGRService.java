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
