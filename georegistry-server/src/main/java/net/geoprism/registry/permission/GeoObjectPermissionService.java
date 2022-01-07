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

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.commongeoregistry.adapter.metadata.RegistryRole;

import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAOIF;
import com.runwaysdk.business.rbac.SingleActorDAOIF;

import net.geoprism.registry.Organization;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.roles.CreateGeoObjectPermissionException;
import net.geoprism.registry.roles.DeleteGeoObjectPermissionException;
import net.geoprism.registry.roles.ReadGeoObjectPermissionException;
import net.geoprism.registry.roles.WriteGeoObjectPermissionException;
import net.geoprism.registry.service.ServiceFactory;

public class GeoObjectPermissionService extends UserPermissionService implements GeoObjectPermissionServiceIF
{
  /**
   * Operation must be one of: - WRITE (Update) - READ - DELETE - CREATE
   * 
   * @param op
   */
  protected void enforceActorHasPermission(String orgCode, ServerGeoObjectType type, Operation op, boolean allowRC)
  {
    if (!this.doesActorHavePermission(orgCode, type, op, allowRC))
    {
      Organization org = Organization.getByCode(orgCode);

      if (op.equals(Operation.WRITE))
      {
        WriteGeoObjectPermissionException ex = new WriteGeoObjectPermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        ex.setGeoObjectType(type.getLabel().getValue());
        throw ex;
      }
      else if (op.equals(Operation.READ))
      {
        ReadGeoObjectPermissionException ex = new ReadGeoObjectPermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        ex.setGeoObjectType(type.getLabel().getValue());
        throw ex;
      }
      else if (op.equals(Operation.DELETE))
      {
        DeleteGeoObjectPermissionException ex = new DeleteGeoObjectPermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        ex.setGeoObjectType(type.getLabel().getValue());
        throw ex;
      }
      else if (op.equals(Operation.CREATE))
      {
        CreateGeoObjectPermissionException ex = new CreateGeoObjectPermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        ex.setGeoObjectType(type.getLabel().getValue());
        throw ex;
      }
    }
  }

  protected boolean doesActorHavePermission(String orgCode, ServerGeoObjectType type, Operation op, boolean isChangeRequest)
  {
    if (this.hasSessionUser())
    {
      SingleActorDAOIF actor = this.getSessionUser();

      boolean permission = this.hasDirectPermission(actor, orgCode, type, op, isChangeRequest);

      if (!permission)
      {
        ServerGeoObjectType superType = type.getSuperType();

        if (superType != null)
        {
          permission = this.hasDirectPermission(actor, orgCode, superType, op, isChangeRequest);
        }
      }

      return permission;
    }

    return true;
  }

  protected boolean hasDirectPermission(SingleActorDAOIF actor, String orgCode, ServerGeoObjectType type, Operation op, boolean isChangeRequest)
  {
    if (actor == null) // null actor is assumed to be SYSTEM
    {
      return true;
    }

    if (orgCode != null)
    {
      Set<RoleDAOIF> roles = actor.authorizedRoles();

      for (RoleDAOIF role : roles)
      {
        String roleName = role.getRoleName();

        if (RegistryRole.Type.isOrgRole(roleName) && !RegistryRole.Type.isRootOrgRole(roleName))
        {
          String roleOrgCode = RegistryRole.Type.parseOrgCode(roleName);

          if (op.equals(Operation.READ) && !type.getIsPrivate())
          {
            return true;
          }

          if (roleOrgCode.equals(orgCode))
          {
            if (RegistryRole.Type.isRA_Role(roleName))
            {
              return true;
            }
            else if (RegistryRole.Type.isRM_Role(roleName) || RegistryRole.Type.isRC_Role(roleName) || RegistryRole.Type.isAC_Role(roleName))
            {
              String roleGotCode = RegistryRole.Type.parseGotCode(roleName);

              if (type.getCode().equals(roleGotCode))
              {
                if (RegistryRole.Type.isRM_Role(roleName))
                {
                  return true;
                }
                else if (RegistryRole.Type.isRC_Role(roleName))
                {
                  if (isChangeRequest || op.equals(Operation.READ))
                  {
                    return true;
                  }
                }
                else if (RegistryRole.Type.isAC_Role(roleName))
                {
                  if (op.equals(Operation.READ))
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

  @Override
  public boolean canRead(String orgCode, ServerGeoObjectType type)
  {
    return this.doesActorHavePermission(orgCode, type, Operation.READ, true);
  }

  @Override
  public void enforceCanRead(String orgCode, ServerGeoObjectType type)
  {
    this.enforceActorHasPermission(orgCode, type, Operation.READ, true);
  }

  @Override
  public boolean canWrite(String orgCode, ServerGeoObjectType type)
  {
    return this.doesActorHavePermission(orgCode, type, Operation.WRITE, false);
  }

  @Override
  public void enforceCanWrite(String orgCode, ServerGeoObjectType type)
  {
    this.enforceActorHasPermission(orgCode, type, Operation.WRITE, false);
  }

  @Override
  public boolean canCreate(String orgCode, ServerGeoObjectType type)
  {
    return this.doesActorHavePermission(orgCode, type, Operation.CREATE, false);
  }

  @Override
  public void enforceCanCreate(String orgCode, ServerGeoObjectType type)
  {
    this.enforceActorHasPermission(orgCode, type, Operation.CREATE, false);
  }

  @Override
  public boolean canWriteCR(String orgCode, ServerGeoObjectType type)
  {
    return this.doesActorHavePermission(orgCode, type, Operation.WRITE, true);
  }

  @Override
  public void enforceCanWriteCR(String orgCode, ServerGeoObjectType type)
  {
    this.enforceActorHasPermission(orgCode, type, Operation.WRITE, true);
  }

  @Override
  public boolean canCreateCR(String orgCode, ServerGeoObjectType type)
  {
    return this.doesActorHavePermission(orgCode, type, Operation.CREATE, true);
  }

  @Override
  public void enforceCanCreateCR(String orgCode, ServerGeoObjectType gotCode)
  {
    this.enforceActorHasPermission(orgCode, gotCode, Operation.CREATE, true);
  }

  public List<String> getReadableTypes(String orgCode)
  {
    List<ServerGeoObjectType> types = ServiceFactory.getMetadataCache().getAllGeoObjectTypes();

    return types.stream().filter(type -> {
      return GeoObjectPermissionService.this.canRead(orgCode, type);
    }).collect(() -> new LinkedList<String>(), (list, element) -> list.add(element.getCode()), (listA, listB) -> {
    });
  }

  public List<String> getMandateTypes(String orgCode)
  {
    List<ServerGeoObjectType> types = ServiceFactory.getMetadataCache().getAllGeoObjectTypes();

    return types.stream().filter(type -> {
      return GeoObjectPermissionService.this.canWriteCR(orgCode, type);
    }).collect(() -> new LinkedList<String>(), (list, element) -> list.add(element.getCode()), (listA, listB) -> {
    });
  }
}
