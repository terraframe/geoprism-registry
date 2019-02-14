package net.geoprism.registry.test.integration;

import com.runwaysdk.dataaccess.transaction.Transaction;

import net.geoprism.GeoprismPatcher;
import net.geoprism.GeoprismPatcherIF;

public class GeoregistryTestPatcher extends GeoprismPatcher implements GeoprismPatcherIF
{
  @Override
  @Transaction
  public void runWithTransaction()
  {
    super.runWithTransaction();
    
    AndroidIntegrationTestDatabaseBuilder.main(new String[] {});
  }
  
  @Override
  protected void importLocationData()
  {
    // skip
  }
  
  
}
