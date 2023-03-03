/**
 *
 */
package net.geoprism.registry.etl;

import net.geoprism.registry.etl.upload.ImportProgressListenerIF;

public class NullImportProgressListener implements ImportProgressListenerIF
{

  @Override
  public void setWorkTotal(Long workTotal)
  {

  }

  @Override
  public Long getWorkTotal()
  {
    return 0L;
  }

  @Override
  public Long getWorkProgress()
  {

    return 0L;
  }

  @Override
  public void setImportedRecords(Long newImportedRecords)
  {

  }

  @Override
  public Long getImportedRecords()
  {

    return 0L;
  }

  @Override
  public void recordError(Throwable ex, String objectJson, String objectType, long rowNum)
  {

  }

  @Override
  public boolean hasValidationProblems()
  {

    return false;
  }

  @Override
  public void addReferenceProblem(ValidationProblem problem)
  {

  }

  @Override
  public void addRowValidationProblem(ValidationProblem problem)
  {

  }

  @Override
  public void applyValidationProblems()
  {

  }

  @Override
  public void setRowNumber(Long rowNumber)
  {
    
  }

  @Override
  public Long getRowNumber()
  {
    return 1L;
  }

  @Override
  public Long getImportedRecordProgress()
  {
    return 0L;
  }

  @Override
  public void finalizeImport()
  {
    
  }

}
