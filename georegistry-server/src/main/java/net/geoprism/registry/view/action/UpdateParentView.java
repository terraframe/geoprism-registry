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
