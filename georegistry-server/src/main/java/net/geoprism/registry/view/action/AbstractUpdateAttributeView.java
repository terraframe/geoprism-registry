package net.geoprism.registry.view.action;

import net.geoprism.registry.model.graph.VertexServerGeoObject;

abstract public class AbstractUpdateAttributeView
{
  private String attributeName;
  
  abstract public void execute(VertexServerGeoObject go);

  public String getAttributeName()
  {
    return attributeName;
  }

  public void setAttributeName(String attributeName)
  {
    this.attributeName = attributeName;
  }
}
