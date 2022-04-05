package net.geoprism.registry.model;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import net.geoprism.registry.DirectedAcyclicGraphType;
import net.geoprism.registry.UndirectedGraphType;
import net.geoprism.registry.model.graph.GraphStrategy;

public interface GraphType
{
  public GraphStrategy getStrategy();

  public String getCode();
  
  public LocalizedValue getLabel();

  public static GraphType getByCode(String relationshipType, String code)
  {
    if (relationshipType != null)
    {
      if (relationshipType.equals("UndirectedGraphType") || relationshipType.equals(UndirectedGraphType.CLASS))
      {
        return UndirectedGraphType.getByCode(code);
      }
      else if (relationshipType.equals("DirectedAcyclicGraphType") || relationshipType.equals(DirectedAcyclicGraphType.CLASS))
      {
        return DirectedAcyclicGraphType.getByCode(code);
      }
    }

    return ServerHierarchyType.get(code);
  }
}
