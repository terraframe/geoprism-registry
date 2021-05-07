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
package net.geoprism.dhis2.dhis2adapter.response;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.geoprism.dhis2.dhis2adapter.DHIS2Constants;
import net.geoprism.dhis2.dhis2adapter.response.model.Attribute;
import net.geoprism.dhis2.dhis2adapter.response.model.ErrorReport;
import net.geoprism.dhis2.dhis2adapter.response.model.ImportParams;
import net.geoprism.dhis2.dhis2adapter.response.model.Stats;
import net.geoprism.dhis2.dhis2adapter.response.model.TypeReport;

public class MetadataGetResponse<T> extends DHIS2Response
{

  private System system;
  
  private List<T> objects = new ArrayList<T>();
  
  private String objectNamePlural;
  
  public MetadataGetResponse(String response, int statusCode, String objectNamePlural, Class<?> myType)
  {
    super(response, statusCode);
    
    init(objectNamePlural, myType);
  }
  
  public MetadataGetResponse(DHIS2Response http, String objectNamePlural, Class<?> myType)
  {
    super(http.getResponse(), http.getStatusCode());
    
    init(objectNamePlural, myType);
  }
  
  // https://exceptionshub.com/java-type-generic-as-argument-for-gson.html
  private void init(String objectNamePlural, Class<?> myType)
  {
    this.objectNamePlural = objectNamePlural;
    
    GsonBuilder builder = new GsonBuilder();
    builder.setDateFormat(DHIS2Constants.DATE_FORMAT);
    Gson gson = builder.create();
    
    if (this.getJsonObject() != null && this.getJsonObject().has(objectNamePlural))
    {
//      Type list = new TypeToken<List<DHIS2_OBJECT>>() {}.getType();
//      Type list = TypeToken.getParameterized(List.class, myType.getType()).getType();
      Type list = TypeToken.getParameterized(List.class, myType).getType();
      this.objects =  gson.fromJson(this.getJsonObject().get(objectNamePlural), list);
    }
    
    if (this.getJsonObject() != null && this.getJsonObject().has("system"))
    {
      this.system = gson.fromJson(this.getJsonObject().get("system"), System.class);
    }
  }
  
  public String getObjectNamePlural()
  {
    return objectNamePlural;
  }

  public void setObjectNamePlural(String objectNamePlural)
  {
    this.objectNamePlural = objectNamePlural;
  }

  public System getSystem()
  {
    return system;
  }

  public void setSystem(System system)
  {
    this.system = system;
  }

  public List<T> getObjects()
  {
    return this.objects;
  }

  public void setObjects(List<T> objects)
  {
    this.objects = objects;
  }
  
}
