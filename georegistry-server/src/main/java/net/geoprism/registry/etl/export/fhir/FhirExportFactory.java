package net.geoprism.registry.etl.export.fhir;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import com.runwaysdk.dataaccess.ProgrammingErrorException;

import net.geoprism.registry.MasterListVersion;

public class FhirExportFactory
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

  public static FhirDataPopulator getPopulator(final MasterListVersion version)
  {
    List<FhirDataPopulator> populators = FhirExportFactory.getPopulators();

    for (FhirDataPopulator populator : populators)
    {
      if (populator.supports(version))
      {
        return populator;
      }
    }
    return new DefaultFhirDataPopulator();
  }
}
