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
package net.geoprism.registry;

import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.session.Request;

import net.geoprism.dhis2.dhis2adapter.HTTPConnector;
import net.geoprism.registry.etl.export.GeoObjectExportFormat;
import net.geoprism.registry.etl.export.GeoObjectJsonExporter;
import net.geoprism.registry.graph.DHIS2ExternalSystem;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.test.USATestData;

public class JsonSerializationTest
{
  protected static USATestData testData;

  protected ExternalSystem     system;

//  private DHIS2ServiceIF          dhis2;

  private static final String  USERNAME = "admin";

  private static final String  PASSWORD = "district";

  private static final String  URL      = "https://play.dhis2.org/2.31.9";

  private static final String  VERSION  = "31";

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

    system = getDhis2ExternalSystem();

    HTTPConnector connector = new HTTPConnector();
    connector.setCredentials(USERNAME, PASSWORD);
    connector.setServerUrl(URL);
//    dhis2 = new DHIS2Facade(connector, VERSION);
  }

  @After
  public void tearDown()
  {
    if (testData != null)
    {
      testData.tearDownInstanceData();
    }

    deleteExternalSystems();
  }

  @Request
  private ExternalSystem getDhis2ExternalSystem()
  {
    DHIS2ExternalSystem system = new DHIS2ExternalSystem();
    system.setId("JsonSerializationTestDhis2");
    system.setOrganization(testData.ORG_NPS.getServerObject());
    system.getEmbeddedComponent(ExternalSystem.LABEL).setValue("defaultLocale", "Test");
    system.getEmbeddedComponent(ExternalSystem.DESCRIPTION).setValue("defaultLocale", "Test");
    system.setUsername(USERNAME);
    system.setPassword(PASSWORD);
    system.setUrl(URL);
    system.setVersion(VERSION);
    system.apply();

    return system;
  }

  @Request
  private void deleteExternalSystems()
  {
    final String systemId = "JsonSerializationTestDhis2";

    try
    {
      final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(ExternalSystem.CLASS);
      MdAttributeDAOIF attribute = mdVertex.definesAttribute(ExternalSystem.ID);

      StringBuilder builder = new StringBuilder();
      builder.append("SELECT FROM " + mdVertex.getDBClassName());

      builder.append(" WHERE " + attribute.getColumnName() + " = :id");

      final GraphQuery<ExternalSystem> query = new GraphQuery<ExternalSystem>(builder.toString());

      query.setParameter("id", systemId);

      List<ExternalSystem> list = query.getResults();

      for (ExternalSystem es : list)
      {
        es.delete();
      }
    }
    catch (net.geoprism.registry.DataNotFoundException ex)
    {
      // Do nothing
    }
  }

  // @Test
  // @Request
  // public void testDhis2Serialize() throws IOException
  // {
  // ServerGeoObjectType got = testData.DISTRICT.getServerObject();
  // ServerHierarchyType ht = testData.HIER_ADMIN.getServerObject();
  //
  // GeoObjectJsonExporter exporter = new GeoObjectJsonExporter(got, ht, null,
  // true, GeoObjectExportFormat.JSON_DHIS2, system, -1, -1);
  // exporter.setDHIS2Facade(this.dhis2);
  // System.out.println(IOUtils.toString(exporter.export()));
  // }

  @Test
  @Request
  public void testRevealSerialize() throws IOException
  {
    ServerGeoObjectType got = testData.DISTRICT.getServerObject();
    ServerHierarchyType ht = testData.HIER_ADMIN.getServerObject();

    GeoObjectJsonExporter exporter = new GeoObjectJsonExporter(got, ht, null, true, GeoObjectExportFormat.JSON_REVEAL, system, -1, -1);
    System.out.println(exporter.export().toString());
  }
}
