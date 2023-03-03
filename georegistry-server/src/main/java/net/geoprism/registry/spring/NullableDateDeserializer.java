/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
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