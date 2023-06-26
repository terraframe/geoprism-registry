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

public class ExternalId extends AlternateId
{
  public static final String TYPE = "EXTERNAL_ID";

  private String externalSystemId;
  
  private String  externalSystemLabel;
  
  public ExternalId()
  {
    
  }
  
  public ExternalId(String id, String externalSystemId, String externalSystemLabel)
  {
    super(id);
    
    this.externalSystemId = externalSystemId;
    this.externalSystemLabel = externalSystemLabel;
  }

  public String getExternalSystemId()
  {
    return externalSystemId;
  }

  public void setExternalSystemId(String externalSystemId)
  {
    this.externalSystemId = externalSystemId;
  }

  public String getExternalSystemLabel()
  {
    return externalSystemLabel;
  }

  public void setExternalSystemLabel(String externalSystemLabel)
  {
    this.externalSystemLabel = externalSystemLabel;
  }
  
  @Override
  public void populate(JsonObject jo)
  {
    super.populate(jo);
    this.setExternalSystemId(jo.get("externalSystemId").getAsString());
    
    if (jo.has("externalSystemLabel"))
    {
      this.setExternalSystemLabel(jo.get("externalSystemLabel").getAsString());
    }
  }
  
  @Override
  public JsonElement toJSON()
  {
    JsonObject jo = super.toJSON().getAsJsonObject();
    jo.addProperty(AlternateId.TYPE, TYPE);
    jo.addProperty("externalSystemId", this.getExternalSystemId());
    jo.addProperty("externalSystemLabel", this.getExternalSystemLabel());
    return jo;
  }
}
