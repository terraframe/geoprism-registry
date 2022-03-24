package net.geoprism.registry.model.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import com.runwaysdk.dataaccess.ProgrammingErrorException;

import net.geoprism.registry.model.GraphType;

public class GraphValidationService
{
  /**
   * Retrieve all implementations of GraphValidator.
   */
  public static List<GraphValidator> getValidators()
  {
    List<GraphValidator> configurations = new ArrayList<GraphValidator>();

    ServiceLoader<GraphValidator> loader = ServiceLoader.load(GraphValidator.class, Thread.currentThread().getContextClassLoader());

    try
    {
      Iterator<GraphValidator> it = loader.iterator();

      while (it.hasNext())
      {
        configurations.add(it.next());
      }
    }
    catch (ServiceConfigurationError serviceError)
    {
      throw new ProgrammingErrorException(serviceError);
    }

    return configurations;
  }
  
  public static void validate(GraphType graphType, VertexServerGeoObject parent, VertexServerGeoObject child)
  {
    getValidators().forEach(validator -> {
      validator.validate(graphType, parent, child);
    });
  }
}
