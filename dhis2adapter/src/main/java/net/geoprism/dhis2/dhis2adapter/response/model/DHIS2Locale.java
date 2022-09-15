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
