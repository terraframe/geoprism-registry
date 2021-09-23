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
