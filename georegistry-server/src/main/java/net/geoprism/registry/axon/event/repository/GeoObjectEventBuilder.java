package net.geoprism.registry.axon.event.repository;

import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;

import com.google.gson.JsonObject;

import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.view.TypeClass;
import net.geoprism.registry.view.TypeInfo;

public class GeoObjectEventBuilder extends AbstractGeoObjectEventBuilder<GeoObjectOverTime>
{

  public GeoObjectEventBuilder(GeoObjectBusinessServiceIF service)
  {
    super(service);
  }

  @Override
  public String getCode()
  {
    return this.getOrThrow().getCode();
  }

  @Override
  public TypeInfo getType()
  {
    return new TypeInfo(TypeClass.GEO_OBJECT_TYPE, this.getOrThrow().getType().getCode());
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
