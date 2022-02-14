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

import net.geoprism.registry.DirectedAcyclicGraphType;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.ServerChildGraphNode;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerParentGraphNode;

public class DirectedAcyclicGraphStrategy implements GraphStrategy
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

  private DirectedAcyclicGraphType type;

  public DirectedAcyclicGraphStrategy(DirectedAcyclicGraphType type)
  {
    this.type = type;
  }

  @Override
  public ServerChildGraphNode getChildren(VertexServerGeoObject parent, Boolean recursive, Date date)
  {
    ServerChildGraphNode tnRoot = new ServerChildGraphNode(parent, this.type, date, null, null);

    Map<String, Object> parameters = new HashedMap<String, Object>();
    parameters.put("rid", parent.getVertex().getRID());

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT EXPAND( outE(");
    statement.append("'" + this.type.getMdEdgeDAO().getDBClassName() + "'");
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

      VertexServerGeoObject child = new VertexServerGeoObject(parentType, parentVertex, date);

      ServerChildGraphNode tnParent;

      if (recursive)
      {
        tnParent = this.getChildren(child, recursive, date);
      }
      else
      {
        tnParent = new ServerChildGraphNode(parent, this.type, date, null, edge.getOid());
      }

      tnRoot.addChild(tnParent);
    }

    return tnRoot;
  }

  @Override
  public ServerParentGraphNode getParents(VertexServerGeoObject child, Boolean recursive, Date date)
  {
    ServerParentGraphNode tnRoot = new ServerParentGraphNode(child, this.type, date, null, null);

    Map<String, Object> parameters = new HashedMap<String, Object>();
    parameters.put("rid", child.getVertex().getRID());

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT EXPAND( inE(");
    statement.append("'" + this.type.getMdEdgeDAO().getDBClassName() + "'");
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
      }
      else
      {
        tnParent = new ServerParentGraphNode(parent, this.type, date, null, edge.getOid());
      }

      tnRoot.addParent(tnParent);
    }

    return tnRoot;
  }

  @Override
  public ServerParentGraphNode addChild(VertexServerGeoObject geoObject, VertexServerGeoObject child, Date startDate, Date endDate)
  {
    return this.addParent(child, geoObject, startDate, endDate);
  }

  @Override
  public ServerParentGraphNode addParent(VertexServerGeoObject geoObject, VertexServerGeoObject parent, Date startDate, Date endDate)
  {
    if (this.isCycle(geoObject, parent, startDate, endDate))
    {
      throw new UnsupportedOperationException("Cannot add a cycle");
    }

    if (this.getParentEdges(geoObject, parent, startDate, endDate).size() > 0)
    {
      throw new UnsupportedOperationException("Duplicate edge");
    }

    Set<ValueOverTime> votc = this.getParentCollection(geoObject);
    votc.add(new ValueOverTime(startDate, endDate, parent));

    SortedSet<EdgeObject> newEdges = this.setParentCollection(geoObject, votc);

    ServerParentGraphNode node = new ServerParentGraphNode(geoObject, this.type, startDate, endDate, null);
    node.addParent(new ServerParentGraphNode(parent, this.type, startDate, endDate, newEdges.first().getOid()));

    return node;
  }

  private boolean isCycle(VertexServerGeoObject geoObject, VertexServerGeoObject parent, Date startDate, Date endDate)
  {
    // SELECT count(*) FROM (MATCH {class: fastp_rovince0, where: (@rid =
    // #355:29)}.(inE("test_dag")
    // {where: (startDate = date('2020-04-04', 'yyyy-MM-dd'))}.outV())
    // {as: friend, while: ($depth < 3)} RETURN friend.code AS code) WHERE code
    // = "FASTCentralProvince"

    // SELECT COUNT(*) FROM ( MATCH {class: fastp_rovince0, where: (@rid =
    // :rid)}.(outE('test_dag')
    // {where: (:startDate BETWEEN startDate AND endDate OR :endDate BETWEEN
    // startDate AND endDate)}.inV())
    // {as: friend, while: true} RETURN friend.code AS code) WHERE code = :code

    VertexObject vertex = geoObject.getVertex();

    StringBuffer statement = new StringBuffer();
    statement.append("SELECT count(*) FROM (");
    statement.append("MATCH {class: " + geoObject.getType().getMdVertex().getDBClassName() + ", where: (@rid = :rid)}.(outE('" + this.type.getMdEdgeDAO().getDBClassName() + "')");
    statement.append(" {where: (:startDate BETWEEN startDate AND endDate OR :endDate BETWEEN startDate AND endDate)}.inV())");
    statement.append(" {as: friend, while: ($depth < 10000)} RETURN friend.code AS code");
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

  @Override
  public void removeParent(VertexServerGeoObject geoObject, VertexServerGeoObject parent, Date startDate, Date endDate)
  {
    this.getParentEdges(geoObject, parent, startDate, endDate).forEach(edge -> {
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
    TreeSet<EdgeObject> set = new TreeSet<EdgeObject>(new EdgeComparator());

    String statement = "SELECT expand(inE('" + this.type.getMdEdgeDAO().getDBClassName() + "'))";
    statement += " FROM :child";

    GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(statement);
    query.setParameter("child", geoObject.getVertex().getRID());

    set.addAll(query.getResults());

    return set;
  }

  private SortedSet<EdgeObject> getParentEdges(VertexServerGeoObject geoObject, VertexServerGeoObject parent, Date startDate, Date endDate)
  {
    TreeSet<EdgeObject> set = new TreeSet<EdgeObject>(new EdgeComparator());

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM (");
    statement.append("  SELECT expand(inE('" + this.type.getMdEdgeDAO().getDBClassName() + "'))");
    statement.append("  FROM :child");
    statement.append(")");
    statement.append(" WHERE out = :parent");
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
}