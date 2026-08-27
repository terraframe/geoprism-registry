/**
 *
 */
package net.geoprism.registry;

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
}
