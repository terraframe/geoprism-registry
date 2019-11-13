package net.geoprism.registry.graph;

import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.business.graph.VertexQuery;
import com.runwaysdk.dataaccess.graph.VertexObjectDAO;

import net.geoprism.registry.model.ServerGeoObjectType;

public abstract class GeoVertex extends GeoVertexBase
{
  private static final long serialVersionUID = 765825100;

  public GeoVertex()
  {
    super();
  }

  public static VertexObject getVertex(ServerGeoObjectType type, String uuid)
  {
    String statement = "SELECT FROM " + type.getMdVertex().getDBClassName();
    statement += " WHERE uuid = :uuid";

    VertexQuery<GeoVertex> query = new VertexQuery<GeoVertex>(statement);
    query.setParameter("uuid", uuid);

    return query.getSingleResult();
  }

  public static VertexObject newInstance(ServerGeoObjectType type)
  {
    VertexObjectDAO dao = VertexObjectDAO.newInstance(type.getMdVertex());

    return VertexObject.instantiate(dao);
  }
}
