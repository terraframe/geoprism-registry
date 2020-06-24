/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.graph;

import com.google.gson.JsonObject;

import net.geoprism.registry.etl.DHIS2SyncConfig;
import net.geoprism.registry.etl.ExternalSystemSyncConfig;

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
    this.setVersion(json.get(DHIS2ExternalSystem.VERSION).getAsString());

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
    object.addProperty(DHIS2ExternalSystem.VERSION, this.getVersion());

    return object;
  }

  @Override
  public ExternalSystemSyncConfig configuration()
  {
    return new DHIS2SyncConfig();
  }

}
