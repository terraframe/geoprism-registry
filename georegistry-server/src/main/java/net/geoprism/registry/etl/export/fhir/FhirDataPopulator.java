package net.geoprism.registry.etl.export.fhir;

import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;

import com.runwaysdk.business.Business;

public interface FhirDataPopulator
{

  public void populateOrganization(FhirExportContext context, Business row, Organization org);

  public void populateLocation(FhirExportContext context, Business row, Location location);

}
