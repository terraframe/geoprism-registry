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

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.CustomSerializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

public class AttributeBoolean extends Attribute
{
  /**
   * 
   */
  private static final long serialVersionUID = -3802068636170892383L;
  
  private Boolean            value;

  public AttributeBoolean(String name)
  {
    super(name, AttributeBooleanType.TYPE);

    this.value = null;
  }

  @Override
  public void setValue(Object value)
  {
    this.setBoolean((Boolean) value);
  }

  public void setBoolean(Boolean value)
  {
    this.value = value;
  }

  @Override
  public Boolean getValue()
  {
    return this.value;
  }

  @Override
  public void fromJSON(JsonElement jValue, RegistryAdapter registry)
  {
    this.setValue(Boolean.valueOf(jValue.getAsString()));
  }
  
  @Override
  public JsonElement toJSON(CustomSerializer serializer)
  {
    Boolean value = this.getValue();
    
    if (value == null)
    {
      return JsonNull.INSTANCE;
    }

    return new JsonPrimitive(value);
  }
  
}
