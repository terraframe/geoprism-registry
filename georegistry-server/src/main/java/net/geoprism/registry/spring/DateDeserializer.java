package net.geoprism.registry.spring;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import net.geoprism.registry.GeoRegistryUtil;

public class DateDeserializer extends JsonDeserializer<Date>
{
  @Override
  public Date deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException, JsonProcessingException
  {
    String date = jsonParser.getText();
    if (!StringUtils.isBlank(date))
    {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      sdf.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

      try
      {
        return sdf.parse(date);
      }
      catch (ParseException e)
      {
        throw new JsonParseException(jsonParser, "Failed to parse date value [" + date + "]", e);
      }

    }
    return null;
  }
}