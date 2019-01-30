package net.geoprism.georegistry;

import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.system.gis.geo.GeoEntityQuery;

public class UidRestriction implements GeoObjectRestriction
{
  private String registryId;

  public UidRestriction(String registryId)
  {
    this.registryId = registryId;
  }

  @Override
  public void restrict(ValueQuery vQuery, GeoEntityQuery geQuery, BusinessQuery bQuery)
  {
    vQuery.WHERE(bQuery.get(RegistryConstants.UUID).EQ(this.registryId));
  }

  @Override
  public void restrict(ValueQuery vQuery, BusinessQuery bQuery)
  {
    vQuery.WHERE(bQuery.get(RegistryConstants.UUID).EQ(this.registryId));
  }

}
