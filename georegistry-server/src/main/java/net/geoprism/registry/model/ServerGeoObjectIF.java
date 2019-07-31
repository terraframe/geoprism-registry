package net.geoprism.registry.model;

import com.runwaysdk.business.Business;

public interface ServerGeoObjectIF
{
  public String bbox();
  
  /**
   * The Business is used to store additional attributes on the GeoObject. Leaf nodes only have a Business,
   * Tree nodes have a business and a GeoEntity.
   * 
   * @return
   */
  public Business getBusiness();
}
