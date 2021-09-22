package net.geoprism.registry.etl.fhir;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.dataaccess.ProgrammingErrorException;

import net.geoprism.registry.etl.FhirSyncImportConfig;
import net.geoprism.registry.graph.FhirExternalSystem;

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

  public static FhirResourceProcessor getProcessor(final FhirSyncImportConfig config)
  {
    List<FhirResourceProcessor> populators = FhirFactory.getProcessors();

    for (FhirResourceProcessor populator : populators)
    {
      if (populator.supports(config))
      {
        return populator;
      }
    }

    return new FhirResourceProcessor()
    {
      @Override
      public void configure(FhirExternalSystem system)
      {
      }

      @Override
      public void process(Organization organization)
      {
      }

      @Override
      public void process(Location location)
      {
      }

      @Override
      public boolean supports(FhirSyncImportConfig config)
      {
        return false;
      }
    };
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
}
