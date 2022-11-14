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
package com.runwaysdk.json;

import java.lang.reflect.Type;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdAttributePrimitiveDAOIF;
import com.runwaysdk.dataaccess.MdClassDAOIF;

/**
 * This class is a starting point for implementation of generic Runway object serialization / deserialization using Google Gson.
 * 
 * Tests currently exist for the ExternalSystem / OAuthServer usecase in JsonSerializationTest.java (which directly tests this
 * code).
 * 
 * @author rrowlands
 */
public class RunwayJsonAdapters
{
  public static class RunwayDeserializer implements JsonDeserializer<Business>
  {
    @Override
    public Business deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
      Business biz = BusinessFacade.newBusiness(typeOfT.getTypeName());
      MdClassDAOIF metadata = biz.getMdClass();
      
      if (json instanceof JsonObject)
      {
        JsonObject jo = json.getAsJsonObject();
        
        List<? extends MdAttributeDAOIF> mdAttrs = metadata.definesAttributes();
        
        for (MdAttributeDAOIF mdAttr : mdAttrs)
        {
          if (jo.has(mdAttr.definesAttribute()) && jo.get(mdAttr.definesAttribute()).getAsString().length() > 0)
          {
            if (mdAttr instanceof MdAttributePrimitiveDAOIF)
            {
              biz.setValue(mdAttr.definesAttribute(), jo.get(mdAttr.definesAttribute()).getAsString());
            }
          }
        }
      }
      
      return biz;
    }
  }
  
  public static class RunwaySerializer implements JsonSerializer<Business>
  {
    private String[] attrs;
    
    public RunwaySerializer()
    {
      this.attrs = null;
    }
    
    public RunwaySerializer(String[] attrs)
    {
      this.attrs = attrs;
    }
    
    @Override
    public JsonElement serialize(Business src, Type typeOfSrc, JsonSerializationContext context)
    {
      JsonObject json = new JsonObject();
      
      List<? extends MdAttributeConcreteDAOIF> mdAttrs = src.getMdAttributeDAOs();
      
      for (MdAttributeConcreteDAOIF mdAttr : mdAttrs)
      {
        if (mdAttr.isSystem()) { continue; }
        
        if (this.attrs == null || (ArrayUtils.contains(attrs, mdAttr.definesAttribute())))
        {
          if (mdAttr instanceof MdAttributePrimitiveDAOIF)
          {
            json.addProperty(mdAttr.definesAttribute(), src.getValue(mdAttr.definesAttribute()));
          }
        }
      }
      
      return json;
    }
  }
}
