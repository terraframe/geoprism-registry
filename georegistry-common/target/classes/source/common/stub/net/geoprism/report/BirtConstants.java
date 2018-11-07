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
package net.geoprism.report;

import java.io.File;

import com.runwaysdk.constants.DeployProperties;

public interface BirtConstants
{
  public static final String CATEGORY       = "category";

  public static final String CRITERIA       = "criteria";

  public static final String AGGREGATION    = "aggregation";

  public static final String LAYER_ID       = "layerId";

  public static final String DEFAULT_GEO_ID = "defaultGeoId";

  public static final String GEO_NODE_ID    = "geoNodeId";

  public static final String BIRT_SUFFIX    = "imgs/birt";

  /**
   * rptdocument cache directory
   */
  public static final String CACHE_DIR      = DeployProperties.getJspDir() + File.separator + "reportcache";

  /**
   * birt temp imgs directory
   */
  public static final String IMGS_DIR       = DeployProperties.getDeployPath() + File.separator + "imgs" + File.separator + "birt";

}
