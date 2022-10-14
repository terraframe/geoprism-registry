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
package net.geoprism.registry.etl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTimeCollection;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.SchedulerManager;

import net.geoprism.dhis2.dhis2adapter.response.model.Attribute;
import net.geoprism.dhis2.dhis2adapter.response.model.OrganisationUnit;
import net.geoprism.dhis2.dhis2adapter.response.model.ValueType;
import net.geoprism.registry.Organization;
import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.dhis2.DHIS2FeatureService;
import net.geoprism.registry.dhis2.DHIS2ServiceFactory;
import net.geoprism.registry.etl.DHIS2TestService.Dhis2Payload;
import net.geoprism.registry.etl.dhis2.DHIS2PayloadValidator;
import net.geoprism.registry.etl.export.ExportHistory;
import net.geoprism.registry.etl.export.SeverGeoObjectJsonAdapters;
import net.geoprism.registry.etl.export.dhis2.DHIS2GeoObjectJsonAdapters;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.graph.DHIS2ExternalSystem;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.AttributeTypeMetadata;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.service.SynchronizationConfigService;
import net.geoprism.registry.test.AllAttributesDataset;
import net.geoprism.registry.test.SchedulerTestUtils;
import net.geoprism.registry.test.TestAttributeTypeInfo;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.TestGeoObjectTypeInfo;
import net.geoprism.registry.test.TestUserInfo;

public class DHIS2ServiceTest
{
  protected static AllAttributesDataset  testData;

  protected SynchronizationConfigService syncService;

  protected ExternalSystem               system;

  protected DHIS2TestService             dhis2;

  @BeforeClass
  public static void setUpClass()
  {
    TestDataSet.deleteExternalSystems("DHIS2ExportTest");
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
  public void setUp()
  {
    testData.setUpInstanceData();

    this.dhis2 = new DHIS2TestService();
    DHIS2ServiceFactory.setDhis2TransportService(this.dhis2);

    system = createDhis2ExternalSystem();

    syncService = new SynchronizationConfigService();

    // deleteExternalIds();

    // createExternalIds();

    testData.logIn(AllAttributesDataset.USER_ORG_RA);
  }

  @After
  public void tearDown()
  {
    testData.logOut();

    testData.tearDownInstanceData();

    // deleteExternalIds();
    TestDataSet.deleteExternalSystems("DHIS2ExportTest");
  }

  @Request
  private void createExternalIds()
  {
    createExternalIdsInTrans();
  }

  // @Transaction
  private void createExternalIdsInTrans()
  {
    for (TestGeoObjectInfo go : testData.getManagedGeoObjects())
    {
      go.getServerObject().createExternalId(this.system, "ManualIdCreateTest", ImportStrategy.NEW_ONLY);
    }
  }

  @Request
  private void deleteExternalIds()
  {
    final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(GeoVertex.EXTERNAL_ID);

    StringBuilder builder = new StringBuilder();
    builder.append("SELECT FROM " + mdEdge.getDBClassName());

    builder.append(" WHERE out = :system");

    final GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(builder.toString());

    query.setParameter("system", this.system.getRID());

    List<EdgeObject> edges = query.getResults();

    for (EdgeObject edge : edges)
    {
      edge.delete();
    }
  }

  @Request
  private ExternalSystem createDhis2ExternalSystem()
  {
    DHIS2ExternalSystem system = new DHIS2ExternalSystem();
    system.setId("DHIS2ExportTest");
    system.setOrganization(AllAttributesDataset.ORG.getServerObject());
    system.getEmbeddedComponent(ExternalSystem.LABEL).setValue("defaultLocale", "Test");
    system.getEmbeddedComponent(ExternalSystem.DESCRIPTION).setValue("defaultLocale", "Test");
    system.setUsername("mock");
    system.setPassword("mock");
    system.setUrl("mock");
    system.setVersion(this.dhis2.getVersionRemoteServer());
    system.apply();

    return system;
  }

  public static SynchronizationConfig createSyncConfig(ExternalSystem system, DHIS2SyncLevel additionalLevel)
  {
    return createSyncConfig(system, additionalLevel, true);
  }
  
  public static SynchronizationConfig createSyncConfig(ExternalSystem system, DHIS2SyncLevel additionalLevel, boolean apply)
  {
    return createSyncConfig(system, additionalLevel, apply, TestDataSet.DEFAULT_OVER_TIME_DATE, false);
  }

  @Request
  public static SynchronizationConfig createSyncConfig(ExternalSystem system, DHIS2SyncLevel additionalLevel, boolean apply, Date date, boolean syncNonExist)
  {
    // Define reusable objects
    final ServerHierarchyType ht = AllAttributesDataset.HIER.getServerObject();
    final Organization org = AllAttributesDataset.ORG.getServerObject();

    // Create DHIS2 Sync Config
    DHIS2SyncConfig dhis2Config = new DHIS2SyncConfig();
    dhis2Config.setHierarchy(ht);
    dhis2Config.setHierarchyCode(ht.getCode());
    dhis2Config.setLabel(new LocalizedValue("DHIS2 Export Test Data"));
    dhis2Config.setOrganization(org);
    dhis2Config.setDate(date);
    dhis2Config.setSyncNonExistent(syncNonExist);

    // Populate Levels
    SortedSet<DHIS2SyncLevel> levels = new TreeSet<DHIS2SyncLevel>();

    DHIS2SyncLevel level = new DHIS2SyncLevel();
    level.setGeoObjectType(AllAttributesDataset.GOT_ALL.getServerObject().getCode());
    level.setSyncType(DHIS2SyncLevel.Type.ALL);
    level.setMappings(getDefaultMappings());
    level.setLevel(0);
    levels.add(level);

    // Populate Attribute Mappings
    if (additionalLevel != null)
    {
      levels.add(additionalLevel);
    }

    dhis2Config.setLevels(levels);

    // Serialize the DHIS2 Config
    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapter(Date.class, new SeverGeoObjectJsonAdapters.DateSerializer());
    String dhis2JsonConfig = builder.create().toJson(dhis2Config);

    // Create a SynchronizationConfig
    SynchronizationConfig config = new SynchronizationConfig();
    config.setConfiguration(dhis2JsonConfig);
    config.setOrganization(org);
    config.setHierarchy(ht.getMdTermRelationship());
    config.setSystem(system.getOid());
    config.getLabel().setValue("DHIS2 Export Test");

    if (apply)
    {
      config.apply();
    }

    return config;
  }

  public static Collection<DHIS2AttributeMapping> getDefaultMappings()
  {
    Collection<DHIS2AttributeMapping> mappings = new ArrayList<DHIS2AttributeMapping>();

    final String defaultMappingStrategy = DHIS2AttributeMapping.class.getName();

    HashMap<String, String> lazyMap = new HashMap<String, String>();

    // These DHIS2 default attribute names are also hardcoded in the
    // synchronization-config-modal.component.ts front-end as well as the
    // DHIS2FeatureService.buildDefaultDhis2OrgUnitAttributes
    lazyMap.put(OrganisationUnit.NAME, DefaultAttribute.DISPLAY_LABEL.getName());
    lazyMap.put(OrganisationUnit.SHORT_NAME, DefaultAttribute.DISPLAY_LABEL.getName());
    lazyMap.put(OrganisationUnit.CODE, DefaultAttribute.CODE.getName());
    lazyMap.put(OrganisationUnit.OPENING_DATE, DefaultAttribute.CREATE_DATE.getName());

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
  
  private LinkedList<Dhis2Payload> exportCustomAttribute(TestGeoObjectTypeInfo got, TestGeoObjectInfo go, TestAttributeTypeInfo attr, DHIS2AttributeMapping mapping) throws InterruptedException
  {
    return exportCustomAttribute(got, go, attr, mapping, TestDataSet.DEFAULT_OVER_TIME_DATE, false);
  }

  private LinkedList<Dhis2Payload> exportCustomAttribute(TestGeoObjectTypeInfo got, TestGeoObjectInfo go, TestAttributeTypeInfo attr, DHIS2AttributeMapping mapping, Date date, boolean syncNonExist) throws InterruptedException
  {
    /*
     * Create a config
     */
    DHIS2SyncLevel level2 = new DHIS2SyncLevel();
    level2.setGeoObjectType(got.getServerObject().getCode());
    level2.setSyncType(DHIS2SyncLevel.Type.ALL);
    level2.setLevel(1);

    Collection<DHIS2AttributeMapping> mappings = getDefaultMappings();

    if (mapping == null)
    {
      mapping = new DHIS2AttributeMapping();
      mapping.setAttributeMappingStrategy(DHIS2AttributeMapping.class.getName());
    }
    mapping.setCgrAttrName(attr.getAttributeName());
    mapping.setDhis2AttrName(attr.getAttributeName());
    mapping.setExternalId("TEST_EXTERNAL_ID");
    mappings.add(mapping);

    level2.setMappings(mappings);

    SynchronizationConfig config = createSyncConfig(this.system, level2, true, date, syncNonExist);

    /*
     * Run the sync service
     */
    JsonObject jo = syncService.run(testData.clientSession.getSessionId(), config.getOid());
    ExportHistory hist = ExportHistory.get(jo.get("historyId").getAsString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.SUCCESS);

    /*
     * Validate the payloads
     */
    LinkedList<Dhis2Payload> payloads = this.dhis2.getPayloads();
    
    if (syncNonExist || TestDataSet.DEFAULT_OVER_TIME_DATE.equals(date))
    {
      Assert.assertEquals(2, payloads.size());
  
      for (int level = 0; level < payloads.size(); ++level)
      {
        Dhis2Payload payload = payloads.get(level);
  
        JsonObject joPayload = JsonParser.parseString(payload.getData()).getAsJsonObject();
  
        if (level == 0 || level == 1)
        {
          DHIS2PayloadValidator.orgUnit(go, attr, mapping, level, joPayload);
          
          if (mapping instanceof DHIS2OrgUnitGroupAttributeMapping && level == 1)
          {
            DHIS2PayloadValidator.orgUnitGroup(go, attr, (DHIS2OrgUnitGroupAttributeMapping) mapping, level, joPayload);
          }
        }
      }
    }
    else if (!syncNonExist && !TestDataSet.DEFAULT_OVER_TIME_DATE.equals(date))
    {
      Assert.assertEquals(0, payloads.size());
    }
    
    return payloads;
  }

  // @Test
  // @Request
  // public void testSerializeConfig()
  // {
  // DHIS2SyncLevel level2 = new DHIS2SyncLevel();
  // level2.setGeoObjectType(testData.GOT_CHAR.getServerObject().getCode());
  // level2.setSyncType(DHIS2SyncLevel.Type.ALL);
  // level2.setLevel(1);
  //
  // SynchronizationConfig config = createSyncConfig(level2);
  //
  // JsonObject json = config.toJSON();
  //
  // JsonObject dhis2Config =
  // json.get(SynchronizationConfig.CONFIGURATION).getAsJsonObject();
  //
  // JsonArray levels = dhis2Config.get("levels").getAsJsonArray();
  //
  // Assert.assertEquals(2, levels.size());
  //
  // JsonObject level = levels.get(1).getAsJsonObject();
  //
  // JsonObject attributes = level.get("attributes").getAsJsonObject();
  //
  // Assert.assertEquals(1, attributes.size());
  // }

  @Test
  @Request
  public void testDontSyncNonExist() throws Exception
  {
    exportCustomAttribute(AllAttributesDataset.GOT_CHAR, AllAttributesDataset.GO_CHAR, testData.AT_GO_CHAR, null, new Date(), false);
  }
  
  @Test
  @Request
  public void testDoSyncNonExist() throws Exception
  {
    // We have to use the TestDataSet date otherwise the parent reference won't exist. So we'll just say the child doesn't exist at this date.
    ServerGeoObjectIF sgo = AllAttributesDataset.GO_CHAR.getServerObject();
    sgo.setExists(false, TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_OVER_TIME_DATE);
    sgo.apply(false);
    
    exportCustomAttribute(AllAttributesDataset.GOT_CHAR, AllAttributesDataset.GO_CHAR, testData.AT_GO_CHAR, null, TestDataSet.DEFAULT_OVER_TIME_DATE, true);
  }
  
  @Test
  @Request
  public void testExportCharacterAttr() throws Exception
  {
    exportCustomAttribute(AllAttributesDataset.GOT_CHAR, AllAttributesDataset.GO_CHAR, testData.AT_GO_CHAR, null);
  }
  
  @Test
  @Request
  public void testExportLocalAttr() throws Exception
  {
    exportCustomAttribute(AllAttributesDataset.GOT_LOCAL, AllAttributesDataset.GO_LOCAL, testData.AT_GO_LOCAL, null);
  }

  @Test
  @Request
  public void testExportIntegerAttr() throws Exception
  {
    exportCustomAttribute(AllAttributesDataset.GOT_INT, AllAttributesDataset.GO_INT, testData.AT_GO_INT, null);
  }

  @Test
  @Request
  public void testExportFloatAttr() throws Exception
  {
    exportCustomAttribute(AllAttributesDataset.GOT_FLOAT, AllAttributesDataset.GO_FLOAT, testData.AT_GO_FLOAT, null);
  }

  @Test
  @Request
  public void testExportDateAttr() throws Exception
  {
    exportCustomAttribute(AllAttributesDataset.GOT_DATE, AllAttributesDataset.GO_DATE, testData.AT_GO_DATE, null);
  }

  @Test
  @Request
  public void testExportBoolAttr() throws Exception
  {
    exportCustomAttribute(AllAttributesDataset.GOT_BOOL, AllAttributesDataset.GO_BOOL, testData.AT_GO_BOOL, null);
  }

  @Test
  @Request
  public void testExportTermAsOptionSet() throws Exception
  {
    DHIS2OptionSetAttributeMapping mapping = new DHIS2OptionSetAttributeMapping();
    mapping.setAttributeMappingStrategy(DHIS2OptionSetAttributeMapping.class.getName());

    Map<String, String> terms = new HashMap<String, String>();
    terms.put(AllAttributesDataset.AT_GO_TERM.fetchRootAsClassifier().getClassifierId(), "TEST_EXTERNAL_ID");
    terms.put(AllAttributesDataset.TERM_TERM_VAL1.fetchClassifier().getClassifierId(), "TEST_EXTERNAL_ID");
    terms.put(AllAttributesDataset.TERM_TERM_VAL2.fetchClassifier().getClassifierId(), "TEST_EXTERNAL_ID");
    mapping.setTerms(terms);
    
    exportCustomAttribute(AllAttributesDataset.GOT_TERM, AllAttributesDataset.GO_TERM, testData.AT_GO_TERM, mapping);
  }

  @Test
  @Request
  public void testExportTermAsOrgUnitGroup() throws Exception
  {
    DHIS2OrgUnitGroupAttributeMapping mapping = new DHIS2OrgUnitGroupAttributeMapping();
    mapping.setAttributeMappingStrategy(DHIS2OrgUnitGroupAttributeMapping.class.getName());

    Map<String, String> terms = new HashMap<String, String>();
//    terms.put(AllAttributesDataset.AT_GO_TERM.fetchRootAsClassifier().getClassifierId(), "CXw2yu5fodb"); // Level 1 doesn't have a term mapping
    terms.put(AllAttributesDataset.TERM_TERM_VAL1.fetchClassifier().getClassifierId(), "gzcv65VyaGq");
//    terms.put(AllAttributesDataset.TERM_TERM_VAL2.fetchClassifier().getClassifierId(), "uYxK4wmcPqA"); // This term isn't used by any exported Geo-Objects 
    mapping.setTerms(terms);
    
    exportCustomAttribute(AllAttributesDataset.GOT_TERM, AllAttributesDataset.GO_TERM, testData.AT_GO_TERM, mapping);
  }
  
//  @Test
//  @Request
//  public void testExportExistsAsBoolean() throws Exception
//  {
//    Collection<DHIS2AttributeMapping> mappings = new ArrayList<DHIS2AttributeMapping>();
//    
//    DHIS2AttributeMapping mapping = new DHIS2AttributeMapping();
//    mapping.setCgrAttrName(DefaultAttribute.EXISTS.getName());
//    mapping.setDhis2AttrName(DefaultAttribute.EXISTS.getName());
//    mapping.setAttributeMappingStrategy(DHIS2AttributeMapping.class.getName());
//    mappings.add(mapping);
//    
//    testExists(mappings);
//  }
  
  @Test
  @Request
  public void testExportExistsAsStartDate() throws Exception
  {
    Collection<DHIS2AttributeMapping> mappings = new ArrayList<DHIS2AttributeMapping>();
    
    DHIS2StartDateAttributeMapping mapping = new DHIS2StartDateAttributeMapping();
    mapping.setCgrAttrName(DefaultAttribute.EXISTS.getName());
    mapping.setDhis2AttrName(DefaultAttribute.EXISTS.getName());
    mapping.setAttributeMappingStrategy(DHIS2StartDateAttributeMapping.class.getName());
    mappings.add(mapping);
    
    DHIS2StartDateAttributeMapping mapping2 = new DHIS2StartDateAttributeMapping();
    mapping2.setCgrAttrName(DefaultAttribute.EXISTS.getName());
    mapping2.setDhis2AttrName(DefaultAttribute.EXISTS.getName());
    mapping2.setAttributeMappingStrategy(DHIS2StartDateAttributeMapping.class.getName());
    mapping2.setExternalId("TEST_EXTERNAL_ID");
    mappings.add(mapping2);
    
    testExists(mappings);
  }
  
  @Test
  @Request
  public void testExportExistsAsEndDate() throws Exception
  {
    Collection<DHIS2AttributeMapping> mappings = new ArrayList<DHIS2AttributeMapping>();
    
    DHIS2EndDateAttributeMapping mapping = new DHIS2EndDateAttributeMapping();
    mapping.setCgrAttrName(DefaultAttribute.EXISTS.getName());
    mapping.setDhis2AttrName(DefaultAttribute.EXISTS.getName());
    mapping.setAttributeMappingStrategy(DHIS2EndDateAttributeMapping.class.getName());
    mappings.add(mapping);
    
    DHIS2EndDateAttributeMapping mapping2 = new DHIS2EndDateAttributeMapping();
    mapping2.setCgrAttrName(DefaultAttribute.EXISTS.getName());
    mapping2.setDhis2AttrName(DefaultAttribute.EXISTS.getName());
    mapping2.setExternalId("TEST_EXTERNAL_ID");
    mapping2.setAttributeMappingStrategy(DHIS2EndDateAttributeMapping.class.getName());
    mappings.add(mapping2);
    
    testExists(mappings);
  }
  
  /*
   * The rest of the tests are creates. Let's intentionally test an update.
   */
  @Test
  @Request
  public void testSyncUpdate() throws Exception
  {
    ((VertexServerGeoObject) AllAttributesDataset.GO_BOOL.getServerObject()).createExternalId(system, DHIS2TestService.SIERRA_LEONE_ID, ImportStrategy.NEW_ONLY);
    
    exportCustomAttribute(AllAttributesDataset.GOT_BOOL, AllAttributesDataset.GO_BOOL, testData.AT_GO_BOOL, null);
  }
  
  private void testExists(Collection<DHIS2AttributeMapping> paramMappings) throws Exception
  {
    DHIS2AttributeMapping mapping = paramMappings.iterator().next();
    
    TestGeoObjectTypeInfo got = AllAttributesDataset.GOT_BOOL;
    TestGeoObjectInfo go = AllAttributesDataset.GO_BOOL;
    
    /*
     * Create a config
     */
    DHIS2SyncLevel level2 = new DHIS2SyncLevel();
    level2.setGeoObjectType(got.getServerObject().getCode());
    level2.setSyncType(DHIS2SyncLevel.Type.ALL);
    level2.setLevel(1);
    
    Collection<DHIS2AttributeMapping> mappings = getDefaultMappings();
    mappings.addAll(paramMappings);

    level2.setMappings(mappings);

    SynchronizationConfig config = createSyncConfig(this.system, level2);

    /*
     * Run the sync service
     */
    JsonObject jo = syncService.run(testData.clientSession.getSessionId(), config.getOid());
    ExportHistory hist = ExportHistory.get(jo.get("historyId").getAsString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.SUCCESS);

    /*
     * Validate the payloads
     */
    LinkedList<Dhis2Payload> payloads = this.dhis2.getPayloads();
    Assert.assertEquals((mapping instanceof DHIS2OrgUnitGroupAttributeMapping) ? 3 : 2, payloads.size());

    for (int level = 0; level < payloads.size(); ++level)
    {
      Dhis2Payload payload = payloads.get(level);

      JsonObject joPayload = JsonParser.parseString(payload.getData()).getAsJsonObject();
      
      if (level == 0)
      {
        DHIS2PayloadValidator.orgUnit(go, null, mapping, level, joPayload);
      }
      else if (level == 1)
      {
        JsonArray orgUnits = joPayload.get("organisationUnits").getAsJsonArray();

        Assert.assertEquals(1, orgUnits.size());

        JsonObject orgUnit = orgUnits.get(0).getAsJsonObject();

        Assert.assertEquals(level, orgUnit.get("level").getAsInt());

        Assert.assertEquals("MULTI_POLYGON", orgUnit.get("featureType").getAsString());
        
        Assert.assertEquals(go.getCode(), orgUnit.get("code").getAsString());

        Assert.assertTrue(orgUnit.has("attributeValues"));
        
        Assert.assertEquals(1, orgUnit.get("attributeValues").getAsJsonArray().size());
        
        JsonObject attributeValue = orgUnit.get("attributeValues").getAsJsonArray().get(0).getAsJsonObject();
        
        ServerGeoObjectIF serverGo = go.getServerObject();
        ValueOverTimeCollection votc = serverGo.getValuesOverTime(DefaultAttribute.EXISTS.getName());

        ValueOverTime vot = votc.get(votc.size() - 1);
        
        if (mapping instanceof DHIS2VOTDateAttributeMapping)
        {
          Date expectedDate = null;
          
          if (mapping instanceof DHIS2StartDateAttributeMapping)
          {
            expectedDate = vot.getStartDate();
          }
          else if (mapping instanceof DHIS2EndDateAttributeMapping)
          {
            expectedDate = vot.getEndDate();
          }
          
          String attributeValueValue = attributeValue.get("value").getAsString();
          Assert.assertEquals(DHIS2GeoObjectJsonAdapters.DHIS2Serializer.formatDate((Date) expectedDate), attributeValueValue);
          
          Assert.assertEquals(DHIS2GeoObjectJsonAdapters.DHIS2Serializer.formatDate((Date) expectedDate), orgUnit.get(DefaultAttribute.EXISTS.getName()).getAsString());
        }
        else
        {
          Assert.assertTrue(orgUnit.get(DefaultAttribute.EXISTS.getName()).getAsBoolean());
          Assert.assertTrue(attributeValue.get("value").getAsBoolean());
        }
      }
    }
  }
  
  @Test
  @Request
  public void testGetCustomAttributeConfiguration() throws Exception
  {
    SynchronizationConfig config = createSyncConfig(this.system, null);

    JsonArray custConfig = new DHIS2FeatureService().getDHIS2AttributeConfiguration(testData.clientSession.getSessionId(), config.getSystem(), AllAttributesDataset.GOT_ALL.getCode());

    final List<Attribute> defaultAttributes = DHIS2FeatureService.buildDefaultDhis2OrgUnitAttributes();
    // final Collection<DHIS2AttributeMapping> defaultMappings =
    // getDefaultMappings();
    GeoObjectType got_all = AllAttributesDataset.GOT_ALL.fetchDTO();

    for (int i = 0; i < custConfig.size(); ++i)
    {
      JsonObject attr = custConfig.get(i).getAsJsonObject();

      JsonObject cgrAttr = attr.get("cgrAttr").getAsJsonObject();
      String cgrAttrName = cgrAttr.get("name").getAsString();

      AttributeType attrType = got_all.getAttribute(cgrAttrName).get();
      TestAttributeTypeInfo testAttrType = null;
      
      JsonArray strategies = attr.get("attributeMappingStrategies").getAsJsonArray();
      List<DHIS2AttributeMapping> expectedStrategies = DHIS2FeatureService.getMappingStrategies(attrType);
      Assert.assertEquals(expectedStrategies.size(), strategies.size());
      
      for (int iStrat = 0; iStrat < strategies.size(); ++iStrat)
      {
        JsonObject strategy = strategies.get(iStrat).getAsJsonObject();
        
        DHIS2AttributeMapping expectedStrategy = expectedStrategies.stream().filter(strat -> strat.getClass().getName().equals(strategy.get("type").getAsString())).findFirst().get();
        
        JsonArray dhis2Attrs = strategy.get("dhis2Attrs").getAsJsonArray();

        if (cgrAttrName.equals(testData.AT_ALL_INT.getAttributeName()))
        {
          testAttrType = testData.AT_ALL_INT;

          Assert.assertEquals(1, dhis2Attrs.size());

          JsonObject dhis2Attr = dhis2Attrs.get(0).getAsJsonObject();

          Assert.assertEquals("V9JL0MAFQop", dhis2Attr.get("dhis2Id").getAsString());

          Assert.assertEquals("CGRIntegrationAttributeTest-Integer", dhis2Attr.get("code").getAsString());

          Assert.assertEquals("CGRIntegrationAttributeTest-Integer", dhis2Attr.get("name").getAsString());
        }
        else if (cgrAttrName.equals(testData.AT_ALL_BOOL.getAttributeName()))
        {
          testAttrType = testData.AT_ALL_BOOL;

          Assert.assertEquals(1, dhis2Attrs.size());

          JsonObject dhis2Attr = dhis2Attrs.get(0).getAsJsonObject();

          Assert.assertEquals("HoRXtod7Z8W", dhis2Attr.get("dhis2Id").getAsString());

          Assert.assertEquals("CGRIntegrationAttributeTest-Bool", dhis2Attr.get("code").getAsString());

          Assert.assertEquals("CGRIntegrationAttributeTest-Bool", dhis2Attr.get("name").getAsString());
        }
        else if (cgrAttrName.equals(testData.AT_ALL_CHAR.getAttributeName()))
        {
          testAttrType = testData.AT_ALL_CHAR;

          Assert.assertEquals(13, dhis2Attrs.size());

          List<String> defaultIds = defaultAttributes.stream().map(attribute -> attribute.getValueType().equals(ValueType.TEXT) ? attribute.getId() : null).collect(Collectors.toList());
          List<String> defaultNames = defaultAttributes.stream().map(attribute -> attribute.getValueType().equals(ValueType.TEXT) ? attribute.getName() : null).collect(Collectors.toList());

          for (int j = 0; j < dhis2Attrs.size(); j++)
          {
            JsonObject dhis2Attr = dhis2Attrs.get(j).getAsJsonObject();

            String id = dhis2Attr.get("dhis2Id").getAsString();

            Assert.assertTrue(id.equals("UKNKz1H10EE") || id.equals("n2xYlNbsfko") || id.equals("tw1zAoX9tP6") || defaultIds.contains(id));

            String code = dhis2Attr.get("code").getAsString();
            Assert.assertTrue(code.equals("IRID") || code.equals("NGOID") || code.equals("CGRIntegrationAttributeTest-Char") || defaultNames.contains(code));
          }
        }
        else if (cgrAttrName.equals(testData.AT_ALL_DATE.getAttributeName()))
        {
          testAttrType = testData.AT_ALL_DATE;

          Assert.assertEquals(3, dhis2Attrs.size());

          JsonObject dhis2Attr = dhis2Attrs.get(0).getAsJsonObject();

          String id = dhis2Attr.get("dhis2Id").getAsString();

          List<String> defaultIds = defaultAttributes.stream().map(attribute -> attribute.getValueType().equals(ValueType.DATE) ? attribute.getId() : null).collect(Collectors.toList());
          List<String> defaultNames = defaultAttributes.stream().map(attribute -> attribute.getValueType().equals(ValueType.DATE) ? attribute.getName() : null).collect(Collectors.toList());

          Assert.assertTrue(id.equals("Z5TiJm1H4TC") || defaultIds.contains(id));

          String code = dhis2Attr.get("code").getAsString();
          Assert.assertTrue(code.equals("CGRIntegrationAttributeTest-Date") || defaultNames.contains(code));

          String name = dhis2Attr.get("name").getAsString();
          Assert.assertTrue(name.equals("CGRIntegrationAttributeTest-Date") || defaultNames.contains(name));
        }
        else if (cgrAttrName.equals(testData.AT_ALL_FLOAT.getAttributeName()))
        {
          testAttrType = testData.AT_ALL_FLOAT;

          Assert.assertEquals(1, dhis2Attrs.size());

          JsonObject dhis2Attr = dhis2Attrs.get(0).getAsJsonObject();

          Assert.assertEquals("Po0hdj4UHUv", dhis2Attr.get("dhis2Id").getAsString());

          Assert.assertEquals("CGRIntegrationAttributeTest-Float", dhis2Attr.get("code").getAsString());

          Assert.assertEquals("CGRIntegrationAttributeTest-Float", dhis2Attr.get("name").getAsString());
        }
//        else if (cgrAttrName.equals(testData.AT_ALL_TERM.getAttributeName()))
//        {
//          attrType = testData.AT_ALL_TERM;
  //
//          JsonArray strategies = attr.get("attributeMappingStrategies").getAsJsonArray();
//          Assert.assertEquals(1, strategies.size());
//          Assert.assertEquals(DHISAttributeMapping.class.getName(), strategies.get(0).getAsString());
  //
//          Assert.assertEquals(2, dhis2Attrs.size());
  //
//          for (int j = 0; j < dhis2Attrs.size(); j++)
//          {
//            JsonObject dhis2Attr = dhis2Attrs.get(j).getAsJsonObject();
  //
//            String id = dhis2Attr.get("dhis2Id").getAsString();
  //
//            JsonArray options = dhis2Attr.get("options").getAsJsonArray();
  //
//            if (id.equals("Wt2PuMK4kTt"))
//            {
//              Assert.assertEquals(4, options.size());
  //
//              JsonObject option = options.get(0).getAsJsonObject();
  //
//              Assert.assertNotNull(option.get("code").getAsString());
  //
//              Assert.assertNotNull(option.get("name").getAsString());
  //
//              Assert.assertNotNull(option.get("id").getAsString());
//            }
//            else if (id.equals("Bp9g0VvC1fK"))
//            {
//              Assert.assertEquals(2, options.size());
  //
//              JsonObject option = options.get(0).getAsJsonObject();
  //
//              String code = option.get("code").getAsString();
  //
//              Assert.assertTrue(code.equals("0-14 years") || code.equals("val2"));
  //
//              String optionName = option.get("name").getAsString();
  //
//              Assert.assertTrue(optionName.equals("val1") || optionName.equals("val2"));
  //
//              String optionId = option.get("id").getAsString();
  //
//              Assert.assertTrue(optionId.equals("val1") || optionId.equals("val2"));
//            }
//            else
//            {
//              Assert.fail("Unexpected id [" + id + "].");
//            }
//          }
  //
//          JsonArray cgrTerms = attr.get("terms").getAsJsonArray();
  //
//          Assert.assertEquals(2, cgrTerms.size());
  //
//          for (int k = 0; k < cgrTerms.size(); ++k)
//          {
//            JsonObject term = cgrTerms.get(k).getAsJsonObject();
  //
//            String label = term.get("label").getAsString();
//            Assert.assertTrue(AllAttributesDataset.TERM_ALL_VAL1.getLabel().equals(label) || AllAttributesDataset.TERM_ALL_VAL2.getLabel().equals(label));
  //
//            String code = term.get("code").getAsString();
//            Assert.assertTrue(AllAttributesDataset.TERM_ALL_VAL1.getCode().equals(code) || AllAttributesDataset.TERM_ALL_VAL2.getCode().equals(code));
//          }
//        }
        else if (got_all.getAttribute(cgrAttrName).isPresent())
        {
          AttributeType at = got_all.getAttribute(cgrAttrName).get();

//          if (at.getType().equals(AttributeTermType.TYPE))
//          {
//            Assert.assertEquals(DHIS2TermAttributeMapping.class.getName(), strategies.get(0).getAsString());
//          }
//          else
//          {
//            Assert.assertEquals(DHIS2AttributeMapping.class.getName(), strategies.get(0).getAsString());
//          }

          if (at.getType().equals(AttributeCharacterType.TYPE))
          {
            Assert.assertEquals(13, dhis2Attrs.size());
          }

          testAttrType = AllAttributesDataset.GOT_ALL.getAttribute(cgrAttrName);
        }
        else
        {
          Assert.fail("Unexpected attribute name [" + cgrAttrName + "].");
        }

        Assert.assertEquals(attrType.getLabel().getValue(), cgrAttr.get("label").getAsString());

        Assert.assertEquals(attrType.getType(), cgrAttr.get("type").getAsString());

        Assert.assertNotNull(cgrAttr.get("typeLabel").getAsString());
        Assert.assertEquals(AttributeTypeMetadata.get().getTypeEnumDisplayLabel(attrType.getType()), cgrAttr.get("typeLabel").getAsString());
      }
    }
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

        Assert.assertEquals(18, orgUnitGroups.size());
      });
    }
  }

  @Request
  @Test
  public void testApplySyncConfig() throws Exception
  {
    SynchronizationConfig config = createSyncConfig(this.system, null, false);

    SynchronizationConfigService service = new SynchronizationConfigService();

    JsonObject json = config.toJSON();
    json.remove("oid");

    JsonObject configToJson = service.apply(testData.clientSession.getSessionId(), json.toString());

    String oid = configToJson.get(SynchronizationConfig.OID).getAsString();

    SynchronizationConfig.get(oid);
  }

  @Request
  @Test
  public void testExportGeoObjects() throws InterruptedException
  {
    SynchronizationConfig config = createSyncConfig(this.system, null);

    SynchronizationConfigService service = new SynchronizationConfigService();

    JsonObject joHist = service.run(testData.clientSession.getSessionId(), config.getOid());
    ExportHistory hist = ExportHistory.get(joHist.get("historyId").getAsString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.SUCCESS);

    hist = ExportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(1), hist.getWorkTotal());
    Assert.assertEquals(new Long(1), hist.getWorkProgress());
    Assert.assertEquals(ImportStage.COMPLETE.name(), hist.getStage().get(0).name());

    LinkedList<Dhis2Payload> payloads = this.dhis2.getPayloads();
    Assert.assertEquals(1, payloads.size());

    Dhis2Payload payload = payloads.get(0);

    JsonObject data = JsonParser.parseString(payload.getData()).getAsJsonObject();

    JsonArray orgUnits = data.get("organisationUnits").getAsJsonArray();

    Assert.assertEquals(1, orgUnits.size());

    JsonObject orgUnit = orgUnits.get(0).getAsJsonObject();

    Assert.assertEquals("AllAttrGO_ALL", orgUnit.get("code").getAsString());
  }
  
  /*
   * Tests exporting GeoObjects when the remote server is a snapshot version
   */
  @Request
  @Test
  public void testExportGeoObjectsSnapshotServer() throws InterruptedException
  {
    DHIS2ExternalSystem system = new DHIS2ExternalSystem();
    system.setId("DHIS2ExportTest");
    system.setOrganization(AllAttributesDataset.ORG.getServerObject());
    system.getEmbeddedComponent(ExternalSystem.LABEL).setValue("defaultLocale", "Test");
    system.getEmbeddedComponent(ExternalSystem.DESCRIPTION).setValue("defaultLocale", "Test");
    system.setUsername("mock");
    system.setPassword("mock");
    system.setUrl("mock");
    system.setVersion("2.31.9-SNAPSHOT");
    system.apply();
    
    SynchronizationConfig config = createSyncConfig(system, null);

    SynchronizationConfigService service = new SynchronizationConfigService();

    JsonObject joHist = service.run(testData.clientSession.getSessionId(), config.getOid());
    ExportHistory hist = ExportHistory.get(joHist.get("historyId").getAsString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.SUCCESS);

    hist = ExportHistory.get(hist.getOid());
    Assert.assertEquals(new Long(1), hist.getWorkTotal());
    Assert.assertEquals(new Long(1), hist.getWorkProgress());
    Assert.assertEquals(ImportStage.COMPLETE.name(), hist.getStage().get(0).name());

    LinkedList<Dhis2Payload> payloads = this.dhis2.getPayloads();
    Assert.assertEquals(1, payloads.size());

    Dhis2Payload payload = payloads.get(0);

    JsonObject data = JsonParser.parseString(payload.getData()).getAsJsonObject();

    JsonArray orgUnits = data.get("organisationUnits").getAsJsonArray();

    Assert.assertEquals(1, orgUnits.size());

    JsonObject orgUnit = orgUnits.get(0).getAsJsonObject();

    Assert.assertEquals("AllAttrGO_ALL", orgUnit.get("code").getAsString());
  }

}
