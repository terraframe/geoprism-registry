package net.geoprism.registry.etl;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.runwaysdk.json.RunwayJsonAdapters;

import net.geoprism.account.OauthServer;

public interface OauthExternalSystem
{
  public static final String   OAUTH_SERVER            = "oAuthServer";

  public static final String[] OAUTH_SERVER_JSON_ATTRS = new String[] { OauthServer.SECRETKEY, OauthServer.CLIENTID, OauthServer.PROFILELOCATION, OauthServer.AUTHORIZATIONLOCATION, OauthServer.TOKENLOCATION, OauthServer.SERVERTYPE };

  public OauthServer getOauthServer();

  public void setOauthServerId(String oid);

  public void apply();

  public void setOauthServer(OauthServer oauth);

  public LocalizedValue getLocalizedLabel();

  public default void updateOauthServer(JsonObject jo)
  {
    if (jo.has(OauthExternalSystem.OAUTH_SERVER) && !jo.get(OauthExternalSystem.OAUTH_SERVER).isJsonNull())
    {
      Gson gson2 = new GsonBuilder().registerTypeAdapter(OauthServer.class, new RunwayJsonAdapters.RunwayDeserializer()).create();
      OauthServer oauth = gson2.fromJson(jo.get(OauthExternalSystem.OAUTH_SERVER), OauthServer.class);

      OauthServer dbServer = this.getOauthServer();

      if (dbServer != null)
      {
        dbServer.lock();
        dbServer.populate(oauth);

        oauth = dbServer;
      }

      String systemLabel = this.getLocalizedLabel().getValue();
      oauth.getDisplayLabel().setValue(systemLabel);

      oauth.apply();

      this.setOauthServer(oauth);
      this.apply();
    }
    else if (this.getOauthServer() != null)
    {
      OauthServer existingOauth = this.getOauthServer();

      this.setOauthServerId(null);
      this.apply();

      existingOauth.delete();
    }
  }

}
