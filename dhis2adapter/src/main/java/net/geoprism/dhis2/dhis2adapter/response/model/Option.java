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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Option implements Comparable<Option>
{
  private String code;
  
  private String created;
  
  private String lastUpdated;
  
  private String name;
  
  private String id;
  
  private Integer sortOrder;
  
  private JsonObject optionSet;
  
  private JsonArray attributeValues;
  
  private JsonArray translations;

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

  public Integer getSortOrder()
  {
    return sortOrder;
  }

  public void setSortOrder(Integer sortOrder)
  {
    this.sortOrder = sortOrder;
  }
  
  public String getOptionSetId()
  {
    if (this.optionSet != null)
    {
      return this.optionSet.get("id").getAsString();
    }
    
    return null;
  }

  public JsonObject getOptionSet()
  {
    return optionSet;
  }

  public void setOptionSet(JsonObject optionSet)
  {
    this.optionSet = optionSet;
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
  
  @Override
  public int hashCode()
  {
    return id.hashCode();
  }

  @Override
  public int compareTo(Option o)
  {
    return this.getSortOrder().compareTo(o.getSortOrder());
  }
}
