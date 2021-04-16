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

import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.build.domain.SearchTablePatch;
import com.runwaysdk.session.Request;

import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.test.USATestData;

public class SearchServiceTest
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

    testData.logIn(USATestData.USER_NPS_RA);
  }

  @After
  public void tearDown()
  {
    testData.logOut();

    testData.tearDownInstanceData();
  }

  @Test
  @Request
  public void testSearchTable()
  {
    SearchService service = new SearchService();
    service.clear();
    service.deleteSearchTable();
    service.createSearchTable();

    new SearchTablePatch().createRecords(service);

    Date date = USATestData.DEFAULT_OVER_TIME_DATE;

    List<ServerGeoObjectIF> results = service.search(USATestData.CO_D_ONE.getDisplayLabel(), date, 10L);

    Assert.assertEquals(1, results.size());

    ServerGeoObjectIF result = results.get(0);

    Assert.assertEquals(result.getCode(), USATestData.CO_D_ONE.getCode());

    Assert.assertEquals(3, service.search(USATestData.TEST_DATA_KEY + "ColoradoDistrict", date, 10L).size());
    Assert.assertEquals(1, service.search(USATestData.TEST_DATA_KEY, date, 1L).size());
  }
}
