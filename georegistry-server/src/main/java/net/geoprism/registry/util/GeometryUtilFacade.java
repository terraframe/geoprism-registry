/**
 * Copyright (c) 2023 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism(tm).
 *
 * Geoprism(tm) is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Geoprism(tm) is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.util;

import java.util.List;

public class GeometryUtilFacade
{
  public static final String         GEOM_COLUMN = "geom";

  public static int                  MINX_INDEX  = 0;

  public static int                  MINY_INDEX  = 1;

  public static int                  MAXX_INDEX  = 2;

  public static int                  MAXY_INDEX  = 3;

  private static GeometryUtilService service     = new GeometryUtilService();

  public synchronized static GeometryUtilService getService()
  {
    return service;
  }

  public synchronized static void setService(GeometryUtilService _service)
  {
    service = _service;
  }

  /**
   * Calculates the bounding box of all the layers.
   * 
   * @param views
   * @return double[] {minx, miny, maxx, maxy}
   */
  public static double[] getBBOX(String... views)
  {
    return getService().getBBOX(views);
  }

  /**
   * Calculates the bounding box of all the layers.
   * 
   * @param views
   * @return double[] {minx, miny, maxx, maxy}
   */
  public static double[] getExpandedBBOX(List<String> views, double expandVal)
  {
    return getService().getExpandedBBOX(views, expandVal);
  }
}
