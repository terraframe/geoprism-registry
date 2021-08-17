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

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.MasterList;
import net.geoprism.registry.MasterListVersion;
import net.geoprism.registry.model.ServerGeoObjectType;

public class FhirSyncLevel implements Comparable<FhirSyncLevel>
{
  public static class Serializer implements JsonSerializer<FhirSyncLevel>
  {
    public static String formatDate(Date date)
    {
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
      format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

      if (date != null)
      {
        return format.format(date);
      }
      else
      {
        return null;
      }
    }

    @Override
    public JsonElement serialize(FhirSyncLevel src, Type typeOfSrc, JsonSerializationContext context)
    {
      JsonObject jo = new JsonObject();
      jo.addProperty("masterListId", src.masterListId);
      jo.addProperty("versionId", src.versionId);
      jo.addProperty("level", src.level);

      if (src.versionId != null)
      {
        MasterListVersion version = MasterListVersion.get(src.getVersionId());
        MasterList list = version.getMasterlist();

        jo.addProperty("forDate", formatDate(version.getForDate()));
        jo.addProperty("typeLabel", list.getDisplayLabel().getValue());
      }

      return jo;
    }

  }

  private String  masterListId;

  private String  versionId;

  private Integer level;

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
    return new String(versionId).hashCode() + level;
  }

  @Override
  public int compareTo(FhirSyncLevel o)
  {
    return this.getLevel().compareTo(o.getLevel());
  }
}
