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
package net.geoprism.registry.model.graph;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;

import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdVertexDAOIF;

import net.geoprism.registry.model.ServerChildGraphNode;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerGraphNode;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerParentGraphNode;

public class ServerHierarchyStrategy implements GraphStrategy
{
  private ServerHierarchyType hierarchy;

  public ServerHierarchyStrategy(ServerHierarchyType hierarchy)
  {
    this.hierarchy = hierarchy;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ServerChildGraphNode getChildren(VertexServerGeoObject parent, Boolean recursive, Date date)
  {
    ServerChildGraphNode tnRoot = new ServerChildGraphNode(parent, this.hierarchy, date, null, null);

    Map<String, Object> parameters = new HashedMap<String, Object>();
    parameters.put("rid", parent.getVertex().getRID());

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT EXPAND( outE(");
    statement.append("'" + this.hierarchy.getMdEdge().getDBClassName() + "'");
    statement.append(")");

    if (date != null)
    {
      statement.append("[:date BETWEEN startDate AND endDate]");
      parameters.put("date", date);
    }

    statement.append(") FROM :rid");

    GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(statement.toString(), parameters);

    List<EdgeObject> edges = query.getResults();

    for (EdgeObject edge : edges)
    {
      final VertexObject childVertex = edge.getChild();

      MdVertexDAOIF mdVertex = (MdVertexDAOIF) childVertex.getMdClass();

      ServerGeoObjectType childType = ServerGeoObjectType.get(mdVertex);

      VertexServerGeoObject child = new VertexServerGeoObject(childType, childVertex, date);

      ServerChildGraphNode tnParent;

      if (recursive)
      {
        tnParent = this.getChildren(child, recursive, date);
        tnParent.setOid(edge.getOid());
      }
      else
      {
        tnParent = new ServerChildGraphNode(child, this.hierarchy, date, null, edge.getOid());
      }

      tnRoot.addChild(tnParent);
    }

    return tnRoot;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ServerParentGraphNode getParents(VertexServerGeoObject child, Boolean recursive, Date date)
  {
    ServerParentGraphNode tnRoot = new ServerParentGraphNode(child, this.hierarchy, date, null, null);

    Map<String, Object> parameters = new HashedMap<String, Object>();
    parameters.put("rid", child.getVertex().getRID());

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT EXPAND( inE(");
    statement.append("'" + this.hierarchy.getMdEdge().getDBClassName() + "'");
    statement.append(")");

    if (date != null)
    {
      statement.append("[:date BETWEEN startDate AND endDate]");
      parameters.put("date", date);
    }

    statement.append(") FROM :rid");

    GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(statement.toString(), parameters);

    List<EdgeObject> edges = query.getResults();

    for (EdgeObject edge : edges)
    {
      final VertexObject parentVertex = edge.getParent();

      MdVertexDAOIF mdVertex = (MdVertexDAOIF) parentVertex.getMdClass();

      ServerGeoObjectType parentType = ServerGeoObjectType.get(mdVertex);

      VertexServerGeoObject parent = new VertexServerGeoObject(parentType, parentVertex, date);

      ServerParentGraphNode tnParent;

      if (recursive)
      {
        tnParent = this.getParents(parent, recursive, date);
        tnParent.setOid(edge.getOid());
      }
      else
      {
        tnParent = new ServerParentGraphNode(parent, this.hierarchy, date, null, edge.getOid());
      }

      tnRoot.addParent(tnParent);
    }

    return tnRoot;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ServerGraphNode addChild(VertexServerGeoObject geoObject, VertexServerGeoObject child, Date startDate, Date endDate)
  {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("unchecked")
  @Override
  public ServerGraphNode addParent(VertexServerGeoObject geoObject, VertexServerGeoObject parent, Date startDate, Date endDate)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeParent(VertexServerGeoObject geoObject, VertexServerGeoObject parent, Date startDate, Date endDate)
  {
    throw new UnsupportedOperationException();
  }

}
