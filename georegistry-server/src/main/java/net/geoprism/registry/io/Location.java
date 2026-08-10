/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.io;

import net.geoprism.data.importer.BasicColumnFunction;
import net.geoprism.data.importer.ShapefileFunction;
import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.graph.SourceAuthority;
import net.geoprism.registry.io.view.LocationDTO;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;

public class Location
{
  private ServerGeoObjectType type;

  private ServerHierarchyType hierarchy;

  private ShapefileFunction   function;

  private ParentMatchStrategy matchStrategy;

  private SourceAuthority     authority;

  public Location(ServerGeoObjectType type, ServerHierarchyType hierarchy, ShapefileFunction function, ParentMatchStrategy matchStrategy)
  {
    this(type, hierarchy, function, matchStrategy, null);
  }

  public Location(ServerGeoObjectType type, ServerHierarchyType hierarchy, ShapefileFunction function, ParentMatchStrategy matchStrategy, SourceAuthority authority)
  {
    this.type = type;
    this.hierarchy = hierarchy;
    this.function = function;
    this.matchStrategy = matchStrategy;
    this.authority = authority;
  }

  public ServerGeoObjectType getType()
  {
    return type;
  }

  public void setType(ServerGeoObjectType type)
  {
    this.type = type;
  }

  public ShapefileFunction getFunction()
  {
    return function;
  }

  public void setFunction(BasicColumnFunction function)
  {
    this.function = function;
  }

  public ParentMatchStrategy getMatchStrategy()
  {
    return matchStrategy;
  }

  public void setMatchStrategy(ParentMatchStrategy matchStrategy)
  {
    this.matchStrategy = matchStrategy;
  }

  public ServerHierarchyType getHierarchy()
  {
    return hierarchy;
  }

  public void setHierarchy(ServerHierarchyType hierarchy)
  {
    this.hierarchy = hierarchy;
  }

  public void setFunction(ShapefileFunction function)
  {
    this.function = function;
  }

  public SourceAuthority getAuthority()
  {
    return authority;
  }

  public void setAuthority(SourceAuthority authority)
  {
    this.authority = authority;
  }

  public LocationDTO toDTO()
  {
    LocationDTO dto = new LocationDTO();
    dto.setLabel(this.type.getLabel().getValue());
    dto.setCode(this.type.getCode());
    dto.setMatchStrategy(this.matchStrategy);

    if (function instanceof BasicColumnFunction)
    {
      dto.setTarget( ( (BasicColumnFunction) this.function ).getAttributeName());
    }
    else
    {
      dto.setFunction(ImportConfiguration.toDTO(function));
    }

    if (this.authority != null)
    {
      dto.setAuthority(this.authority.getCode());
    }

    return dto;
  }
}
