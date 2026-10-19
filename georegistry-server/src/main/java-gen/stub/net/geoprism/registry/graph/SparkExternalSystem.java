package net.geoprism.registry.graph;

import com.google.gson.JsonObject;

public class SparkExternalSystem extends SparkExternalSystemBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 1953106797;

  public SparkExternalSystem()
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

    if (json.has(SparkExternalSystem.URL) && !json.get(SparkExternalSystem.URL).isJsonNull())
    {
      this.setUrl(json.get(SparkExternalSystem.URL).getAsString());
    }
  }

  @Override
  public JsonObject toJSON()
  {
    JsonObject object = super.toJSON();
    object.addProperty(SparkExternalSystem.URL, this.getUrl());

    return object;
  }

}
