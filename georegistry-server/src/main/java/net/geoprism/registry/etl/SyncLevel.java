package net.geoprism.registry.etl;

import net.geoprism.registry.model.ServerGeoObjectType;

public class SyncLevel
{

  public static enum Type {
    ORG_UNITS, RELATIONSHIPS, ALL
  }

  private ServerGeoObjectType geoObjectType;

  private Type                syncType;

  private int                 level;

  public ServerGeoObjectType getGeoObjectType()
  {
    return geoObjectType;
  }

  public void setGeoObjectType(ServerGeoObjectType geoObjectType)
  {
    this.geoObjectType = geoObjectType;
  }

  public Type getSyncType()
  {
    return syncType;
  }

  public void setSyncType(Type syncType)
  {
    this.syncType = syncType;
  }

  public int getLevel()
  {
    return level;
  }

  public void setLevel(int level)
  {
    this.level = level;
  }

}
