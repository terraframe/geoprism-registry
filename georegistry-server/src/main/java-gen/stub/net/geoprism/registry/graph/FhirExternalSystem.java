package net.geoprism.registry.graph;

import com.google.gson.JsonObject;

import net.geoprism.registry.etl.ExternalSystemSyncConfig;

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
    return false;
  }

  @Override
  public ExternalSystemSyncConfig configuration()
  {
    throw new UnsupportedOperationException();
  }

  protected void populate(JsonObject json)
  {
    super.populate(json);

    this.setUrl(json.get(DHIS2ExternalSystem.URL).getAsString());
  }

  @Override
  public JsonObject toJSON()
  {
    JsonObject object = super.toJSON();
    object.addProperty(DHIS2ExternalSystem.URL, this.getUrl());

    return object;
  }

}
