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
package net.geoprism.registry.etl;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutionContext;
import com.runwaysdk.system.scheduler.JobHistory;
import com.runwaysdk.system.scheduler.JobHistoryRecord;
import com.runwaysdk.system.scheduler.QuartzRunwayJob;
import com.runwaysdk.system.scheduler.QueueingQuartzJob;

import net.geoprism.registry.Organization;
import net.geoprism.registry.etl.upload.FormatSpecificImporterIF;
import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.etl.upload.ImportHistoryProgressScribe;
import net.geoprism.registry.etl.upload.ImportProgressListenerIF;
import net.geoprism.registry.etl.upload.ObjectImporterIF;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.permission.RolePermissionService;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.ws.GlobalNotificationMessage;
import net.geoprism.registry.ws.MessageType;
import net.geoprism.registry.ws.NotificationFacade;

public class DataImportJob extends DataImportJobBase
{
  private static final long serialVersionUID = 1742592504;

  private static Logger     logger           = LoggerFactory.getLogger(DataImportJob.class);

  public DataImportJob()
  {
    super();
  }

  @Override
  public synchronized JobHistory start()
  {
    throw new UnsupportedOperationException();
  }

  public synchronized ImportHistory start(ImportConfiguration configuration)
  {
    return executableJobStart(configuration);
  }

  private ImportHistory executableJobStart(ImportConfiguration configuration)
  {
    JobHistoryRecord record = startInTrans(configuration);

    this.getQuartzJob().start(record);

    return (ImportHistory) record.getChild();
  }

  @Transaction
  private JobHistoryRecord startInTrans(ImportConfiguration configuration)
  {
    ServerGeoObjectType type = ( (GeoObjectImportConfiguration) configuration ).getType();
    Organization org = type.getOrganization();

    RolePermissionService perms = ServiceFactory.getRolePermissionService();
    if (perms.isRA())
    {
      perms.enforceRA(org.getCode());
    }
    else if (perms.isRM())
    {
      perms.enforceRM(org.getCode(), type);
    }
    else
    {
      perms.enforceRM();
    }

    ImportHistory history = (ImportHistory) this.createNewHistory();

    configuration.setHistoryId(history.getOid());
    configuration.setJobId(this.getOid());

    history.appLock();
    history.setConfigJson(configuration.toJSON().toString());
    history.setImportFileId(configuration.getVaultFileId());

    if (configuration instanceof GeoObjectImportConfiguration)
    {
      history.setOrganization(org);
      history.setGeoObjectTypeCode(type.getCode());
    }

    history.apply();

    JobHistoryRecord record = new JobHistoryRecord(this, history);
    record.apply();

    return record;
  }

  protected void validate(ImportConfiguration config)
  {
    config.validate();
  }

  public static void deleteValidationProblems(ImportHistory history)
  {
    ValidationProblemQuery vpq = new ValidationProblemQuery(new QueryFactory());
    vpq.WHERE(vpq.getHistory().EQ(history));
    OIterator<? extends ValidationProblem> it = vpq.getIterator();
    try
    {
      while (it.hasNext())
      {
        it.next().delete();
      }
    }
    finally
    {
      it.close();
    }
  }

  @Override
  public void execute(ExecutionContext executionContext) throws MalformedURLException, InvocationTargetException
  {
    try
    {
      Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

      ImportHistory history = (ImportHistory) executionContext.getJobHistoryRecord().getChild();
      ImportStage stage = history.getStage().get(0);
      ImportConfiguration config = ImportConfiguration.build(history.getConfigJson());

      process(executionContext, history, stage, config);
    }
    finally
    {
      Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
    }
  }

  // TODO : It might actually be faster to first convert into a shared temp
  // table, assuming you're resolving the parent references into it.
  private void process(ExecutionContext executionContext, ImportHistory history, ImportStage stage, ImportConfiguration config) throws MalformedURLException, InvocationTargetException
  {
    validate(config);

    // TODO : We should have a single transaction where we do all the history
    // configuration upfront, that way the job is either fully configured (and
    // resumable) or it isn't (no in-between)
    config.setHistoryId(history.getOid());
    config.setJobId(this.getOid());

    if (stage.equals(ImportStage.VALIDATE))
    {
      // We can't do this because it prevents people from resuming the job where
      // it left off
      // history.appLock();
      // history.setWorkProgress(0L);
      // history.setImportedRecords(0L);
      // history.apply();

      ImportProgressListenerIF progressListener = runImport(history, stage, config);

      if (progressListener.hasValidationProblems())
      {
        executionContext.setStatus(AllJobStatus.FEEDBACK);

        progressListener.applyValidationProblems();

        history.appLock();
        history.clearStage();
        history.addStage(ImportStage.VALIDATION_RESOLVE);
        history.setConfigJson(config.toJSON().toString());
        history.apply();

        NotificationFacade.queue(new GlobalNotificationMessage(MessageType.IMPORT_JOB_CHANGE, null));
      }
      else
      {
        history.appLock();
        history.clearStage();
        history.addStage(ImportStage.IMPORT);
        history.setConfigJson(config.toJSON().toString());
        history.setWorkProgress(0L);
        history.setImportedRecords(0L);
        history.apply();

        NotificationFacade.queue(new GlobalNotificationMessage(MessageType.IMPORT_JOB_CHANGE, null));

        this.process(executionContext, history, ImportStage.IMPORT, config);
      }
    }
    else if (stage.equals(ImportStage.IMPORT))
    {
      deleteValidationProblems(history);

      // We can't do this because it prevents people from resuming the job where
      // it left off
      // history.appLock();
      // history.setWorkProgress(0L);
      // history.setImportedRecords(0L);
      // history.apply();

      runImport(history, stage, config);

      if (history.hasImportErrors())
      {
        history.appLock();
        history.clearStage();
        history.addStage(ImportStage.IMPORT_RESOLVE);
        history.setConfigJson(config.toJSON().toString());
        history.apply();

        executionContext.setStatus(AllJobStatus.FEEDBACK);
      }
      else
      {
        history.appLock();
        history.clearStage();
        history.addStage(ImportStage.COMPLETE);
        history.setConfigJson(config.toJSON().toString());
        history.apply();
      }

      NotificationFacade.queue(new GlobalNotificationMessage(MessageType.IMPORT_JOB_CHANGE, null));
    }
    else if (stage.equals(ImportStage.RESUME_IMPORT)) // TODO : I'm not sure
                                                      // this code block is ever
                                                      // used
    {
      runImport(history, stage, config);

      if (history.hasImportErrors())
      {
        history.appLock();
        history.clearStage();
        history.addStage(ImportStage.IMPORT_RESOLVE);
        history.setConfigJson(config.toJSON().toString());
        history.apply();

        executionContext.setStatus(AllJobStatus.FEEDBACK);
      }
      else
      {
        history.appLock();
        history.clearStage();
        history.addStage(ImportStage.COMPLETE);
        history.setConfigJson(config.toJSON().toString());
        history.apply();
      }

      NotificationFacade.queue(new GlobalNotificationMessage(MessageType.IMPORT_JOB_CHANGE, null));
    }
    else
    {
      String msg = "Invalid import stage [" + stage.getEnumName() + "].";
      logger.error(msg);
      throw new ProgrammingErrorException(msg);
    }
  }

  private ImportProgressListenerIF runImport(ImportHistory history, ImportStage stage, ImportConfiguration config) throws MalformedURLException, InvocationTargetException
  {
    ImportHistoryProgressScribe progressListener = new ImportHistoryProgressScribe(history);

    FormatSpecificImporterIF formatImporter = FormatSpecificImporterFactory.getImporter(config.getFormatType(), history.getImportFile(), (GeoObjectImportConfiguration) config, progressListener);

    ObjectImporterIF objectImporter = ObjectImporterFactory.getImporter(config.getObjectType(), config, progressListener);

    formatImporter.setObjectImporter(objectImporter);
    objectImporter.setFormatSpecificImporter(formatImporter);

    if (history.getWorkProgress() > 0)
    {
      formatImporter.setStartIndex(history.getWorkProgress() + 1); // We add one
                                                                   // because
                                                                   // the
                                                                   // previous
                                                                   // import
                                                                   // failed at
                                                                   // one row
                                                                   // after
                                                                   // where we
                                                                   // were.
    }

    formatImporter.run(stage);

    return progressListener;
  }

  @Request
  @Override
  public void afterJobExecute(JobHistory history)
  {
    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.IMPORT_JOB_CHANGE, null));

    // TODO : Deleting the ExecutableJob here will also delete the history.
    // AllJobStatus finalStatus = history.getStatus().get(0);
    // ImportStage stage = ((ImportHistory) history).getStage().get(0);
    //
    // if (
    // (finalStatus.equals(AllJobStatus.SUCCESS) &&
    // stage.equals(ImportStage.COMPLETE))
    // || (finalStatus.equals(AllJobStatus.FAILURE))
    // )
    // {
    // String filename = ((ImportHistory)history).getImportFile().getFileName();
    // try
    // {
    // ((ImportHistory)history).getImportFile().delete();
    // }
    // catch (Throwable t)
    // {
    // logger.error("Error deleting vault file. File still exists [" + filename
    // + "].");
    // }
    //
    // this.delete();
    // }
  }

  @Override
  public synchronized void resume(JobHistoryRecord jhr)
  {
    this.resumeInTrans(jhr);

    super.resume(jhr);
  }

  @Transaction
  private void resumeInTrans(JobHistoryRecord jhr)
  {
    ImportHistory hist = (ImportHistory) jhr.getChild();

    ImportStage stage = hist.getStage().get(0);

    if (hist.getStatus().get(0).equals(AllJobStatus.RUNNING))
    {
      // This code block happens when the server restarts after a job was
      // running when it died.
      // Do nothing. Allow the job to resume as normal.
    }
    else
    {
      if (stage.equals(ImportStage.VALIDATION_RESOLVE))
      {
        DataImportJob.deleteValidationProblems(hist);

        hist.appLock();
        hist.clearStage();
        hist.addStage(ImportStage.VALIDATE);
        hist.setWorkProgress(0L);
        hist.setImportedRecords(0L);
        hist.apply();
      }
      // else if (stage.equals(ImportStage.IMPORT_RESOLVE))
      // {
      // hist.appLock();
      // hist.clearStage();
      // hist.setWorkProgress(0);
      // hist.addStage(ImportStage.RESUME_IMPORT);
      // hist.apply();
      // }
      else
      {
        logger.error("Resuming job with unexpected initial stage [" + stage + "].");

        hist.appLock();
        hist.clearStage();
        hist.addStage(ImportStage.VALIDATE);
        hist.apply();
      }
    }

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.IMPORT_JOB_CHANGE, null));
  }

  @Override
  protected JobHistory createNewHistory()
  {
    ImportHistory history = new ImportHistory();
    history.setStartTime(new Date());
    history.addStatus(AllJobStatus.NEW);
    history.addStage(ImportStage.VALIDATE);
    history.setWorkProgress(0L);
    history.setImportedRecords(0L);
    history.apply();

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.IMPORT_JOB_CHANGE, null));

    return history;
  }

  public boolean canResume()
  {
    return true;
  }

  @Override
  protected QuartzRunwayJob createQuartzRunwayJob()
  {
    return new QueueingQuartzJob(this);
  }
}
