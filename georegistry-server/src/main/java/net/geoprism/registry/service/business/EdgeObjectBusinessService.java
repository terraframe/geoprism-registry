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

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphQuery;

import net.geoprism.registry.model.EdgeType;

@Service
public class EdgeObjectBusinessService
{
  public Optional<EdgeObject> getByOid(EdgeType type, String oid)
  {
    String dbClassName = type.getMdEdgeDAO().getDBClassName();

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + dbClassName);
    statement.append(" WHERE oid = :oid");

    GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(statement.toString());
    query.setParameter("oid", oid);

    return Optional.ofNullable(query.getSingleResult());
  }

  public Optional<EdgeObject> getByUid(EdgeType type, String uid)
  {
    String dbClassName = type.getMdEdgeDAO().getDBClassName();

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + dbClassName);
    statement.append(" WHERE uid = :uid");

    GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(statement.toString());
    query.setParameter("uid", uid);

    return Optional.ofNullable(query.getSingleResult());
  }

}
