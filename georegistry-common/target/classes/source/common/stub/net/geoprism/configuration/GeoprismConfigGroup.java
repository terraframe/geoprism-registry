/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.configuration;

import com.runwaysdk.configuration.ConfigurationManager.ConfigGroupIF;

/**
 * Defines the bundle locations for configuration groups. Intended for use with Runway's ConfigurationManager API.
 * 
 * @author Richard Rowlands
 */
public enum GeoprismConfigGroup implements ConfigGroupIF {
  CLIENT("geoprism/", "client"),
  COMMON("geoprism/", "common"),
  SERVER("geoprism/", "server"),
  ROOT("", "root");

  private String path;

  private String identifier;

  GeoprismConfigGroup(String path, String identifier) {
    this.path = path;
    this.identifier = identifier;
  }

  public String getPath() {
    return this.path;
  }

  public String getOidentifier() {
    return identifier;
  }
}