package net.geoprism.registry.query.graph;

import java.util.Map;

public class VertexCodeRestriction implements VertexGeoObjectRestriction
{
  private String code;

  public VertexCodeRestriction(String code)
  {
    this.code = code;
  }

  @Override
  public void restrict(StringBuilder statement, Map<String, Object> parameters)
  {
    statement.append(",where: (code = :code)");

    parameters.put("code", this.code);
  }
}
