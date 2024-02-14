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
package net.geoprism.registry.service.permission;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.metadata.RegistryRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionIF;

import net.geoprism.registry.Organization;
import net.geoprism.registry.OrganizationQuery;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.service.business.GPROrganizationBusinessService;

@Service
@Primary
public class GPROrganizationPermissionService extends UserPermissionService implements OrganizationPermissionServiceIF
{
  @Autowired
  private RolePermissionService          roleService;

  @Autowired
  private GPROrganizationBusinessService service;

  public void enforceActorCanCreate()
  {
    this.roleService.enforceSRA();
  }

  public void enforceActorCanUpdate()
  {
    this.roleService.enforceSRA();
  }

  public boolean canActorCreate()
  {
    return this.roleService.isSRA();
  }

  public boolean canActorUpdate()
  {
    return this.roleService.isSRA();
  }

  public void enforceActorCanDelete()
  {
    this.roleService.enforceSRA();
  }

  public boolean canActorDelete()
  {
    return this.roleService.isSRA();
  }

  public boolean canActorRead(String orgCode)
  {
    // People need to be able to read all organizations so that we can display
    // them in a read-only format
    // See comments at the bottom of ticket 206 for a specific usecase
    // (hierarchy manager read-only)

    // It's OK to return true here because the PUBLIC user won't actually have
    // Runway-level object
    // permissions on Organization.

    return true;
  }

  public void enforceActorCanRead(SingleActorDAOIF actor, String orgCode)
  {
    return;
  }

  /**
   * @param org
   * @return If the current user is part of the registry admin role for the
   *         given organization
   */
  public boolean isRegistryAdmin(ServerOrganization org)
  {
    if (new RolePermissionService().isSRA())
    {
      return true;
    }

    String roleName = RegistryRole.Type.getRA_RoleName( ( org.getCode() ));

    final SessionIF session = Session.getCurrentSession();

    if (session != null)
    {
      return session.userHasRole(roleName);
    }

    return true;
  }

  /**
   * @param org
   * @return If the current user is part of the registry admin role for the
   *         given organization
   */
  public boolean isRegistryMaintainer(ServerOrganization org)
  {
    if (new RolePermissionService().isSRA())
    {
      return true;
    }

    final SessionIF session = Session.getCurrentSession();

    if (session != null)
    {
      Map<String, ServerGeoObjectType> types = this.service.getGeoObjectTypes(org);

      Set<Entry<String, ServerGeoObjectType>> entries = types.entrySet();

      for (Entry<String, ServerGeoObjectType> entry : entries)
      {
        String roleName = RegistryRole.Type.getRM_RoleName(org.getCode(), entry.getKey());

        boolean hasRole = session.userHasRole(roleName);

        if (hasRole)
        {
          return true;
        }
      }

      return false;
    }

    return true;
  }

  public boolean isMemberOrSRA(ServerOrganization org)
  {
    if (new RolePermissionService().isSRA())
    {
      return true;
    }

    return ServerOrganization.isMember(org);
  }

  public boolean isMemberOrSRA(Organization org)
  {
    if (new RolePermissionService().isSRA())
    {
      return true;
    }

    return Organization.isMember(org);
  }

  /**
   * @param org
   * @return If the current user is part of the registry admin role for the
   *         given organization
   */
  public boolean isRegistryAdmin(Organization org)
  {
    if (new RolePermissionService().isSRA())
    {
      return true;
    }

    String roleName = RegistryRole.Type.getRA_RoleName( ( org.getCode() ));

    final SessionIF session = Session.getCurrentSession();

    if (session != null)
    {
      return session.userHasRole(roleName);
    }

    return true;
  }

  public List<Organization> getUserAdminOrganizations()
  {
    OrganizationQuery query = new OrganizationQuery(new QueryFactory());
    query.ORDER_BY_ASC(query.getDisplayLabel().localize());

    try (final OIterator<? extends Organization> iterator = query.getIterator())
    {
      final List<? extends Organization> orgs = iterator.getAll();

      List<Organization> result = orgs.stream().filter(o -> {
        return isRegistryAdmin(o);
      }).collect(Collectors.toList());

      return result;
    }
  }

  public List<ServerOrganization> getUserAdminServerOrganizations()
  {
    return getUserAdminOrganizations().stream().map(org -> ServerOrganization.get(org)).collect(Collectors.toList());
  }
}
