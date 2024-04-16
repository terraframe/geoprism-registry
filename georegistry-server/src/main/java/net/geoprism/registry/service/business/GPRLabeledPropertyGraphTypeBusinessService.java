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
package net.geoprism.registry.service.business;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.registry.Organization;
import net.geoprism.registry.service.permission.RolePermissionService;

@Service
@Primary
public class GPRLabeledPropertyGraphTypeBusinessService extends LabeledPropertyGraphTypeBusinessService implements LabeledPropertyGraphTypeBusinessServiceIF
{
  @Override
  public void apply(LabeledPropertyGraphType type)
  {
//    ServerHierarchyType hierarchy = ServerHierarchyType.get(type.getHierarchy());

    // Ensure the user has permissions to create
    Organization organization = type.getOrganization();

    new RolePermissionService().enforceRA(organization.getCode());
    
    type.setOrganization(organization);

    super.apply(type);
  }
}
