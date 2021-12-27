/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.hierarchy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.runwaysdk.session.Request;

import junit.framework.Assert;
import net.geoprism.registry.conversion.VertexGeoObjectStrategy;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerParentTreeNode;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.USATestData;

public class HierarchyChangeTest
{
  private static USATestData testData;

  @Before
  public void setUp()
  {
    testData = USATestData.newTestData();
    testData.setUpMetadata();

    testData.setUpInstanceData();

    testData.logIn(USATestData.USER_NPS_RA);
  }

  @After
  public void tearDown()
  {
    if (testData != null)
    {
      testData.logOut();

      testData.tearDownInstanceData();

      testData.tearDownMetadata();
    }
  }

  @Test
  @Request
  public void testRemoveFromHierarchyWithData()
  {
    String sessionId = testData.clientSession.getSessionId();

    String hierarchyCode = USATestData.HIER_ADMIN.getCode();
    String countryTypeCode = USATestData.COUNTRY.getCode();
    String stateTypeCode = USATestData.STATE.getCode();

    String cd1TypeCode = USATestData.CO_D_ONE.getCode();

    ServerGeoObjectIF geoobject = new VertexGeoObjectStrategy(USATestData.DISTRICT.getServerObject()).getGeoObjectByCode(cd1TypeCode);
    ServerParentTreeNode parents = geoobject.getParentGeoObjects(null, false, TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assert.assertEquals(1, parents.getParents().size());

    ServiceFactory.getHierarchyService().removeFromHierarchy(sessionId, hierarchyCode, countryTypeCode, stateTypeCode, true);

    ServerGeoObjectIF test = new VertexGeoObjectStrategy(USATestData.DISTRICT.getServerObject()).getGeoObjectByCode(cd1TypeCode);
    ServerParentTreeNode tParents = test.getParentGeoObjects(null, false, TestDataSet.DEFAULT_OVER_TIME_DATE);

    Assert.assertEquals(0, tParents.getParents().size());
  }
}
