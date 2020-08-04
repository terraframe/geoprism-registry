package net.geoprism.registry.service;

import org.junit.BeforeClass;

import net.geoprism.registry.test.FastTestDataset;

public class LocationServiceRMTest extends AbstractLocationServiceTest
{
  @BeforeClass
  public static void setUpClass()
  {
    testData = FastTestDataset.newTestData();
    testData.setSessionUser(testData.USER_CGOV_RM);

    testData.setUpMetadata();
    testData.setUpInstanceData();
  }
}
