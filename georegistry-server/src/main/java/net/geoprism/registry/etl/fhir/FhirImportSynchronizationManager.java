/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.etl.ExportJobHasErrors;
import net.geoprism.registry.etl.FhirSyncImportConfig;
import net.geoprism.registry.etl.export.ExportErrorQuery;
import net.geoprism.registry.etl.export.ExportHistory;
import net.geoprism.registry.etl.export.ExportStage;
import net.geoprism.registry.etl.export.HttpError;
import net.geoprism.registry.graph.FhirExternalSystem;
import net.geoprism.registry.ws.GlobalNotificationMessage;
import net.geoprism.registry.ws.MessageType;
import net.geoprism.registry.ws.NotificationFacade;

public class FhirImportSynchronizationManager
{
  private SynchronizationConfig config;

  private FhirSyncImportConfig  details;

  private ExportHistory         history;

  public FhirImportSynchronizationManager(SynchronizationConfig config, FhirSyncImportConfig details, ExportHistory history)
  {
    this.config = config;
    this.details = details;
    this.history = history;
  }

  public void synchronize()
  {
    final FhirExternalSystem system = (FhirExternalSystem) this.details.getSystem();

    try (FhirConnection connection = FhirConnectionFactory.get(system))
    {

      FhirResourceProcessor processor = FhirFactory.getProcessor(this.details.getImplementation());

      FhirResourceImporter importer = new FhirResourceImporter(connection, processor, this.history, this.config.getLastSynchDate());
      importer.synchronize();

      history.appLock();
      history.clearStage();
      history.addStage(ExportStage.COMPLETE);
      history.apply();

      config.appLock();
      config.setLastSynchDate(new Date());
      config.apply();

      NotificationFacade.queue(new GlobalNotificationMessage(MessageType.DATA_EXPORT_JOB_CHANGE, null));

      handleExportErrors();
    }
    catch (ExportJobHasErrors e)
    {
      throw e;
    }
    catch (Exception e)
    {
      throw new HttpError(e);
    }
  }

  private void handleExportErrors()
  {
    ExportErrorQuery query = new ExportErrorQuery(new QueryFactory());
    query.WHERE(query.getHistory().EQ(history));
    Boolean hasErrors = query.getCount() > 0;

    if (hasErrors)
    {
      ExportJobHasErrors ex = new ExportJobHasErrors();

      throw ex;
    }
  }
}
