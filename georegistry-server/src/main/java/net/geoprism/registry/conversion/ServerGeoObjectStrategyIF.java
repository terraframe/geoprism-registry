package net.geoprism.registry.conversion;

import org.commongeoregistry.adapter.dataaccess.GeoObject;

import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.query.ServerGeoObjectQuery;

public interface ServerGeoObjectStrategyIF
{
  public ServerGeoObjectType getType();

  public ServerGeoObjectIF constructFromGeoObject(GeoObject geoObject, boolean isNew);

  public ServerGeoObjectIF constructFromDB(Object dbObject);

  public ServerGeoObjectIF getGeoObjectByCode(String code);

  public ServerGeoObjectIF newInstance();

  public ServerGeoObjectQuery createQuery();

}
