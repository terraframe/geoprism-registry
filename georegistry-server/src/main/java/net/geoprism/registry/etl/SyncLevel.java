/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.etl;

import net.geoprism.registry.model.ServerGeoObjectType;

public class SyncLevel
{

  public static enum Type {
    ORG_UNITS, RELATIONSHIPS, ALL
  }
  
  private transient ServerGeoObjectType geoObjectType;

  private String geoObjectTypeCode;

  private Type                syncType;

  private int                 level;

  public ServerGeoObjectType getGeoObjectType()
  {
    if (geoObjectType == null)
    {
      geoObjectType = ServerGeoObjectType.get(geoObjectTypeCode);
    }
    
    return geoObjectType;
  }

  public void setGeoObjectType(String geoObjectTypeCode)
  {
    this.geoObjectTypeCode = geoObjectTypeCode;
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
