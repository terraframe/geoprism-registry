/**
 *
 */
package net.geoprism.registry.service;

import java.util.Date;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import com.google.gson.JsonObject;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.database.DuplicateDataDatabaseException;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.ListType;
import net.geoprism.registry.ListTypeEntry;
import net.geoprism.registry.ListTypeQuery;
import net.geoprism.registry.ListTypeVersion;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.USATestData;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class ListTypeInheritedHierarchyTest implements InstanceTestClassListener
{
  private static USATestData testData;

  @BeforeClass
  public static void setUpClass()
  {
    testData = USATestData.newTestData();
    testData.setUpMetadata();

    setUpInReq();
  }

  @Request
  private static void setUpInReq()
  {
    ServerGeoObjectType sGO = USATestData.DISTRICT.getServerObject();
    sGO.setInheritedHierarchy(USATestData.HIER_SCHOOL.getServerObject(), USATestData.HIER_ADMIN.getServerObject());
  }

  @AfterClass
  @Request
  public static void cleanUpClass()
  {
    if (testData != null)
    {
      testData.tearDownMetadata();
    }
  }

  @Before
  public void setUp()
  {
    cleanUpExtra();

    testData.setUpInstanceData();

    testData.logIn(USATestData.USER_NPS_RA);
  }

  @After
  public void tearDown()
  {
    testData.logOut();

    cleanUpExtra();

    testData.tearDownInstanceData();
  }

  @Request
  public void cleanUpExtra()
  {
    ListTypeQuery query = new ListTypeQuery(new QueryFactory());

    OIterator<? extends ListType> it = query.getIterator();

    try
    {
      while (it.hasNext())
      {
        it.next().delete();
      }
    }
    finally
    {
      it.close();
    }
  }

  @Test
  public void testPublishVersion()
  {
    TestDataSet.executeRequestAsUser(USATestData.USER_ADMIN, () -> {

      JsonObject json = ListTypeTest.getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_SCHOOL, USATestData.SCHOOL_ZONE, USATestData.COUNTRY, USATestData.STATE, USATestData.DISTRICT);

      ListType test = ListType.apply(json);

      try
      {
        ListTypeEntry entry = test.getOrCreateEntry(new Date(), null);
        ListTypeVersion version = entry.getWorking();

        try
        {
          MdBusinessDAOIF mdTable = MdBusinessDAO.get(version.getMdBusinessOid());

          Assert.assertNotNull(mdTable);

          version.publish();
        }
        finally
        {
          entry.delete();
        }
      }
      finally
      {
        test.delete();
      }
    });
  }

  @Test
  @Request
  public void testMarkAsInvalidByInheritedParent()
  {
    JsonObject json = ListTypeTest.getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_SCHOOL, USATestData.SCHOOL_ZONE, USATestData.DISTRICT, USATestData.STATE);

    ListType masterlist = ListType.apply(json);

    try
    {
      masterlist.markAsInvalid(USATestData.HIER_ADMIN.getServerObject(), USATestData.STATE.getServerObject());

      Assert.assertFalse(masterlist.isValid());
    }
    catch (DuplicateDataDatabaseException e)
    {
      masterlist.delete();
    }
  }

}
