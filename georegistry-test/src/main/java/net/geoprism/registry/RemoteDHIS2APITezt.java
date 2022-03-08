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
package net.geoprism.registry;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.apache.http.entity.StringEntity;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.SchedulerManager;

import net.geoprism.dhis2.dhis2adapter.DHIS2Objects;
import net.geoprism.dhis2.dhis2adapter.response.DHIS2Response;
import net.geoprism.dhis2.dhis2adapter.response.MetadataGetResponse;
import net.geoprism.dhis2.dhis2adapter.response.MetadataImportResponse;
import net.geoprism.dhis2.dhis2adapter.response.model.Attribute;
import net.geoprism.dhis2.dhis2adapter.response.model.OrganisationUnit;
import net.geoprism.registry.dhis2.DHIS2FeatureService;
import net.geoprism.registry.dhis2.DHIS2ServiceFactory;
import net.geoprism.registry.dhis2.DHIS2SynchronizationManager;
import net.geoprism.registry.etl.DHIS2AttributeMapping;
import net.geoprism.registry.etl.DHIS2SyncConfig;
import net.geoprism.registry.etl.DHIS2SyncLevel;
import net.geoprism.registry.etl.DHIS2TermAttributeMapping;
import net.geoprism.registry.etl.export.ExportHistory;
import net.geoprism.registry.etl.export.ExportStage;
import net.geoprism.registry.etl.export.dhis2.DHIS2OptionCache;
import net.geoprism.registry.etl.export.dhis2.DHIS2TransportServiceIF;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.graph.DHIS2ExternalSystem;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.SynchronizationConfigService;
import net.geoprism.registry.test.AllAttributesDataset;
import net.geoprism.registry.test.TestAttributeTypeInfo;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.TestGeoObjectTypeInfo;
import net.geoprism.registry.test.TestUserInfo;

@Ignore
public class RemoteDHIS2APITezt
{
  public static final String TEST_DATA_KEY = "RemoteDHIS2Test";
  
  private static final Integer API_VERSION = 37;
  
  private static final String VERSION = "2." + String.valueOf(API_VERSION) + ".2";
  
  private static final String URL = "https://play.dhis2.org/" + VERSION + "/";
  
  private static final String USERNAME = "admin";
  
  private static final String PASSWORD = "district";
  
  protected static RemoteDHIS2Dataset  testData;
  
  protected SynchronizationConfigService syncService;

  protected DHIS2ExternalSystem          system;

  protected DHIS2TransportServiceIF      dhis2;
  
  public static class RemoteDHIS2Dataset extends AllAttributesDataset
  {
    public static final String TEST_DATA_KEY = "RemoteDHIS2";
    
    public static final TestGeoObjectTypeInfo REMOTE_GOT_PARENT = new TestGeoObjectTypeInfo(TEST_DATA_KEY + "RemoteGotParent", GeometryType.MULTIPOLYGON, AllAttributesDataset.ORG);
    
    public static final TestGeoObjectInfo     REMOTE_GO_PARENT  = new TestGeoObjectInfo(TEST_DATA_KEY + "RemoteGoParent", REMOTE_GOT_PARENT);
    
    {
      managedGeoObjectTypeInfos.add(REMOTE_GOT_PARENT);
      managedGeoObjectInfos.add(REMOTE_GO_PARENT);
    }
    
    @Transaction
    @Override
    public void setUpClassRelationships()
    {
      HIER.setRoot(REMOTE_GOT_PARENT);

      REMOTE_GOT_PARENT.addChild(GOT_ALL, HIER);
      GOT_ALL.addChild(GOT_CHAR, HIER);
      GOT_ALL.addChild(GOT_INT, HIER);
      GOT_ALL.addChild(GOT_FLOAT, HIER);
      GOT_ALL.addChild(GOT_BOOL, HIER);
      GOT_ALL.addChild(GOT_DATE, HIER);
      GOT_ALL.addChild(GOT_TERM, HIER);
    }

    @Transaction
    @Override
    public void setUpRelationships()
    {
      REMOTE_GO_PARENT.addChild(GO_ALL, HIER);
      GO_ALL.addChild(GO_CHAR, HIER);
      GO_ALL.addChild(GO_INT, HIER);
      GO_ALL.addChild(GO_FLOAT, HIER);
      GO_ALL.addChild(GO_BOOL, HIER);
      GO_ALL.addChild(GO_DATE, HIER);
      GO_ALL.addChild(GO_TERM, HIER);
    }

    @Override
    public String getTestDataKey()
    {
      return TEST_DATA_KEY;
    }
  }

  @BeforeClass
  public static void setUpClass()
  {
    System.out.println("Test will run against server [" + URL + "].");
    
    TestDataSet.deleteExternalSystems("RemoteDHIS2Test");
    testData = new RemoteDHIS2Dataset();
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
  public void setUp() throws Exception
  {
    testData.setUpInstanceData();

    system = createDhis2ExternalSystem();
    
    syncService = new SynchronizationConfigService();
    
    this.dhis2 = DHIS2ServiceFactory.buildDhis2TransportService(this.system);
    
    setRootExternalId();

    testData.logIn(AllAttributesDataset.USER_ORG_RA);
    
    // Delete any data from a previous run
    removeRemoteOrgUnitByCode(AllAttributesDataset.GO_BOOL.getCode());
    removeRemoteOrgUnitByCode(AllAttributesDataset.GO_CHAR.getCode());
    removeRemoteOrgUnitByCode(AllAttributesDataset.GO_INT.getCode());
    removeRemoteOrgUnitByCode(AllAttributesDataset.GO_FLOAT.getCode());
    removeRemoteOrgUnitByCode(AllAttributesDataset.GO_TERM.getCode());
    removeRemoteOrgUnitByCode(AllAttributesDataset.GO_DATE.getCode());
    removeRemoteOrgUnitByCode(AllAttributesDataset.GO_ALL.getCode());
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
  
  @Request
  private void setRootExternalId() throws Exception
  {
    OrganisationUnit ou = this.getRemoteOrgUnitByCode("OU_525");
    RemoteDHIS2Dataset.REMOTE_GO_PARENT.getServerObject().createExternalId(this.system, ou.getId(), ImportStrategy.NEW_ONLY);
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
  
  private OrganisationUnit getRemoteOrgUnitByCode(String code) throws Exception
  {
    MetadataGetResponse<OrganisationUnit> resp = dhis2.metadataGet(OrganisationUnit.class);
    
    Assert.assertTrue(resp.isSuccess());
    
    List<OrganisationUnit> orgUnits = resp.getObjects();
    
    for (OrganisationUnit ou : orgUnits)
    {
      if (ou.getCode() != null && ou.getCode().equals(code))
      {
        return ou;
      }
    }
    
    return null;
  }
  
  private void removeRemoteOrgUnitByCode(String code) throws Exception
  {
    OrganisationUnit orgUnit = getRemoteOrgUnitByCode(code);
    
    if (orgUnit != null)
    {
      DHIS2Response resp2 = this.dhis2.entityIdDelete(DHIS2Objects.ORGANISATION_UNITS, orgUnit.getId(), null);
      
      if (!resp2.isSuccess())
      {
        System.out.println(resp2.getResponse());
      }
      
      Assert.assertTrue(resp2.isSuccess());
    }
  }
  
  public static Collection<DHIS2AttributeMapping> getDefaultMappings()
  {
    Collection<DHIS2AttributeMapping> mappings = new ArrayList<DHIS2AttributeMapping>();
    
    final String defaultMappingStrategy = DHIS2AttributeMapping.class.getName();
    
    HashMap<String, String> lazyMap = new HashMap<String, String>();
    
    // These DHIS2 default attribute names are also hardcoded in the synchronization-config-modal.component.ts front-end as well as the DHIS2FeatureService.buildDefaultDhis2OrgUnitAttributes
    lazyMap.put("name", DefaultAttribute.DISPLAY_LABEL.getName());
    lazyMap.put("shortName", DefaultAttribute.DISPLAY_LABEL.getName());
    lazyMap.put("code", DefaultAttribute.CODE.getName());
    lazyMap.put("openingDate", DefaultAttribute.CREATE_DATE.getName());
    
    for (Entry<String, String> entry : lazyMap.entrySet())
    {
      DHIS2AttributeMapping name = new DHIS2AttributeMapping();
      name.setCgrAttrName(entry.getValue());
      name.setDhis2AttrName(entry.getKey());
      name.setAttributeMappingStrategy(defaultMappingStrategy);
      mappings.add(name);
    }
    
    return mappings;
  }
  
  @Request
  public SynchronizationConfig createSyncConfig(TestGeoObjectTypeInfo got, TestGeoObjectInfo go, TestAttributeTypeInfo attr, String externalAttrId)
  {
    // Define reusable objects
    final ServerHierarchyType ht = AllAttributesDataset.HIER.getServerObject();
    final Organization org = AllAttributesDataset.ORG.getServerObject();

    // Create DHIS2 Sync Config
    DHIS2SyncConfig dhis2Config = new DHIS2SyncConfig();
    dhis2Config.setHierarchy(ht);
    dhis2Config.setLabel(new LocalizedValue("DHIS2 Export Test Data"));
    dhis2Config.setOrganization(org);

    // Populate Levels
    SortedSet<DHIS2SyncLevel> levels = new TreeSet<DHIS2SyncLevel>();
    
    DHIS2SyncLevel level1 = new DHIS2SyncLevel();
    level1.setGeoObjectType(RemoteDHIS2Dataset.REMOTE_GOT_PARENT.getServerObject().getCode());
    level1.setSyncType(DHIS2SyncLevel.Type.RELATIONSHIPS);
    level1.setLevel(0);
    levels.add(level1);

    DHIS2SyncLevel level2 = new DHIS2SyncLevel();
    level2.setGeoObjectType(AllAttributesDataset.GOT_ALL.getServerObject().getCode());
    level2.setSyncType(DHIS2SyncLevel.Type.ALL);
    level2.setMappings(getDefaultMappings());
    level2.setLevel(1);
    levels.add(level2);

    DHIS2SyncLevel level3 = new DHIS2SyncLevel();
    level3.setGeoObjectType(got.getServerObject().getCode());
    level3.setSyncType(DHIS2SyncLevel.Type.ALL);
    level3.setMappings(getDefaultMappings());
    level3.setLevel(2);
    levels.add(level3);
    
    Collection<DHIS2AttributeMapping> mappings = getDefaultMappings();
    DHIS2TermAttributeMapping mapping = new DHIS2TermAttributeMapping();
    mapping.setCgrAttrName(attr.getAttributeName());
    mapping.setExternalId(externalAttrId);
    mappings.add(mapping);
    level3.setMappings(mappings);
    
    Map<String, String> terms = new HashMap<String, String>();
    terms.put(AllAttributesDataset.AT_GO_TERM.fetchRootAsClassifier().getClassifierId(), "HNaxl8GKadp");
    terms.put(AllAttributesDataset.TERM_TERM_VAL1.fetchClassifier().getClassifierId(), "HNaxl8GKadp");
    terms.put(AllAttributesDataset.TERM_TERM_VAL2.fetchClassifier().getClassifierId(), "fSdvGdkbjH2");
    mapping.setTerms(terms);

    dhis2Config.setLevels(levels);

    // Serialize the DHIS2 Config
    GsonBuilder builder = new GsonBuilder();
    String dhis2JsonConfig = builder.create().toJson(dhis2Config);

    // Create a SynchronizationConfig
    SynchronizationConfig config = new SynchronizationConfig();
    config.setConfiguration(dhis2JsonConfig);
    config.setOrganization(org);
    config.setHierarchy(ht.getUniversalRelationship());
    config.setSystem(system.getOid());
    config.getLabel().setValue("DHIS2 Export Test");
    config.apply();

    return config;
  }
  
  public void exportCustomAttribute(TestGeoObjectTypeInfo got, TestGeoObjectInfo go, TestAttributeTypeInfo attr, String externalAttrId) throws Exception
  {
    // Make sure our prerequsite data exists on the server
    String attrPayload = IOUtils.toString(RemoteDHIS2APITezt.class.getClassLoader().getResourceAsStream("remote-dhis2-api-test-attrs.json"), "UTF-8");
    MetadataImportResponse attrImportResponse = this.dhis2.metadataPost(null, new StringEntity(attrPayload.toString(), Charset.forName("UTF-8")));
    Assert.assertTrue(attrImportResponse.isSuccess());
    
    SynchronizationConfig config = createSyncConfig(got, go, attr, externalAttrId);
    DHIS2SyncConfig dhis2Config = (DHIS2SyncConfig) config.buildConfiguration();
    
    ExportHistory history = new ExportHistory();
    history.setStartTime(new Date());
    history.addStatus(AllJobStatus.NEW);
    history.addStage(ExportStage.CONNECTING);
    history.apply();
    
    new DHIS2SynchronizationManager(dhis2, dhis2Config, history).synchronize();
    
    history = ExportHistory.get(history.getOid());
    
    Assert.assertEquals(ExportStage.COMPLETE.name(), history.getStage().get(0).name());
  }
  
  @Test
  public void testGetConfigForExternalSystem()
  {
    TestUserInfo[] users = new TestUserInfo[] { AllAttributesDataset.USER_ADMIN, AllAttributesDataset.USER_ORG_RA };
    
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
    JsonArray custConfig = new DHIS2FeatureService().getDHIS2AttributeConfiguration(testData.clientSession.getSessionId(), this.system.getOid(), AllAttributesDataset.GOT_ALL.getCode());
    
    for (int i = 0; i < custConfig.size(); ++i)
    {
      JsonObject attr = custConfig.get(i).getAsJsonObject();
      
      JsonObject cgrAttr = attr.get("cgrAttr").getAsJsonObject();

      Assert.assertTrue(cgrAttr.get("name").getAsString().length() > 0);

      attr.get("dhis2Attrs").getAsJsonArray();

      Assert.assertTrue(cgrAttr.get("label").getAsString().length() > 0);

      Assert.assertTrue(cgrAttr.get("type").getAsString().length() > 0);

      Assert.assertNotNull(cgrAttr.get("typeLabel").getAsString());
      Assert.assertTrue(cgrAttr.get("typeLabel").getAsString().length() > 0);
    }
  }
  
  @Request
  @Test
  public void testGetDHIS2Attributes() throws Exception
  {
    MetadataGetResponse<Attribute> resp = dhis2.<Attribute> metadataGet(Attribute.class);

    Assert.assertTrue(resp.isSuccess());
    
    Assert.assertTrue(resp.getObjects().size() > 0);
  }
  
  @Request
  @Test
  public void testOptionCache() throws Exception
  {
    DHIS2OptionCache cache = new DHIS2OptionCache(this.dhis2);
    
    Assert.assertTrue(cache.getOptionSets().keySet().size() > 0);
  }

  @Request
  @Test
  public void testVersion() throws Exception
  {
    Assert.assertEquals(API_VERSION, this.dhis2.getVersionRemoteServerApi());
    Assert.assertEquals(VERSION, this.dhis2.getVersionRemoteServer());
    Assert.assertEquals(null, this.dhis2.getVersionApiCompat());
  }
  
  @Request
  @Test
  public void testFetchIds() throws Exception
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
