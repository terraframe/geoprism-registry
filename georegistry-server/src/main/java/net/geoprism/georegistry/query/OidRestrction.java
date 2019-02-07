package net.geoprism.georegistry.query;

import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.constants.ComponentInfo;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.system.gis.geo.GeoEntityQuery;

public class OidRestrction implements GeoObjectRestriction
{
  private String runwayId;

  public OidRestrction(String runwayId)
  {
    this.runwayId = runwayId;
  }

  public String getRunwayId()
  {
    return runwayId;
  }

  public void setRunwayId(String runwayId)
  {
    this.runwayId = runwayId;
  }

  @Override
  public void restrict(ValueQuery vQuery, GeoEntityQuery geQuery, BusinessQuery bQuery)
  {
    vQuery.WHERE(geQuery.getOid().EQ(this.runwayId));
  }

  @Override
  public void restrict(ValueQuery vQuery, BusinessQuery bQuery)
  {
    vQuery.WHERE(bQuery.get(ComponentInfo.OID).EQ(this.runwayId));
  }
}
