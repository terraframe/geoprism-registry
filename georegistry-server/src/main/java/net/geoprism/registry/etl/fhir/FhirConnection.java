package net.geoprism.registry.etl.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import net.geoprism.registry.graph.FhirExternalSystem;

public interface FhirConnection extends AutoCloseable
{

  IGenericClient getClient();

  FhirContext getFhirContext();

  FhirExternalSystem getExternalSystem();

  String getSystem();

  public void open();

}