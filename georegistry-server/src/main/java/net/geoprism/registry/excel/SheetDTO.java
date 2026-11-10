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
package net.geoprism.registry.excel;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.json.JSONObject;

import com.runwaysdk.dataaccess.ProgrammingErrorException;

import net.geoprism.registry.io.GeoObjectImportConfiguration;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

public class SheetDTO
{
  private String                    name;

  private Map<String, List<String>> attributes;

  public SheetDTO()
  {
    this.attributes = new HashMap<>();

    this.attributes.put(AttributeBooleanType.TYPE, new LinkedList<>());
    this.attributes.put(GeoObjectImportConfiguration.TEXT, new LinkedList<>());
    this.attributes.put(GeoObjectImportConfiguration.NUMERIC, new LinkedList<>());
    this.attributes.put(AttributeDateType.TYPE, new LinkedList<>());
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public Map<String, List<String>> getAttributes()
  {
    return attributes;
  }

  public void setAttributes(Map<String, List<String>> attributes)
  {
    this.attributes = attributes;
  }

  public void put(String baseType, String attributeName)
  {
    this.attributes.putIfAbsent(baseType, new LinkedList<>());

    this.attributes.get(baseType).add(attributeName);

  }

  public static JSONObject toJSON(SheetDTO sheetDTO)
  {
    try
    {
      JsonMapper mapper = JsonMapper.shared();
      return new JSONObject(mapper.writeValueAsString(sheetDTO));
    }
    catch (JacksonException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }
}
