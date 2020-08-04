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

import org.commongeoregistry.adapter.RegistryAdapter;
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

import net.geoprism.ontology.Classifier;
import net.geoprism.ontology.ClassifierIsARelationship;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.conversion.TermConverter;
import net.geoprism.registry.graph.GeoVertexType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.permission.PermissionContext;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestGeoObjectTypeInfo;
import net.geoprism.registry.test.TestHierarchyTypeInfo;

public class HierarchyManagementServiceTest
{

  protected static FastTestDataset         testData;
  
  public static TestGeoObjectTypeInfo  TEST_GOT;

  private static TestHierarchyTypeInfo TEST_HT;

  private final static String          ROOT_TEST_TERM_CLASSIFIER_ID = "TEST";

  private static String                ROOT_TEST_TERM_KEY           = null;

  @BeforeClass
  public static void setUpClass()
  {
    testData = FastTestDataset.newTestData();
    testData.setUpMetadata();

    TEST_GOT = testData.newTestGeoObjectTypeInfo("HMST_Country", testData.ORG_CGOV);

    TEST_HT = new TestHierarchyTypeInfo("HMST_ReportDiv", testData.ORG_CGOV);
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

      deleteExtraMetadata();
    }

    setUpInRequest();
    
    testData.logIn(testData.USER_CGOV_RA);
  }

  @After
  public void tearDown()
  {
    testData.logOut();
    
    deleteExtraMetadata();

    testData.tearDownInstanceData();

    tearDownInRequest();
  }

  private void deleteExtraMetadata()
  {
    TEST_HT.delete();
    TEST_GOT.delete();
  }

  @Request
  private static void setUpInRequest()
  {
    setUpTransaction();
  }

  @Transaction
  private static void setUpTransaction()
  {
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
    }
    catch (RuntimeException e)
    {
      e.printStackTrace();
    }
  }

  @Test
  public void testCreateGeoObjectType()
  {
    String organizationCode = testData.ORG_CGOV.getCode();

    GeoObjectType province = MetadataFactory.newGeoObjectType(TEST_GOT.getCode(), GeometryType.POLYGON, new LocalizedValue("Province"), new LocalizedValue(""), true, organizationCode, testData.adapter);

    String gtJSON = province.toJSON().toString();

    testData.adapter.createGeoObjectType(testData.clientSession.getSessionId(), gtJSON);

    checkMdBusinessAttributes(TEST_GOT.getCode());
    checkMdGraphAttributes(TEST_GOT.getCode());
  }

  @Test
  public void testCreateGeoObjectTypeCharacter_AndUpdate()
  {
    String organizationCode = testData.ORG_CGOV.getCode();

    GeoObjectType province = MetadataFactory.newGeoObjectType(TEST_GOT.getCode(), GeometryType.POLYGON, new LocalizedValue("Province"), new LocalizedValue(""), true, organizationCode, testData.adapter);

    String geoObjectTypeCode = province.getCode();

    String gtJSON = province.toJSON().toString();

    AttributeType testChar = AttributeType.factory("testChar", new LocalizedValue("testCharLocalName"), new LocalizedValue("testCharLocalDescrip"), AttributeCharacterType.TYPE, false, false, false);

    testData.adapter.createGeoObjectType(testData.clientSession.getSessionId(), gtJSON);
    String attributeTypeJSON = testChar.toJSON().toString();
    testChar = testData.adapter.createAttributeType(geoObjectTypeCode, attributeTypeJSON);

    MdAttributeConcreteDAOIF mdAttributeConcreteDAOIF = checkAttribute(TEST_GOT.getCode(), testChar.getName());

    Assert.assertNotNull("A GeoObjectType did not define the attribute: " + testChar.getName(), mdAttributeConcreteDAOIF);
    Assert.assertTrue("A GeoObjectType did not define the attribute of the correct type: " + mdAttributeConcreteDAOIF.getType(), mdAttributeConcreteDAOIF instanceof MdAttributeCharacterDAOIF);

    testChar.setLabel(MdAttributeLocalInfo.DEFAULT_LOCALE, "testCharLocalName-Update");
    testChar.setDescription(MdAttributeLocalInfo.DEFAULT_LOCALE, "testCharLocalDescrip-Update");
    attributeTypeJSON = testChar.toJSON().toString();
    testChar = testData.adapter.updateAttributeType(geoObjectTypeCode, attributeTypeJSON);

    Assert.assertEquals("testCharLocalName-Update", testChar.getLabel().getValue());
    Assert.assertEquals("testCharLocalDescrip-Update", testChar.getDescription().getValue());
  }

  @Test
  public void testCreateGeoObjectTypeDate()
  {
    String organizationCode = testData.ORG_CGOV.getCode();

    GeoObjectType province = MetadataFactory.newGeoObjectType(TEST_GOT.getCode(), GeometryType.POLYGON, new LocalizedValue("Province"), new LocalizedValue(""), true, organizationCode, testData.adapter);

    String gtJSON = province.toJSON().toString();

    AttributeType testDate = AttributeType.factory("testDate", new LocalizedValue("testDateLocalName"), new LocalizedValue("testDateLocalDescrip"), AttributeDateType.TYPE, false, false, false);

    testData.adapter.createGeoObjectType(testData.clientSession.getSessionId(), gtJSON);

    String geoObjectTypeCode = province.getCode();
    String attributeTypeJSON = testDate.toJSON().toString();
    testDate = testData.adapter.createAttributeType(geoObjectTypeCode, attributeTypeJSON);

    MdAttributeConcreteDAOIF mdAttributeConcreteDAOIF = checkAttribute(TEST_GOT.getCode(), testDate.getName());

    Assert.assertNotNull("A GeoObjectType did not define the attribute: " + testDate.getName(), mdAttributeConcreteDAOIF);
    Assert.assertTrue("A GeoObjectType did not define the attribute of the correct type: " + mdAttributeConcreteDAOIF.getType(), mdAttributeConcreteDAOIF instanceof MdAttributeMomentDAOIF);
  }

  @Test
  public void testCreateGeoObjectTypeInteger()
  {
    String organizationCode = testData.ORG_CGOV.getCode();

    GeoObjectType province = MetadataFactory.newGeoObjectType(TEST_GOT.getCode(), GeometryType.POLYGON, new LocalizedValue("Province"), new LocalizedValue(""), true, organizationCode, testData.adapter);

    String gtJSON = province.toJSON().toString();

    AttributeType testInteger = AttributeType.factory("testInteger", new LocalizedValue("testIntegerLocalName"), new LocalizedValue("testIntegerLocalDescrip"), AttributeIntegerType.TYPE, false, false, false);

    testData.adapter.createGeoObjectType(testData.clientSession.getSessionId(), gtJSON);

    String geoObjectTypeCode = province.getCode();
    String attributeTypeJSON = testInteger.toJSON().toString();
    testInteger = testData.adapter.createAttributeType(geoObjectTypeCode, attributeTypeJSON);

    MdAttributeConcreteDAOIF mdAttributeConcreteDAOIF = checkAttribute(TEST_GOT.getCode(), testInteger.getName());

    Assert.assertNotNull("A GeoObjectType did not define the attribute: " + testInteger.getName(), mdAttributeConcreteDAOIF);
    Assert.assertTrue("A GeoObjectType did not define the attribute of the correct type: " + mdAttributeConcreteDAOIF.getType(), mdAttributeConcreteDAOIF instanceof MdAttributeLongDAOIF);
  }

  @Test
  public void testCreateGeoObjectTypeBoolean()
  {
    String organizationCode = testData.ORG_CGOV.getCode();

    GeoObjectType province = MetadataFactory.newGeoObjectType(TEST_GOT.getCode(), GeometryType.POLYGON, new LocalizedValue("Province"), new LocalizedValue(""), true, organizationCode, testData.adapter);

    String gtJSON = province.toJSON().toString();

    AttributeType testBoolean = AttributeType.factory("testBoolean", new LocalizedValue("testBooleanName"), new LocalizedValue("testBooleanDescrip"), AttributeBooleanType.TYPE, false, false, false);

    testData.adapter.createGeoObjectType(testData.clientSession.getSessionId(), gtJSON);

    String geoObjectTypeCode = province.getCode();
    String attributeTypeJSON = testBoolean.toJSON().toString();
    testBoolean = testData.adapter.createAttributeType(geoObjectTypeCode, attributeTypeJSON);

    MdAttributeConcreteDAOIF mdAttributeConcreteDAOIF = checkAttribute(TEST_GOT.getCode(), testBoolean.getName());

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
    String className = "TestClass";
    String attributeName = "SomeAttribute";

    String classifierId = TermConverter.buildRootClassClassifierId(className);

     Classifier classifier = this.buildClassAttributeClassifierTree(className, attributeName);

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
    String organizationCode = testData.ORG_CGOV.getCode();

    GeoObjectType province = MetadataFactory.newGeoObjectType(TEST_GOT.getCode(), GeometryType.POLYGON, new LocalizedValue("Province"), new LocalizedValue(""), true, organizationCode, testData.adapter);

    String geoObjectTypeCode = province.getCode();

    AttributeTermType attributeTermType = (AttributeTermType) AttributeType.factory("testTerm", new LocalizedValue("Test Term Name"), new LocalizedValue("Test Term Description"), AttributeTermType.TYPE, false, false, false);
    Term term = new Term(TEST_GOT.getCode() + "_" + "testTerm", new LocalizedValue("Test Term Name"), new LocalizedValue("Test Term Description"));
    attributeTermType.setRootTerm(term);

    province.addAttribute(attributeTermType);

    String gtJSON = province.toJSON().toString();

    testData.adapter.createGeoObjectType(testData.clientSession.getSessionId(), gtJSON);

    String attributeTypeJSON = attributeTermType.toJSON().toString();
    attributeTermType = (AttributeTermType) testData.adapter.createAttributeType(geoObjectTypeCode, attributeTypeJSON);

    MdAttributeConcreteDAOIF mdAttributeConcreteDAOIF = checkAttribute(TEST_GOT.getCode(), attributeTermType.getName());

    Assert.assertNotNull("A GeoObjectType did not define the attribute: " + attributeTermType.getName(), mdAttributeConcreteDAOIF);
    Assert.assertTrue("A GeoObjectType did not define the attribute of the correct type: " + mdAttributeConcreteDAOIF.getType(), mdAttributeConcreteDAOIF instanceof MdAttributeTermDAOIF);

    Term rootTerm = attributeTermType.getRootTerm();

    Term childTerm1 = new Term("termValue1", new LocalizedValue("Term Value 1"), new LocalizedValue(""));
    Term childTerm2 = new Term("termValue2", new LocalizedValue("Term Value 2"), new LocalizedValue(""));

    testData.adapter.createTerm(rootTerm.getCode(), childTerm1.toJSON().toString());
    testData.adapter.createTerm(rootTerm.getCode(), childTerm2.toJSON().toString());

    province = testData.adapter.getGeoObjectTypes(new String[] { TEST_GOT.getCode() }, null, PermissionContext.READ)[0];
    AttributeTermType attributeTermType2 = (AttributeTermType) province.getAttribute("testTerm").get();

    // Check to see if the cache was updated.
    checkTermsCreate(attributeTermType2);

    attributeTermType.setLabel(MdAttributeLocalInfo.DEFAULT_LOCALE, "Test Term Name Update");
    attributeTermType.setDescription(MdAttributeLocalInfo.DEFAULT_LOCALE, "Test Term Description Update");

    attributeTermType = (AttributeTermType) testData.adapter.updateAttributeType(geoObjectTypeCode, attributeTermType.toJSON().toString());

    Assert.assertEquals(attributeTermType.getLabel().getValue(), "Test Term Name Update");
    Assert.assertEquals(attributeTermType.getDescription().getValue(), "Test Term Description Update");

    checkTermsCreate(attributeTermType);

    // Test updating the term
    childTerm2 = new Term("termValue2", new LocalizedValue("Term Value 2a"), new LocalizedValue(""));

    testData.adapter.updateTerm(rootTerm.getCode(), childTerm2.toJSON().toString());

    province = testData.adapter.getGeoObjectTypes(new String[] { TEST_GOT.getCode() }, null, PermissionContext.READ)[0];
    AttributeTermType attributeTermType3 = (AttributeTermType) province.getAttribute("testTerm").get();

    checkTermsUpdate(attributeTermType3);

    testData.adapter.deleteTerm(rootTerm.getCode(), "termValue2");

    province = testData.adapter.getGeoObjectTypes(new String[] { TEST_GOT.getCode() }, null, PermissionContext.READ)[0];
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
  // @Test
  // public void testCreateGeoObjectTypePoint()
  // {
  // RegistryAdapterServer registry = new
  // RegistryAdapterServer(RegistryIdService.getInstance());
  //
  // String organizationCode = testData.ORG_CGOV.getCode();
  //
  // GeoObjectType village = MetadataFactory.newGeoObjectType(VILLAGE.getCode(),
  // GeometryType.POINT, new LocalizedValue("Village"), new LocalizedValue(""),
  // true, organizationCode, registry);
  //
  // String villageJSON = village.toJSON().toString();
  //
  // service.createGeoObjectType(testData.adminSession.getSessionId(),
  // villageJSON);
  //
  // checkAttributePoint(VILLAGE.getCode());
  // }
  //
  // @Test
  // public void testCreateGeoObjectTypeLine()
  // {
  // RegistryAdapterServer registry = new
  // RegistryAdapterServer(RegistryIdService.getInstance());
  //
  // String organizationCode = testData.ORG_CGOV.getCode();
  //
  // GeoObjectType river = MetadataFactory.newGeoObjectType(RIVER.getCode(),
  // GeometryType.LINE, new LocalizedValue("River"), new LocalizedValue(""),
  // true, organizationCode, registry);
  //
  // String riverJSON = river.toJSON().toString();
  //
  // service.createGeoObjectType(testData.adminSession.getSessionId(),
  // riverJSON);
  //
  // checkAttributeLine(RIVER.getCode());
  // }
  //
  // @Test
  // public void testCreateGeoObjectTypePolygon()
  // {
  // RegistryAdapterServer registry = new
  // RegistryAdapterServer(RegistryIdService.getInstance());
  //
  // String organizationCode = testData.ORG_CGOV.getCode();
  //
  // GeoObjectType geoObjectType =
  // MetadataFactory.newGeoObjectType(DISTRICT.getCode(), GeometryType.POLYGON,
  // new LocalizedValue("District"), new LocalizedValue(""), true,
  // organizationCode, registry);
  //
  // String gtJSON = geoObjectType.toJSON().toString();
  //
  // service.createGeoObjectType(testData.adminSession.getSessionId(), gtJSON);
  //
  // checkAttributePolygon(DISTRICT.getCode());
  // }
  //
  // @Test
  // public void testCreateGeoObjectTypeMultiPoint()
  // {
  // RegistryAdapterServer registry = new
  // RegistryAdapterServer(RegistryIdService.getInstance());
  //
  // String organizationCode = testData.ORG_CGOV.getCode();
  //
  // GeoObjectType village = MetadataFactory.newGeoObjectType(VILLAGE.getCode(),
  // GeometryType.MULTIPOINT, new LocalizedValue("Village"), new
  // LocalizedValue(""), true, organizationCode, registry);
  //
  // String villageJSON = village.toJSON().toString();
  //
  // service.createGeoObjectType(testData.adminSession.getSessionId(),
  // villageJSON);
  //
  // checkAttributeMultiPoint(VILLAGE.getCode());
  // }
  //
  // @Test
  // public void testCreateGeoObjectTypeMultiLine()
  // {
  // RegistryAdapterServer registry = new
  // RegistryAdapterServer(RegistryIdService.getInstance());
  //
  // String organizationCode = testData.ORG_CGOV.getCode();
  //
  // GeoObjectType river = MetadataFactory.newGeoObjectType(RIVER.getCode(),
  // GeometryType.MULTILINE, new LocalizedValue("River"), new
  // LocalizedValue(""),true, organizationCode, registry);
  //
  // String riverJSON = river.toJSON().toString();
  //
  // service.createGeoObjectType(testData.adminSession.getSessionId(),
  // riverJSON);
  //
  // checkAttributeMultiLine(RIVER.getCode());
  // }
  //
  // @Test
  // public void testCreateGeoObjectTypeMultiPolygon()
  // {
  // RegistryAdapterServer registry = new
  // RegistryAdapterServer(RegistryIdService.getInstance());
  //
  // String organizationCode = testData.ORG_CGOV.getCode();
  //
  // GeoObjectType geoObjectType =
  // MetadataFactory.newGeoObjectType(DISTRICT.getCode(),
  // GeometryType.MULTIPOLYGON, new LocalizedValue("District"), new
  // LocalizedValue(""), true, organizationCode, registry);
  //
  // String gtJSON = geoObjectType.toJSON().toString();
  //
  // service.createGeoObjectType(testData.adminSession.getSessionId(), gtJSON);
  //
  // checkAttributeMultiPolygon(DISTRICT.getCode());
  // }

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
    String organizationCode = testData.ORG_CGOV.getCode();

    GeoObjectType province = MetadataFactory.newGeoObjectType(TEST_GOT.getCode(), GeometryType.POLYGON, new LocalizedValue("Province Test"), new LocalizedValue("Some Description"), true, organizationCode, testData.adapter);

    String gtJSON = province.toJSON().toString();

    testData.adapter.createGeoObjectType(testData.clientSession.getSessionId(), gtJSON);

    province = testData.adapter.getGeoObjectTypes(new String[] { TEST_GOT.getCode() }, null, PermissionContext.READ)[0];

    province.setLabel(MdAttributeLocalInfo.DEFAULT_LOCALE, "Province Test 2");
    province.setDescription(MdAttributeLocalInfo.DEFAULT_LOCALE, "Some Description 2");

    gtJSON = province.toJSON().toString();
    testData.adapter.updateGeoObjectType(gtJSON);

    province = testData.adapter.getGeoObjectTypes(new String[] { TEST_GOT.getCode() }, null, PermissionContext.READ)[0];

    Assert.assertEquals("Display label was not updated on a GeoObjectType", "Province Test 2", province.getLabel().getValue());
    Assert.assertEquals("Description  was not updated on a GeoObjectType", "Some Description 2", province.getDescription().getValue());
  }

  @Test
  public void testCreateHierarchyType()
  {
    String organizationCode = testData.ORG_CGOV.getCode();

    HierarchyType reportingDivision = MetadataFactory.newHierarchyType(TEST_HT.getCode(), new LocalizedValue("Reporting Division"), new LocalizedValue("The rporting division hieracy..."), organizationCode, testData.adapter);
    String gtJSON = reportingDivision.toJSON().toString();

    ServiceFactory.getHierarchyService().createHierarchyType(testData.clientSession.getSessionId(), gtJSON);

    HierarchyType[] hierarchies = ServiceFactory.getHierarchyService().getHierarchyTypes(testData.clientSession.getSessionId(), new String[] { TEST_HT.getCode() }, PermissionContext.READ);

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

    String organizationCode = testData.ORG_CGOV.getCode();

    reportingDivision = MetadataFactory.newHierarchyType(TEST_HT.getCode(), new LocalizedValue("Reporting Division"), new LocalizedValue("The rporting division hieracy..."), organizationCode, testData.adapter);
    String gtJSON = reportingDivision.toJSON().toString();

    ServiceFactory.getHierarchyService().createHierarchyType(testData.clientSession.getSessionId(), gtJSON);

    HierarchyType[] hierarchies = ServiceFactory.getHierarchyService().getHierarchyTypes(testData.clientSession.getSessionId(), new String[] { TEST_HT.getCode() }, PermissionContext.READ);

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

  @Test
  public void testUpdateHierarchyType()
  {
    HierarchyType reportingDivision = testData.HIER_ADMIN.toDTO();
    String gtJSON = reportingDivision.toJSON().toString();

    reportingDivision.setLabel(new LocalizedValue("Reporting Division 2"));

    reportingDivision.setDescription(new LocalizedValue("The rporting division hieracy 2"));

    gtJSON = reportingDivision.toJSON().toString();

    reportingDivision = ServiceFactory.getHierarchyService().updateHierarchyType(testData.clientSession.getSessionId(), gtJSON);

    Assert.assertNotNull("The created hierarchy was not returned", reportingDivision);
    Assert.assertEquals("Reporting Division 2", reportingDivision.getLabel().getValue());
    Assert.assertEquals("The rporting division hieracy 2", reportingDivision.getDescription().getValue());
  }

  @Test
  public void testAddToHierarchy()
  {
    String organizationCode = testData.ORG_CGOV.getCode();

    GeoObjectType country = MetadataFactory.newGeoObjectType(TEST_GOT.getCode(), GeometryType.POLYGON, new LocalizedValue("Country Test"), new LocalizedValue("Some Description"), true, organizationCode, testData.adapter);

    // Create the GeoObjectTypes
    String gtJSON = country.toJSON().toString();
    country = testData.adapter.createGeoObjectType(testData.clientSession.getSessionId(), gtJSON);

    HierarchyType adminHierarchy = ServiceFactory.getHierarchyService().addToHierarchy(testData.clientSession.getSessionId(), testData.HIER_ADMIN.getCode(), Universal.ROOT, country.getCode());

    List<HierarchyType.HierarchyNode> rootGots = adminHierarchy.getRootGeoObjectTypes();

    for (HierarchyType.HierarchyNode node : rootGots)
    {
      if (node.getGeoObjectType().getCode().equals(country.getCode()))
      {
        return;
      }
    }

    Assert.fail("We did not find the child we just added.");
  }

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
    HierarchyType[] hierarchyTypes = ServiceFactory.getHierarchyService().getHierarchyTypes(testData.clientSession.getSessionId(), null, PermissionContext.READ);

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
  
  
  
  
  
  @Request
  private void checkMdGraphAttributes(String code)
  {
    MdGeoVertexDAOIF mdGraphClassDAOIF = (MdGeoVertexDAOIF) MdGeoVertexDAO.get(MdGeoVertexInfo.CLASS, GeoVertexType.buildMdGeoVertexKey(code));

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

}
