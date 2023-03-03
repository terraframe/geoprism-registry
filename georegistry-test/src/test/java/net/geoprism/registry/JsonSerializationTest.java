/**
 *
 */
package net.geoprism.registry;

import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.json.RunwayJsonAdapters;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import net.geoprism.account.OauthServer;
import net.geoprism.account.OauthServerQuery;
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
    deleteOauthServers();
  }

  @Request
  private ExternalSystem getDhis2ExternalSystem()
  {
    DHIS2ExternalSystem system = new DHIS2ExternalSystem();
    system.setId("JsonSerializationTestDhis2");
    system.setOrganization(USATestData.ORG_NPS.getServerObject());
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
  private OauthServer getOauthServer()
  {
    OauthServer oauth = new OauthServer();
    oauth.setSecretKey("1e6db50c-0fee-11e5-98d0-3c15c2c6caf6");
    oauth.setClientId("geoprism");
    oauth.setProfileLocation("http://test-profile.example.net/profile");
    oauth.setAuthorizationLocation("http://test-profile.example.net/authorization");
    oauth.setTokenLocation("http://test-profile.example.net/token");
    oauth.setServerType("DHIS2");
    oauth.apply();
    
    return oauth;
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
  
  @Request
  private void deleteOauthServers()
  {
    try
    {
      OauthServerQuery q = new OauthServerQuery(new QueryFactory());
      
      q.WHERE(q.getClientId().EQ("geoprism"));
      
      OIterator<? extends OauthServer> it = q.getIterator();
      
      while (it.hasNext())
      {
        it.next().delete();
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
  // ServerGeoObjectType got = USATestData.DISTRICT.getServerObject();
  // ServerHierarchyType ht = USATestData.HIER_ADMIN.getServerObject();
  //
  // GeoObjectJsonExporter exporter = new GeoObjectJsonExporter(got, ht, null,
  // true, GeoObjectExportFormat.JSON_DHIS2, system, -1, -1);
  // exporter.setDHIS2Facade(this.dhis2);
  // System.out.println(IOUtils.toString(exporter.export()));
  // }

  @Test
  @Request
  public void testSerializeExternalSystem()
  {
    DHIS2ExternalSystem dhis2Sys = (DHIS2ExternalSystem) system;
    
    JsonObject json = dhis2Sys.toJSON();
    DHIS2ExternalSystem dhis2Sys2 = (DHIS2ExternalSystem) ExternalSystem.desieralize(json);
    
    Assert.assertEquals(dhis2Sys.getUrl(), dhis2Sys2.getUrl());
    Assert.assertEquals(dhis2Sys.getUsername(), dhis2Sys2.getUsername());
    Assert.assertEquals(dhis2Sys.getPassword(), dhis2Sys2.getPassword());
  }
  
  @Test
  @Request
  public void testSerializeOauthServer()
  {
    OauthServer oauth = this.getOauthServer();
    
    Gson gson = new GsonBuilder().registerTypeAdapter(OauthServer.class, new RunwayJsonAdapters.RunwaySerializer(DHIS2ExternalSystem.OAUTH_SERVER_JSON_ATTRS)).create();
    JsonElement json = gson.toJsonTree(oauth);
    
    Gson gson2 = new GsonBuilder().registerTypeAdapter(OauthServer.class, new RunwayJsonAdapters.RunwayDeserializer()).create();
    OauthServer oauth2 = gson2.fromJson(json, OauthServer.class);
    
    Assert.assertEquals(oauth.getSecretKey(), oauth2.getSecretKey());
    Assert.assertEquals(oauth.getClientId(), oauth2.getClientId());
    Assert.assertEquals(oauth.getAuthorizationLocation(), oauth2.getAuthorizationLocation());
    Assert.assertEquals(oauth.getProfileLocation(), oauth2.getProfileLocation());
    Assert.assertEquals(oauth.getTokenLocation(), oauth2.getTokenLocation());
    Assert.assertEquals(oauth.getServerType(), oauth2.getServerType());
  }
  
  @Test
  @Request
  public void testSerializeOauthServerWithExternalSystem()
  {
    DHIS2ExternalSystem dhis2Sys = (DHIS2ExternalSystem) system;
    OauthServer oauth = this.getOauthServer();
    
    dhis2Sys.setOauthServer(oauth);
    
//    Gson gson = new GsonBuilder().registerTypeAdapter(OauthServer.class, new RunwayJsonAdapters.RunwaySerializer(DHIS2ExternalSystem.OAUTH_SERVER_JSON_ATTRS)).create();
//    JsonObject json = gson.toJsonTree(oauth).getAsJsonObject();
    JsonObject json = dhis2Sys.toJSON();
    
    Assert.assertTrue(json.has(DHIS2ExternalSystem.OAUTH_SERVER));
    
    Gson gson2 = new GsonBuilder().registerTypeAdapter(OauthServer.class, new RunwayJsonAdapters.RunwayDeserializer()).create();
    OauthServer oauth2 = gson2.fromJson(json.get(DHIS2ExternalSystem.OAUTH_SERVER), OauthServer.class);
    
    Assert.assertEquals(oauth.getSecretKey(), oauth2.getSecretKey());
    Assert.assertEquals(oauth.getClientId(), oauth2.getClientId());
    Assert.assertEquals(oauth.getAuthorizationLocation(), oauth2.getAuthorizationLocation());
    Assert.assertEquals(oauth.getProfileLocation(), oauth2.getProfileLocation());
    Assert.assertEquals(oauth.getTokenLocation(), oauth2.getTokenLocation());
    
    DHIS2ExternalSystem dhis2Sys2 = (DHIS2ExternalSystem) ExternalSystem.desieralize(json);
    
    Assert.assertEquals(dhis2Sys.getUrl(), dhis2Sys2.getUrl());
    Assert.assertEquals(dhis2Sys.getUsername(), dhis2Sys2.getUsername());
    Assert.assertEquals(dhis2Sys.getPassword(), dhis2Sys2.getPassword());
  }
  
  @Test
  @Request
  public void testRevealSerialize() throws IOException
  {
    ServerGeoObjectType got = USATestData.DISTRICT.getServerObject();
    ServerHierarchyType ht = USATestData.HIER_ADMIN.getServerObject();

    GeoObjectJsonExporter exporter = new GeoObjectJsonExporter(got, ht, null, true, GeoObjectExportFormat.JSON_REVEAL, system, -1, -1);
    System.out.println(exporter.export().toString());
  }
}
