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
package org.commongeoregistry.adapter.metadata;

import java.io.Serializable;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class OrganizationDTO implements Serializable
{
  /**
   * 
   */
  private static final long serialVersionUID = 5581583993542023928L;

  public static final String         JSON_CODE                   = "code";
  
  public static final String         JSON_LOCALIZED_LABEL        = "label";
  
  public static final String         JSON_LOCALIZED_CONTACT_INFO = "contactInfo";
  
  /**
   * Unique but human readable identifier. For example, "MOH" for the Ministry of Health
   */
  private String                     code;

  /**
   * The localized label of this organization, such as "Ministry of Health".
   */
  private LocalizedValue             label;

  /**
   * The localized contact information of this organization.
   */
  private LocalizedValue             contactInfo;
  
  /**
   * Precondition: code cannot be null
   * Precondition: label cannot be null and its default value must contain a value.
   * Precondition: contactInfo cannot be null.
   * 
   * @param code organization code
   * @param label localized label
   * @param contactInfo localized contact information
   */
  public OrganizationDTO(String code, LocalizedValue label, LocalizedValue contactInfo)
  {
    this.init(code, label, contactInfo);
  }
 
  /**
   * 
   * @param code organization code
   * @param label localized label
   * @param contactInfo localized contact information
   */
  private void init(String code, LocalizedValue label, LocalizedValue contactInfo)
  {
    this.code =             code;
    this.label =            label;
    this.contactInfo =      contactInfo;
  }

  /**
   * Returns the code which is the human readable unique identifier.
   * 
   * @return Code value.
   */
  public String getCode()
  {
    return this.code;
  }
  
  /**
   * Returns the localized label of this {@link OrganizationDTO} used for the
   * presentation layer.
   * 
   * @return Localized label of this {@link OrganizationDTO}.
   */
  public LocalizedValue getLabel()
  {
    return this.label;
  }
  
  /**
   * Sets the localized display label of this {@link OrganizationDTO}.
   * 
   * Precondition: label may not be null
   * 
   * @param label
   */
  public void setLabel(LocalizedValue label)
  {
    this.label = label;
  }
  
  /**
   * Sets the localized display label of this {@link OrganizationDTO}.
   * 
   * Precondition: key may not be null
   * Precondition: key must represent a valid locale that has been defined on the back-end
   * 
   * @param key string of the locale name.
   * @param value value for the given locale.
   */
  public void setLabel(String key, String value)
  {
    this.label.setValue(key, value);
  }
  
  /**
   * Returns the localized label of the contact information of this {@link OrganizationDTO}.
   * 
   * @return Localized label of this {@link OrganizationDTO}.
   */
  public LocalizedValue getContactInfo()
  {
    return this.contactInfo;
  }

  
  /**
   * Sets the localized contact info of this {@link OrganizationDTO}.
   * 
   * Precondition: contactInfo may not be null
   * 
   * @param contactInfo
   */
  public void setContactInfo(LocalizedValue contactInfo)
  {
    this.contactInfo = contactInfo;
  }
  
  /**
   * Sets the localized contact info of this {@link OrganizationDTO}.
   * 
   * Precondition: key may not be null
   * Precondition: key must represent a valid locale that has been defined on the back-end
   * 
   * @param key string of the locale name.
   * @param value value for the given locale.
   */
  public void setContactInfo(String key, String value)
  {
    this.contactInfo.setValue(key, value);
  }
  
  /**
   * Creates a {@link OrganizationDTO} from the given JSON string.
   * 
   * @param sJson
   *          JSON string that defines the {@link OrganizationDTO}.
   * @return
   */
  public static OrganizationDTO fromJSON(String sJson)
  {
    JsonParser parser = new JsonParser();

    JsonObject oJson = parser.parse(sJson).getAsJsonObject();

    String code = oJson.get(JSON_CODE).getAsString();
    LocalizedValue label = LocalizedValue.fromJSON(oJson.get(JSON_LOCALIZED_LABEL).getAsJsonObject());
    LocalizedValue contactInfo = LocalizedValue.fromJSON(oJson.get(JSON_LOCALIZED_CONTACT_INFO).getAsJsonObject());

    // TODO Need to validate that the default attributes are still defined.
    OrganizationDTO geoObjType = new OrganizationDTO(code, label, contactInfo);
    
    return geoObjType;
  }
  
  
  /**
   * Return the JSON representation of this {@link OrganizationDTO}.
   * 
   * @return JSON representation of this {@link OrganizationDTO}.
   */
  public final JsonObject toJSON()
  {
    return toJSON(new DefaultSerializer());
  }
  
  /**
   * Return the JSON representation of this {@link OrganizationDTO}. Filters the
   * attributes to include in serialization.
   * 
   * @param filter
   *          Filter used to determine if an attribute is included
   * 
   * @return JSON representation of this {@link OrganizationDTO}.
   */
  public JsonObject toJSON(CustomSerializer serializer)
  {
    JsonObject json = new JsonObject();
    
    json.addProperty(JSON_CODE, this.getCode());
    
    json.add(JSON_LOCALIZED_LABEL, this.getLabel().toJSON(serializer));
    
    json.add(JSON_LOCALIZED_CONTACT_INFO, this.getContactInfo().toJSON(serializer));

    return json;
  }
  
}
