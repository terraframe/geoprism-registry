package net.geoprism.registry.etl;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutionContext;
import com.runwaysdk.system.scheduler.JobHistory;
import com.runwaysdk.system.scheduler.JobHistoryRecord;
import com.runwaysdk.system.scheduler.QuartzRunwayJob;
import com.runwaysdk.system.scheduler.QueueingQuartzJob;

public class DataImportJob extends DataImportJobBase
{
  private static final long serialVersionUID = 1742592504;
  
  private static Logger logger = LoggerFactory.getLogger(DataImportJob.class);
  
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
    ImportHistory history = (ImportHistory) this.createNewHistory();
    
    configuration.setHistoryId(history.getOid());
    configuration.setJobId(this.getOid());
    
    history.appLock();
    history.setConfigJson(configuration.toJSON().toString());
    history.setImportFileId(configuration.getVaultFileId());
    history.apply();
    
    JobHistoryRecord record = new JobHistoryRecord(this, history);
    record.apply();

    this.getQuartzJob().start(record);

    return history;
  }
  
  protected void validate(ImportConfiguration config)
  {
    config.validate();
  }
  
  private void deleteValidationProblems(ImportHistory history)
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
    ImportHistory history = (ImportHistory) executionContext.getJobHistoryRecord().getChild();
    ImportStage stage = history.getStage().get(0);
    ImportConfiguration config = ImportConfiguration.build(history.getConfigJson());
    
    process(executionContext, history, stage, config);
  }

  // TODO : It might actually be faster to first convert into a shared temp table, assuming you're resolving the parent references into it.
  private void process(ExecutionContext executionContext, ImportHistory history, ImportStage stage, ImportConfiguration config) throws MalformedURLException, InvocationTargetException
  {
    validate(config);
    
    // TODO : We should have a single transaction where we do all the history configuration upfront, that way the job is either fully configured (and resumable) or it isn't (no in-between)
    config.setHistoryId(history.getOid());
    config.setJobId(this.getOid());
    
    if (stage.equals(ImportStage.VALIDATE))
    {
      deleteValidationProblems(history);
      
      history.appLock();
      history.setWorkProgress(0L);
      history.setImportedRecords(0L);
      history.apply();
      
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
      }
      else
      {
        history.appLock();
        history.clearStage();
        history.addStage(ImportStage.IMPORT);
        history.setConfigJson(config.toJSON().toString());
        history.apply();
        
        this.process(executionContext, history, ImportStage.IMPORT, config);
      }
    }
    else if (stage.equals(ImportStage.IMPORT))
    {
      deleteValidationProblems(history);
      
      history.appLock();
      history.setWorkProgress(0L);
      history.setImportedRecords(0L);
      history.apply();
      
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
    }
    else if (stage.equals(ImportStage.RESUME_IMPORT))
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
    
    FormatSpecificImporterIF formatImporter = FormatSpecificImporterFactory.getImporter(config.getFormatType(), history.getImportFile(), config, progressListener);
    
    ObjectImporterIF objectImporter = ObjectImporterFactory.getImporter(config.getObjectType(), config, progressListener);
    
    formatImporter.setObjectImporter(objectImporter);
    objectImporter.setFormatSpecificImporter(formatImporter);
    
    if (history.getWorkProgress() > 0)
    {
      formatImporter.setStartIndex(history.getWorkProgress());
    }
    
    formatImporter.run(stage);
    
    return progressListener;
  }
  
  @Request
  @Override
  public void afterJobExecute(JobHistory history)
  {
    // TODO : Deleting the ExecutableJob here will also delete the history.
//    AllJobStatus finalStatus = history.getStatus().get(0);
//    ImportStage stage = ((ImportHistory) history).getStage().get(0);
//    
//    if (
//        (finalStatus.equals(AllJobStatus.SUCCESS) && stage.equals(ImportStage.COMPLETE))
//        || (finalStatus.equals(AllJobStatus.FAILURE))
//       )
//    {
//      String filename = ((ImportHistory)history).getImportFile().getFileName();
//      try
//      {
//        ((ImportHistory)history).getImportFile().delete();
//      }
//      catch (Throwable t)
//      {
//        logger.error("Error deleting vault file. File still exists [" + filename + "].");
//      }
//      
//      this.delete();
//    }
  }
  
  @Override
  public synchronized void resume(JobHistoryRecord jhr)
  {
    ImportHistory hist = (ImportHistory) jhr.getChild();
    
    ImportStage stage = hist.getStage().get(0);
    
    if (stage.equals(ImportStage.VALIDATION_RESOLVE))
    {
      hist.appLock();
      hist.clearStage();
      hist.addStage(ImportStage.VALIDATE);
      hist.setWorkProgress(0L);
      hist.setImportedRecords(0L);
      hist.apply();
    }
//    else if (stage.equals(ImportStage.IMPORT_RESOLVE))
//    {
//    hist.appLock();
//    hist.clearStage();
//    hist.setWorkProgress(0);
//    hist.addStage(ImportStage.RESUME_IMPORT);
//    hist.apply();
//  }
    else
    {
      logger.error("Resuming job with unexpected initial stage [" + stage + "].");
      
      hist.appLock();
      hist.clearStage();
      hist.addStage(ImportStage.VALIDATE);
      hist.apply();
    }
    
    super.resume(jhr);
  }
  
  @Override
  protected JobHistory createNewHistory()
  {
    ImportHistory history = new ImportHistory();
    history.setStartTime(new Date());
    history.addStatus(AllJobStatus.NEW);
    history.addStage(ImportStage.VALIDATE); 
    history.apply();
    
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
