package net.geoprism.registry;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.entity.StringEntity;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.SchedulerManager;

import junit.framework.Assert;
import net.geoprism.dhis2.dhis2adapter.DHIS2Objects;
import net.geoprism.dhis2.dhis2adapter.exception.HTTPException;
import net.geoprism.dhis2.dhis2adapter.exception.IncompatibleServerVersionException;
import net.geoprism.dhis2.dhis2adapter.exception.InvalidLoginException;
import net.geoprism.dhis2.dhis2adapter.exception.UnexpectedResponseException;
import net.geoprism.dhis2.dhis2adapter.response.DHIS2Response;
import net.geoprism.dhis2.dhis2adapter.response.MetadataGetResponse;
import net.geoprism.dhis2.dhis2adapter.response.MetadataImportResponse;
import net.geoprism.dhis2.dhis2adapter.response.model.Attribute;
import net.geoprism.dhis2.dhis2adapter.response.model.OrganisationUnit;
import net.geoprism.registry.dhis2.DHIS2FeatureService;
import net.geoprism.registry.dhis2.DHIS2ServiceFactory;
import net.geoprism.registry.etl.DHIS2AttributeMapping;
import net.geoprism.registry.etl.DHIS2ServiceTest;
import net.geoprism.registry.etl.DHIS2SyncConfig;
import net.geoprism.registry.etl.SyncLevel;
import net.geoprism.registry.etl.export.ExportHistory;
import net.geoprism.registry.etl.export.ExportStage;
import net.geoprism.registry.etl.export.dhis2.DHIS2OptionCache;
import net.geoprism.registry.etl.export.dhis2.DHIS2TransportServiceIF;
import net.geoprism.registry.graph.DHIS2ExternalSystem;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.service.SynchronizationConfigService;
import net.geoprism.registry.test.AllAttributesDataset;
import net.geoprism.registry.test.TestAttributeTypeInfo;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.TestGeoObjectTypeInfo;
import net.geoprism.registry.test.TestUserInfo;

public class RemoteDHIS2APITest
{
  private static final Integer API_VERSION = 35;
  
  private static final String VERSION = "2." + String.valueOf(API_VERSION) + ".1";
  
  private static final String URL = "https://play.dhis2.org/" + VERSION + "/";
  
  private static final String USERNAME = "admin";
  
  private static final String PASSWORD = "district";
  
  protected static AllAttributesDataset  testData;

  protected SynchronizationConfigService syncService;

  protected DHIS2ExternalSystem          system;

  protected DHIS2TransportServiceIF      dhis2;

  @BeforeClass
  public static void setUpClass()
  {
    TestDataSet.deleteExternalSystems("RemoteDHIS2Test");
    testData = AllAttributesDataset.newTestData();
    testData.setUpMetadata();

    if (!SchedulerManager.initialized())
    {
      SchedulerManager.start();
    }
  }

  @AfterClass
  public static void cleanUpClass()
  {
    testData.tearDownMetadata();
  }

  @Before
  public void setUp() throws UnexpectedResponseException, InvalidLoginException, HTTPException, IncompatibleServerVersionException
  {
    testData.setUpInstanceData();

    system = createDhis2ExternalSystem();
    
    syncService = new SynchronizationConfigService();
    
    this.dhis2 = DHIS2ServiceFactory.getDhis2TransportService(this.system);

    testData.logIn(AllAttributesDataset.USER_ORG_RA);
  }

  @After
  public void tearDown()
  {
    testData.logOut();

    testData.tearDownInstanceData();

    TestDataSet.deleteExternalSystems("RemoteDHIS2Test");
  }

  @Request
  private DHIS2ExternalSystem createDhis2ExternalSystem()
  {
    DHIS2ExternalSystem system = new DHIS2ExternalSystem();
    system.setId("RemoteDHIS2Test");
    system.setOrganization(testData.ORG.getServerObject());
    system.getEmbeddedComponent(ExternalSystem.LABEL).setValue("defaultLocale", "Test");
    system.getEmbeddedComponent(ExternalSystem.DESCRIPTION).setValue("defaultLocale", "Test");
    system.setUsername(USERNAME);
    system.setPassword(PASSWORD);
    system.setUrl(URL);
    system.setVersion(VERSION);
    system.apply();

    return system;
  }
  
  @Test
  @Request
  public void testExportCharacterAttr() throws Exception
  {
    exportCustomAttribute(AllAttributesDataset.GOT_CHAR, AllAttributesDataset.GO_CHAR, testData.AT_GO_CHAR, "tw1zAoX9tP6");
  }

  @Test
  @Request
  public void testExportIntegerAttr() throws Exception
  {
    exportCustomAttribute(AllAttributesDataset.GOT_INT, AllAttributesDataset.GO_INT, testData.AT_GO_INT, "V9JL0MAFQop");
  }

  @Test
  @Request
  public void testExportFloatAttr() throws Exception
  {
    exportCustomAttribute(AllAttributesDataset.GOT_FLOAT, AllAttributesDataset.GO_FLOAT, testData.AT_GO_FLOAT, "Po0hdj4UHUv");
  }

  @Test
  @Request
  public void testExportDateAttr() throws Exception
  {
    exportCustomAttribute(AllAttributesDataset.GOT_DATE, AllAttributesDataset.GO_DATE, testData.AT_GO_DATE, "Z5TiJm1H4TC");
  }

  @Test
  @Request
  public void testExportBoolAttr() throws Exception
  {
    exportCustomAttribute(AllAttributesDataset.GOT_BOOL, AllAttributesDataset.GO_BOOL, testData.AT_GO_BOOL, "HoRXtod7Z8W");
  }

  @Test
  @Request
  public void testExportTermAttr() throws Exception
  {
    exportCustomAttribute(AllAttributesDataset.GOT_TERM, AllAttributesDataset.GO_TERM, testData.AT_GO_TERM, "Bp9g0VvC1fK");
  }
  
  private void removeOrgUnitByCode(String code) throws InvalidLoginException, HTTPException
  {
    OrganisationUnit orgUnit = null;
    
    MetadataGetResponse<OrganisationUnit> resp = dhis2.metadataGet(OrganisationUnit.class);
    
    List<OrganisationUnit> orgUnits = resp.getObjects();
    
    for (OrganisationUnit ou : orgUnits)
    {
      if (ou.getCode().equals(code))
      {
        orgUnit = ou;
      }
    }
    
    if (orgUnit != null)
    {
      DHIS2Response resp2 = this.dhis2.entityIdDelete(DHIS2Objects.ORGANISATION_UNITS, orgUnit.getId(), null);
      System.out.println(resp2.getResponse());
    }
  }
  
  public void exportCustomAttribute(TestGeoObjectTypeInfo got, TestGeoObjectInfo go, TestAttributeTypeInfo attr, String externalAttrId) throws Exception
  {
    // Delete any data from a previous run
    removeOrgUnitByCode(AllAttributesDataset.GO_BOOL.getCode());
    removeOrgUnitByCode(AllAttributesDataset.GO_CHAR.getCode());
    removeOrgUnitByCode(AllAttributesDataset.GO_INT.getCode());
    removeOrgUnitByCode(AllAttributesDataset.GO_FLOAT.getCode());
    removeOrgUnitByCode(AllAttributesDataset.GO_TERM.getCode());
    removeOrgUnitByCode(AllAttributesDataset.GO_DATE.getCode());
    this.removeOrgUnitByCode(AllAttributesDataset.GO_ALL.getCode());
    
    // Make sure our prerequsite data exists on the server
    String attrPayload = IOUtils.toString(RemoteDHIS2APITest.class.getClassLoader().getResourceAsStream("remote-dhis2-api-test-attrs.json"), "UTF-8");
    MetadataImportResponse attrImportResponse = this.dhis2.metadataPost(null, new StringEntity(attrPayload.toString(), Charset.forName("UTF-8")));
    Assert.assertTrue(attrImportResponse.isSuccess());
    
    SyncLevel level2 = new SyncLevel();
    level2.setGeoObjectType(got.getServerObject().getCode());
    level2.setSyncType(SyncLevel.Type.ALL);
    level2.setLevel(2);

    Map<String, DHIS2AttributeMapping> mappings = new HashMap<String, DHIS2AttributeMapping>();

    DHIS2AttributeMapping mapping = new DHIS2AttributeMapping();

    mapping.setName(attr.getAttributeName());
    mapping.setExternalId(externalAttrId);
    mappings.put(attr.getAttributeName(), mapping);

    level2.setAttributes(mappings);
    
    Map<String, String> terms = new HashMap<String, String>();
    terms.put(TestDataSet.getClassifierIfExist(AllAttributesDataset.TERM_TERM_ROOT.getCode()).getClassifierId(), "HNaxl8GKadp");
    terms.put(TestDataSet.getClassifierIfExist(AllAttributesDataset.TERM_TERM_VAL1.getCode()).getClassifierId(), "HNaxl8GKadp");
    terms.put(TestDataSet.getClassifierIfExist(AllAttributesDataset.TERM_TERM_VAL2.getCode()).getClassifierId(), "fSdvGdkbjH2");
    mapping.setTerms(terms);
    
    SynchronizationConfig config = DHIS2ServiceTest.createSyncConfig(system, level2);
    DHIS2SyncConfig dhis2Config = (DHIS2SyncConfig) config.buildConfiguration();
    
    DHIS2FeatureService dhis2Features = new DHIS2FeatureService();
    
    ExportHistory history = new ExportHistory();
    history.setStartTime(new Date());
    history.addStatus(AllJobStatus.NEW);
    history.addStage(ExportStage.CONNECTING);
    history.apply();
    
    dhis2Features.synchronize(dhis2, dhis2Config, history);
    
    history = ExportHistory.get(history.getOid());
    
    Assert.assertEquals(ExportStage.COMPLETE.name(), history.getStage().get(0).name());
  }
  
  @Test
  public void testGetConfigForExternalSystem()
  {
    TestUserInfo[] users = new TestUserInfo[] { AllAttributesDataset.ADMIN_USER, AllAttributesDataset.USER_ORG_RA };
    
    for (TestUserInfo user : users)
    {
      TestDataSet.runAsUser(user, (request, adapter) -> {
        JsonObject jo = adapter.getConfigForExternalSystem(this.system.getOid(), AllAttributesDataset.HIER.getCode());
        
        Assert.assertTrue(jo.has("types"));
        JsonArray types = jo.get("types").getAsJsonArray();
        
        Assert.assertTrue(types.size() == 7 || types.size() == 8);
        
        Assert.assertTrue(jo.has("orgUnitGroups"));
        JsonArray orgUnitGroups = jo.get("orgUnitGroups").getAsJsonArray();
        
        Assert.assertTrue(orgUnitGroups.size() > 0);
      });
    }
  }
  
  @Test
  @Request
  public void testGetCustomAttributeConfiguration() throws Exception
  {
    JsonArray custConfig = this.syncService.getCustomAttributeConfiguration(testData.clientSession.getSessionId(), this.system.getOid(), AllAttributesDataset.GOT_ALL.getCode());
    
    for (int i = 0; i < custConfig.size(); ++i)
    {
      JsonObject attr = custConfig.get(i).getAsJsonObject();

      Assert.assertTrue(attr.get("name").getAsString().length() > 0);

      attr.get("dhis2Attrs").getAsJsonArray();

      Assert.assertTrue(attr.get("label").getAsString().length() > 0);

      Assert.assertTrue(attr.get("type").getAsString().length() > 0);

      Assert.assertNotNull(attr.get("typeLabel").getAsString());
      Assert.assertTrue(attr.get("typeLabel").getAsString().length() > 0);
    }
  }
  
  @Request
  @Test
  public void testGetDHIS2Attributes() throws InterruptedException, UnexpectedResponseException, InvalidLoginException, HTTPException, IncompatibleServerVersionException
  {
    MetadataGetResponse<Attribute> resp = dhis2.<Attribute> metadataGet(Attribute.class);

    Assert.assertTrue(resp.isSuccess());
    
    Assert.assertTrue(resp.getObjects().size() > 0);
  }
  
  @Request
  @Test
  public void testOptionCache() throws InterruptedException, UnexpectedResponseException, InvalidLoginException, HTTPException, IncompatibleServerVersionException
  {
    DHIS2OptionCache cache = new DHIS2OptionCache(this.dhis2);
    
    Assert.assertTrue(cache.getOptionSets().keySet().size() > 0);
  }

  @Request
  @Test
  public void testVersion() throws InterruptedException, UnexpectedResponseException, InvalidLoginException, HTTPException, IncompatibleServerVersionException
  {
    Assert.assertEquals(API_VERSION, this.dhis2.getVersionRemoteServerApi());
    Assert.assertEquals(VERSION, this.dhis2.getVersionRemoteServer());
    Assert.assertEquals(null, this.dhis2.getVersionApiCompat());
  }
  
  @Request
  @Test
  public void testFetchIds() throws InterruptedException, HTTPException, InvalidLoginException, UnexpectedResponseException, IncompatibleServerVersionException
  {
    Map<String, String> map = new HashMap<String, String>();
    
    for (int i = 0; i < 30; ++i)
    {
      String id = this.dhis2.getDhis2Id();
      
      if (map.put(id, id) != null)
      {
        Assert.fail("Non unqiue id");
      }
    }
  }
}
