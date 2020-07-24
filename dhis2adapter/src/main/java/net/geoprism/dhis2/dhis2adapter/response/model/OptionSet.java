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
package net.geoprism.dhis2.dhis2adapter.response.model;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class OptionSet
{
  private String created;
  
  private String lastUpdated;
  
  private String name;
  
  private String id;
  
  private String publicAccess;
  
  private Integer version;
  
  private ValueType valueType;
  
  private JsonObject user;
  
  private JsonArray userGroupAccesses;
  
  private JsonArray attributeValues;
  
  private JsonArray translations;
  
  private JsonArray userAccesses;
  
  private JsonArray options;
  
  public List<String> getOptionIds()
  {
    ArrayList<String> response = new ArrayList<String>();
    
    if (options != null)
    {
      for (int i = 0; i < options.size(); ++i)
      {
        response.add(options.get(i).getAsJsonObject().get("id").getAsString());
      }
    }
    
    return response;
  }

  public String getCreated()
  {
    return created;
  }

  public void setCreated(String created)
  {
    this.created = created;
  }

  public String getLastUpdated()
  {
    return lastUpdated;
  }

  public void setLastUpdated(String lastUpdated)
  {
    this.lastUpdated = lastUpdated;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String getPublicAccess()
  {
    return publicAccess;
  }

  public void setPublicAccess(String publicAccess)
  {
    this.publicAccess = publicAccess;
  }

  public Integer getVersion()
  {
    return version;
  }

  public void setVersion(Integer version)
  {
    this.version = version;
  }

  public ValueType getValueType()
  {
    return valueType;
  }

  public void setValueType(ValueType valueType)
  {
    this.valueType = valueType;
  }

  public JsonObject getUser()
  {
    return user;
  }

  public void setUser(JsonObject user)
  {
    this.user = user;
  }

  public JsonArray getUserGroupAccesses()
  {
    return userGroupAccesses;
  }

  public void setUserGroupAccesses(JsonArray userGroupAccesses)
  {
    this.userGroupAccesses = userGroupAccesses;
  }

  public JsonArray getAttributeValues()
  {
    return attributeValues;
  }

  public void setAttributeValues(JsonArray attributeValues)
  {
    this.attributeValues = attributeValues;
  }

  public JsonArray getTranslations()
  {
    return translations;
  }

  public void setTranslations(JsonArray translations)
  {
    this.translations = translations;
  }

  public JsonArray getUserAccesses()
  {
    return userAccesses;
  }

  public void setUserAccesses(JsonArray userAccesses)
  {
    this.userAccesses = userAccesses;
  }

  public JsonArray getOptions()
  {
    return options;
  }

  public void setOptions(JsonArray options)
  {
    this.options = options;
  }
}
