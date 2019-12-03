package net.geoprism.registry.query.postgres;

import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.system.gis.geo.GeoEntityQuery;

public interface TreeGeoObjectRestriction
{
  public void restrict(ValueQuery vQuery, GeoEntityQuery geQuery, BusinessQuery bQuery);
}
