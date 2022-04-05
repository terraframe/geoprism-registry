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
  public ServerGraphNode addChild(VertexServerGeoObject geoObject, VertexServerGeoObject child, Date startDate, Date endDate, boolean validate)
  {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("unchecked")
  @Override
  public ServerGraphNode addParent(VertexServerGeoObject geoObject, VertexServerGeoObject parent, Date startDate, Date endDate, boolean validate)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeParent(VertexServerGeoObject geoObject, VertexServerGeoObject parent, Date startDate, Date endDate)
  {
    throw new UnsupportedOperationException();
  }

}
