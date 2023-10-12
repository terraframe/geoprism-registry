/**
 *
 */
package net.geoprism.registry.service;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.test.FastTestDataset;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class LocationServiceRMTest extends AbstractLocationServiceTest implements InstanceTestClassListener
{
  @BeforeClass
  public static void setUpClass()
  {
    testData = FastTestDataset.newTestData();

    testData.setUpMetadata();
    testData.setUpInstanceData();

    testData.logIn(FastTestDataset.USER_CGOV_RM);
  }
}
