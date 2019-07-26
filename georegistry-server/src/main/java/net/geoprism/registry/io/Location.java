/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.io;

import org.commongeoregistry.adapter.metadata.GeoObjectType;

import com.google.gson.JsonObject;
import com.runwaysdk.system.gis.geo.Universal;

import net.geoprism.data.importer.BasicColumnFunction;
import net.geoprism.data.importer.ShapefileFunction;

public class Location
{

  private GeoObjectType     type;

  private Universal         universal;

  private ShapefileFunction function;

  public Location(GeoObjectType type, Universal universal, ShapefileFunction function)
  {
    this.type = type;
    this.universal = universal;
    this.function = function;
  }

  public GeoObjectType getType()
  {
    return type;
  }

  public void setType(GeoObjectType type)
  {
    this.type = type;
  }

  public Universal getUniversal()
  {
    return universal;
  }

  public void setUniversal(Universal universal)
  {
    this.universal = universal;
  }

  public ShapefileFunction getFunction()
  {
    return function;
  }

  public void setFunction(BasicColumnFunction function)
  {
    this.function = function;
  }

  public JsonObject toJson()
  {
    JsonObject object = new JsonObject();
    object.addProperty("label", this.type.getLabel().getValue());
    object.addProperty("code", this.type.getCode());
    object.addProperty("target", this.function.toJson());

    return object;
  }
}
