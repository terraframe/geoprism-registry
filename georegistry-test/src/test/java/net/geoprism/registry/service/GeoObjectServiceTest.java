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

import java.util.Set;
import java.util.UUID;

import org.commongeoregistry.adapter.GeoObjectTypeNotFoundException;
import org.commongeoregistry.adapter.constants.DefaultTerms;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.geotools.geometry.jts.GeometryBuilder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.business.SmartExceptionDTO;
import com.runwaysdk.session.Request;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import net.geoprism.registry.GeometryTypeException;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.roles.CreateGeoObjectPermissionException;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.TestRegistryAdapterClient;
import net.geoprism.registry.test.TestUserInfo;

public class GeoObjectServiceTest
{
  protected static FastTestDataset      testData;

  public static final TestGeoObjectInfo TEST_GO = new TestGeoObjectInfo("GOSERV_TEST_GO", FastTestDataset.COUNTRY);
  
  public static final TestGeoObjectInfo TEST_GO_PRIVATE = new TestGeoObjectInfo("GOSERV_TEST_GO_PRIVATE", FastTestDataset.PROVINCE_PRIVATE);
  
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
  public void testGetGeoObject()
  {
    // Allowed Users
    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_CGOV_RM, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC };

    for (TestUserInfo user : allowedUsers)
    {
      FastTestDataset.runAsUser(user, (request, adapter) -> {
        GeoObject geoObj = adapter.getGeoObject(FastTestDataset.CAMBODIA.getRegistryId(), FastTestDataset.CAMBODIA.getGeoObjectType().getCode());

        FastTestDataset.CAMBODIA.assertEquals(geoObj);

        Assert.assertEquals(DefaultTerms.GeoObjectStatusTerm.ACTIVE.code, geoObj.getStatus().getCode());
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
      FastTestDataset.runAsUser(user, (request, adapter) -> {
        GeoObject geoObj = adapter.getGeoObject(FastTestDataset.PROV_CENTRAL_PRIVATE.getRegistryId(), FastTestDataset.PROV_CENTRAL_PRIVATE.getGeoObjectType().getCode());

        
        FastTestDataset.PROV_CENTRAL_PRIVATE.assertEquals(geoObj);

        Assert.assertEquals(DefaultTerms.GeoObjectStatusTerm.ACTIVE.code, geoObj.getStatus().getCode());
      });
    }

    // Disallowed Users
    TestUserInfo[] disllowedUsers = new TestUserInfo[] { FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_MOHA_RM, FastTestDataset.USER_MOHA_RC, FastTestDataset.USER_MOHA_AC, FastTestDataset.USER_CGOV_RM, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC };
  
    for (TestUserInfo user : disllowedUsers)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request, adapter) -> {
          adapter.getGeoObject(FastTestDataset.PROV_CENTRAL_PRIVATE.getRegistryId(), FastTestDataset.PROV_CENTRAL_PRIVATE.getGeoObjectType().getCode());
        
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
      FastTestDataset.runAsUser(user, (request, adapter) -> {
        GeoObject geoObj = adapter.getGeoObjectByCode(FastTestDataset.CAMBODIA.getCode(), FastTestDataset.CAMBODIA.getGeoObjectType().getCode());

        Assert.assertEquals(geoObj.toJSON().toString(), GeoObject.fromJSON(adapter, geoObj.toJSON().toString()).toJSON().toString());
        Assert.assertEquals(DefaultTerms.GeoObjectStatusTerm.ACTIVE.code, geoObj.getStatus().getCode());
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
    
    serverGo.apply(false);
    
    Assert.assertEquals(TEST_GO.getCode(), TEST_GO.getServerObject().getCode());
  }
  
  @Test
  public void testGetPrivateGeoObjectByCode()
  {
    // Allowed Users
    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM_PRIVATE, FastTestDataset.USER_CGOV_RC_PRIVATE, FastTestDataset.USER_CGOV_AC_PRIVATE };
    for (TestUserInfo user : allowedUsers)
    {
      FastTestDataset.runAsUser(user, (request, adapter) -> {
        GeoObject geoObj = adapter.getGeoObjectByCode(FastTestDataset.PROV_CENTRAL_PRIVATE.getCode(), FastTestDataset.PROV_CENTRAL_PRIVATE.getGeoObjectType().getCode());

        Assert.assertEquals(geoObj.toJSON().toString(), GeoObject.fromJSON(adapter, geoObj.toJSON().toString()).toJSON().toString());
        Assert.assertEquals(DefaultTerms.GeoObjectStatusTerm.ACTIVE.code, geoObj.getStatus().getCode());
      });
    }

    // Disallowed Users
    TestUserInfo[] disllowedUsers = new TestUserInfo[] { FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_MOHA_RM, FastTestDataset.USER_MOHA_RC, FastTestDataset.USER_MOHA_AC, FastTestDataset.USER_CGOV_RM, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC };
    for (TestUserInfo user : disllowedUsers)
    {
      try
      {
        FastTestDataset.runAsUser(user, (request, adapter) -> {
          adapter.getGeoObjectByCode(FastTestDataset.PROV_CENTRAL_PRIVATE.getCode(), FastTestDataset.PROV_CENTRAL_PRIVATE.getGeoObjectType().getCode());
      
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

    GeoObject geoObj = testData.adapter.newGeoObjectInstance(FastTestDataset.PROVINCE.getCode());
    geoObj.setGeometry(point);
    geoObj.setCode(FastTestDataset.CAMBODIA.getCode());
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, FastTestDataset.CAMBODIA.getDisplayLabel());

    try
    {
      testData.adapter.createGeoObject(geoObj.toJSON().toString());

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
      TestDataSet.runAsUser(user, (request, adapter) -> {
        TestDataSet.populateAdapterIds(user, adapter);

        GeoObject returned = adapter.createGeoObject(TEST_GO.newGeoObject(adapter).toJSON().toString());

        TEST_GO.assertEquals(returned);

        Assert.assertEquals(DefaultTerms.GeoObjectStatusTerm.PENDING.code, returned.getStatus().getCode());

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
          adapter.createGeoObject(TEST_GO.newGeoObject(ServiceFactory.getAdapter()).toJSON().toString());

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
  public void testCreatePrivateGeoObject()
  {
    // Allowed Users
    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM_PRIVATE };

    for (TestUserInfo user : allowedUsers)
    {
      TestDataSet.runAsUser(user, (request, adapter) -> {
        TestDataSet.populateAdapterIds(user, adapter);

        GeoObject returned = adapter.createGeoObject(TEST_GO_PRIVATE.newGeoObject(adapter).toJSON().toString());

        TEST_GO_PRIVATE.assertEquals(returned);

        Assert.assertEquals(DefaultTerms.GeoObjectStatusTerm.PENDING.code, returned.getStatus().getCode());

        TEST_GO_PRIVATE.assertApplied();
        TEST_GO_PRIVATE.delete();
      });
    }

    // Disallowed Users
    TestUserInfo[] disallowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RM, FastTestDataset.USER_CGOV_AC, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_MOHA_RM, FastTestDataset.USER_MOHA_RC, FastTestDataset.USER_MOHA_AC };

    for (TestUserInfo user : disallowedUsers)
    {
      TestGeoObjectInfo go = testData.newTestGeoObjectInfo("UpdateTest", FastTestDataset.PROVINCE_PRIVATE);
      go.apply();

      TestDataSet.runAsUser(user, (request, adapter) -> {
        
        if (TestDataSet.populateAdapterIds(user, adapter))
        {
          try
          {
            adapter.newGeoObjectInstance(FastTestDataset.PROVINCE_PRIVATE.getCode());
            
            Assert.fail("Expected an error");
          }
          catch (GeoObjectTypeNotFoundException ex)
          {
            // Expected
          }
        }
        
        try
        {
          adapter.createGeoObject(TEST_GO_PRIVATE.newGeoObject(ServiceFactory.getAdapter()).toJSON().toString());

          Assert.fail();
        }
        catch (SmartExceptionDTO ex)
        {
          Assert.assertEquals(CreateGeoObjectPermissionException.CLASS, ex.getType());
        }
      });
    }
  }

  private void updateGO(TestRegistryAdapterClient adapter, TestGeoObjectInfo go)
  {
    go.setWkt(TestDataSet.WKT_POLYGON_2);
    go.setDisplayLabel("Some new value");

    GeoObject update = go.fetchGeoObject();
    go.populate(update);

    GeoObject returnedUpdate = adapter.updateGeoObject(update.toJSON().toString());

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
      TestGeoObjectInfo go = testData.newTestGeoObjectInfo("UpdateTest", FastTestDataset.COUNTRY);
      go.apply();

      TestDataSet.runAsUser(user, (request, adapter) -> {
        updateGO(adapter, go);
      });

      go.delete();
    }

    // Disallowed Users
    TestUserInfo[] disallowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_AC, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_MOHA_RM, FastTestDataset.USER_MOHA_RC, FastTestDataset.USER_MOHA_AC };

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
  
  @Test
  public void testUpdatePrivateGeoObject()
  {
    // Allowed Users
    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM_PRIVATE };

    for (TestUserInfo user : allowedUsers)
    {
      TestGeoObjectInfo go = testData.newTestGeoObjectInfo("UpdateTest", FastTestDataset.PROVINCE_PRIVATE);
      go.apply();

      TestDataSet.runAsUser(user, (request, adapter) -> {
        updateGO(adapter, go);
      });

      go.delete();
    }

    // Disallowed Users
    TestUserInfo[] disallowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RM, FastTestDataset.USER_CGOV_AC, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_MOHA_RA, FastTestDataset.USER_MOHA_RM, FastTestDataset.USER_MOHA_RC, FastTestDataset.USER_MOHA_AC };

    for (TestUserInfo user : disallowedUsers)
    {
      TestGeoObjectInfo go = testData.newTestGeoObjectInfo("UpdateTest", FastTestDataset.PROVINCE_PRIVATE);
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

  @Test
  public void testGetUIDS()
  {
    Set<String> ids = testData.adapter.getGeoObjectUids(100);

    Assert.assertEquals(100, ids.size());

    assertIdIssued(ids);
  }

  @Request
  private void assertIdIssued(Set<String> ids)
  {
    for (String id : ids)
    {
      Assert.assertTrue(RegistryIdService.getInstance().isIssuedId(id));
    }
  }

  @Test
  public void testGetGeoObjectSuggestions()
  {
    JsonArray results = testData.adapter.getGeoObjectSuggestions(FastTestDataset.PROV_CENTRAL.getDisplayLabel().substring(0, 3), FastTestDataset.PROVINCE.getCode(), FastTestDataset.CAMBODIA.getCode(), FastTestDataset.CAMBODIA.getGeoObjectType().getCode(), FastTestDataset.HIER_ADMIN.getCode(), null);

    Assert.assertEquals(1, results.size());

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
    JsonArray results = testData.adapter.getGeoObjectSuggestions(FastTestDataset.PROV_CENTRAL.getDisplayLabel().substring(0, 3), FastTestDataset.PROVINCE.getCode(), null, null, null, null);

    Assert.assertEquals(1, results.size());

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
  @Test(expected = SmartExceptionDTO.class)
  public void testUnissuedIdCreate()
  {
    // Create
    GeoObject geoObj = testData.adapter.newGeoObjectInstance(FastTestDataset.PROVINCE.getCode());
    geoObj.setWKTGeometry(FastTestDataset.CAMBODIA.getWkt());
    geoObj.setCode(FastTestDataset.CAMBODIA.getCode());
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, FastTestDataset.CAMBODIA.getDisplayLabel());
    geoObj.setUid(UUID.randomUUID().toString());
    testData.adapter.createGeoObject(geoObj.toJSON().toString());
  }

  /**
   * Test to make sure we can't just provide random ids, they actually have to
   * be issued by our id service
   */
  @Test(expected = SmartExceptionDTO.class)
  public void testUnissuedIdUpdate()
  {
    // Update
    GeoObject waGeoObj = testData.adapter.getGeoObject(FastTestDataset.CAMBODIA.getRegistryId(), FastTestDataset.CAMBODIA.getGeoObjectType().getCode());
    waGeoObj.setWKTGeometry(FastTestDataset.CAMBODIA.getWkt());
    waGeoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, FastTestDataset.CAMBODIA.getDisplayLabel());
    waGeoObj.setUid(UUID.randomUUID().toString());
    testData.adapter.updateGeoObject(waGeoObj.toJSON().toString());
  }
}
