package org.commongeoregistry.adapter.dataaccess;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ExternalId extends AlternateId
{
  public static final String TYPE = "EXTERNAL_ID";

  private String externalSystemId;
  
  private String  externalSystemLabel;
  
  public ExternalId()
  {
    
  }
  
  public ExternalId(String id, String externalSystemId, String externalSystemLabel)
  {
    super(id);
    
    this.externalSystemId = externalSystemId;
    this.externalSystemLabel = externalSystemLabel;
  }

  public String getExternalSystemId()
  {
    return externalSystemId;
  }

  public void setExternalSystemId(String externalSystemId)
  {
    this.externalSystemId = externalSystemId;
  }

  public String getExternalSystemLabel()
  {
    return externalSystemLabel;
  }

  public void setExternalSystemLabel(String externalSystemLabel)
  {
    this.externalSystemLabel = externalSystemLabel;
  }
  
  @Override
  public void populate(JsonObject jo)
  {
    super.populate(jo);
    this.setExternalSystemId(jo.get("externalSystemId").getAsString());
    
    if (jo.has("externalSystemLabel"))
    {
      this.setExternalSystemLabel(jo.get("externalSystemLabel").getAsString());
    }
  }
  
  @Override
  public JsonElement toJSON()
  {
    JsonObject jo = super.toJSON().getAsJsonObject();
    jo.addProperty(AlternateId.TYPE, TYPE);
    jo.addProperty("externalSystemId", this.getExternalSystemId());
    jo.addProperty("externalSystemLabel", this.getExternalSystemLabel());
    return jo;
  }
}
