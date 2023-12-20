package net.geoprism.registry;

import net.geoprism.registry.test.FastTestDataset;

public class FastDatasetTest implements InstanceTestClassListener
{
  protected static FastTestDataset testData;

  @Override
  public void beforeClassSetup() throws Exception
  {
    testData = FastTestDataset.newTestData();
    testData.setUpMetadata();
  }

  @Override
  public void afterClassSetup() throws Exception
  {
    if (testData != null)
    {
      testData.tearDownMetadata();
    }
  }
}
