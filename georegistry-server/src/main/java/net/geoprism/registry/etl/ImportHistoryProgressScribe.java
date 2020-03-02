package net.geoprism.registry.etl;

import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.system.scheduler.JobHistory;

class ImportHistoryProgressScribe implements ImportProgressListenerIF
{
  private static Logger logger = LoggerFactory.getLogger(ImportHistoryProgressScribe.class);
  
  private ImportHistory history;
  
  private int recordedErrors = 0;
  
  private Set<ValidationProblem> validationProblems = new TreeSet<ValidationProblem>();
  
  public ImportHistoryProgressScribe(ImportHistory history)
  {
    this.history = history;
  }
  
  @Override
  public void setWorkTotal(Long workTotal)
  {
    this.history.appLock();
    this.history.setWorkTotal(workTotal);
    this.history.apply();
    
    logger.info("Starting import with total work size [" + this.history.getWorkTotal() + "] and import stage [" + this.history.getStage().get(0) + "].");
  }

  @Override
  public void setWorkProgress(Long newWorkProgress)
  {
    this.history.appLock();
    this.history.setWorkProgress(newWorkProgress);
    this.history.apply();
  }
  
  @Override
  public void setImportedRecords(Long newImportedRecords)
  {
    this.history.appLock();
    this.history.setImportedRecords(newImportedRecords);
    this.history.apply();
  }

  @Override
  public Long getWorkTotal()
  {
    return this.history.getWorkTotal();
  }

  @Override
  public Long getWorkProgress()
  {
    return this.history.getWorkProgress();
  }

  @Override
  public Long getImportedRecords()
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

  @Override
  public boolean hasValidationProblems()
  {
    return this.validationProblems.size() > 0;
  }

  @Override
  public void addValidationProblem(ValidationProblem problem)
  {
    this.validationProblems.add(problem);
  }
  
  public Set<ValidationProblem> getValidationProblems()
  {
    return this.validationProblems;
  }
}