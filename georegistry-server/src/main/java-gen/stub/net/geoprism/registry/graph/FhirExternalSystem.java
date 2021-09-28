package net.geoprism.registry.graph;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.json.RunwayJsonAdapters;

import net.geoprism.account.OauthServer;
import net.geoprism.registry.etl.ExternalSystemSyncConfig;
import net.geoprism.registry.etl.FhirSyncExportConfig;
import net.geoprism.registry.etl.FhirSyncImportConfig;
import net.geoprism.registry.etl.OauthExternalSystem;

public class FhirExternalSystem extends FhirExternalSystemBase implements OauthExternalSystem
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
  public void delete()
  {
    OauthServer oauth = this.getOauthServer();

    super.delete();

    if (oauth != null)
    {
      oauth.delete();
    }
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
    this.setUsername(json.get(FhirExternalSystem.USERNAME).getAsString());

    String password = json.has(FhirExternalSystem.PASSWORD) ? json.get(FhirExternalSystem.PASSWORD).getAsString() : null;

    if (password != null && password.length() > 0)
    {
      this.setPassword(password);
    }
  }

  @Override
  public JsonObject toJSON()
  {
    JsonObject object = super.toJSON();
    object.addProperty(FhirExternalSystem.URL, this.getUrl());
    object.addProperty(FhirExternalSystem.SYSTEM, this.getSystem());
    object.addProperty(FhirExternalSystem.USERNAME, this.getUsername());

    if (this.getOauthServer() != null)
    {
      Gson gson = new GsonBuilder().registerTypeAdapter(OauthServer.class, new RunwayJsonAdapters.RunwaySerializer(OAUTH_SERVER_JSON_ATTRS)).create();
      JsonElement oauthJson = gson.toJsonTree(this.getOauthServer());
      object.add(OAUTH_SERVER, oauthJson);
    }

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

  public static boolean isFhirOauth(OauthServer server)
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(FhirExternalSystem.CLASS);
    MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(OAUTHSERVER);

    StringBuilder builder = new StringBuilder();
    builder.append("SELECT COUNT(*) FROM " + mdVertex.getDBClassName());
    builder.append(" WHERE " + mdAttribute.getColumnName() + " = :server");

    final GraphQuery<Long> query = new GraphQuery<Long>(builder.toString());
    query.setParameter("server", server.getOid());

    return ( query.getSingleResult() > 0 );
  }

}
