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
package org.commongeoregistry.adapter.dataaccess;

import java.io.Serializable;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeGeometryType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.CustomSerializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

public abstract class Attribute implements Serializable
{
  /**
   * 
   */
  private static final long serialVersionUID = -6682494916732516027L;

  private String            name;

  private String            type;

  public Attribute(String name, String type)
  {
    this.name = name;
    this.type = type;
  }

  public String getName()
  {
    return this.name;
  }

  /**
   * 
   * @return
   */
  public String getType()
  {
    return this.type;
  }

  public void validate(AttributeType attributeType, Object _value)
  {
    // Stub method for optional validation
  }

  public abstract Object getValue();

  public abstract void setValue(Object value);

  public static Attribute attributeFactory(AttributeType attributeType)
  {
    Attribute attribute;

    if (attributeType instanceof AttributeDateType)
    {
      attribute = new AttributeDate(attributeType.getName());
    }
    else if (attributeType instanceof AttributeIntegerType)
    {
      attribute = new AttributeInteger(attributeType.getName());
    }
    else if (attributeType instanceof AttributeFloatType)
    {
      attribute = new AttributeFloat(attributeType.getName());
    }
    else if (attributeType instanceof AttributeTermType)
    {
      attribute = new AttributeTerm(attributeType.getName());
    }
    else if (attributeType instanceof AttributeClassificationType)
    {
      attribute = new AttributeClassification(attributeType.getName());
    }
    else if (attributeType instanceof AttributeBooleanType)
    {
      attribute = new AttributeBoolean(attributeType.getName());
    }
    else if (attributeType instanceof AttributeLocalType)
    {
      attribute = new AttributeLocal(attributeType.getName());
    }
    else if (attributeType instanceof AttributeGeometryType)
    {
      attribute = new AttributeGeometry(attributeType.getName());
    }
    else
    {
      attribute = new AttributeCharacter(attributeType.getName());
    }

    return attribute;
  }

  public String toString()
  {
    return this.getName() + ": " + this.getValue();
  }

  public JsonElement toJSON(CustomSerializer serializer)
  {
    Object value = this.getValue();

    if (value == null)
    {
      return JsonNull.INSTANCE;
    }

    return new JsonPrimitive(value.toString());
  }

  public void fromJSON(JsonElement jValue, RegistryAdapter registry)
  {
    this.setValue(jValue.getAsString());
  }

}
