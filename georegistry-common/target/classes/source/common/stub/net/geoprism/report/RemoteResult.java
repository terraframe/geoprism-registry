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
package net.geoprism.report;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

public class RemoteResult implements RemoteResultIF
{
  private Map<String, String> values;

  public RemoteResult()
  {
    this.values = new HashMap<String, String>();
  }

  @Override
  public String getValue(String columnName)
  {
    return this.values.get(columnName);
  }

  public void setValue(String columnName, String value)
  {
    this.values.put(columnName, value);
  }

  @Override
  public JSONObject toJSON() throws JSONException
  {
    JSONObject object = new JSONObject();

    Set<Entry<String, String>> entries = this.values.entrySet();

    for (Entry<String, String> entry : entries)
    {
      object.put(entry.getKey(), entry.getValue());
    }

    return object;
  }

  @SuppressWarnings("unchecked")
  public static RemoteResultIF deserialize(JSONObject object) throws JSONException
  {
    RemoteResult result = new RemoteResult();

    Iterator<String> keys = object.keys();

    while (keys.hasNext())
    {
      String key = keys.next();

      String value = object.getString(key);

      result.setValue(key, value);
    }

    return result;
  }
}
