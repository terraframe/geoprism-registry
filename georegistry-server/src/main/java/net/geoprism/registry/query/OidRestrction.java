/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.query;

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
