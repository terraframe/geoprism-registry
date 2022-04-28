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
package org.commongeoregistry.adapter.action.geoobject;

import org.commongeoregistry.adapter.action.AbstractActionDTO;
import org.commongeoregistry.adapter.constants.RegistryUrls;

import com.google.gson.JsonObject;

public class UpdateGeoObjectActionDTO extends AbstractActionDTO
{
  private JsonObject geoObject;
  
  public UpdateGeoObjectActionDTO()
  {
    super(RegistryUrls.GEO_OBJECT_UPDATE);
  }
  
  @Override
  protected void buildJson(JsonObject json)
  {
    super.buildJson(json);
    
    json.add(RegistryUrls.GEO_OBJECT_UPDATE_PARAM_GEOOBJECT, this.geoObject);
  }
  
  @Override
  protected void buildFromJson(JsonObject json)
  {
    super.buildFromJson(json);
    
    this.geoObject = json.get(RegistryUrls.GEO_OBJECT_UPDATE_PARAM_GEOOBJECT).getAsJsonObject();
  }
  
  public void setGeoObject(JsonObject geoObject)
  {
    this.geoObject = geoObject;
  }
  
  public JsonObject getGeoObject()
  {
    return geoObject;
  }
}
