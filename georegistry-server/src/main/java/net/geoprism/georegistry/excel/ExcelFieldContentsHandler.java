package net.geoprism.georegistry.excel;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.poi.ss.util.CellReference;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.json.JSONException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.dataaccess.ProgrammingErrorException;

import net.geoprism.data.etl.ColumnType;
import net.geoprism.data.etl.excel.ExcelFormulaException;
import net.geoprism.data.etl.excel.ExcelHeaderException;
import net.geoprism.data.etl.excel.InvalidHeaderRowException;
import net.geoprism.data.etl.excel.SheetHandler;
import net.geoprism.georegistry.io.GeoObjectConfiguration;

public class ExcelFieldContentsHandler implements SheetHandler
{
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

    private String           categoryId;

    private String           label;

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

    public void setLabel(String label)
    {
      this.label = label.trim();
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

    public void setCategoryId(String oid)
    {
      this.categoryId = oid;
    }

    public String getBaseType()
    {
      if (this.dataTypes.size() == 1)
      {
        ColumnType type = this.dataTypes.iterator().next();

        if (type.equals(ColumnType.NUMBER))
        {
          return GeoObjectConfiguration.NUMERIC;
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
          return GeoObjectConfiguration.TEXT;
        }
      }

      return GeoObjectConfiguration.TEXT;
    }

    public JsonObject toJSON()
    {
      JsonObject object = new JsonObject();
      object.addProperty("name", this.name.trim());

      if (label == null)
      {
        object.addProperty("label", this.name.trim());
      }
      else
      {
        object.addProperty("label", this.label.trim());
      }

      object.addProperty("aggregatable", true);
      object.addProperty("fieldPosition", this.getInputPosition());

      if (this.dataTypes.size() == 1)
      {
        ColumnType type = this.dataTypes.iterator().next();

        object.addProperty("type", type.name());
        object.addProperty("columnType", type.name());
        object.addProperty("accepted", false);

        if (type.equals(ColumnType.NUMBER))
        {
          if (this.scale > 0)
          {
            object.addProperty("precision", ( this.precision + this.scale ));
            object.addProperty("scale", this.scale);
            object.addProperty("type", GeoObjectConfiguration.NUMERIC);
            object.addProperty("ratio", false);
          }
          else
          {
            object.addProperty("type", GeoObjectConfiguration.NUMERIC);
          }
        }
        else if (type.equals(ColumnType.DATE))
        {
          object.addProperty("type", AttributeDateType.TYPE);
        }
        else if (type.equals(ColumnType.BOOLEAN))
        {
          object.addProperty("type", AttributeBooleanType.TYPE);
        }
        else if ( ( type.equals(ColumnType.TEXT) && this.values.size() < LIMIT ))
        {
          object.addProperty("type", GeoObjectConfiguration.TEXT);
        }
      }
      else
      {
        object.addProperty("columnType", ColumnType.TEXT.name());
        object.addProperty("type", GeoObjectConfiguration.TEXT);
        object.addProperty("accepted", false);

        if (this.categoryId != null || this.values.size() < LIMIT)
        {
          object.addProperty("type", GeoObjectConfiguration.TEXT);

          if (this.categoryId != null)
          {
            object.addProperty("root", this.categoryId);
          }
        }
      }

      return object;
    }
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
  private JsonArray           sheets;

  public ExcelFieldContentsHandler()
  {
    this.sheets = new JsonArray();
  }

  public Field getField(String cellReference)
  {
    CellReference reference = new CellReference(cellReference);
    Integer column = new Integer(reference.getCol());

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

  public JsonArray getSheets()
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
      JsonObject attributes = new JsonObject();
      attributes.add(AttributeBooleanType.TYPE, new JsonArray());
      attributes.add(GeoObjectConfiguration.TEXT, new JsonArray());
      attributes.add(GeoObjectConfiguration.NUMERIC, new JsonArray());
      attributes.add(AttributeDateType.TYPE, new JsonArray());

      Set<Integer> keys = this.map.keySet();

      for (Integer key : keys)
      {
        Field field = this.map.get(key);
        validateField(field, key);

        String name = field.getName();
        String baseType = field.getBaseType();

        attributes.get(baseType).getAsJsonArray().add(name);
      }

      JsonObject sheet = new JsonObject();
      sheet.addProperty("name", this.sheetName);
      sheet.add("attributes", attributes);

      this.sheets.add(sheet);
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
    if (cellType.equals(ColumnType.FORMULA))
    {
      throw new ExcelFormulaException();
    }

    if (this.rowNum == 0)
    {
      if (! ( cellType.equals(ColumnType.TEXT) || cellType.equals(ColumnType.INLINE_STRING) ) || !this.attributeNames.add(formattedValue))
      {
        throw new InvalidHeaderRowException();
      }

      Field attribute = this.getField(cellReference);
      attribute.setName(formattedValue);
      attribute.setInputPosition(this.getFieldPosition(cellReference));
    }
    else
    {
      Field attribute = this.getField(cellReference);
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

  @Override
  public void headerFooter(String text, boolean isHeader, String tagName)
  {
  }

  @Override
  public void setDatasetProperty(String dataset)
  {
  }
}
