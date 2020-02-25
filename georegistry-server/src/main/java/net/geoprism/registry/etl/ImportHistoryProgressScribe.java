package net.geoprism.registry.etl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.system.scheduler.JobHistory;

class ImportHistoryProgressScribe implements ImportProgressListenerIF
{
  private static Logger logger = LoggerFactory.getLogger(ImportHistoryProgressScribe.class);
  
  private ImportHistory history;
  
  private int recordedErrors = 0;
  
  public ImportHistoryProgressScribe(ImportHistory history)
  {
    this.history = history;
  }
  
  @Override
  public void setWorkTotal(int workTotal)
  {
    this.history.appLock();
    this.history.setWorkTotal(workTotal);
    this.history.apply();
    
    logger.info("Starting import with total work size [" + this.history.getWorkTotal() + "] and import stage [" + this.history.getStage().get(0) + "].");
  }

  @Override
  public void setWorkProgress(int newWorkProgress)
  {
    this.history.appLock();
    this.history.setWorkProgress(newWorkProgress);
    this.history.apply();
  }
  
  @Override
  public void setImportedRecords(int newImportedRecords)
  {
    this.history.appLock();
    this.history.setImportedRecords(newImportedRecords);
    this.history.apply();
  }

  @Override
  public Integer getWorkTotal()
  {
    return this.history.getWorkTotal();
  }

  @Override
  public Integer getWorkProgress()
  {
    return this.history.getWorkProgress();
  }

  @Override
  public Integer getImportedRecords()
  {
    return this.history.getImportedRecords();
  }

  @Override
  public void recordError(Throwable ex, String objectJson, String objectType)
  {
    ImportError error = new ImportError();
    error.setHistory(this.history);
    error.setErrorJson(JobHistory.exceptionToJson(ex).toString());
    error.setObjectJson(objectJson);
    error.setObjectType(objectType);
    error.apply();
    
    this.recordedErrors++;
  }
  
  public int getRecordedErrorCount()
  {
    return this.recordedErrors;
  }
}