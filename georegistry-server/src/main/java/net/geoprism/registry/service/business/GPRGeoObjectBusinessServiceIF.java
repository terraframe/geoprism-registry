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

import org.commongeoregistry.adapter.dataaccess.AlternateId;
import org.springframework.stereotype.Component;

import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;

@Component
public interface GPRGeoObjectBusinessServiceIF extends GeoObjectBusinessServiceIF
{

  public String getExternalId(ServerGeoObjectIF sgo, ExternalSystem system);
  
  public void setAlternateIds(ServerGeoObjectIF sgo, List<AlternateId> alternateIds);

  public VertexServerGeoObject getByExternalId(String externalId, ExternalSystem system, ServerGeoObjectType type);

  public void createExternalId(ServerGeoObjectIF sgo, ExternalSystem system, String id, ImportStrategy importStrategy);

}
