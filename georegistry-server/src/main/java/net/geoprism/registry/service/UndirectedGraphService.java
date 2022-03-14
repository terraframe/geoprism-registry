package net.geoprism.registry.service;

import net.geoprism.registry.UndirectedGraphType;
import net.geoprism.registry.model.GraphType;

public class UndirectedGraphService extends GraphService
{

  @Override
  public GraphType getGraphType(String code)
  {
    return UndirectedGraphType.getByCode(code);
  }
}
