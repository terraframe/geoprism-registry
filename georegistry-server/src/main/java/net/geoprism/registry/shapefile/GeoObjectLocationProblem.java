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
package net.geoprism.registry.shapefile;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;

public class GeoObjectLocationProblem implements Comparable<GeoObjectLocationProblem>
{
  private ServerGeoObjectType type;

  private ServerGeoObjectIF   parent;

  private String              label;

  private JsonArray           context;

  public GeoObjectLocationProblem(ServerGeoObjectType type, String label, ServerGeoObjectIF parent, JsonArray context)
  {
    this.type = type;
    this.label = label;
    this.context = context;
    this.parent = parent;
  }

  public String getKey()
  {
    if (this.parent != null)
    {
      return this.parent.getCode() + "-" + this.label;
    }
    else
    {
      return this.label;
    }
  }

  public JsonObject toJSON()
  {
    JsonObject object = new JsonObject();
    object.addProperty("label", label);
    object.addProperty("type", this.type.getCode());
    object.addProperty("typeLabel", this.type.getLabel().getValue());
    object.add("context", context);

    if (this.parent != null)
    {
      object.addProperty("parent", this.parent.getCode());
    }

    return object;
  }

  @Override
  public int compareTo(GeoObjectLocationProblem o)
  {
    return this.getKey().compareTo(o.getKey());
  }
}
