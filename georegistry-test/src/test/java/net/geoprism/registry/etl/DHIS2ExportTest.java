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
package net.geoprism.registry.etl;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.junit.After;
import org.junit.AfterClass;
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
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.SchedulerManager;

import junit.framework.Assert;
import net.geoprism.dhis2.dhis2adapter.response.model.ValueType;
import net.geoprism.registry.Organization;
import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.etl.DHIS2TestService.Dhis2Payload;
import net.geoprism.registry.etl.export.DataExportServiceFactory;
import net.geoprism.registry.etl.export.ExportHistory;
import net.geoprism.registry.etl.export.dhis2.DHIS2GeoObjectJsonAdapters;
import net.geoprism.registry.graph.DHIS2ExternalSystem;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.SynchronizationConfigService;
import net.geoprism.registry.test.AllAttributesDataset;
import net.geoprism.registry.test.SchedulerTestUtils;
import net.geoprism.registry.test.TestAttributeTypeInfo;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.TestGeoObjectTypeInfo;

public class DHIS2ExportTest
{
  protected static AllAttributesDataset testData;
  
  protected SynchronizationConfigService syncService;
  
  protected ExternalSystem system;
  
  protected DHIS2TestService dhis2;
  
  @BeforeClass
  public static void setUpClass()
  {
    TestDataSet.deleteExternalSystems("DHIS2ExportTest");
    
    testData = AllAttributesDataset.newTestData();
    testData.setUpMetadata();
    
    SchedulerManager.start();
  }

  @AfterClass
  public static void cleanUpClass()
  {
    if (testData != null)
    {
      testData.tearDownMetadata();
    }
    
    SchedulerManager.shutdown();
  }

  @Before
  public void setUp()
  {
    if (testData != null)
    {
      testData.setUpInstanceData();
    }
    
    this.dhis2 = new DHIS2TestService();
    DataExportServiceFactory.setDhis2Service(this.dhis2);
    
    system = createDhis2ExternalSystem();
    
    syncService = new SynchronizationConfigService();
    
    
    
//    deleteExternalIds();
    
    
//    createExternalIds();
  }
  
  @After
  public void tearDown()
  {
    if (testData != null)
    {
      testData.tearDownInstanceData();
    }
    
//    deleteExternalIds();
    TestDataSet.deleteExternalSystems("DHIS2ExportTest");
  }
  
  @Request
  private void createExternalIds()
  {
    createExternalIdsInTrans();
  }
//  @Transaction
  private void createExternalIdsInTrans()
  {
    for (TestGeoObjectInfo go : testData.getManagedGeoObjects())
    {
      go.getServerObject().createExternalId(this.system, "ManualIdCreateTest");
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
      System.out.println("Deleting external id with oid [" + edge.getObjectValue("oid") + "]");
      edge.delete();
    }
  }
  
  @Request
  private ExternalSystem createDhis2ExternalSystem()
  {
    DHIS2ExternalSystem system = new DHIS2ExternalSystem();
    system.setId("DHIS2ExportTest");
    system.setOrganization(testData.ORG.getServerObject());
    system.getEmbeddedComponent(ExternalSystem.LABEL).setValue("defaultLocale", "Test");
    system.getEmbeddedComponent(ExternalSystem.DESCRIPTION).setValue("defaultLocale", "Test");
    system.setUsername("mock");
    system.setPassword("mock");
    system.setUrl("mock");
    system.setVersion("2.31.9");
    system.apply();
    
    return system;
  }
  
  @Request
  private SynchronizationConfig createSyncConfig(SyncLevel additionalLevel, TestGeoObjectTypeInfo got, TestAttributeTypeInfo attr)
  {
    // Define reusable objects
    final ServerHierarchyType ht = testData.HIER.getServerObject();
    final Organization org = testData.ORG.getServerObject();
    
    // Create DHIS2 Sync Config
    DHIS2SyncConfig dhis2Config = new DHIS2SyncConfig();
    dhis2Config.setHierarchy(ht);
    dhis2Config.setLabel(new LocalizedValue("DHIS2 Export Test Data"));
    dhis2Config.setOrganization(org);
    
    // Populate Levels
    SortedSet<SyncLevel> levels = new TreeSet<SyncLevel>();
    
    SyncLevel level = new SyncLevel();
    level.setGeoObjectType(testData.GOT_ALL.getServerObject().getCode());
    level.setSyncType(SyncLevel.Type.ALL);
    level.setLevel(1);
    levels.add(level);
    
    // Populate Attribute Mappings
    if (additionalLevel != null)
    {
      Map<String, DHIS2AttributeMapping> mappings = new HashMap<String, DHIS2AttributeMapping>();
      
      DHIS2AttributeMapping mapping = new DHIS2AttributeMapping();
      
      mapping.setDhis2ValueType(ValueType.TEXT);
      mapping.setRunwayAttributeId(attr.getServerObject().getOid());
      mapping.setExternalId("TEST_EXTERNAL_ID");
      mappings.put(attr.getAttributeName(), mapping);
      
      additionalLevel.setAttributes(mappings);
      
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
    config.setHierarchy(ht.getEntityRelationship());
    config.setSystem(this.system.getOid());
    config.getLabel().setValue("DHIS2 Export Test");
    config.apply();
    
    return config;
  }
  
  private void exportCustomAttribute(TestGeoObjectTypeInfo got, TestGeoObjectInfo go, TestAttributeTypeInfo attr) throws InterruptedException
  {
    SyncLevel level2 = new SyncLevel();
    level2.setGeoObjectType(got.getServerObject().getCode());
    level2.setSyncType(SyncLevel.Type.ALL);
    level2.setLevel(2);
    
    SynchronizationConfig config = createSyncConfig(level2, got, attr);
    
    JsonObject jo = syncService.run(testData.adminSession.getSessionId(), config.getOid());
    ExportHistory hist = ExportHistory.get(jo.get("historyId").getAsString());
    
    SchedulerTestUtils.waitUntilStatus(hist, AllJobStatus.SUCCESS);
    
    LinkedList<Dhis2Payload> payloads = this.dhis2.getPayloads();
    Assert.assertEquals(2, payloads.size());
    
    for (int level = 0; level < payloads.size(); ++level)
    {
      Dhis2Payload payload = payloads.get(level);
      
      System.out.println("Payload " + level + " " + payload.getData());
      
      JsonObject joPayload = JsonParser.parseString(payload.getData()).getAsJsonObject();
      
      JsonArray orgUnits = joPayload.get("organisationUnits").getAsJsonArray();
      
      Assert.assertEquals(1, orgUnits.size());
      
      JsonObject orgUnit = orgUnits.get(0).getAsJsonObject();
      
      Assert.assertEquals(level + 1, orgUnit.get("level").getAsInt());
      
      Assert.assertEquals("MULTI_POLYGON", orgUnit.get("featureType").getAsString());
      
      if (level == 0)
      {
        Assert.assertEquals(testData.GO_ALL.getCode(), orgUnit.get("code").getAsString());
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
        
        if (attr.toDTO() instanceof AttributeCharacterType)
        {
          Assert.assertEquals(go.getServerObject().getValue(attr.getAttributeName()), attributeValue.get("value").getAsString());
        }
        else if (attr.toDTO() instanceof AttributeIntegerType)
        {
          Assert.assertEquals(go.getServerObject().getValue(attr.getAttributeName()), attributeValue.get("value").getAsLong());
        }
        else if (attr.toDTO() instanceof AttributeFloatType)
        {
          Assert.assertEquals(go.getServerObject().getValue(attr.getAttributeName()), attributeValue.get("value").getAsDouble());
        }
        else if (attr.toDTO() instanceof AttributeDateType)
        {
          String expected = DHIS2GeoObjectJsonAdapters.DHIS2Serializer.formatDate((Date) go.getServerObject().getValue(attr.getAttributeName()));
          String actual = attributeValue.get("value").getAsString();
          
          Assert.assertEquals(expected, actual);
        }
        else if (attr.toDTO() instanceof AttributeBooleanType)
        {
          Assert.assertEquals(go.getServerObject().getValue(attr.getAttributeName()), attributeValue.get("value").getAsBoolean());
        }
        
        Assert.assertEquals("TEST_EXTERNAL_ID", attributeValue.get("attribute").getAsJsonObject().get("id").getAsString());
      }
    }
  }
  
//  @Test
//  @Request
//  public void testSerializeConfig()
//  {
//    SyncLevel level2 = new SyncLevel();
//    level2.setGeoObjectType(testData.GOT_CHAR.getServerObject().getCode());
//    level2.setSyncType(SyncLevel.Type.ALL);
//    level2.setLevel(2);
//    
//    SynchronizationConfig config = createSyncConfig(level2);
//    
//    JsonObject json = config.toJSON();
//    
//    JsonObject dhis2Config = json.get(SynchronizationConfig.CONFIGURATION).getAsJsonObject();
//    
//    JsonArray levels = dhis2Config.get("levels").getAsJsonArray();
//    
//    Assert.assertEquals(2, levels.size());
//    
//    JsonObject level = levels.get(1).getAsJsonObject();
//    
//    JsonObject attributes = level.get("attributes").getAsJsonObject();
//    
//    Assert.assertEquals(1, attributes.size());
//  }
  
  @Test
  @Request
  public void testExportCharacterAttr() throws Exception
  {
    System.out.println("Starting Character Test");
    exportCustomAttribute(testData.GOT_CHAR, testData.GO_CHAR, testData.AT_GO_CHAR);
  }
  
  @Test
  @Request
  public void testExportIntegerAttr() throws Exception
  {
    System.out.println("Starting Integer Test");
    exportCustomAttribute(testData.GOT_INT, testData.GO_INT, testData.AT_GO_INT);
  }
  
  @Test
  @Request
  public void testExportFloatAttr() throws Exception
  {
    System.out.println("Starting float test");
    exportCustomAttribute(testData.GOT_FLOAT, testData.GO_FLOAT, testData.AT_GO_FLOAT);
  }
  
  @Test
  @Request
  public void testExportDateAttr() throws Exception
  {
    System.out.println("Starting date test");
    exportCustomAttribute(testData.GOT_DATE, testData.GO_DATE, testData.AT_GO_DATE);
  }
  
  @Test
  @Request
  public void testExportBoolAttr() throws Exception
  {
    System.out.println("Starting boolean test");
    exportCustomAttribute(testData.GOT_BOOL, testData.GO_BOOL, testData.AT_GO_BOOL);
  }
}
