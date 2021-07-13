package net.geoprism.registry.view.action;

import net.geoprism.registry.model.graph.VertexServerGeoObject;

abstract public class AbstractUpdateAttributeView
{
  protected String attributeName;
  
  protected String governanceStatus;

  public String getAttributeName()
  {
    return attributeName;
  }

  public void setAttributeName(String attributeName)
  {
    this.attributeName = attributeName;
  }

  public String getGovernanceStatus()
  {
    return governanceStatus;
  }

  public void setGovernanceStatus(String governanceStatus)
  {
    this.governanceStatus = governanceStatus;
  }
  
  public void populate()
  {
    
  }
  
  abstract public void execute(VertexServerGeoObject go);
}
