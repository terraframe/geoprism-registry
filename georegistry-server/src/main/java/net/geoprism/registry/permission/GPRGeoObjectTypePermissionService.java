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

import java.util.HashSet;
import java.util.Set;

import org.commongeoregistry.adapter.metadata.RegistryRole;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.business.rbac.RoleDAOIF;
import com.runwaysdk.business.rbac.SingleActorDAOIF;

import net.geoprism.graphrepo.permission.GeoObjectTypePermissionServiceIF;
import net.geoprism.graphrepo.permission.RepoPermissionActionIF;
import net.geoprism.graphrepo.permission.UserPermissionService;
import net.geoprism.graphrepo.permission.UserPermissionService.RepoPermissionAction;
import net.geoprism.registry.Organization;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.roles.CreateGeoObjectTypePermissionException;
import net.geoprism.registry.roles.DeleteGeoObjectTypePermissionException;
import net.geoprism.registry.roles.ReadGeoObjectTypePermissionException;
import net.geoprism.registry.roles.WriteGeoObjectTypePermissionException;

@Service
@Primary
public class GPRGeoObjectTypePermissionService extends UserPermissionService implements GeoObjectTypePermissionServiceIF
{
  /**
   * Action must be one of: - WRITE (Update) - READ - DELETE - CREATE
   * 
   * @param action
   */
  public void enforceActorHasPermission(String orgCode, ServerGeoObjectType got, boolean isPrivate, RepoPermissionActionIF action)
  {
    if (!this.doesActorHavePermission(orgCode, got, isPrivate, action))
    {
      Organization org = Organization.getByCode(orgCode);

      if (action.equals(RepoPermissionAction.WRITE))
      {
        WriteGeoObjectTypePermissionException ex = new WriteGeoObjectTypePermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        ex.setGeoObjectType(got.getLabel().getValue());
        throw ex;
      }
      else if (action.equals(RepoPermissionAction.READ))
      {
        ReadGeoObjectTypePermissionException ex = new ReadGeoObjectTypePermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        ex.setGeoObjectType(got.getLabel().getValue());
        throw ex;
      }
      else if (action.equals(RepoPermissionAction.DELETE))
      {
        DeleteGeoObjectTypePermissionException ex = new DeleteGeoObjectTypePermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        ex.setGeoObjectType(got.getLabel().getValue());
        throw ex;
      }
      else if (action.equals(RepoPermissionAction.CREATE))
      {
        CreateGeoObjectTypePermissionException ex = new CreateGeoObjectTypePermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        throw ex;
      }
      else
      {
        throw new UnsupportedOperationException();
      }
    }
  }

  protected boolean doesActorHavePermission(String orgCode, ServerGeoObjectType got, boolean isPrivate, RepoPermissionActionIF action)
  {
    if (this.hasSessionUser())
    {
      boolean permission = this.hasDirectPermission(orgCode, got, isPrivate, action);

      if (!permission && got != null)
      {
        ServerGeoObjectType superType = got.getSuperType();

        if (superType != null)
        {
          permission = this.hasDirectPermission(orgCode, superType, isPrivate, action);
        }
      }

      return permission;
    }

    return true;
  }

  private boolean hasDirectPermission(String orgCode, ServerGeoObjectType got, boolean isPrivate, RepoPermissionActionIF action)
  {
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

          if (action.equals(RepoPermissionAction.READ) && !isPrivate)
          {
            return true;
          }

          if (roleOrgCode.equals(orgCode))
          {
            if (action.equals(RepoPermissionAction.READ) && isPrivate)
            {
              return true;
            }

            if (RegistryRole.Type.isRA_Role(roleName))
            {
              return true;
            }
            else if (RegistryRole.Type.isRM_Role(roleName) || RegistryRole.Type.isRC_Role(roleName) || RegistryRole.Type.isAC_Role(roleName))
            {
              String roleGotCode = RegistryRole.Type.parseGotCode(roleName);

              if (got != null && got.getCode().equals(roleGotCode))
              {
                if (RegistryRole.Type.isRM_Role(roleName))
                {
                  if (action.equals(RepoPermissionAction.READ))
                  {
                    return true;
                  }
                }
                else if (RegistryRole.Type.isRC_Role(roleName))
                {
                  if (action.equals(RepoPermissionAction.READ)) // ||
                                                               // isChangeRequest
                  {
                    return true;
                  }
                }
                else if (RegistryRole.Type.isAC_Role(roleName))
                {
                  if (action.equals(RepoPermissionAction.READ))
                  {
                    return true;
                  }
                }
              }
            }
          }
        }
        else if (RegistryRole.Type.isSRA_Role(roleName))
        {
          return true;
        }
      }
    }

    return false;
  }

  public Set<RepoPermissionActionIF> getPermissions(ServerGeoObjectType got)
  {
    final String orgCode = got.getOrganization().getCode();
    final Boolean isPrivate = got.getIsPrivate();

    HashSet<RepoPermissionActionIF> actions = new HashSet<RepoPermissionActionIF>();

    if (this.canRead(orgCode, got, isPrivate))
    {
      actions.add(RepoPermissionAction.READ);
    }

    if (this.canWrite(orgCode, got, isPrivate))
    {
      actions.add(RepoPermissionAction.WRITE);
    }

    if (this.canCreate(orgCode, got, isPrivate))
    {
      actions.add(RepoPermissionAction.CREATE);
    }

    if (this.canDelete(orgCode, got, isPrivate))
    {
      actions.add(RepoPermissionAction.DELETE);
    }

    return actions;
  }

  @Override
  public boolean canRead(String orgCode, ServerGeoObjectType got, boolean isPrivate)
  {
    return this.doesActorHavePermission(orgCode, got, isPrivate, RepoPermissionAction.READ);
  }

  @Override
  public void enforceCanRead(String orgCode, ServerGeoObjectType got, boolean isPrivate)
  {
    this.enforceActorHasPermission(orgCode, got, isPrivate, RepoPermissionAction.READ);
  }

  @Override
  public boolean canWrite(String orgCode, ServerGeoObjectType got, boolean isPrivate)
  {
    return this.doesActorHavePermission(orgCode, got, isPrivate, RepoPermissionAction.WRITE);
  }

  @Override
  public void enforceCanWrite(String orgCode, ServerGeoObjectType got, boolean isPrivate)
  {
    this.enforceActorHasPermission(orgCode, got, isPrivate, RepoPermissionAction.WRITE);
  }

  @Override
  public boolean canCreate(String orgCode, ServerGeoObjectType got, boolean isPrivate)
  {
    return this.doesActorHavePermission(orgCode, got, isPrivate, RepoPermissionAction.CREATE);
  }

  @Override
  public void enforceCanCreate(String orgCode, boolean isPrivate)
  {
    this.enforceActorHasPermission(orgCode, null, isPrivate, RepoPermissionAction.CREATE);
  }

  @Override
  public boolean canDelete(String orgCode, ServerGeoObjectType got, boolean isPrivate)
  {
    return this.doesActorHavePermission(orgCode, got, isPrivate, RepoPermissionAction.DELETE);
  }

  @Override
  public void enforceCanDelete(String orgCode, ServerGeoObjectType got, boolean isPrivate)
  {
    this.enforceActorHasPermission(orgCode, got, isPrivate, RepoPermissionAction.DELETE);
  }

}
