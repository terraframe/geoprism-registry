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
package net.geoprism.registry.query;

import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.query.graph.VertexExternalIdRestriction;
import net.geoprism.registry.query.graph.VertexGeoObjectQuery;
import net.geoprism.registry.query.graph.VertexGeoObjectRestriction;

public class ServerExternalIdRestriction implements ServerGeoObjectRestriction
{
  private ServerGeoObjectType type;

  private ExternalSystem      system;

  private String              externalId;

  public ServerExternalIdRestriction(ServerGeoObjectType type, ExternalSystem system, String externalId)
  {
    this.type = type;
    this.system = system;
    this.externalId = externalId;
  }

  @Override
  public VertexGeoObjectRestriction create(VertexGeoObjectQuery query)
  {
    return new VertexExternalIdRestriction(this.type, this.system, this.externalId);
  }
}
