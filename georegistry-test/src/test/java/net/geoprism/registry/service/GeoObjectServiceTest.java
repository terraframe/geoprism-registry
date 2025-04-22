/**
 *
 */
package net.geoprism.registry.service;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.geotools.geometry.jts.GeometryBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.business.SmartExceptionDTO;
import com.runwaysdk.session.Request;

import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.GeometryTypeException;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.roles.CreateGeoObjectPermissionException;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.service.business.ServiceFactory;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.TestRegistryClient;
import net.geoprism.registry.test.TestUserInfo;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class GeoObjectServiceTest extends FastDatasetTest implements InstanceTestClassListener
{
  public static final TestGeoObjectInfo TEST_GO         = new TestGeoObjectInfo("GOSERV_TEST_GO", FastTestDataset.COUNTRY, FastTestDataset.SOURCE);

  public static final TestGeoObjectInfo TEST_GO_PRIVATE = new TestGeoObjectInfo("GOSERV_TEST_GO_PRIVATE", FastTestDataset.PROVINCE_PRIVATE, FastTestDataset.SOURCE);

  @Autowired
  private TestRegistryClient            client;

  @Autowired
  private GeoObjectBusinessServiceIF    goService;

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
  public void testGetGeoObject()
  {
    // Allowed Users
    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_CGOV_RM, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC };

    for (TestUserInfo user : allowedUsers)
    {
      FastTestDataset.runAsUser(user, (request) -> {
        GeoObject geoObj = client.getGeoObject(FastTestDataset.CAMBODIA.getRegistryId(), FastTestDataset.CAMBODIA.getGeoObjectType().getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

        FastTestDataset.CAMBODIA.assertEquals(geoObj);

        Assert.assertEquals(true, geoObj.getExists());
      });
    }
  }

  @Test
  public void testGetPrivateGeoObject()
  {
    // Test allowed users on a private GeoObjectType
    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM_PRIVATE, FastTestDataset.USER_CGOV_RC_PRIVATE, FastTestDataset.USER_CGOV_AC_PRIVATE };
    for (TestUserInfo user : allowedUsers)
    {
      FastTestDataset.runAsUser(user, (request) -> {
        GeoObject geoObj = client.getGeoObject(FastTestDataset.PROV_CENTRAL_PRIVATE.getRegistryId(), FastTestDataset.PROV_CENTRAL_PRIVATE.getGeoObjectType().getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

        FastTestDataset.PROV_CENTRAL_PRIVATE.assertEquals(geoObj);

        Assert.assertEquals(true, geoObj.getExists());
      });
    }

    // Disallowed Users
    TestUserInfo[] disllowedUsers = new TestUserInfo[] { FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_MOHA_RM, FastTestDataset.USER_MOHA_RC, FastTestDataset.USER_MOHA_AC, FastTestDataset.USER_CGOV_RM, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC };

    for (TestUserInfo user : disllowedUsers)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request) -> {
          client.getGeoObject(FastTestDataset.PROV_CENTRAL_PRIVATE.getRegistryId(), FastTestDataset.PROV_CENTRAL_PRIVATE.getGeoObjectType().getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

          Assert.fail();
        });
      }
      catch (SmartExceptionDTO e)
      {
        // Expected
      }
    }
  }

  @Test
  public void testGetGeoObjectByCode()
  {
    // Allowed Users
    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_CGOV_RM, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC };

    for (TestUserInfo user : allowedUsers)
    {
      FastTestDataset.runAsUser(user, (request) -> {
        GeoObject geoObj = client.getGeoObjectByCode(FastTestDataset.CAMBODIA.getCode(), FastTestDataset.CAMBODIA.getGeoObjectType().getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

        Assert.assertEquals(geoObj.toJSON().toString(), GeoObject.fromJSON(client.getAdapter(), geoObj.toJSON().toString()).toJSON().toString());
        Assert.assertEquals(true, geoObj.getExists());
      });
    }
  }

  @Test
  @Request
  public void testCodeStripWhitespace()
  {
    TEST_GO.apply();

    ServerGeoObjectIF serverGo = TEST_GO.getServerObject();

    serverGo.setCode("\t" + serverGo.getCode() + " ");

    goService.apply(serverGo, false);

    Assert.assertEquals(TEST_GO.getCode(), TEST_GO.getServerObject().getCode());
  }

  @Test
  public void testGetPrivateGeoObjectByCode()
  {
    // Allowed Users
    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM_PRIVATE, FastTestDataset.USER_CGOV_RC_PRIVATE, FastTestDataset.USER_CGOV_AC_PRIVATE };
    for (TestUserInfo user : allowedUsers)
    {
      FastTestDataset.runAsUser(user, (request) -> {
        GeoObject geoObj = client.getGeoObjectByCode(FastTestDataset.PROV_CENTRAL_PRIVATE.getCode(), FastTestDataset.PROV_CENTRAL_PRIVATE.getGeoObjectType().getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

        Assert.assertEquals(geoObj.toJSON().toString(), GeoObject.fromJSON(client.getAdapter(), geoObj.toJSON().toString()).toJSON().toString());
        Assert.assertEquals(true, geoObj.getExists());
      });
    }

    // Disallowed Users
    TestUserInfo[] disllowedUsers = new TestUserInfo[] { FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_MOHA_RM, FastTestDataset.USER_MOHA_RC, FastTestDataset.USER_MOHA_AC, FastTestDataset.USER_CGOV_RM, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC };
    for (TestUserInfo user : disllowedUsers)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request) -> {
          client.getGeoObjectByCode(FastTestDataset.PROV_CENTRAL_PRIVATE.getCode(), FastTestDataset.PROV_CENTRAL_PRIVATE.getGeoObjectType().getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

          Assert.fail();
        });
      }
      catch (SmartExceptionDTO e)
      {
        // This is expected
      }
    }
  }

  @Test
  public void testCreateGeoObjectBadGeometry()
  {
    GeometryBuilder builder = new GeometryBuilder(new GeometryFactory());
    Point point = builder.point(48.44, -123.37);

    GeoObject geoObj = client.getAdapter().newGeoObjectInstance(FastTestDataset.PROVINCE.getCode());
    geoObj.setGeometry(point);
    geoObj.setCode(FastTestDataset.CAMBODIA.getCode());
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, FastTestDataset.CAMBODIA.getDisplayLabel());

    try
    {
      client.createGeoObject(geoObj.toJSON().toString(), TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE);

      Assert.fail("Able to create a GeoObject with a wrong geometry type");
    }
    catch (SmartExceptionDTO e)
    {
      // This is expected
      Assert.assertEquals(GeometryTypeException.CLASS, e.getType());
    }
  }

  @Test
  public void testCreateGeoObject()
  {
    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM };

    for (TestUserInfo user : allowedUsers)
    {
      TestDataSet.runAsUser(user, (request) -> {
        TestDataSet.populateAdapterIds(user, client.getAdapter());

        GeoObject returned = client.createGeoObject(TEST_GO.newGeoObject(client.getAdapter()).toJSON().toString(), TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE);

        TEST_GO.assertEquals(returned);

        Assert.assertEquals(true, returned.getExists());

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
          client.createGeoObject(TEST_GO.newGeoObject(client.getAdapter()).toJSON().toString(), TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE);

          Assert.fail();
        }
        catch (SmartExceptionDTO ex)
        {
          Assert.assertEquals(CreateGeoObjectPermissionException.CLASS, ex.getType());
        }
      });
    }
  }

  @Test
  public void testCreateGeoObjectNoDate()
  {
    TestDataSet.runAsUser(FastTestDataset.USER_CGOV_RA, (request) -> {
      TestDataSet.populateAdapterIds(FastTestDataset.USER_CGOV_RA, client.getAdapter());

      GeoObject returned = client.createGeoObject(TEST_GO.newGeoObject(client.getAdapter()).toJSON().toString(), null, null);

      TEST_GO.assertEquals(returned);

      Assert.assertEquals(true, returned.getExists());

      Calendar cal = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
      cal.clear();
      cal.setTime(new Date());
      cal.add(Calendar.DAY_OF_YEAR, 1);
      Date startDate = cal.getTime();

      TEST_GO.assertApplied(startDate);
      TEST_GO.delete();
    });
  }

  @Test
  public void testCreatePrivateGeoObject()
  {
    // Allowed Users
    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM_PRIVATE };

    for (TestUserInfo user : allowedUsers)
    {
      TestDataSet.runAsUser(user, (request) -> {
        TestDataSet.populateAdapterIds(user, client.getAdapter());

        GeoObject returned = client.createGeoObject(TEST_GO_PRIVATE.newGeoObject(client.getAdapter()).toJSON().toString(), TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE);

        TEST_GO_PRIVATE.assertEquals(returned);

        Assert.assertEquals(true, returned.getExists());

        TEST_GO_PRIVATE.assertApplied();
        TEST_GO_PRIVATE.delete();
      });
    }

    // Disallowed Users
    TestUserInfo[] disallowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_AC, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_MOHA_RM, FastTestDataset.USER_MOHA_RC, FastTestDataset.USER_MOHA_AC };

    for (TestUserInfo user : disallowedUsers)
    {
      TestGeoObjectInfo go = testData.newTestGeoObjectInfo("UpdateTest", FastTestDataset.PROVINCE_PRIVATE, FastTestDataset.SOURCE);
      go.apply();

      TestDataSet.runAsUser(user, (request) -> {

        try
        {
          client.createGeoObject(TEST_GO_PRIVATE.newGeoObject(client.getAdapter()).toJSON().toString(), TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE);

          Assert.fail();
        }
        catch (SmartExceptionDTO ex)
        {
          Assert.assertEquals(CreateGeoObjectPermissionException.CLASS, ex.getType());
        }
      });
    }
  }

  private void updateGO(TestGeoObjectInfo go, Date startDate, Date endDate)
  {
    go.setWkt(TestDataSet.WKT_POLYGON_2);
    go.setDisplayLabel("Some new value");

    GeoObject update = go.fetchGeoObject();
    go.populate(update);

    GeoObject returnedUpdate = client.updateGeoObject(update.toJSON().toString(), startDate, endDate);

    go.assertEquals(returnedUpdate);

    go.assertApplied();
  }

  @Test
  public void testUpdateGeoObject()
  {
    // Allowed Users
    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM };

    for (TestUserInfo user : allowedUsers)
    {
      TestGeoObjectInfo go = testData.newTestGeoObjectInfo("UpdateTest", FastTestDataset.COUNTRY, FastTestDataset.SOURCE);
      go.apply();

      TestDataSet.runAsUser(user, (request) -> {
        updateGO(go, TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE);
      });

      go.delete();
    }

    // Disallowed Users
    TestUserInfo[] disallowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_AC, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_MOHA_RM, FastTestDataset.USER_MOHA_RC, FastTestDataset.USER_MOHA_AC };

    for (TestUserInfo user : disallowedUsers)
    {
      TestGeoObjectInfo go = testData.newTestGeoObjectInfo("UpdateTest", FastTestDataset.COUNTRY, FastTestDataset.SOURCE);
      go.apply();

      TestDataSet.runAsUser(user, (request) -> {
        try
        {
          updateGO(go, TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE);

          Assert.fail();
        }
        catch (SmartExceptionDTO ex)
        {
          // expected
        }
      });
    }
  }

  @Test
  public void testUpdateGeoObjectNoDate()
  {
    TestGeoObjectInfo go = testData.newTestGeoObjectInfo("UpdateTest", FastTestDataset.COUNTRY, FastTestDataset.SOURCE);
    go.apply();

    TestDataSet.runAsUser(FastTestDataset.USER_CGOV_RA, (request) -> {
      updateGO(go, null, null);
    });

    go.delete();
  }

  @Test
  public void testUpdatePrivateGeoObject()
  {
    // Allowed Users
    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM_PRIVATE };

    for (TestUserInfo user : allowedUsers)
    {
      TestGeoObjectInfo go = testData.newTestGeoObjectInfo("UpdateTest", FastTestDataset.PROVINCE_PRIVATE, FastTestDataset.SOURCE);
      go.apply();

      TestDataSet.runAsUser(user, (request) -> {
        updateGO(go, TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE);
      });

      go.delete();
    }

    // Disallowed Users
    TestUserInfo[] disallowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RM, FastTestDataset.USER_CGOV_AC, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_MOHA_RM, FastTestDataset.USER_MOHA_RC, FastTestDataset.USER_MOHA_AC };

    for (TestUserInfo user : disallowedUsers)
    {
      TestGeoObjectInfo go = testData.newTestGeoObjectInfo("UpdateTest", FastTestDataset.PROVINCE_PRIVATE, FastTestDataset.SOURCE);
      go.apply();

      TestDataSet.runAsUser(user, (request) -> {
        try
        {
          updateGO(go, TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE);

          Assert.fail();
        }
        catch (SmartExceptionDTO ex)
        {
          // expected
        }
      });
    }
  }

  @Test
  public void testGetUIDS()
  {
    Set<String> ids = client.getUIDs(100);

    Assert.assertEquals(100, ids.size());

    assertIdIssued(ids);
  }

  @Request
  private void assertIdIssued(Set<String> ids)
  {
    for (String id : ids)
    {
      Assert.assertTrue(ServiceFactory.getIdService().isIssuedId(id));
    }
  }

  @Test
  public void testGetGeoObjectSuggestions()
  {
    JsonArray results = client.getGeoObjectSuggestions(FastTestDataset.PROV_CENTRAL.getDisplayLabel().substring(0, 3), FastTestDataset.PROVINCE.getCode(), FastTestDataset.CAMBODIA.getCode(), null, null, FastTestDataset.CAMBODIA.getGeoObjectType().getCode(), FastTestDataset.HIER_ADMIN.getCode());

    Assert.assertEquals(2, results.size());

    JsonObject result = results.get(0).getAsJsonObject();

    Assert.assertEquals(FastTestDataset.PROV_CENTRAL.getDisplayLabel(), result.get("name").getAsString());
    // Assert.assertEquals(testData.CAMBODIA.getOid(), result.getString("id"));
    // // This is commented out because the ids are different due to postgres +
    // orientdb inconsistencies
    Assert.assertEquals(FastTestDataset.PROV_CENTRAL.getCode(), result.get(GeoObject.CODE).getAsString());
  }

  @Test
  public void testGetGeoObjectSuggestionsOnDate()
  {
    JsonArray results = client.getGeoObjectSuggestions(FastTestDataset.PROV_CENTRAL.getDisplayLabel().substring(0, 3), FastTestDataset.PROVINCE.getCode(), FastTestDataset.CAMBODIA.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, FastTestDataset.CAMBODIA.getGeoObjectType().getCode(), FastTestDataset.HIER_ADMIN.getCode());

    Assert.assertEquals(2, results.size());

    JsonObject result = results.get(0).getAsJsonObject();

    Assert.assertEquals(FastTestDataset.PROV_CENTRAL.getDisplayLabel(), result.get("name").getAsString());
    // Assert.assertEquals(testData.CAMBODIA.getOid(), result.getString("id"));
    // // This is commented out because the ids are different due to postgres +
    // orientdb inconsistencies
    Assert.assertEquals(FastTestDataset.PROV_CENTRAL.getCode(), result.get(GeoObject.CODE).getAsString());
  }

  @Test
  public void testGetGeoObjectSuggestionsNoParent()
  {
    JsonArray results = client.getGeoObjectSuggestions(FastTestDataset.PROV_CENTRAL.getDisplayLabel().substring(0, 3), FastTestDataset.PROVINCE.getCode(), null, null, null, null, null);

    Assert.assertEquals(2, results.size());

    JsonObject result = results.get(0).getAsJsonObject();

    Assert.assertEquals(FastTestDataset.PROV_CENTRAL.getDisplayLabel(), result.get("name").getAsString());
    // Assert.assertEquals(testData.CAMBODIA.getOid(), result.getString("id"));
    // // This is commented out because the ids are different due to postgres +
    // orientdb inconsistencies
    Assert.assertEquals(FastTestDataset.PROV_CENTRAL.getCode(), result.get(GeoObject.CODE).getAsString());
  }

  @Test
  public void testGetGeoObjectSuggestionsNoParentOnDate()
  {
    JsonArray results = client.getGeoObjectSuggestions(FastTestDataset.PROV_CENTRAL.getDisplayLabel().substring(0, 3), FastTestDataset.PROVINCE.getCode(), null, TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, null, null);

    Assert.assertEquals(2, results.size());

    JsonObject result = results.get(0).getAsJsonObject();

    Assert.assertEquals(FastTestDataset.PROV_CENTRAL.getDisplayLabel(), result.get("name").getAsString());
    // Assert.assertEquals(testData.CAMBODIA.getOid(), result.getString("id"));
    // // This is commented out because the ids are different due to postgres +
    // orientdb inconsistencies
    Assert.assertEquals(FastTestDataset.PROV_CENTRAL.getCode(), result.get(GeoObject.CODE).getAsString());
  }

  /**
   * Test to make sure we can't just provide random ids, they actually have to
   * be issued by our id service
   */
  /*
   * @Test(expected = SmartExceptionDTO.class) public void
   * testUnissuedIdCreate() { // Create GeoObject geoObj =
   * client.newGeoObjectInstance(FastTestDataset.PROVINCE.getCode());
   * geoObj.setWKTGeometry(FastTestDataset.CAMBODIA.getWkt());
   * geoObj.setCode(FastTestDataset.CAMBODIA.getCode());
   * geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE,
   * FastTestDataset.CAMBODIA.getDisplayLabel());
   * geoObj.setUid(UUID.randomUUID().toString());
   * client.createGeoObject(geoObj.toJSON().toString(),
   * TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE); }
   */

  /**
   * Test to make sure we can't just provide random ids, they actually have to
   * be issued by our id service
   */
  @Test(expected = SmartExceptionDTO.class)
  public void testUnissuedIdUpdate()
  {
    // Update
    GeoObject waGeoObj = client.getGeoObject(FastTestDataset.CAMBODIA.getRegistryId(), FastTestDataset.CAMBODIA.getGeoObjectType().getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);
    waGeoObj.setWKTGeometry(FastTestDataset.CAMBODIA.getWkt());
    waGeoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, FastTestDataset.CAMBODIA.getDisplayLabel());
    waGeoObj.setUid(UUID.randomUUID().toString());
    client.updateGeoObject(waGeoObj.toJSON().toString(), TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE);
  }
}
