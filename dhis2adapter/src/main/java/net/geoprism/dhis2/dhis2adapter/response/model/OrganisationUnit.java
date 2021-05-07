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

import java.util.Date;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class OrganisationUnit
{
  private String code;
  
  private String created;
  
  private String lastUpdated;
  
  private String name;
  
  private String id;
  
  private String shortName;
  
  private Integer level;
  
  private String path;
  
  private String coordinates;
  
  private String featureType;
  
  private String address;
  
  private String description;
  
  private String contactPersion;
  
  private String url;
  
  private String phoneNumber;
  
  private Date closedDate;
  
  private String comment;
  
  private Date openingDate;
  
  private String email;
  
  private JsonObject lastUpdatedBy;
  
  private JsonObject createdBy;
  
  private JsonArray attributeValues;
  
  private JsonArray translations;

  public String getAddress()
  {
    return address;
  }

  public void setAddress(String address)
  {
    this.address = address;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getContactPersion()
  {
    return contactPersion;
  }

  public void setContactPersion(String contactPersion)
  {
    this.contactPersion = contactPersion;
  }

  public String getUrl()
  {
    return url;
  }

  public void setUrl(String url)
  {
    this.url = url;
  }

  public String getPhoneNumber()
  {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber)
  {
    this.phoneNumber = phoneNumber;
  }

  public Date getClosedDate()
  {
    return closedDate;
  }

  public void setClosedDate(Date closedDate)
  {
    this.closedDate = closedDate;
  }

  public String getComment()
  {
    return comment;
  }

  public void setComment(String comment)
  {
    this.comment = comment;
  }

  public Date getOpeningDate()
  {
    return openingDate;
  }

  public void setOpeningDate(Date openingDate)
  {
    this.openingDate = openingDate;
  }

  public String getEmail()
  {
    return email;
  }

  public void setEmail(String email)
  {
    this.email = email;
  }

  public JsonObject getLastUpdatedBy()
  {
    return lastUpdatedBy;
  }

  public void setLastUpdatedBy(JsonObject lastUpdatedBy)
  {
    this.lastUpdatedBy = lastUpdatedBy;
  }

  public JsonObject getCreatedBy()
  {
    return createdBy;
  }

  public void setCreatedBy(JsonObject createdBy)
  {
    this.createdBy = createdBy;
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

  public Integer getLevel()
  {
    return level;
  }

  public void setLevel(Integer level)
  {
    this.level = level;
  }

  public String getPath()
  {
    return path;
  }

  public void setPath(String path)
  {
    this.path = path;
  }

  public String getCoordinates()
  {
    return coordinates;
  }

  public void setCoordinates(String coordinates)
  {
    this.coordinates = coordinates;
  }

  public String getFeatureType()
  {
    return featureType;
  }

  public void setFeatureType(String featureType)
  {
    this.featureType = featureType;
  }
}
