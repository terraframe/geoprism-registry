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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.poi.ss.util.CellReference;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.ProgrammingErrorException;

import net.geoprism.data.etl.excel.ColumnType;
import net.geoprism.data.etl.excel.ExcelFormulaException;
import net.geoprism.data.etl.excel.ExcelHeaderException;
import net.geoprism.data.etl.excel.InvalidHeaderRowException;
import net.geoprism.data.etl.excel.SheetHandler;
import net.geoprism.registry.io.GeoObjectImportConfiguration;

public class ExcelFieldContentsHandler implements SheetHandler
{
  private static final Logger logger = LoggerFactory.getLogger(ExcelFieldContentsHandler.class);

  public static class Field
  {
    /**
     * Number of unique values
     */
    private static final int LIMIT = 10;

    private String           name;

    private int              precision;

    private int              scale;

    private int              inputPosition;

    private Set<ColumnType>  dataTypes;

    private Set<String>      values;

    public Field()
    {
      this.dataTypes = new TreeSet<ColumnType>();
      this.precision = 0;
      this.scale = 0;
      this.values = new HashSet<String>(LIMIT);
    }

    public void setName(String name)
    {
      this.name = name.trim();
    }

    public String getName()
    {
      return name.trim();
    }

    public void setInputPosition(int position)
    {
      this.inputPosition = position;
    }

    public int getInputPosition()
    {
      return this.inputPosition;
    }

    public void addDataType(ColumnType dataType)
    {
      this.dataTypes.add(dataType);
    }

    public void setPrecision(Integer precision)
    {
      this.precision = Math.max(this.precision, precision);
    }

    public void setScale(Integer scale)
    {
      this.scale = Math.max(this.scale, scale);
    }

    public void addValue(String value)
    {
      if (this.values.size() < LIMIT)
      {
        this.values.add(value);
      }
    }

    public String getBaseType()
    {
      if (this.dataTypes.size() == 1)
      {
        ColumnType type = this.dataTypes.iterator().next();

        if (type.equals(ColumnType.NUMBER))
        {
          return GeoObjectImportConfiguration.NUMERIC;
        }
        else if (type.equals(ColumnType.DATE))
        {
          return AttributeDateType.TYPE;
        }
        else if (type.equals(ColumnType.BOOLEAN))
        {
          return AttributeBooleanType.TYPE;
        }
        else if ( ( type.equals(ColumnType.TEXT) && this.values.size() < LIMIT ))
        {
          return GeoObjectImportConfiguration.TEXT;
        }
      }

      return GeoObjectImportConfiguration.TEXT;
    }

    // public JsonObject toJSON()
    // {
    // JsonObject object = new JsonObject();
    // object.addProperty("name", this.name.trim());
    //
    // if (label == null)
    // {
    // object.addProperty("label", this.name.trim());
    // }
    // else
    // {
    // object.addProperty("label", this.label.trim());
    // }
    //
    // object.addProperty("aggregatable", true);
    // object.addProperty("fieldPosition", this.getInputPosition());
    //
    // if (this.dataTypes.size() == 1)
    // {
    // ColumnType type = this.dataTypes.iterator().next();
    //
    // object.addProperty("type", type.name());
    // object.addProperty("columnType", type.name());
    // object.addProperty("accepted", false);
    //
    // if (type.equals(ColumnType.NUMBER))
    // {
    // if (this.scale > 0)
    // {
    // object.addProperty("precision", ( this.precision + this.scale ));
    // object.addProperty("scale", this.scale);
    // object.addProperty("type", GeoObjectImportConfiguration.NUMERIC);
    // object.addProperty("ratio", false);
    // }
    // else
    // {
    // object.addProperty("type", GeoObjectImportConfiguration.NUMERIC);
    // }
    // }
    // else if (type.equals(ColumnType.DATE))
    // {
    // object.addProperty("type", AttributeDateType.TYPE);
    // }
    // else if (type.equals(ColumnType.BOOLEAN))
    // {
    // object.addProperty("type", AttributeBooleanType.TYPE);
    // }
    // else if ( ( type.equals(ColumnType.TEXT) && this.values.size() < LIMIT ))
    // {
    // object.addProperty("type", GeoObjectImportConfiguration.TEXT);
    // }
    // }
    // else
    // {
    // object.addProperty("columnType", ColumnType.TEXT.name());
    // object.addProperty("type", GeoObjectImportConfiguration.TEXT);
    // object.addProperty("accepted", false);
    //
    // if (this.categoryId != null || this.values.size() < LIMIT)
    // {
    // object.addProperty("type", GeoObjectImportConfiguration.TEXT);
    //
    // if (this.categoryId != null)
    // {
    // object.addProperty("root", this.categoryId);
    // }
    // }
    // }
    //
    // return object;
    // }
  }

  /**
   * Current row number
   */
  private int                 rowNum;

  /**
   * Current sheet name
   */
  private String              sheetName;

  /**
   * Column-Attribute Map for the current sheet
   */
  private Map<Integer, Field> map;

  /**
   * Set of the attributes names in the current sheet
   */
  private Set<String>         attributeNames;

  /**
   * JsonArray containing attribute information for all of the sheets.
   */
  private JSONArray           sheets;

  public ExcelFieldContentsHandler()
  {
    this.sheets = new JSONArray();
  }

  public Field createField(Integer column)
  {
    if (!this.map.containsKey(column))
    {
      this.map.put(column, new Field());
    }

    return this.map.get(column);
  }

  public int getFieldPosition(String cellReference)
  {
    CellReference reference = new CellReference(cellReference);

    return reference.getCol();
  }

  public JSONArray getSheets()
  {
    return this.sheets;
  }

  @Override
  public void startSheet(String sheetName)
  {
    this.sheetName = sheetName;
    this.rowNum = 0;
    this.map = new HashMap<Integer, Field>();
    this.attributeNames = new TreeSet<String>();
  }

  private boolean validateField(Field field, Integer columnPosition)
  {
    if (field.name == null)
    {
      ExcelHeaderException exception = new ExcelHeaderException();
      exception.setRow(Integer.toString(field.getInputPosition()));
      exception.setColumn(Integer.toString(columnPosition));

      throw exception;
    }

    return true;
  }

  @Override
  public void endSheet()
  {
    try
    {
      JSONObject attributes = new JSONObject();
      attributes.put(AttributeBooleanType.TYPE, new JSONArray());
      attributes.put(GeoObjectImportConfiguration.TEXT, new JSONArray());
      attributes.put(GeoObjectImportConfiguration.NUMERIC, new JSONArray());
      attributes.put(AttributeDateType.TYPE, new JSONArray());

      Set<Entry<Integer, Field>> entrySet = this.map.entrySet();

      for (Entry<Integer, Field> entry : entrySet)
      {
        Field field = entry.getValue();
        validateField(field, entry.getKey());

        String name = field.getName();
        String baseType = field.getBaseType();

        attributes.getJSONArray(baseType).put(name);

        if (baseType.equals(GeoObjectImportConfiguration.NUMERIC))
        {
          attributes.getJSONArray(GeoObjectImportConfiguration.TEXT).put(name);
        }
      }

      JSONObject sheet = new JSONObject();
      sheet.put("name", this.sheetName);
      sheet.put("attributes", attributes);

      this.sheets.put(sheet);
    }
    catch (JSONException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  @Override
  public void startRow(int rowNum)
  {
    this.rowNum = rowNum;
  }

  @Override
  public void endRow()
  {
  }

  @Override
  public void cell(String cellReference, String contentValue, String formattedValue, ColumnType cellType)
  {
    CellReference reference = new CellReference(cellReference);
    final Integer colNum = Integer.valueOf(reference.getCol());

    if (cellType.equals(ColumnType.FORMULA))
    {
      logger.error("Your excel spreadsheet has a formula at [" + reference.formatAsString() + "]"); // TODO
                                                                                                    // :
                                                                                                    // Embed
                                                                                                    // this
                                                                                                    // into
                                                                                                    // the
                                                                                                    // exception
      throw new ExcelFormulaException();
    }

    if (this.rowNum == 0)
    {
      if (! ( cellType.equals(ColumnType.TEXT) || cellType.equals(ColumnType.INLINE_STRING) ) || !this.attributeNames.add(formattedValue))
      {
        throw new InvalidHeaderRowException();
      }

      Field attribute = this.createField(colNum);
      attribute.setName(formattedValue);
      attribute.setInputPosition(this.getFieldPosition(cellReference));
    }
    else if (formattedValue != null && formattedValue.trim().length() > 0)
    {
      Field attribute = this.map.get(colNum);

      // The reveal format has extra data at the end of the spreadsheet which
      // isn't directly mappable to an attribute
      if (attribute != null)
      {
        attribute.addDataType(cellType);

        if (cellType.equals(ColumnType.NUMBER))
        {
          BigDecimal decimal = new BigDecimal(contentValue).stripTrailingZeros();

          /*
           * Precision is the total number of digits. Scale is the number of
           * digits after the decimal place.
           */
          int precision = decimal.precision();
          int scale = decimal.scale();

          attribute.setPrecision(precision - scale);
          attribute.setScale(scale);
        }
        else if (cellType.equals(ColumnType.TEXT) || cellType.equals(ColumnType.INLINE_STRING))
        {
          attribute.addValue(contentValue);
        }
      }
    }
  }

  @Override
  public void headerFooter(String text, boolean isHeader, String tagName)
  {
  }

  @Override
  public void setDatasetProperty(String dataset)
  {
  }
}
