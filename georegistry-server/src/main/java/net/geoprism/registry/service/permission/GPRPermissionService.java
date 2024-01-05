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
package net.geoprism.registry.service.permission;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import net.geoprism.registry.BusinessType;
import net.geoprism.registry.model.ServerOrganization;

@Service
@Primary
public class GPRPermissionService extends PermissionService implements PermissionServiceIF
{
  @Autowired
  private RolePermissionService rolePermissions;

  @Override
  public boolean canWrite(BusinessType type)
  {
    ServerOrganization organization = ServerOrganization.get(type.getOrganization());

    return rolePermissions.isRA(organization.getCode()) || rolePermissions.isSRA();
  }

  @Override
  public boolean canRead(BusinessType type)
  {
    ServerOrganization organization = ServerOrganization.get(type.getOrganization());

    return ServerOrganization.isMember(organization) || rolePermissions.isSRA();
  }

  @Override
  public boolean isAdmin(ServerOrganization organization)
  {
    return rolePermissions.isRA(organization.getCode()) || rolePermissions.isSRA();
  }

  @Override
  public boolean isMember(ServerOrganization organization)
  {
    return ServerOrganization.isMember(organization) || rolePermissions.isSRA();
  }

}
