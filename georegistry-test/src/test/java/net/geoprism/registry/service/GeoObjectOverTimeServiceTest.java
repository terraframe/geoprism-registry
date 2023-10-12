/**
 *
 */
package net.geoprism.registry.service;

import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.geotools.geometry.jts.GeometryBuilder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.runwaysdk.business.SmartExceptionDTO;

import net.geoprism.registry.GeometryTypeException;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.TestRegistryClient;
import net.geoprism.registry.test.TestUserInfo;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class GeoObjectOverTimeServiceTest implements InstanceTestClassListener
{
  protected static FastTestDataset      testData;

  public static final TestGeoObjectInfo TEST_GO = new TestGeoObjectInfo("GOSERV_TEST_GO", FastTestDataset.COUNTRY);

  @Autowired
  private TestRegistryClient            client;

  @BeforeClass
  public static void setUpClass()
  {
    testData = FastTestDataset.newTestData();
    testData.setUpMetadata();
  }

  @AfterClass
  public static void cleanUpClass()
  {
    testData.tearDownMetadata();
  }

  @Before
  public void setUp()
  {
    testData.setUpInstanceData();

    TEST_GO.delete();

    testData.logIn(FastTestDataset.USER_CGOV_RA);
  }

  @After
  public void tearDown()
  {
    testData.logOut();

    testData.tearDownInstanceData();
  }

  @Test
  public void testGetGeoObjectOverTime()
  {
    // Allowed Users
    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC };

    for (TestUserInfo user : allowedUsers)
    {
      FastTestDataset.runAsUser(user, (request) -> {
        GeoObjectOverTime geoObj = client.getGeoObjectOverTime(FastTestDataset.CAMBODIA.getRegistryId(), FastTestDataset.CAMBODIA.getGeoObjectType().getCode());

        FastTestDataset.CAMBODIA.assertEquals(geoObj);
      });
    }
  }

  @Test
  public void testGetGeoObjectOverTimeByCode()
  {
    // Allowed Users
    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC };

    for (TestUserInfo user : allowedUsers)
    {
      FastTestDataset.runAsUser(user, (request) -> {
        GeoObjectOverTime geoObj = client.getGeoObjectOverTimeByCode(FastTestDataset.CAMBODIA.getCode(), FastTestDataset.CAMBODIA.getGeoObjectType().getCode());

        Assert.assertEquals(geoObj.toJSON().toString(), GeoObjectOverTime.fromJSON(client.getAdapter(), geoObj.toJSON().toString()).toJSON().toString());
        Assert.assertEquals(true, geoObj.getExists(TestDataSet.DEFAULT_OVER_TIME_DATE));
      });
    }
  }

  @Test
  public void testCreateGeoObjectOverTimeBadGeometry()
  {
    GeometryBuilder builder = new GeometryBuilder(new GeometryFactory());
    Point point = builder.point(48.44, -123.37);

    GeoObjectOverTime geoObj = TEST_GO.newGeoObjectOverTime(client.getAdapter());

    geoObj.setGeometry(point, TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_OVER_TIME_DATE);

    try
    {
      client.createGeoObjectOverTime(geoObj.toJSON().toString());

      Assert.fail("Able to create a GeoObject with a wrong geometry type");
    }
    catch (SmartExceptionDTO e)
    {
      // This is expected
      Assert.assertEquals(GeometryTypeException.CLASS, e.getType());
    }
  }

  @Test
  public void testCreateGeoObjectOverTime()
  {
    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM };

    for (TestUserInfo user : allowedUsers)
    {
      TestDataSet.runAsUser(user, (request) -> {
        TestDataSet.populateAdapterIds(user, client.getAdapter());

        TEST_GO.assertEquals(client.createGeoObjectOverTime(TEST_GO.newGeoObjectOverTime(client.getAdapter()).toJSON().toString()));
        TEST_GO.assertApplied();
        TEST_GO.delete();
      });
    }

    TestUserInfo[] disallowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC, FastTestDataset.USER_MOHA_RA };

    for (TestUserInfo user : disallowedUsers)
    {
      TestDataSet.runAsUser(user, (request) -> {
        TestDataSet.populateAdapterIds(user, client.getAdapter());

        try
        {
          client.createGeoObjectOverTime(TEST_GO.newGeoObjectOverTime(ServiceFactory.getAdapter()).toJSON().toString());

          Assert.fail();
        }
        catch (SmartExceptionDTO ex)
        {
          // expected
        }
      });
    }
  }

  private void updateGO(TestGeoObjectInfo go)
  {
    go.setWkt(TestDataSet.WKT_POLYGON_2);
    go.setDisplayLabel("Some new value");

    GeoObjectOverTime update = go.fetchGeoObjectOverTime();
    go.populate(update);

    GeoObjectOverTime returnedUpdate = client.updateGeoObjectOverTime(update.toJSON().toString());

    go.assertEquals(returnedUpdate);

    go.assertApplied();
  }

  @Test
  public void testUpdateGeoObjectOverTime()
  {
    // Allowed Users
    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM };

    for (TestUserInfo user : allowedUsers)
    {
      TestGeoObjectInfo go = testData.newTestGeoObjectInfo("UpdateTest", FastTestDataset.COUNTRY);
      go.apply();

      TestDataSet.runAsUser(user, (request) -> {
        updateGO(go);
      });

      go.delete();
    }

    // Disallowed Users
    TestUserInfo[] disallowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_AC, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_MOHA_RA };

    for (TestUserInfo user : disallowedUsers)
    {
      TestGeoObjectInfo go = testData.newTestGeoObjectInfo("UpdateTest", FastTestDataset.COUNTRY);
      go.apply();

      TestDataSet.runAsUser(user, (request) -> {
        try
        {
          updateGO(go);

          Assert.fail();
        }
        catch (SmartExceptionDTO ex)
        {
          // expected
        }
      });
    }
  }

}
