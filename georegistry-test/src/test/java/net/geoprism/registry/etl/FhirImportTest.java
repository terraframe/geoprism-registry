/**
 *
 */
package net.geoprism.registry.etl;

import java.util.Date;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Resource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.google.gson.GsonBuilder;
import com.runwaysdk.session.Request;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.USADatasetTest;
import net.geoprism.registry.etl.fhir.BasicFhirConnection;
import net.geoprism.registry.etl.fhir.BasicFhirResourceProcessor;
import net.geoprism.registry.etl.fhir.FhirFactory;
import net.geoprism.registry.etl.fhir.FhirResourceImporter;
import net.geoprism.registry.etl.fhir.FhirResourceProcessor;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.graph.FhirExternalSystem;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.service.request.SynchronizationConfigService;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.USATestData;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class FhirImportTest extends USADatasetTest implements InstanceTestClassListener
{
  @Autowired
  protected SynchronizationConfigService syncService;

  @Autowired
  protected GeoObjectBusinessServiceIF   objectService;

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
    final ServerOrganization org = USATestData.ORG_NPS.getServerObject();

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

      ServerGeoObjectIF geoobject = this.objectService.getGeoObjectByCode("USATestDataHsTwo", "USATestDataHealthStop");
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
