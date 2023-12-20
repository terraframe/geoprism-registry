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
package net.geoprism.registry.service.business;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.business.rbac.UserDAO;

import net.geoprism.GeoprismUser;
import net.geoprism.account.OauthServer;
import net.geoprism.configuration.GeoprismProperties;
import net.geoprism.registry.UserInfo;
import net.geoprism.registry.graph.DHIS2ExternalSystem;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.session.UserNotOuathEnabledException;

@Service
@Primary
public class GPRExternalProfileBusinessService extends ExternalProfileBusinessService implements ExternalProfileBusinessServiceIF
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
