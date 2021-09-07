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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.session.Request;

import net.geoprism.registry.geoobject.ServerGeoObjectService;
import net.geoprism.registry.model.LocationInfo;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.test.USATestData;

public class GeoObjectUtilTest
{
  private static USATestData testData;

  @BeforeClass
  public static void setUpClass()
  {
    testData = USATestData.newTestData();
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
    testData.setUpInstanceData();

    testData.logIn();
  }

  @After
  public void tearDown()
  {
    testData.logOut();

    testData.tearDownInstanceData();
  }

  @Test
  @Request
  public void testGetAncestorMapForTreeType()
  {
    ServerGeoObjectType type = USATestData.AREA.getServerObject();
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode());

    ServerGeoObjectIF object = new ServerGeoObjectService().getGeoObjectByCode(USATestData.CO_A_ONE.getCode(), type);
    
    List<GeoObjectType> dtoAncestors = type.getTypeAncestors(hierarchyType, true);
    List<ServerGeoObjectType> ancestors = new LinkedList<ServerGeoObjectType>();
    for (GeoObjectType ancestor : dtoAncestors)
    {
      ancestors.add(ServerGeoObjectType.get(ancestor));
    }

    Map<String, LocationInfo> map = object.getAncestorMap(hierarchyType, ancestors);

    Assert.assertEquals(3, map.size());

    // Validate the county values
    Assert.assertTrue(map.containsKey(USATestData.COUNTY.getCode()));

    LocationInfo vObject = map.get(USATestData.COUNTY.getCode());

    Assert.assertEquals(USATestData.CO_C_ONE.getCode(), vObject.getCode());
    Assert.assertEquals(USATestData.CO_C_ONE.getDisplayLabel(), vObject.getLabel());

    // Validate the state values
    Assert.assertTrue(map.containsKey(USATestData.STATE.getCode()));

    vObject = map.get(USATestData.STATE.getCode());

    Assert.assertEquals(USATestData.COLORADO.getCode(), vObject.getCode());
    Assert.assertEquals(USATestData.COLORADO.getDisplayLabel(), vObject.getLabel());

    // Validate the country values
    Assert.assertTrue(map.containsKey(USATestData.COUNTRY.getCode()));

    vObject = map.get(USATestData.COUNTRY.getCode());

    Assert.assertEquals(USATestData.USA.getCode(), vObject.getCode());
    Assert.assertEquals(USATestData.USA.getDisplayLabel(), vObject.getLabel());
  }
}
