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

import java.util.ArrayList;
import java.util.List;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.metadata.CustomSerializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class AttributeList<T> extends Attribute
{

  private static final long serialVersionUID = 6874451021655655964L;
  
  public static final String TYPE = "list";
  
  private List<T> list;
  
  private String elementType;

  public AttributeList(String name, String elementType)
  {
    super(name, AttributeList.TYPE);
    this.elementType = elementType;
  }

  @Override
  public List<T> getValue()
  {
    return this.list;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setValue(Object value)
  {
    this.list = (List<T>) value;
  }
  
  public String getElementType()
  {
    return elementType;
  }

  public void setElementType(String elementType)
  {
    this.elementType = elementType;
  }

  @Override
  public JsonElement toJSON(CustomSerializer serializer)
  {
    if (this.getValue() != null)
    {
      JsonArray ja = new JsonArray();
      List<T> list = this.getValue();
      
      for (T t : list)
      {
        if (t instanceof String)
        {
          ja.add((String) t);
        }
        else if (t instanceof Number)
        {
          ja.add((Number) t); 
        }
        else if (t instanceof AlternateId)
        {
          String json = ((AlternateId) t).toJSON().toString();
          
          ja.add(JsonParser.parseString(json));
        }
        else
        {
          throw new UnsupportedOperationException("Must be a primitive");
        }
      }

      return ja;
    }
    
    return null;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void fromJSON(JsonElement jValue, RegistryAdapter registry)
  {
    if (jValue == null || jValue.isJsonNull())
    {
      this.setValue(null);
    }
    else if (! jValue.isJsonArray())
    {
      throw new UnsupportedOperationException();
    }
    else
    {
      List<T> list = new ArrayList<T>();
      JsonArray ja = jValue.getAsJsonArray();
      
      for (int i = 0; i < ja.size(); ++i)
      {
        JsonElement ele = ja.get(i);
        
        if (this.elementType.equals(DefaultAttribute.ALTERNATE_ID_ELEMENT_TYPE))
        {
          list.add((T) AlternateId.fromJSON(ele));
        }
        else if (this.elementType.equals(String.class.getName()))
        {
          list.add((T) ele.getAsString());
        }
        else if (this.elementType.equals(Integer.class.getName()) || this.elementType.equals(Double.class.getName()) || this.elementType.equals(Float.class.getName())
            || this.elementType.equals(Long.class.getName()))
        {
          list.add((T) ele.getAsNumber());
        }
        else
        {
          throw new UnsupportedOperationException("Element [" + ele.toString() + "] not supported for list with expected element type [" + this.elementType + "].");
        }
      }
      
      this.setValue(list);
    }
  }

}
