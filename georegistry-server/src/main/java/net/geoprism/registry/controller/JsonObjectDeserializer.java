package net.geoprism.registry.controller;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonObjectDeserializer extends JsonDeserializer<JsonObject>
{
  @Override
  public JsonObject deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException, JsonProcessingException
  {
    JsonNode node = jsonParser.readValueAsTree();
    String text = node.toPrettyString();

    if (!StringUtils.isEmpty(text))
    {
      JsonElement element = com.google.gson.JsonParser.parseString(text.toString());
      return element.getAsJsonObject();
    }

    return null;
  }
}