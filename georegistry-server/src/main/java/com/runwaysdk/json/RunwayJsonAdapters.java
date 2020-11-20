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
        
        if (this.attrs == null || (this.attrs != null && ArrayUtils.contains(attrs, mdAttr.definesAttribute())))
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
