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
package net.geoprism.registry.conversion;

public class GeometryTypeFactory
{
  /**
   * Convert Geometry types between GeoPrism and the CGR standard.
   * 
   * @param geoPrismgeometryType
   * @return CGR GeometryType
   */
  public static org.commongeoregistry.adapter.constants.GeometryType get(com.runwaysdk.system.gis.geo.GeometryType geoPrismGeometryType)
  {
    if (geoPrismGeometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.POINT))
    {
      return org.commongeoregistry.adapter.constants.GeometryType.POINT;
    }
    else if (geoPrismGeometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.LINE))
    {
      return org.commongeoregistry.adapter.constants.GeometryType.LINE;
    }
    else if (geoPrismGeometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.POLYGON))
    {
      return org.commongeoregistry.adapter.constants.GeometryType.POLYGON;
    }
    else if (geoPrismGeometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.MULTIPOINT))
    {
      return org.commongeoregistry.adapter.constants.GeometryType.MULTIPOINT;
    }
    else if (geoPrismGeometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.MULTILINE))
    {
      return org.commongeoregistry.adapter.constants.GeometryType.MULTILINE;
    }
    else if (geoPrismGeometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.MULTIPOLYGON))
    {
      return org.commongeoregistry.adapter.constants.GeometryType.MULTIPOLYGON;
    }
    else
    {
      return null;
    }
  }

  /**
   * Convert Geometry types between the CGR standard by GeoPrism.
   * 
   * @param geoPrismgeometryType
   * @return CGR GeometryType
   */
  public static com.runwaysdk.system.gis.geo.GeometryType get(org.commongeoregistry.adapter.constants.GeometryType adapterGeometryType)
  {
    if (adapterGeometryType.equals(org.commongeoregistry.adapter.constants.GeometryType.POINT))
    {
      return com.runwaysdk.system.gis.geo.GeometryType.POINT;
    }
    else if (adapterGeometryType.equals(org.commongeoregistry.adapter.constants.GeometryType.LINE))
    {
      return com.runwaysdk.system.gis.geo.GeometryType.LINE;
    }
    else if (adapterGeometryType.equals(org.commongeoregistry.adapter.constants.GeometryType.POLYGON))
    {
      return com.runwaysdk.system.gis.geo.GeometryType.POLYGON;
    }
    else if (adapterGeometryType.equals(org.commongeoregistry.adapter.constants.GeometryType.MULTIPOINT))
    {
      return com.runwaysdk.system.gis.geo.GeometryType.MULTIPOINT;
    }
    else if (adapterGeometryType.equals(org.commongeoregistry.adapter.constants.GeometryType.MULTILINE))
    {
      return com.runwaysdk.system.gis.geo.GeometryType.MULTILINE;
    }
    else if (adapterGeometryType.equals(org.commongeoregistry.adapter.constants.GeometryType.MULTIPOLYGON))
    {
      return com.runwaysdk.system.gis.geo.GeometryType.MULTIPOLYGON;
    }
    else
    {
      return null;
    }
  }

}
