/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.io;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import net.geoprism.data.importer.BasicColumnFunction;
import net.geoprism.data.importer.FeatureRow;
import net.geoprism.data.importer.ShapefileFunction;

public class LocalizedValueFunction implements ShapefileFunction
{
  private Map<String, ShapefileFunction> map;

  public LocalizedValueFunction()
  {
    this.map = new HashMap<String, ShapefileFunction>();
  }

  public void add(String locale, BasicColumnFunction function)
  {
    this.map.put(locale, function);
  }

  public ShapefileFunction getFunction(String locale)
  {
    return this.map.get(locale);
  }

  @Override
  public Object getValue(FeatureRow feature)
  {
    Map<String, String> localeValues = new HashMap<>();

    Set<Entry<String, ShapefileFunction>> entries = map.entrySet();

    for (Entry<String, ShapefileFunction> entry : entries)
    {
      String locale = entry.getKey();
      ShapefileFunction function = entry.getValue();

      Object value = function.getValue(feature);

      if (value != null)
      {
        localeValues.put(locale, value.toString());
      }
    }

    return new LocalizedValue("", localeValues);
  }

  @Override
  public String toJson()
  {
    throw new UnsupportedOperationException();
  }
}
