/**
 *
 */
package net.geoprism.registry.test.integration;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;

import net.geoprism.registry.test.CambodiaTestDataset;
import net.geoprism.registry.test.USATestData;

/**
 * This class must be run before running the Andorid FullStackIntegrationTest. This class brings your database
 * to a state which will enable the test to run successfully, regardless of any tests that ran before it.
 * 
 * @author rrowlands
 */
public class DevelopmentDatabaseBuilder
{
  public static void main(String[] args)
  {
    DevelopmentDatabaseBuilder.buildFromMain();
  }
  
  @Request
  private static void buildFromMain()
  {
    CambodiaTestDataset.main(new String[] {});
    
    buildInTransaction();
  }
  
  @Transaction
  private static void buildInTransaction()
  {
    
  }
}
