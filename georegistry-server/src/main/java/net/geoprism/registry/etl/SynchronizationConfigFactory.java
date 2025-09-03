package net.geoprism.registry.etl;

import net.geoprism.registry.SynchronizationConfig.Type;

public class SynchronizationConfigFactory
{
  public static ExternalSystemSyncConfig get(String type)
  {
    return get(Type.valueOf(type));
  }

  public static ExternalSystemSyncConfig get(Type type)
  {
    if (type.equals(Type.DHIS2))
    {
      return new DHIS2SyncConfig();
    }
    else if (type.equals(Type.FHIR_IMPORT))
    {
      return new FhirImportConfig();
    }
    else if (type.equals(Type.FHIR_EXPORT))
    {
      return new FhirExportConfig();
    }
    else if (type.equals(Type.JENA))
    {
      return new JenaExportConfig();
    }

    throw new UnsupportedOperationException();
  }
}
