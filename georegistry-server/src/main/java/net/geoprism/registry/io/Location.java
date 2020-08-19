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
package net.geoprism.registry.io;

import org.json.JSONObject;

import net.geoprism.data.importer.BasicColumnFunction;
import net.geoprism.data.importer.ShapefileFunction;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;

public class Location
{
  private ServerGeoObjectType type;

  private ServerHierarchyType hierarchy;

  private ShapefileFunction   function;

  private ParentMatchStrategy matchStrategy;

  public Location(ServerGeoObjectType type, ServerHierarchyType hierarchy, ShapefileFunction function, ParentMatchStrategy matchStrategy)
  {
    this.type = type;
    this.hierarchy = hierarchy;
    this.function = function;
    this.matchStrategy = matchStrategy;
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

  public JSONObject toJSON()
  {
    JSONObject object = new JSONObject();
    object.put("label", this.type.getLabel().getValue());
    object.put("code", this.type.getCode());
    object.put("target", this.function.toJson());
    object.put(GeoObjectImportConfiguration.MATCH_STRATEGY, this.matchStrategy);

    return object;
  }
}
