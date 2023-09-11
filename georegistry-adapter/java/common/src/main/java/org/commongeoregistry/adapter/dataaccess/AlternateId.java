/**
 *
 */
package org.commongeoregistry.adapter.dataaccess;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

abstract public class AlternateId
{
  public static final String TYPE = "type";
  
  private String id;
  
  public AlternateId()
  {
    
  }
  
  public AlternateId(String id)
  {
    this.id = id;
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }
  
  public void populate(JsonObject jo)
  {
    this.setId(jo.get("id").getAsString());
  }
  
  public static AlternateId fromJSON(JsonElement json)
  {
    AlternateId id;
    
    JsonObject jo = json.getAsJsonObject();
    
    final String type = jo.get(TYPE).getAsString();
    
    if (type.equals(ExternalId.TYPE))
    {
      id = new ExternalId();
    }
    else
    {
      throw new UnsupportedOperationException();
    }
    
    id.populate(jo);
    
    return id;
  }

  public JsonElement toJSON()
  {
    JsonObject jo = new JsonObject();
    jo.addProperty("id", this.id);
    return jo;
  }
}
