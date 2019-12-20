/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.model.postgres;

import java.util.Locale;

import org.commongeoregistry.adapter.constants.DefaultAttribute;

import com.runwaysdk.dataaccess.ValueObject;
import com.runwaysdk.system.gis.geo.GeoEntity;

import net.geoprism.registry.model.LocationInfo;

public class ValueObjectContainer implements LocationInfo
{
  private ValueObject object;

  public ValueObjectContainer(ValueObject object)
  {
    super();
    this.object = object;
  }

  @Override
  public String getCode()
  {
    return object.getValue(GeoEntity.GEOID);
  }

  @Override
  public String getLabel() 
  {
    return object.getValue(DefaultAttribute.DISPLAY_LABEL.getName());
  }

  @Override
  public String getLabel(Locale locale)
  {
    return object.getValue(DefaultAttribute.DISPLAY_LABEL.getName() + "_" + locale.toString());
  }

}
