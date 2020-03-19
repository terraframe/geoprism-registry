package net.geoprism.registry.etl;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.system.scheduler.JobHistory;

class ImportHistoryProgressScribe implements ImportProgressListenerIF
{
  private static Logger logger = LoggerFactory.getLogger(ImportHistoryProgressScribe.class);
  
  private ImportHistory history;
  
  private int recordedErrors = 0;
  
  private Set<ValidationProblem> referenceProblems = new TreeSet<ValidationProblem>();
  
  private Set<ValidationProblem> rowValidationProblems = new TreeSet<ValidationProblem>();
  
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
    
    this.history.appLock();
    this.history.setErrorCount(this.history.getErrorCount() + 1);
    this.history.apply();
    
    this.recordedErrors++;
  }
  
  public int getRecordedErrorCount()
  {
    return this.recordedErrors;
  }

  @Override
  public boolean hasValidationProblems()
  {
    return this.rowValidationProblems.size() > 0 || this.referenceProblems.size() > 0;
  }
  
  @Override
  public void addReferenceProblem(ValidationProblem problem)
  {
    Iterator<ValidationProblem> it = this.referenceProblems.iterator();
    
    while (it.hasNext())
    {
      ValidationProblem vp = it.next();
      
      if (vp.getKey().equals(problem.getKey()))
      {
        vp.addAffectedRowNumber(Long.valueOf(problem.getAffectedRows()));
        return;
      }
    }
    
    this.referenceProblems.add(problem);
  }

  @Override
  public void addRowValidationProblem(ValidationProblem problem)
  {
    Iterator<ValidationProblem> it = this.rowValidationProblems.iterator();
    
    while (it.hasNext())
    {
      ValidationProblem vp = it.next();
      
      if (vp.getKey().equals(problem.getKey()))
      {
        vp.addAffectedRowNumber(Long.valueOf(problem.getAffectedRows()));
        return;
      }
    }
    
    this.rowValidationProblems.add(problem);
  }

  @Override
  public void applyValidationProblems()
  {
    for (ValidationProblem problem : this.referenceProblems)
    {
      problem.setHistory(this.history);
      problem.apply();
    }
    for (ValidationProblem problem : this.rowValidationProblems)
    {
      problem.setHistory(this.history);
      problem.apply();
    }
  }
}