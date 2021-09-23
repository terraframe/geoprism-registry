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

import com.google.gson.JsonObject;

import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.graph.FhirExternalSystem;

public class FhirSyncImportConfig extends ExternalSystemSyncConfig
{
  private String implementation;

  public String getImplementation()
  {
    return implementation;
  }

  public void setImplementation(String implementation)
  {
    this.implementation = implementation;
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

    this.implementation = json.get("implementation").getAsString();
  }

}
