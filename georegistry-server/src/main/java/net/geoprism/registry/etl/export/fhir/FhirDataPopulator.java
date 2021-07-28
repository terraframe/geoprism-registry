package net.geoprism.registry.etl.export.fhir;

import org.hl7.fhir.r4.model.Bundle;

import com.runwaysdk.business.Business;

import net.geoprism.registry.MasterListVersion;

public interface FhirDataPopulator
{
  public boolean supports(MasterListVersion version);

  public void configure(FhirExportContext context, MasterListVersion version, boolean resolveIds);

  public void populate(Business row, Facility facility);

  public void createExtraResources(Business row, Bundle bundle, Facility facility);

  public void finish(Bundle bundle);
}
