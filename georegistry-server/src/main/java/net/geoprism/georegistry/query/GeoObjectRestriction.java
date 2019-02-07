package net.geoprism.georegistry.query;

import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.system.gis.geo.GeoEntityQuery;

public interface GeoObjectRestriction
{
  public void restrict(ValueQuery vQuery, GeoEntityQuery geQuery, BusinessQuery bQuery);

  public void restrict(ValueQuery vQuery, BusinessQuery bQuery);
}
