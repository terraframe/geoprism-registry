/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.view.action;

import java.util.Date;
import java.util.SortedSet;

import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;

import net.geoprism.registry.action.InvalidChangeRequestException;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;

public class UpdateParentView extends UpdateChangeOverTimeAttributeView
{
  protected String hierarchyCode;

  public String getHierarchyCode()
  {
    return hierarchyCode;
  }

  public void setHierarchyCode(String hierarchyCode)
  {
    this.hierarchyCode = hierarchyCode;
  }
  
  @Override
  public void execute(VertexServerGeoObject go)
  {
    final ServerHierarchyType hierarchyType = ServerHierarchyType.get(this.getHierarchyCode());
    SortedSet<EdgeObject> looseVotc = go.getEdges(hierarchyType);
    
    for (UpdateValueOverTimeView vot : this.valuesOverTime)
    {
      ((UpdateParentValueOverTimeView) vot).executeParent(this, go, looseVotc);
    }
    
    // The edge work has already been applied at this point. We just need to validate what's in the DB
    this.validateValuesOverTime(looseVotc);
  }
  
  public void validateValuesOverTime(SortedSet<EdgeObject> votc)
  {
    for (EdgeObject edge: votc)
    {
      Date startDate = edge.getObjectValue(GeoVertex.START_DATE);
      Date endDate = edge.getObjectValue(GeoVertex.END_DATE);
      
      if (startDate == null)
      {
        throw new InvalidChangeRequestException();
      }
      if (endDate == null)
      {
        edge.setValue(GeoVertex.END_DATE, ValueOverTime.INFINITY_END_DATE);
      }
      
      if (startDate.after(endDate))
      {
        throw new InvalidChangeRequestException();
      }
    }
  
    for (EdgeObject edge: votc)
    {
      Date s1 = edge.getObjectValue(GeoVertex.START_DATE);
      Date e1 = edge.getObjectValue(GeoVertex.END_DATE);
      
      for (EdgeObject edge2 : votc) {
        if (edge != edge2) {
          Date s2 = edge2.getObjectValue(GeoVertex.START_DATE);
          Date e2 = edge2.getObjectValue(GeoVertex.END_DATE);
  
          if (this.dateRangeOverlaps(s1.getTime(), e1.getTime(), s2.getTime(), e2.getTime())) {
            throw new InvalidChangeRequestException();
          }
        }
      }
    }
  }
  
  public boolean dateRangeOverlaps(long a_start, long a_end, long b_start, long b_end) {
    if (a_start <= b_start && b_start <= a_end) return true; // b starts in a
    if (a_start <= b_end && b_end <= a_end) return true; // b ends in a
    if (b_start < a_start && a_end < b_end) return true; // a in b
    return false;
  }
}
