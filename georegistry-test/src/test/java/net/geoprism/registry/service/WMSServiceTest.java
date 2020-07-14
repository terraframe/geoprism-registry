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

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.session.Request;

import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.test.USATestData;

public class WMSServiceTest
{
  protected static USATestData     testData;

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
  public void testCreateDeleteDatabaseViewOnTreeType() throws SQLException
  {
    ServerGeoObjectType type = this.testData.STATE.getServerObject();

    WMSService service = new WMSService();

    String viewName = service.createDatabaseView(type, true);

    try
    {
      ResultSet results = Database.query("SELECT * FROM " + viewName);

      try
      {
        Assert.assertTrue(results.next());
      }
      finally
      {
        results.close();
      }
    }
    finally
    {
      service.deleteDatabaseView(type);
    }
  }

  @Test
  @Request
  public void testCreateDeleteDatabaseViewOnLeafType() throws SQLException
  {
    ServerGeoObjectType type = this.testData.DISTRICT.getServerObject();

    WMSService service = new WMSService();

    String viewName = service.createDatabaseView(type, true);

    try
    {
      ResultSet results = Database.query("SELECT * FROM " + viewName);

      try
      {
        Assert.assertTrue(results.next());
      }
      finally
      {
        results.close();
      }
    }
    finally
    {
      service.deleteDatabaseView(type);
    }
  }

}
