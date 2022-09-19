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
package net.geoprism.dhis2.dhis2adapter.response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.geoprism.dhis2.dhis2adapter.DHIS2Constants;

public class EntityGetResponse<T> extends DHIS2Response
{

  private T entity = null;
  
  public EntityGetResponse(String response, int statusCode, Class<?> entityType)
  {
    super(response, statusCode);
    
    init(entityType);
  }
  
  public EntityGetResponse(DHIS2Response http, Class<?> entityType)
  {
    super(http.getResponse(), http.getStatusCode());
    
    init(entityType);
  }
  
  @SuppressWarnings("unchecked")
  private void init(Class<?> entityType)
  {
    GsonBuilder builder = new GsonBuilder();
    builder.setDateFormat(DHIS2Constants.DATE_FORMAT);
    Gson gson = builder.create();
    
    if (this.getJsonObject() != null)
    {
      this.entity =  (T) gson.fromJson(this.getJsonObject(), entityType);
    }
  }
  
  public T getEntity()
  {
    return this.entity;
  }

  public void setEntity(T entity)
  {
    this.entity = entity;
  }
  
}
