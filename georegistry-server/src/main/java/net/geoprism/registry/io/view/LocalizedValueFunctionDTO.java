/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.io.view;

import java.util.HashMap;
import java.util.Map;

public class LocalizedValueFunctionDTO extends ColumnFunctionDTO
{
  public static final String             TYPE = "localized";

  private Map<String, ColumnFunctionDTO> map;

  public LocalizedValueFunctionDTO()
  {
    this.map = new HashMap<String, ColumnFunctionDTO>();
  }

  public Map<String, ColumnFunctionDTO> getMap()
  {
    return map;
  }

  public void setMap(Map<String, ColumnFunctionDTO> map)
  {
    this.map = map;
  }

  public void put(String key, ColumnFunctionDTO function)
  {
    this.map.put(key, function);
  }

  @Override
  public String getType()
  {
    return TYPE;
  }

}
