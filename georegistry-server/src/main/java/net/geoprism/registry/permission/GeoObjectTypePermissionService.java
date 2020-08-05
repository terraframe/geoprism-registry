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

public class GeoObjectTypePermissionService implements GeoObjectTypePermissionServiceIF
{
  /**
   * Operation must be one of: - WRITE (Update) - READ - DELETE - CREATE
   * 
   * @param actor
   * @param op
   */
  public void enforceActorHasPermission(SingleActorDAOIF actor, String orgCode, String gotLabel, Operation op)
  {
    if (!this.doesActorHavePermission(actor, orgCode, op, null))
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

  protected boolean doesActorHavePermission(SingleActorDAOIF actor, String orgCode, Operation op, PermissionContext context)
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

          if (RegistryRole.Type.isRA_Role(roleName) && ( orgCode.equals(roleOrgCode) ))
          {
            return true;
          }
          else if (op.equals(Operation.READ))
          {
            if ( ( RegistryRole.Type.isRA_Role(roleName) || RegistryRole.Type.isRM_Role(roleName) || RegistryRole.Type.isAC_Role(roleName) || RegistryRole.Type.isRC_Role(roleName) ))
            {
              if (context != null && context.equals(PermissionContext.WRITE))
              {
                if (orgCode.equals(roleOrgCode))
                {
                  return true;
                }
              }
              else
              {
                return true;
              }
            }
          }
        }
        // SRA only has the ability to see type and hierarchies, it does not
        // have permissions to modify
        else if (op.equals(Operation.READ) && RegistryRole.Type.isSRA_Role(roleName))
        {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  public boolean canRead(SingleActorDAOIF actor, String orgCode, PermissionContext context)
  {
    return this.doesActorHavePermission(actor, orgCode, Operation.READ, context);
  }

  @Override
  public void enforceCanRead(SingleActorDAOIF actor, String orgCode, String gotLabel)
  {
    this.enforceActorHasPermission(actor, orgCode, gotLabel, Operation.READ);
  }

  @Override
  public boolean canWrite(SingleActorDAOIF actor, String orgCode)
  {
    return this.doesActorHavePermission(actor, orgCode, Operation.WRITE, null);
  }

  @Override
  public void enforceCanWrite(SingleActorDAOIF actor, String orgCode, String gotLabel)
  {
    this.enforceActorHasPermission(actor, orgCode, gotLabel, Operation.WRITE);
  }

  @Override
  public boolean canCreate(SingleActorDAOIF actor, String orgCode)
  {
    return this.doesActorHavePermission(actor, orgCode, Operation.CREATE, null);
  }

  @Override
  public void enforceCanCreate(SingleActorDAOIF actor, String orgCode)
  {
    this.enforceActorHasPermission(actor, orgCode, null, Operation.CREATE);
  }

  @Override
  public boolean canDelete(SingleActorDAOIF actor, String orgCode)
  {
    return this.doesActorHavePermission(actor, orgCode, Operation.DELETE, null);
  }

  @Override
  public void enforceCanDelete(SingleActorDAOIF actor, String orgCode, String gotLabel)
  {
    this.enforceActorHasPermission(actor, orgCode, gotLabel, Operation.DELETE);
  }

}
