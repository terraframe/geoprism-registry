package net.geoprism.registry.service;

import net.geoprism.registry.DirectedAcyclicGraphType;
import net.geoprism.registry.model.GraphType;

public class DirectedAcyclicGraphService extends GraphService
{

  @Override
  public GraphType getGraphType(String code)
  {
    return DirectedAcyclicGraphType.getByCode(code);
  }
}
