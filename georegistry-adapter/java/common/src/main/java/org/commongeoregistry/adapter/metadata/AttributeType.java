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
import java.util.Locale;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.google.gson.JsonObject;

/**
 * Primary abstraction for attribute metadata on {@link GeoObjectType}.
 * 
 * @author nathan
 *
 */
public abstract class AttributeType implements Serializable
{
  /**
   * 
   */
  private static final long  serialVersionUID           = -2037233821367602621L;

  public static final String JSON_CODE                  = "code";

  public static final String JSON_LOCALIZED_LABEL       = "label";

  public static final String JSON_LOCALIZED_DESCRIPTION = "description";

  public static final String JSON_TYPE                  = "type";

  public static final String JSON_IS_DEFAULT            = "isDefault";

  public static final String JSON_REQUIRED              = "required";

  public static final String JSON_UNIQUE                = "unique";

  public static final String JSON_IS_CHANGE             = "isChangeOverTime";

  /**
   * Unique code of the attribute
   */
  private String             name;

  /**
   * Label of the attribute
   */
  private LocalizedValue     label;

  /**
   * Description of the type
   */
  private LocalizedValue     description;

  /**
   * Attribute type constant
   */
  private String             type;

  /**
   * Flag denoting if the attribute represents a default attribute as opposed to
   * a custom attribute
   */
  private boolean            isDefault;

  /**
   * Flag denoting if the attribute value is required
   */
  private boolean            required;

  /**
   * Flag denoting if the attribute value is unique
   */
  private boolean            unique;

  private boolean            isChangeOverTime;

  public AttributeType(String _name, LocalizedValue _label, LocalizedValue _description, String _type, boolean _isDefault, boolean _required, boolean _unique)
  {
    this.name = _name;
    this.label = _label;
    this.description = _description;
    this.type = _type;
    this.isDefault = _isDefault;
    this.required = _required;
    this.unique = _unique;
    this.isChangeOverTime = true;
  }

  public String getName()
  {
    return this.name;
  }

  public String getType()
  {
    return this.type;
  }

  public LocalizedValue getLabel()
  {
    return this.label;
  }

  public void setLabel(LocalizedValue label)
  {
    this.label = label;
  }

  public void setLabel(String label)
  {
    this.label.setValue(label);
  }

  public void setLabel(Locale locale, String label)
  {
    this.label.setValue(locale, label);
  }

  public void setLabel(String key, String label)
  {
    this.label.setValue(key, label);
  }

  public LocalizedValue getDescription()
  {
    return this.description;
  }

  public void setDescription(LocalizedValue description)
  {
    this.description = description;
  }

  public void setDescription(String description)
  {
    this.description.setValue(description);
  }

  public void setDescription(Locale locale, String description)
  {
    this.description.setValue(locale, description);
  }

  public void setDescription(String key, String description)
  {
    this.description.setValue(key, description);
  }

  public boolean getIsDefault()
  {
    return this.isDefault;
  }

  public boolean isRequired()
  {
    return required;
  }

  public void setRequired(boolean required)
  {
    this.required = required;
  }

  public boolean isUnique()
  {
    return unique;
  }

  public void setUnique(boolean unique)
  {
    this.unique = unique;
  }

  public void validate(Object _value)
  {
    // Stub method used to validate the value according to the metadata of the
    // AttributeType
  }

  public final JsonObject toJSON()
  {
    return this.toJSON(new DefaultSerializer());
  }

  public boolean isChangeOverTime()
  {
    return isChangeOverTime;
  }

  public void setIsChangeOverTime(boolean isChangeOverTime)
  {
    this.isChangeOverTime = isChangeOverTime;
  }

  public JsonObject toJSON(CustomSerializer serializer)
  {
    JsonObject json = new JsonObject();

    json.addProperty(JSON_CODE, this.getName());

    json.addProperty(JSON_TYPE, this.getType());

    json.add(JSON_LOCALIZED_LABEL, this.getLabel().toJSON(serializer));

    json.add(JSON_LOCALIZED_DESCRIPTION, this.getDescription().toJSON(serializer));

    json.addProperty(JSON_IS_DEFAULT, this.getIsDefault());
    json.addProperty(JSON_REQUIRED, this.isRequired());
    json.addProperty(JSON_UNIQUE, this.isUnique());
    json.addProperty(JSON_IS_CHANGE, this.isChangeOverTime());

    serializer.configure(this, json);

    return json;
  }

  /**
   * Populates any additional attributes from JSON that were not populated in
   * {@link GeoObjectType#fromJSON(String, org.commongeoregistry.adapter.RegistryAdapter)}
   * 
   * @param attrObj
   * @return {@link AttributeType}
   */
  public void fromJSON(JsonObject attrObj)
  {
  }

  public static AttributeType factory(String _name, LocalizedValue _label, LocalizedValue _description, String _type, boolean _required, boolean _unique, boolean _isChange)
  {
    AttributeType attributeType = null;
    
    DefaultAttribute defaultAttr = DefaultAttribute.getByAttributeName(_name);
    boolean _isDefault = defaultAttr == null ? false : defaultAttr.getIsDefault();

    if (_type.equals(AttributeCharacterType.TYPE))
    {
      attributeType = new AttributeCharacterType(_name, _label, _description, _isDefault, _required, _unique);
    }
    else if (_type.equals(AttributeLocalType.TYPE))
    {
      attributeType = new AttributeLocalType(_name, _label, _description, _isDefault, _required, _unique);
    }
    else if (_type.equals(AttributeDateType.TYPE))
    {
      attributeType = new AttributeDateType(_name, _label, _description, _isDefault, _required, _unique);
    }
    else if (_type.equals(AttributeIntegerType.TYPE))
    {
      attributeType = new AttributeIntegerType(_name, _label, _description, _isDefault, _required, _unique);
    }
    else if (_type.equals(AttributeFloatType.TYPE))
    {
      attributeType = new AttributeFloatType(_name, _label, _description, _isDefault, _required, _unique);
    }
    else if (_type.equals(AttributeTermType.TYPE))
    {
      attributeType = new AttributeTermType(_name, _label, _description, _isDefault, _required, _unique);
    }
    else if (_type.equals(AttributeClassificationType.TYPE))
    {
      attributeType = new AttributeClassificationType(_name, _label, _description, _isDefault, _required, _unique);
    }
    else if (_type.equals(AttributeBooleanType.TYPE))
    {
      attributeType = new AttributeBooleanType(_name, _label, _description, _isDefault, _required, _unique);
    }
    else if (_type.equals(AttributeGeometryType.TYPE))
    {
      attributeType = new AttributeGeometryType(_name, _label, _description, _isDefault, _required, _unique);
    }

    attributeType.setIsChangeOverTime(_isChange);

    return attributeType;
  }

  public static AttributeType parse(JsonObject joAttr)
  {
    String name = joAttr.get(AttributeType.JSON_CODE).getAsString();
    boolean required = joAttr.get(AttributeType.JSON_REQUIRED).getAsBoolean();
    boolean unique = joAttr.get(AttributeType.JSON_UNIQUE).getAsBoolean();
    boolean isChange = joAttr.has(AttributeType.JSON_IS_CHANGE) ? joAttr.get(AttributeType.JSON_IS_CHANGE).getAsBoolean() : true;

    LocalizedValue attributeLabel = LocalizedValue.fromJSON(joAttr.get(AttributeType.JSON_LOCALIZED_LABEL).getAsJsonObject());
    LocalizedValue attributeDescription = LocalizedValue.fromJSON(joAttr.get(AttributeType.JSON_LOCALIZED_DESCRIPTION).getAsJsonObject());

    AttributeType attrType = AttributeType.factory(name, attributeLabel, attributeDescription, joAttr.get(AttributeType.JSON_TYPE).getAsString(), required, unique, isChange);
    attrType.fromJSON(joAttr);

    return attrType;
  }

}
