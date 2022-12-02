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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.commongeoregistry.adapter.metadata.RegistryRole;

import com.runwaysdk.business.rbac.RoleDAOIF;
import com.runwaysdk.business.rbac.SingleActorDAOIF;

import net.geoprism.registry.Organization;
import net.geoprism.registry.OrganizationRAException;
import net.geoprism.registry.OrganizationRMException;
import net.geoprism.registry.SRAException;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.roles.RAException;

public class RolePermissionService extends UserPermissionService
{
  public boolean isSRA()
  {
    if (!this.hasSessionUser())
    {
      return true;
    }

    SingleActorDAOIF actor = this.getSessionUser();

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

  public void enforceSRA()
  {
    if (!isSRA())
    {
      SRAException ex = new SRAException();
      throw ex;
    }
  }

  public boolean isRA()
  {
    return isRA(null);
  }

  public boolean isRA(String orgCode)
  {
    if (!this.hasSessionUser())
    {
      return true;
    }

    SingleActorDAOIF actor = this.getSessionUser();

    Set<RoleDAOIF> roles = actor.authorizedRoles();

    for (RoleDAOIF role : roles)
    {
      String roleName = role.getRoleName();

      if (RegistryRole.Type.isRA_Role(roleName))
      {
        String roleOrgCode = RegistryRole.Type.parseOrgCode(roleName);

        if (orgCode != null && orgCode.equals(roleOrgCode))
        {
          return true;
        }
        else if (orgCode == null)
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

  public void enforceRA()
  {
    enforceRA(null);
  }

  public void enforceRA(String orgCode)
  {
    if (!isRA(orgCode))
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

  public boolean isRM()
  {
    return isRM(null, null);
  }

  public boolean isRM(String orgCode)
  {
    return isRM(orgCode, null);
  }

  public boolean isRM(String orgCode, ServerGeoObjectType type)
  {
    if (!this.hasSessionUser())
    {
      return true;
    }

    SingleActorDAOIF actor = this.getSessionUser();

    Set<RoleDAOIF> roles = actor.authorizedRoles();
    Set<String> typeCodes = this.getTypeCodes(type);

    for (RoleDAOIF role : roles)
    {
      String roleName = role.getRoleName();

      if (RegistryRole.Type.isRM_Role(roleName))
      {
        String roleOrgCode = RegistryRole.Type.parseOrgCode(roleName);
        String roleGotCode = RegistryRole.Type.parseGotCode(roleName);

        if (orgCode != null && type != null && ( orgCode.equals(roleOrgCode) && typeCodes.contains(roleGotCode) ))
        {
          return true;
        }
        else if (type == null && orgCode != null && orgCode.equals(roleOrgCode))
        {
          return true;
        }
        else if (type == null && orgCode == null)
        {
          return true;
        }
      }
    }

    return false;
  }

  public boolean isRC()
  {
    return this.isRC(null, null);
  }

  public boolean isRC(String orgCode)
  {
    return this.isRC(orgCode, null);
  }

  public boolean isRC(String orgCode, ServerGeoObjectType type)
  {
    if (!this.hasSessionUser())
    {
      return true;
    }

    SingleActorDAOIF actor = this.getSessionUser();

    Set<RoleDAOIF> roles = actor.authorizedRoles();
    Set<String> typeCodes = this.getTypeCodes(type);

    for (RoleDAOIF role : roles)
    {
      String roleName = role.getRoleName();

      if (RegistryRole.Type.isRC_Role(roleName))
      {
        String roleOrgCode = RegistryRole.Type.parseOrgCode(roleName);
        String roleGotCode = RegistryRole.Type.parseGotCode(roleName);

        if (orgCode != null && type != null && ( orgCode.equals(roleOrgCode) && typeCodes.contains(roleGotCode) ))
        {
          return true;
        }
        else if (type == null && orgCode != null && orgCode.equals(roleOrgCode))
        {
          return true;
        }
        else if (type == null && orgCode == null)
        {
          return true;
        }
      }
    }

    return false;
  }

  public boolean isAC()
  {
    return this.isAC(null, null);
  }

  public boolean isAC(String orgCode)
  {
    return this.isRC(orgCode, null);
  }

  public boolean isAC(String orgCode, ServerGeoObjectType type)
  {
    if (!this.hasSessionUser())
    {
      return true;
    }

    SingleActorDAOIF actor = this.getSessionUser();

    Set<RoleDAOIF> roles = actor.authorizedRoles();

    Set<String> typeCodes = this.getTypeCodes(type);

    for (RoleDAOIF role : roles)
    {
      String roleName = role.getRoleName();

      if (RegistryRole.Type.isAC_Role(roleName))
      {
        String roleOrgCode = RegistryRole.Type.parseOrgCode(roleName);
        String roleGotCode = RegistryRole.Type.parseGotCode(roleName);

        if (orgCode != null && type != null && ( orgCode.equals(roleOrgCode) && typeCodes.contains(roleGotCode) ))
        {
          return true;
        }
        else if (type == null && orgCode != null && orgCode.equals(roleOrgCode))
        {
          return true;
        }
        else if (type == null && orgCode == null)
        {
          return true;
        }
      }
    }

    return false;
  }

  private Set<String> getTypeCodes(ServerGeoObjectType type)
  {
    Set<String> typeCodes = new TreeSet<String>();

    if (type != null)
    {
      typeCodes.add(type.getCode());

      ServerGeoObjectType superType = type.getSuperType();

      if (superType != null)
      {
        typeCodes.add(superType.getCode());
      }
    }
    return typeCodes;
  }

  public void enforceRM()
  {
    enforceRM(null, null);
  }

  public void enforceRM(String orgCode, ServerGeoObjectType type)
  {
    if (!isRM(orgCode, type))
    {
      if (type != null && orgCode != null && orgCode.equals(""))
      {
        Organization org = Organization.getByCode(orgCode);

        OrganizationRMException ex = new OrganizationRMException();
        ex.setOrganizationLabel(org.getDisplayLabel().getValue());
        ex.setGeoObjectTypeLabel(type.getLabel().getValue());
        throw ex;
      }
      else if (orgCode != null && orgCode.equals(""))
      {
        Organization org = Organization.getByCode(orgCode);

        OrganizationRMException ex = new OrganizationRMException();
        ex.setOrganizationLabel(org.getDisplayLabel().getValue());
        throw ex;
      }
      else
      {
        OrganizationRMException ex = new OrganizationRMException();
        throw ex;
      }
    }
  }

  /**
   * If the session user is an org role, this method will return the user's
   * organization. Otherwise this method will return null.
   */
  public String getOrganization()
  {
    if (this.hasSessionUser())
    {
      SingleActorDAOIF actor = this.getSessionUser();

      Set<RoleDAOIF> roles = actor.authorizedRoles();

      for (RoleDAOIF role : roles)
      {
        String roleName = role.getRoleName();

        if (RegistryRole.Type.isOrgRole(roleName) && !RegistryRole.Type.isRootOrgRole(roleName))
        {
          String roleOrgCode = RegistryRole.Type.parseOrgCode(roleName);

          return roleOrgCode;
        }
      }
    }

    return null;
  }

  /**
   * If the session user is a role, this method will return the user's
   * GeoObjectType. Otherwise this method will return null.
   */
  public List<String> getRMGeoObjectTypes()
  {
    List<String> types = new ArrayList<String>();

    SingleActorDAOIF actor = this.getSessionUser();

    Set<RoleDAOIF> roles = actor.authorizedRoles();

    for (RoleDAOIF role : roles)
    {
      String roleName = role.getRoleName();

      if (RegistryRole.Type.isOrgRole(roleName) && RegistryRole.Type.isRM_Role(roleName))
      {
        String gotCode = RegistryRole.Type.parseGotCode(roleName);

        types.add(gotCode);
      }
    }

    return types;
  }
}
