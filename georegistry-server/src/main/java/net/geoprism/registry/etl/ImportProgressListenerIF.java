package net.geoprism.registry.etl;

public interface ImportProgressListenerIF
{
  public void setWorkTotal(int workTotal);
  
  public Integer getWorkTotal();
  
  public void setWorkProgress(int newWorkProgress);
  
  public Integer getWorkProgress();
  
  public void setImportedRecords(int newImportedRecords);
  
  public Integer getImportedRecords();
  
  public void recordError(Throwable ex, String objectJson, String objectType);
}
