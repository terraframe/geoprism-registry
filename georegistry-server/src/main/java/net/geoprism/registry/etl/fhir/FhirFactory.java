/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.etl.fhir;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.stream.Collector;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.dataaccess.ProgrammingErrorException;

public class FhirFactory
{
  public static List<FhirDataPopulator> getPopulators()
  {
    List<FhirDataPopulator> configurations = new ArrayList<FhirDataPopulator>();

    ServiceLoader<FhirDataPopulator> loader = ServiceLoader.load(FhirDataPopulator.class, Thread.currentThread().getContextClassLoader());

    try
    {
      Iterator<FhirDataPopulator> it = loader.iterator();

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

  public static FhirDataPopulator getPopulator(String className)
  {
    List<FhirDataPopulator> populators = FhirFactory.getPopulators();

    for (FhirDataPopulator populator : populators)
    {
      if (populator.getClass().getName().equals(className))
      {
        return populator;
      }
    }

    throw new ProgrammingErrorException("Unable to find Fhir data populator with the class name of [" + className + "]");
  }

  public static List<FhirResourceProcessor> getProcessors()
  {
    List<FhirResourceProcessor> configurations = new ArrayList<FhirResourceProcessor>();

    ServiceLoader<FhirResourceProcessor> loader = ServiceLoader.load(FhirResourceProcessor.class, Thread.currentThread().getContextClassLoader());

    try
    {
      Iterator<FhirResourceProcessor> it = loader.iterator();

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

  public static FhirResourceProcessor getProcessor(String className)
  {
    List<FhirResourceProcessor> processors = FhirFactory.getProcessors();

    for (FhirResourceProcessor processor : processors)
    {
      if (processor.getClass().getName().equals(className))
      {
        return processor;
      }
    }

    throw new ProgrammingErrorException("Unable to find Fhir data processor with the class name of [" + className + "]");
  }

  public static JsonArray getExportImplementations()
  {
    List<FhirDataPopulator> populators = getPopulators();

    return populators.stream().map(p -> {
      JsonObject object = new JsonObject();
      object.addProperty("className", p.getClass().getName());
      object.addProperty("label", p.getLabel());

      return object;
    }).collect(Collector.of(() -> new JsonArray(), (r, t) -> r.add((JsonObject) t), (x1, x2) -> {
      x1.addAll(x2);
      return x1;
    }));
  }

  public static JsonArray getImportImplementations()
  {
    List<FhirResourceProcessor> populators = getProcessors();

    return populators.stream().map(p -> {
      JsonObject object = new JsonObject();
      object.addProperty("className", p.getClass().getName());
      object.addProperty("label", p.getLabel());

      return object;
    }).collect(Collector.of(() -> new JsonArray(), (r, t) -> r.add((JsonObject) t), (x1, x2) -> {
      x1.addAll(x2);
      return x1;
    }));
  }
}
