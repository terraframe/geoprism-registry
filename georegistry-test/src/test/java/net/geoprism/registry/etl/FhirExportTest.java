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

import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Type;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.GsonBuilder;
import com.runwaysdk.session.Request;
import com.vividsolutions.jts.io.geojson.GeoJsonWriter;

import ca.uhn.fhir.context.FhirContext;
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

public class FhirExportTest
{
  protected static USATestData           testData;

  protected static MasterList            multiHierarchyList;

  protected static MasterListVersion     multiHierarchyVersion;

  protected static MasterList            basicHierarchyList;

  protected static MasterListVersion     basicHierarchyVersion;

  protected SynchronizationConfigService syncService;

  @BeforeClass
  public static void setUpClass()
  {
    TestDataSet.deleteExternalSystems("FHIRExportTest");

    testData = USATestData.newTestData();
    testData.setUpMetadata();
    testData.setUpInstanceData();

    classSetupInRequest();
  }

  @Request
  public static void classSetupInRequest()
  {
    MasterListBuilder.Hierarchy admin = new MasterListBuilder.Hierarchy();
    admin.setType(USATestData.HIER_ADMIN);
    admin.setParents(USATestData.COUNTRY, USATestData.STATE, USATestData.DISTRICT);

    MasterListBuilder.Hierarchy reportsTo = new MasterListBuilder.Hierarchy();
    reportsTo.setType(USATestData.HIER_REPORTS_TO);
    reportsTo.setParents(USATestData.HEALTH_POST);

    MasterListBuilder multiHierarchyBuilder = new MasterListBuilder();
    multiHierarchyBuilder.setOrg(USATestData.ORG_NPS.getServerObject());
    multiHierarchyBuilder.setInfo(USATestData.HEALTH_STOP);
    multiHierarchyBuilder.setVisibility(MasterList.PUBLIC);
    multiHierarchyBuilder.setMaster(false);
    multiHierarchyBuilder.setHts(admin, reportsTo);
    multiHierarchyBuilder.setCode("TEST_MULTI");

    multiHierarchyList = multiHierarchyBuilder.build();

    multiHierarchyVersion = multiHierarchyList.getOrCreateVersion(new Date(), MasterListVersion.EXPLORATORY);
    multiHierarchyVersion.publishNoAuth();

    MasterListBuilder basicBuilder = new MasterListBuilder();
    basicBuilder.setOrg(USATestData.ORG_NPS.getServerObject());
    basicBuilder.setInfo(USATestData.HEALTH_STOP);
    basicBuilder.setVisibility(MasterList.PUBLIC);
    basicBuilder.setMaster(false);
    basicBuilder.setHts(admin);
    basicBuilder.setCode("TEST_BASIC");

    basicHierarchyList = basicBuilder.build();

    basicHierarchyVersion = basicHierarchyList.getOrCreateVersion(new Date(), MasterListVersion.EXPLORATORY);
    basicHierarchyVersion.publishNoAuth();
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
  public static SynchronizationConfig createSyncConfig(ExternalSystem system, MasterList list, MasterListVersion version)
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
  public void testExportMultiHierarchyList() throws InterruptedException
  {
    try
    {
      FhirExternalSystem system = createExternalSystem();

      SynchronizationConfig config = createSyncConfig(system, multiHierarchyList, multiHierarchyVersion);

      FhirExportSynchronizationManager manager = new FhirExportSynchronizationManager((FhirSyncExportConfig) config.buildConfiguration(), null);
      Bundle bundle = manager.generateBundle();

      // Assert bundle values
      List<BundleEntryComponent> entries = bundle.getEntry();

      Assert.assertEquals(8, entries.size());

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
      Bundle bundle = manager.generateBundle();

      // Assert bundle values
      List<BundleEntryComponent> entries = bundle.getEntry();

      Assert.assertEquals(8, entries.size());

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
