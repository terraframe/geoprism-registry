package net.geoprism.registry.graph;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TreeStrategyConfiguration implements StrategyConfiguration
{

  private String code;

  private String typeCode;

  public TreeStrategyConfiguration()
  {
  }

  public TreeStrategyConfiguration(String code, String typeCode)
  {
    this.code = code;
    this.typeCode = typeCode;
  }

  public String getCode()
  {
    return code;
  }

  public void setCode(String code)
  {
    this.code = code;
  }

  public String getTypeCode()
  {
    return typeCode;
  }

  public void setTypeCode(String typeCode)
  {
    this.typeCode = typeCode;
  }

  @Override
  public JsonElement toJson()
  {
    JsonObject object = new JsonObject();
    object.addProperty("code", code);
    object.addProperty("typeCode", typeCode);

    return object;
  }
  
  @Override
  public StrategyPublisher getPublisher()
  {
    return new TreeStrategyPublisher(this);
  }

  public static TreeStrategyConfiguration parse(String jsonString)
  {
    return parse(JsonParser.parseString(jsonString));
  }

  public static TreeStrategyConfiguration parse(JsonElement element)
  {
    JsonObject object = element.getAsJsonObject();
    String typeCode = object.get("typeCode").getAsString();
    String code = object.get("code").getAsString();

    return new TreeStrategyConfiguration(code, typeCode);
  }

}
