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

import java.util.Date;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class DHIS2Locale
{
  private String name;
  
  private Date created;
  
  private Date lastUpdated;
  
  private JsonArray translations;
  
  private boolean externalAccess;
  
  private JsonArray userGroupAccesses;
  
  private JsonArray userAccesses;
  
  private JsonArray favorites;
  
  private User lastUpdatedBy;
  
  private JsonObject sharing;
  
  private String locale;
  
  private String displayName;
  
  private boolean favorite;
  
  private String id;
  
  private JsonArray attributeValues;

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public Date getCreated()
  {
    return created;
  }

  public void setCreated(Date created)
  {
    this.created = created;
  }

  public Date getLastUpdated()
  {
    return lastUpdated;
  }

  public void setLastUpdated(Date lastUpdated)
  {
    this.lastUpdated = lastUpdated;
  }

  public JsonArray getTranslations()
  {
    return translations;
  }

  public void setTranslations(JsonArray translations)
  {
    this.translations = translations;
  }

  public boolean isExternalAccess()
  {
    return externalAccess;
  }

  public void setExternalAccess(boolean externalAccess)
  {
    this.externalAccess = externalAccess;
  }

  public JsonArray getUserGroupAccesses()
  {
    return userGroupAccesses;
  }

  public void setUserGroupAccesses(JsonArray userGroupAccesses)
  {
    this.userGroupAccesses = userGroupAccesses;
  }

  public JsonArray getUserAccesses()
  {
    return userAccesses;
  }

  public void setUserAccesses(JsonArray userAccesses)
  {
    this.userAccesses = userAccesses;
  }

  public JsonArray getFavorites()
  {
    return favorites;
  }

  public void setFavorites(JsonArray favorites)
  {
    this.favorites = favorites;
  }

  public User getLastUpdatedBy()
  {
    return lastUpdatedBy;
  }

  public void setLastUpdatedBy(User lastUpdatedBy)
  {
    this.lastUpdatedBy = lastUpdatedBy;
  }

  public JsonObject getSharing()
  {
    return sharing;
  }

  public void setSharing(JsonObject sharing)
  {
    this.sharing = sharing;
  }

  public String getLocale()
  {
    return locale;
  }

  public void setLocale(String locale)
  {
    this.locale = locale;
  }

  public String getDisplayName()
  {
    return displayName;
  }

  public void setDisplayName(String displayName)
  {
    this.displayName = displayName;
  }

  public boolean isFavorite()
  {
    return favorite;
  }

  public void setFavorite(boolean favorite)
  {
    this.favorite = favorite;
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public JsonArray getAttributeValues()
  {
    return attributeValues;
  }

  public void setAttributeValues(JsonArray attributeValues)
  {
    this.attributeValues = attributeValues;
  }
}
