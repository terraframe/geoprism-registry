package net.geoprism.registry.graph;

import java.util.List;

import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;

import net.geoprism.registry.etl.ExternalSystemSyncConfig;
import net.geoprism.registry.etl.FhirSyncExportConfig;
import net.geoprism.registry.etl.FhirSyncImportConfig;

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
  public ExternalSystemSyncConfig configuration(Boolean isImport)
  {
    if (isImport != null && isImport)
    {
      return new FhirSyncImportConfig();
    }

    return new FhirSyncExportConfig();
  }

  protected void populate(JsonObject json)
  {
    super.populate(json);

    this.setUrl(json.get(FhirExternalSystem.URL).getAsString());
    this.setSystem(json.get(FhirExternalSystem.SYSTEM).getAsString());
  }

  @Override
  public JsonObject toJSON()
  {
    JsonObject object = super.toJSON();
    object.addProperty(FhirExternalSystem.URL, this.getUrl());
    object.addProperty(FhirExternalSystem.SYSTEM, this.getSystem());

    return object;
  }

  public static List<FhirExternalSystem> getAll()
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(FhirExternalSystem.CLASS);

    StringBuilder builder = new StringBuilder();
    builder.append("SELECT FROM " + mdVertex.getDBClassName());

    final GraphQuery<FhirExternalSystem> query = new GraphQuery<FhirExternalSystem>(builder.toString());

    return query.getResults();
  }

}
