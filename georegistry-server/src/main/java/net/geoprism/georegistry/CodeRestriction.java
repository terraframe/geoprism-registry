package net.geoprism.georegistry;

import org.commongeoregistry.adapter.constants.DefaultAttribute;

import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.system.gis.geo.GeoEntityQuery;

public class CodeRestriction implements GeoObjectRestriction
{
  private String code;

  public CodeRestriction(String code)
  {
    this.code = code;
  }

  @Override
  public void restrict(ValueQuery vQuery, GeoEntityQuery geQuery, BusinessQuery bQuery)
  {
    vQuery.WHERE(geQuery.getGeoId().EQ(this.code));
  }

  @Override
  public void restrict(ValueQuery vQuery, BusinessQuery bQuery)
  {
    vQuery.WHERE(bQuery.get(DefaultAttribute.CODE.getName()).EQ(this.code));
  }

}
