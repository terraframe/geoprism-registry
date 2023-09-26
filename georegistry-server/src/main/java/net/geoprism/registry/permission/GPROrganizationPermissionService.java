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

import org.springframework.stereotype.Service;

import com.runwaysdk.business.rbac.SingleActorDAOIF;

import net.geoprism.graphrepo.permission.OrganizationPermissionServiceIF;
import net.geoprism.graphrepo.permission.UserPermissionService;
import net.geoprism.registry.service.GPRServiceFactory;

@Service
public class GPROrganizationPermissionService extends UserPermissionService implements OrganizationPermissionServiceIF
{

  public void enforceActorCanCreate()
  {
    GPRServiceFactory.getRolePermissionService().enforceSRA();
  }

  public void enforceActorCanUpdate()
  {
    GPRServiceFactory.getRolePermissionService().enforceSRA();
  }

  public boolean canActorCreate()
  {
    return GPRServiceFactory.getRolePermissionService().isSRA();
  }

  public boolean canActorUpdate()
  {
    return GPRServiceFactory.getRolePermissionService().isSRA();
  }

  public void enforceActorCanDelete()
  {
    GPRServiceFactory.getRolePermissionService().enforceSRA();
  }

  public boolean canActorDelete()
  {
    return GPRServiceFactory.getRolePermissionService().isSRA();
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

}
