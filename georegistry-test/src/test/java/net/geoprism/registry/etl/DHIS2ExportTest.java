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
import java.util.List;
import java.util.Map;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.GsonBuilder;
import com.runwaysdk.session.Request;

import net.geoprism.dhis2.dhis2adapter.DHIS2Facade;
import net.geoprism.dhis2.dhis2adapter.HTTPConnector;
import net.geoprism.dhis2.dhis2adapter.response.model.ValueType;
import net.geoprism.registry.Organization;
import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.etl.export.DataExportJob;
import net.geoprism.registry.graph.DHIS2ExternalSystem;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.SynchronizationConfigService;
import net.geoprism.registry.test.CustomAttributeDataset;
import net.geoprism.registry.test.TestDataSet;

public class DHIS2ExportTest
{
  protected static CustomAttributeDataset testData;
  
  protected SynchronizationConfigService syncService;
  
  protected ExternalSystem system;
  
  private DHIS2Facade dhis2;
  
  private static final String USERNAME = "admin";
  
  private static final String PASSWORD = "district";
  
  private static final String URL = "https://play.dhis2.org/2.31.9";
  
  private static final String VERSION = "31";
  
  private static TestGeoObjectInfo 
  
  @BeforeClass
  public static void setUpClass()
  {
    testData = CustomAttributeDataset.newTestData();
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
    
    system = createDhis2ExternalSystem();
    
    HTTPConnector connector = new HTTPConnector();
    connector.setCredentials(USERNAME, PASSWORD);
    connector.setServerUrl(URL);
    dhis2 = new DHIS2Facade(connector, VERSION);
    
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
  private SynchronizationConfig createSyncConfig()
  {
    // Define reusable objects
    final ServerGeoObjectType got = testData.STATE.getServerObject();
    final ServerHierarchyType ht = testData.HIER_ADMIN.getServerObject();
    final Organization org = testData.ORG_NPS.getServerObject();
    
    // Create DHIS2 Sync Config
    DHIS2SyncConfig dhis2Config = new DHIS2SyncConfig();
    dhis2Config.setHierarchy(ht);
    dhis2Config.setLabel(new LocalizedValue("DHIS2 Export Test Data"));
    dhis2Config.setOrganization(org);
    
    // Populate Levels
    List<SyncLevel> levels = new ArrayList<SyncLevel>();
    
    SyncLevel level = new SyncLevel();
    level.setGeoObjectType(got.getCode());
    level.setSyncType(SyncLevel.Type.ALL);
    level.setLevel(1);
    levels.add(level);
    
    dhis2Config.setLevels(levels);
    
    // Populate Attribute Mappings
    Map<String, DHIS2AttributeMapping> mappings = new HashMap<String, DHIS2AttributeMapping>();
    
    DHIS2AttributeMapping mapping = new DHIS2AttributeMapping();
    
    mapping.setDhis2ValueType(ValueType.TEXT);
    mapping.setRunwayAttributeId(testData.testChar.getServerObject().getOid());
    mappings.put(testData.testChar.getAttributeName(), mapping);
    
    dhis2Config.setAttributes(mappings);
    
    GsonBuilder builder = new GsonBuilder();
    String dhis2JsonConfig = builder.create().toJson(dhis2Config);
    
    SynchronizationConfig config = new SynchronizationConfig();
    config.setConfiguration(dhis2JsonConfig);
    config.setOrganization(org);
    config.setHierarchy(ht.getEntityRelationship());
    config.setSystem(this.system.getOid());
    config.apply();
    
    return config;
  }
  
  @Test
  @Request
  public void testExportWithCustomAttributes() throws Exception
  {
    SynchronizationConfig config = createSyncConfig();
    
    System.out.println(config.toJSON().toString());
    
    syncService.run(testData.adminSession.getSessionId(), config.getOid());
  }
}
