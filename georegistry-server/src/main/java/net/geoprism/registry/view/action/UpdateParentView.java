package net.geoprism.registry.view.action;

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
    for (UpdateValueOverTimeView vot : this.valuesOverTime)
    {
      vot.execute(this, go);
    }
  }
}
