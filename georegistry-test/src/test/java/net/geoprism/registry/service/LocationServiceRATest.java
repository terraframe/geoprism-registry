/**
 *
 */
package net.geoprism.registry.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.InstanceTestClassExtension;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.test.FastTestDataset;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@ExtendWith(InstanceTestClassExtension.class)
@Disabled
public class LocationServiceRATest extends AbstractLocationServiceTest implements InstanceTestClassListener
{
  @BeforeAll
  public static void setUpClass()
  {
    testData = FastTestDataset.newTestData();

    testData.setUpMetadata();
    testData.setUpInstanceData();
    
    testData.logIn(FastTestDataset.USER_CGOV_RA);
  }
}
