package net.geoprism.registry.model;

import net.geoprism.registry.model.graph.GraphStrategy;

public interface GraphType
{
  public GraphStrategy getStrategy();

  public String getCode();
}
