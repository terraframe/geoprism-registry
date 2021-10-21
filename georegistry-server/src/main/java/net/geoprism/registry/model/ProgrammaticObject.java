package net.geoprism.registry.model;

import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.graph.VertexObjectDAO;

import net.geoprism.registry.ProgrammaticType;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.graph.VertexServerGeoObject;

public class ProgrammaticObject
{
  private VertexObject     vertex;

  private ProgrammaticType type;

  public ProgrammaticObject(VertexObject vertex, ProgrammaticType type)
  {
    this.vertex = vertex;
    this.type = type;
  }

  public ProgrammaticType getType()
  {
    return type;
  }

  public VertexObject getVertex()
  {
    return vertex;
  }

  public static ProgrammaticObject newInstance(ProgrammaticType type)
  {
    VertexObject vertex = VertexObject.instantiate(VertexObjectDAO.newInstance(type.getMdVertexDAO()));

    return new ProgrammaticObject(vertex, type);
  }

  public void setValue(String attributeName, Object value)
  {
    this.vertex.setValue(attributeName, value);
  }

  public <T> T getObjectValue(String attributeName)
  {
    return this.vertex.getObjectValue(attributeName);
  }

  public JsonObject toJSON()
  {
    return new JsonObject();
  }

  public void apply()
  {
    this.vertex.apply();
  }

  public void delete()
  {
    this.vertex.delete();
  }

  public void setGeoObject(ServerGeoObjectIF geoObject)
  {
    if (geoObject instanceof VertexServerGeoObject)
    {
      VertexObject geoVertex = ( (VertexServerGeoObject) geoObject ).getVertex();

      this.vertex.setValue(ProgrammaticType.GEO_OBJECT, geoVertex);
    }
  }

  public VertexServerGeoObject getGeoObject()
  {
    String oid = this.vertex.getObjectValue(ProgrammaticType.GEO_OBJECT);

    if (oid != null)
    {
      VertexObject geoVertex = VertexObject.get(GeoVertex.CLASS, oid);
      MdVertexDAOIF mdVertex = (MdVertexDAOIF) geoVertex.getMdClass();
      ServerGeoObjectType vertexType = ServerGeoObjectType.get(mdVertex);

      return new VertexServerGeoObject(vertexType, geoVertex);
    }

    return null;
  }

  public static ProgrammaticObject get(ProgrammaticType type, String attributeName, Object value)
  {
    MdVertexDAOIF mdVertex = type.getMdVertexDAO();
    MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(attributeName);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());
    statement.append(" WHERE " + mdAttribute.getColumnName() + " = :" + attributeName);

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());
    query.setParameter(attributeName, value);

    VertexObject result = query.getSingleResult();

    if (result != null)
    {
      return new ProgrammaticObject(result, type);
    }

    return null;
  }

}
