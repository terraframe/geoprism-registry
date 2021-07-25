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
package net.geoprism.registry.etl;

import java.util.SortedSet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.graph.FhirExternalSystem;

public class FhirSyncConfig extends ExternalSystemSyncConfig
{
  public static final String       LEVELS = "levels";

  public static final String       TYPE   = "type";

  private SortedSet<FhirSyncLevel> levels;

  public SortedSet<FhirSyncLevel> getLevels()
  {
    return levels;
  }

  public void setLevels(SortedSet<FhirSyncLevel> levels)
  {
    this.levels = levels;
  }

  @Override
  public FhirExternalSystem getSystem()
  {
    return (FhirExternalSystem) super.getSystem();
  }

  @Override
  public void populate(SynchronizationConfig config)
  {
    super.populate(config);

    JsonObject json = config.getConfigurationJson();

    JsonArray jaLevels = json.get(LEVELS).getAsJsonArray();

    GsonBuilder builder = new GsonBuilder();

    Gson gson = builder.create();
    this.levels = gson.fromJson(jaLevels, new TypeToken<SortedSet<FhirSyncLevel>>()
    {
    }.getType());
  }

}
