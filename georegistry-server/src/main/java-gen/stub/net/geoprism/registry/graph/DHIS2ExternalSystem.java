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
    this.setUrl(json.get(DHIS2ExternalSystem.URL).getAsString());

    String password = json.has(DHIS2ExternalSystem.PASSWORD) ? json.get(DHIS2ExternalSystem.PASSWORD).getAsString() : null;

    if (password != null && password.length() > 0)
    {
      this.setPassword(password);
    }
  }

  @Override
  public JsonObject toJSON()
  {
    JsonObject object = super.toJSON();
    object.addProperty(DHIS2ExternalSystem.USERNAME, this.getUsername());
    object.addProperty(DHIS2ExternalSystem.URL, this.getUrl());

    return object;
  }

}
