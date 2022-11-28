package net.geoprism.registry.model.graph;

import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdVertexDAOIF;

import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.ServerGeoObjectType;

public class ExternalId
{
  public static final String KEY_SEPARATOR = "!~_-";
  
  public static final String CLASS = GeoVertex.EXTERNAL_ID;
  
  public static final String ID = "id";
  
  public static final String KEY = "key";
  
  private EdgeObject edge;
  
  public ExternalId(EdgeObject edge)
  {
    this.edge = edge;
  }
  
  public void apply()
  {
    this.edge.setValue(KEY, this.buildKey());
    
    this.edge.apply();
  }
  
  public String buildKey()
  {
    return this.getExternalId() + KEY_SEPARATOR + this.getParent().getOid() + KEY_SEPARATOR + this.getChild().getType().getMdVertex().getDBClassName();
  }
  
  public VertexServerGeoObject getChild()
  {
    VertexObject child = this.edge.getChild();
    
    ServerGeoObjectType type = ServerGeoObjectType.get((MdVertexDAOIF) child.getMdClass());
    
    return new VertexServerGeoObject(type, child);
  }
  
  public ExternalSystem getParent()
  {
    return (ExternalSystem) this.edge.getParent();
  }
  
  public String getOid()
  {
    return this.edge.getOid();
  }
  
  public String getExternalId()
  {
    return this.edge.getObjectValue("id");
  }
  
  public void setExternalId(String id)
  {
    this.edge.setValue(ID, id);
  }
}
