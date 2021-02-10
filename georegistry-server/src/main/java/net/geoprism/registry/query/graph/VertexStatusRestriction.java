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
package net.geoprism.registry.query.graph;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import net.geoprism.registry.GeoObjectStatus;
import net.geoprism.registry.query.ServerStatusRestriction.JoinOp;

public class VertexStatusRestriction implements VertexGeoObjectRestriction
{
  private List<GeoObjectStatus> statuses;
  
  private Date date;
  
  private JoinOp joinOp;

  public VertexStatusRestriction(List<GeoObjectStatus> statuses, Date date, JoinOp joinOp)
  {
    this.statuses = statuses;
    this.date = date;
    this.joinOp = joinOp;
  }

  @Override
  public void restrict(StringBuilder statement, Map<String, Object> parameters)
  {
    String statusRestriction = getStatusRestriction(parameters);
    
    statement.append(",where: (status_cot CONTAINS ( (:date BETWEEN startDate AND endDate) AND (" + statusRestriction + ")))");

    parameters.put("date", this.date);
  }
  
  public String getStatusRestriction(Map<String, Object> parameters)
  {
    List<String> restrictions = new ArrayList<String>();
    
    for (int i = 0; i < statuses.size(); ++i)
    {
      String status = statuses.get(i).getOid();
      
      restrictions.add("value CONTAINS :status" + i);
      
      parameters.put("status" + i, status);
    }
    
    return StringUtils.join(restrictions, " " + this.joinOp.name() + " ");
  }
}
