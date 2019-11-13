package net.geoprism.registry.conversion;

import org.commongeoregistry.adapter.dataaccess.GeoObject;

import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;

public interface ServerGeoObjectConverterIF
{
  public ServerGeoObjectType getType();

  public ServerGeoObjectIF constructFromGeoObject(GeoObject geoObject, boolean isNew);

  public ServerGeoObjectIF constructFromDB(Object dbObject);

}
