package net.geoprism.registry.model.graph;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.collections4.map.HashedMap;

import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;

import net.geoprism.registry.UndirectedGraphType;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.ServerChildGraphNode;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerGraphNode;
import net.geoprism.registry.model.ServerParentGraphNode;

public class UndirectedGraphStrategy implements GraphStrategy
{
  private static class EdgeComparator implements Comparator<EdgeObject>
  {
    @Override
    public int compare(EdgeObject o1, EdgeObject o2)
    {
      Date d1 = o1.getObjectValue(GeoVertex.START_DATE);
      Date d2 = o2.getObjectValue(GeoVertex.START_DATE);

      return d1.compareTo(d2);
    }
  }

  private UndirectedGraphType type;

  public UndirectedGraphStrategy(UndirectedGraphType type)
  {
    this.type = type;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends ServerGraphNode> T getChildren(VertexServerGeoObject source, Boolean recursive, Date date)
  {
    return (T) getChildren(source, recursive, date, new TreeSet<String>());
  }

  private ServerChildGraphNode getChildren(VertexServerGeoObject source, Boolean recursive, Date date, TreeSet<String> visited)
  {
    ServerChildGraphNode tnRoot = new ServerChildGraphNode(source, this.type, date, null, null);

    List<EdgeObject> edges = this.getEdges(source, date);

    for (EdgeObject edge : edges)
    {
      Object sourceRid = source.getVertex().getRID();

      final VertexObject vertex = edge.getChild().getRID().equals(sourceRid) ? edge.getParent() : edge.getChild();

      MdVertexDAOIF mdVertex = (MdVertexDAOIF) vertex.getMdClass();

      ServerGeoObjectType vertexType = ServerGeoObjectType.get(mdVertex);

      VertexServerGeoObject target = new VertexServerGeoObject(vertexType, vertex, date);

      if (!source.getUid().equals(target.getUid()))
      {
        ServerChildGraphNode tnParent;

        if (recursive && !visited.contains(target.getUid()))
        {
          visited.add(target.getUid());

          tnParent = this.getChildren(target, recursive, date, visited);
          tnParent.setOid(edge.getOid());
        }
        else
        {
          tnParent = new ServerChildGraphNode(target, this.type, date, null, edge.getOid());
        }

        tnRoot.addChild(tnParent);
      }
    }

    return tnRoot;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends ServerGraphNode> T getParents(VertexServerGeoObject child, Boolean recursive, Date date)
  {
    return (T) getParents(child, recursive, date, new TreeSet<String>());
  }

  private ServerParentGraphNode getParents(VertexServerGeoObject source, Boolean recursive, Date date, TreeSet<String> visited)
  {
    ServerParentGraphNode tnRoot = new ServerParentGraphNode(source, this.type, date, null, null);

    List<EdgeObject> edges = this.getEdges(source, date);

    for (EdgeObject edge : edges)
    {
      Object sourceRid = source.getVertex().getRID();

      final VertexObject vertex = edge.getChild().getRID().equals(sourceRid) ? edge.getParent() : edge.getChild();

      MdVertexDAOIF mdVertex = (MdVertexDAOIF) vertex.getMdClass();

      ServerGeoObjectType targetType = ServerGeoObjectType.get(mdVertex);

      VertexServerGeoObject target = new VertexServerGeoObject(targetType, vertex, date);

      if (!target.getUid().equals(source.getUid()))
      {
        ServerParentGraphNode tnParent;

        if (recursive & !visited.contains(target.getUid()))
        {
          visited.add(target.getUid());

          tnParent = this.getParents(target, recursive, date, visited);
          tnParent.setOid(edge.getOid());
        }
        else
        {
          tnParent = new ServerParentGraphNode(target, this.type, date, null, edge.getOid());
        }

        tnRoot.addParent(tnParent);
      }
    }

    return tnRoot;
  }

  @Override
  public <T extends ServerGraphNode> T addChild(VertexServerGeoObject geoObject, VertexServerGeoObject child, Date startDate, Date endDate)
  {
    return this.addParent(child, geoObject, startDate, endDate);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends ServerGraphNode> T addParent(VertexServerGeoObject geoObject, VertexServerGeoObject parent, Date startDate, Date endDate)
  {
    if (this.isCycle(geoObject, parent, startDate, endDate))
    {
      throw new UnsupportedOperationException("Cyclic graph is not supported");
    }

    if (this.getEdges(geoObject, parent, startDate, endDate).size() > 0)
    {
      throw new UnsupportedOperationException("Duplicate edge");
    }

    Set<ValueOverTime> votc = this.getParentCollection(geoObject);
    votc.add(new ValueOverTime(startDate, endDate, parent));

    SortedSet<EdgeObject> newEdges = this.setParentCollection(geoObject, votc);

    ServerParentGraphNode node = new ServerParentGraphNode(geoObject, this.type, startDate, endDate, null);
    node.addParent(new ServerParentGraphNode(parent, this.type, startDate, endDate, newEdges.first().getOid()));

    return (T) node;
  }

  @Override
  public void removeParent(VertexServerGeoObject geoObject, VertexServerGeoObject parent, Date startDate, Date endDate)
  {
    this.getEdges(geoObject, parent, startDate, endDate).forEach(edge -> {
      edge.delete();
    });
  }

  private Set<ValueOverTime> getParentCollection(VertexServerGeoObject geoObject)
  {
    Set<ValueOverTime> set = new TreeSet<ValueOverTime>(new Comparator<ValueOverTime>()
    {
      @Override
      public int compare(ValueOverTime o1, ValueOverTime o2)
      {
        return o1.getOid().compareTo(o2.getOid());
      }
    });

    SortedSet<EdgeObject> edges = this.getParentEdges(geoObject);

    for (EdgeObject edge : edges)
    {
      final Date startDate = edge.getObjectValue(GeoVertex.START_DATE);
      final Date endDate = edge.getObjectValue(GeoVertex.END_DATE);

      VertexObject parentVertex = edge.getParent();
      MdVertexDAOIF mdVertex = (MdVertexDAOIF) parentVertex.getMdClass();
      ServerGeoObjectType parentType = ServerGeoObjectType.get(mdVertex);
      VertexServerGeoObject parent = new VertexServerGeoObject(parentType, parentVertex, startDate);

      set.add(new ValueOverTime(edge.getOid(), startDate, endDate, parent));
    }

    return set;
  }

  private SortedSet<EdgeObject> setParentCollection(VertexServerGeoObject geoObject, Set<ValueOverTime> votc)
  {
    SortedSet<EdgeObject> newEdges = new TreeSet<EdgeObject>(new EdgeComparator());
    SortedSet<EdgeObject> edges = this.getParentEdges(geoObject);

    for (EdgeObject edge : edges)
    {
      final Date startDate = edge.getObjectValue(GeoVertex.START_DATE);
      final Date endDate = edge.getObjectValue(GeoVertex.END_DATE);

      VertexObject parentVertex = edge.getParent();
      MdVertexDAOIF mdVertex = (MdVertexDAOIF) parentVertex.getMdClass();
      ServerGeoObjectType parentType = ServerGeoObjectType.get(mdVertex);
      final VertexServerGeoObject edgeGo = new VertexServerGeoObject(parentType, parentVertex, startDate);

      ValueOverTime inVot = null;

      for (ValueOverTime vot : votc)
      {
        if (vot.getOid() == edge.getOid())
        {
          inVot = vot;
          break;
        }
      }

      if (inVot == null)
      {
        edge.delete();
      }
      else
      {
        VertexServerGeoObject inGo = (VertexServerGeoObject) inVot.getValue();

        boolean hasValueChange = false;

        if ( ( inGo == null && edgeGo != null ) || ( inGo != null && edgeGo == null ))
        {
          hasValueChange = true;
        }
        else if ( ( inGo != null && edgeGo != null ) && !inGo.equals(edgeGo))
        {
          hasValueChange = true;
        }

        if (hasValueChange)
        {
          edge.delete();

          EdgeObject newEdge = geoObject.getVertex().addParent(inGo.getVertex(), this.type.getMdEdgeDAO());
          newEdge.setValue(GeoVertex.START_DATE, startDate);
          newEdge.setValue(GeoVertex.END_DATE, endDate);
          newEdge.apply();

          newEdges.add(newEdge);
        }
        else
        {
          boolean hasChanges = false;

          if (startDate != inVot.getStartDate())
          {
            hasChanges = true;
            edge.setValue(GeoVertex.START_DATE, startDate);
          }

          if (endDate != inVot.getEndDate())
          {
            hasChanges = true;
            edge.setValue(GeoVertex.END_DATE, endDate);
          }

          if (hasChanges)
          {
            edge.apply();
          }
        }
      }
    }

    for (ValueOverTime vot : votc)
    {
      boolean isNew = true;

      for (EdgeObject edge : edges)
      {
        if (vot.getOid() == edge.getOid())
        {
          isNew = false;
        }
      }

      if (isNew)
      {
        EdgeObject newEdge = geoObject.getVertex().addParent( ( (VertexServerGeoObject) vot.getValue() ).getVertex(), this.type.getMdEdgeDAO());
        newEdge.setValue(GeoVertex.START_DATE, vot.getStartDate());
        newEdge.setValue(GeoVertex.END_DATE, vot.getEndDate());
        newEdge.apply();

        newEdges.add(newEdge);
      }
    }

    return newEdges;
  }

  private SortedSet<EdgeObject> getParentEdges(VertexServerGeoObject geoObject)
  {
    String statement = "SELECT expand(inE('" + this.type.getMdEdgeDAO().getDBClassName() + "'))";
    statement += " FROM :child";

    GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(statement);
    query.setParameter("child", geoObject.getVertex().getRID());

    TreeSet<EdgeObject> set = new TreeSet<EdgeObject>(new EdgeComparator());
    set.addAll(query.getResults());
    return set;
  }

  private List<EdgeObject> getEdges(VertexServerGeoObject geoObject, Date date)
  {
    Map<String, Object> parameters = new HashedMap<String, Object>();
    parameters.put("rid", geoObject.getVertex().getRID());

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT EXPAND( bothE(");
    statement.append("'" + this.type.getMdEdgeDAO().getDBClassName() + "'");
    statement.append(")");

    if (date != null)
    {
      statement.append("[:date BETWEEN startDate AND endDate]");
      parameters.put("date", date);
    }

    statement.append(") FROM :rid");

    GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(statement.toString(), parameters);

    return query.getResults();
  }

  private SortedSet<EdgeObject> getEdges(VertexServerGeoObject geoObject, VertexServerGeoObject parent, Date startDate, Date endDate)
  {
    TreeSet<EdgeObject> set = new TreeSet<EdgeObject>(new EdgeComparator());

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM (");
    statement.append("  SELECT expand(bothE('" + this.type.getMdEdgeDAO().getDBClassName() + "'))");
    statement.append("  FROM :child");
    statement.append(")");
    statement.append(" WHERE (out = :parent OR in = :parent)");
    statement.append(" AND startDate = :startDate");
    statement.append(" AND endDate = :endDate");

    GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(statement.toString());
    query.setParameter("child", geoObject.getVertex().getRID());
    query.setParameter("parent", parent.getVertex().getRID());
    query.setParameter("startDate", startDate);
    query.setParameter("endDate", endDate);

    set.addAll(query.getResults());

    return set;
  }

  public boolean isCycle(VertexServerGeoObject geoObject, VertexServerGeoObject parent, Date startDate, Date endDate)
  {
    VertexObject vertex = geoObject.getVertex();

    StringBuffer statement = new StringBuffer();
    statement.append("SELECT count(*) FROM (");
    statement.append("MATCH {class: " + geoObject.getType().getMdVertex().getDBClassName() + ", where: (@rid = :rid)}.(outE('" + this.type.getMdEdgeDAO().getDBClassName() + "')");
    statement.append(" {where: (:startDate BETWEEN startDate AND endDate OR :endDate BETWEEN startDate AND endDate)}.outV())");
    statement.append(" {as: friend, while: ($depth < 10)} RETURN friend.code AS code");
    statement.append(")");
    statement.append(" WHERE code = :code");

    GraphQuery<Long> query = new GraphQuery<Long>(statement.toString());
    query.setParameter("rid", vertex.getRID());
    query.setParameter("startDate", startDate);
    query.setParameter("endDate", endDate);
    query.setParameter("code", parent.getCode());

    Long count = query.getSingleResult();

    return ( count != null && count > 0 );
  }

}
