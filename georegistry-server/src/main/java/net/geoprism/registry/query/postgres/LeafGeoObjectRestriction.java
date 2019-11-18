package net.geoprism.registry.query.postgres;

import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.query.ValueQuery;

public interface LeafGeoObjectRestriction
{
  public void restrict(ValueQuery vQuery, BusinessQuery bQuery);
}
