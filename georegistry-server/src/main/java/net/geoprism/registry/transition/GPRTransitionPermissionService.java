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
package net.geoprism.registry.transition;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.geoprism.graphrepo.permission.TransitionPermissionServiceIF;
import net.geoprism.graphrepo.permission.UserPermissionService;
import net.geoprism.registry.graph.transition.TransitionEvent;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.permission.RolePermissionService;
import net.geoprism.registry.service.GPRServiceFactory;

public class GPRTransitionPermissionService extends UserPermissionService implements TransitionPermissionServiceIF
{
  public Set<RepoPermissionAction> getPermissions(TransitionEvent event)
  {
    final RolePermissionService perms = GPRServiceFactory.getRolePermissionService();

    final String beforeOrgCode = event.getBeforeTypeOrgCode();
    final String beforeGotCode = event.getBeforeTypeCode();
    
    final String afterOrgCode = event.getAfterTypeOrgCode();
    final String afterGotCode = event.getAfterTypeCode();

    HashSet<RepoPermissionAction> actions = new HashSet<RepoPermissionAction>();
    
    if (beforeOrgCode == null || beforeGotCode == null)
    {
      return actions;
    }
    
    ServerGeoObjectType beforeType = ServerGeoObjectType.get(beforeGotCode, true);
    ServerGeoObjectType afterType = ServerGeoObjectType.get(afterGotCode, true);

    if (perms.isSRA())
    {
      actions.addAll(Arrays.asList(RepoPermissionAction.values()));
    }
    else if (perms.isRA(beforeOrgCode))
    {
      actions.addAll(Arrays.asList(RepoPermissionAction.values()));
    }
    else if (perms.isRM(beforeOrgCode, beforeType))
    {
      actions.addAll(Arrays.asList(RepoPermissionAction.values()));
    }
    else if ( (!beforeType.getIsPrivate() && !afterType.getIsPrivate()) || perms.isRA(afterOrgCode) || perms.isRM(afterOrgCode, afterType) )
    {
      actions.add(RepoPermissionAction.READ);
    }

    return actions;
  }
}
