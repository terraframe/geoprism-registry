package net.geoprism.registry.etl;

public interface ImportProgressListenerIF
{
  public void setWorkTotal(Long workTotal);
  
  public Long getWorkTotal();
  
  public void setWorkProgress(Long newWorkProgress);
  
  public Long getWorkProgress();
  
  public void setImportedRecords(Long newImportedRecords);
  
  public Long getImportedRecords();
  
  public void recordError(Throwable ex, String objectJson, String objectType, long rowNum);

  public boolean hasValidationProblems();

  public void addReferenceProblem(ValidationProblem problem);
  
  public void addRowValidationProblem(ValidationProblem problem);
  
  public void applyValidationProblems();
}
