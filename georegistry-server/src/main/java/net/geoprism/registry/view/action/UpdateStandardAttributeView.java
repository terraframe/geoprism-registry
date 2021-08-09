package net.geoprism.registry.view.action;

import net.geoprism.registry.model.graph.VertexServerGeoObject;

public class UpdateStandardAttributeView extends AbstractUpdateAttributeView
{

  protected Object oldValue;
  
  protected Object newValue;
  
  @Override
  public void execute(VertexServerGeoObject go)
  {
    if (newValue != null)
    {
      go.setValue(this.getAttributeName(), newValue);
    }
  }

}
