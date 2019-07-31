package net.geoprism.registry.model;

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
