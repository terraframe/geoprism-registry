/**
 *
 */
package net.geoprism.registry.excel;

import java.io.File;

import com.runwaysdk.resource.FileResource;

import net.geoprism.data.importer.FeatureRow;
import net.geoprism.registry.etl.ImportStage;
import net.geoprism.registry.etl.ValidationProblem;
import net.geoprism.registry.etl.upload.ExcelImporter;
import net.geoprism.registry.etl.upload.FormatSpecificImporterIF;
import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.etl.upload.ImportProgressListenerIF;
import net.geoprism.registry.etl.upload.ObjectImporterIF;
import net.geoprism.registry.io.GeoObjectImportConfiguration;

public class ExcelRowCounter
{
  public static class RowCounterListener implements ImportProgressListenerIF
  {
    private long workTotal;
    
    private long completed;
    
    private long imported;
    
    @Override
    public void setWorkTotal(Long workTotal)
    {
      this.workTotal = workTotal;
    }

    @Override
    public Long getWorkTotal()
    {
      return workTotal;
    }

    @Override
    public void setCompletedRow(Long rowNumber)
    {
      completed = rowNumber;
    }

    @Override
    public Long getRowNumber()
    {
      return completed;
    }

    @Override
    public void setImportedRecords(Long newImportedRecords)
    {
      imported = newImportedRecords;
    }

    @Override
    public Long getImportedRecords()
    {
      return imported;
    }

    @Override
    public Long getWorkProgress()
    {
      return completed;
    }

    @Override
    public Long getImportedRecordProgress()
    {
      return null;
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
    public void finalizeImport()
    {
      
    }

    @Override
    public void incrementImportedRecords()
    {
      
    }

    @Override
    public boolean isComplete(Long rowNumber)
    {
      return false;
    }
  }
  
  public static class NullObjectImporter implements ObjectImporterIF
  {
    ImportProgressListenerIF monitor;
    
    public NullObjectImporter(ImportProgressListenerIF monitor)
    {
      this.monitor = monitor;
    }

    @Override
    public void validateRow(FeatureRow simpleFeatureRow) throws InterruptedException
    {
      this.monitor.setCompletedRow(simpleFeatureRow.getRowNumber());
    }

    @Override
    public void importRow(FeatureRow simpleFeatureRow) throws InterruptedException
    {
      
    }

    @Override
    public void setFormatSpecificImporter(FormatSpecificImporterIF formatImporter)
    {
      
    }

    @Override
    public ImportConfiguration getConfiguration()
    {
      return null;
    }

    @Override
    public void close()
    {
      
    }
  }
  
  public static void main(String[] args) throws InterruptedException
  {
    ImportProgressListenerIF monitor = new RowCounterListener();
    ImportConfiguration config = new GeoObjectImportConfiguration();
    
    ExcelImporter importer = new ExcelImporter(new FileResource(new File("/home/rrowlands/Documents/cgr-staging-health-center-list.xlsx")), config, monitor);
    importer.setObjectImporter(new NullObjectImporter(monitor));
    importer.run(ImportStage.VALIDATE);
    
    System.out.println("Run with total = " + monitor.getWorkTotal() + " progress = " + monitor.getWorkProgress());
  }
}
