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
package net.geoprism.registry.etl.export;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.model.ServerGeoObjectIF;

public class SeverGeoObjectJsonAdapters
{
  public static class ServerGeoObjectSerializer implements JsonSerializer<ServerGeoObjectIF>
  {
    private Date startDate;
    
    private Date endDate;
    
    public ServerGeoObjectSerializer(Date startDate, Date endDate)
    {
      this.startDate = startDate;
      this.endDate = endDate;
    }
    
    @Override
    public JsonElement serialize(ServerGeoObjectIF sgo, Type typeOfSrc, JsonSerializationContext context)
    {
      return context.serialize(sgo.toGeoObject(this.startDate, this.endDate));
    }
  }

  public static class LocalizedValueSerializer implements JsonSerializer<LocalizedValue>
  {
    @Override
    public JsonElement serialize(LocalizedValue value, Type typeOfSrc, JsonSerializationContext context)
    {
      return value.toJSON();
    }
  }

  public static class DateSerializer implements JsonSerializer<Date>
  {
    private SimpleDateFormat format;

    public DateSerializer()
    {
      format = new SimpleDateFormat("yyyy-MM-dd");
      format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);
    }

    @Override
    public JsonElement serialize(Date value, Type typeOfSrc, JsonSerializationContext context)
    {
      return new JsonPrimitive(format.format(value));
    }
  }

}
