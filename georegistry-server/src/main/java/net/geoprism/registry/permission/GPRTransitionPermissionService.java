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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import net.geoprism.graphrepo.permission.TransitionPermissionServiceIF;
import net.geoprism.graphrepo.permission.UserPermissionService;
import net.geoprism.registry.graph.transition.TransitionEvent;
import net.geoprism.registry.model.ServerGeoObjectType;

@Service
@Primary
public class GPRTransitionPermissionService extends UserPermissionService implements TransitionPermissionServiceIF
{
  @Autowired
  private RolePermissionService permissions;

  public Set<RepoPermissionAction> getPermissions(TransitionEvent event)
  {
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

    if (permissions.isSRA())
    {
      actions.addAll(Arrays.asList(RepoPermissionAction.values()));
    }
    else if (permissions.isRA(beforeOrgCode))
    {
      actions.addAll(Arrays.asList(RepoPermissionAction.values()));
    }
    else if (permissions.isRM(beforeOrgCode, beforeType))
    {
      actions.addAll(Arrays.asList(RepoPermissionAction.values()));
    }
    else if ( (!beforeType.getIsPrivate() && !afterType.getIsPrivate()) || permissions.isRA(afterOrgCode) || permissions.isRM(afterOrgCode, afterType) )
    {
      actions.add(RepoPermissionAction.READ);
    }

    return actions;
  }
}
