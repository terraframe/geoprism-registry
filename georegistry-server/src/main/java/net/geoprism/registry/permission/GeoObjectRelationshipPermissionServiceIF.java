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

import net.geoprism.registry.model.ServerGeoObjectType;

public interface GeoObjectRelationshipPermissionServiceIF
{
  public boolean canAddChild(String orgCode, ServerGeoObjectType parentType, ServerGeoObjectType childType);

  public void enforceCanAddChild(String orgCode, ServerGeoObjectType parentType, ServerGeoObjectType childType);

  public boolean canViewChild(String orgCode, ServerGeoObjectType parentType, ServerGeoObjectType childType);

  public void enforceCanViewChild(String orgCode, ServerGeoObjectType parentType, ServerGeoObjectType childType);

  public boolean canAddChildCR(String orgCode, ServerGeoObjectType parentType, ServerGeoObjectType childType);

  public void enforceCanAddChildCR(String orgCode, ServerGeoObjectType parentType, ServerGeoObjectType childType);

  public boolean canRemoveChild(String orgCode, ServerGeoObjectType parentType, ServerGeoObjectType childType);

  public void enforceCanRemoveChild(String orgCode, ServerGeoObjectType parentType, ServerGeoObjectType childType);

  public boolean canRemoveChildCR(String orgCode, ServerGeoObjectType parentType, ServerGeoObjectType childType);

  public void enforceCanRemoveChildCR(String orgCode, ServerGeoObjectType parentType, ServerGeoObjectType childType);
}
