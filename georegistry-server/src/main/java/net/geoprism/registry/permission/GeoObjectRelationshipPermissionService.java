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

import java.util.Set;

import org.commongeoregistry.adapter.metadata.RegistryRole;

import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAOIF;
import com.runwaysdk.business.rbac.SingleActorDAOIF;

import net.geoprism.registry.Organization;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.roles.GeoObjectAddChildPermissionException;
import net.geoprism.registry.roles.GeoObjectRemoveChildPermissionException;
import net.geoprism.registry.roles.GeoObjectViewRelationshipPermissionException;

public class GeoObjectRelationshipPermissionService extends UserPermissionService implements GeoObjectRelationshipPermissionServiceIF
{
  protected void enforceActorHasPermission(String orgCode, ServerGeoObjectType parentType, ServerGeoObjectType childType, Operation op, boolean isChangeRequest)
  {
    if (!this.doesActorHavePermission(orgCode, parentType, childType, op, isChangeRequest))
    {
      Organization org = Organization.getByCode(orgCode);

      if (op.equals(Operation.ADD_CHILD))
      {
        GeoObjectAddChildPermissionException ex = new GeoObjectAddChildPermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        throw ex;
      }
      else if (op.equals(Operation.DELETE_CHILD))
      {
        GeoObjectRemoveChildPermissionException ex = new GeoObjectRemoveChildPermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        throw ex;
      }
      else if (op.equals(Operation.READ_CHILD))
      {
        GeoObjectViewRelationshipPermissionException ex = new GeoObjectViewRelationshipPermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        throw ex;
      }
    }
  }

  protected boolean hasDirectPermission(String orgCode, ServerGeoObjectType parentType, ServerGeoObjectType childType, Operation op, boolean isChangeRequest)
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

        if (RegistryRole.Type.isOrgRole(roleName) && !RegistryRole.Type.isRootOrgRole(roleName))
        {
          String roleOrgCode = RegistryRole.Type.parseOrgCode(roleName);

          if (op.equals(Operation.READ_CHILD) && (childType != null && !childType.getIsPrivate()))
          {
            return true;
          }
          
          if ( roleOrgCode.equals(orgCode) )
          {
            if ( RegistryRole.Type.isRA_Role(roleName) )
            {
                return true;
            }
            else if ( RegistryRole.Type.isRM_Role(roleName) || RegistryRole.Type.isRC_Role(roleName) || RegistryRole.Type.isAC_Role(roleName) )
            {
              String roleGotCode = RegistryRole.Type.parseGotCode(roleName);
              
              if ( childType == null || childType.getCode().equals(roleGotCode) )
              {
                if ( RegistryRole.Type.isRM_Role(roleName) )
                {
                  return true;
                }
                else if ( RegistryRole.Type.isRC_Role(roleName)  )
                {
                  if ( isChangeRequest || op.equals(Operation.READ_CHILD) )
                  {
                    return true;
                  }
                }
                else if ( RegistryRole.Type.isAC_Role(roleName) )
                {
                  if ( op.equals(Operation.READ_CHILD) )
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

  protected boolean doesActorHavePermission(String orgCode, ServerGeoObjectType parentType, ServerGeoObjectType childType, Operation op, boolean isChangeRequest)
  {
    boolean permission = this.hasDirectPermission(orgCode, parentType, childType, op, isChangeRequest);

    if (!permission)
    {
      ServerGeoObjectType superType = childType.getSuperType();

      if (superType != null)
      {
        permission = this.hasDirectPermission(orgCode, parentType, superType, op, isChangeRequest);
      }
    }

    return permission;
  }

  @Override
  public boolean canAddChild(String orgCode, ServerGeoObjectType parentType, ServerGeoObjectType childType)
  {
    return this.doesActorHavePermission(orgCode, parentType, childType, Operation.ADD_CHILD, false);
  }

  @Override
  public void enforceCanAddChild(String orgCode, ServerGeoObjectType parentType, ServerGeoObjectType childType)
  {
    this.enforceActorHasPermission(orgCode, parentType, childType, Operation.ADD_CHILD, false);
  }

  @Override
  public boolean canAddChildCR(String orgCode, ServerGeoObjectType parentType, ServerGeoObjectType childType)
  {
    return this.doesActorHavePermission(orgCode, parentType, childType, Operation.ADD_CHILD, true);
  }

  @Override
  public void enforceCanAddChildCR(String orgCode, ServerGeoObjectType parentType, ServerGeoObjectType childType)
  {
    this.enforceActorHasPermission(orgCode, parentType, childType, Operation.ADD_CHILD, true);
  }

  @Override
  public boolean canRemoveChild(String orgCode, ServerGeoObjectType parentType, ServerGeoObjectType childType)
  {
    return this.doesActorHavePermission(orgCode, parentType, childType, Operation.DELETE_CHILD, false);
  }

  @Override
  public void enforceCanRemoveChild(String orgCode, ServerGeoObjectType parentType, ServerGeoObjectType childType)
  {
    this.enforceActorHasPermission(orgCode, parentType, childType, Operation.DELETE_CHILD, false);
  }

  @Override
  public boolean canRemoveChildCR(String orgCode, ServerGeoObjectType parentType, ServerGeoObjectType childType)
  {
    return this.doesActorHavePermission(orgCode, parentType, childType, Operation.DELETE_CHILD, true);
  }

  @Override
  public void enforceCanRemoveChildCR(String orgCode, ServerGeoObjectType parentType, ServerGeoObjectType childType)
  {
    this.enforceActorHasPermission(orgCode, parentType, childType, Operation.DELETE_CHILD, true);
  }

  @Override
  public boolean canViewChild(String orgCode, ServerGeoObjectType parentType, ServerGeoObjectType childType)
  {
    return this.doesActorHavePermission(orgCode, parentType, childType, Operation.READ_CHILD, false);
  }

  @Override
  public void enforceCanViewChild(String orgCode, ServerGeoObjectType parentType, ServerGeoObjectType childType)
  {
    this.enforceActorHasPermission(orgCode, parentType, childType, Operation.READ_CHILD, false);
  }
}
