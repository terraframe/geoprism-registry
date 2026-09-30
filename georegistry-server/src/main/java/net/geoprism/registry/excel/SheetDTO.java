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
