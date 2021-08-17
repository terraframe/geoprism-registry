package net.geoprism.registry.etl.fhir;

import java.util.Date;
import java.util.List;

import org.hl7.fhir.r4.hapi.fluentpath.FhirPathR4;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Resource;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.system.scheduler.JobHistory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IRestfulClientFactory;
import ca.uhn.fhir.rest.param.DateRangeParam;
import net.geoprism.registry.etl.export.ExportError;
import net.geoprism.registry.etl.export.ExportHistory;
import net.geoprism.registry.graph.FhirExternalSystem;

public class FhirResourceImporter
{
  private FhirExternalSystem    system;

  private FhirResourceProcessor processor;

  private ExportHistory         history;

  private Date                  since;

  public FhirResourceImporter(FhirExternalSystem system, FhirResourceProcessor processor, ExportHistory history, Date since)
  {
    super();

    this.system = system;
    this.processor = processor;
    this.history = history;
    // this.since = since;
  }

  public void synchronize()
  {
    this.processor.configure(this.system);

    FhirContext ctx = FhirContext.forR4();

    IRestfulClientFactory factory = ctx.getRestfulClientFactory();
    factory.setSocketTimeout(-1);

    IGenericClient client = factory.newGenericClient(system.getUrl());

    Bundle bundle = client.search().forResource(Location.class).lastUpdated(new DateRangeParam(this.since, null)).include(new Include("Location:organization")).returnBundle(Bundle.class).execute();

    this.history.appLock();
    this.history.setWorkTotal(Long.valueOf(bundle.getTotal() * 2));
    this.history.apply();

    long count = 0;
    long exportCount = 0;

    while (bundle != null)
    {
      FhirPathR4 path = new FhirPathR4(ctx);
      List<Location> locations = path.evaluate(bundle, "Bundle.entry.resource.ofType(Location)", Location.class);

      for (Location location : locations)
      {
        try
        {
          handleLocation(location);

          this.history.appLock();
          this.history.setWorkProgress(count++);
          this.history.setExportedRecords(exportCount++);
          this.history.apply();
        }
        catch (Exception e)
        {
          this.recordExportError(e, this.history, location);

          this.history.appLock();
          this.history.setWorkProgress(count++);
          this.history.apply();
        }
      }

      List<Organization> organizations = path.evaluate(bundle, "Bundle.entry.resource.ofType(Organization)", Organization.class);

      for (Organization organization : organizations)
      {
        try
        {
          handleOrganization(organization);

          this.history.appLock();
          this.history.setWorkProgress(count++);
          this.history.setExportedRecords(exportCount++);
          this.history.apply();
        }
        catch (Exception e)
        {
          this.recordExportError(e, this.history, organization);

          this.history.appLock();
          this.history.setWorkProgress(count++);
          this.history.apply();
        }
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

  @Transaction
  public void handleOrganization(Organization organization)
  {
    this.processor.process(organization);
  }

  @Transaction
  public void handleLocation(Location location)
  {
    this.processor.process(location);
  }

  @Transaction
  private void recordExportError(Exception ex, ExportHistory history, Resource resource)
  {
    if (this.history != null)
    {
      ExportError exportError = new ExportError();
      exportError.setCode(resource.getId());

      if (ex != null)
      {
        exportError.setErrorJson(JobHistory.exceptionToJson(ex).toString());
      }

      // exportError.setRowIndex(ee.rowIndex);

      exportError.setHistory(history);

      exportError.apply();
    }
  }

}
