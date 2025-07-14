package net.geoprism.registry.spring;

import java.io.IOException;

import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.gson.JsonObject;

public class GeoObjectOverTimeSerializer extends JsonSerializer<GeoObjectOverTime>
{

  @Override
  public void serialize(GeoObjectOverTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException
  {
    JsonObject json = value.toJSON();
    
    gen.writeRaw(json.toString());
  }

}
