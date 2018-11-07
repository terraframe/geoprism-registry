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
package net.geoprism.util;

import java.util.Map;



public class CollectionUtil 
{
  /**
   * Populate the key-value pair of map with the value specified. If the value is null or empty then the default value is used instead.
   * 
   * @param _map
   * @param _key
   * @param _value
   * @param _defaultValue
   */
  public static void populateMap(Map<String, Double> _map, String _key, String _value, Double _defaultValue)
  {
    if (_value != null && _value.length() > 0)
    {
      _map.put(_key, Double.parseDouble(_value));
    }
    else
    {
      _map.put(_key, _defaultValue);
    }
  }

}
