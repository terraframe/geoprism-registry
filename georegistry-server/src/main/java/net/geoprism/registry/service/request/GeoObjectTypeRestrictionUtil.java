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
package net.geoprism.registry.service.request;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.commongeoregistry.adapter.metadata.RegistryRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.business.rbac.RoleDAOIF;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.session.Session;

import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.service.permission.RolePermissionService;

/**
 * A general utility class for adding graph sql conditions for GeoObjectTypes.
 * 
 * @author rrowlands
 */
@Service
public class GeoObjectTypeRestrictionUtil
{
  @Autowired
  private RolePermissionService          permissions;

  @Autowired
  private GeoObjectTypeBusinessServiceIF service;

  public String buildTypeWritePermissionsFilter(String orgColumnAlias, String gotCodeAlias)
  {
    if (this.permissions.isSRA())
    {
      return "";
    }

    List<String> conditions = hasMandateOnType(orgColumnAlias, gotCodeAlias, false);

    if (conditions.size() > 0)
    {
      return "(" + StringUtils.join(conditions, " OR ") + ")";
    }
    else
    {
      return "";
    }
  }

  public String buildTypeReadPermissionsFilter(String orgColumnAlias, String gotCodeAlias)
  {
    if (this.permissions.isSRA())
    {
      return "";
    }

    final List<String> privateTypes = ServerGeoObjectType.getAll().stream().filter(type -> type.getIsPrivate()).map(type -> "'" + type.getCode() + "'").collect(Collectors.toList());

    StringBuilder builder = new StringBuilder();

    builder.append("(");

    // Must be a public type
    builder.append(gotCodeAlias + " NOT IN [" + StringUtils.join(privateTypes, ", ") + "]");

    // Or they have CGR mandate permissions on this type
    List<String> conditions = hasMandateOnType(orgColumnAlias, gotCodeAlias, true);
    if (conditions.size() > 0)
    {
      builder.append(" OR (" + StringUtils.join(conditions, " OR ") + ")");
    }

    builder.append(")");

    return builder.toString();
  }

  public List<String> hasMandateOnType(String orgCodeAttr, String gotCodeAttr, boolean allowRC)
  {
    List<String> criteria = new ArrayList<String>();
    List<String> raOrgs = new ArrayList<String>();
    List<String> goRoles = new ArrayList<String>();

    SingleActorDAOIF actor = Session.getCurrentSession().getUser();
    for (RoleDAOIF role : actor.authorizedRoles())
    {
      String roleName = role.getRoleName();

      if (RegistryRole.Type.isOrgRole(roleName) && !RegistryRole.Type.isRootOrgRole(roleName))
      {
        if (RegistryRole.Type.isRA_Role(roleName))
        {
          String roleOrgCode = RegistryRole.Type.parseOrgCode(roleName);
          raOrgs.add(roleOrgCode);
        }
        else if (RegistryRole.Type.isRM_Role(roleName))
        {
          goRoles.add(roleName);
        }
        else if (allowRC && RegistryRole.Type.isRC_Role(roleName))
        {
          goRoles.add(roleName);
        }
      }
    }

    for (String orgCode : raOrgs)
    {
      criteria.add("(" + orgCodeAttr + " = '" + orgCode + "')");
    }

    for (String roleName : goRoles)
    {
      String roleOrgCode = RegistryRole.Type.parseOrgCode(roleName);
      String gotCode = RegistryRole.Type.parseGotCode(roleName);

      criteria.add("(" + orgCodeAttr + " = '" + roleOrgCode + "' AND " + gotCodeAttr + " = '" + gotCode + "')");

      // If they have permission to an abstract parent type, then they also have
      // permission to all its children.
      ServerGeoObjectType op = ServerGeoObjectType.get(gotCode);

      if (op != null && op.getIsAbstract())
      {
        List<ServerGeoObjectType> subTypes = this.service.getSubtypes(op);

        for (ServerGeoObjectType subType : subTypes)
        {
          criteria.add("(" + orgCodeAttr + " = '" + subType.getOrganization().getCode() + "' AND " + gotCodeAttr + " = '" + subType.getCode() + "')");
        }
      }
    }

    return criteria;
  }
}
