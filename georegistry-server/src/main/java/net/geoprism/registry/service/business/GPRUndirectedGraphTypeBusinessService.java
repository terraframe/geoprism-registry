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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;

import net.geoprism.registry.RegistryConstants;

@Service
@Primary
public class GPRUndirectedGraphTypeBusinessService extends UndirectedGraphTypeBusinessService implements UndirectedGraphTypeBusinessServiceIF
{
  @Autowired
  protected GPRHierarchyTypeBusinessService htService;
  
  @Override
  protected void createPermissions(MdEdgeDAO mdEdgeDAO)
  {
    RoleDAO maintainer = RoleDAO.findRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE).getBusinessDAO();
    RoleDAO consumer = RoleDAO.findRole(RegistryConstants.API_CONSUMER_ROLE).getBusinessDAO();
    RoleDAO contributor = RoleDAO.findRole(RegistryConstants.REGISTRY_CONTRIBUTOR_ROLE).getBusinessDAO();
    
    htService.grantWritePermissionsOnMdTermRel(mdEdgeDAO);
    htService.grantWritePermissionsOnMdTermRel(maintainer, mdEdgeDAO);
    htService.grantReadPermissionsOnMdTermRel(consumer, mdEdgeDAO);
    htService.grantReadPermissionsOnMdTermRel(contributor, mdEdgeDAO);
  }
}
