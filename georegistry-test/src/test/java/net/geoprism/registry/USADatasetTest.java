/**
 *
 */
package net.geoprism.registry;

import net.geoprism.registry.test.USATestData;

public class USADatasetTest implements InstanceTestClassListener
{
  protected static USATestData testData;

  @Override
  public void beforeClassSetup() throws Exception
  {
    testData = USATestData.newTestData();
    testData.setUpMetadata();
  }

  @Override
  public void afterClassSetup() throws Exception
  {
    testData.tearDownMetadata();
  }
}
