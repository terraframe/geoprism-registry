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

import java.lang.reflect.Type;
import java.util.Date;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import com.runwaysdk.dataaccess.ProgrammingErrorException;

import net.geoprism.registry.etl.export.dhis2.DHIS2GeoObjectJsonAdapters;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;

public class DHIS2AttributeMapping
{
  public static final String ATTRIBUTE_MAPPING_STRATEGY_JSON = "attributeMappingStrategy";
  
  private static final Logger logger = LoggerFactory.getLogger(DHIS2AttributeMapping.class);
  
  @SerializedName(ATTRIBUTE_MAPPING_STRATEGY_JSON)
  protected String attributeMappingStrategy;
  
  protected String cgrAttrName;
  
  protected String dhis2AttrName;
  
  protected String externalId;
  
  public String getExternalId()
  {
    return externalId;
  }

  public void setExternalId(String externalId)
  {
    this.externalId = externalId;
  }

  public String getCgrAttrName()
  {
    return cgrAttrName;
  }

  public void setCgrAttrName(String name)
  {
    this.cgrAttrName = name;
  }
  
  public String getAttributeMappingStrategy()
  {
    if (attributeMappingStrategy == null || attributeMappingStrategy.length() == 0)
    {
      return DHIS2AttributeMapping.class.getName();
    }
    else
    {
      return attributeMappingStrategy;
    }
  }

  public void setAttributeMappingStrategy(String attributeMappingStrategy)
  {
    this.attributeMappingStrategy = attributeMappingStrategy;
  }

  public String getDhis2AttrName()
  {
    return dhis2AttrName;
  }

  public void setDhis2AttrName(String dhis2AttrName)
  {
    this.dhis2AttrName = dhis2AttrName;
  }

  public boolean isStandardAttribute()
  {
    return this.cgrAttrName != null && this.cgrAttrName.length() > 0
        && this.dhis2AttrName != null && this.dhis2AttrName.length() > 0
        && (this.externalId == null || this.externalId.length() == 0);
  }
  
  public boolean isCustomAttribute()
  {
    return this.cgrAttrName != null && this.cgrAttrName.length() > 0
        && this.dhis2AttrName != null && this.dhis2AttrName.length() > 0
        && this.externalId != null && this.externalId.length() > 0;
  }

  public void writeStandardAttributes(VertexServerGeoObject serverGo, JsonObject jo, DHIS2SyncConfig dhis2Config, DHIS2SyncLevel level)
  {
    if (this.isStandardAttribute())
    {
      ServerGeoObjectType got = level.getGeoObjectType();
      AttributeType attr = got.getAttribute(this.getCgrAttrName()).get();
      
      Object value = serverGo.getValue(attr.getName());
      
      if (value == null || (value instanceof String && ((String)value).length() == 0))
      {
        return;
      }
      
      this.writeAttributeValue(attr, this.dhis2AttrName, value, jo);
    }
  }

  public void writeCustomAttributes(JsonArray attributeValues, VertexServerGeoObject serverGo, DHIS2SyncConfig dhis2Config, DHIS2SyncLevel syncLevel, String lastUpdateDate, String createDate)
  {
    if (this.isCustomAttribute())
    {
      ServerGeoObjectType got = syncLevel.getGeoObjectType();
      
      AttributeType attr = got.getAttribute(this.getCgrAttrName()).get();
      Object value = serverGo.getValue(attr.getName());
      
      if (value == null || (value instanceof String && ((String)value).length() == 0))
      {
        return;
      }
      
      JsonObject av = new JsonObject();

      av.addProperty("lastUpdated", lastUpdateDate);

      av.addProperty("created", createDate);

      this.writeAttributeValue(attr, "value", value, av);

      JsonObject joAttr = new JsonObject();
      joAttr.addProperty("id", this.getExternalId());
      av.add("attribute", joAttr);

      attributeValues.add(av);
    }
  }

  protected void writeAttributeValue(AttributeType attr, String propertyName, Object value, JsonObject json)
  {
    if (attr instanceof AttributeBooleanType)
    {
      json.addProperty(propertyName, (Boolean) value);
    }
    else if (attr instanceof AttributeIntegerType)
    {
      json.addProperty(propertyName, (Long) value);
    }
    else if (attr instanceof AttributeFloatType)
    {
      json.addProperty(propertyName, (Double) value);
    }
    else if (attr instanceof AttributeDateType)
    {
      json.addProperty(propertyName, DHIS2GeoObjectJsonAdapters.DHIS2Serializer.formatDate((Date) value));
    }
    else if (attr instanceof AttributeLocalType)
    {
      json.addProperty(propertyName, ((LocalizedValue)value).getValue(LocalizedValue.DEFAULT_LOCALE));
    }
    else
    {
      json.addProperty(propertyName, String.valueOf(value));
    }
  }
  
  public static class DHIS2AttributeMappingDeserializer implements JsonDeserializer<DHIS2AttributeMapping>
  {
    private Gson defaultGson = new Gson();
    
    @Override
    public DHIS2AttributeMapping deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
      JsonObject jo = json.getAsJsonObject();
      
      String typeName;
      if (jo.has(ATTRIBUTE_MAPPING_STRATEGY_JSON))
      {
        typeName = jo.get(ATTRIBUTE_MAPPING_STRATEGY_JSON).getAsString();
      }
      else
      {
        typeName = DHIS2AttributeMapping.class.getName();
      }
      
      try
      {
        @SuppressWarnings("unchecked")
        Class<DHIS2AttributeMapping> clazz = (Class<DHIS2AttributeMapping>) DHIS2AttributeMapping.class.getClassLoader().loadClass(typeName);
        return defaultGson.fromJson(json, clazz);
      }
      catch (ClassNotFoundException | SecurityException e)
      {
        logger.error("Unable to instantiate mapping strategy.", e);
        throw new ProgrammingErrorException(e);
      }
    }
  }
  
}
