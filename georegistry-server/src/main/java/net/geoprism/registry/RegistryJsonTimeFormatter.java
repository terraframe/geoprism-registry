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
package net.geoprism.registry;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.runwaysdk.dataaccess.ProgrammingErrorException;

public class RegistryJsonTimeFormatter extends TypeAdapter<Date>
{

  @Override
  public void write(JsonWriter out, Date value) throws IOException
  {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    sdf.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);
    
    out.value(sdf.format(value));
  }

  @Override
  public Date read(JsonReader in) throws IOException
  {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    sdf.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);
    
    try
    {
      return sdf.parse(in.nextString());
    }
    catch (ParseException | IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }
  
}
