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

import java.util.Map;

import com.google.gson.annotations.SerializedName;

import net.geoprism.registry.model.ServerGeoObjectType;

public class SyncLevel implements Comparable<SyncLevel>
{

  public static enum Type {
    ORG_UNITS, RELATIONSHIPS, ALL
  }
  
  private transient ServerGeoObjectType geoObjectType;

  @SerializedName(DHIS2SyncConfig.GEO_OBJECT_TYPE)
  private String geoObjectTypeCode;

  @SerializedName(DHIS2SyncConfig.TYPE)
  private Type                syncType;

  private Integer                 level;
  
  private Map<String, DHIS2AttributeMapping> attributes;

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

  public Integer getLevel()
  {
    return level;
  }

  public void setLevel(Integer level)
  {
    this.level = level;
  }
  
  public Boolean hasAttribute(String name)
  {
    return this.attributes != null && this.attributes.containsKey(name);
  }
  
  public DHIS2AttributeMapping getAttribute(String name)
  {
    return this.attributes.get(name);
  }
  
  public Map<String, DHIS2AttributeMapping> getAttributes()
  {
    return attributes;
  }

  public void setAttributes(Map<String, DHIS2AttributeMapping> attributes)
  {
    this.attributes = attributes;
  }
  
  @Override
  public int hashCode() {
    return new String(geoObjectTypeCode + syncType.name()).hashCode() + level;
  }

  @Override
  public int compareTo(SyncLevel o)
  {
    return this.getLevel().compareTo(o.getLevel());
  }

  public boolean isAttributeMapped(String name)
  {
    if (this.hasAttribute(name))
    {
      DHIS2AttributeMapping mapping = this.attributes.get(name);
      
      return mapping.getExternalId() != null && mapping.getExternalId().length() > 0;
    }
    
    return false;
  }

}
