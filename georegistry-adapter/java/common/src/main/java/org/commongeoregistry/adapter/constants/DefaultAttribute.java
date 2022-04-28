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
package org.commongeoregistry.adapter.constants;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeGeometryType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;

public enum DefaultAttribute {
  UID("uid", "UID", "The internal globally unique identifier ID", AttributeCharacterType.TYPE, true, true, false, false),

  CODE("code", "Code", "Human readable unique identified", AttributeCharacterType.TYPE, true, true, false, false),
  
  INVALID("invalid", "Invalid", "This Geo-Object is no longer valid.", AttributeBooleanType.TYPE, true, true, false, false),

  DISPLAY_LABEL("displayLabel", "Display Label", "Label of the location", AttributeLocalType.TYPE, true, false, false, true),

  TYPE("type", "Type", "The type of the GeoObject", AttributeCharacterType.TYPE, true, false, false, false),

  EXISTS("exists", "Exists", "Does this GeoObject exist", AttributeBooleanType.TYPE, true, false, false, true),

  SEQUENCE("sequence", "Sequence", "The sequence number of the GeoObject that is incremented when the object is updated", AttributeIntegerType.TYPE, true, false, false, false),

  CREATE_DATE("createDate", "Date Created", "The date the object was created", AttributeDateType.TYPE, true, false, false, false),

  LAST_UPDATE_DATE("lastUpdateDate", "Date Last Updated", "The date the object was updated", AttributeDateType.TYPE, true, false, false, false),
  
  GEOMETRY("geometry", "Geometry", "The geometries for the GeoObject.", AttributeGeometryType.TYPE, true, false, false, true);
  
//  ORGANIZATION("organization", "Organization", "The responsible organization", AttributeCharacterType.TYPE, true, false, false, false);

  private String  name;

  private String  defaultLabel;

  private String  defaultDescription;

  private String  type;

  private boolean isDefault;

  private boolean required;

  private boolean unique;
  
  private boolean isChange;

  private DefaultAttribute(String name, String defaultLabel, String defaultDescription, String type, boolean isDefault, boolean required, boolean unique, boolean isChange)
  {
    this.name =               name;
    this.defaultLabel =       defaultLabel;
    this.defaultDescription = defaultDescription;
    this.type =               type;
    this.isDefault =          isDefault;
    this.required =           required;
    this.unique =             unique;
    this.isChange =           isChange;
  }

  public String getName()
  {
    return this.name;
  }

  public String getDefaultLocalizedName()
  {
    return this.defaultLabel;
  }

  public String getDefaultDescription()
  {
    return this.defaultDescription;
  }

  public String getType()
  {
    return this.type;
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
  
  public boolean isChangeOverTime()
  {
    return isChange;
  }

  public void setChangeOverTime(boolean isChange)
  {
    this.isChange = isChange;
  }
  
  public static DefaultAttribute getByAttributeName(String attributeName)
  {
    DefaultAttribute[] all = DefaultAttribute.values();
    
    for (int i = 0; i < all.length; ++i)
    {
      if (all[i].getName().equals(attributeName))
      {
        return all[i];
      }
    }
    
    return null;
  }

  public AttributeType createAttributeType()
  {
    LocalizedValue label = new LocalizedValue(this.getDefaultLocalizedName());
    LocalizedValue description = new LocalizedValue(this.getDefaultDescription());

    return AttributeType.factory(this.getName(), label, description, this.getType(), this.isRequired(), this.isUnique(), this.isChangeOverTime());
  }
}
