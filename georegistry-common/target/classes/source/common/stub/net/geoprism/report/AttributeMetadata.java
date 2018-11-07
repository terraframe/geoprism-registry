/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.report;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class AttributeMetadata implements AttributeMetadataIF
{
  private String  name;

  private String  label;

  private Boolean required;

  public AttributeMetadata(String name, String label, Boolean required)
  {
    this.name = name;
    this.label = label;
    this.required = required;
  }

  @Override
  public String getDisplayLabel()
  {
    return this.label;
  }

  @Override
  public String getName()
  {
    return this.name;
  }

  @Override
  public boolean isRequired()
  {
    return this.required;
  }

  public JSONObject toJSON() throws JSONException
  {
    JSONObject object = new JSONObject();
    object.put("type", this.getClass().getName());
    object.put("name", this.name);
    object.put("label", this.label);
    object.put("required", this.required);

    return object;
  }

  public static AttributeMetadataIF deserialize(JSONObject object) throws JSONException
  {
    String type = object.getString("type");
    String name = object.getString("name");
    String label = object.getString("label");
    boolean required = object.getBoolean("required");

    if (type.equals(AttributeBooleanMetadata.class.getName()))
    {
      return new AttributeBooleanMetadata(name, label, required);
    }
    else if (type.equals(AttributeCharacterMetadata.class.getName()))
    {
      int size = object.getInt("size");

      return new AttributeCharacterMetadata(name, label, required, size);
    }
    else if (type.equals(AttributeDateMetadata.class.getName()))
    {
      return new AttributeDateMetadata(name, label, required);
    }
    else if (type.equals(AttributeDateTimeMetadata.class.getName()))
    {
      return new AttributeDateTimeMetadata(name, label, required);
    }
    else if (type.equals(AttributeDecimalMetadata.class.getName()))
    {
      int precision = object.getInt("precision");
      int scale = object.getInt("scale");

      return new AttributeDecimalMetadata(name, label, required, precision, scale);
    }
    else if (type.equals(AttributeDoubleMetadata.class.getName()))
    {
      int precision = object.getInt("precision");
      int scale = object.getInt("scale");

      return new AttributeDoubleMetadata(name, label, required, precision, scale);
    }
    else if (type.equals(AttributeLongMetadata.class.getName()))
    {
      return new AttributeLongMetadata(name, label, required);
    }
    else if (type.equals(AttributeTextMetadata.class.getName()))
    {
      return new AttributeTextMetadata(name, label, required);
    }
    else if (type.equals(AttributeTimeMetadata.class.getName()))
    {
      return new AttributeTimeMetadata(name, label, required);
    }

    throw new RuntimeException("Unsupported type [" + type + "]");
  }

}
