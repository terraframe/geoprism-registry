/**
 *
 */
package net.geoprism.registry.service;

import org.junit.BeforeClass;

import net.geoprism.registry.test.FastTestDataset;

public class LocationServiceRCTest extends AbstractLocationServiceTest
{
  @BeforeClass
  public static void setUpClass()
  {
    testData = FastTestDataset.newTestData();

    testData.setUpMetadata();
    testData.setUpInstanceData();
    
    testData.logIn(FastTestDataset.USER_CGOV_RC);
  }
}
