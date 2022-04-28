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
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.CustomSerializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

public class AttributeInteger extends Attribute
{

  /**
   * 
   */
  private static final long serialVersionUID = -2116815892488790274L;
  
  private Long integer;
  
  public AttributeInteger(String name)
  {
    super(name, AttributeIntegerType.TYPE);
    
    this.integer = null;
  }
  
  @Override
  public void setValue(Object integer)
  {
    this.setInteger((Long)integer);
  }
  
  public void setInteger(Long integer)
  {
    this.integer = integer;
  }
  
  @Override
  public Long getValue()
  {
    return this.integer;
  }
  
  public void fromJSON(JsonElement jValue, RegistryAdapter registry)
  {
    if (!(jValue instanceof JsonNull))
    {
      this.setValue(jValue.getAsLong());
    }
  }

}
