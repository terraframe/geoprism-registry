/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Common Geo Registry Adapter(tm).
 *
 * Common Geo Registry Adapter(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Common Geo Registry Adapter(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Common Geo Registry Adapter(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package org.commongeoregistry.adapter.dataaccess;

import java.lang.reflect.Type;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.CustomSerializer;
import org.commongeoregistry.adapter.metadata.DefaultSerializer;
import org.wololo.jts2geojson.GeoJSONReader;
import org.wololo.jts2geojson.GeoJSONWriter;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.vividsolutions.jts.geom.Geometry;

public class GeoObjectOverTimeJsonAdapters
{
  public static final String JSON_ATTRIBUTES  = "attributes";
  
  public static final String JSON_TYPE = "type";
  
  public static final String JSON_CODE = "code";
  
  public static class GeoObjectDeserializer implements JsonDeserializer<GeoObjectOverTime>
  {
    private RegistryAdapter registry;

    public GeoObjectDeserializer(RegistryAdapter registry)
    {
      this.registry = registry;
    }
    
    public static String getCode(String goJson)
    {
      JsonObject jo = new JsonParser().parse(goJson).getAsJsonObject();
      
      JsonObject attributes = jo.get(GeoObjectOverTimeJsonAdapters.JSON_ATTRIBUTES).getAsJsonObject();
      
      return attributes.get(GeoObjectOverTimeJsonAdapters.JSON_CODE).getAsString();
    }
    
    public static String getTypeCode(String goJson)
    {
      JsonObject jo = new JsonParser().parse(goJson).getAsJsonObject();
      
      JsonObject attributes = jo.get(GeoObjectOverTimeJsonAdapters.JSON_ATTRIBUTES).getAsJsonObject();
      
      return attributes.get(GeoObjectOverTimeJsonAdapters.JSON_TYPE).getAsString();
    }

    @Override
    public GeoObjectOverTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
      JsonObject joGO = json.getAsJsonObject();
      JsonObject joAttrs = joGO.getAsJsonObject(JSON_ATTRIBUTES);
      
      String type = joAttrs.get(DefaultAttribute.TYPE.getName()).getAsString();

      GeoObjectOverTime geoObj;
      if (joAttrs.has("uid"))
      {
        geoObj = registry.newGeoObjectOverTimeInstance(type, false);
      }
      else
      {
        geoObj = registry.newGeoObjectOverTimeInstance(type, true);
      }

      for (String key : geoObj.votAttributeMap.keySet())
      {
        ValueOverTimeCollectionDTO votc = geoObj.votAttributeMap.get(key);
        votc.clear();

        if (joAttrs.has(key) && !joAttrs.get(key).isJsonNull())
        {
          JsonObject attributeOverTime = joAttrs.get(key).getAsJsonObject();
          
          JsonArray jaValues = attributeOverTime.get("values").getAsJsonArray();
          
          for (int i = 0; i < jaValues.size(); ++i)
          {
            ValueOverTimeDTO vot = ValueOverTimeDTO.fromJSON(jaValues.get(i).toString(), votc, registry);
            
            votc.add(vot);
          }
        }
      }
      
      for (String key : geoObj.attributeMap.keySet())
      {
        Attribute attr = geoObj.attributeMap.get(key);

        if (joAttrs.has(key) && !joAttrs.get(key).isJsonNull())
        {
          attr.fromJSON(joAttrs.get(key), registry);
        }
      }
      
      return geoObj;
    }
  }

  public static class GeoObjectSerializer implements JsonSerializer<GeoObjectOverTime>
  {
    private CustomSerializer serializer;

    public GeoObjectSerializer(CustomSerializer serializer)
    {
      this.serializer = serializer;
    }

    public GeoObjectSerializer()
    {
      this.serializer = new DefaultSerializer();
    }

    @Override
    public JsonElement serialize(GeoObjectOverTime go, Type typeOfSrc, JsonSerializationContext context)
    {
      JsonObject jsonObj = new JsonObject();
      
      JsonObject attrs = new JsonObject();
      for (String key : go.votAttributeMap.keySet())
      {
        ValueOverTimeCollectionDTO votc = go.votAttributeMap.get(key);
        AttributeType type = votc.getAttributeType();
        
        JsonObject attributeOverTime = new JsonObject();
        attributeOverTime.addProperty("name", type.getName());
        attributeOverTime.addProperty("type", type.getType());
        
        JsonArray values = new JsonArray();
        
        for (ValueOverTimeDTO vot : votc)
        {
          values.add(vot.toJSON(serializer));
        }
        
        attributeOverTime.add("values", values);
        
        attrs.add(type.getName(), attributeOverTime);
      }
      
      for (String key : go.attributeMap.keySet())
      {
        Attribute attr = go.attributeMap.get(key);
        
        JsonElement value = attr.toJSON(serializer);
        if (!value.isJsonNull())
        {
          attrs.add(attr.getName(), value);
        }
      }

      jsonObj.add(JSON_ATTRIBUTES, attrs);

      return jsonObj;
    }
  }

}
