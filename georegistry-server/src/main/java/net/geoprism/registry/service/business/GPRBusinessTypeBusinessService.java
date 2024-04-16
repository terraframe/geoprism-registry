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

import com.google.gson.JsonObject;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.system.Roles;

import ca.uhn.fhir.rest.annotation.Transaction;
import net.geoprism.registry.BusinessType;
import net.geoprism.registry.RegistryConstants;

@Service
@Primary
public class GPRBusinessTypeBusinessService extends BusinessTypeBusinessService implements BusinessTypeBusinessServiceIF
{
  @Override
  @Transaction
  public BusinessType apply(JsonObject object)
  {
    BusinessType type = super.apply(object);

    // Assign permissions
    String[] roleNames = new String[] { RegistryConstants.REGISTRY_ADMIN_ROLE, RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE };

    for (String roleName : roleNames)
    {
      Roles role = Roles.findRoleByName(roleName);

      MdVertexDAOIF mdVertex = type.getMdVertexDAO();
      MdEdgeDAOIF mdEdge = type.getMdEdgeDAO();

      RoleDAO roleDAO = (RoleDAO) BusinessFacade.getEntityDAO(role);
      roleDAO.grantPermission(Operation.CREATE, mdVertex.getOid());
      roleDAO.grantPermission(Operation.DELETE, mdVertex.getOid());
      roleDAO.grantPermission(Operation.WRITE, mdVertex.getOid());
      roleDAO.grantPermission(Operation.WRITE_ALL, mdVertex.getOid());

      // Assign edge permissions
      roleDAO.grantPermission(Operation.CREATE, mdEdge.getOid());
      roleDAO.grantPermission(Operation.DELETE, mdEdge.getOid());
      roleDAO.grantPermission(Operation.WRITE, mdEdge.getOid());
      roleDAO.grantPermission(Operation.WRITE_ALL, mdEdge.getOid());
      roleDAO.grantPermission(Operation.ADD_CHILD, mdEdge.getOid());
      roleDAO.grantPermission(Operation.ADD_PARENT, mdEdge.getOid());
    }

    return type;
  }
}
