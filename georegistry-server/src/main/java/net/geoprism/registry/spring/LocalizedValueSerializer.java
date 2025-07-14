package net.geoprism.registry.spring;

import java.io.IOException;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class LocalizedValueSerializer extends JsonSerializer<LocalizedValue>
{

  @Override
  public void serialize(LocalizedValue value, JsonGenerator gen, SerializerProvider serializers) throws IOException
  {
    gen.writeString(value.toJSON().toString());
  }
}