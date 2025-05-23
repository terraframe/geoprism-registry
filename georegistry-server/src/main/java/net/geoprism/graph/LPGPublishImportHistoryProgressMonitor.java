package net.geoprism.graph;

import net.geoprism.registry.etl.ImportHistory;
import net.geoprism.registry.etl.ImportStage;
import net.geoprism.registry.lpg.LPGPublishProgressMonitorIF;

public class LPGPublishImportHistoryProgressMonitor implements LPGPublishProgressMonitorIF
{
  
  private ImportHistory history;
  
  public LPGPublishImportHistoryProgressMonitor(ImportHistory history)
  {
    this.history = history;
  }
  
  @Override
  public void appLock()
  {
    this.history.appLock();
  }

  @Override
  public void apply()
  {
    this.history.apply();
  }

  @Override
  public void setWorkProgress(Long num)
  {
    this.history.setWorkProgress(num);
    this.history.setImportedRecords(num);
  }

  @Override
  public void setWorkTotal(Long total)
  {
    this.history.setWorkTotal(total);
  }

  @Override
  public void clearStage()
  {
    this.history.clearStage();
  }

  @Override
  public void addStage(Object importStage)
  {
    this.history.addStage((ImportStage) importStage);
  }

}
