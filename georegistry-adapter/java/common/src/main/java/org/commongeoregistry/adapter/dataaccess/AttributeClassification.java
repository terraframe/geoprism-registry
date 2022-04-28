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
import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.CustomSerializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AttributeClassification extends Attribute
{
  /**
   * 
   */
  private static final long serialVersionUID = -7912192621951141119L;

  private String            code;

  private LocalizedValue    label;

  public AttributeClassification(String name)
  {
    super(name, AttributeClassificationType.TYPE);
  }

  /**
   * Clears any existing term references and sets it to the given reference
   * 
   */
  @Override
  public void setValue(Object value)
  {
    if (value instanceof Term)
    {
      this.setValue((Term) value);
    }
    else
    {
      this.setCode((String) value);
    }
  }

  /**
   * Clears any existing term references and sets it to the given reference
   * 
   */
  public void setValue(Term term)
  {
    this.setCode(term.getCode());
    this.setLabel(term.getLabel());
  }

  public void setCode(String code)
  {
    this.code = code;
  }

  public void setLabel(LocalizedValue label)
  {
    this.label = label;
  }

  @Override
  public Object getValue()
  {
    return this.code;
  }

  @Override
  public JsonElement toJSON(CustomSerializer serializer)
  {
    JsonObject object = new JsonObject();
    object.addProperty("code", this.code);

    if (this.label != null)
    {
      object.add("label", this.label.toJSON(serializer));
    }

    return object;
  }

  @Override
  public void fromJSON(JsonElement jValue, RegistryAdapter registry)
  {
    if (jValue.isJsonPrimitive() && jValue.getAsJsonPrimitive().isString())
    {
      String termCode = jValue.getAsString();

      this.setCode(termCode);
    }
    else if (jValue.isJsonObject())
    {
      JsonObject object = jValue.getAsJsonObject();
      String termCode = object.get("code").getAsString();

      this.setCode(termCode);

      if (object.has("label"))
      {
        this.setLabel(LocalizedValue.fromJSON(object.get("label").getAsJsonObject()));
      }
    }
  }

  @Override
  public String toString()
  {
    String toString = this.getName() + ": ";

    toString += " Term: " + this.code;

    return toString;
  }

}
