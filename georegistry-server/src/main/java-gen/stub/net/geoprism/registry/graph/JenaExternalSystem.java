package net.geoprism.registry.graph;

import com.google.gson.JsonObject;

public class JenaExternalSystem extends JenaExternalSystemBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 1676839465;

  public JenaExternalSystem()
  {
    super();
  }

  @Override
  public boolean isExportSupported()
  {
    return true;
  }
  
  protected void populate(JsonObject json)
  {
    super.populate(json);

    if (json.has(JenaExternalSystem.URL) && !json.get(JenaExternalSystem.URL).isJsonNull())
    {
      this.setUrl(json.get(JenaExternalSystem.URL).getAsString());
    }
  }

  @Override
  public JsonObject toJSON()
  {
    JsonObject object = super.toJSON();
    object.addProperty(FhirExternalSystem.URL, this.getUrl());

    return object;
  }

}
