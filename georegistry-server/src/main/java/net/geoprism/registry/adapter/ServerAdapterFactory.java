package net.geoprism.registry.adapter;

import org.commongeoregistry.adapter.dataaccess.GeoObject;

import net.geoprism.registry.model.ServerGeoObjectIF;

public class ServerAdapterFactory
{
  public static ServerGeoObjectIF geoObject(GeoObject go)
  {
    // if (go.getType().isLeaf())
    // {
    // return new ServerLeafGeoObject(go);
    // }
    // else
    // {
    // return new ServerTreeGeoObject(go);
    // }
    return null;
  }
}
