/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.etl;

public class FhirSyncLevel implements Comparable<FhirSyncLevel>
{

  public static enum Type {
    ALL
  }

  private String  masterListId;

  private String  versionId;

  private Type    syncType;

  private Integer level;

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

  public String getMasterListId()
  {
    return masterListId;
  }

  public void setMasterListId(String masterListId)
  {
    this.masterListId = masterListId;
  }

  public String getVersionId()
  {
    return versionId;
  }

  public void setVersionId(String versionId)
  {
    this.versionId = versionId;
  }

  @Override
  public int hashCode()
  {
    return new String(versionId + syncType.name()).hashCode() + level;
  }

  @Override
  public int compareTo(FhirSyncLevel o)
  {
    return this.getLevel().compareTo(o.getLevel());
  }
}
