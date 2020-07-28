/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.permission;

import java.util.Set;

import org.commongeoregistry.adapter.metadata.RegistryRole;

import com.runwaysdk.business.rbac.RoleDAOIF;
import com.runwaysdk.business.rbac.SingleActorDAOIF;

import net.geoprism.registry.Organization;
import net.geoprism.registry.OrganizationRAException;
import net.geoprism.registry.SRAException;
import net.geoprism.registry.roles.RAException;

public class RolePermissionService
{
  public boolean isSRA(SingleActorDAOIF actor)
  {
    if (actor == null)
    {
      return true;
    }
    
    Set<RoleDAOIF> roles = actor.authorizedRoles();
    
    for (RoleDAOIF role : roles)
    {
      String roleName = role.getRoleName();
      
      if (RegistryRole.Type.isSRA_Role(roleName))
      {
        return true;
      }
    }
    
    return false;
  }
  
  public void enforceSRA(SingleActorDAOIF actor)
  {
    if (!isSRA(actor))
    {
      SRAException ex = new SRAException();
      throw ex;
    }
  }
  
  public boolean isRA(SingleActorDAOIF actor)
  {
    return isRA(actor, null);
  }
  
  public boolean isRA(SingleActorDAOIF actor, String orgCode)
  {
    if (actor == null)
    {
      return true;
    }
    
    Set<RoleDAOIF> roles = actor.authorizedRoles();
    
    for (RoleDAOIF role : roles)
    {
      String roleName = role.getRoleName();
      
      if (RegistryRole.Type.isRA_Role(roleName))
      {
        String roleOrgCode = RegistryRole.Type.parseOrgCode(roleName);
        
        if (orgCode != null)
        {
          return orgCode.equals(roleOrgCode);
        }
        else
        {
          return true;
        }
      }
      else if (RegistryRole.Type.isSRA_Role(roleName))
      {
        return true;
      }
    }
    
    return false;
  }
  
  public void enforceRA(SingleActorDAOIF actor)
  {
    enforceRA(actor, null);
  }
  
  public void enforceRA(SingleActorDAOIF actor, String orgCode)
  {
    if (!isRA(actor, orgCode))
    {
      if (orgCode != null)
      {
        Organization org = Organization.getByCode(orgCode);
        
        OrganizationRAException ex = new OrganizationRAException();
        ex.setOrganizationLabel(org.getDisplayLabel().getValue());
        throw ex;
      }
      else
      {
        RAException ex = new RAException();
        throw ex;
      }
    }
  }
}
