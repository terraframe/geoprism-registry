package net.geoprism.registry.view.action;

import net.geoprism.registry.model.graph.VertexServerGeoObject;

public class UpdateChangeOverTimeAttributeView extends AbstractUpdateAttributeView
{

  protected UpdateValueOverTimeView[] valuesOverTime;
  
  @Override
  public void execute(VertexServerGeoObject go)
  {
    for (UpdateValueOverTimeView vot : this.valuesOverTime)
    {
      vot.execute(this, go);
    }
  }
  
}
