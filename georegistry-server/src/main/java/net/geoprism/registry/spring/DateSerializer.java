package net.geoprism.registry.spring;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import net.geoprism.registry.GeoRegistryUtil;

public class DateSerializer extends JsonSerializer<Date>
{
  @Override
  public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException
  {
    if (value != null)
    {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      sdf.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

      gen.writeRawValue(sdf.format(value));
    }
  }

}