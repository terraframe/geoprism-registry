/**
 *
 */
package net.geoprism.registry.test.integration;

import net.geoprism.build.GeoprismDatabaseBuilder;
import net.geoprism.build.GeoprismDatabaseBuilderIF;
import net.geoprism.registry.test.CambodiaTestDataset;

public class GeoregistryTestPatcher extends GeoprismDatabaseBuilder implements GeoprismDatabaseBuilderIF
{
  @Override
  protected void runWithRequest()
  {
    super.runWithRequest();
    
//    AndroidIntegrationTestDatabaseBuilder.main(new String[] {});
    CambodiaTestDataset.main(new String[] {});
  }
}
