package net.geoprism.registry.shapefile;

import java.util.HashMap;
import java.util.Map;

import org.commongeoregistry.adapter.dataaccess.GeoObject;

import com.runwaysdk.dataaccess.ProgrammingErrorException;

public class ShapefileColumnNameGenerator
{
  private Map<String, String> columnNames;

  public ShapefileColumnNameGenerator()
  {
    this.columnNames = new HashMap<String, String>();
  }

  public String getColumnName(String name)
  {
    if (this.columnNames.containsKey(name))
    {
      return this.columnNames.get(name);
    }

    throw new ProgrammingErrorException("Unable to find column name with key [" + name + "]");
  }

  public String generateColumnName(String name)
  {
    if (!this.columnNames.containsKey(name))
    {
      String format = this.format(name);

      int count = 1;

      String value = format;

      while (this.columnNames.containsValue(value))
      {
        if (count == 1)
        {
          format = format.substring(0, format.length() - 1);
        }

        if (count == 10)
        {
          format = format.substring(0, format.length() - 1);
        }

        value = format + ( count++ );
      }

      this.columnNames.put(name, value);
    }

    return this.columnNames.get(name);
  }

  private String format(String name)
  {
    if (name.equals(GeoObject.DISPLAY_LABEL))
    {
      return "label";
    }

    return name.substring(0, Math.min(10, name.length()));
  }

}
