/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.query.graph;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;

import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;

public class VertexSelectGeoObjectQuery
{
  private ServerGeoObjectType   type;

  private Date                  date;

  private VertexServerGeoObject prev;

  private Integer               limit;

  public VertexSelectGeoObjectQuery(ServerGeoObjectType type, Date date)
  {
    this.type = type;
    this.date = date;
  }

  public VertexSelectGeoObjectQuery(ServerGeoObjectType type, Date date, VertexServerGeoObject prev)
  {
    this.type = type;
    this.date = date;
    this.prev = prev;
  }

  public ServerGeoObjectType getType()
  {
    return type;
  }

  public void setType(ServerGeoObjectType type)
  {
    this.type = type;
  }

  public Integer getLimit()
  {
    return limit;
  }

  public void setLimit(Integer limit)
  {
    this.limit = limit;
  }

  public GraphQuery<VertexObject> getQuery()
  {
    HashMap<String, Object> parameters = new HashMap<String, Object>();

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + this.type.getMdVertex().getDBClassName());

    if (this.prev != null)
    {
      statement.append(" WHERE @rid > :rid");
    }

    if (this.limit != null)
    {
      statement.append(" LIMIT " + this.limit);
    }

    return new GraphQuery<VertexObject>(statement.toString(), parameters);
  }

  public ServerGeoObjectIF getSingleResult()
  {
    GraphQuery<VertexObject> query = this.getQuery();

    VertexObject vertex = query.getSingleResult();

    if (vertex != null)
    {
      return new VertexServerGeoObject(type, vertex, this.date);
    }

    return null;
  }

  public List<VertexServerGeoObject> getResults()
  {
    List<VertexServerGeoObject> list = new LinkedList<VertexServerGeoObject>();
    GraphQuery<VertexObject> query = this.getQuery();

    List<VertexObject> results = query.getResults();

    for (VertexObject result : results)
    {
      list.add(new VertexServerGeoObject(type, result, this.date));
    }

    return list;
  }
}
