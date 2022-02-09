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

import java.util.Date;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Resource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.GsonBuilder;
import com.runwaysdk.session.Request;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import net.geoprism.registry.Organization;
import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.etl.fhir.BasicFhirConnection;
import net.geoprism.registry.etl.fhir.BasicFhirResourceProcessor;
import net.geoprism.registry.etl.fhir.FhirFactory;
import net.geoprism.registry.etl.fhir.FhirResourceImporter;
import net.geoprism.registry.etl.fhir.FhirResourceProcessor;
import net.geoprism.registry.geoobject.ServerGeoObjectService;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.graph.FhirExternalSystem;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.SynchronizationConfigService;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.USATestData;

public class FhirImportTest
{
  protected static USATestData           testData;

  protected SynchronizationConfigService syncService;

  @BeforeClass
  public static void setUpClass()
  {
    TestDataSet.deleteExternalSystems("FHIRImportTest");

    testData = USATestData.newTestData();
    testData.setUpMetadata();
    testData.setUpInstanceData();
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

    syncService = new SynchronizationConfigService();

    testData.logIn(USATestData.USER_NPS_RA);
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
    FhirExternalSystem system = new FhirExternalSystem();
    system.setId("FHIRImportTest");
    system.setOrganization(USATestData.ORG_NPS.getServerObject());
    system.getEmbeddedComponent(ExternalSystem.LABEL).setValue("defaultLocale", "Test");
    system.getEmbeddedComponent(ExternalSystem.DESCRIPTION).setValue("defaultLocale", "Test");
    system.setUrl("localhost:8080/fhir");
    system.setSystem("localhost");
    system.apply();

    return system;
  }

  @Request
  public static SynchronizationConfig createSyncConfig(ExternalSystem system)
  {
    // Define reusable objects
    final ServerHierarchyType ht = USATestData.HIER_ADMIN.getServerObject();
    final Organization org = USATestData.ORG_NPS.getServerObject();

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
    config.setHierarchy(ht.getMdTermRelationship());
    config.setSystem(system.getOid());
    config.getLabel().setValue("FHIR Import Test");
    config.setIsImport(true);
    config.apply();

    return config;
  }

  @Request
  @Test
  public void testBasicImport() throws InterruptedException
  {
    try
    {
      FhirExternalSystem system = createExternalSystem();

      SynchronizationConfig config = createSyncConfig(system);
      FhirSyncImportConfig iConfig = (FhirSyncImportConfig) config.buildConfiguration();

      FhirResourceProcessor processor = FhirFactory.getProcessor(iConfig.getImplementation());

      IParser parser = FhirContext.forR4().newJsonParser();

      Bundle bundle = new Bundle();
      bundle.addEntry(new BundleEntryComponent().setResource((Resource) parser.parseResource(this.getClass().getResourceAsStream("/fhir/organization.json"))));
      bundle.addEntry(new BundleEntryComponent().setResource((Resource) parser.parseResource(this.getClass().getResourceAsStream("/fhir/location.json"))));

      FhirResourceImporter importer = new FhirResourceImporter(new BasicFhirConnection(system), processor, null, null);
      importer.synchronize(bundle);

      ServerGeoObjectIF geoobject = new ServerGeoObjectService().getGeoObjectByCode("USATestDataHsTwo", "USATestDataHealthStop");
      geoobject.setDate(new Date());
      LocalizedValue displayLabel = geoobject.getDisplayLabel();

      Assert.assertEquals("USATestDataHsTwo ZZZZZZZ", displayLabel.getValue());
    }
    finally
    {
      TestDataSet.deleteExternalSystems("FHIRImportTest");
    }
  }
}
