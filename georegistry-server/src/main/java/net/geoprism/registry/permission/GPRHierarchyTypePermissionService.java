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
package net.geoprism.registry.permission;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.commongeoregistry.adapter.metadata.RegistryRole;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.business.rbac.RoleDAOIF;
import com.runwaysdk.business.rbac.SingleActorDAOIF;

import net.geoprism.graphrepo.permission.HierarchyTypePermissionServiceIF;
import net.geoprism.graphrepo.permission.RepoPermissionActionIF;
import net.geoprism.graphrepo.permission.UserPermissionService;
import net.geoprism.graphrepo.permission.UserPermissionService.RepoPermissionAction;
import net.geoprism.registry.Organization;
import net.geoprism.registry.roles.CreateHierarchyPermissionException;
import net.geoprism.registry.roles.DeleteHierarchyPermissionException;
import net.geoprism.registry.roles.ReadHierarchyPermissionException;
import net.geoprism.registry.roles.UpdateHierarchyPermissionException;

@Service
@Primary
public class GPRHierarchyTypePermissionService extends UserPermissionService implements HierarchyTypePermissionServiceIF
{
  public void enforceActorHasPermission(String orgCode, RepoPermissionAction action)
  {
    if (!this.getPermissions(orgCode).contains(action))
    {
      Organization org = Organization.getByCode(orgCode);

      if (action.equals(RepoPermissionAction.WRITE))
      {
        UpdateHierarchyPermissionException ex = new UpdateHierarchyPermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        throw ex;
      }
      else if (action.equals(RepoPermissionAction.READ))
      {
        ReadHierarchyPermissionException ex = new ReadHierarchyPermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        throw ex;
      }
      else if (action.equals(RepoPermissionAction.DELETE))
      {
        DeleteHierarchyPermissionException ex = new DeleteHierarchyPermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        throw ex;
      }
      else if (action.equals(RepoPermissionAction.CREATE))
      {
        CreateHierarchyPermissionException ex = new CreateHierarchyPermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        throw ex;
      }
    }
  }
  
  public Set<RepoPermissionActionIF> getPermissions(String orgCode)
  {
    if (!this.hasSessionUser()) // null actor is assumed to be SYSTEM
    {
      return new HashSet<RepoPermissionActionIF>(Arrays.asList(RepoPermissionAction.values()));
    }
    
//    final String orgCode = sht.getOrganization().getCode();
    
    HashSet<RepoPermissionActionIF> actions = new HashSet<RepoPermissionActionIF>();
    
    actions.add(RepoPermissionAction.READ);
    
    if (orgCode != null)
    {
      SingleActorDAOIF actor = this.getSessionUser();
      Set<RoleDAOIF> roles = actor.authorizedRoles();

      for (RoleDAOIF role : roles)
      {
        String roleName = role.getRoleName();

        if (RegistryRole.Type.isOrgRole(roleName) && !RegistryRole.Type.isRootOrgRole(roleName))
        {
          String roleOrgCode = RegistryRole.Type.parseOrgCode(roleName);

          if (orgCode.equals(roleOrgCode))
          {
            if (RegistryRole.Type.isRA_Role(roleName))
            {
              actions.add(RepoPermissionAction.WRITE);
              actions.add(RepoPermissionAction.CREATE);
              actions.add(RepoPermissionAction.DELETE);
            }
          }
        }
        else if (RegistryRole.Type.isSRA_Role(roleName))
        {
          actions.add(RepoPermissionAction.WRITE);
          actions.add(RepoPermissionAction.CREATE);
          actions.add(RepoPermissionAction.DELETE);
        }
      }
    }
    
    return actions;
  }

  @Override
  public boolean canRead(String orgCode)
  {
    return this.getPermissions(orgCode).contains(RepoPermissionAction.READ);
  }

  @Override
  public void enforceCanRead(String orgCode)
  {
    this.enforceActorHasPermission(orgCode, RepoPermissionAction.READ);
  }

  @Override
  public boolean canWrite(String orgCode)
  {
    return this.getPermissions(orgCode).contains(RepoPermissionAction.WRITE);
  }

  @Override
  public void enforceCanWrite(String orgCode)
  {
    this.enforceActorHasPermission(orgCode, RepoPermissionAction.WRITE);
  }

  @Override
  public boolean canCreate(String orgCode)
  {
    return this.getPermissions(orgCode).contains(RepoPermissionAction.CREATE);
  }

  @Override
  public void enforceCanCreate(String orgCode)
  {
    this.enforceActorHasPermission(orgCode, RepoPermissionAction.CREATE);
  }

  @Override
  public boolean canDelete(String orgCode)
  {
    return this.getPermissions(orgCode).contains(RepoPermissionAction.DELETE);
  }

  @Override
  public void enforceCanDelete(String orgCode)
  {
    this.enforceActorHasPermission(orgCode, RepoPermissionAction.DELETE);
  }

}
