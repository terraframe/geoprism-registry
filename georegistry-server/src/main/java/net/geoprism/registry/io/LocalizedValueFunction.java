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
