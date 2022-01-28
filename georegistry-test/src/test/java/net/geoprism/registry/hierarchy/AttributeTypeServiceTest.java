/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.hierarchy;

import java.util.List;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.MetadataFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.MdAttributeBooleanDAOIF;
import com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF;
import com.runwaysdk.dataaccess.MdAttributeClassificationDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdAttributeLongDAOIF;
import com.runwaysdk.dataaccess.MdAttributeMomentDAOIF;
import com.runwaysdk.dataaccess.MdAttributeTermDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.session.Request;

import net.geoprism.ontology.Classifier;
import net.geoprism.ontology.ClassifierIsARelationship;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.classification.ClassificationTypeTest;
import net.geoprism.registry.conversion.TermConverter;
import net.geoprism.registry.model.Classification;
import net.geoprism.registry.model.ClassificationType;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.permission.PermissionContext;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectTypeInfo;

public class AttributeTypeServiceTest
{
  public static final TestGeoObjectTypeInfo TEST_GOT  = new TestGeoObjectTypeInfo("GOTTest_TEST1", FastTestDataset.ORG_CGOV);

  protected static FastTestDataset          testData;

  protected static ClassificationType       type;

  protected static String                   TYPE_CODE = null;

  protected static String                   CODE      = "Classification-ROOT";

  @BeforeClass
  public static void setUpClass()
  {
    testData = FastTestDataset.newTestData();
    testData.setUpMetadata();
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

    setUpExtras();

    testData.logIn(FastTestDataset.USER_CGOV_RA);
  }

  @After
  public void tearDown()
  {
    testData.logOut();

    cleanUpExtras();

    testData.tearDownInstanceData();
  }

  private void cleanUpExtras()
  {
    TestDataSet.deleteClassifier("termValue1");
    TestDataSet.deleteClassifier("termValue2");

    TEST_GOT.delete();

    deleteMdClassification();
  }

  private void setUpExtras()
  {
    cleanUpExtras();

    createMdClassification();
  }

  @Request
  private void createMdClassification()
  {
    type = ClassificationType.apply(ClassificationTypeTest.createMock());

    Classification root = Classification.newInstance(type);
    root.setCode(CODE);
    root.setDisplayLabel(new LocalizedValue("Test Classification"));
    root.apply(null);

    TYPE_CODE = type.getCode();
  }

  @Request
  private void deleteMdClassification()
  {
    if (type != null)
    {
      type.delete();

      type = null;
    }
  }

  @Test
  public void testCreateGeoObjectTypeCharacter_AndUpdate()
  {
    String organizationCode = testData.ORG_CGOV.getCode();

    GeoObjectType province = MetadataFactory.newGeoObjectType(TEST_GOT.getCode(), GeometryType.POLYGON, new LocalizedValue("Province"), new LocalizedValue(""), true, organizationCode, testData.adapter);

    String geoObjectTypeCode = province.getCode();

    String gtJSON = province.toJSON().toString();

    AttributeType testChar = AttributeType.factory("testChar", new LocalizedValue("testCharLocalName"), new LocalizedValue("testCharLocalDescrip"), AttributeCharacterType.TYPE, false, false, false);

    testData.adapter.createGeoObjectType(gtJSON);
    String attributeTypeJSON = testChar.toJSON().toString();
    testChar = testData.adapter.createAttributeType(geoObjectTypeCode, attributeTypeJSON);

    MdAttributeDAOIF mdAttributeConcreteDAOIF = checkAttribute(TEST_GOT.getCode(), testChar.getName());

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

    testData.adapter.createGeoObjectType(gtJSON);

    String geoObjectTypeCode = province.getCode();
    String attributeTypeJSON = testDate.toJSON().toString();
    testDate = testData.adapter.createAttributeType(geoObjectTypeCode, attributeTypeJSON);

    MdAttributeDAOIF mdAttributeConcreteDAOIF = checkAttribute(TEST_GOT.getCode(), testDate.getName());

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

    testData.adapter.createGeoObjectType(gtJSON);

    String geoObjectTypeCode = province.getCode();
    String attributeTypeJSON = testInteger.toJSON().toString();
    testInteger = testData.adapter.createAttributeType(geoObjectTypeCode, attributeTypeJSON);

    MdAttributeDAOIF mdAttributeConcreteDAOIF = checkAttribute(TEST_GOT.getCode(), testInteger.getName());

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

    testData.adapter.createGeoObjectType(gtJSON);

    String geoObjectTypeCode = province.getCode();
    String attributeTypeJSON = testBoolean.toJSON().toString();
    testBoolean = testData.adapter.createAttributeType(geoObjectTypeCode, attributeTypeJSON);

    MdAttributeDAOIF mdAttributeConcreteDAOIF = checkAttribute(TEST_GOT.getCode(), testBoolean.getName());

    Assert.assertNotNull("A GeoObjectType did not define the attribute: " + testBoolean.getName(), mdAttributeConcreteDAOIF);
    Assert.assertTrue("A GeoObjectType did not define the attribute of the correct type: " + mdAttributeConcreteDAOIF.getType(), mdAttributeConcreteDAOIF instanceof MdAttributeBooleanDAOIF);
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

    testData.adapter.createGeoObjectType(gtJSON);

    String attributeTypeJSON = attributeTermType.toJSON().toString();
    attributeTermType = (AttributeTermType) testData.adapter.createAttributeType(geoObjectTypeCode, attributeTypeJSON);

    MdAttributeDAOIF mdAttributeConcreteDAOIF = checkAttribute(TEST_GOT.getCode(), attributeTermType.getName());

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

  @Test
  public void testCreateGeoObjectTypeClassification()
  {
    String organizationCode = FastTestDataset.ORG_CGOV.getCode();

    AttributeClassificationType attributeClassificationType = (AttributeClassificationType) AttributeType.factory("testClassification", new LocalizedValue("Test Classification Name"), new LocalizedValue("Test Classification Description"), AttributeClassificationType.TYPE, false, false, false);
    attributeClassificationType.setClassificationType(TYPE_CODE);
    attributeClassificationType.setRootTerm(new Term(CODE, new LocalizedValue("Test Term Name"), new LocalizedValue("Test Term Description")));

    GeoObjectType province = MetadataFactory.newGeoObjectType(TEST_GOT.getCode(), GeometryType.POLYGON, new LocalizedValue("Province"), new LocalizedValue(""), true, organizationCode, testData.adapter);
    province.addAttribute(attributeClassificationType);

    String geoObjectTypeCode = province.getCode();

    String gtJSON = province.toJSON().toString();

    testData.adapter.createGeoObjectType(gtJSON);

    String attributeTypeJSON = attributeClassificationType.toJSON().toString();
    attributeClassificationType = (AttributeClassificationType) testData.adapter.createAttributeType(geoObjectTypeCode, attributeTypeJSON);

    MdAttributeDAOIF mdAttributeConcreteDAOIF = checkAttribute(TEST_GOT.getCode(), attributeClassificationType.getName());

    Assert.assertNotNull("A GeoObjectType did not define the attribute: " + attributeClassificationType.getName(), mdAttributeConcreteDAOIF);
    Assert.assertTrue("A GeoObjectType did not define the attribute of the correct type: " + mdAttributeConcreteDAOIF.getType(), mdAttributeConcreteDAOIF instanceof MdAttributeClassificationDAOIF);

    Term rootTerm = attributeClassificationType.getRootTerm();

    Assert.assertNotNull("AttributeClassification root term not set correctly: " + attributeClassificationType.getName(), rootTerm);
  }

  // @Request
  // private VertexObject createRootClassification(MdClassificationDAO
  // mdClassification)
  // {
  // MdVertexDAOIF mdVertexDAO = mdClassification.getReferenceMdVertexDAO();
  //
  // VertexObject classification = (VertexObject)
  // VertexObject.instantiate(VertexObjectDAO.newInstance(mdVertexDAO));
  // classification.setEmbeddedValue(VertexObject.DISPLAYLABEL,
  // MdAttributeLocalInfo.DEFAULT_LOCALE, "test");
  // // classification.setCode()
  // }

  /*
   * Utility methods
   */

  @Request
  private MdAttributeDAOIF checkAttribute(String geoObjectTypeCode, String attributeName)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(geoObjectTypeCode);
    MdVertexDAOIF mdVertex = type.getMdVertex();

    return mdVertex.definesAttribute(attributeName);
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
    Classifier rootTestClassifier = Classifier.getRoot();

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
}
