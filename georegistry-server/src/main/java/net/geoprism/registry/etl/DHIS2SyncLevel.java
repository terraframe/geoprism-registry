/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.annotations.SerializedName;

import net.geoprism.registry.model.ServerGeoObjectType;

public class DHIS2SyncLevel implements Comparable<DHIS2SyncLevel>
{

  public static enum Type {
    ORG_UNITS, RELATIONSHIPS, ALL, NONE
  }
  
  private transient ServerGeoObjectType geoObjectType;

  @SerializedName(DHIS2SyncConfig.GEO_OBJECT_TYPE)
  private String geoObjectTypeCode;

  @SerializedName(DHIS2SyncConfig.TYPE)
  private Type                syncType;

  private Integer                 level;
  
  private Collection<DHIS2AttributeMapping> mappings;
  
  private String         orgUnitGroupId;
  
  private transient Map<String, Set<String>> orgUnitGroupIdSet = new HashMap<String, Set<String>>();

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
  
  public Collection<DHIS2AttributeMapping> getMappings()
  {
    return mappings;
  }

  public void setMappings(Collection<DHIS2AttributeMapping> mappings)
  {
    this.mappings = mappings;
  }

  @Override
  public int hashCode() {
    return new String(geoObjectTypeCode + syncType.name()).hashCode() + level;
  }

  @Override
  public int compareTo(DHIS2SyncLevel o)
  {
    return this.getLevel().compareTo(o.getLevel());
  }

  public String getOrgUnitGroupId()
  {
    return orgUnitGroupId;
  }

  public void setOrgUnitGroupId(String orgUnitGroupId)
  {
    this.orgUnitGroupId = orgUnitGroupId;
  }
  
  public Set<String> newOrgUnitGroupIdSet(String orgUnitGroupId)
  {
    Set<String> set = new HashSet<String>();
    
    this.orgUnitGroupIdSet.put(orgUnitGroupId, set);
    
    return set;
  }
  
  public Set<String> getOrCreateOrgUnitGroupIdSet(String orgUnitGroupId)
  {
    if (!this.orgUnitGroupIdSet.containsKey(orgUnitGroupId))
    {
      this.newOrgUnitGroupIdSet(orgUnitGroupId);
    }
    
    return this.orgUnitGroupIdSet.get(orgUnitGroupId);
  }

  public Set<String> getOrgUnitGroupIdSet(String orgUnitGroupId)
  {
    return this.orgUnitGroupIdSet.get(orgUnitGroupId);
  }
  
  public Map<String, Set<String>> getOrgUnitGroupIdSet()
  {
    return this.orgUnitGroupIdSet;
  }
}
