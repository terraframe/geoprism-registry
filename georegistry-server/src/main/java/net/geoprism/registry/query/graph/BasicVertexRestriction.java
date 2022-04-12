package net.geoprism.registry.query.graph;

import java.util.Map;

public interface BasicVertexRestriction
{
  public void restrict(StringBuilder statement, Map<String, Object> parameters);
}
