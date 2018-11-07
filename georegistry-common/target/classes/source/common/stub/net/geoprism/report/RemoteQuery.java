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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RemoteQuery implements RemoteQueryIF
{
  private Map<String, AttributeMetadataIF> attributes;

  private LinkedList<RemoteResultIF>       results;

  public RemoteQuery()
  {
    this.attributes = new LinkedHashMap<String, AttributeMetadataIF>();
    this.results = new LinkedList<RemoteResultIF>();
  }

  public void addResult(RemoteResultIF result)
  {
    this.results.add(result);
  }

  @Override
  public List<RemoteResultIF> getResultSet()
  {
    return this.results;
  }

  @Override
  public void clearResultSet()
  {
    this.results.clear();
  }

  public void addAttribute(AttributeMetadataIF metadata)
  {
    this.attributes.put(metadata.getName(), metadata);
  }

  @Override
  public Collection<String> getAttributeNames()
  {
    return this.attributes.keySet();
  }

  @Override
  public AttributeMetadataIF getAttributeMetadata(String attributeName)
  {
    return this.attributes.get(attributeName);
  }

  public String serialize()
  {
    try
    {
      return this.toJSON().toString();
    }
    catch (JSONException e)
    {
      throw new RuntimeException(e);
    }
  }

  public JSONObject toJSON() throws JSONException
  {
    JSONArray metadata = new JSONArray();

    Set<Entry<String, AttributeMetadataIF>> entries = this.attributes.entrySet();

    for (Entry<String, AttributeMetadataIF> entry : entries)
    {
      metadata.put(entry.getValue().toJSON());
    }

    JSONArray results = new JSONArray();

    for (RemoteResultIF result : this.results)
    {
      results.put(result.toJSON());
    }

    JSONObject object = new JSONObject();
    object.put("attributes", metadata);
    object.put("results", results);

    return object;
  }

  public static RemoteQueryIF deserialize(String json)
  {
    try
    {
      RemoteQuery query = new RemoteQuery();

      JSONObject object = new JSONObject(json);

      JSONArray attributes = object.getJSONArray("attributes");

      for (int i = 0; i < attributes.length(); i++)
      {
        JSONObject attribute = attributes.getJSONObject(i);

        AttributeMetadataIF metadata = AttributeMetadata.deserialize(attribute);

        query.addAttribute(metadata);
      }

      JSONArray results = object.getJSONArray("results");

      for (int i = 0; i < results.length(); i++)
      {
        JSONObject result = results.getJSONObject(i);

        RemoteResultIF metadata = RemoteResult.deserialize(result);

        query.addResult(metadata);
      }

      return query;
    }
    catch (JSONException e)
    {
      throw new RuntimeException(e);
    }
  }
}
