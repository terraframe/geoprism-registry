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

import com.runwaysdk.business.rbac.Operation;

import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.permission.UserPermissionService.CGRPermissionAction;

public interface GeoObjectTypePermissionServiceIF
{

  public boolean canRead(String orgCode, ServerGeoObjectType got, boolean isPrivate);

  public void enforceCanRead(String orgCode, ServerGeoObjectType got, boolean isPrivate);

  public boolean canWrite(String orgCode, ServerGeoObjectType got, boolean isPrivate);

  public void enforceCanWrite(String orgCode, ServerGeoObjectType got, boolean isPrivate);

  public boolean canCreate(String orgCode, ServerGeoObjectType got, boolean isPrivate);

  public void enforceCanCreate(String orgCode, boolean isPrivate);

  public boolean canDelete(String orgCode, ServerGeoObjectType got, boolean isPrivate);

  public void enforceCanDelete(String orgCode, ServerGeoObjectType got, boolean isPrivate);

  public void enforceActorHasPermission(String orgCode, ServerGeoObjectType got, boolean isPrivate, CGRPermissionActionIF action);

  public Set<CGRPermissionActionIF> getPermissions(ServerGeoObjectType got);
  
}
