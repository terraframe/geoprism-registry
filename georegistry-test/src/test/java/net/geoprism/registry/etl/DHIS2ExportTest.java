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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import net.geoprism.registry.graph.DHIS2ExternalSystem;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.SynchronizationConfigService;
import net.geoprism.registry.test.CustomAttributeDataset;
import net.geoprism.registry.test.SchedulerTestUtils;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.TestGeoObjectTypeInfo;

public class DHIS2ExportTest
{
  protected static CustomAttributeDataset testData;
  
  protected SynchronizationConfigService syncService;
  
  protected ExternalSystem system;
  
  protected DHIS2TestService dhis2;
  
  @BeforeClass
  public static void setUpClass()
  {
    TestDataSet.deleteExternalSystems("DHIS2ExportTest");
    
    testData = CustomAttributeDataset.newTestData();
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
  }

  @After
  public void tearDown()
  {
    if (testData != null)
    {
      testData.tearDownInstanceData();
    }
    
    TestDataSet.deleteExternalSystems("DHIS2ExportTest");
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
  private SynchronizationConfig createSyncConfig(SyncLevel additionalLevel)
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
    List<SyncLevel> levels = new ArrayList<SyncLevel>();
    
    SyncLevel level = new SyncLevel();
    level.setGeoObjectType(testData.GOT_ALL.getServerObject().getCode());
    level.setSyncType(SyncLevel.Type.ALL);
    level.setLevel(1);
    levels.add(level);
    
    levels.add(additionalLevel);
    
    dhis2Config.setLevels(levels);
    
    // Populate Attribute Mappings
    Map<String, DHIS2AttributeMapping> mappings = new HashMap<String, DHIS2AttributeMapping>();
    
    DHIS2AttributeMapping mapping = new DHIS2AttributeMapping();
    
    mapping.setDhis2ValueType(ValueType.TEXT);
    mapping.setRunwayAttributeId(testData.AT_GO_CHAR.getServerObject().getOid());
    mappings.put(testData.AT_GO_CHAR.getAttributeName(), mapping);
    
    dhis2Config.setAttributes(mappings);
    
    GsonBuilder builder = new GsonBuilder();
    String dhis2JsonConfig = builder.create().toJson(dhis2Config);
    
    SynchronizationConfig config = new SynchronizationConfig();
    config.setConfiguration(dhis2JsonConfig);
    config.setOrganization(org);
    config.setHierarchy(ht.getEntityRelationship());
    config.setSystem(this.system.getOid());
    config.getLabel().setValue("DHIS2 Export Test");
    config.apply();
    
    return config;
  }
  
  private void exportCustomAttribute(TestGeoObjectTypeInfo got, TestGeoObjectInfo go) throws InterruptedException
  {
    SyncLevel level2 = new SyncLevel();
    level2.setGeoObjectType(got.getServerObject().getCode());
    level2.setSyncType(SyncLevel.Type.ALL);
    level2.setLevel(2);
    
    SynchronizationConfig config = createSyncConfig(level2);
    
    JsonObject jo = syncService.run(testData.adminSession.getSessionId(), config.getOid());
    ExportHistory hist = ExportHistory.get(jo.get("historyId").getAsString());
    
    SchedulerTestUtils.waitUntilStatus(hist, AllJobStatus.SUCCESS);
    
    LinkedList<Dhis2Payload> payloads = this.dhis2.getPayloads();
    Assert.assertEquals(2, payloads.size());
    
    for (int level = 0; level < payloads.size(); ++level)
    {
      Dhis2Payload payload = payloads.get(level);
      
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
      }
    }
  }
  
  @Test
  @Request
  public void testExportCharacterAttr() throws Exception
  {
    exportCustomAttribute(testData.GOT_CHAR, testData.GO_CHAR);
  }
}
