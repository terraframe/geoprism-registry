package net.geoprism.registry.model.graph;

import net.geoprism.registry.model.GraphType;

public interface GraphValidator
{
  public void validate(GraphType graphType, VertexServerGeoObject parent, VertexServerGeoObject child);
}
