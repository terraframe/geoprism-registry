package net.geoprism.registry;

import com.runwaysdk.session.Request;

/**
 * This class must be run before running the Andorid FullStackIntegrationTest. This class brings your database
 * to a state which will enable the test to run successfully, regardless of any tests that ran before it.
 * 
 * @author rrowlands
 */
public class FullStackIntegrationTestDatabaseBuilder
{
  public static void main(String[] args)
  {
    FullStackIntegrationTestDatabaseBuilder.build();
  }
  
  @Request
  private static void build()
  {
    USATestData data = new USATestData();
    data.setUp();
    
    data.newTestGeoEntityInfo("Utah", USATestData.STATE).delete();
  }
}
