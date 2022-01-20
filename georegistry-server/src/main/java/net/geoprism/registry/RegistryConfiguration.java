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
package net.geoprism.registry;

import com.runwaysdk.dataaccess.MdRelationshipDAOIF;
import com.runwaysdk.dataaccess.metadata.MdRelationshipDAO;
import com.runwaysdk.system.gis.geo.AllowedIn;
import com.runwaysdk.system.gis.geo.LocatedIn;

import net.geoprism.ConfigurationIF;
import net.geoprism.DefaultConfiguration;
import net.geoprism.ForgotPasswordRequest;
import net.geoprism.GeoprismUser;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.session.ForgotPasswordOnOauthUser;

public class RegistryConfiguration extends DefaultConfiguration implements ConfigurationIF
{
  
  @Override
  public String getGeoEntityRelationship(MdRelationshipDAOIF mdRelationshipDAOIF)
  {
    if (mdRelationshipDAOIF.definesType().equals(AllowedIn.CLASS))
    {
      return MdRelationshipDAO.getMdRelationshipDAO(LocatedIn.CLASS).getOid();
    }

    try
    {
      String code = ServerHierarchyType.buildHierarchyKeyFromMdTermRelUniversal(mdRelationshipDAOIF.getKey());
      ServerHierarchyType hierarchyType = ServerHierarchyType.get(code);

      return hierarchyType.getEntityRelationship().getOid();
    }
    catch (Exception e)
    {
      return null;
    }
  }
  
  @Override
  public void onInitiateForgotPasswordForUser(GeoprismUser user, ForgotPasswordRequest req)
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
