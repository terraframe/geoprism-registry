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
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.CustomSerializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AttributeFloat extends Attribute
{

  /**
   * 
   */
  private static final long serialVersionUID = 585645995864808480L;
  
  private Double floatValue;
  
  public AttributeFloat(String name)
  {
    super(name, AttributeFloatType.TYPE);
    
    this.floatValue = null;
  }
  
  @Override
  public void setValue(Object floatValue)
  {
    this.setFloat((Double)floatValue);
  }
  
  public void setFloat(Double floatValue)
  {
    this.floatValue = floatValue;
  }
  
  @Override
  public Double getValue()
  {
    return this.floatValue;
  }
  
  @Override
  public void fromJSON(JsonElement jValue, RegistryAdapter registry)
  {
    this.setValue(jValue.getAsDouble());
  }


}
