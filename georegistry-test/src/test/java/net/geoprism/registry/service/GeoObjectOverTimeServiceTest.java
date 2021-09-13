/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
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

import com.runwaysdk.business.SmartExceptionDTO;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import net.geoprism.registry.GeometryTypeException;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.TestRegistryAdapterClient;
import net.geoprism.registry.test.TestUserInfo;

public class GeoObjectOverTimeServiceTest
{
  protected static FastTestDataset      testData;

  public static final TestGeoObjectInfo TEST_GO = new TestGeoObjectInfo("GOSERV_TEST_GO", FastTestDataset.COUNTRY);

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

    TestDataSet.populateAdapterIds(null, testData.adapter);
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
      FastTestDataset.runAsUser(user, (request, adapter) -> {
        GeoObjectOverTime geoObj = adapter.getGeoObjectOverTime(FastTestDataset.CAMBODIA.getRegistryId(), FastTestDataset.CAMBODIA.getGeoObjectType().getCode());

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
      FastTestDataset.runAsUser(user, (request, adapter) -> {
        GeoObjectOverTime geoObj = adapter.getGeoObjectOverTimeByCode(FastTestDataset.CAMBODIA.getCode(), FastTestDataset.CAMBODIA.getGeoObjectType().getCode());

        Assert.assertEquals(geoObj.toJSON().toString(), GeoObjectOverTime.fromJSON(adapter, geoObj.toJSON().toString()).toJSON().toString());
        Assert.assertEquals(true, geoObj.getExists(TestDataSet.DEFAULT_OVER_TIME_DATE));
      });
    }
  }

  @Test
  public void testCreateGeoObjectOverTimeBadGeometry()
  {
    GeometryBuilder builder = new GeometryBuilder(new GeometryFactory());
    Point point = builder.point(48.44, -123.37);

    GeoObjectOverTime geoObj = TEST_GO.newGeoObjectOverTime(testData.adapter);

    geoObj.setGeometry(point, TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_OVER_TIME_DATE);

    try
    {
      testData.adapter.createGeoObjectOverTime(geoObj.toJSON().toString());

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
      TestDataSet.runAsUser(user, (request, adapter) -> {
        TestDataSet.populateAdapterIds(user, adapter);

        TEST_GO.assertEquals(adapter.createGeoObjectOverTime(TEST_GO.newGeoObjectOverTime(adapter).toJSON().toString()));
        TEST_GO.assertApplied();
        TEST_GO.delete();
      });
    }

    TestUserInfo[] disallowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC, FastTestDataset.USER_MOHA_RA };

    for (TestUserInfo user : disallowedUsers)
    {
      TestDataSet.runAsUser(user, (request, adapter) -> {
        TestDataSet.populateAdapterIds(user, adapter);

        try
        {
          adapter.createGeoObjectOverTime(TEST_GO.newGeoObjectOverTime(ServiceFactory.getAdapter()).toJSON().toString());

          Assert.fail();
        }
        catch (SmartExceptionDTO ex)
        {
          // expected
        }
      });
    }
  }

  private void updateGO(TestRegistryAdapterClient adapter, TestGeoObjectInfo go)
  {
    go.setWkt(TestDataSet.WKT_POLYGON_2);
    go.setDisplayLabel("Some new value");

    GeoObjectOverTime update = go.fetchGeoObjectOverTime();
    go.populate(update);

    GeoObjectOverTime returnedUpdate = adapter.updateGeoObjectOverTime(update.toJSON().toString());

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

      TestDataSet.runAsUser(user, (request, adapter) -> {
        updateGO(adapter, go);
      });

      go.delete();
    }

    // Disallowed Users
    TestUserInfo[] disallowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_AC, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_MOHA_RA };

    for (TestUserInfo user : disallowedUsers)
    {
      TestGeoObjectInfo go = testData.newTestGeoObjectInfo("UpdateTest", FastTestDataset.COUNTRY);
      go.apply();

      TestDataSet.runAsUser(user, (request, adapter) -> {
        try
        {
          updateGO(adapter, go);

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
