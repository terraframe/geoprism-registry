/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.io;

import java.io.IOException;
import java.util.Map;

import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;
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
    GeoObjectType type = testData.AREA.getGeoObjectType(GeometryType.POLYGON);
    HierarchyType hierarchy = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(LocatedIn.class.getSimpleName()).get();

    GeoObject object = ServiceFactory.getUtilities().getGeoObjectByCode(testData.CO_A_ONE.getCode(), type.getCode());

    Map<String, ValueObject> map = GeoObjectUtil.getAncestorMap(object, hierarchy);

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
    GeoObjectType type = testData.DISTRICT.getGeoObjectType(GeometryType.POLYGON);
    HierarchyType hierarchy = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(LocatedIn.class.getSimpleName()).get();

    GeoObject object = ServiceFactory.getUtilities().getGeoObjectByCode(testData.CO_D_ONE.getCode(), type.getCode());

    Map<String, ValueObject> map = GeoObjectUtil.getAncestorMap(object, hierarchy);

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
