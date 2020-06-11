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
package net.geoprism.registry.io;

import java.util.Map;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.session.Request;

import junit.framework.Assert;
import net.geoprism.registry.model.LocationInfo;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.test.USATestData;

public class GeoObjectUtilTest
{
  private static USATestData     testData;

  @BeforeClass
  public static void setUpClass()
  {
    testData = USATestData.newTestDataForClass();
    testData.setUpMetadata();
  }
  
  @AfterClass
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
    if (testData != null)
    {
      testData.setUpInstanceData();
    }
  }

  @After
  public void tearDown()
  {
    if (testData != null)
    {
      testData.tearDownInstanceData();
    }
  }

  @Test
  @Request
  public void testGetAncestorMapForTreeType()
  {
    ServerGeoObjectType type = testData.AREA.getServerObject();
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(testData.HIER_ADMIN.getCode());

//    GeoObject object = ServiceFactory.getUtilities().getGeoObjectByCode(testData.CO_A_ONE.getCode(), type.getCode());
//    GeoObject object = ServiceFactory.getRegistryService().getGeoObjectByCode(Session.getCurrentSession().getOid(), testData.CO_A_ONE.getCode(), type.getCode());
    GeoObject object = testData.adapter.getGeoObjectByCode(testData.CO_A_ONE.getCode(), type.getCode());
    
    Map<String, LocationInfo> map = GeoObjectUtil.getAncestorMap(object, hierarchyType);

    Assert.assertEquals(5, map.size());

    // Validate the county values
    Assert.assertTrue(map.containsKey(testData.COUNTY.getCode()));

    LocationInfo vObject = map.get(testData.COUNTY.getCode());

    Assert.assertEquals(testData.CO_C_ONE.getCode(), vObject.getCode());
    Assert.assertEquals(testData.CO_C_ONE.getDisplayLabel(), vObject.getLabel());

    // Validate the state values
    Assert.assertTrue(map.containsKey(testData.STATE.getCode()));

    vObject = map.get(testData.STATE.getCode());

    Assert.assertEquals(testData.COLORADO.getCode(), vObject.getCode());
    Assert.assertEquals(testData.COLORADO.getDisplayLabel(), vObject.getLabel());

    // Validate the country values
    Assert.assertTrue(map.containsKey(testData.COUNTRY.getCode()));

    vObject = map.get(testData.COUNTRY.getCode());

    Assert.assertEquals(testData.USA.getCode(), vObject.getCode());
    Assert.assertEquals(testData.USA.getDisplayLabel(), vObject.getLabel());
  }
}
