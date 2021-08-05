package net.geoprism.registry.etl.fhir;

import java.util.Date;
import java.util.List;

import org.hl7.fhir.r4.hapi.fluentpath.FhirPathR4;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IRestfulClientFactory;
import ca.uhn.fhir.rest.param.DateRangeParam;
import net.geoprism.registry.graph.FhirExternalSystem;

public class FhirResourceImporter
{
  private FhirExternalSystem    system;

  private FhirResourceProcessor processor;

  private Date                  since;

  public FhirResourceImporter(FhirExternalSystem system, FhirResourceProcessor processor, Date since)
  {
    super();

    this.system = system;
    this.processor = processor;
    this.since = since;
  }

  public void synchronize()
  {
    this.processor.configure(this.system);

    FhirContext ctx = FhirContext.forR4();

    IRestfulClientFactory factory = ctx.getRestfulClientFactory();
    factory.setSocketTimeout(-1);

    IGenericClient client = factory.newGenericClient(system.getUrl());

    Bundle bundle = client.search().forResource(Location.class).lastUpdated(new DateRangeParam(this.since, null)).include(new Include("Location:organization")).returnBundle(Bundle.class).execute();

    while (bundle != null)
    {
      FhirPathR4 path = new FhirPathR4(ctx);
      List<Location> locations = path.evaluate(bundle, "Bundle.entry.resource.ofType(Location)", Location.class);

      for (Location location : locations)
      {
        this.processor.process(location);
      }

      List<Organization> organizations = path.evaluate(bundle, "Bundle.entry.resource.ofType(Organization)", Organization.class);

      for (Organization organization : organizations)
      {
        this.processor.process(organization);
      }

      if (bundle.getLink(Bundle.LINK_NEXT) != null)
      {
        bundle = client.loadPage().next(bundle).execute();
      }
      else
      {
        bundle = null;
      }
    }
  }

}
