/**
 *
 */
package net.geoprism.registry.etl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.google.gson.GsonBuilder;
import com.runwaysdk.session.Request;

import ca.uhn.fhir.context.FhirContext;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.ListType;
import net.geoprism.registry.ListTypeBuilder;
import net.geoprism.registry.ListTypeEntry;
import net.geoprism.registry.ListTypeVersion;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.USADatasetTest;
import net.geoprism.registry.etl.fhir.BasicFhirConnection;
import net.geoprism.registry.etl.fhir.FhirExportSynchronizationManager;
import net.geoprism.registry.etl.fhir.MCSDFhirDataPopulator;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.graph.FhirExternalSystem;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.service.request.SynchronizationConfigService;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.USATestData;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class FhirExportTest extends USADatasetTest implements InstanceTestClassListener
{
  protected static ListType              multiHierarchyList;

  protected static ListTypeVersion       multiHierarchyVersion;

  protected static ListType              basicHierarchyList;

  protected static ListTypeVersion       basicHierarchyVersion;

  @Autowired
  protected SynchronizationConfigService syncService;

  @Override
  public void beforeClassSetup() throws Exception
  {
    TestDataSet.deleteExternalSystems("FHIRExportTest");

    super.beforeClassSetup();
    
    testData.setUpInstanceData();

    classSetupInRequest();
  }

  @Request
  public void classSetupInRequest() throws ParseException
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    Date date = format.parse("2020-04-05");

    ListTypeBuilder.Hierarchy admin = new ListTypeBuilder.Hierarchy();
    admin.setType(USATestData.HIER_ADMIN);
    admin.setParents(USATestData.COUNTRY, USATestData.STATE, USATestData.DISTRICT);

    ListTypeBuilder.Hierarchy reportsTo = new ListTypeBuilder.Hierarchy();
    reportsTo.setType(USATestData.HIER_REPORTS_TO);
    reportsTo.setParents(USATestData.HEALTH_POST);

    ListTypeBuilder multiHierarchyBuilder = new ListTypeBuilder();
    multiHierarchyBuilder.setOrg(USATestData.ORG_NPS.getServerObject());
    multiHierarchyBuilder.setInfo(USATestData.HEALTH_STOP);
    multiHierarchyBuilder.setHts(admin, reportsTo);
    multiHierarchyBuilder.setCode("TEST_MULTI");

    multiHierarchyList = multiHierarchyBuilder.build();

    ListTypeEntry multiHierarchyEntry = multiHierarchyList.getOrCreateEntry(date, null);
    multiHierarchyVersion = multiHierarchyEntry.getWorking();
    multiHierarchyVersion.publishNoAuth();

    ListTypeBuilder basicBuilder = new ListTypeBuilder();
    basicBuilder.setOrg(USATestData.ORG_NPS.getServerObject());
    basicBuilder.setInfo(USATestData.HEALTH_STOP);
    basicBuilder.setHts(admin);
    basicBuilder.setCode("TEST_BASIC");

    basicHierarchyList = basicBuilder.build();

    ListTypeEntry basicHierarchyEntry = basicHierarchyList.getOrCreateEntry(date, null);
    basicHierarchyVersion = basicHierarchyEntry.getWorking();
    basicHierarchyVersion.publishNoAuth();
  }

  @Override
  public void afterClassSetup() throws Exception
  {
    cleanUpClassInRequest();

    super.afterClassSetup();
  }

  @Request
  public void cleanUpClassInRequest()
  {
    if (multiHierarchyList != null)
    {
      multiHierarchyList.delete();
    }

    if (basicHierarchyList != null)
    {
      basicHierarchyList.delete();
    }
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
  public static SynchronizationConfig createSyncConfig(ExternalSystem system, ListType list, ListTypeVersion version)
  {
    // Define reusable objects
    final ServerHierarchyType ht = USATestData.HIER_ADMIN.getServerObject();
    final ServerOrganization org = USATestData.ORG_NPS.getServerObject();

    // Create the FHIR Sync Config
    FhirSyncExportConfig sourceConfig = new FhirSyncExportConfig();
    sourceConfig.setLabel(new LocalizedValue("FHIR Export Test Data"));
    sourceConfig.setOrganization(org);

    // Populate Levels
    SortedSet<FhirSyncLevel> levels = new TreeSet<FhirSyncLevel>();

    FhirSyncLevel level = new FhirSyncLevel();
    level.setLevel(0);
    level.setMasterListId(list.getOid());
    level.setVersionId(version.getOid());
    level.setImplementation(MCSDFhirDataPopulator.class.getName());
    levels.add(level);

    sourceConfig.setLevels(levels);

    // Serialize the FHIR Config
    GsonBuilder builder = new GsonBuilder();
    String fhirExportJsonConfig = builder.create().toJson(sourceConfig);

    // Create a SynchronizationConfig
    SynchronizationConfig config = new SynchronizationConfig();
    config.setConfiguration(fhirExportJsonConfig);
    config.setOrganization(org);
    config.setGraphHierarchy(ht.getObject());
    config.setSystem(system.getOid());
    config.getLabel().setValue("FHIR Export Test");
    config.apply();

    return config;
  }

  @Request
  @Test
  public void testExportMultiHierarchyList() throws InterruptedException
  {
    try
    {
      FhirExternalSystem system = createExternalSystem();

      SynchronizationConfig config = createSyncConfig(system, multiHierarchyList, multiHierarchyVersion);

      FhirExportSynchronizationManager manager = new FhirExportSynchronizationManager((FhirSyncExportConfig) config.buildConfiguration(), null);
      Bundle bundle = manager.generateBundle(new BasicFhirConnection(system));

      // Assert bundle values
      List<BundleEntryComponent> entries = bundle.getEntry();

      Assert.assertEquals(4, entries.size());

      // Assert the organization entry
      BundleEntryComponent entry = entries.get(0);

      {
        Assert.assertEquals("Organization/" + USATestData.HS_TWO.getCode(), entry.getFullUrl());

        org.hl7.fhir.r4.model.Organization organization = (org.hl7.fhir.r4.model.Organization) entry.getResource();

        Assert.assertEquals("Organization/" + USATestData.HS_TWO.getCode(), organization.getId());
        Assert.assertEquals(USATestData.HS_TWO.getDisplayLabel(), organization.getName());

        List<Identifier> identifiers = organization.getIdentifier();

        Assert.assertEquals(1, identifiers.size());
        Assert.assertEquals(USATestData.HS_TWO.getCode(), identifiers.get(0).getValue());
        Assert.assertEquals(system.getSystem(), identifiers.get(0).getSystem());

        List<StringType> aliases = organization.getAlias();

        Assert.assertEquals(1, aliases.size());
        Assert.assertEquals(USATestData.HS_TWO.getDisplayLabel(), aliases.get(0).asStringValue());

        List<CanonicalType> profiles = organization.getMeta().getProfile();

        Assert.assertEquals(2, profiles.size());
        Assert.assertEquals("http://ihe.net/fhir/StructureDefinition/IHE.mCSD.Organization", profiles.get(0).getValue());
        Assert.assertEquals("http://ihe.net/fhir/StructureDefinition/IHE.mCSD.JurisdictionsOrganization", profiles.get(1).getValue());

        Reference partOf = organization.getPartOf();

        Assert.assertNull(partOf.getReference());

        List<Extension> extensions = organization.getExtensionsByUrl("http://ihe.net/fhir/StructureDefinition/IHE.mCSD.hierarchy.extension");

        Assert.assertEquals(2, extensions.size());

        Extension extension = extensions.get(0);

        Assert.assertEquals(USATestData.HIER_ADMIN.getDisplayLabel(), extension.castToCodeableConcept(extension.getExtensionByUrl("hierarchy-type").getValue()).getText());
        Assert.assertEquals("Organization/" + USATestData.CO_D_ONE.getCode(), extension.castToReference(extension.getExtensionByUrl("part-of").getValue()).getReference());

        extension = extensions.get(1);

        Assert.assertEquals(USATestData.HIER_REPORTS_TO.getDisplayLabel(), extension.castToCodeableConcept(extension.getExtensionByUrl("hierarchy-type").getValue()).getText());
        Assert.assertEquals("Organization/" + USATestData.HP_TWO.getCode(), extension.castToReference(extension.getExtensionByUrl("part-of").getValue()).getReference());

        System.out.println(FhirContext.forR4().newJsonParser().setPrettyPrint(true).encodeResourceToString(organization));
      }

      // Assert the corresponding location entry
      entry = entries.get(1);

      {
        Assert.assertEquals("Location/" + USATestData.HS_TWO.getCode(), entry.getFullUrl());

        org.hl7.fhir.r4.model.Location location = (org.hl7.fhir.r4.model.Location) entry.getResource();

        Assert.assertEquals("Location/" + USATestData.HS_TWO.getCode(), location.getId());
        Assert.assertEquals(USATestData.HS_TWO.getDisplayLabel(), location.getName());

        List<Identifier> identifiers = location.getIdentifier();

        Assert.assertEquals(1, identifiers.size());
        Assert.assertEquals(USATestData.HS_TWO.getCode(), identifiers.get(0).getValue());
        Assert.assertEquals(system.getSystem(), identifiers.get(0).getSystem());

        List<StringType> aliases = location.getAlias();

        Assert.assertEquals(1, aliases.size());
        Assert.assertEquals(USATestData.HS_TWO.getDisplayLabel(), aliases.get(0).asStringValue());

        List<CanonicalType> profiles = location.getMeta().getProfile();

        Assert.assertEquals(2, profiles.size());
        Assert.assertEquals("http://ihe.net/fhir/StructureDefinition/IHE.mCSD.Location", profiles.get(0).getValue());
        Assert.assertEquals("http://ihe.net/fhir/StructureDefinition/IHE.mCSD.JurisdictionLocation", profiles.get(1).getValue());

        Reference managingOrganization = location.getManagingOrganization();

        Assert.assertEquals("Organization/" + USATestData.HS_TWO.getCode(), managingOrganization.getReference());

        Extension extension = location.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/location-boundary-geojson");

        byte[] bytes = Base64.getDecoder().decode(extension.castToAttachment(extension.getValue()).getDataElement().asStringValue());

        Assert.assertEquals(new GeoJsonWriter().write(USATestData.HS_TWO.getGeometry()), new String(bytes));

        System.out.println(FhirContext.forR4().newJsonParser().setPrettyPrint(true).encodeResourceToString(location));
      }
    }
    finally
    {
      TestDataSet.deleteExternalSystems("FHIRExportTest");
    }
  }

  @Request
  @Test
  public void testExportBasicHierarchyList() throws InterruptedException
  {
    try
    {
      FhirExternalSystem system = createExternalSystem();

      SynchronizationConfig config = createSyncConfig(system, basicHierarchyList, basicHierarchyVersion);

      FhirExportSynchronizationManager manager = new FhirExportSynchronizationManager((FhirSyncExportConfig) config.buildConfiguration(), null);
      Bundle bundle = manager.generateBundle(new BasicFhirConnection(system));

      // Assert bundle values
      List<BundleEntryComponent> entries = bundle.getEntry();

      Assert.assertEquals(4, entries.size());

      // Assert the organization entry
      BundleEntryComponent entry = entries.get(0);

      {
        Assert.assertEquals("Organization/" + USATestData.HS_TWO.getCode(), entry.getFullUrl());

        org.hl7.fhir.r4.model.Organization organization = (org.hl7.fhir.r4.model.Organization) entry.getResource();

        Assert.assertEquals("Organization/" + USATestData.HS_TWO.getCode(), organization.getId());
        Assert.assertEquals(USATestData.HS_TWO.getDisplayLabel(), organization.getName());

        List<Identifier> identifiers = organization.getIdentifier();

        Assert.assertEquals(1, identifiers.size());
        Assert.assertEquals(USATestData.HS_TWO.getCode(), identifiers.get(0).getValue());
        Assert.assertEquals(system.getSystem(), identifiers.get(0).getSystem());

        List<StringType> aliases = organization.getAlias();

        Assert.assertEquals(1, aliases.size());
        Assert.assertEquals(USATestData.HS_TWO.getDisplayLabel(), aliases.get(0).asStringValue());

        List<CanonicalType> profiles = organization.getMeta().getProfile();

        Assert.assertEquals(2, profiles.size());
        Assert.assertEquals("http://ihe.net/fhir/StructureDefinition/IHE.mCSD.Organization", profiles.get(0).getValue());
        Assert.assertEquals("http://ihe.net/fhir/StructureDefinition/IHE.mCSD.JurisdictionsOrganization", profiles.get(1).getValue());

        Reference partOf = organization.getPartOf();

        Assert.assertEquals("Organization/" + USATestData.CO_D_ONE.getCode(), partOf.getReference());

        Assert.assertEquals(0, organization.getExtensionsByUrl("http://ihe.net/fhir/StructureDefinition/IHE.mCSD.hierarchy.extension").size());
      }

      // Assert the corresponding location entry
      entry = entries.get(1);

      {
        Assert.assertEquals("Location/" + USATestData.HS_TWO.getCode(), entry.getFullUrl());

        org.hl7.fhir.r4.model.Location location = (org.hl7.fhir.r4.model.Location) entry.getResource();

        Assert.assertEquals("Location/" + USATestData.HS_TWO.getCode(), location.getId());
        Assert.assertEquals(USATestData.HS_TWO.getDisplayLabel(), location.getName());

        List<Identifier> identifiers = location.getIdentifier();

        Assert.assertEquals(1, identifiers.size());
        Assert.assertEquals(USATestData.HS_TWO.getCode(), identifiers.get(0).getValue());
        Assert.assertEquals(system.getSystem(), identifiers.get(0).getSystem());

        List<StringType> aliases = location.getAlias();

        Assert.assertEquals(1, aliases.size());
        Assert.assertEquals(USATestData.HS_TWO.getDisplayLabel(), aliases.get(0).asStringValue());

        List<CanonicalType> profiles = location.getMeta().getProfile();

        Assert.assertEquals(2, profiles.size());
        Assert.assertEquals("http://ihe.net/fhir/StructureDefinition/IHE.mCSD.Location", profiles.get(0).getValue());
        Assert.assertEquals("http://ihe.net/fhir/StructureDefinition/IHE.mCSD.JurisdictionLocation", profiles.get(1).getValue());

        Reference managingOrganization = location.getManagingOrganization();

        Assert.assertEquals("Organization/" + USATestData.HS_TWO.getCode(), managingOrganization.getReference());

        Extension extension = location.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/location-boundary-geojson");

        byte[] bytes = Base64.getDecoder().decode(extension.castToAttachment(extension.getValue()).getDataElement().asStringValue());

        Assert.assertEquals(new GeoJsonWriter().write(USATestData.HS_TWO.getGeometry()), new String(bytes));
      }
    }
    finally
    {
      TestDataSet.deleteExternalSystems("FHIRExportTest");
    }
  }
}
