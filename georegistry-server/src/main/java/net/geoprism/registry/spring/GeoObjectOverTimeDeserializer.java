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

import org.apache.commons.lang.StringUtils;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import net.geoprism.registry.service.business.ServiceFactory;

public class GeoObjectOverTimeDeserializer extends JsonDeserializer<GeoObjectOverTime>
{
  @Override
  public GeoObjectOverTime deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException, JsonProcessingException
  {
    JsonNode node = jsonParser.readValueAsTree();
    String text = node.toPrettyString();

    if (!StringUtils.isEmpty(text))
    {
      return GeoObjectOverTime.fromJSON(ServiceFactory.getAdapter(), text);
          
    }

    return null;
  }
}