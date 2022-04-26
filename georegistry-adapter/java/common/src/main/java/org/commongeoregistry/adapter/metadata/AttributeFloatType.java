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

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.google.gson.JsonObject;

public class AttributeFloatType extends AttributeNumericType
{

  public static final String JSON_PRECISION   = "precision";

  public static final String JSON_SCALE       = "scale";

  /**
   * 
   */
  private static final long  serialVersionUID = -2000724524967535694L;

  public static String       TYPE             = "float";

  private int                precision;

  private int                scale;

  public AttributeFloatType(String _name, LocalizedValue _label, LocalizedValue _description, boolean _isDefault, boolean _required, boolean _unique)
  {
    super(_name, _label, _description, TYPE, _isDefault, _required, _unique);

    this.precision = 32;
    this.scale = 8;
  }

  public int getPrecision()
  {
    return precision;
  }

  public void setPrecision(int precision)
  {
    this.precision = precision;
  }

  public int getScale()
  {
    return scale;
  }

  public void setScale(int scale)
  {
    this.scale = scale;
  }

  @Override
  public JsonObject toJSON(CustomSerializer serializer)
  {
    JsonObject object = super.toJSON(serializer);
    object.addProperty(JSON_PRECISION, this.precision);
    object.addProperty(JSON_SCALE, this.scale);

    return object;
  }

  @Override
  public void fromJSON(JsonObject attrObj)
  {
    super.fromJSON(attrObj);

    this.precision = attrObj.get(JSON_PRECISION).getAsInt();
    this.scale = attrObj.get(JSON_SCALE).getAsInt();
  }

}
