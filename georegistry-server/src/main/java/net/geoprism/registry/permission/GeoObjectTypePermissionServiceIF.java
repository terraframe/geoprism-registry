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

import com.runwaysdk.business.rbac.Operation;

public interface GeoObjectTypePermissionServiceIF
{

  public boolean canRead(String orgCode, String gotCode, boolean isPrivate);

  public void enforceCanRead(String orgCode, String gotCode, boolean isPrivate, String gotLabel);

  public boolean canWrite(String orgCode, String gotCode, boolean isPrivate);

  public void enforceCanWrite(String orgCode, String gotCode, boolean isPrivate, String gotLabel);

  public boolean canCreate(String orgCode, String gotCode, boolean isPrivate);

  public void enforceCanCreate(String orgCode, String gotCode, boolean isPrivate);

  public boolean canDelete(String orgCode, String gotCode, boolean isPrivate);

  public void enforceCanDelete(String orgCode, String gotCode, boolean isPrivate, String gotLabel);

  public void enforceActorHasPermission(String orgCode, String gotCode, boolean isPrivate, String gotLabel, Operation op);

}
