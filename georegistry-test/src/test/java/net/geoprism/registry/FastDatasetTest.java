/**
 *
 */
package net.geoprism.registry;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;

import com.runwaysdk.session.Request;

import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestOrganizationInfo;

public class FastDatasetTest extends ConceptDatasetTest implements InstanceTestClassListener
{
  protected static FastTestDataset testData;

  @Override
  protected TestOrganizationInfo getOrganization()
  {
    return FastTestDataset.ORG_CGOV;
  }

  @Override
  public void beforeClassSetup() throws Exception
  {
    testData = FastTestDataset.newTestData();
    testData.setUpMetadata();

    super.beforeClassSetup();
  }

  @Override
  public void afterClassSetup() throws Exception
  {
    super.afterClassSetup();

    if (testData != null)
    {
      testData.tearDownMetadata();
    }
  }

  @Before
  @Request
  public void setUp()
  {
    testData.setUpInstanceData();

    testData.logIn();
  }

  @After
  public void tearDown() throws IOException
  {
    testData.logOut();

    testData.tearDownInstanceData();
  }
}
