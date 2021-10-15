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
package net.geoprism.registry.etl.export;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutionContext;
import com.runwaysdk.system.scheduler.JobHistory;
import com.runwaysdk.system.scheduler.JobHistoryRecord;
import com.runwaysdk.system.scheduler.QuartzRunwayJob;
import com.runwaysdk.system.scheduler.QueueingQuartzJob;

import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.dhis2.DHIS2FeatureService;
import net.geoprism.registry.dhis2.DHIS2ServiceFactory;
import net.geoprism.registry.dhis2.DHIS2SynchronizationManager;
import net.geoprism.registry.etl.DHIS2SyncConfig;
import net.geoprism.registry.etl.ExternalSystemSyncConfig;
import net.geoprism.registry.etl.FhirSyncExportConfig;
import net.geoprism.registry.etl.FhirSyncImportConfig;
import net.geoprism.registry.etl.export.dhis2.DHIS2TransportServiceIF;
import net.geoprism.registry.etl.fhir.FhirExportSynchronizationManager;
import net.geoprism.registry.etl.fhir.FhirImportSynchronizationManager;
import net.geoprism.registry.ws.GlobalNotificationMessage;
import net.geoprism.registry.ws.MessageType;
import net.geoprism.registry.ws.NotificationFacade;

/**
 * This class is currently hardcoded to DHIS2 export, however the metadata is
 * attempting to be generic enough to scale to more generic usecases.
 * 
 * @author rrowlands
 * @author jsmethie
 *
 */
public class DataExportJob extends DataExportJobBase
{
  private static final long   serialVersionUID = -1821569567;

  private static final Logger logger           = LoggerFactory.getLogger(DataExportJob.class);

  public DataExportJob()
  {
    super();
  }

  @Override
  public synchronized JobHistory start()
  {
    throw new UnsupportedOperationException();
  }

  public synchronized ExportHistory start(SynchronizationConfig configuration)
  {
    return executableJobStart(configuration);
  }

  private ExportHistory executableJobStart(SynchronizationConfig configuration)
  {
    JobHistoryRecord record = startInTrans(configuration);

    this.getQuartzJob().start(record);

    return (ExportHistory) record.getChild();
  }

  @Transaction
  private JobHistoryRecord startInTrans(SynchronizationConfig configuration)
  {
    ExportHistory history = (ExportHistory) this.createNewHistory();

    JobHistoryRecord record = new JobHistoryRecord(this, history);
    record.apply();

    return record;
  }

  @Override
  public void execute(ExecutionContext executionContext) throws Throwable
  {
    ExportHistory history = (ExportHistory) executionContext.getJobHistoryRecord().getChild();

    this.setStage(history, ExportStage.EXPORT);

    SynchronizationConfig c = this.getConfig();
    ExternalSystemSyncConfig config = c.buildConfiguration();

    if (config instanceof DHIS2SyncConfig)
    {
      DHIS2SyncConfig dhis2Config = (DHIS2SyncConfig) config;

      DHIS2TransportServiceIF dhis2 = DHIS2ServiceFactory.buildDhis2TransportService(dhis2Config.getSystem());

      DHIS2FeatureService dhis2FeatureService = new DHIS2FeatureService();
      dhis2FeatureService.setExternalSystemDhis2Version(dhis2, dhis2Config.getSystem());

      new DHIS2SynchronizationManager(dhis2, dhis2Config, history).synchronize();
    }
    else if (config instanceof FhirSyncExportConfig)
    {
      FhirExportSynchronizationManager manager = new FhirExportSynchronizationManager((FhirSyncExportConfig) config, history);
      manager.synchronize();
    }
    else if (config instanceof FhirSyncImportConfig)
    {
      FhirImportSynchronizationManager manager = new FhirImportSynchronizationManager(c, (FhirSyncImportConfig) config, history);
      manager.synchronize();
    }
  }

  @Override
  public void afterJobExecute(JobHistory history)
  {
    super.afterJobExecute(history);

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.DATA_EXPORT_JOB_CHANGE, null));
  }

  private void setStage(ExportHistory history, ExportStage stage)
  {
    history.appLock();
    history.clearStage();
    history.addStage(stage);
    history.apply();

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.DATA_EXPORT_JOB_CHANGE, null));
  }

  @Override
  protected JobHistory createNewHistory()
  {
    ExportHistory history = new ExportHistory();
    history.setStartTime(new Date());
    history.addStatus(AllJobStatus.NEW);
    history.addStage(ExportStage.CONNECTING);
    history.apply();

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.DATA_EXPORT_JOB_CHANGE, null));

    return history;
  }

  @Override
  public boolean canResume(JobHistoryRecord jhr)
  {
    return false;
  }

  @Override
  protected QuartzRunwayJob createQuartzRunwayJob()
  {
    return new QueueingQuartzJob(this);
  }

  public static List<? extends DataExportJob> getAll(SynchronizationConfig config)
  {
    DataExportJobQuery query = new DataExportJobQuery(new QueryFactory());
    query.WHERE(query.getConfig().EQ(config));

    try (OIterator<? extends DataExportJob> it = query.getIterator())
    {
      return it.getAll();
    }
  }
}
