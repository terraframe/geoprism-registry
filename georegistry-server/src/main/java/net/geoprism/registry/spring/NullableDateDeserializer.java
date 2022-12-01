package net.geoprism.registry.spring;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.io.GeoObjectImportConfiguration;

public class NullableDateDeserializer extends JsonDeserializer<Date>
{
  @Override
  public Date deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException, JsonProcessingException
  {
    String date = jsonParser.getText();
    if (!StringUtils.isEmpty(date))
    {
      SimpleDateFormat format = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);
      format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

      try
      {
        return format.parse(date);
      }
      catch (ParseException e)
      {
        throw new JsonParseException(jsonParser, "Unable to parse date [" + date + "], expected format [" + GeoObjectImportConfiguration.DATE_FORMAT + "]", e);
      }
    }

    return null;
  }
}