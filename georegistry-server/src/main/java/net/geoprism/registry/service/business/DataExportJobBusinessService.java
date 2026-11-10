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
package net.geoprism.registry.service.business;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.system.scheduler.ExecutionContext;

import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.etl.ExternalSystemSyncConfig;
import net.geoprism.registry.etl.FhirExportConfig;
import net.geoprism.registry.etl.FhirImportConfig;
import net.geoprism.registry.etl.JenaExportConfig;
import net.geoprism.registry.etl.export.DataExportJob;
import net.geoprism.registry.etl.export.ExportHistory;
import net.geoprism.registry.etl.export.ExportStage;
import net.geoprism.registry.ws.GlobalNotificationMessage;
import net.geoprism.registry.ws.MessageType;
import net.geoprism.registry.ws.NotificationFacade;

@Service
public class DataExportJobBusinessService implements DataExportJobBusinessServiceIF
{
  @Autowired
  private JenaSynchronizationService       jenaService;

  @Autowired
  private FhirImportSynchronizationService fImportService;

  @Autowired
  private FhirExportSynchronizationService fExportService;

  @Override
  public void execute(DataExportJob job, ExecutionContext executionContext) throws Throwable
  {
    ExportHistory history = (ExportHistory) executionContext.getJobHistoryRecord().getChild();

    this.setStage(history, ExportStage.EXPORT);

    SynchronizationConfig synchronization = job.getConfig();
    ExternalSystemSyncConfig config = synchronization.toConfiguration();

    if (config instanceof FhirExportConfig)
    {
      this.fExportService.execute((FhirExportConfig) config, history);
    }
    else if (config instanceof FhirImportConfig)
    {
      this.fImportService.execute(synchronization, (FhirImportConfig) config, history);
    }
    else if (config instanceof JenaExportConfig)
    {
      this.jenaService.execute(synchronization, (JenaExportConfig) config, history);
    }
  }

  private void setStage(ExportHistory history, ExportStage stage)
  {
    history.appLock();
    history.clearStage();
    history.addStage(stage);
    history.apply();

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.DATA_EXPORT_JOB_CHANGE));
  }
}
