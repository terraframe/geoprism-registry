/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
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
