/**
 *
 */
package net.geoprism.registry;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;

import com.runwaysdk.constants.VaultProperties;
import com.runwaysdk.session.Request;

import net.geoprism.registry.io.PostalCodeFactory;
import net.geoprism.registry.test.TestOrganizationInfo;
import net.geoprism.registry.test.USATestData;

public abstract class USADatasetTest extends ConceptDatasetTest implements InstanceTestClassListener
{
  protected static USATestData testData;

  @Override
  protected TestOrganizationInfo getOrganization()
  {
    return USATestData.ORG_NPS;
  }

  @Override
  public void beforeClassSetup() throws Exception
  {
    testData = USATestData.newTestData();
    testData.setUpMetadata();

    super.beforeClassSetup();
  }

  @Override
  public void afterClassSetup() throws Exception
  {
    super.afterClassSetup();

    testData.tearDownMetadata();
  }

  @Before
  @Request
  public void setUp() throws Exception
  {
    super.setUp();

    testData.setUpInstanceData();

    testData.logIn(USATestData.USER_NPS_RA);
  }

  @After
  public void tearDown() throws Exception
  {
    testData.logOut();

    testData.tearDownInstanceData();

    super.tearDown();
  }

}
