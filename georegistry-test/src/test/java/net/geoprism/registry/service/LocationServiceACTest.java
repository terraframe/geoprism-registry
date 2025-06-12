/**
 *
 */
package net.geoprism.registry.service;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.test.FastTestDataset;

@ContextConfiguration(classes = { TestConfig.class }) @WebAppConfiguration
@RunWith(SpringInstanceTestClassRunner.class)
@Ignore
public class LocationServiceACTest extends AbstractLocationServiceTest implements InstanceTestClassListener
{
  @BeforeClass
  public static void setUpClass()
  {
    testData = FastTestDataset.newTestData();
    testData.setUpMetadata();
    testData.setUpInstanceData();

    testData.logIn(FastTestDataset.USER_CGOV_AC);
  }

}
