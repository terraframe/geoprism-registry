/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Common Geo Registry Adapter(tm).
 *
 * Common Geo Registry Adapter(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Common Geo Registry Adapter(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Common Geo Registry Adapter(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package org.commongeoregistry.adapter.dataaccess;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

abstract public class AlternateId
{
  public static final String TYPE = "type";
  
  private String id;
  
  public AlternateId()
  {
    
  }
  
  public AlternateId(String id)
  {
    this.id = id;
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }
  
  public void populate(JsonObject jo)
  {
    this.setId(jo.get("id").getAsString());
  }
  
  public static AlternateId fromJSON(JsonElement json)
  {
    AlternateId id;
    
    JsonObject jo = json.getAsJsonObject();
    
    final String type = jo.get(TYPE).getAsString();
    
    if (type.equals(ExternalId.TYPE))
    {
      id = new ExternalId();
    }
    else
    {
      throw new UnsupportedOperationException();
    }
    
    id.populate(jo);
    
    return id;
  }

  public JsonElement toJSON()
  {
    JsonObject jo = new JsonObject();
    jo.addProperty("id", this.id);
    return jo;
  }
}
