/**
 *
 */
package net.geoprism.registry.etl;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import com.google.gson.GsonBuilder;
import com.runwaysdk.session.Request;

import net.geoprism.account.OauthServer;
import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.etl.fhir.BasicFhirResourceProcessor;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.graph.FhirExternalSystem;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.service.request.SynchronizationConfigService;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;

@ContextConfiguration(classes = { TestConfig.class }) @WebAppConfiguration
@RunWith(SpringInstanceTestClassRunner.class)
public class FhirOauthImportTest extends FastDatasetTest implements InstanceTestClassListener
{
  @Autowired
  protected SynchronizationConfigService syncService;

  @Override
  public void beforeClassSetup() throws Exception
  {
    TestDataSet.deleteExternalSystems("FHIRImportTest");

    super.beforeClassSetup();
  }

  @Before
  public void setUp()
  {
    testData.setUpInstanceData();

    syncService = new SynchronizationConfigService();

    testData.logIn(FastTestDataset.USER_CGOV_RA);
  }

  @After
  public void tearDown()
  {
    testData.logOut();

    testData.tearDownInstanceData();
  }

  @Request
  private FhirExternalSystem createExternalSystem()
  {
    OauthServer oauth = new OauthServer();
    oauth.setTokenLocation("https://63.35.75.89/keycloak/auth/realms/GOFR/protocol/openid-connect/token");
    oauth.setProfileLocation("https://63.35.75.89/keycloak/auth/realms/GOFR/protocol/openid-connect/profile");
    oauth.setAuthorizationLocation("https://63.35.75.89/keycloak/auth/realms/GOFR/protocol/openid-connect/authorization");
    oauth.setSecretKey("df3dcc28-f79f-4df7-bd5c-427afe60a41b");
    oauth.setClientId("gofr-api");
    oauth.setServerType("Keycloak");
    oauth.apply();

    FhirExternalSystem system = new FhirExternalSystem();
    system.setId("FHIRImportTest");
    system.setOrganization(FastTestDataset.ORG_CGOV.getServerObject());
    system.getEmbeddedComponent(ExternalSystem.LABEL).setValue("defaultLocale", "Test");
    system.getEmbeddedComponent(ExternalSystem.DESCRIPTION).setValue("defaultLocale", "Test");
    system.setUrl("https://63.35.75.89/fhir/DEFAULT/");
    system.setSystem("localhost");
    system.setUsername("cgr");
    system.setPassword("cgr2021");
    system.setOauthServer(oauth);
    system.apply();

    return system;
  }

  @Request
  public static SynchronizationConfig createSyncConfig(ExternalSystem system)
  {
    // Define reusable objects
    final ServerHierarchyType ht = FastTestDataset.HIER_ADMIN.getServerObject();
    final ServerOrganization org = FastTestDataset.ORG_CGOV.getServerObject();

    // Create DHIS2 Sync Config
    FhirSyncImportConfig sourceConfig = new FhirSyncImportConfig();
    sourceConfig.setLabel(new LocalizedValue("FHIR Import Test Data"));
    sourceConfig.setOrganization(org);
    sourceConfig.setImplementation(BasicFhirResourceProcessor.class.getName());

    // Serialize the FHIR Config
    GsonBuilder builder = new GsonBuilder();
    String fhirExportJsonConfig = builder.create().toJson(sourceConfig);

    // Create a SynchronizationConfig
    SynchronizationConfig config = new SynchronizationConfig();
    config.setConfiguration(fhirExportJsonConfig);
    config.setOrganization(org);
    config.setGraphHierarchy(ht.getObject());
    config.setSystem(system.getOid());
    config.getLabel().setValue("FHIR Import Test");
    config.setIsImport(true);
    config.apply();

    return config;
  }

  // private HttpClientBuilder getHttpClientBuilder() throws
  // NoSuchAlgorithmException
  // {
  // HostnameVerifier hostnameVerifier =
  // SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
  //
  // SSLConnectionSocketFactory sslSocketFactory = new
  // SSLConnectionSocketFactory(SSLContext.getDefault(), hostnameVerifier);
  // Registry<ConnectionSocketFactory> socketFactoryRegistry =
  // RegistryBuilder.<ConnectionSocketFactory> create().register("http",
  // PlainConnectionSocketFactory.getSocketFactory()).register("https",
  // sslSocketFactory).build();
  //
  // BasicHttpClientConnectionManager connMgr = new
  // BasicHttpClientConnectionManager(socketFactoryRegistry);
  //
  // HttpClientBuilder b = HttpClientBuilder.create();
  // b.setConnectionManager(connMgr);
  //
  // return b;
  // }
  //
  @Request
  @Test
  public void testConnection() throws Exception
  {
    // FhirExternalSystem system = null;
    //
    // try
    // {
    // system = createExternalSystem();
    //
    // try (OauthFhirConnection connection = new OauthFhirConnection(system,
    // system.getOauthServer()))
    // {
    // Assert.assertNotNull(connection.getAccessToken());
    // Assert.assertNotNull(connection.getExpiresIn());
    // Assert.assertNotNull(connection.getLastSessionRefresh());
    //
    // IBaseBundle bundle =
    // connection.getClient().search().forResource(org.hl7.fhir.r4.model.Organization.class).execute();
    //
    // IParser parser = connection.getFhirContext().newJsonParser();
    // parser.setPrettyPrint(true);
    //
    // System.out.println(parser.encodeResourceToString(bundle));
    // }
    // }
    // finally
    // {
    // if (system != null)
    // {
    // system.delete();
    // }
    // }
  }
}
