/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Common Geo Registry Adapter(tm).
 *
 * Common Geo Registry Adapter(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Common Geo Registry Adapter(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Common Geo Registry Adapter(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package org.commongeoregistry.adapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.commongeoregistry.adapter.action.AbstractActionDTO;
import org.commongeoregistry.adapter.action.geoobject.CreateGeoObjectActionDTO;
import org.commongeoregistry.adapter.action.geoobject.UpdateGeoObjectActionDTO;
import org.commongeoregistry.adapter.action.tree.AddChildActionDTO;
import org.commongeoregistry.adapter.action.tree.RemoveChildActionDTO;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.dataaccess.UnknownTermException;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.commongeoregistry.adapter.metadata.MetadataFactory;
import org.commongeoregistry.adapter.metadata.OrganizationDTO;
import org.commongeoregistry.adapter.metadata.RegistryRole;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.JsonObject;

public class SerializationTest
{
  @Test
  public void testGeoObjectOverTime()
  {
    RegistryAdapterServer registry = new RegistryAdapterServer(new MockIdService());

    MetadataFactory.newGeoObjectType("State", GeometryType.POLYGON, new LocalizedValue("State"), new LocalizedValue("State"), true, null, registry);

    String geom = "POLYGON ((10000 10000, 12300 40000, 16800 50000, 12354 60000, 13354 60000, 17800 50000, 13300 40000, 11000 10000, 10000 10000))";

    GeoObjectOverTime geoObject = registry.newGeoObjectOverTimeInstance("State");

    geoObject.setWKTGeometry(geom, null);
    geoObject.setCode("Colorado");
    geoObject.setUid("CO");
    geoObject.setDisplayLabel(new LocalizedValue("Colorado Display Label"), null, null);
    geoObject.setExists(true, null, null);

    String sJson = geoObject.toJSON().toString();
    GeoObjectOverTime geoObject2 = GeoObjectOverTime.fromJSON(registry, sJson);
    String sJson2 = geoObject2.toJSON().toString();

    Assert.assertEquals(sJson, sJson2);
    Assert.assertEquals("Colorado", geoObject2.getCode());
    Assert.assertEquals("CO", geoObject2.getUid());
    Assert.assertEquals("Colorado Display Label", geoObject2.getDisplayLabel(null).getValue());
    Assert.assertEquals("Colorado Display Label", geoObject2.getDisplayLabel(null).getValue(LocalizedValue.DEFAULT_LOCALE));
    Assert.assertEquals(geoObject.getExists(null), geoObject2.getExists(null));
  }

  @Test
  public void testTerm()
  {
    Term facilityType = new Term("FACILITY_TYPE", new LocalizedValue("Facility Type"), new LocalizedValue("..."));
    Term clinic = new Term("CLINIC", new LocalizedValue("Clinic"), new LocalizedValue("..."));
    Term matWard = new Term("MATERNITY_WARD", new LocalizedValue("Maternity Ward"), new LocalizedValue("..."));
    facilityType.addChild(clinic);
    facilityType.addChild(matWard);

    JsonObject jsonObject = facilityType.toJSON();

    Term facilityType2 = Term.fromJSON(jsonObject);

    Assert.assertEquals(facilityType.getCode(), facilityType2.getCode());
    Assert.assertEquals(facilityType.getLabel().getValue(), facilityType2.getLabel().getValue());
    Assert.assertEquals(facilityType.getDescription().getValue(), facilityType2.getDescription().getValue());

    Assert.assertEquals(facilityType.getChildren().size(), facilityType2.getChildren().size());
  }

  @Test
  public void testGeoObject()
  {
    RegistryAdapterServer registry = new RegistryAdapterServer(new MockIdService());

    MetadataFactory.newGeoObjectType("State", GeometryType.POLYGON, new LocalizedValue("State"), new LocalizedValue("State"), true, null, registry);

    String geom = "POLYGON ((10000 10000, 12300 40000, 16800 50000, 12354 60000, 13354 60000, 17800 50000, 13300 40000, 11000 10000, 10000 10000))";

    GeoObject geoObject = registry.newGeoObjectInstance("State");

    geoObject.setWKTGeometry(geom);
    geoObject.setCode("Colorado");
    geoObject.setUid("CO");
    geoObject.getDisplayLabel().setValue("Colorado Display Label");
    geoObject.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "Colorado Display Label");

    String sJson = geoObject.toJSON().toString();
    GeoObject geoObject2 = GeoObject.fromJSON(registry, sJson);
    String sJson2 = geoObject2.toJSON().toString();

    Assert.assertEquals(sJson, sJson2);
    Assert.assertEquals("Colorado", geoObject2.getCode());
    Assert.assertEquals("CO", geoObject2.getUid());
    Assert.assertEquals("Colorado Display Label", geoObject2.getLocalizedDisplayLabel());
    Assert.assertEquals("Colorado Display Label", geoObject2.getDisplayLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
    Assert.assertEquals(geoObject.getExists(), geoObject2.getExists());
  }

  @Test
  public void testLocalizedValue()
  {
    LocalizedValue label = new LocalizedValue("State");
    label.setValue(Locale.ENGLISH, "english");
    label.setValue(Locale.US, "english_us_1");

    JsonObject json = label.toJSON();
    LocalizedValue actual = LocalizedValue.fromJSON(json);

    Assert.assertEquals("State", actual.getValue());
    Assert.assertEquals("english", actual.getValue(Locale.ENGLISH));
    Assert.assertEquals("english_us_1", actual.getValue(Locale.US));
  }

  /**
   * Tests to make sure optional values are allowed and handled properly.
   */
  @Test
  public void testOptionalGeoObject()
  {
    RegistryAdapterServer registry = new RegistryAdapterServer(new MockIdService());

    MetadataFactory.newGeoObjectType("State", GeometryType.POLYGON, new LocalizedValue("State"), new LocalizedValue("State"), true, null, registry);

    GeoObject geoObject = registry.newGeoObjectInstance("State");

    String sJson = geoObject.toJSON().toString();
    GeoObject geoObject2 = GeoObject.fromJSON(registry, sJson);
    String sJson2 = geoObject2.toJSON().toString();

    Assert.assertEquals(sJson, sJson2);
  }

  @Test
  public void testOrganizationSerialization()
  {
    RegistryAdapterServer registryServerInterface = new RegistryAdapterServer(new MockIdService());

    String code = "MOH";

    OrganizationDTO orgOriginal = MetadataFactory.newOrganization(code, new LocalizedValue("Ministry of Health"), new LocalizedValue("Contact Joe at 555-555-5555"), registryServerInterface);

    JsonObject orgJSON = orgOriginal.toJSON();
    String sJson = orgJSON.toString();

    OrganizationDTO orgNew = OrganizationDTO.fromJSON(sJson);
    String sJson2 = orgNew.toJSON().toString();

    try
    {
      Assert.assertEquals(orgOriginal.getCode(), orgNew.getCode());
      Assert.assertEquals(orgOriginal.getLabel().getValue(), orgNew.getLabel().getValue());
      Assert.assertEquals(orgOriginal.getContactInfo().getValue(), orgNew.getContactInfo().getValue());

      Assert.assertEquals(sJson, sJson2);
    }
    finally
    {
      registryServerInterface.getMetadataCache().removeOrganization(orgOriginal.getCode());
    }
  }

  @Test
  public void testRoleSerialization()
  {
    // SRA
    RegistryRole sra1 = RegistryRole.createSRA(new LocalizedValue("Super Registry Administrator"));
    sra1.setAssigned(true);
    String sraJSON1 = sra1.toJSON().toString();

    RegistryRole sra2 = RegistryRole.fromJSON(sraJSON1);
    String sraJSON2 = sra2.toJSON().toString();

    Assert.assertEquals(sraJSON1, sraJSON2);

    // RA
    RegistryRole ra1 = RegistryRole.createRA(new LocalizedValue("Super Registry Administrator"), "MOH");
    String raJSON1 = ra1.toJSON().toString();

    RegistryRole ra2 = RegistryRole.fromJSON(raJSON1);
    String raJSON2 = ra2.toJSON().toString();

    Assert.assertEquals(raJSON1, raJSON2);

    // RM
    RegistryRole rm1 = RegistryRole.createRM(new LocalizedValue("Super Registry Administrator"), "MOH", "Village");
    rm1.setOrganizationLabel("DEFAULT", "Ministry of Health");
    rm1.setGeoObjectTypeLabel("DEFAULT", "Village");
    String rmJSON1 = rm1.toJSON().toString();

    RegistryRole rm2 = RegistryRole.fromJSON(rmJSON1);
    String rmJSON2 = rm2.toJSON().toString();

    Assert.assertEquals(rmJSON1, rmJSON2);

    // RC
    RegistryRole rc1 = RegistryRole.createRC(new LocalizedValue("Super Registry Administrator"), "MOH", "Village");
    String rcJSON1 = rc1.toJSON().toString();

    RegistryRole rc2 = RegistryRole.fromJSON(rcJSON1);
    String rcJSON2 = rc2.toJSON().toString();

    Assert.assertEquals(rcJSON1, rcJSON2);
  }

  @Test
  public void testGeoObjectType()
  {
    RegistryAdapterServer registry = new RegistryAdapterServer(new MockIdService());

    GeoObjectType state = MetadataFactory.newGeoObjectType("State", GeometryType.POLYGON, new LocalizedValue("State"), new LocalizedValue("State"), true, null, registry);

    String sJson = state.toJSON().toString();

    GeoObjectType state2 = GeoObjectType.fromJSON(sJson, registry);
    String sJson2 = state2.toJSON().toString();

    Assert.assertEquals(sJson, sJson2);
  }

  @Test
  public void testGeoObjectTypeOrganization()
  {
    OrganizationDTO orgOriginal = null;
    GeoObjectType state = null;

    String code = "MOH";
    String parentCode = "PARENT_CODE";

    RegistryAdapterServer registryServerInterface = new RegistryAdapterServer(new MockIdService());

    try
    {
      orgOriginal = MetadataFactory.newOrganization(code, new LocalizedValue("Ministry of Health"), new LocalizedValue("Contact Joe at 555-555-5555"), registryServerInterface);

      state = MetadataFactory.newGeoObjectType("State", GeometryType.POLYGON, new LocalizedValue("State"), new LocalizedValue("State"), true, orgOriginal.getCode(), registryServerInterface);
      state.setIsAbstract(true);
      state.setSuperTypeCode(parentCode);

      String sJson = state.toJSON().toString();

      GeoObjectType state2 = GeoObjectType.fromJSON(sJson, registryServerInterface);
      String sJson2 = state2.toJSON().toString();

      Assert.assertEquals(sJson, sJson2);
      Assert.assertEquals(code, state2.getOrganizationCode());
      Assert.assertEquals(parentCode, state2.getSuperTypeCode());
      Assert.assertTrue(state2.getIsAbstract());
    }
    finally
    {
      if (state != null)
      {
        registryServerInterface.getMetadataCache().removeGeoObjectType(state.getCode());
      }

      if (orgOriginal != null)
      {
        registryServerInterface.getMetadataCache().removeOrganization(orgOriginal.getCode());
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGeoObjectCustomAttributes()
  {
    RegistryAdapterServer registry = new RegistryAdapterServer(new MockIdService());

    GeoObjectType state = MetadataFactory.newGeoObjectType("State", GeometryType.POLYGON, new LocalizedValue("State"), new LocalizedValue("State"), true, null, registry);
    
    Term testRoot = MetadataFactory.newTerm("testRoot", new LocalizedValue("testRoot"), new LocalizedValue("testRoot"), registry);
    Term testChild = MetadataFactory.newTerm("testChild", new LocalizedValue("testChild"), new LocalizedValue("testChild"), registry);
    testRoot.addChild(testChild);

    AttributeType testChar = AttributeType.factory("testChar", new LocalizedValue("testCharLocalName"), new LocalizedValue("testCharLocalDescrip"), AttributeCharacterType.TYPE, false, false, false);
    AttributeType testDate = AttributeType.factory("testDate", new LocalizedValue("testDateLocalName"), new LocalizedValue("testDateLocalDescrip"), AttributeDateType.TYPE, false, false, false);
    AttributeType testInteger = AttributeType.factory("testInteger", new LocalizedValue("testIntegerLocalName"), new LocalizedValue("testIntegerLocalDescrip"), AttributeIntegerType.TYPE, false, false, false);
    AttributeType testBoolean = AttributeType.factory("testBoolean", new LocalizedValue("testBooleanName"), new LocalizedValue("testBooleanDescrip"), AttributeBooleanType.TYPE, false, false, false);
    AttributeTermType testTerm = (AttributeTermType) AttributeType.factory("testTerm", new LocalizedValue("testTermLocalName"), new LocalizedValue("testTermLocalDescrip"), AttributeTermType.TYPE, false, false, false);
    testTerm.setRootTerm(testRoot);
    
    AttributeClassificationType testClassification = (AttributeClassificationType) AttributeType.factory("testClassification", new LocalizedValue("testClassificationLocalName"), new LocalizedValue("testClassificationLocalDescrip"), AttributeClassificationType.TYPE, false, false, false);
    testClassification.setClassificationType("test.classification.Test");
    testClassification.setRootTerm(testRoot);

    state.addAttribute(testChar);
    state.addAttribute(testDate);
    state.addAttribute(testInteger);
    state.addAttribute(testBoolean);
    state.addAttribute(testTerm);
    state.addAttribute(testClassification);

    String geom = "POLYGON ((10000 10000, 12300 40000, 16800 50000, 12354 60000, 13354 60000, 17800 50000, 13300 40000, 11000 10000, 10000 10000))";

    GeoObject geoObject = registry.newGeoObjectInstance("State");

    geoObject.setWKTGeometry(geom);
    geoObject.setCode("Colorado");
    geoObject.setUid("CO");

    geoObject.setValue("testChar", "Test Character Value");
    geoObject.setValue("testDate", new Date());
    geoObject.setValue("testInteger", 3L);
    geoObject.setValue("testBoolean", false);
    geoObject.setValue("testTerm", testChild);
    geoObject.setValue("testClassification", testChild);

    String sJson = geoObject.toJSON().toString();
    GeoObject geoObject2 = GeoObject.fromJSON(registry, sJson);
    String sJson2 = geoObject2.toJSON().toString();

    Assert.assertEquals(sJson, sJson2);
    Assert.assertEquals(geoObject.getValue("testChar"), geoObject2.getValue("testChar"));
    Assert.assertEquals(geoObject.getValue("testDate"), geoObject2.getValue("testDate"));
    Assert.assertEquals(geoObject.getValue("testInteger"), geoObject2.getValue("testInteger"));
    Assert.assertEquals(geoObject.getValue("testBoolean"), geoObject2.getValue("testBoolean"));
    Assert.assertEquals(geoObject.getValue("testClassification"), geoObject2.getValue("testClassification"));

    Assert.assertEquals( ( (Iterator<String>) geoObject.getValue("testTerm") ).next(), ( (Iterator<String>) geoObject2.getValue("testTerm") ).next());
  }

  @Test(expected = UnknownTermException.class)
  public void testGeoObjectBadTerm()
  {
    RegistryAdapterServer registryServerInterface = new RegistryAdapterServer(new MockIdService());

    GeoObjectType state = MetadataFactory.newGeoObjectType("State", GeometryType.POLYGON, new LocalizedValue("State"), new LocalizedValue("State"), true, null, registryServerInterface);
    
    Term testRoot = MetadataFactory.newTerm("testRoot", new LocalizedValue("testRoot"), new LocalizedValue("testRoot"), registryServerInterface);
    Term testChild = MetadataFactory.newTerm("testChild", new LocalizedValue("testChild"), new LocalizedValue("testChild"), registryServerInterface);
    testRoot.addChild(testChild);

    AttributeTermType testTerm = (AttributeTermType) AttributeType.factory("testTerm", new LocalizedValue("testTermLocalName"), new LocalizedValue("testTermLocalDescrip"), AttributeTermType.TYPE, false, false, false);
    testTerm.setRootTerm(testRoot);

    state.addAttribute(testTerm);

    String geom = "POLYGON ((10000 10000, 12300 40000, 16800 50000, 12354 60000, 13354 60000, 17800 50000, 13300 40000, 11000 10000, 10000 10000))";

    GeoObject geoObject = registryServerInterface.newGeoObjectInstance("State");

    geoObject.setWKTGeometry(geom);
    geoObject.setCode("Colorado");
    geoObject.setUid("CO");
    geoObject.setValue("testTerm", "Bad");
  }

  /**
   * Tests to make sure that custom attributes can be added to GeoObjectTypes,
   * and also that they are serialized correctly.
   */
  @Test
  public void testGeoObjectTypeCustomAttributes()
  {
    RegistryAdapterServer registry = new RegistryAdapterServer(new MockIdService());

    GeoObjectType state = MetadataFactory.newGeoObjectType("State", GeometryType.POLYGON, new LocalizedValue("State"), new LocalizedValue("State"), true, null, registry);
    Term testRoot = MetadataFactory.newTerm("testRoot", new LocalizedValue("testRoot"), new LocalizedValue("testRoot"), registry);

    AttributeType testChar = AttributeType.factory("testChar", new LocalizedValue("testCharLocalName"), new LocalizedValue("testCharLocalDescrip"), AttributeCharacterType.TYPE, true, true, false);
    AttributeType testDate = AttributeType.factory("testDate", new LocalizedValue("testDateLocalName"), new LocalizedValue("testDateLocalDescrip"), AttributeDateType.TYPE, false, false, false);
    AttributeType testInteger = AttributeType.factory("testInteger", new LocalizedValue("testIntegerLocalName"), new LocalizedValue("testDateLocalDescrip"), AttributeIntegerType.TYPE, false, false, false);
    AttributeType testTerm = AttributeType.factory("testTerm", new LocalizedValue("testTermLocalName"), new LocalizedValue("testTermLocalDescrip"), AttributeTermType.TYPE, false, false, false);
    AttributeFloatType testFloat = (AttributeFloatType) AttributeType.factory("testFloat", new LocalizedValue("testFloatLocalName"), new LocalizedValue("testFloatLocalDescrip"), AttributeFloatType.TYPE, false, false, false);
    testFloat.setPrecision(20);
    testFloat.setScale(10);

    AttributeClassificationType testClassification = (AttributeClassificationType) AttributeType.factory("testClassification", new LocalizedValue("testClassificationLocalName"), new LocalizedValue("testClassificationLocalDescrip"), AttributeClassificationType.TYPE, false, false, false);
    testClassification.setClassificationType("test.classification.Test");
    testClassification.setRootTerm(testRoot);

    state.addAttribute(testChar);
    state.addAttribute(testDate);
    state.addAttribute(testInteger);
    state.addAttribute(testFloat);
    state.addAttribute(testTerm);
    state.addAttribute(testClassification);

    String sJson = state.toJSON().toString();
    GeoObjectType state2 = GeoObjectType.fromJSON(sJson, registry);
    String sJson2 = state2.toJSON().toString();

    Assert.assertEquals(sJson, sJson2);

    AttributeType attribute = state2.getAttribute("testChar").get();

    Assert.assertEquals(testChar.getName(), attribute.getName());
    Assert.assertEquals(testChar.isRequired(), attribute.isRequired());
    Assert.assertEquals(testChar.isUnique(), attribute.isUnique());
    Assert.assertEquals(testChar.getDescription().getValue(), attribute.getDescription().getValue());
    Assert.assertEquals(testChar.getLabel().getValue(), attribute.getLabel().getValue());
    Assert.assertEquals(testChar.getIsDefault(), attribute.getIsDefault());

    AttributeFloatType attributeFloat = (AttributeFloatType) state2.getAttribute(testFloat.getName()).get();

    Assert.assertEquals(testFloat.getPrecision(), attributeFloat.getPrecision());
    Assert.assertEquals(testFloat.getScale(), attributeFloat.getScale());

    Assert.assertEquals(testDate.getName(), state2.getAttribute("testDate").get().getName());
    Assert.assertEquals(testInteger.getName(), state2.getAttribute("testInteger").get().getName());

    AttributeClassificationType attributeClassification = (AttributeClassificationType) state2.getAttribute(testClassification.getName()).get();

    Assert.assertEquals(testClassification.getClassificationType(), attributeClassification.getClassificationType());
    Assert.assertEquals(testClassification.getRootTerm().getCode(), attributeClassification.getRootTerm().getCode());
  }

  @Test
  public void testHierarchyType()
  {
    RegistryAdapterServer registry = new RegistryAdapterServer(new MockIdService());

    TestFixture.defineExampleHierarchies(registry);

    HierarchyType geoPolitical = registry.getMetadataCache().getHierachyType(TestFixture.GEOPOLITICAL).get();

    String geoPoliticalJson = geoPolitical.toJSON().toString();
    HierarchyType geoPolitical2 = HierarchyType.fromJSON(geoPoliticalJson, registry);
    String geoPoliticalJson2 = geoPolitical2.toJSON().toString();

    Assert.assertEquals(geoPoliticalJson, geoPoliticalJson2);
    Assert.assertEquals(geoPolitical.getCode(), geoPolitical2.getCode());
    Assert.assertEquals(geoPolitical.getAccessConstraints(), geoPolitical2.getAccessConstraints());
    Assert.assertEquals(geoPolitical.getUseConstraints(), geoPolitical2.getUseConstraints());
    Assert.assertEquals(geoPolitical.getAcknowledgement(), geoPolitical2.getAcknowledgement());
    Assert.assertEquals(geoPolitical.getDisclaimer(), geoPolitical2.getDisclaimer());
    Assert.assertEquals(geoPolitical.getContact(), geoPolitical2.getContact());
    Assert.assertEquals(geoPolitical.getPhoneNumber(), geoPolitical2.getPhoneNumber());
    Assert.assertEquals(geoPolitical.getDescription().getValue(), geoPolitical2.getDescription().getValue());
    Assert.assertEquals(geoPolitical.getLabel().getValue(), geoPolitical2.getLabel().getValue());
    Assert.assertEquals(geoPolitical.getRootGeoObjectTypes().size(), geoPolitical2.getRootGeoObjectTypes().size());
    Assert.assertEquals(geoPolitical.getRootGeoObjectTypes().get(0).getChildren().size(), geoPolitical2.getRootGeoObjectTypes().get(0).getChildren().size());
  }

  @Test
  public void testHierarchyTypeOrganization()
  {
    OrganizationDTO orgOriginal = null;
    HierarchyType hierarchyType = null;

    String code = "MOH";

    RegistryAdapterServer registryServerInterface = new RegistryAdapterServer(new MockIdService());

    try
    {
      orgOriginal = MetadataFactory.newOrganization(code, new LocalizedValue("Ministry of Health"), new LocalizedValue("Contact Joe at 555-555-5555"), registryServerInterface);

      hierarchyType = MetadataFactory.newHierarchyType("Admin", new LocalizedValue("Administration"), new LocalizedValue("Administration"), orgOriginal.getCode(), registryServerInterface);

      String sJson = hierarchyType.toJSON().toString();

      HierarchyType hierarchyType2 = HierarchyType.fromJSON(sJson, registryServerInterface);
      String sJson2 = hierarchyType2.toJSON().toString();

      Assert.assertEquals(sJson, sJson2);
      Assert.assertEquals(code, hierarchyType2.getOrganizationCode());
    }
    finally
    {
      if (hierarchyType != null)
      {
        registryServerInterface.getMetadataCache().removeHierarchyType(hierarchyType.getCode());
      }

      if (orgOriginal != null)
      {
        registryServerInterface.getMetadataCache().removeOrganization(orgOriginal.getCode());
      }
    }
  }

  @Test
  public void testChildTreeNode()
  {
    RegistryAdapterServer registry = new RegistryAdapterServer(new MockIdService());

    TestFixture.defineExampleHierarchies(registry);
    HierarchyType geoPolitical = registry.getMetadataCache().getHierachyType(TestFixture.GEOPOLITICAL).get();

    GeoObject pOne = registry.newGeoObjectInstance(TestFixture.PROVINCE);
    pOne.setCode("pOne");
    pOne.setUid("pOne");
    ChildTreeNode ptOne = new ChildTreeNode(pOne, geoPolitical);

    GeoObject dOne = registry.newGeoObjectInstance(TestFixture.DISTRICT);
    dOne.setCode("dOne");
    dOne.setUid("dOne");
    ChildTreeNode dtOne = new ChildTreeNode(dOne, geoPolitical);
    ptOne.addChild(dtOne);

    GeoObject cOne = registry.newGeoObjectInstance(TestFixture.COMMUNE);
    cOne.setCode("cOne");
    cOne.setUid("cOne");
    ChildTreeNode ctOne = new ChildTreeNode(cOne, geoPolitical);
    dtOne.addChild(ctOne);

    GeoObject dTwo = registry.newGeoObjectInstance(TestFixture.DISTRICT);
    dTwo.setCode("dTwo");
    dTwo.setUid("dTwo");
    ChildTreeNode dtTwo = new ChildTreeNode(dTwo, geoPolitical);
    ptOne.addChild(dtTwo);

    GeoObject cTwo = registry.newGeoObjectInstance(TestFixture.COMMUNE);
    cTwo.setCode("cTwo");
    cTwo.setUid("cTwo");
    ChildTreeNode ctTwo = new ChildTreeNode(cTwo, geoPolitical);
    ptOne.addChild(ctTwo);

    String ptOneJson = ptOne.toJSON().toString();
    ChildTreeNode ptOne2 = ChildTreeNode.fromJSON(ptOneJson, registry);

    String ptOne2Json = ptOne2.toJSON().toString();

    Assert.assertEquals(ptOneJson, ptOne2Json);
    Assert.assertEquals(ptOne.getChildren().size(), ptOne2.getChildren().size());
    Assert.assertEquals(ptOne.getChildren().get(0).getChildren().size(), ptOne2.getChildren().get(0).getChildren().size());
    Assert.assertEquals(ptOne.getChildren().get(0).getChildren().get(0).getChildren().size(), ptOne2.getChildren().get(0).getChildren().get(0).getChildren().size());
    Assert.assertEquals(ptOne.getHierachyType(), ptOne2.getHierachyType());
    Assert.assertEquals(ptOne.getChildren().get(0).getHierachyType(), ptOne2.getChildren().get(0).getHierachyType());
  }

  @Test
  public void testParentTreeNode()
  {
    RegistryAdapterServer registry = new RegistryAdapterServer(new MockIdService());

    TestFixture.defineExampleHierarchies(registry);
    HierarchyType geoPolitical = registry.getMetadataCache().getHierachyType(TestFixture.GEOPOLITICAL).get();

    GeoObject cOne = registry.newGeoObjectInstance(TestFixture.COMMUNE);
    cOne.setCode("cOne");
    cOne.setUid("cOne");
    ParentTreeNode ctOne = new ParentTreeNode(cOne, geoPolitical);

    GeoObject dOne = registry.newGeoObjectInstance(TestFixture.DISTRICT);
    dOne.setCode("dOne");
    dOne.setUid("dOne");
    ParentTreeNode dtOne = new ParentTreeNode(dOne, geoPolitical);
    ctOne.addParent(dtOne);

    GeoObject dTwo = registry.newGeoObjectInstance(TestFixture.DISTRICT);
    dTwo.setCode("dTwo");
    dTwo.setUid("dTwo");
    ParentTreeNode dtTwo = new ParentTreeNode(dTwo, geoPolitical);
    ctOne.addParent(dtTwo);

    GeoObject pOne = registry.newGeoObjectInstance(TestFixture.PROVINCE);
    pOne.setCode("pOne");
    pOne.setUid("pOne");
    ParentTreeNode ptOne = new ParentTreeNode(pOne, geoPolitical);
    dtOne.addParent(ptOne);

    GeoObject pTwo = registry.newGeoObjectInstance(TestFixture.PROVINCE);
    pTwo.setCode("pTwo");
    pTwo.setUid("pTwo");
    ParentTreeNode ptTwo = new ParentTreeNode(pTwo, geoPolitical);
    dtTwo.addParent(ptTwo);

    String ctOneJson = ctOne.toJSON().toString();
    ParentTreeNode ctOne2 = ParentTreeNode.fromJSON(ctOneJson, registry);

    String ctOne2Json = ctOne2.toJSON().toString();

    Assert.assertEquals(ctOneJson, ctOne2Json);
    Assert.assertEquals(ctOne.getParents().size(), ctOne2.getParents().size());
    Assert.assertEquals(ctOne.getParents().get(0).getParents().size(), ctOne2.getParents().get(0).getParents().size());
    Assert.assertEquals(ctOne.getParents().get(0).getParents().get(0).getParents().size(), ctOne2.getParents().get(0).getParents().get(0).getParents().size());
    Assert.assertEquals(ctOne.getHierachyType(), ctOne2.getHierachyType());
    Assert.assertEquals(ctOne.getParents().get(0).getHierachyType(), ctOne2.getParents().get(0).getHierachyType());
  }

  @Test
  public void testActions()
  {
    RegistryAdapterServer registry = new RegistryAdapterServer(new MockIdService());
    TestFixture.defineExampleHierarchies(registry);
    GeoObject geoObj1 = TestFixture.createGeoObject(registry, "PROV_ONE", TestFixture.PROVINCE);
    GeoObject geoObj2 = TestFixture.createGeoObject(registry, "DIST_ONE", TestFixture.DISTRICT);
    GeoObjectType province = geoObj1.getType();
    HierarchyType geoPolitical = registry.getMetadataCache().getHierachyType(TestFixture.GEOPOLITICAL).get();

    List<AbstractActionDTO> actions = new ArrayList<AbstractActionDTO>();

    /*
     * Add Child
     */
    AddChildActionDTO addChild = new AddChildActionDTO();
    addChild.setParentCode(geoObj1.getCode());
    addChild.setParentTypeCode(geoObj1.getType().getCode());
    addChild.setChildCode(geoObj2.getCode());
    addChild.setChildTypeCode(geoObj2.getType().getCode());
    addChild.setHierarchyCode(geoPolitical.getCode());
    addChild.setStartDate(new Date());
    addChild.setEndDate(new Date());

    String addChildJson = addChild.toJSON().toString();
    String addChildJson2 = AbstractActionDTO.parseAction(addChildJson).toJSON().toString();
    Assert.assertEquals(addChildJson, addChildJson2);
    actions.add(addChild);

    /*
     * Remove Child
     */
    RemoveChildActionDTO removeChild = new RemoveChildActionDTO();
    removeChild.setParentCode(geoObj1.getCode());
    removeChild.setParentTypeCode(geoObj1.getType().getCode());
    removeChild.setChildCode(geoObj2.getCode());
    removeChild.setChildTypeCode(geoObj2.getType().getCode());
    removeChild.setHierarchyCode(geoPolitical.getCode());
    removeChild.setStartDate(new Date());
    removeChild.setEndDate(new Date());

    String removeChildJson = removeChild.toJSON().toString();
    String removeChildJson2 = AbstractActionDTO.parseAction(removeChildJson).toJSON().toString();
    Assert.assertEquals(removeChildJson, removeChildJson2);
    actions.add(removeChild);

    /*
     * Create a GeoObject
     */
    CreateGeoObjectActionDTO create = new CreateGeoObjectActionDTO();
    create.setGeoObject(geoObj1.toJSON());

    String createJson = create.toJSON().toString();
    String createJson2 = AbstractActionDTO.parseAction(createJson).toJSON().toString();
    Assert.assertEquals(createJson, createJson2);
    actions.add(create);

    /*
     * Update a GeoObject
     */
    UpdateGeoObjectActionDTO update = new UpdateGeoObjectActionDTO();
    update.setGeoObject(geoObj1.toJSON());

    String updateJson = update.toJSON().toString();
    String updateJson2 = AbstractActionDTO.parseAction(updateJson).toJSON().toString();
    Assert.assertEquals(updateJson, updateJson2);
    actions.add(create);

    /*
     * Update a GeoObjectType
     */
    UpdateGeoObjectActionDTO createGOT = new UpdateGeoObjectActionDTO();
    createGOT.setGeoObject(province.toJSON());

    String createGOTJson = createGOT.toJSON().toString();
    String createGOTJson2 = AbstractActionDTO.parseAction(createGOTJson).toJSON().toString();
    Assert.assertEquals(createGOTJson, createGOTJson2);
    actions.add(createGOT);

    /*
     * Serialize the actions
     */
    String sActions = AbstractActionDTO.serializeActions(actions).toString();
    String sActions2 = AbstractActionDTO.serializeActions(AbstractActionDTO.parseActions(sActions)).toString();
    Assert.assertEquals(sActions, sActions2);

    System.out.println(sActions);
  }
}
