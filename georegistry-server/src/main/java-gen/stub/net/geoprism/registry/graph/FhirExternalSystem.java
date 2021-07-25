package net.geoprism.registry.graph;

import com.google.gson.JsonObject;

import net.geoprism.registry.etl.ExternalSystemSyncConfig;
import net.geoprism.registry.etl.FhirSyncConfig;

public class FhirExternalSystem extends FhirExternalSystemBase
{
  private static final long serialVersionUID = -217307289;

  public FhirExternalSystem()
  {
    super();
  }

  @Override
  public boolean isExportSupported()
  {
    return true;
  }

  @Override
  public ExternalSystemSyncConfig configuration()
  {
    return new FhirSyncConfig();
  }

  protected void populate(JsonObject json)
  {
    super.populate(json);

    this.setUrl(json.get(FhirExternalSystem.URL).getAsString());
  }

  @Override
  public JsonObject toJSON()
  {
    JsonObject object = super.toJSON();
    object.addProperty(FhirExternalSystem.URL, this.getUrl());

    return object;
  }

}
