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
package net.geoprism.registry.etl;

import java.util.Date;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTimeCollection;
import com.runwaysdk.localization.LocalizationFacade;

import net.geoprism.dhis2.dhis2adapter.response.model.Attribute;
import net.geoprism.dhis2.dhis2adapter.response.model.ValueType;
import net.geoprism.registry.etl.export.dhis2.DHIS2GeoObjectJsonAdapters;
import net.geoprism.registry.etl.export.dhis2.DHIS2OptionCache;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;

abstract public class DHIS2VOTDateAttributeMapping extends DHIS2AttributeMapping
{
  
  private static final Logger logger = LoggerFactory.getLogger(DHIS2StartDateAttributeMapping.class);
  
  @Override
  protected void writeAttributeValue(AttributeType attr, String propertyName, Object value, JsonObject json)
  {
    json.addProperty(propertyName, DHIS2GeoObjectJsonAdapters.DHIS2Serializer.formatDate((Date) value));
  }
  
  protected Object getAttributeValue(VertexServerGeoObject serverGo, Date date, AttributeType attr, ServerGeoObjectType got)
  {
    if (date == null)
    {
      ValueOverTimeCollection votc = serverGo.getValuesOverTime(attr.getName());

      if (votc.size() > 0)
      {
        return this.getVOTDate(votc.get(votc.size() - 1));
      }
    }
    else
    {
      ValueOverTimeCollection votc = serverGo.getValuesOverTime(attr.getName());
      
      for (ValueOverTime vot : votc)
      {
        if (vot.between(date))
        {
          return this.getVOTDate(vot);
        }
      }
    }
    
    return null;
  }
  
  abstract protected Date getVOTDate(ValueOverTime vot);
  
  @Override
  protected JsonArray buildDhis2Attributes(final DHIS2OptionCache optionCache, List<Attribute> dhis2Attrs, AttributeType cgrAttr)
  {
    JsonArray jaDhis2Attrs = new JsonArray();
    
    for (Attribute dhis2Attr : dhis2Attrs)
    {
      if (!dhis2Attr.getOrganisationUnitAttribute() || dhis2Attr.getValueType() == null)
      {
        continue;
      }

      boolean valid = false;

      JsonObject jo = new JsonObject();

      if (dhis2Attr.getOptionSetId() == null && ( dhis2Attr.getValueType().equals(ValueType.DATE) || dhis2Attr.getValueType().equals(ValueType.DATETIME) || dhis2Attr.getValueType().equals(ValueType.TIME) || dhis2Attr.getValueType().equals(ValueType.AGE) ))
      {
        valid = true;
      }

      if (valid)
      {
        jo.addProperty("dhis2Id", dhis2Attr.getId());
        jo.addProperty("code", dhis2Attr.getCode());
        jo.addProperty("name", dhis2Attr.getName());
        jaDhis2Attrs.add(jo);
      }
    }
    
    return jaDhis2Attrs;
  }

}
