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
package net.geoprism.registry.axon.event.repository;

import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;

import com.google.gson.JsonObject;

import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.view.TypeClass;
import net.geoprism.registry.view.TypeInfo;

public class GeoObjectEventBuilder extends AbstractGeoObjectEventBuilder<GeoObjectOverTime>
{

  public GeoObjectEventBuilder(GeoObjectBusinessServiceIF service)
  {
    super(service);
  }

  @Override
  public String getCode()
  {
    return this.getOrThrow().getCode();
  }

  @Override
  public TypeInfo getType()
  {
    return new TypeInfo(TypeClass.GEO_OBJECT_TYPE, this.getOrThrow().getType().getCode());
  }

  @Override
  protected JsonObject toJSON()
  {
    return this.getOrThrow().toJSON();
  }

  @Override
  protected void removeAllEdges(ServerHierarchyType hierarchyType)
  {
    throw new UnsupportedOperationException();
  }
}
