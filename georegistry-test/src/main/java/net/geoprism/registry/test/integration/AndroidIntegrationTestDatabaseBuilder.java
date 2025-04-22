/**
 *
 */
package net.geoprism.registry.test.integration;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;

import net.geoprism.registry.test.USATestData;

/**
 * This class must be run before running the Andorid FullStackIntegrationTest.
 * This class brings your database to a state which will enable the test to run
 * successfully, regardless of any tests that ran before it.
 * 
 * @author rrowlands
 */
public class AndroidIntegrationTestDatabaseBuilder
{
  public static void main(String[] args)
  {
    AndroidIntegrationTestDatabaseBuilder.buildFromMain();
  }

  @Request
  private static void buildFromMain()
  {
    USATestData data = USATestData.newTestData();
    data.setUpMetadata();
    data.setUpInstanceData();

    build(data);
  }

  @Request
  public static void build(USATestData data)
  {
    buildInTransaction(data);
  }

  @Transaction
  private static void buildInTransaction(USATestData data)
  {
    data.newTestGeoObjectInfo("Utah", USATestData.STATE, USATestData.SOURCE).delete();
    data.newTestGeoObjectInfo("California", USATestData.STATE, USATestData.SOURCE).delete();
    data.newTestGeoObjectInfo("TEST_ADD_CHILD", USATestData.DISTRICT, USATestData.SOURCE).apply();
  }
}
