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
package net.geoprism.registry.service.business;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import net.geoprism.registry.Publish;
import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.etl.export.DataExportJob;
import net.geoprism.registry.graph.ExternalSystem;

@Component
public interface SynchronizationConfigBusinessServiceIF
{
  public List<SynchronizationConfig> getSynchronizations(Publish publish);

  public void delete(SynchronizationConfig synchronization);

  List<SynchronizationConfig> getAll(ExternalSystem system);

  SynchronizationConfig deserialize(JsonObject json, boolean lock);

  SynchronizationConfig deserialize(JsonObject json);

  List<SynchronizationConfig> getSynchronizationConfigsForOrg(Integer pageNumber, Integer pageSize);

  long getCount();

  List<? extends DataExportJob> getJobs(SynchronizationConfig synchronization);

  void apply(SynchronizationConfig config);

}
