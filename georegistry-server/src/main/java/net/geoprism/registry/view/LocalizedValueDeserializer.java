package net.geoprism.registry.view;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class LocalizedValueDeserializer extends JsonDeserializer<LocalizedValue>
{
  @Override
  public LocalizedValue deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException, JsonProcessingException
  {
    String value = jsonParser.getText();
    if (!StringUtils.isBlank(value))
    {
      return LocalizedValue.fromJSON(com.google.gson.JsonParser.parseString(value).getAsJsonObject());
    }

    return null;
  }
}