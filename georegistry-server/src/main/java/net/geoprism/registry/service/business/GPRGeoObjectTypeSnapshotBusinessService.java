/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.service.business;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.ComponentIF;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.business.rbac.UserDAO;
import com.runwaysdk.constants.UserInfo;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.GeoObjectTypeSnapshotQuery;
import net.geoprism.rbac.RoleConstants;
import net.geoprism.registry.Commit;
import net.geoprism.registry.CommitHasSnapshotQuery;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.model.SnapshotContainer;

@Service
@Primary
public class GPRGeoObjectTypeSnapshotBusinessService extends GeoObjectTypeSnapshotBusinessService implements GeoObjectTypeSnapshotBusinessServiceIF
{
  @Override
  public GeoObjectTypeSnapshot get(SnapshotContainer<?> version, String code)
  {
    if (version instanceof Commit)
    {
      QueryFactory factory = new QueryFactory();

      CommitHasSnapshotQuery vQuery = new CommitHasSnapshotQuery(factory);
      vQuery.WHERE(vQuery.getParent().EQ((Commit) version));

      GeoObjectTypeSnapshotQuery query = new GeoObjectTypeSnapshotQuery(factory);
      query.LEFT_JOIN_EQ(vQuery.getChild());
      query.AND(query.getCode().EQ(code));

      try (OIterator<? extends GeoObjectTypeSnapshot> it = query.getIterator())
      {
        if (it.hasNext())
        {
          return it.next();
        }
      }

      return null;

    }

    return super.get(version, code);
  }

  @Override
  protected void assignPermissions(ComponentIF component)
  {
    RoleDAO adminRole = RoleDAO.findRole(RoleConstants.ADMIN).getBusinessDAO();
    adminRole.grantPermission(Operation.CREATE, component.getOid());
    adminRole.grantPermission(Operation.DELETE, component.getOid());
    adminRole.grantPermission(Operation.WRITE, component.getOid());
    adminRole.grantPermission(Operation.WRITE_ALL, component.getOid());

    RoleDAO maintainer = RoleDAO.findRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE).getBusinessDAO();
    maintainer.grantPermission(Operation.CREATE, component.getOid());
    maintainer.grantPermission(Operation.DELETE, component.getOid());
    maintainer.grantPermission(Operation.WRITE, component.getOid());
    maintainer.grantPermission(Operation.WRITE_ALL, component.getOid());

    RoleDAO consumer = RoleDAO.findRole(RegistryConstants.API_CONSUMER_ROLE).getBusinessDAO();
    consumer.grantPermission(Operation.READ, component.getOid());
    consumer.grantPermission(Operation.READ_ALL, component.getOid());

    RoleDAO contributor = RoleDAO.findRole(RegistryConstants.REGISTRY_CONTRIBUTOR_ROLE).getBusinessDAO();
    contributor.grantPermission(Operation.READ, component.getOid());
    contributor.grantPermission(Operation.READ_ALL, component.getOid());

    UserDAO publicRole = UserDAO.findUser(UserInfo.PUBLIC_USER_NAME).getBusinessDAO();
    publicRole.grantPermission(Operation.READ, component.getOid());
    publicRole.grantPermission(Operation.READ_ALL, component.getOid());
  }
}
