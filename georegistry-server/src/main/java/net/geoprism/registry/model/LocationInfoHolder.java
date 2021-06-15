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
package net.geoprism.registry.model;

import java.util.Locale;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

public class LocationInfoHolder implements LocationInfo
{

  private String code;
  
  private LocalizedValue label;
  
  private ServerGeoObjectType type;
  
  public LocationInfoHolder(String code, LocalizedValue label, ServerGeoObjectType type)
  {
    this.code = code;
    this.label = label;
    this.type = type;
  }
  
  public void setCode(String code)
  {
    this.code = code;
  }

  public void setLabel(LocalizedValue label)
  {
    this.label = label;
  }

  @Override
  public String getCode()
  {
    return this.code;
  }

  @Override
  public String getLabel()
  {
    return this.label.getValue();
  }

  @Override
  public String getLabel(Locale locale)
  {
    return this.label.getValue(locale);
  }

  public ServerGeoObjectType getType()
  {
    return type;
  }

  public void setType(ServerGeoObjectType type)
  {
    this.type = type;
  }
  
}
