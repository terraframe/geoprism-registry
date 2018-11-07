/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism;

import org.json.JSONArray;
import org.json.JSONException;

import com.runwaysdk.mvc.JsonConfiguration;
import com.runwaysdk.mvc.JsonSerializable;
import com.runwaysdk.mvc.RestSerializer;

public class ListSerializable implements JsonSerializable
{
  private Iterable<?> list;

  public ListSerializable(Iterable<?> list)
  {
    this.list = list;
  }

  @Override
  public Object serialize(RestSerializer serializer, JsonConfiguration configuration) throws JSONException
  {
    JSONArray serialized = new JSONArray();

    for (Object object : this.list)
    {
      serialized.put(serializer.serialize(object, configuration));
    }

    return serialized;
  }
}
