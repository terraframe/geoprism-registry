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

import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAOIF;
import com.runwaysdk.business.rbac.SingleActorDAOIF;

import net.geoprism.registry.Organization;
import net.geoprism.registry.roles.GeoObjectAddChildPermissionException;
import net.geoprism.registry.roles.GeoObjectRemoveChildPermissionException;
import net.geoprism.registry.roles.GeoObjectViewRelationshipPermissionException;

public class GeoObjectRelationshipPermissionService implements GeoObjectRelationshipPermissionServiceIF
{
  protected void enforceActorHasPermission(SingleActorDAOIF actor, String orgCode, String parentTypeCode, String childTypeCode, Operation op, boolean isChangeRequest)
  {
    if (!this.doesActorHavePermission(actor, orgCode, parentTypeCode, childTypeCode, op, isChangeRequest))
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

  protected boolean doesActorHavePermission(SingleActorDAOIF actor, String orgCode, String parentTypeCode, String childTypeCode, Operation op, boolean isChangeRequest)
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

          if (RegistryRole.Type.isRA_Role(roleName))
          {
            return orgCode.equals(roleOrgCode) || op.equals(Operation.READ_CHILD);
          }
          else if (RegistryRole.Type.isRM_Role(roleName) && orgCode.equals(roleOrgCode))
          {
            String gotCode = RegistryRole.Type.parseGotCode(roleName);

            if (parentTypeCode == null || childTypeCode == null || gotCode.equals(parentTypeCode) || gotCode.equals(childTypeCode))
            {
              return true;
            }
          }
          else if (RegistryRole.Type.isRC_Role(roleName) && orgCode.equals(roleOrgCode))
          {
            if (isChangeRequest)
            {
              return true;
            }

            String gotCode = RegistryRole.Type.parseGotCode(roleName);

            if (parentTypeCode != null && gotCode.equals(parentTypeCode))
            {
              return true;
            }

            if (childTypeCode != null && gotCode.equals(childTypeCode))
            {
              return true;
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
  public boolean canAddChild(SingleActorDAOIF actor, String orgCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    return this.doesActorHavePermission(actor, orgCode, parentGeoObjectTypeCode, childGeoObjectTypeCode, Operation.ADD_CHILD, false);
  }

  @Override
  public void enforceCanAddChild(SingleActorDAOIF actor, String orgCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    this.enforceActorHasPermission(actor, orgCode, parentGeoObjectTypeCode, childGeoObjectTypeCode, Operation.ADD_CHILD, false);
  }

  @Override
  public boolean canAddChildCR(SingleActorDAOIF actor, String orgCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    return this.doesActorHavePermission(actor, orgCode, parentGeoObjectTypeCode, childGeoObjectTypeCode, Operation.ADD_CHILD, true);
  }

  @Override
  public void enforceCanAddChildCR(SingleActorDAOIF actor, String orgCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    this.enforceActorHasPermission(actor, orgCode, parentGeoObjectTypeCode, childGeoObjectTypeCode, Operation.ADD_CHILD, true);
  }

  @Override
  public boolean canRemoveChild(SingleActorDAOIF actor, String orgCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    return this.doesActorHavePermission(actor, orgCode, parentGeoObjectTypeCode, childGeoObjectTypeCode, Operation.DELETE_CHILD, false);
  }

  @Override
  public void enforceCanRemoveChild(SingleActorDAOIF actor, String orgCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    this.enforceActorHasPermission(actor, orgCode, parentGeoObjectTypeCode, childGeoObjectTypeCode, Operation.DELETE_CHILD, false);
  }

  @Override
  public boolean canRemoveChildCR(SingleActorDAOIF actor, String orgCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    return this.doesActorHavePermission(actor, orgCode, parentGeoObjectTypeCode, childGeoObjectTypeCode, Operation.DELETE_CHILD, true);
  }

  @Override
  public void enforceCanRemoveChildCR(SingleActorDAOIF actor, String orgCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    this.enforceActorHasPermission(actor, orgCode, parentGeoObjectTypeCode, childGeoObjectTypeCode, Operation.DELETE_CHILD, true);
  }

  @Override
  public boolean canViewChild(SingleActorDAOIF actor, String orgCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    return this.doesActorHavePermission(actor, orgCode, parentGeoObjectTypeCode, childGeoObjectTypeCode, Operation.READ_CHILD, false);
  }

  @Override
  public void enforceCanViewChild(SingleActorDAOIF actor, String orgCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    this.enforceActorHasPermission(actor, orgCode, parentGeoObjectTypeCode, childGeoObjectTypeCode, Operation.READ_CHILD, false);
  }
}
