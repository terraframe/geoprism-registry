/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.etl;

import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.GsonBuilder;
import com.runwaysdk.session.Request;

import net.geoprism.registry.MasterList;
import net.geoprism.registry.MasterListBuilder;
import net.geoprism.registry.MasterListVersion;
import net.geoprism.registry.Organization;
import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.etl.fhir.FhirExportSynchronizationManager;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.graph.FhirExternalSystem;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.SynchronizationConfigService;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.USATestData;

public class FHIRExportTest
{
  protected static USATestData           testData;

  protected static MasterList            list;

  protected static MasterListVersion     version;

  protected SynchronizationConfigService syncService;

  protected ExternalSystem               system;

  @BeforeClass
  public static void setUpClass()
  {
    TestDataSet.deleteExternalSystems("FHIRExportTest");

    testData = USATestData.newTestData();
    testData.setUpMetadata();

    classSetupInRequest();
  }

  @Request
  public static void classSetupInRequest()
  {
    MasterListBuilder builder = new MasterListBuilder();
    builder.setOrg(USATestData.ORG_NPS.getServerObject());
    builder.setHt(USATestData.HIER_ADMIN);
    builder.setInfo(USATestData.HEALTH_FACILITY);
    builder.setVisibility(MasterList.PUBLIC);
    builder.setMaster(false);
    builder.setParents(USATestData.COUNTRY, USATestData.STATE, USATestData.DISTRICT);
    builder.setSubtypeHierarchies(USATestData.HIER_REPORTS_TO);

    list = builder.build();

    version = list.getOrCreateVersion(new Date(), MasterListVersion.EXPLORATORY);
    version.publishNoAuth();
  }

  @AfterClass
  public static void cleanUpClass()
  {
    cleanUpClassInRequest();

    testData.tearDownMetadata();
  }

  @Request
  public static void cleanUpClassInRequest()
  {
    if (list != null)
    {
      list.delete();
    }
  }

  @Before
  public void setUp()
  {
    testData.setUpInstanceData();

    system = createExternalSystem();

    syncService = new SynchronizationConfigService();

    testData.logIn(USATestData.USER_NPS_RA);
  }

  @After
  public void tearDown()
  {
    testData.logOut();

    TestDataSet.deleteExternalSystems("FHIRExportTest");

    testData.tearDownInstanceData();
  }

  @Request
  private ExternalSystem createExternalSystem()
  {
    FhirExternalSystem system = new FhirExternalSystem();
    system.setId("FHIRExportTest");
    system.setOrganization(USATestData.ORG_NPS.getServerObject());
    system.getEmbeddedComponent(ExternalSystem.LABEL).setValue("defaultLocale", "Test");
    system.getEmbeddedComponent(ExternalSystem.DESCRIPTION).setValue("defaultLocale", "Test");
    system.setUrl("localhost:8080/fhir");
    system.setSystem("localhost");
    system.apply();

    return system;
  }

  @Request
  public static SynchronizationConfig createSyncConfig(ExternalSystem system, DHIS2SyncLevel additionalLevel)
  {
    // Define reusable objects
    final ServerHierarchyType ht = USATestData.HIER_ADMIN.getServerObject();
    final Organization org = USATestData.ORG_NPS.getServerObject();

    // Create DHIS2 Sync Config
    FhirSyncExportConfig sourceConfig = new FhirSyncExportConfig();
    sourceConfig.setLabel(new LocalizedValue("FHIR Export Test Data"));
    sourceConfig.setOrganization(org);

    // Populate Levels
    SortedSet<FhirSyncLevel> levels = new TreeSet<FhirSyncLevel>();

    FhirSyncLevel level = new FhirSyncLevel();
    level.setLevel(0);
    level.setMasterListId(list.getOid());
    level.setVersionId(version.getOid());
    levels.add(level);

    sourceConfig.setLevels(levels);

    // Serialize the FHIR Config
    GsonBuilder builder = new GsonBuilder();
    String fhirExportJsonConfig = builder.create().toJson(sourceConfig);

    // Create a SynchronizationConfig
    SynchronizationConfig config = new SynchronizationConfig();
    config.setConfiguration(fhirExportJsonConfig);
    config.setOrganization(org);
    config.setHierarchy(ht.getUniversalRelationship());
    config.setSystem(system.getOid());
    config.getLabel().setValue("FHIR Export Test");
    config.apply();

    return config;
  }

  @Request
  @Test
  public void testExportList() throws InterruptedException
  {
    SynchronizationConfig config = createSyncConfig(this.system, null);

    FhirExportSynchronizationManager manager = new FhirExportSynchronizationManager((FhirSyncExportConfig) config.buildConfiguration(), null);
    Bundle bundle = manager.generateBundle();

    // Assert bundle values
  }

}
