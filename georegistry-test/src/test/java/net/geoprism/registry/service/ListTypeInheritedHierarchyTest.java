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
package net.geoprism.registry.service;

import java.util.Date;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.database.DuplicateDataDatabaseException;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import net.geoprism.registry.ListType;
import net.geoprism.registry.ListTypeEntry;
import net.geoprism.registry.ListTypeQuery;
import net.geoprism.registry.ListTypeVersion;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.USATestData;

public class ListTypeInheritedHierarchyTest
{
  private static USATestData testData;

  @BeforeClass
  public static void setUpClass()
  {
    testData = USATestData.newTestData();
    testData.setUpMetadata();

    setUpInReq();
  }

  @Request
  private static void setUpInReq()
  {
    ServerGeoObjectType sGO = USATestData.DISTRICT.getServerObject();
    sGO.setInheritedHierarchy(USATestData.HIER_SCHOOL.getServerObject(), USATestData.HIER_ADMIN.getServerObject());
  }

  @AfterClass
  @Request
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
    cleanUpExtra();

    testData.setUpInstanceData();

    testData.logIn(USATestData.USER_NPS_RA);
  }

  @After
  public void tearDown()
  {
    testData.logOut();

    cleanUpExtra();

    testData.tearDownInstanceData();
  }

  @Request
  public void cleanUpExtra()
  {
    ListTypeQuery query = new ListTypeQuery(new QueryFactory());

    OIterator<? extends ListType> it = query.getIterator();

    try
    {
      while (it.hasNext())
      {
        it.next().delete();
      }
    }
    finally
    {
      it.close();
    }
  }

  @Test
  public void testPublishVersion()
  {
    TestDataSet.executeRequestAsUser(USATestData.USER_ADMIN, () -> {

      JsonObject json = ListTypeTest.getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_SCHOOL, USATestData.SCHOOL_ZONE, USATestData.COUNTRY, USATestData.STATE, USATestData.DISTRICT);

      ListType test = ListType.apply(json);

      try
      {
        ListTypeEntry entry = test.getOrCreateEntry(new Date(), null);
        ListTypeVersion version = entry.getWorking();

        try
        {
          MdBusinessDAOIF mdTable = MdBusinessDAO.get(version.getMdBusinessOid());

          Assert.assertNotNull(mdTable);

          version.publish();
        }
        finally
        {
          entry.delete();
        }
      }
      finally
      {
        test.delete();
      }
    });
  }

  @Test
  @Request
  public void testMarkAsInvalidByInheritedParent()
  {
    JsonObject json = ListTypeTest.getJson(USATestData.ORG_NPS.getServerObject(), USATestData.HIER_SCHOOL, USATestData.SCHOOL_ZONE, USATestData.DISTRICT, USATestData.STATE);

    ListType masterlist = ListType.apply(json);

    try
    {
      masterlist.markAsInvalid(USATestData.HIER_ADMIN.getServerObject(), USATestData.STATE.getServerObject());

      Assert.assertFalse(masterlist.isValid());
    }
    catch (DuplicateDataDatabaseException e)
    {
      masterlist.delete();
    }
  }

}
