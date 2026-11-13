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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.json.JSONObject;

import com.runwaysdk.dataaccess.ProgrammingErrorException;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

public class SheetDTO
{
  private String                    name;

  private Map<String, List<String>> attributes;

  /**
   * Names of the columns which contain duplicate values within the imported
   * dataset
   */
  private Set<String>               nonUniqueAttributes;

  /**
   * The first few rows of the dataset ({@link #MAX_SAMPLE_ROWS} at most), shown
   * to the user as a preview while they match columns to attributes. Each row
   * maps a column name to its value as a string; values are truncated to
   * {@link #MAX_SAMPLE_VALUE_LENGTH} characters.
   */
  private List<Map<String, String>> sample;

  public static final int           MAX_SAMPLE_ROWS         = 10;

  public static final int           MAX_SAMPLE_VALUE_LENGTH = 200;

  public SheetDTO()
  {
    this.nonUniqueAttributes = new TreeSet<>();
    this.sample = new LinkedList<>();

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

  public Set<String> getNonUniqueAttributes()
  {
    return nonUniqueAttributes;
  }

  public void setNonUniqueAttributes(Set<String> nonUniqueAttributes)
  {
    this.nonUniqueAttributes = nonUniqueAttributes;
  }

  public List<Map<String, String>> getSample()
  {
    return sample;
  }

  public void setSample(List<Map<String, String>> sample)
  {
    this.sample = sample;
  }

  /**
   * @return true if the sample has reached {@link #MAX_SAMPLE_ROWS}. (Not named isSampleFull so that Jackson does not serialize it as a property.)
   */
  public boolean sampleFull()
  {
    return this.sample.size() >= MAX_SAMPLE_ROWS;
  }

  /**
   * Adds a row to the sample, unless the sample is already full. Null values
   * are omitted and long values are truncated.
   *
   * @param row
   *          Column name to value, in column order
   */
  public void addSampleRow(Map<String, ?> row)
  {
    if (this.sampleFull())
    {
      return;
    }

    Map<String, String> values = new LinkedHashMap<>();

    row.forEach((column, value) -> {
      if (value != null)
      {
        String string = value.toString();

        if (value instanceof Date)
        {
          SimpleDateFormat format = new SimpleDateFormat(GeoRegistryUtil.LOCAL_DATE_FORMAT);
          format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

          string = format.format((Date) value);
        }

        if (string.length() > MAX_SAMPLE_VALUE_LENGTH)
        {
          string = string.substring(0, MAX_SAMPLE_VALUE_LENGTH) + "…";
        }

        values.put(column, string);
      }
    });

    this.sample.add(values);
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
