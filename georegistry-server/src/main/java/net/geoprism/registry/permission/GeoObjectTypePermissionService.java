/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.permission;

import java.util.Set;

import org.commongeoregistry.adapter.metadata.RegistryRole;

import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAOIF;
import com.runwaysdk.business.rbac.SingleActorDAOIF;

import net.geoprism.registry.Organization;
import net.geoprism.registry.roles.CreateGeoObjectTypePermissionException;
import net.geoprism.registry.roles.DeleteGeoObjectTypePermissionException;
import net.geoprism.registry.roles.ReadGeoObjectTypePermissionException;
import net.geoprism.registry.roles.WriteGeoObjectTypePermissionException;

public class GeoObjectTypePermissionService extends UserPermissionService implements GeoObjectTypePermissionServiceIF
{
  /**
   * Operation must be one of: - WRITE (Update) - READ - DELETE - CREATE
   * 
   * @param op
   */
  public void enforceActorHasPermission(String orgCode, boolean isPrivate, String gotLabel, Operation op)
  {
    if (!this.doesActorHavePermission(orgCode, isPrivate, op, null))
    {
      Organization org = Organization.getByCode(orgCode);

      if (op.equals(Operation.WRITE))
      {
        WriteGeoObjectTypePermissionException ex = new WriteGeoObjectTypePermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        ex.setGeoObjectType(gotLabel);
        throw ex;
      }
      else if (op.equals(Operation.READ))
      {
        ReadGeoObjectTypePermissionException ex = new ReadGeoObjectTypePermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        ex.setGeoObjectType(gotLabel);
        throw ex;
      }
      else if (op.equals(Operation.DELETE))
      {
        DeleteGeoObjectTypePermissionException ex = new DeleteGeoObjectTypePermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        ex.setGeoObjectType(gotLabel);
        throw ex;
      }
      else if (op.equals(Operation.CREATE))
      {
        CreateGeoObjectTypePermissionException ex = new CreateGeoObjectTypePermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        throw ex;
      }
    }
  }

  protected boolean doesActorHavePermission(String orgCode, boolean isPrivate, Operation op, PermissionContext context)
  {
    if (!this.hasSessionUser()) // null actor is assumed to be SYSTEM
    {
      return true;
    }

    if (orgCode != null)
    {
      SingleActorDAOIF actor = this.getSessionUser();

      Set<RoleDAOIF> roles = actor.authorizedRoles();

      for (RoleDAOIF role : roles)
      {
        String roleName = role.getRoleName();

        if ( RegistryRole.Type.isOrgRole(roleName) && !RegistryRole.Type.isRootOrgRole(roleName) )
        {
          String roleOrgCode = RegistryRole.Type.parseOrgCode(roleName);

          if ( RegistryRole.Type.isRA_Role(roleName) && orgCode.equals(roleOrgCode) )
          {
            return true;
          }
          else if (op.equals(Operation.READ))
          {
            if ( RegistryRole.Type.isRA_Role(roleName) || RegistryRole.Type.isRM_Role(roleName) || RegistryRole.Type.isAC_Role(roleName) || RegistryRole.Type.isRC_Role(roleName) )
            {
              if (context != null && context.equals(PermissionContext.WRITE))
              {
                if (orgCode.equals(roleOrgCode))
                {
                  return true;
                }
              }
              else if ( !isPrivate || (isPrivate && orgCode.equals(roleOrgCode)) )
              {
                return true;
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

  @Override
  public boolean canRead(String orgCode, boolean isPrivate, PermissionContext context)
  {
    return this.doesActorHavePermission(orgCode, isPrivate, Operation.READ, context);
  }

  @Override
  public void enforceCanRead(String orgCode, boolean isPrivate, String gotLabel)
  {
    this.enforceActorHasPermission(orgCode, isPrivate, gotLabel, Operation.READ);
  }

  @Override
  public boolean canWrite(String orgCode, boolean isPrivate)
  {
    return this.doesActorHavePermission(orgCode, isPrivate, Operation.WRITE, null);
  }

  @Override
  public void enforceCanWrite(String orgCode, boolean isPrivate, String gotLabel)
  {
    this.enforceActorHasPermission(orgCode, isPrivate, gotLabel, Operation.WRITE);
  }

  @Override
  public boolean canCreate(String orgCode, boolean isPrivate)
  {
    return this.doesActorHavePermission(orgCode, isPrivate, Operation.CREATE, null);
  }

  @Override
  public void enforceCanCreate(String orgCode, boolean isPrivate)
  {
    this.enforceActorHasPermission(orgCode, isPrivate, null, Operation.CREATE);
  }

  @Override
  public boolean canDelete(String orgCode, boolean isPrivate)
  {
    return this.doesActorHavePermission(orgCode, isPrivate, Operation.DELETE, null);
  }

  @Override
  public void enforceCanDelete(String orgCode, boolean isPrivate, String gotLabel)
  {
    this.enforceActorHasPermission(orgCode, isPrivate, gotLabel, Operation.DELETE);
  }

}
