package net.geoprism.registry.view;

import java.util.HashMap;
import java.util.Map;

public class LocalizedValueFunctionDTO extends ColumnFunctionDTO
{
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

}
