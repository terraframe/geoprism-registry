package net.geoprism.registry;

import net.geoprism.GeoprismConfigurationIF;

public class GeoprismGeoregistryConfiguration implements GeoprismConfigurationIF
{

  @Override
  public String getHomeUrl()
  {
    return "/cgr/manage";
  }

}
