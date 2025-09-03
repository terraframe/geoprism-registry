package net.geoprism.registry.service.business;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutionContext;

import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.dhis2.DHIS2FeatureService;
import net.geoprism.registry.dhis2.DHIS2ServiceFactory;
import net.geoprism.registry.dhis2.DHIS2SynchronizationManager;
import net.geoprism.registry.dhis2.SynchronizationHistoryProgressScribe;
import net.geoprism.registry.etl.DHIS2SyncConfig;
import net.geoprism.registry.etl.ExternalSystemSyncConfig;
import net.geoprism.registry.etl.FhirExportConfig;
import net.geoprism.registry.etl.FhirImportConfig;
import net.geoprism.registry.etl.JenaExportConfig;
import net.geoprism.registry.etl.export.DataExportJob;
import net.geoprism.registry.etl.export.ExportHistory;
import net.geoprism.registry.etl.export.ExportStage;
import net.geoprism.registry.etl.export.dhis2.DHIS2TransportServiceIF;
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

    if (config instanceof DHIS2SyncConfig)
    {
      DHIS2SyncConfig dhis2Config = (DHIS2SyncConfig) config;

      DHIS2TransportServiceIF dhis2 = DHIS2ServiceFactory.buildDhis2TransportService(dhis2Config.getSystem());

      DHIS2FeatureService dhis2FeatureService = new DHIS2FeatureService();
      dhis2FeatureService.setExternalSystemDhis2Version(dhis2, dhis2Config.getSystem());

      AllJobStatus status = new DHIS2SynchronizationManager(dhis2, dhis2Config, history, new SynchronizationHistoryProgressScribe(history)).synchronize();
      executionContext.setStatus(status);
    }
    else if (config instanceof FhirExportConfig)
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

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.DATA_EXPORT_JOB_CHANGE, null));
  }
}
