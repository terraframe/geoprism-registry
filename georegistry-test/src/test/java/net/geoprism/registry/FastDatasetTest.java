/**
 *
 */
package net.geoprism.registry;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;

import com.runwaysdk.constants.VaultProperties;
import com.runwaysdk.session.Request;

import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;

public class FastDatasetTest extends DatasetTest implements InstanceTestClassListener
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
