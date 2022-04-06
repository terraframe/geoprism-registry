/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
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
