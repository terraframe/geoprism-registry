package net.geoprism.registry.graph;

import com.google.gson.JsonObject;

public class DHIS2ExternalSystem extends DHIS2ExternalSystemBase
{
  private static final long serialVersionUID = -1956421203;

  public DHIS2ExternalSystem()
  {
    super();
  }

  protected void populate(JsonObject json)
  {
    super.populate(json);

    this.setUsername(json.get(DHIS2ExternalSystem.USERNAME).getAsString());
    this.setPassword(json.get(DHIS2ExternalSystem.PASSWORD).getAsString());
    this.setUrl(json.get(DHIS2ExternalSystem.URL).getAsString());
  }

  @Override
  public JsonObject toJSON()
  {
    JsonObject object = this.toJSON();
    object.addProperty(DHIS2ExternalSystem.USERNAME, this.getUsername());
    object.addProperty(DHIS2ExternalSystem.URL, this.getUrl());

    return object;
  }

}
