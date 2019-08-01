/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.io;

import java.io.IOException;
import java.util.Map;

import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.dataaccess.ValueObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.SessionFacade;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.LocatedIn;

import junit.framework.Assert;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.test.USATestData;

public class GeoObjectUtilTest
{
  private static USATestData     testData;

  private static ClientRequestIF adminCR;

  @BeforeClass
  public static void setUp()
  {
    testData = USATestData.newTestData(GeometryType.POLYGON, true);

    adminCR = testData.adminClientRequest;

    reload();
  }

  @Request
  public static void reload()
  {
    /*
     * Reload permissions for the new attributes
     */
    SessionFacade.getSessionForRequest(adminCR.getSessionId()).reloadPermissions();
  }

  @AfterClass
  public static void tearDown() throws IOException
  {
    testData.cleanUp();
  }

  @Test
  @Request
  public void testGetAncestorMapForTreeType()
  {
    ServerGeoObjectType type = testData.AREA.getGeoObjectType(GeometryType.POLYGON);
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    GeoObject object = ServiceFactory.getUtilities().getGeoObjectByCode(testData.CO_A_ONE.getCode(), type.getCode());

    Map<String, ValueObject> map = GeoObjectUtil.getAncestorMap(object, hierarchyType);

    Assert.assertEquals(5, map.size());

    // Validate the county values
    Assert.assertTrue(map.containsKey(testData.COUNTY.getCode()));

    ValueObject vObject = map.get(testData.COUNTY.getCode());

    Assert.assertEquals(testData.CO_C_ONE.getCode(), vObject.getValue(GeoEntity.GEOID));
    Assert.assertEquals(testData.CO_C_ONE.getDisplayLabel(), vObject.getValue(GeoEntity.DISPLAYLABEL));

    // Validate the state values
    Assert.assertTrue(map.containsKey(testData.STATE.getCode()));

    vObject = map.get(testData.STATE.getCode());

    Assert.assertEquals(testData.COLORADO.getCode(), vObject.getValue(GeoEntity.GEOID));
    Assert.assertEquals(testData.COLORADO.getDisplayLabel(), vObject.getValue(GeoEntity.DISPLAYLABEL));

    // Validate the country values
    Assert.assertTrue(map.containsKey(testData.COUNTRY.getCode()));

    vObject = map.get(testData.COUNTRY.getCode());

    Assert.assertEquals(testData.USA.getCode(), vObject.getValue(GeoEntity.GEOID));
    Assert.assertEquals(testData.USA.getDisplayLabel(), vObject.getValue(GeoEntity.DISPLAYLABEL));
  }

  @Request
  @Test
  public void testGetAncestorMapForTreeLeaf()
  {
    ServerGeoObjectType type = testData.DISTRICT.getGeoObjectType(GeometryType.POLYGON);
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(LocatedIn.class.getSimpleName());

    GeoObject object = ServiceFactory.getUtilities().getGeoObjectByCode(testData.CO_D_ONE.getCode(), type.getCode());

    Map<String, ValueObject> map = GeoObjectUtil.getAncestorMap(object, hierarchyType);

    // Validate the state values
    Assert.assertTrue(map.containsKey(testData.STATE.getCode()));

    ValueObject vObject = map.get(testData.STATE.getCode());

    Assert.assertEquals(testData.COLORADO.getCode(), vObject.getValue(GeoEntity.GEOID));
    Assert.assertEquals(testData.COLORADO.getDisplayLabel(), vObject.getValue(GeoEntity.DISPLAYLABEL));

    // Validate the country values
    Assert.assertTrue(map.containsKey(testData.COUNTRY.getCode()));

    vObject = map.get(testData.COUNTRY.getCode());

    Assert.assertEquals(testData.USA.getCode(), vObject.getValue(GeoEntity.GEOID));
    Assert.assertEquals(testData.USA.getDisplayLabel(), vObject.getValue(GeoEntity.DISPLAYLABEL));

  }
}
