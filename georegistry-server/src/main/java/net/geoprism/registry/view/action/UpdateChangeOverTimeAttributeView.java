package net.geoprism.registry.view.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTimeCollection;

import net.geoprism.registry.action.InvalidChangeRequestException;
import net.geoprism.registry.model.graph.VertexServerGeoObject;

public class UpdateChangeOverTimeAttributeView extends AbstractUpdateAttributeView
{

  protected UpdateValueOverTimeView[] valuesOverTime;
  
  @Override
  public void execute(VertexServerGeoObject go)
  {
    String attributeName = this.getAttributeNameGeomAccounting(go);
    
    // This list is intentionally NOT a ValueOverTimeCollection. The reason for this is because we
    // DON'T want any of the reordering or splitting logic to happen until AFTER we have applied
    // all of the actions. The logic we want to avoid is ValueOverTimeCollection.calculateStartDates
    List<ValueOverTime> looseVotc = new ArrayList<ValueOverTime>(go.getValuesOverTime(attributeName));
    
    for (UpdateValueOverTimeView vot : this.valuesOverTime)
    {
      vot.execute(this, go, looseVotc);
    }
    
    ValueOverTimeCollection newVotc = new ValueOverTimeCollection();
    newVotc.addAll(looseVotc);
    
    this.validateValuesOverTime(newVotc);
    
    go.getVertex().getGraphObjectDAO().getAttribute(attributeName).setValuesOverTime(newVotc);
  }
  
  public String getAttributeNameGeomAccounting(VertexServerGeoObject go)
  {
    String attributeName = this.getAttributeName();
    
    if (attributeName.equals("geometry"))
    {
      attributeName = go.getGeometryAttributeName();
    }
    
    return attributeName;
  }
  
  public void validateValuesOverTime(ValueOverTimeCollection votc)
  {
    for (ValueOverTime vot: votc)
    {
      if (vot.getStartDate() == null)
      {
        throw new InvalidChangeRequestException();
      }
      if (vot.getEndDate() == null)
      {
        vot.setEndDate(ValueOverTime.INFINITY_END_DATE);
      }
      
      if (vot.getStartDate().after(vot.getEndDate()))
      {
        throw new InvalidChangeRequestException();
      }
    }
  
    for (ValueOverTime vot: votc)
    {
      Date s1 = vot.getStartDate();
      Date e1 = vot.getEndDate();
      
      for (ValueOverTime vot2 : votc) {
        if (vot != vot2) {
          Date s2 = vot2.getStartDate();
          Date e2 = vot2.getEndDate();
  
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
