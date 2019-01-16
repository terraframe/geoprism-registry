package net.geoprism.georegistry.excel;

import java.util.Map;

import net.geoprism.data.importer.FeatureRow;

public class MapFeatureRow implements FeatureRow
{
  private Map<String, Object> row;

  public MapFeatureRow(Map<String, Object> row)
  {
    this.row = row;
  }

  @Override
  public Object getValue(String attributeName)
  {
    return this.row.get(attributeName);
  }
}
