package net.geoprism.registry.etl.export.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import net.geoprism.registry.graph.FhirExternalSystem;

public class FhirExportContext
{
  private FhirExternalSystem externalSystem;

  private IGenericClient     client;

  public FhirExportContext(FhirExternalSystem externalSystem, IGenericClient client)
  {
    super();
    this.externalSystem = externalSystem;
    this.client = client;
  }

  public IGenericClient getClient()
  {
    return client;
  }

  public FhirContext getFhirContext()
  {
    return this.client.getFhirContext();
  }

  public FhirExternalSystem getExternalSystem()
  {
    return externalSystem;
  }

  public String getSystem()
  {
    return "terraframe.com";
  }
}
