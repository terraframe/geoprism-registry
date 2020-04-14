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
package net.geoprism.registry.service;

import java.util.List;

import net.geoprism.ontology.Classifier;
import net.geoprism.ontology.ClassifierIsARelationship;
import net.geoprism.registry.Organization;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.conversion.TermConverter;
import net.geoprism.registry.graph.GeoVertexType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.test.TestGeoObjectTypeInfo;
import net.geoprism.registry.test.TestHierarchyTypeInfo;
import net.geoprism.registry.test.USATestData;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.RegistryAdapterServer;
import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.commongeoregistry.adapter.metadata.MetadataFactory;
import org.commongeoregistry.adapter.metadata.OrganizationDTO;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.constants.MdBusinessInfo;
import com.runwaysdk.dataaccess.DuplicateDataException;
import com.runwaysdk.dataaccess.MdAttributeBooleanDAOIF;
import com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeLongDAOIF;
import com.runwaysdk.dataaccess.MdAttributeMomentDAOIF;
import com.runwaysdk.dataaccess.MdAttributeTermDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.cache.DataNotFoundException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.gis.constants.GISConstants;
import com.runwaysdk.gis.constants.MdGeoVertexInfo;
import com.runwaysdk.gis.dataaccess.MdGeoVertexDAOIF;
import com.runwaysdk.gis.dataaccess.metadata.graph.MdGeoVertexDAO;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.AllowedIn;
import com.runwaysdk.system.gis.geo.LocatedIn;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdBusiness;

public class HierarchyManagementServiceTest
{

  public static RegistryAdapter        adapter                      = null;

  public static RegistryService        service                      = null;

  public static TestGeoObjectTypeInfo  COUNTRY;

  private static TestGeoObjectTypeInfo PROVINCE;

  private static TestGeoObjectTypeInfo DISTRICT;

  private static TestGeoObjectTypeInfo VILLAGE;

//  private static TestGeoObjectTypeInfo HOUSEHOLD;
//
//  private static TestGeoObjectTypeInfo RIVER;

  private static TestHierarchyTypeInfo REPORTING_DIVISION;

  private static TestHierarchyTypeInfo ADMINISTRATIVE_DIVISION;

  private final static String          ROOT_TEST_TERM_CLASSIFIER_ID = "TEST";

  private static String                ROOT_TEST_TERM_KEY           = null;

  private static String                ORG_MOI                      = "MOI";

  protected static USATestData         testData;

  @BeforeClass
  public static void setUpClass()
  {
    testData = USATestData.newTestDataForClass();
    testData.setUpMetadata();

    COUNTRY = testData.newTestGeoObjectTypeInfo("HMST_Country");
    PROVINCE = testData.newTestGeoObjectTypeInfo("HMST_Province");
    DISTRICT = testData.newTestGeoObjectTypeInfo("HMST_District");
    VILLAGE = testData.newTestGeoObjectTypeInfo("HMST_Village");
//    HOUSEHOLD = testData.newTestGeoObjectTypeInfo("HMST_Household");
//    RIVER = testData.newTestGeoObjectTypeInfo("HMST_River");

    ADMINISTRATIVE_DIVISION = testData.newTestHierarchyTypeInfo("HMST_AdminDiv");
    REPORTING_DIVISION = testData.newTestHierarchyTypeInfo("HMST_ReportDiv");
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

      for (TestGeoObjectTypeInfo got : testData.getManagedGeoObjectTypeExtras())
      {
        got.delete();
      }
    }

    setUpInRequest();
  }

  @After
  public void tearDown()
  {
    if (testData != null)
    {
      for (TestHierarchyTypeInfo ht : testData.getManagedHierarchyTypeExtras())
      {
        ht.delete();
      }

      testData.tearDownInstanceData();
    }

    tearDownInRequest();
  }

  @Request
  private static void setUpInRequest()
  {
    setUpTransaction();
  }

  @Transaction
  private static void setUpTransaction()
  {
    service = RegistryService.getInstance();

    Classifier rootClassifier = Classifier.getByKey(com.runwaysdk.business.ontology.Term.ROOT_KEY);

    try
    {
      Classifier rootTestClassifier = new Classifier();
      rootTestClassifier.setClassifierId(ROOT_TEST_TERM_CLASSIFIER_ID);
      rootTestClassifier.setClassifierPackage(RegistryConstants.REGISTRY_PACKAGE);
      rootTestClassifier.getDisplayLabel().setDefaultValue("Test Root");
      rootTestClassifier.apply();

      ROOT_TEST_TERM_KEY = rootTestClassifier.getKeyName();

      rootTestClassifier.addLink(rootClassifier, ClassifierIsARelationship.CLASS).apply();
    }
    catch (DuplicateDataException e)
    {
      e.printStackTrace();
    }
    catch (RuntimeException e)
    {
      e.printStackTrace();
    }

  }

  @Request
  private static void tearDownInRequest()
  {
    tearDownTransaction();
  }

  @Transaction
  private static void tearDownTransaction()
  {
    try
    {
      // Just in case a previous test did not clean up properly.
      // try
      // {
      // Universal riverTestUniversal = Universal.getByKey(RIVER_CODE);
      // MdBusiness mdBusiness = riverTestUniversal.getMdBusiness();
      // riverTestUniversal.delete();
      // mdBusiness.delete();
      // }
      // catch (DataNotFoundException e)
      // {
      // }
      //
      // try
      // {
      // Universal householdTestUniversal = Universal.getByKey(HOUSEHOLD_CODE);
      // MdBusiness mdBusiness = householdTestUniversal.getMdBusiness();
      // householdTestUniversal.delete();
      // mdBusiness.delete();
      // }
      // catch (DataNotFoundException e)
      // {
      // }
      //
      // try
      // {
      // Universal universal = Universal.getByKey(VILLAGE.getCode());
      // MdBusiness mdBusiness = universal.getMdBusiness();
      // universal.delete();
      // mdBusiness.delete();
      // }
      // catch (DataNotFoundException e)
      // {
      // }
      //
      // try
      // {
      // Universal universal = Universal.getByKey(DISTRICT.getCode());
      // MdBusiness mdBusiness = universal.getMdBusiness();
      // universal.delete();
      // mdBusiness.delete();
      // }
      // catch (DataNotFoundException e)
      // {
      // }
      //
      // try
      // {
      // Universal provinceTestUniversal = Universal.getByKey(PROVINCE);
      // MdBusiness mdBusiness = provinceTestUniversal.getMdBusiness();
      // provinceTestUniversal.delete();
      //
      // // efawe mdBusiness.getAllAttribute();
      //
      // mdBusiness.delete();
      // }
      // catch (DataNotFoundException e)
      // {
      // }
      //
      // try
      // {
      // Universal countryTestUniversal = Universal.getByKey(COUNTRY);
      // MdBusiness mdBusiness = countryTestUniversal.getMdBusiness();
      // countryTestUniversal.delete();
      // mdBusiness.delete();
      // }
      // catch (DataNotFoundException e)
      // {
      // }
      //
      // try
      // {
      // MdTermRelationship mdTermRelationship =
      // MdTermRelationship.getByKey(ServerHierarchyType.buildMdTermRelUniversalKey(REPORTING_DIVISION_CODE));
      // mdTermRelationship.delete();
      // }
      // catch (DataNotFoundException e)
      // {
      // }
      //
      // try
      // {
      // MdTermRelationship mdTermRelationship =
      // MdTermRelationship.getByKey(ServerHierarchyType.buildMdTermRelGeoEntityKey(REPORTING_DIVISION_CODE));
      // mdTermRelationship.delete();
      // }
      // catch (DataNotFoundException e)
      // {
      // }
      //
      // try
      // {
      // MdTermRelationship mdTermRelationship =
      // MdTermRelationship.getByKey(ServerHierarchyType.buildMdTermRelUniversalKey(ADMINISTRATIVE_DIVISION_CODE));
      // mdTermRelationship.delete();
      // }
      // catch (DataNotFoundException e)
      // {
      // }
      //
      // try
      // {
      // MdTermRelationship mdTermRelationship =
      // MdTermRelationship.getByKey(ServerHierarchyType.buildMdTermRelGeoEntityKey(ADMINISTRATIVE_DIVISION_CODE));
      // mdTermRelationship.delete();
      // }
      // catch (DataNotFoundException e)
      // {
      // }

      try
      {
        String classifierKey = Classifier.buildKey(RegistryConstants.REGISTRY_PACKAGE, ROOT_TEST_TERM_CLASSIFIER_ID);
        Classifier rootTestClassifier = Classifier.getByKey(classifierKey);
        rootTestClassifier.delete();
      }
      catch (DataNotFoundException e)
      {
      }

      try
      {
        String classifierKey = Classifier.buildKey(RegistryConstants.REGISTRY_PACKAGE, "termValue1");
        Classifier childTerm1 = Classifier.getByKey(classifierKey);
        childTerm1.delete();
      }
      catch (DataNotFoundException e)
      {
      }
      try
      {
        String classifierKey = Classifier.buildKey(RegistryConstants.REGISTRY_PACKAGE, "termValue2");
        Classifier childTerm2 = Classifier.getByKey(classifierKey);
        childTerm2.delete();
      }
      catch (DataNotFoundException e)
      {
      }
      try
      {
        Organization organization = Organization.getByKey(ORG_MOI);
        organization.delete();
      }
      catch (DataNotFoundException e)
      {
      }
    }
    catch (RuntimeException e)
    {
      e.printStackTrace();
    }
  }

  @Test
  public void testCreateOganization()
  {
    OrganizationDTO orgDTOclient = this.createOrganization(ORG_MOI);
    
    try
    {
      OrganizationDTO[] orgs = service.getOrganizations(testData.adminSession.getSessionId(), new String[] { ORG_MOI });

      Assert.assertEquals("Organization was not properly created", 1, orgs.length);

      OrganizationDTO orgDTOserver = orgs[0];

      Assert.assertEquals("Organization code was not correct", orgDTOclient.getCode(), orgDTOserver.getCode());
      Assert.assertEquals("Organization label was not correct", orgDTOclient.getLabel().getValue(), orgDTOserver.getLabel().getValue());
      Assert.assertEquals("Organization contact info was not correct", orgDTOclient.getContactInfo().getValue(), orgDTOserver.getContactInfo().getValue());
    }
    finally
    {
      this.deleteOrganization(ORG_MOI);
    }
  }
  
  private OrganizationDTO createOrganization(String organizationCode)
  {
    RegistryAdapterServer registry = new RegistryAdapterServer(RegistryIdService.getInstance());

    OrganizationDTO orgDTOclient = MetadataFactory.newOrganization(organizationCode, new LocalizedValue("Ministry of Interior"), new LocalizedValue("Contact Joe at..."), registry);

    String gtJSON = orgDTOclient.toJSON().toString();

    service.createOrganization(testData.adminSession.getSessionId(), gtJSON);
    
    return orgDTOclient;
  }
  
  private void deleteOrganization(String organizationCode)
  {
    service.deleteOrganization(testData.adminSession.getSessionId(), organizationCode);
  }

  @Test
  public void testCreateGeoObjectType()
  {
    RegistryAdapterServer registry = new RegistryAdapterServer(RegistryIdService.getInstance());

    String organizationCode = null;

    GeoObjectType province = MetadataFactory.newGeoObjectType(PROVINCE.getCode(), GeometryType.POLYGON, new LocalizedValue("Province"), new LocalizedValue(""), true, organizationCode, registry);

    String gtJSON = province.toJSON().toString();

    service.createGeoObjectType(testData.adminSession.getSessionId(), gtJSON);

    checkMdBusinessAttributes(PROVINCE.getCode());
    checkMdGraphAttributes(PROVINCE.getCode());
  }
  
  @Test
  public void testCreateGeoObjectTypeWithOrganization()
  {
    GeoObjectType serverProvince = null;
    
    String organizationCode = ORG_MOI;
    this.createOrganization(organizationCode);
    
    try
    {
      RegistryAdapterServer registry = new RegistryAdapterServer(RegistryIdService.getInstance());

      GeoObjectType clientProvince = MetadataFactory.newGeoObjectType(PROVINCE.getCode(), GeometryType.POLYGON, new LocalizedValue("Province"), new LocalizedValue(""), true,  organizationCode, registry);

      String gtJSON = clientProvince.toJSON().toString();

      serverProvince = service.createGeoObjectType(testData.adminSession.getSessionId(), gtJSON);

      Assert.assertEquals("Organization code was not properly returned from the server", organizationCode, serverProvince.getOrganizationCode());
      
      checkMdBusinessAttributes(PROVINCE.getCode());
    }
    finally
    {
      if (serverProvince != null)
      {
        service.deleteGeoObjectType(testData.adminSession.getSessionId(), PROVINCE.getCode());
      }
 
      this.deleteOrganization(organizationCode);
    }
  }

  @Request
  private void checkMdGraphAttributes(String code)
  {
    MdGeoVertexDAOIF mdGraphClassDAOIF = (MdGeoVertexDAOIF)MdGeoVertexDAO.get(MdGeoVertexInfo.CLASS, GeoVertexType.buildMdGeoVertexKey(code));

    // DefaultAttribute.UID - Defined on the MdBusiness and the values are from
    // the {@code GeoObject#OID};
    try
    {
      mdGraphClassDAOIF.definesAttribute(DefaultAttribute.UID.getName());
    }
    catch (DataNotFoundException e)
    {
      Assert.fail("Attribute that implements GeoObject.UID does not exist. It should be defined on the business class");
    }

    // DefaultAttribute.CODE - defined by GeoEntity geoId
    try
    {
      mdGraphClassDAOIF.definesAttribute(DefaultAttribute.CODE.getName());
    }
    catch (DataNotFoundException e)
    {
      Assert.fail("Attribute that implements GeoObjectType.CODE does not exist.It should be defined on the business class");
    }

    // DefaultAttribute.CREATED_DATE - The create data on the GeoObject?
    try
    {
      mdGraphClassDAOIF.definesAttribute(MdBusinessInfo.CREATE_DATE);
    }
    catch (DataNotFoundException e)
    {
      Assert.fail("Attribute that implements GeoObjectType.CREATED_DATE does not exist.It should be defined on the business class");
    }

    // DefaultAttribute.UPDATED_DATE - The update data on the GeoObject?
    try
    {
      mdGraphClassDAOIF.definesAttribute(MdBusinessInfo.LAST_UPDATE_DATE);
    }
    catch (DataNotFoundException e)
    {
      Assert.fail("Attribute that implements GeoObjectType.LAST_UPDATE_DATE does not exist.It should be defined on the business class");
    }

    // DefaultAttribute.STATUS
    try
    {
      mdGraphClassDAOIF.definesAttribute(MdBusinessInfo.LAST_UPDATE_DATE);
    }
    catch (DataNotFoundException e)
    {
      Assert.fail("Attribute that implements GeoObjectType.LAST_UPDATE_DATE does not exist.It should be defined on the business class");
    }
  }
  
  @Request
  private void checkMdBusinessAttributes(String code)
  {
    Universal universal = Universal.getByKey(code);
    MdBusiness mdBusiness = universal.getMdBusiness();

    MdBusinessDAOIF mdBusinessDAOIF = (MdBusinessDAOIF) BusinessFacade.getEntityDAO(mdBusiness);
    // For debugging
    // mdBusinessDAOIF.getAllDefinedMdAttributes().forEach(a ->
    // System.out.println(a.definesAttribute() +" "+a.getType()));

    // DefaultAttribute.UID - Defined on the MdBusiness and the values are from
    // the {@code GeoObject#OID};
    try
    {
      mdBusinessDAOIF.definesAttribute(DefaultAttribute.UID.getName());
    }
    catch (DataNotFoundException e)
    {
      Assert.fail("Attribute that implements GeoObject.UID does not exist. It should be defined on the business class");
    }

    // DefaultAttribute.CODE - defined by GeoEntity geoId
    try
    {
      mdBusinessDAOIF.definesAttribute(DefaultAttribute.CODE.getName());
    }
    catch (DataNotFoundException e)
    {
      Assert.fail("Attribute that implements GeoObjectType.CODE does not exist.It should be defined on the business class");
    }

    // DefaultAttribute.CREATED_DATE - The create data on the GeoObject?
    try
    {
      mdBusinessDAOIF.definesAttribute(MdBusinessInfo.CREATE_DATE);
    }
    catch (DataNotFoundException e)
    {
      Assert.fail("Attribute that implements GeoObjectType.CREATED_DATE does not exist.It should be defined on the business class");
    }

    // DefaultAttribute.UPDATED_DATE - The update data on the GeoObject?
    try
    {
      mdBusinessDAOIF.definesAttribute(MdBusinessInfo.LAST_UPDATE_DATE);
    }
    catch (DataNotFoundException e)
    {
      Assert.fail("Attribute that implements GeoObjectType.LAST_UPDATE_DATE does not exist.It should be defined on the business class");
    }

    // DefaultAttribute.STATUS
    try
    {
      mdBusinessDAOIF.definesAttribute(MdBusinessInfo.LAST_UPDATE_DATE);
    }
    catch (DataNotFoundException e)
    {
      Assert.fail("Attribute that implements GeoObjectType.LAST_UPDATE_DATE does not exist.It should be defined on the business class");
    }
  }

  @Test
  public void testCreateGeoObjectTypeCharacter_AndUpdate()
  {
    RegistryAdapterServer registry = new RegistryAdapterServer(RegistryIdService.getInstance());

    String organizationCode = null;

    GeoObjectType province = MetadataFactory.newGeoObjectType(PROVINCE.getCode(), GeometryType.POLYGON, new LocalizedValue("Province"), new LocalizedValue(""), true, organizationCode, registry);

    String geoObjectTypeCode = province.getCode();

    String gtJSON = province.toJSON().toString();

    AttributeType testChar = AttributeType.factory("testChar", new LocalizedValue("testCharLocalName"), new LocalizedValue("testCharLocalDescrip"), AttributeCharacterType.TYPE, false, false, false);

    service.createGeoObjectType(testData.adminSession.getSessionId(), gtJSON);
    String attributeTypeJSON = testChar.toJSON().toString();
    testChar = service.createAttributeType(testData.adminSession.getSessionId(), geoObjectTypeCode, attributeTypeJSON);

    MdAttributeConcreteDAOIF mdAttributeConcreteDAOIF = checkAttribute(PROVINCE.getCode(), testChar.getName());

    Assert.assertNotNull("A GeoObjectType did not define the attribute: " + testChar.getName(), mdAttributeConcreteDAOIF);
    Assert.assertTrue("A GeoObjectType did not define the attribute of the correct type: " + mdAttributeConcreteDAOIF.getType(), mdAttributeConcreteDAOIF instanceof MdAttributeCharacterDAOIF);

    testChar.setLabel(MdAttributeLocalInfo.DEFAULT_LOCALE, "testCharLocalName-Update");
    testChar.setDescription(MdAttributeLocalInfo.DEFAULT_LOCALE, "testCharLocalDescrip-Update");
    attributeTypeJSON = testChar.toJSON().toString();
    testChar = service.updateAttributeType(this.testData.adminSession.getSessionId(), geoObjectTypeCode, attributeTypeJSON);

    Assert.assertEquals("testCharLocalName-Update", testChar.getLabel().getValue());
    Assert.assertEquals("testCharLocalDescrip-Update", testChar.getDescription().getValue());
  }

  @Test
  public void testCreateGeoObjectTypeDate()
  {
    RegistryAdapterServer registry = new RegistryAdapterServer(RegistryIdService.getInstance());

    String organizationCode = null;

    GeoObjectType province = MetadataFactory.newGeoObjectType(PROVINCE.getCode(), GeometryType.POLYGON, new LocalizedValue("Province"), new LocalizedValue(""), true, organizationCode, registry);

    String gtJSON = province.toJSON().toString();

    AttributeType testDate = AttributeType.factory("testDate", new LocalizedValue("testDateLocalName"), new LocalizedValue("testDateLocalDescrip"), AttributeDateType.TYPE, false, false, false);

    service.createGeoObjectType(testData.adminSession.getSessionId(), gtJSON);

    String geoObjectTypeCode = province.getCode();
    String attributeTypeJSON = testDate.toJSON().toString();
    testDate = service.createAttributeType(testData.adminSession.getSessionId(), geoObjectTypeCode, attributeTypeJSON);

    MdAttributeConcreteDAOIF mdAttributeConcreteDAOIF = checkAttribute(PROVINCE.getCode(), testDate.getName());

    Assert.assertNotNull("A GeoObjectType did not define the attribute: " + testDate.getName(), mdAttributeConcreteDAOIF);
    Assert.assertTrue("A GeoObjectType did not define the attribute of the correct type: " + mdAttributeConcreteDAOIF.getType(), mdAttributeConcreteDAOIF instanceof MdAttributeMomentDAOIF);
  }

  @Test
  public void testCreateGeoObjectTypeInteger()
  {
    RegistryAdapterServer registry = new RegistryAdapterServer(RegistryIdService.getInstance());

    String organizationCode = null;

    GeoObjectType province = MetadataFactory.newGeoObjectType(PROVINCE.getCode(), GeometryType.POLYGON, new LocalizedValue("Province"), new LocalizedValue(""), true, organizationCode, registry);

    String gtJSON = province.toJSON().toString();

    AttributeType testInteger = AttributeType.factory("testInteger", new LocalizedValue("testIntegerLocalName"), new LocalizedValue("testIntegerLocalDescrip"), AttributeIntegerType.TYPE, false, false, false);

    service.createGeoObjectType(testData.adminSession.getSessionId(), gtJSON);

    String geoObjectTypeCode = province.getCode();
    String attributeTypeJSON = testInteger.toJSON().toString();
    testInteger = service.createAttributeType(testData.adminSession.getSessionId(), geoObjectTypeCode, attributeTypeJSON);

    MdAttributeConcreteDAOIF mdAttributeConcreteDAOIF = checkAttribute(PROVINCE.getCode(), testInteger.getName());

    Assert.assertNotNull("A GeoObjectType did not define the attribute: " + testInteger.getName(), mdAttributeConcreteDAOIF);
    Assert.assertTrue("A GeoObjectType did not define the attribute of the correct type: " + mdAttributeConcreteDAOIF.getType(), mdAttributeConcreteDAOIF instanceof MdAttributeLongDAOIF);
  }

  @Test
  public void testCreateGeoObjectTypeBoolean()
  {
    RegistryAdapterServer registry = new RegistryAdapterServer(RegistryIdService.getInstance());

    String organizationCode = null;

    GeoObjectType province = MetadataFactory.newGeoObjectType(PROVINCE.getCode(), GeometryType.POLYGON, new LocalizedValue("Province"), new LocalizedValue(""), true, organizationCode, registry);

    String gtJSON = province.toJSON().toString();

    AttributeType testBoolean = AttributeType.factory("testBoolean", new LocalizedValue("testBooleanName"), new LocalizedValue("testBooleanDescrip"), AttributeBooleanType.TYPE, false, false, false);

    service.createGeoObjectType(testData.adminSession.getSessionId(), gtJSON);

    String geoObjectTypeCode = province.getCode();
    String attributeTypeJSON = testBoolean.toJSON().toString();
    testBoolean = service.createAttributeType(testData.adminSession.getSessionId(), geoObjectTypeCode, attributeTypeJSON);

    MdAttributeConcreteDAOIF mdAttributeConcreteDAOIF = checkAttribute(PROVINCE.getCode(), testBoolean.getName());

    Assert.assertNotNull("A GeoObjectType did not define the attribute: " + testBoolean.getName(), mdAttributeConcreteDAOIF);
    Assert.assertTrue("A GeoObjectType did not define the attribute of the correct type: " + mdAttributeConcreteDAOIF.getType(), mdAttributeConcreteDAOIF instanceof MdAttributeBooleanDAOIF);
  }

  @Test
  public void testClassifierToTerm()
  {
    // String classKey = RegistryConstants.TERM_CLASS+"_TestType";
    // Term classTerm = new Term(classKey, "Test Type", "");
    //
    //
    // String attributeKey = classKey+"_AttributeName";
    // Term attributeTerm = new Term(attributeKey, "Attribute Root", "");
    // classTerm.addChild(classTerm);
    //
    // Term attributeValueTerm1 = new Term(attributeTerm+"_1", "Attribute Value
    // 1", "");
    // attributeTerm.addChild(attributeValueTerm1);
    // Term attributeValueTerm2 = new Term(attributeTerm+"_2", "Attribute Value
    // 2", "");
    // attributeTerm.addChild(attributeValueTerm2);
    // Term attributeValueTerm3 = new Term(attributeTerm+"_3", "Attribute Value
    // 3", "");
    // attributeTerm.addChild(attributeValueTerm3);

    this.buildClassifierTree();
  }

  @Request
  private void buildClassifierTree()
  {
    // Classifier.getByKey("net.geoprism.registry.CLASS_VillageTest").delete();
    // Classifier.getByKey("net.geoprism.registry.CLASS_ProvinceTest_testTerm").delete();
    // Classifier.getByKey("net.geoprism.registry.CLASS_TestClass_SomeAttribute").delete();
    // Classifier.getByKey("net.geoprism.registry.CLASS_TestClass_SomeAttribute_Value1").delete();
    // Classifier.getByKey("net.geoprism.registry.CLASS_TestClass_SomeAttribute_Value2").delete();
    // Classifier.getByKey("net.geoprism.registry.CLASS_TestClass_SomeAttribute_Value3").delete();

    String className = "TestClass";
    String attributeName = "SomeAttribute";

    String classifierId = TermConverter.buildRootClassClassifierId(className);

    String key = Classifier.buildKey(RegistryConstants.REGISTRY_PACKAGE, classifierId);

    Classifier classifier = null;
    // try
    // {
    // classifier = Classifier.getByKey(key);
    // classifier.getAllIsAChild().forEach(c -> c.delete());
    // classifier.delete();
    // }
    // catch (DataNotFoundException e)
    // {
    //
    // }
    classifier = this.buildClassAttributeClassifierTree(className, attributeName);

    try
    {
      TermConverter termBuilder = new TermConverter(classifier.getKeyName());

      Term term = termBuilder.build();

      classifierId = TermConverter.buildRootClassClassifierId(className);

      Assert.assertEquals(classifierId, term.getCode());

      int childCount = 0;

      OIterator<? extends Classifier> i = classifier.getAllIsAChild();
      while (i.hasNext())
      {
        i.next();
        childCount++;
      }

      Assert.assertEquals(childCount, term.getChildren().size());
    }
    finally
    {
      try
      {
        classifier.delete();
      }
      catch (RuntimeException e)
      {
        e.printStackTrace();
        System.out.println();
        ;
      }
    }
  }

  @Transaction
  private Classifier buildClassAttributeClassifierTree(String className, String attributeName)
  {
    Classifier rootTestClassifier = Classifier.getByKey(ROOT_TEST_TERM_KEY);

    String classifierId = TermConverter.buildRootClassClassifierId(className);
    Classifier classTerm = this.createClassifier(classifierId, "Test Type");

    classTerm.addLink(rootTestClassifier, ClassifierIsARelationship.CLASS).apply();

    String attributeKey = classifierId + "_" + attributeName;
    Classifier attributeTerm = this.createClassifier(attributeKey, "Attribute Test Root");
    attributeTerm.addLink(classTerm, ClassifierIsARelationship.CLASS).apply();

    Classifier attributeValueTerm1 = this.createClassifier(attributeKey + "_Value1", "Attribute Value 1");
    attributeValueTerm1.addLink(attributeTerm, ClassifierIsARelationship.CLASS).apply();

    Classifier attributeValueTerm2 = this.createClassifier(attributeKey + "_Value2", "Attribute Value 2");
    attributeValueTerm2.addLink(attributeTerm, ClassifierIsARelationship.CLASS).apply();

    Classifier attributeValueTerm3 = this.createClassifier(attributeKey + "_Value3", "Attribute Value 3");
    attributeValueTerm3.addLink(attributeTerm, ClassifierIsARelationship.CLASS).apply();

    return classTerm;
  }

  private Classifier createClassifier(String classifierId, String displayLabel)
  {
    // Create Classifier Terms
    Classifier classifier = new Classifier();
    classifier.setClassifierId(classifierId);
    classifier.setClassifierPackage(RegistryConstants.REGISTRY_PACKAGE);
    classifier.getDisplayLabel().setDefaultValue(displayLabel);
    classifier.apply();

    return classifier;
  }

  @Test
  public void testCreateGeoObjectTypeTerm()
  {
    RegistryAdapterServer registry = new RegistryAdapterServer(RegistryIdService.getInstance());

    String organizationCode = null;

    GeoObjectType province = MetadataFactory.newGeoObjectType(PROVINCE.getCode(), GeometryType.POLYGON, new LocalizedValue("Province"), new LocalizedValue(""), true, organizationCode, registry);

    String geoObjectTypeCode = province.getCode();

    AttributeTermType attributeTermType = (AttributeTermType) AttributeType.factory("testTerm", new LocalizedValue("Test Term Name"), new LocalizedValue("Test Term Description"), AttributeTermType.TYPE, false, false, false);
    Term term = new Term(PROVINCE.getCode() + "_" + "testTerm", new LocalizedValue("Test Term Name"), new LocalizedValue("Test Term Description"));
    attributeTermType.setRootTerm(term);

    province.addAttribute(attributeTermType);

    String gtJSON = province.toJSON().toString();

    service.createGeoObjectType(testData.adminSession.getSessionId(), gtJSON);

    String attributeTypeJSON = attributeTermType.toJSON().toString();
    attributeTermType = (AttributeTermType) service.createAttributeType(testData.adminSession.getSessionId(), geoObjectTypeCode, attributeTypeJSON);

    MdAttributeConcreteDAOIF mdAttributeConcreteDAOIF = checkAttribute(PROVINCE.getCode(), attributeTermType.getName());

    Assert.assertNotNull("A GeoObjectType did not define the attribute: " + attributeTermType.getName(), mdAttributeConcreteDAOIF);
    Assert.assertTrue("A GeoObjectType did not define the attribute of the correct type: " + mdAttributeConcreteDAOIF.getType(), mdAttributeConcreteDAOIF instanceof MdAttributeTermDAOIF);

    Term rootTerm = attributeTermType.getRootTerm();

    Term childTerm1 = new Term("termValue1", new LocalizedValue("Term Value 1"), new LocalizedValue(""));
    Term childTerm2 = new Term("termValue2", new LocalizedValue("Term Value 2"), new LocalizedValue(""));

    service.createTerm(testData.adminSession.getSessionId(), rootTerm.getCode(), childTerm1.toJSON().toString());
    service.createTerm(testData.adminSession.getSessionId(), rootTerm.getCode(), childTerm2.toJSON().toString());

    province = service.getGeoObjectTypes(testData.adminSession.getSessionId(), new String[] { PROVINCE.getCode() })[0];
    AttributeTermType attributeTermType2 = (AttributeTermType) province.getAttribute("testTerm").get();

    // Check to see if the cache was updated.
    checkTermsCreate(attributeTermType2);

    attributeTermType.setLabel(MdAttributeLocalInfo.DEFAULT_LOCALE, "Test Term Name Update");
    attributeTermType.setDescription(MdAttributeLocalInfo.DEFAULT_LOCALE, "Test Term Description Update");

    attributeTermType = (AttributeTermType) service.updateAttributeType(testData.adminSession.getSessionId(), geoObjectTypeCode, attributeTermType.toJSON().toString());

    Assert.assertEquals(attributeTermType.getLabel().getValue(), "Test Term Name Update");
    Assert.assertEquals(attributeTermType.getDescription().getValue(), "Test Term Description Update");

    checkTermsCreate(attributeTermType);

    // Test updating the term
    childTerm2 = new Term("termValue2", new LocalizedValue("Term Value 2a"), new LocalizedValue(""));

    service.updateTerm(testData.adminSession.getSessionId(), childTerm2.toJSON().toString());

    province = service.getGeoObjectTypes(testData.adminSession.getSessionId(), new String[] { PROVINCE.getCode() })[0];
    AttributeTermType attributeTermType3 = (AttributeTermType) province.getAttribute("testTerm").get();

    checkTermsUpdate(attributeTermType3);

    service.deleteTerm(testData.adminSession.getSessionId(), "termValue2");

    province = service.getGeoObjectTypes(testData.adminSession.getSessionId(), new String[] { PROVINCE.getCode() })[0];
    attributeTermType3 = (AttributeTermType) province.getAttribute("testTerm").get();

    System.out.println(attributeTermType3.getRootTerm().toString());

    checkTermsDelete(attributeTermType3);
  }

  /**
   * Method for checking the state of the {@link Term}s on an
   * {@link AttributeTermType}
   * 
   * @param attributeTermType
   */
  private void checkTermsCreate(AttributeTermType attributeTermType)
  {
    Term rootTerm;
    Term childTerm1;
    Term childTerm2;

    rootTerm = attributeTermType.getRootTerm();

    List<Term> childTerms = rootTerm.getChildren();

    Assert.assertEquals(2, childTerms.size());

    childTerm1 = childTerms.get(0);
    childTerm2 = childTerms.get(1);

    Assert.assertEquals(childTerm1.getCode(), "termValue1");
    Assert.assertEquals(childTerm1.getLabel().getValue(), "Term Value 1");

    Assert.assertEquals(childTerm2.getCode(), "termValue2");
    Assert.assertEquals(childTerm2.getLabel().getValue(), "Term Value 2");
  }

  /**
   * Method for checking the state of the {@link Term}s on an
   * {@link AttributeTermType}
   * 
   * @param attributeTermType
   */
  private void checkTermsUpdate(AttributeTermType attributeTermType)
  {
    Term rootTerm;
    Term childTerm1;
    Term childTerm2;

    rootTerm = attributeTermType.getRootTerm();

    List<Term> childTerms = rootTerm.getChildren();

    Assert.assertEquals(2, childTerms.size());

    childTerm1 = childTerms.get(0);
    childTerm2 = childTerms.get(1);

    Assert.assertEquals(childTerm1.getCode(), "termValue1");
    Assert.assertEquals(childTerm1.getLabel().getValue(), "Term Value 1");

    Assert.assertEquals(childTerm2.getCode(), "termValue2");
    Assert.assertEquals(childTerm2.getLabel().getValue(), "Term Value 2a");
  }

  /**
   * Method for checking the state of the {@link Term}s on an
   * {@link AttributeTermType}
   * 
   * @param attributeTermType
   */
  private void checkTermsDelete(AttributeTermType attributeTermType)
  {
    Term rootTerm;
    Term childTerm1;

    rootTerm = attributeTermType.getRootTerm();

    List<Term> childTerms = rootTerm.getChildren();

    Assert.assertEquals(1, childTerms.size());

    childTerm1 = childTerms.get(0);

    Assert.assertEquals(childTerm1.getCode(), "termValue1");
    Assert.assertEquals(childTerm1.getLabel().getValue(), "Term Value 1");
  }

  @Request
  private MdAttributeConcreteDAOIF checkAttribute(String geoObjectTypeCode, String attributeName)
  {
    Universal universal = Universal.getByKey(geoObjectTypeCode);
    MdBusiness mdBusiness = universal.getMdBusiness();

    MdBusinessDAOIF mdBusinessDAOIF = (MdBusinessDAOIF) BusinessFacade.getEntityDAO(mdBusiness);

    return mdBusinessDAOIF.definesAttribute(attributeName);
  }

// Heads up: clean up do we remove these tests?
//  @Test
//  public void testCreateGeoObjectTypePoint()
//  {
//    RegistryAdapterServer registry = new RegistryAdapterServer(RegistryIdService.getInstance());
//
//    String organizationCode = null;
//
//    GeoObjectType village = MetadataFactory.newGeoObjectType(VILLAGE.getCode(), GeometryType.POINT, new LocalizedValue("Village"), new LocalizedValue(""), true, organizationCode, registry);
//
//    String villageJSON = village.toJSON().toString();
//
//    service.createGeoObjectType(testData.adminSession.getSessionId(), villageJSON);
//
//    checkAttributePoint(VILLAGE.getCode());
//  }
//
//  @Test
//  public void testCreateGeoObjectTypeLine()
//  {
//    RegistryAdapterServer registry = new RegistryAdapterServer(RegistryIdService.getInstance());
//
//    String organizationCode = null;
//
//    GeoObjectType river = MetadataFactory.newGeoObjectType(RIVER.getCode(), GeometryType.LINE, new LocalizedValue("River"), new LocalizedValue(""), true, organizationCode, registry);
//
//    String riverJSON = river.toJSON().toString();
//
//    service.createGeoObjectType(testData.adminSession.getSessionId(), riverJSON);
//
//    checkAttributeLine(RIVER.getCode());
//  }
//
//  @Test
//  public void testCreateGeoObjectTypePolygon()
//  {
//    RegistryAdapterServer registry = new RegistryAdapterServer(RegistryIdService.getInstance());
//
//    String organizationCode = null;
//
//    GeoObjectType geoObjectType = MetadataFactory.newGeoObjectType(DISTRICT.getCode(), GeometryType.POLYGON, new LocalizedValue("District"), new LocalizedValue(""), true, organizationCode, registry);
//
//    String gtJSON = geoObjectType.toJSON().toString();
//
//    service.createGeoObjectType(testData.adminSession.getSessionId(), gtJSON);
//
//    checkAttributePolygon(DISTRICT.getCode());
//  }
//
//  @Test
//  public void testCreateGeoObjectTypeMultiPoint()
//  {
//    RegistryAdapterServer registry = new RegistryAdapterServer(RegistryIdService.getInstance());
//
//    String organizationCode = null;
//
//    GeoObjectType village = MetadataFactory.newGeoObjectType(VILLAGE.getCode(), GeometryType.MULTIPOINT, new LocalizedValue("Village"), new LocalizedValue(""), true, organizationCode, registry);
//
//    String villageJSON = village.toJSON().toString();
//
//    service.createGeoObjectType(testData.adminSession.getSessionId(), villageJSON);
//
//    checkAttributeMultiPoint(VILLAGE.getCode());
//  }
//
//  @Test
//  public void testCreateGeoObjectTypeMultiLine()
//  {
//    RegistryAdapterServer registry = new RegistryAdapterServer(RegistryIdService.getInstance());
//
//    String organizationCode = null;
//
//    GeoObjectType river = MetadataFactory.newGeoObjectType(RIVER.getCode(), GeometryType.MULTILINE, new LocalizedValue("River"), new LocalizedValue(""),true, organizationCode, registry);
//
//    String riverJSON = river.toJSON().toString();
//
//    service.createGeoObjectType(testData.adminSession.getSessionId(), riverJSON);
//
//    checkAttributeMultiLine(RIVER.getCode());
//  }
//
//  @Test
//  public void testCreateGeoObjectTypeMultiPolygon()
//  {
//    RegistryAdapterServer registry = new RegistryAdapterServer(RegistryIdService.getInstance());
//
//    String organizationCode = null;
//
//    GeoObjectType geoObjectType = MetadataFactory.newGeoObjectType(DISTRICT.getCode(), GeometryType.MULTIPOLYGON, new LocalizedValue("District"), new LocalizedValue(""), true, organizationCode, registry);
//
//    String gtJSON = geoObjectType.toJSON().toString();
//
//    service.createGeoObjectType(testData.adminSession.getSessionId(), gtJSON);
//
//    checkAttributeMultiPolygon(DISTRICT.getCode());
//  }

  @Request
  private void checkAttributePoint(String code)
  {
    Universal universal = Universal.getByKey(code);
    MdBusiness mdBusiness = universal.getMdBusiness();

    MdBusinessDAOIF mdBusinessDAOIF = (MdBusinessDAOIF) BusinessFacade.getEntityDAO(mdBusiness);

    MdAttributeConcreteDAOIF mdAttributeConcreteDAOIF = mdBusinessDAOIF.definesAttribute(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);

    Assert.assertNotNull("A GeoObjectType did not define the proper geometry type attribute: " + RegistryConstants.GEOMETRY_ATTRIBUTE_NAME, mdAttributeConcreteDAOIF);
  }

  @Request
  private void checkAttributeLine(String code)
  {
    Universal universal = Universal.getByKey(code);
    MdBusiness mdBusiness = universal.getMdBusiness();

    MdBusinessDAOIF mdBusinessDAOIF = (MdBusinessDAOIF) BusinessFacade.getEntityDAO(mdBusiness);

    MdAttributeConcreteDAOIF mdAttributeConcreteDAOIF = mdBusinessDAOIF.definesAttribute(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);

    Assert.assertNotNull("A GeoObjectType did not define the proper geometry type attribute: " + RegistryConstants.GEOMETRY_ATTRIBUTE_NAME, mdAttributeConcreteDAOIF);

  }

  @Request
  private void checkAttributePolygon(String code)
  {
    Universal universal = Universal.getByKey(code);
    MdBusiness mdBusiness = universal.getMdBusiness();

    MdBusinessDAOIF mdBusinessDAOIF = (MdBusinessDAOIF) BusinessFacade.getEntityDAO(mdBusiness);

    MdAttributeConcreteDAOIF mdAttributeConcreteDAOIF = mdBusinessDAOIF.definesAttribute(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);

    Assert.assertNotNull("A GeoObjectType did not define the proper geometry type attribute: " + RegistryConstants.GEOMETRY_ATTRIBUTE_NAME, mdAttributeConcreteDAOIF);
  }

  @Request
  private void checkAttributeMultiPoint(String code)
  {
    Universal universal = Universal.getByKey(code);
    MdBusiness mdBusiness = universal.getMdBusiness();

    MdBusinessDAOIF mdBusinessDAOIF = (MdBusinessDAOIF) BusinessFacade.getEntityDAO(mdBusiness);

    MdAttributeConcreteDAOIF mdAttributeConcreteDAOIF = mdBusinessDAOIF.definesAttribute(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);

    Assert.assertNotNull("A GeoObjectType did not define the proper geometry type attribute: " + RegistryConstants.GEOMETRY_ATTRIBUTE_NAME, mdAttributeConcreteDAOIF);
  }

  @Request
  private void checkAttributeMultiLine(String code)
  {
    Universal universal = Universal.getByKey(code);
    MdBusiness mdBusiness = universal.getMdBusiness();

    MdBusinessDAOIF mdBusinessDAOIF = (MdBusinessDAOIF) BusinessFacade.getEntityDAO(mdBusiness);

    MdAttributeConcreteDAOIF mdAttributeConcreteDAOIF = mdBusinessDAOIF.definesAttribute(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);

    Assert.assertNotNull("A GeoObjectType did not define the proper geometry type attribute: " + RegistryConstants.GEOMETRY_ATTRIBUTE_NAME, mdAttributeConcreteDAOIF);
  }

  @Request
  private void checkAttributeMultiPolygon(String code)
  {
    Universal universal = Universal.getByKey(code);
    MdBusiness mdBusiness = universal.getMdBusiness();

    MdBusinessDAOIF mdBusinessDAOIF = (MdBusinessDAOIF) BusinessFacade.getEntityDAO(mdBusiness);

    MdAttributeConcreteDAOIF mdAttributeConcreteDAOIF = mdBusinessDAOIF.definesAttribute(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);

    Assert.assertNotNull("A GeoObjectType did not define the proper geometry type attribute: " + RegistryConstants.GEOMETRY_ATTRIBUTE_NAME, mdAttributeConcreteDAOIF);
  }

  @Test
  public void testUpdateGeoObjectType()
  {
    RegistryAdapterServer registry = new RegistryAdapterServer(RegistryIdService.getInstance());

    String organizationCode = null;

    GeoObjectType province = MetadataFactory.newGeoObjectType(PROVINCE.getCode(), GeometryType.POLYGON, new LocalizedValue("Province Test"), new LocalizedValue("Some Description"), true, organizationCode, registry);

    String gtJSON = province.toJSON().toString();

    service.createGeoObjectType(testData.adminSession.getSessionId(), gtJSON);

    province = service.getGeoObjectTypes(testData.adminSession.getSessionId(), new String[] { PROVINCE.getCode() })[0];

    province.setLabel(MdAttributeLocalInfo.DEFAULT_LOCALE, "Province Test 2");
    province.setDescription(MdAttributeLocalInfo.DEFAULT_LOCALE, "Some Description 2");

    gtJSON = province.toJSON().toString();
    service.updateGeoObjectType(testData.adminSession.getSessionId(), gtJSON);

    province = service.getGeoObjectTypes(testData.adminSession.getSessionId(), new String[] { PROVINCE.getCode() })[0];

    Assert.assertEquals("Display label was not updated on a GeoObjectType", "Province Test 2", province.getLabel().getValue());
    Assert.assertEquals("Description  was not updated on a GeoObjectType", "Some Description 2", province.getDescription().getValue());
  }

  
  @Test
  public void testCreateHierarchyType()
  {
    RegistryAdapterServer registry = new RegistryAdapterServer(RegistryIdService.getInstance());

    // newGeoObjectType(PROVINCE_CODE, GeometryType.POLYGON, "Province", "",
    // false, registry);

    String organizationCode = null;
    
    HierarchyType reportingDivision = MetadataFactory.newHierarchyType(REPORTING_DIVISION.getCode(), new LocalizedValue("Reporting Division"), new LocalizedValue("The rporting division hieracy..."), organizationCode, registry);
    String gtJSON = reportingDivision.toJSON().toString();

    service.createHierarchyType(testData.adminSession.getSessionId(), gtJSON);

    HierarchyType[] hierarchies = service.getHierarchyTypes(testData.adminSession.getSessionId(), new String[] { REPORTING_DIVISION.getCode() });

    Assert.assertNotNull("The created hierarchy was not returned", hierarchies);

    Assert.assertEquals("The wrong number of hierarchies were returned.", 1, hierarchies.length);

    HierarchyType hierarchy = hierarchies[0];

    Assert.assertEquals("Reporting Division", hierarchy.getLabel().getValue());

    // test the types that were created
    String mdTermRelUniversal = ServerHierarchyType.buildMdTermRelUniversalKey(reportingDivision.getCode());
    String expectedMdTermRelUniversal = GISConstants.GEO_PACKAGE + "." + reportingDivision.getCode() + RegistryConstants.UNIVERSAL_RELATIONSHIP_POST;
    Assert.assertEquals("The type name of the MdTermRelationshp defining the universals was not correctly defined for the given code.", expectedMdTermRelUniversal, mdTermRelUniversal);

    String mdTermRelGeoEntity = ServerHierarchyType.buildMdTermRelGeoEntityKey(reportingDivision.getCode());
    String expectedMdTermRelGeoEntity = GISConstants.GEO_PACKAGE + "." + reportingDivision.getCode();
    Assert.assertEquals("The type name of the MdTermRelationshp defining the geoentities was not correctly defined for the given code.", expectedMdTermRelGeoEntity, mdTermRelGeoEntity);
  }
  
  @Test
  public void testCreateHierarchyTypeWithOrganization()
  {
    HierarchyType reportingDivision = null;

    String organizationCode = ORG_MOI;
    
    this.createOrganization(organizationCode);
    
    try
    {
      RegistryAdapterServer registry = new RegistryAdapterServer(RegistryIdService.getInstance());
    
      reportingDivision = MetadataFactory.newHierarchyType(REPORTING_DIVISION.getCode(), new LocalizedValue("Reporting Division"), new LocalizedValue("The rporting division hieracy..."), organizationCode, registry);
      String gtJSON = reportingDivision.toJSON().toString();

      service.createHierarchyType(testData.adminSession.getSessionId(), gtJSON);

      HierarchyType[] hierarchies = service.getHierarchyTypes(testData.adminSession.getSessionId(), new String[] { REPORTING_DIVISION.getCode() });

      Assert.assertNotNull("The created hierarchy was not returned", hierarchies);

      Assert.assertEquals("The wrong number of hierarchies were returned.", 1, hierarchies.length);

      HierarchyType hierarchy = hierarchies[0];

      Assert.assertEquals("Reporting Division", hierarchy.getLabel().getValue());
      
      Assert.assertEquals(organizationCode, hierarchy.getOrganizationCode());

      // test the types that were created
      String mdTermRelUniversal = ServerHierarchyType.buildMdTermRelUniversalKey(reportingDivision.getCode());
      String expectedMdTermRelUniversal = GISConstants.GEO_PACKAGE + "." + reportingDivision.getCode() + RegistryConstants.UNIVERSAL_RELATIONSHIP_POST;
      Assert.assertEquals("The type name of the MdTermRelationshp defining the universals was not correctly defined for the given code.", expectedMdTermRelUniversal, mdTermRelUniversal);

      String mdTermRelGeoEntity = ServerHierarchyType.buildMdTermRelGeoEntityKey(reportingDivision.getCode());
      String expectedMdTermRelGeoEntity = GISConstants.GEO_PACKAGE + "." + reportingDivision.getCode();
      Assert.assertEquals("The type name of the MdTermRelationshp defining the geoentities was not correctly defined for the given code.", expectedMdTermRelGeoEntity, mdTermRelGeoEntity);
    }
    finally
    {
      if (reportingDivision != null)
      {
        service.deleteHierarchyType(testData.adminSession.getSessionId(), REPORTING_DIVISION.getCode());
      }
      
      this.deleteOrganization(organizationCode);
    }
  }

  @Test
  public void testUpdateHierarchyType()
  {
    RegistryAdapterServer registry = new RegistryAdapterServer(RegistryIdService.getInstance());

    // newGeoObjectType(PROVINCE_CODE, GeometryType.POLYGON, "Province", "",
    // false, registry);

    String organizationCode = null;
    
    HierarchyType reportingDivision = MetadataFactory.newHierarchyType(REPORTING_DIVISION.getCode(), new LocalizedValue("Reporting Division"), new LocalizedValue("The rporting division hieracy..."), organizationCode, registry);
    String gtJSON = reportingDivision.toJSON().toString();

    reportingDivision = service.createHierarchyType(testData.adminSession.getSessionId(), gtJSON);

    reportingDivision.setLabel(new LocalizedValue("Reporting Division 2"));

    reportingDivision.setDescription(new LocalizedValue("The rporting division hieracy 2"));

    gtJSON = reportingDivision.toJSON().toString();

    reportingDivision = service.updateHierarchyType(testData.adminSession.getSessionId(), gtJSON);

    Assert.assertNotNull("The created hierarchy was not returned", reportingDivision);
    Assert.assertEquals("Reporting Division 2", reportingDivision.getLabel().getValue());
    Assert.assertEquals("The rporting division hieracy 2", reportingDivision.getDescription().getValue());
  }

  @Test
  public void testAddToHierarchy()
  {
    RegistryAdapterServer registry = new RegistryAdapterServer(RegistryIdService.getInstance());

    String organizationCode = null;

    GeoObjectType country = MetadataFactory.newGeoObjectType(COUNTRY.getCode(), GeometryType.POLYGON, new LocalizedValue("Country Test"), new LocalizedValue("Some Description"), true, organizationCode, registry);

    GeoObjectType province = MetadataFactory.newGeoObjectType(PROVINCE.getCode(), GeometryType.POLYGON, new LocalizedValue("Province Test"), new LocalizedValue("Some Description"), true, organizationCode, registry);

    GeoObjectType district = MetadataFactory.newGeoObjectType(DISTRICT.getCode(), GeometryType.POLYGON, new LocalizedValue("District Test"), new LocalizedValue("Some Description"), true, organizationCode, registry);

    GeoObjectType village = MetadataFactory.newGeoObjectType(VILLAGE.getCode(), GeometryType.POLYGON, new LocalizedValue("Village Test"), new LocalizedValue("Some Description"), true, organizationCode, registry);

    HierarchyType reportingDivision = MetadataFactory.newHierarchyType(REPORTING_DIVISION.getCode(), new LocalizedValue("Reporting Division"), new LocalizedValue("The reporting division hieracy..."), organizationCode, registry);

    HierarchyType administrativeDivision = MetadataFactory.newHierarchyType(ADMINISTRATIVE_DIVISION.getCode(), new LocalizedValue("Administrative Division"), new LocalizedValue("The administrative division hieracy..."), organizationCode, registry);

    // Create the GeoObjectTypes
    String gtJSON = country.toJSON().toString();
    country = service.createGeoObjectType(testData.adminSession.getSessionId(), gtJSON);

    gtJSON = province.toJSON().toString();
    province = service.createGeoObjectType(testData.adminSession.getSessionId(), gtJSON);

    gtJSON = district.toJSON().toString();
    district = service.createGeoObjectType(testData.adminSession.getSessionId(), gtJSON);

    gtJSON = village.toJSON().toString();
    village = service.createGeoObjectType(testData.adminSession.getSessionId(), gtJSON);

    String htJSON = reportingDivision.toJSON().toString();
    reportingDivision = service.createHierarchyType(testData.adminSession.getSessionId(), htJSON);

    String htJSON2 = administrativeDivision.toJSON().toString();
    administrativeDivision = service.createHierarchyType(testData.adminSession.getSessionId(), htJSON2);

    Assert.assertEquals("HierarchyType \"" + REPORTING_DIVISION.getCode() + "\" should not have any GeoObjectTypes in the hierarchy", 0, reportingDivision.getRootGeoObjectTypes().size());

    reportingDivision = service.addToHierarchy(testData.adminSession.getSessionId(), reportingDivision.getCode(), Universal.ROOT, country.getCode());

    Assert.assertEquals("HierarchyType \"" + REPORTING_DIVISION.getCode() + "\" should have one root type", 1, reportingDivision.getRootGeoObjectTypes().size());

    HierarchyType.HierarchyNode countryNode = reportingDivision.getRootGeoObjectTypes().get(0);

    Assert.assertEquals("HierarchyType \"" + REPORTING_DIVISION.getCode() + "\" should have root of type", COUNTRY.getCode(), countryNode.getGeoObjectType().getCode());

    Assert.assertEquals("GeoObjectType \"" + COUNTRY.getCode() + "\" should have no child", 0, countryNode.getChildren().size());

    reportingDivision = service.addToHierarchy(testData.adminSession.getSessionId(), reportingDivision.getCode(), country.getCode(), province.getCode());

    countryNode = reportingDivision.getRootGeoObjectTypes().get(0);

    Assert.assertEquals("GeoObjectType \"" + COUNTRY.getCode() + "\" should have one child", 1, countryNode.getChildren().size());

    HierarchyType.HierarchyNode provinceNode = countryNode.getChildren().get(0);

    Assert.assertEquals("GeoObjectType \"" + COUNTRY.getCode() + "\" should have a child of type", PROVINCE.getCode(), provinceNode.getGeoObjectType().getCode());

    reportingDivision = service.addToHierarchy(testData.adminSession.getSessionId(), reportingDivision.getCode(), province.getCode(), district.getCode());

    countryNode = reportingDivision.getRootGeoObjectTypes().get(0);
    provinceNode = countryNode.getChildren().get(0);
    HierarchyType.HierarchyNode districtNode = provinceNode.getChildren().get(0);

    Assert.assertEquals("GeoObjectType \"" + PROVINCE.getCode() + "\" should have a child of type", DISTRICT.getCode(), districtNode.getGeoObjectType().getCode());

    reportingDivision = service.addToHierarchy(testData.adminSession.getSessionId(), reportingDivision.getCode(), district.getCode(), village.getCode());

    countryNode = reportingDivision.getRootGeoObjectTypes().get(0);
    provinceNode = countryNode.getChildren().get(0);
    districtNode = provinceNode.getChildren().get(0);
    HierarchyType.HierarchyNode villageNode = districtNode.getChildren().get(0);

    Assert.assertEquals("GeoObjectType \"" + DISTRICT.getCode() + "\" should have a child of type", VILLAGE.getCode(), villageNode.getGeoObjectType().getCode());

    reportingDivision = service.removeFromHierarchy(testData.adminSession.getSessionId(), reportingDivision.getCode(), district.getCode(), village.getCode());

    countryNode = reportingDivision.getRootGeoObjectTypes().get(0);
    provinceNode = countryNode.getChildren().get(0);
    districtNode = provinceNode.getChildren().get(0);

    Assert.assertEquals("GeoObjectType \"" + DISTRICT.getCode() + "\" should have no child", 0, districtNode.getChildren().size());

    reportingDivision = service.removeFromHierarchy(testData.adminSession.getSessionId(), reportingDivision.getCode(), province.getCode(), district.getCode());

    countryNode = reportingDivision.getRootGeoObjectTypes().get(0);
    provinceNode = countryNode.getChildren().get(0);

    Assert.assertEquals("GeoObjectType \"" + PROVINCE.getCode() + "\" should have no child", 0, provinceNode.getChildren().size());

    reportingDivision = service.removeFromHierarchy(testData.adminSession.getSessionId(), reportingDivision.getCode(), country.getCode(), province.getCode());

    countryNode = reportingDivision.getRootGeoObjectTypes().get(0);

    Assert.assertEquals("GeoObjectType \"" + COUNTRY.getCode() + "\" should have no child", 0, countryNode.getChildren().size());

    reportingDivision = service.removeFromHierarchy(testData.adminSession.getSessionId(), reportingDivision.getCode(), Universal.ROOT, country.getCode());

    Assert.assertEquals("HierarchyType \"" + REPORTING_DIVISION.getCode() + "\" should not have any GeoObjectTypes in the hierarchy", 0, reportingDivision.getRootGeoObjectTypes().size());
  }

  /**
   * Leaf types cannot be parents in a hierarchy.
   */
  // Heads up: clean up
  // @Test
  // public void testAddToLeaf()
  // {
  // RegistryAdapterServer registry = new
  // RegistryAdapterServer(RegistryIdService.getInstance());
  //
  // GeoObjectType province =
  // MetadataFactory.newGeoObjectType(PROVINCE.getCode(), GeometryType.POLYGON,
  // new LocalizedValue("Province Test"), new LocalizedValue("Some
  // Description"), false, true, registry);
  //
  // GeoObjectType village = MetadataFactory.newGeoObjectType(VILLAGE.getCode(),
  // GeometryType.POINT, new LocalizedValue("Village Test"), new
  // LocalizedValue("Some Description"), false, true, registry);
  //
  // GeoObjectType household =
  // MetadataFactory.newGeoObjectType(HOUSEHOLD.getCode(), GeometryType.POINT,
  // new LocalizedValue("Household Test"), new LocalizedValue("Some
  // Description"), true, true, registry);
  //
  // HierarchyType reportingDivision =
  // MetadataFactory.newHierarchyType(REPORTING_DIVISION.getCode(), new
  // LocalizedValue("Reporting Division"), new LocalizedValue("The reporting
  // division hieracy..."), registry);
  //
  // // Create the GeoObjectTypes
  // String gtJSON = province.toJSON().toString();
  // province =
  // service.createGeoObjectType(testData.adminSession.getSessionId(), gtJSON);
  //
  // gtJSON = village.toJSON().toString();
  // village = service.createGeoObjectType(testData.adminSession.getSessionId(),
  // gtJSON);
  //
  // gtJSON = household.toJSON().toString();
  // household =
  // service.createGeoObjectType(testData.adminSession.getSessionId(), gtJSON);
  //
  // String htJSON = reportingDivision.toJSON().toString();
  // reportingDivision =
  // service.createHierarchyType(testData.adminSession.getSessionId(), htJSON);
  //
  // reportingDivision =
  // service.addToHierarchy(testData.adminSession.getSessionId(),
  // reportingDivision.getCode(), Universal.ROOT, province.getCode());
  //
  // reportingDivision =
  // service.addToHierarchy(testData.adminSession.getSessionId(),
  // reportingDivision.getCode(), province.getCode(), household.getCode());
  //
  // try
  // {
  // reportingDivision =
  // service.addToHierarchy(testData.adminSession.getSessionId(),
  // reportingDivision.getCode(), household.getCode(), village.getCode());
  // }
  // catch (RuntimeException re)
  // {
  // String expectedMessage = "You cannot add [Village Test] to the hierarchy
  // [Reporting Division] as a child to [Household Test] because [Village Test]
  // is a Leaf Type.";
  // String returnedMessage = re.getLocalizedMessage();
  //
  // Assert.assertEquals("Wrong error message returned when trying to add a
  // GeoObjectType as a child to a Leaf GeoObjectType", expectedMessage,
  // returnedMessage);
  // }
  // }
  //
  // /**
  // * Leaf types cannot be parents in a hierarchy.
  // */
  // @Test
  // public void testLeafReferenceAttributes()
  // {
  // RegistryAdapterServer registry = new
  // RegistryAdapterServer(RegistryIdService.getInstance());
  //
  // GeoObjectType village = MetadataFactory.newGeoObjectType(VILLAGE.getCode(),
  // GeometryType.POINT, new LocalizedValue("Village Test"), new
  // LocalizedValue("Some Description"), false, true, registry);
  //
  // GeoObjectType household =
  // MetadataFactory.newGeoObjectType(HOUSEHOLD.getCode(), GeometryType.POINT,
  // new LocalizedValue("Household Test"), new LocalizedValue("Some
  // Description"), true, true, registry);
  //
  // HierarchyType reportingDivision =
  // MetadataFactory.newHierarchyType(REPORTING_DIVISION.getCode(), new
  // LocalizedValue("Reporting Division"), new LocalizedValue("The reporting
  // division hieracy..."), registry);
  //
  // // Create the GeoObjectTypes
  // String gtJSON = village.toJSON().toString();
  // village = service.createGeoObjectType(testData.adminSession.getSessionId(),
  // gtJSON);
  //
  // gtJSON = household.toJSON().toString();
  // household =
  // service.createGeoObjectType(testData.adminSession.getSessionId(), gtJSON);
  //
  // String htJSON = reportingDivision.toJSON().toString();
  // reportingDivision =
  // service.createHierarchyType(testData.adminSession.getSessionId(), htJSON);
  //
  // reportingDivision =
  // service.addToHierarchy(testData.adminSession.getSessionId(),
  // reportingDivision.getCode(), Universal.ROOT, village.getCode());
  // reportingDivision =
  // service.addToHierarchy(testData.adminSession.getSessionId(),
  // reportingDivision.getCode(), village.getCode(), household.getCode());
  //
  // this.checkReferenceAttribute(reportingDivision.getCode(),
  // village.getCode(), household.getCode());
  // }

  @Request
  private void checkReferenceAttribute(String hierarchyTypeCode, String parentCode, String childCode)
  {
    Universal parentUniversal = Universal.getByKey(parentCode);
    Universal childUniversal = Universal.getByKey(childCode);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(hierarchyTypeCode);

    String refAttrName = hierarchyType.getParentReferenceAttributeName(parentUniversal);

    MdBusiness childMdBusiness = childUniversal.getMdBusiness();
    MdBusinessDAOIF childMdBusinessDAOIF = (MdBusinessDAOIF) BusinessFacade.getEntityDAO(childMdBusiness);

    try
    {
      MdAttributeConcreteDAOIF mdAttribute = childMdBusinessDAOIF.definesAttribute(refAttrName);

      Assert.assertNotNull("By adding a leaf type as a child of a non-leaf type, a reference attribute [" + refAttrName + "] to the parent was not defined on the child.", mdAttribute);

    }
    catch (DataNotFoundException e)
    {
      Assert.fail("Attribute that implements GeoObject.UID does not exist. It should be defined on the business class");
    }
  }

  @Test
  public void testHierarchyType()
  {
    HierarchyType[] hierarchyTypes = service.getHierarchyTypes(testData.adminSession.getSessionId(), null);

    for (HierarchyType hierarchyType : hierarchyTypes)
    {
      System.out.println(hierarchyType.toJSON());
    }
  }

  /**
   * The hardcoded {@link AllowedIn} and {@link LocatedIn} relationship do not
   * follow the common geo registry convention.
   */
  @Test
  public void testLocatedInCode_To_MdTermRelUniversal()
  {
    String locatedInClassName = LocatedIn.class.getSimpleName();

    String mdTermRelUniversalType = ServerHierarchyType.buildMdTermRelUniversalKey(locatedInClassName);

    Assert.assertEquals("HierarchyCode LocatedIn did not get converted to the AllowedIn Universal relationshipType.", AllowedIn.CLASS, mdTermRelUniversalType);
  }

  /**
   * The hardcoded {@link AllowedIn} and {@link LocatedIn} relationship do not
   * follow the common geo registry convention.
   */
  @Test
  public void testToMdTermRelUniversal_To_HierarchyCode()
  {
    String allowedInClass = AllowedIn.CLASS;

    String hierarchyCode = ServerHierarchyType.buildHierarchyKeyFromMdTermRelUniversal(allowedInClass);

    Assert.assertEquals("AllowedIn relationship type did not get converted into the LocatedIn  hierarchy code", LocatedIn.class.getSimpleName(), hierarchyCode);
  }

  /**
   * The hardcoded {@link AllowedIn} and {@link LocatedIn} relationship do not
   * follow the common geo registry convention.
   */
  @Test
  public void testLocatedInCode_To_MdTermRelGeoEntity()
  {
    String locatedInClassName = LocatedIn.class.getSimpleName();

    String mdTermRelGeoEntity = ServerHierarchyType.buildMdTermRelGeoEntityKey(locatedInClassName);

    Assert.assertEquals("HierarchyCode LocatedIn did not get converted to the AllowedIn Universal relationshipType.", LocatedIn.CLASS, mdTermRelGeoEntity);
  }

  /**
   * The hardcoded {@link AllowedIn} and {@link LocatedIn} relationship do not
   * follow the common geo registry convention.
   */
  @Test
  public void testToMdTermRelGeoEntity_To_HierarchyCode()
  {
    String locatedInClass = LocatedIn.CLASS;

    String hierarchyCode = ServerHierarchyType.buildHierarchyKeyFromMdTermRelGeoEntity(locatedInClass);

    Assert.assertEquals("AllowedIn relationship type did not get converted into the LocatedIn  hierarchy code", LocatedIn.class.getSimpleName(), hierarchyCode);
  }

}
