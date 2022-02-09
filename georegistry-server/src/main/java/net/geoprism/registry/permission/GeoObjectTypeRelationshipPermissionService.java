/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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

import java.util.Collection;
import java.util.Set;

import org.commongeoregistry.adapter.metadata.RegistryRole;

import com.runwaysdk.business.ontology.Term;
import com.runwaysdk.business.rbac.RoleDAOIF;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.system.gis.geo.AllowedIn;
import com.runwaysdk.system.gis.geo.LocatedIn;

import net.geoprism.registry.Organization;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.roles.HierarchyRelationshipPermissionException;

public class GeoObjectTypeRelationshipPermissionService extends UserPermissionService implements GeoObjectTypeRelationshipPermissionServiceIF
{
  public enum RelationshipAction {

  }

  public boolean doesActorHaveRelationshipPermission(ServerHierarchyType ht, ServerGeoObjectType parentType, ServerGeoObjectType childType, boolean allowRC)
  {
    boolean permission = this.directRelationshipPermission(ht, parentType, childType, allowRC);

    if (!permission)
    {
      ServerGeoObjectType superType = childType.getSuperType();

      if (superType != null)
      {
        permission = this.directRelationshipPermission(ht, parentType, superType, allowRC);
      }
    }

    return permission;
  }

  private boolean directRelationshipPermission(ServerHierarchyType ht, ServerGeoObjectType parentType, ServerGeoObjectType childType, boolean allowRC)
  {
    if (!this.hasSessionUser()) // null actor is assumed to be SYSTEM
    {
      return true;
    }

    if (ht.getMdTermRelationship().getKey().equals(AllowedIn.CLASS) || ht.getMdTermRelationship().getKey().equals(LocatedIn.CLASS))
    {
      return true; // AllowedIn is deprecated and should not be used by the
      // end-user.
    }

    Organization thisOrg = ht.getOrganization();

    if (thisOrg != null)
    {
      SingleActorDAOIF actor = this.getSessionUser();

      String thisOrgCode = thisOrg.getCode();

      Set<RoleDAOIF> roles = actor.authorizedRoles();

      for (RoleDAOIF role : roles)
      {
        String roleName = role.getRoleName();

        if (RegistryRole.Type.isOrgRole(roleName) && !RegistryRole.Type.isRootOrgRole(roleName))
        {
          String orgCode = RegistryRole.Type.parseOrgCode(roleName);

          if (RegistryRole.Type.isRA_Role(roleName) && orgCode.equals(thisOrgCode))
          {
            return true;
          }
          else if (RegistryRole.Type.isRM_Role(roleName) && orgCode.equals(thisOrgCode))
          {
            String gotCode = RegistryRole.Type.parseGotCode(roleName);

            if (parentType == null || childType == null // Null parent / child
            // is used for
            // GeoObjectEditor
            // widget
                || gotCode.equals(parentType.getCode()) || gotCode.equals(childType.getCode()))
            {
              return true;
            }
          }
          else if (allowRC && RegistryRole.Type.isRC_Role(roleName) && orgCode.equals(thisOrgCode))
          {
            String gotCode = RegistryRole.Type.parseGotCode(roleName);

            if (gotCode.equals(parentType.getCode()) || gotCode.equals(childType.getCode()))
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
  public Collection<CGRPermissionActionIF> getPermissions(ServerGeoObjectType serverGeoObjectType)
  {
    // TODO
    throw new UnsupportedOperationException();
  }

  /**
   * Throws an exception if the provided actor does not have permissions to this
   * HierarchyType.
   */
  public void enforceActorHasRelationshipPermission(ServerHierarchyType ht, ServerGeoObjectType parentType, ServerGeoObjectType childType, boolean allowRC)
  {
    if (!this.doesActorHaveRelationshipPermission(ht, parentType, childType, allowRC))
    {
      String parentLabel;
      if (parentType == null || parentType.getCode().equals(Term.ROOT_KEY))
      {
        parentLabel = LocalizationFacade.localize("hierarchy.rootNode");
      }
      else
      {
        parentLabel = parentType.getLabel().getValue();
      }
      String childLabel = childType.getLabel().getValue();

      HierarchyRelationshipPermissionException ex = new HierarchyRelationshipPermissionException();
      ex.setParentGeoObjectType(parentLabel);
      ex.setChildGeoObjectType(childLabel);
      ex.setOrganization(ht.getOrganization().getDisplayLabel().getValue());
      throw ex;
    }
  }

  @Override
  public boolean canAddChild(ServerHierarchyType ht, ServerGeoObjectType parentType, ServerGeoObjectType childType)
  {
    return this.doesActorHaveRelationshipPermission(ht, parentType, childType, false);
  }

  @Override
  public void enforceCanAddChild(ServerHierarchyType ht, ServerGeoObjectType parentType, ServerGeoObjectType childType)
  {
    this.enforceActorHasRelationshipPermission(ht, parentType, childType, false);
  }

  @Override
  public boolean canRemoveChild(ServerHierarchyType ht, ServerGeoObjectType parentType, ServerGeoObjectType childType)
  {
    return this.doesActorHaveRelationshipPermission(ht, parentType, childType, false);
  }

  @Override
  public void enforceCanRemoveChild(ServerHierarchyType ht, ServerGeoObjectType parentType, ServerGeoObjectType childType)
  {
    this.enforceActorHasRelationshipPermission(ht, parentType, childType, false);
  }
}
