/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.etl.fhir;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.hapi.fluentpath.FhirPathR4;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleLinkComponent;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.system.scheduler.JobHistory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.param.DateRangeParam;
import net.geoprism.registry.etl.export.ExportError;
import net.geoprism.registry.etl.export.ExportHistory;

public class FhirResourceImporter
{
  private static Logger         logger      = LoggerFactory.getLogger(FhirResourceImporter.class);

  private FhirConnection        connection;

  private FhirResourceProcessor processor;

  private ExportHistory         history;

  private Date                  since;

  long                          count       = 0;

  long                          exportCount = 0;

  public FhirResourceImporter(FhirConnection connection, FhirResourceProcessor processor, ExportHistory history, Date since)
  {
    super();

    this.connection = connection;
    this.processor = processor;
    this.history = history;
    // this.since = since;
  }

  public void synchronize()
  {
    this.processor.configure(this.connection.getExternalSystem());

    IGenericClient client = this.connection.getClient();

    Bundle bundle = client.search().forResource(Location.class).count(2000).lastUpdated(new DateRangeParam(this.since, null)).include(new Include("Location:organization")).returnBundle(Bundle.class).execute();

    this.history.appLock();
    this.history.setWorkTotal(Long.valueOf(bundle.getTotal() * 2));
    this.history.apply();

    while (bundle != null)
    {
      this.process(bundle);

      BundleLinkComponent link = bundle.getLink(Bundle.LINK_NEXT);

      if (link != null)
      {
        // The link may come back with the local url instead of the global url
        // As such replace the base local url with the base global url
        // String localUrl = link.getUrl();
        // String[] split = localUrl.split("\\?");
        // String globalUrl = this.connection.getExternalSystem().getUrl() + "?"
        // + split[1];
        //
        // link.setUrl(globalUrl);
        //
        bundle = client.loadPage().next(bundle).execute();
      }
      else
      {
        bundle = null;
      }
    }
  }

  public void synchronize(Bundle bundle)
  {
    this.processor.configure(this.connection.getExternalSystem());

    process(bundle);
  }

  private void process(Bundle bundle)
  {
//    FhirPathR4 path = new FhirPathR4(FhirContext.forR4());
//    List<Location> locations = path.evaluate(bundle, "Bundle.entry.resource.ofType(Location)", Location.class);
    
    List<Location> locations = bundle.getEntry().stream().map(e -> e.getResource()).filter(r -> (r instanceof Location)).map(r -> (Location) r).collect(Collectors.toList());

    for (Location location : locations)
    {
      try
      {
        handleLocation(location);

        if (this.history != null)
        {
          this.history.appLock();
          this.history.setWorkProgress(count++);
          this.history.setExportedRecords(exportCount++);
          this.history.apply();
        }
      }
      catch (Exception e)
      {
        if (this.history != null)
        {
          this.recordExportError(e, this.history, location);

          this.history.appLock();
          this.history.setWorkProgress(count++);
          this.history.apply();
        }
        else
        {
          throw new ProgrammingErrorException(e);
        }
      }
    }

//    List<Organization> organizations = path.evaluate(bundle, "Bundle.entry.resource.ofType(Organization)", Organization.class);
    List<Organization> organizations = bundle.getEntry().stream().map(e -> e.getResource()).filter(r -> (r instanceof Organization)).map(r -> (Organization) r).collect(Collectors.toList());

    for (Organization organization : organizations)
    {
      try
      {
        handleOrganization(organization);

        if (this.history != null)
        {
          this.history.appLock();
          this.history.setWorkProgress(count++);
          this.history.setExportedRecords(exportCount++);
          this.history.apply();
        }
      }
      catch (Exception e)
      {
        if (this.history != null)
        {
          this.recordExportError(e, this.history, organization);

          this.history.appLock();
          this.history.setWorkProgress(count++);
          this.history.apply();
        }
        else
        {
          throw new ProgrammingErrorException(e);
        }
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
    if (ex instanceof ProgrammingErrorException)
    {
      logger.error("Unknown error while processing the FHIR resource [" + resource.getId() + "]", ex);
    }

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
