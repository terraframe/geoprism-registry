/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism(tm).
 *
 * Geoprism(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry;

import com.runwaysdk.configuration.ConfigurationManager.ConfigGroupIF;

/**
 * Defines the bundle locations for configuration groups. Intended for use with Runway's ConfigurationManager API.
 * 
 * @author Richard Rowlands
 */
public enum GeoregistryConfigGroup implements ConfigGroupIF {
  CLIENT("georegistry/", "client"),
  COMMON("georegistry/", "common"),
  SERVER("georegistry/", "server"),
  ROOT("", "root");

  private String path;

  private String identifier;

  GeoregistryConfigGroup(String path, String identifier) {
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