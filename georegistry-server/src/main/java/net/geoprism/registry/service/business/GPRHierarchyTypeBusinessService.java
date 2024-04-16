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

import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.transaction.Transaction;

import net.geoprism.registry.ListType;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.request.SerializedListTypeCache;

@Service
@Primary
public class GPRHierarchyTypeBusinessService extends HierarchyTypeBusinessService implements HierarchyTypeBusinessServiceIF
{
  @Override
  public void refresh(ServerHierarchyType sht)
  {
    super.refresh(sht);
    SerializedListTypeCache.getInstance().clear();
  }

  @Override
  @Transaction
  protected void deleteInTrans(ServerHierarchyType sht)
  {
    super.deleteInTrans(sht);

    ListType.markAllAsInvalid(sht, null);
  }

  @Override
  @Transaction
  protected void removeFromHierarchy(ServerHierarchyType sht, ServerGeoObjectType parentType, ServerGeoObjectType childType, boolean migrateChildren)
  {
    super.removeFromHierarchy(sht, parentType, childType, migrateChildren);

    ListType.markAllAsInvalid(sht, childType);
    SerializedListTypeCache.getInstance().clear();
  }

  @Override
  @Transaction
  protected ServerHierarchyType createHierarchyTypeInTrans(HierarchyType dto)
  {
    ServerHierarchyType hierarchyType = super.createHierarchyTypeInTrans(dto);

    // Assign GPR permissions
    RoleDAO maintainer = RoleDAO.findRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE).getBusinessDAO();
    RoleDAO consumer = RoleDAO.findRole(RegistryConstants.API_CONSUMER_ROLE).getBusinessDAO();
    RoleDAO contributor = RoleDAO.findRole(RegistryConstants.REGISTRY_CONTRIBUTOR_ROLE).getBusinessDAO();

    MdEdgeDAOIF objectEdge = hierarchyType.getObjectEdge();

    this.grantWritePermissionsOnMdTermRel(objectEdge);
    this.grantWritePermissionsOnMdTermRel(maintainer, objectEdge);
    this.grantReadPermissionsOnMdTermRel(consumer, objectEdge);
    this.grantReadPermissionsOnMdTermRel(contributor, objectEdge);

    MdEdgeDAOIF definitionEdge = hierarchyType.getDefinitionEdge();

    this.grantWritePermissionsOnMdTermRel(definitionEdge);
    this.grantWritePermissionsOnMdTermRel(maintainer, definitionEdge);
    this.grantReadPermissionsOnMdTermRel(consumer, definitionEdge);
    this.grantReadPermissionsOnMdTermRel(contributor, definitionEdge);

    return hierarchyType;
  }
}
