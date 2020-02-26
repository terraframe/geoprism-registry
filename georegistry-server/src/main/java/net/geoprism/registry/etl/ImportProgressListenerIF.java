package net.geoprism.registry.etl;

import java.util.Set;

public interface ImportProgressListenerIF
{
  public void setWorkTotal(Long workTotal);
  
  public Long getWorkTotal();
  
  public void setWorkProgress(Long newWorkProgress);
  
  public Long getWorkProgress();
  
  public void setImportedRecords(Long newImportedRecords);
  
  public Long getImportedRecords();
  
  public void recordError(Throwable ex, String objectJson, String objectType);

  public boolean hasValidationProblems();

  public void addValidationProblem(ValidationProblem problem);
  
  public Set<ValidationProblem> getValidationProblems();
}
