package net.geoprism.registry.query.graph;

import java.util.Map;

public interface VertexGeoObjectRestriction
{
  public void restrict(StringBuilder statement, Map<String, Object> parameters);
}
