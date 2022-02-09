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
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
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
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.SchedulerManager;

import net.geoprism.dhis2.dhis2adapter.response.model.Attribute;
import net.geoprism.dhis2.dhis2adapter.response.model.ValueType;
import net.geoprism.registry.Organization;
import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.dhis2.DHIS2FeatureService;
import net.geoprism.registry.dhis2.DHIS2ServiceFactory;
import net.geoprism.registry.etl.DHIS2TestService.Dhis2Payload;
import net.geoprism.registry.etl.export.ExportHistory;
import net.geoprism.registry.etl.export.dhis2.DHIS2GeoObjectJsonAdapters;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.graph.DHIS2ExternalSystem;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.AttributeTypeMetadata;
import net.geoprism.registry.model.ServerHierarchyType;
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

    //if (!SchedulerManager.initialized())
    //{
      SchedulerManager.start();
    //}
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

  @Request
  public static SynchronizationConfig createSyncConfig(ExternalSystem system, DHIS2SyncLevel additionalLevel, boolean apply)
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
  
  private DHIS2AttributeMapping findMapping(String cgrAttrName, Collection<DHIS2AttributeMapping> mappings)
  {
    for (DHIS2AttributeMapping mapping : mappings)
    {
      if (mapping.getCgrAttrName().equals(cgrAttrName))
      {
        return mapping;
      }
    }
    
    return null;
  }

  private void exportCustomAttribute(TestGeoObjectTypeInfo got, TestGeoObjectInfo go, TestAttributeTypeInfo attr) throws InterruptedException
  {
    DHIS2SyncLevel level2 = new DHIS2SyncLevel();
    level2.setGeoObjectType(got.getServerObject().getCode());
    level2.setSyncType(DHIS2SyncLevel.Type.ALL);
    level2.setLevel(1);

    Collection<DHIS2AttributeMapping> mappings = getDefaultMappings();

    DHIS2AttributeMapping mapping;
    if (attr.getType().equals(AttributeTermType.TYPE))
    {
      mapping = new DHIS2TermAttributeMapping();
      mapping.setAttributeMappingStrategy(DHIS2TermAttributeMapping.class.getName());
      
      Map<String, String> terms = new HashMap<String, String>();
      terms.put(AllAttributesDataset.AT_GO_TERM.fetchRootAsClassifier().getClassifierId(), "TEST_EXTERNAL_ID");
      terms.put(AllAttributesDataset.TERM_TERM_VAL1.fetchClassifier().getClassifierId(), "TEST_EXTERNAL_ID");
      terms.put(AllAttributesDataset.TERM_TERM_VAL2.fetchClassifier().getClassifierId(), "TEST_EXTERNAL_ID");
      ( (DHIS2TermAttributeMapping) mapping ).setTerms(terms);
    }
    else
    {
      mapping = new DHIS2AttributeMapping();
      mapping.setAttributeMappingStrategy(DHIS2AttributeMapping.class.getName());
    }
    mapping.setCgrAttrName(attr.getAttributeName());
    mapping.setDhis2AttrName(attr.getAttributeName());
    mapping.setExternalId("TEST_EXTERNAL_ID");
    mappings.add(mapping);

    level2.setMappings(mappings);

    SynchronizationConfig config = createSyncConfig(this.system, level2);

    JsonObject jo = syncService.run(testData.clientSession.getSessionId(), config.getOid());
    ExportHistory hist = ExportHistory.get(jo.get("historyId").getAsString());

    SchedulerTestUtils.waitUntilStatus(hist.getOid(), AllJobStatus.SUCCESS);

    LinkedList<Dhis2Payload> payloads = this.dhis2.getPayloads();
    Assert.assertEquals(2, payloads.size());

    for (int level = 0; level < payloads.size(); ++level)
    {
      Dhis2Payload payload = payloads.get(level);

      JsonObject joPayload = JsonParser.parseString(payload.getData()).getAsJsonObject();

      JsonArray orgUnits = joPayload.get("organisationUnits").getAsJsonArray();

      Assert.assertEquals(1, orgUnits.size());

      JsonObject orgUnit = orgUnits.get(0).getAsJsonObject();

      Assert.assertEquals(level, orgUnit.get("level").getAsInt());

      Assert.assertEquals("MULTI_POLYGON", orgUnit.get("featureType").getAsString());

      if (level == 0)
      {
        Assert.assertEquals(AllAttributesDataset.GO_ALL.getCode(), orgUnit.get("code").getAsString());
      }
      else
      {
        Assert.assertEquals(go.getCode(), orgUnit.get("code").getAsString());

        Assert.assertTrue(orgUnit.has("attributeValues"));

        JsonArray attributeValues = orgUnit.get("attributeValues").getAsJsonArray();

        Assert.assertEquals(1, attributeValues.size());

        JsonObject attributeValue = attributeValues.get(0).getAsJsonObject();

        Assert.assertNotNull(attributeValue.get("lastUpdated").getAsString());

        Assert.assertNotNull(attributeValue.get("created").getAsString());

        AttributeType attrDto = attr.fetchDTO();

        if (attrDto instanceof AttributeIntegerType)
        {
          Assert.assertEquals(go.getServerObject().getValue(attr.getAttributeName()), attributeValue.get("value").getAsLong());
        }
        else if (attrDto instanceof AttributeFloatType)
        {
          Assert.assertEquals(go.getServerObject().getValue(attr.getAttributeName()), attributeValue.get("value").getAsDouble());
        }
        else if (attrDto instanceof AttributeDateType)
        {
          // TODO : If we fetch the object from the database in this manner the
          // miliseconds aren't included on the date. But if we fetch the object
          // via a query (as in DataExportJob) then the miliseconds ARE
          // included...
          // String expected =
          // DHIS2GeoObjectJsonAdapters.DHIS2Serializer.formatDate((Date)
          // go.getServerObject().getValue(attr.getAttributeName()));

          String expected = DHIS2GeoObjectJsonAdapters.DHIS2Serializer.formatDate(AllAttributesDataset.GO_DATE_VALUE);
          String actual = attributeValue.get("value").getAsString();

          Assert.assertEquals(expected, actual);
        }
        else if (attrDto instanceof AttributeBooleanType)
        {
          Assert.assertEquals(go.getServerObject().getValue(attr.getAttributeName()), attributeValue.get("value").getAsBoolean());
        }
        else if (attrDto instanceof AttributeTermType)
        {
          String dhis2Id = attributeValue.get("value").getAsString();

          // Term term = (Term)
          // go.getServerObject().getValue(attr.getAttributeName());

          Assert.assertEquals("TEST_EXTERNAL_ID", dhis2Id);
        }
        else
        {
          Assert.assertEquals(go.getServerObject().getValue(attr.getAttributeName()), attributeValue.get("value").getAsString());
        }

        Assert.assertEquals("TEST_EXTERNAL_ID", attributeValue.get("attribute").getAsJsonObject().get("id").getAsString());
      }
    }
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
  public void testExportCharacterAttr() throws Exception
  {
    exportCustomAttribute(AllAttributesDataset.GOT_CHAR, AllAttributesDataset.GO_CHAR, testData.AT_GO_CHAR);
  }

  @Test
  @Request
  public void testExportIntegerAttr() throws Exception
  {
    exportCustomAttribute(AllAttributesDataset.GOT_INT, AllAttributesDataset.GO_INT, testData.AT_GO_INT);
  }

  @Test
  @Request
  public void testExportFloatAttr() throws Exception
  {
    exportCustomAttribute(AllAttributesDataset.GOT_FLOAT, AllAttributesDataset.GO_FLOAT, testData.AT_GO_FLOAT);
  }

  @Test
  @Request
  public void testExportDateAttr() throws Exception
  {
    exportCustomAttribute(AllAttributesDataset.GOT_DATE, AllAttributesDataset.GO_DATE, testData.AT_GO_DATE);
  }

  @Test
  @Request
  public void testExportBoolAttr() throws Exception
  {
    exportCustomAttribute(AllAttributesDataset.GOT_BOOL, AllAttributesDataset.GO_BOOL, testData.AT_GO_BOOL);
  }

  @Test
  @Request
  public void testExportTermAttr() throws Exception
  {
    exportCustomAttribute(AllAttributesDataset.GOT_TERM, AllAttributesDataset.GO_TERM, testData.AT_GO_TERM);
  }

  @Test
  @Request
  public void testGetCustomAttributeConfiguration() throws Exception
  {
    SynchronizationConfig config = createSyncConfig(this.system, null);

    JsonArray custConfig = new DHIS2FeatureService().getDHIS2AttributeConfiguration(testData.clientSession.getSessionId(), config.getSystem(), AllAttributesDataset.GOT_ALL.getCode());
    
    final List<Attribute> defaultAttributes = DHIS2FeatureService.buildDefaultDhis2OrgUnitAttributes();
//    final Collection<DHIS2AttributeMapping> defaultMappings = getDefaultMappings();
    GeoObjectType got_all = AllAttributesDataset.GOT_ALL.fetchDTO();

    for (int i = 0; i < custConfig.size(); ++i)
    {
      JsonObject attr = custConfig.get(i).getAsJsonObject();

      JsonObject cgrAttr = attr.get("cgrAttr").getAsJsonObject();
      String cgrAttrName = cgrAttr.get("name").getAsString();

      TestAttributeTypeInfo attrType = null;

      JsonArray dhis2Attrs = attr.get("dhis2Attrs").getAsJsonArray();

      if (cgrAttrName.equals(testData.AT_ALL_INT.getAttributeName()))
      {
        attrType = testData.AT_ALL_INT;
        
        JsonArray strategies = attr.get("attributeMappingStrategies").getAsJsonArray();
        Assert.assertEquals(1, strategies.size());
        Assert.assertEquals(DHIS2AttributeMapping.class.getName(), strategies.get(0).getAsString());

        Assert.assertEquals(1, dhis2Attrs.size());

        JsonObject dhis2Attr = dhis2Attrs.get(0).getAsJsonObject();

        Assert.assertEquals("V9JL0MAFQop", dhis2Attr.get("dhis2Id").getAsString());

        Assert.assertEquals("CGRIntegrationAttributeTest-Integer", dhis2Attr.get("code").getAsString());

        Assert.assertEquals("CGRIntegrationAttributeTest-Integer", dhis2Attr.get("name").getAsString());
      }
      else if (cgrAttrName.equals(testData.AT_ALL_BOOL.getAttributeName()))
      {
        attrType = testData.AT_ALL_BOOL;
        
        JsonArray strategies = attr.get("attributeMappingStrategies").getAsJsonArray();
        Assert.assertEquals(1, strategies.size());
        Assert.assertEquals(DHIS2AttributeMapping.class.getName(), strategies.get(0).getAsString());

        Assert.assertEquals(1, dhis2Attrs.size());

        JsonObject dhis2Attr = dhis2Attrs.get(0).getAsJsonObject();

        Assert.assertEquals("HoRXtod7Z8W", dhis2Attr.get("dhis2Id").getAsString());

        Assert.assertEquals("CGRIntegrationAttributeTest-Bool", dhis2Attr.get("code").getAsString());

        Assert.assertEquals("CGRIntegrationAttributeTest-Bool", dhis2Attr.get("name").getAsString());
      }
      else if (cgrAttrName.equals(testData.AT_ALL_CHAR.getAttributeName()))
      {
        attrType = testData.AT_ALL_CHAR;
        
        JsonArray strategies = attr.get("attributeMappingStrategies").getAsJsonArray();
        Assert.assertEquals(1, strategies.size());
        Assert.assertEquals(DHIS2AttributeMapping.class.getName(), strategies.get(0).getAsString());

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
        attrType = testData.AT_ALL_DATE;
        
        JsonArray strategies = attr.get("attributeMappingStrategies").getAsJsonArray();
        Assert.assertEquals(1, strategies.size());
        Assert.assertEquals(DHIS2AttributeMapping.class.getName(), strategies.get(0).getAsString());

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
        attrType = testData.AT_ALL_FLOAT;
        
        JsonArray strategies = attr.get("attributeMappingStrategies").getAsJsonArray();
        Assert.assertEquals(1, strategies.size());
        Assert.assertEquals(DHIS2AttributeMapping.class.getName(), strategies.get(0).getAsString());

        Assert.assertEquals(1, dhis2Attrs.size());

        JsonObject dhis2Attr = dhis2Attrs.get(0).getAsJsonObject();

        Assert.assertEquals("Po0hdj4UHUv", dhis2Attr.get("dhis2Id").getAsString());

        Assert.assertEquals("CGRIntegrationAttributeTest-Float", dhis2Attr.get("code").getAsString());

        Assert.assertEquals("CGRIntegrationAttributeTest-Float", dhis2Attr.get("name").getAsString());
      }
      else if (cgrAttrName.equals(testData.AT_ALL_TERM.getAttributeName()))
      {
        attrType = testData.AT_ALL_TERM;
        
        JsonArray strategies = attr.get("attributeMappingStrategies").getAsJsonArray();
        Assert.assertEquals(1, strategies.size());
        Assert.assertEquals(DHIS2TermAttributeMapping.class.getName(), strategies.get(0).getAsString());

        Assert.assertEquals(2, dhis2Attrs.size());

        for (int j = 0; j < dhis2Attrs.size(); j++)
        {
          JsonObject dhis2Attr = dhis2Attrs.get(j).getAsJsonObject();

          String id = dhis2Attr.get("dhis2Id").getAsString();

          JsonArray options = dhis2Attr.get("options").getAsJsonArray();

          if (id.equals("Wt2PuMK4kTt"))
          {
            Assert.assertEquals(4, options.size());

            JsonObject option = options.get(0).getAsJsonObject();

            Assert.assertNotNull(option.get("code").getAsString());

            Assert.assertNotNull(option.get("name").getAsString());

            Assert.assertNotNull(option.get("id").getAsString());
          }
          else if (id.equals("Bp9g0VvC1fK"))
          {
            Assert.assertEquals(2, options.size());

            JsonObject option = options.get(0).getAsJsonObject();

            String code = option.get("code").getAsString();

            Assert.assertTrue(code.equals("0-14 years") || code.equals("val2"));

            String optionName = option.get("name").getAsString();

            Assert.assertTrue(optionName.equals("val1") || optionName.equals("val2"));

            String optionId = option.get("id").getAsString();

            Assert.assertTrue(optionId.equals("val1") || optionId.equals("val2"));
          }
          else
          {
            Assert.fail("Unexpected id [" + id + "].");
          }
        }

        JsonArray cgrTerms = attr.get("terms").getAsJsonArray();

        Assert.assertEquals(2, cgrTerms.size());

        for (int k = 0; k < cgrTerms.size(); ++k)
        {
          JsonObject term = cgrTerms.get(k).getAsJsonObject();

          String label = term.get("label").getAsString();
          Assert.assertTrue(AllAttributesDataset.TERM_ALL_VAL1.getLabel().equals(label) || AllAttributesDataset.TERM_ALL_VAL2.getLabel().equals(label));

          String code = term.get("code").getAsString();
          Assert.assertTrue(AllAttributesDataset.TERM_ALL_VAL1.getCode().equals(code) || AllAttributesDataset.TERM_ALL_VAL2.getCode().equals(code));
        }
      }
      else if (got_all.getAttribute(cgrAttrName).isPresent())
      {
        AttributeType at = got_all.getAttribute(cgrAttrName).get();
        
        JsonArray strategies = attr.get("attributeMappingStrategies").getAsJsonArray();
        Assert.assertEquals(1, strategies.size());
        
        if (at.getType().equals(AttributeTermType.TYPE))
        {
          Assert.assertEquals(DHIS2TermAttributeMapping.class.getName(), strategies.get(0).getAsString());
        }
        else
        {
          Assert.assertEquals(DHIS2AttributeMapping.class.getName(), strategies.get(0).getAsString());
        }
        
        if (at.getType().equals(AttributeCharacterType.TYPE))
        {
          Assert.assertEquals(13, dhis2Attrs.size());
        }
        
        attrType = AllAttributesDataset.GOT_ALL.getAttribute(cgrAttrName);
      }
      else
      {
        Assert.fail("Unexpected attribute name [" + cgrAttrName + "].");
      }

      Assert.assertEquals(attrType.fetchDTO().getLabel().getValue(), cgrAttr.get("label").getAsString());

      Assert.assertEquals(attrType.fetchDTO().getType(), cgrAttr.get("type").getAsString());

      Assert.assertNotNull(cgrAttr.get("typeLabel").getAsString());
      Assert.assertEquals(AttributeTypeMetadata.get().getTypeEnumDisplayLabel(attrType.fetchDTO().getType()), cgrAttr.get("typeLabel").getAsString());
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
  
}
