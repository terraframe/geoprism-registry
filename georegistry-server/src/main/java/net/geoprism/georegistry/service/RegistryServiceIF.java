package net.geoprism.georegistry.service;

import org.commongeoregistry.adapter.dataaccess.GeoObject;

public interface RegistryServiceIF
{
  public GeoObject getGeoObject(String uid);
  
  public GeoObject updateGeoObject(String jGeoObj);
  
  
}
