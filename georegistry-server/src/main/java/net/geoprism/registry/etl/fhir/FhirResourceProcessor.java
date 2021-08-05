package net.geoprism.registry.etl.fhir;

import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;

import net.geoprism.registry.etl.FhirSyncImportConfig;
import net.geoprism.registry.graph.FhirExternalSystem;

public interface FhirResourceProcessor
{
  public void configure(FhirExternalSystem system);

  public void process(Location location);

  public void process(Organization organization);

  public boolean supports(FhirSyncImportConfig config);

}
