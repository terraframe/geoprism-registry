package net.geoprism.registry.view.action;

import java.lang.reflect.Type;

import org.commongeoregistry.adapter.metadata.AttributeType;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import net.geoprism.registry.model.ServerGeoObjectType;

public class UpdateAttributeViewJsonAdapters
{
  public static final String PARENT_ATTR_NAME = "_PARENT_";
  
  public static AbstractUpdateAttributeView deserialize(String json, String attributeName, ServerGeoObjectType type)
  {
    GsonBuilder builder = new GsonBuilder();
    
    if (attributeName.equals(PARENT_ATTR_NAME))
    {
      builder.registerTypeAdapter(UpdateValueOverTimeView.class, new UpdateParentValueOverTimeViewDeserializer());
      
      return builder.create().fromJson(json, UpdateParentView.class);
    }
    else
    {
      AttributeType attr = type.getAttribute(attributeName).get();
      
      if (attr.isChangeOverTime())
      {
        return builder.create().fromJson(json, UpdateChangeOverTimeAttributeView.class);
      }
      else
      {
        return builder.create().fromJson(json, AbstractUpdateAttributeView.class);
      }
    }
  }

//  public static class UpdateAttributeViewDeserializer implements JsonDeserializer<UpdateAttributeView>
//  {
//    protected ServerGeoObjectType type;
//    
//    protected String attributeName;
//
//    public UpdateAttributeViewDeserializer(ServerGeoObjectType type, String attributeName)
//    {
//      this.type = type;
//      this.attributeName = attributeName;
//    }
//    
//    @Override
//    public UpdateAttributeView deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
//    {
//      if (this.attributeName.equals(PARENT_ATTR_NAME))
//      {
//        return context.deserialize(json, UpdateParentView.class);
//      }
//      else
//      {
//        AttributeType attr = type.getAttribute(this.attributeName).get();
//        
//        if (attr.isChangeOverTime())
//        {
//          return context.deserialize(json, UpdateChangeOverTimeAttributeView.class);
//        }
//        else
//        {
//          return context.deserialize(json, UpdateAttributeView.class);
//        }
//      }
//    }
//  }
  
  public static class UpdateParentValueOverTimeViewDeserializer implements JsonDeserializer<UpdateParentValueOverTimeView>
  {
    @Override
    public UpdateParentValueOverTimeView deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
      return context.deserialize(json, UpdateParentValueOverTimeView.class);
    }
  }
}
