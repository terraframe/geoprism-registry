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
import net.geoprism.registry.query.ServerGeoObjectQuery;
import net.geoprism.registry.query.ServerGeoObjectRestriction;

public class VertexGeoObjectQuery implements ServerGeoObjectQuery
{
  private ServerGeoObjectType        type;

  private Date                       date;

  private ServerGeoObjectRestriction restriction;

  private Integer                    limit;

  public VertexGeoObjectQuery(ServerGeoObjectType type, Date date)
  {
    this.type = type;
    this.date = date;
  }

  public ServerGeoObjectType getType()
  {
    return type;
  }

  public void setType(ServerGeoObjectType type)
  {
    this.type = type;
  }

  public ServerGeoObjectRestriction getRestriction()
  {
    return restriction;
  }

  public void setRestriction(ServerGeoObjectRestriction restriction)
  {
    this.restriction = restriction;
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

    // MATCH {class: district0, as: location, where:
    // (coalesce(displayLabel.defaultLocale) = 'Anlong
    // Veaeng')}.in('located_in0'){as: parent, where: (@class='cambodia0'),
    // while: ($depth < 6)} RETURN location, parent

    StringBuilder statement = new StringBuilder();
    statement.append("MATCH { ");
    statement.append("class: " + this.type.getMdVertex().getDBClassName());
    statement.append(", as: location");

    if (this.restriction != null)
    {
      this.restriction.create(this).restrict(statement, parameters);
    }

    statement.append("} RETURN $elements");
    statement.append(" ORDER BY location.code ASC");

    if (this.limit != null)
    {
      statement.append(" LIMIT " + this.limit);
    }

    return new GraphQuery<VertexObject>(statement.toString(), parameters);
  }

  public GraphQuery<Long> getCountQuery()
  {
    HashMap<String, Object> parameters = new HashMap<String, Object>();

    StringBuilder statement = new StringBuilder();
    statement.append("MATCH { ");
    statement.append("class: " + this.type.getMdVertex().getDBClassName());
    statement.append(", as: location");

    if (this.restriction != null)
    {
      this.restriction.create(this).restrict(statement, parameters);
    }

    statement.append("} RETURN count(location)");

    return new GraphQuery<Long>(statement.toString(), parameters);
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

  public List<ServerGeoObjectIF> getResults()
  {
    List<ServerGeoObjectIF> list = new LinkedList<ServerGeoObjectIF>();
    GraphQuery<VertexObject> query = this.getQuery();

    List<VertexObject> results = query.getResults();

    for (VertexObject result : results)
    {
      list.add(new VertexServerGeoObject(type, result, this.date));
    }

    return list;
  }

  public Long getCount()
  {
    return this.getCountQuery().getSingleResult();
  }
}
