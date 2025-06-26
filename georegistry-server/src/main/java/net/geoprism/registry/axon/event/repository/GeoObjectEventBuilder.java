package net.geoprism.registry.axon.event.repository;

import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;

import com.google.gson.JsonObject;

import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;

public class GeoObjectEventBuilder extends AbstractGeoObjectEventBuilder<GeoObjectOverTime>
{

  public GeoObjectEventBuilder(GeoObjectBusinessServiceIF service)
  {
    super(service);
  }

  @Override
  public String getUid()
  {
    return this.getOrThrow().getUid();
  }

  @Override
  public String getType()
  {
    return this.getOrThrow().getType().getCode();
  }

  @Override
  protected JsonObject toJSON()
  {
    return this.getOrThrow().toJSON();
  }

  @Override
  protected void removeAllEdges(ServerHierarchyType hierarchyType)
  {
    throw new UnsupportedOperationException();
  }
}
