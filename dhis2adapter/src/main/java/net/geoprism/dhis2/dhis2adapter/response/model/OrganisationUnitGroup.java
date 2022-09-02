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
package net.geoprism.dhis2.dhis2adapter.response.model;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class OrganisationUnitGroup
{
  private String code;
  
  private String created;
  
  private String lastUpdated;
  
  private String name;
  
  private String id;
  
  private String shortName;
  
  private String symbol;
  
  private String publicAccess;
  
  private JsonObject user;
  
  private JsonArray userGroupAccesses;
  
  private JsonArray attributeValues;
  
  private JsonArray translations;
  
  private JsonArray userAccesses;
  
  private JsonArray organisationUnits;

  public String getCode()
  {
    return code;
  }

  public void setCode(String code)
  {
    this.code = code;
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

  public String getShortName()
  {
    return shortName;
  }

  public void setShortName(String shortName)
  {
    this.shortName = shortName;
  }

  public String getSymbol()
  {
    return symbol;
  }

  public void setSymbol(String symbol)
  {
    this.symbol = symbol;
  }

  public String getPublicAccess()
  {
    return publicAccess;
  }

  public void setPublicAccess(String publicAccess)
  {
    this.publicAccess = publicAccess;
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

  public JsonArray getOrganisationUnits()
  {
    return organisationUnits;
  }

  public void setOrganisationUnits(JsonArray organisationUnits)
  {
    this.organisationUnits = organisationUnits;
  }
  
  public void setOrgUnitIds(Set<String> set)
  {
    this.organisationUnits = new JsonArray();
    
    for (String id : set)
    {
      JsonObject joId = new JsonObject();
      
      joId.addProperty("id", id);
      
      this.organisationUnits.add(joId);
    }
  }
  
  public Set<String> getOrgUnitIds()
  {
    Set<String> set = new HashSet<String>();
    
    if (this.organisationUnits != null)
    {
      JsonArray ja = this.organisationUnits;
      
      for (int i = 0; i < ja.size(); ++i)
      {
        JsonObject joId = ja.get(i).getAsJsonObject();
        
        set.add(joId.get("id").getAsString());
      }
    }
    
    return set;
  }
}
