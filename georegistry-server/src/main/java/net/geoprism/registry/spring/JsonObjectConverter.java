package net.geoprism.registry.spring;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Component
public class JsonObjectConverter implements Converter<String, JsonObject>
{

  /**
   * Override the convert method
   * 
   * @param json
   * @return
   */
  @Override
  public JsonObject convert(String json)
  {
    return JsonParser.parseString(json).getAsJsonObject();
  }
}