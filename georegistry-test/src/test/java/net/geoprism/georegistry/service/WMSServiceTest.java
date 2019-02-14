package net.geoprism.georegistry.service;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.session.Request;

import net.geoprism.registry.test.USATestData;

public class WMSServiceTest
{
  protected USATestData     testData;

  protected ClientRequestIF adminCR;

  @Before
  public void setUp()
  {
    this.testData = USATestData.newTestData(GeometryType.POLYGON, true);

    this.adminCR = testData.adminClientRequest;
  }

  @After
  public void tearDown() throws IOException
  {
    testData.cleanUp();
  }

  @Test
  @Request
  public void testCreateDeleteDatabaseViewOnTreeType() throws SQLException
  {
    GeoObjectType type = this.testData.STATE.getGeoObjectType(GeometryType.POLYGON);

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
    GeoObjectType type = this.testData.DISTRICT.getGeoObjectType(GeometryType.POLYGON);

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
