/**
 *
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.session.Request;

import net.geoprism.ontology.Classifier;
import net.geoprism.ontology.ClassifierIsARelationship;
import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.classification.ClassificationTypeTest;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.config.TestConfig;
import net.geoprism.registry.conversion.TermConverter;
import net.geoprism.registry.model.Classification;
import net.geoprism.registry.model.ClassificationType;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.permission.PermissionContext;
import net.geoprism.registry.service.business.ClassificationBusinessServiceIF;
import net.geoprism.registry.service.business.ClassificationTypeBusinessServiceIF;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectTypeInfo;
import net.geoprism.registry.test.TestRegistryClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class AttributeTypeServiceTest extends FastDatasetTest implements InstanceTestClassListener
{
  public static final TestGeoObjectTypeInfo   TEST_GOT  = new TestGeoObjectTypeInfo("GOTTest_TEST1", FastTestDataset.ORG_CGOV);

  protected static ClassificationType         type;

  protected static String                     TYPE_CODE = null;

  protected static String                     CODE      = "Classification-ROOT";

  @Autowired
  private TestRegistryClient                  client;

  @Autowired
  private ClassificationTypeBusinessServiceIF typeService;

  @Autowired
  private ClassificationBusinessServiceIF     service;

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
    type = this.typeService.apply(ClassificationTypeTest.createMock());

    Classification root = this.service.newInstance(type);
    root.setCode(CODE);
    root.setDisplayLabel(new LocalizedValue("Test Classification"));
    this.service.apply(root, null);

    TYPE_CODE = type.getCode();
  }

  @Request
  private void deleteMdClassification()
  {
    if (type != null)
    {
      this.typeService.delete(type);

      type = null;
    }
  }

  @Test
  public void testCreateGeoObjectTypeCharacter_AndUpdate()
  {
    String organizationCode = FastTestDataset.ORG_CGOV.getCode();

    GeoObjectType province = MetadataFactory.newGeoObjectType(TEST_GOT.getCode(), GeometryType.POLYGON, new LocalizedValue("Province"), new LocalizedValue(""), true, organizationCode, client.getAdapter());

    String geoObjectTypeCode = province.getCode();

    String gtJSON = province.toJSON().toString();

    AttributeType testChar = AttributeType.factory("testChar", new LocalizedValue("testCharLocalName"), new LocalizedValue("testCharLocalDescrip"), AttributeCharacterType.TYPE, false, false, false);

    this.client.createGeoObjectType(gtJSON);

    String attributeTypeJSON = testChar.toJSON().toString();
    testChar = this.client.createAttributeType(geoObjectTypeCode, attributeTypeJSON);

    net.geoprism.registry.graph.AttributeType mdAttributeConcreteDAOIF = checkAttribute(TEST_GOT.getCode(), testChar.getName());

    Assert.assertNotNull("A GeoObjectType did not define the attribute: " + testChar.getName(), mdAttributeConcreteDAOIF);
    Assert.assertTrue("A GeoObjectType did not define the attribute of the correct type: " + mdAttributeConcreteDAOIF.getType(), mdAttributeConcreteDAOIF instanceof net.geoprism.registry.graph.AttributeCharacterType);

    testChar.setLabel(MdAttributeLocalInfo.DEFAULT_LOCALE, "testCharLocalName-Update");
    testChar.setDescription(MdAttributeLocalInfo.DEFAULT_LOCALE, "testCharLocalDescrip-Update");
    attributeTypeJSON = testChar.toJSON().toString();
    testChar = this.client.updateAttributeType(geoObjectTypeCode, attributeTypeJSON);

    Assert.assertEquals("testCharLocalName-Update", testChar.getLabel().getValue());
    Assert.assertEquals("testCharLocalDescrip-Update", testChar.getDescription().getValue());
  }

  @Test
  public void testCreateGeoObjectTypeDate()
  {
    String organizationCode = FastTestDataset.ORG_CGOV.getCode();

    GeoObjectType province = MetadataFactory.newGeoObjectType(TEST_GOT.getCode(), GeometryType.POLYGON, new LocalizedValue("Province"), new LocalizedValue(""), true, organizationCode, client.getAdapter());

    String gtJSON = province.toJSON().toString();

    AttributeType testDate = AttributeType.factory("testDate", new LocalizedValue("testDateLocalName"), new LocalizedValue("testDateLocalDescrip"), AttributeDateType.TYPE, false, false, false);

    this.client.createGeoObjectType(gtJSON);

    String geoObjectTypeCode = province.getCode();
    String attributeTypeJSON = testDate.toJSON().toString();
    testDate = this.client.createAttributeType(geoObjectTypeCode, attributeTypeJSON);

    net.geoprism.registry.graph.AttributeType mdAttributeConcreteDAOIF = checkAttribute(TEST_GOT.getCode(), testDate.getName());

    Assert.assertNotNull("A GeoObjectType did not define the attribute: " + testDate.getName(), mdAttributeConcreteDAOIF);
    Assert.assertTrue("A GeoObjectType did not define the attribute of the correct type: " + mdAttributeConcreteDAOIF.getType(), mdAttributeConcreteDAOIF instanceof net.geoprism.registry.graph.AttributeDateType);
  }

  @Test
  public void testCreateGeoObjectTypeInteger()
  {
    String organizationCode = FastTestDataset.ORG_CGOV.getCode();

    GeoObjectType province = MetadataFactory.newGeoObjectType(TEST_GOT.getCode(), GeometryType.POLYGON, new LocalizedValue("Province"), new LocalizedValue(""), true, organizationCode, client.getAdapter());

    String gtJSON = province.toJSON().toString();

    AttributeType testInteger = AttributeType.factory("testInteger", new LocalizedValue("testIntegerLocalName"), new LocalizedValue("testIntegerLocalDescrip"), AttributeIntegerType.TYPE, false, false, false);

    this.client.createGeoObjectType(gtJSON);

    String geoObjectTypeCode = province.getCode();
    String attributeTypeJSON = testInteger.toJSON().toString();
    testInteger = this.client.createAttributeType(geoObjectTypeCode, attributeTypeJSON);

    net.geoprism.registry.graph.AttributeType mdAttributeConcreteDAOIF = checkAttribute(TEST_GOT.getCode(), testInteger.getName());

    Assert.assertNotNull("A GeoObjectType did not define the attribute: " + testInteger.getName(), mdAttributeConcreteDAOIF);
    Assert.assertTrue("A GeoObjectType did not define the attribute of the correct type: " + mdAttributeConcreteDAOIF.getType(), mdAttributeConcreteDAOIF instanceof net.geoprism.registry.graph.AttributeLongType);
  }

  @Test
  public void testCreateGeoObjectTypeBoolean()
  {
    String organizationCode = FastTestDataset.ORG_CGOV.getCode();

    GeoObjectType province = MetadataFactory.newGeoObjectType(TEST_GOT.getCode(), GeometryType.POLYGON, new LocalizedValue("Province"), new LocalizedValue(""), true, organizationCode, client.getAdapter());

    String gtJSON = province.toJSON().toString();

    AttributeType testBoolean = AttributeType.factory("testBoolean", new LocalizedValue("testBooleanName"), new LocalizedValue("testBooleanDescrip"), AttributeBooleanType.TYPE, false, false, false);

    this.client.createGeoObjectType(gtJSON);

    String geoObjectTypeCode = province.getCode();
    String attributeTypeJSON = testBoolean.toJSON().toString();
    testBoolean = this.client.createAttributeType(geoObjectTypeCode, attributeTypeJSON);

    net.geoprism.registry.graph.AttributeType mdAttributeConcreteDAOIF = checkAttribute(TEST_GOT.getCode(), testBoolean.getName());

    Assert.assertNotNull("A GeoObjectType did not define the attribute: " + testBoolean.getName(), mdAttributeConcreteDAOIF);
    Assert.assertTrue("A GeoObjectType did not define the attribute of the correct type: " + mdAttributeConcreteDAOIF.getType(), mdAttributeConcreteDAOIF instanceof net.geoprism.registry.graph.AttributeBooleanType);
  }

  @Test
  public void testCreateGeoObjectTypeTerm()
  {
    String organizationCode = FastTestDataset.ORG_CGOV.getCode();

    GeoObjectType province = MetadataFactory.newGeoObjectType(TEST_GOT.getCode(), GeometryType.POLYGON, new LocalizedValue("Province"), new LocalizedValue(""), true, organizationCode, client.getAdapter());

    String geoObjectTypeCode = province.getCode();

    AttributeTermType attributeTermType = (AttributeTermType) AttributeType.factory("testTerm", new LocalizedValue("Test Term Name"), new LocalizedValue("Test Term Description"), AttributeTermType.TYPE, false, false, false);
    Term term = new Term(TEST_GOT.getCode() + "_" + "testTerm", new LocalizedValue("Test Term Name"), new LocalizedValue("Test Term Description"));
    attributeTermType.setRootTerm(term);

    province.addAttribute(attributeTermType);

    String gtJSON = province.toJSON().toString();

    this.client.createGeoObjectType(gtJSON);

    String attributeTypeJSON = attributeTermType.toJSON().toString();
    attributeTermType = (AttributeTermType) this.client.createAttributeType(geoObjectTypeCode, attributeTypeJSON);

    net.geoprism.registry.graph.AttributeType mdAttributeConcreteDAOIF = checkAttribute(TEST_GOT.getCode(), attributeTermType.getName());

    Assert.assertNotNull("A GeoObjectType did not define the attribute: " + attributeTermType.getName(), mdAttributeConcreteDAOIF);
    Assert.assertTrue("A GeoObjectType did not define the attribute of the correct type: " + mdAttributeConcreteDAOIF.getType(), mdAttributeConcreteDAOIF instanceof net.geoprism.registry.graph.AttributeTermType);

    Term rootTerm = attributeTermType.getRootTerm();

    Term childTerm1 = new Term("termValue1", new LocalizedValue("Term Value 1"), new LocalizedValue(""));
    Term childTerm2 = new Term("termValue2", new LocalizedValue("Term Value 2"), new LocalizedValue(""));

    this.client.createTerm(rootTerm.getCode(), childTerm1.toJSON().toString());
    this.client.createTerm(rootTerm.getCode(), childTerm2.toJSON().toString());

    province = this.client.getGeoObjectTypes(new String[] { TEST_GOT.getCode() }, PermissionContext.READ)[0];
    AttributeTermType attributeTermType2 = (AttributeTermType) province.getAttribute("testTerm").get();

    // Check to see if the cache was updated.
    checkTermsCreate(attributeTermType2);

    attributeTermType.setLabel(MdAttributeLocalInfo.DEFAULT_LOCALE, "Test Term Name Update");
    attributeTermType.setDescription(MdAttributeLocalInfo.DEFAULT_LOCALE, "Test Term Description Update");

    attributeTermType = (AttributeTermType) this.client.updateAttributeType(geoObjectTypeCode, attributeTermType.toJSON().toString());

    Assert.assertEquals(attributeTermType.getLabel().getValue(), "Test Term Name Update");
    Assert.assertEquals(attributeTermType.getDescription().getValue(), "Test Term Description Update");

    checkTermsCreate(attributeTermType);

    // Test updating the term
    childTerm2 = new Term("termValue2", new LocalizedValue("Term Value 2a"), new LocalizedValue(""));

    this.client.updateTerm(rootTerm.getCode(), childTerm2.toJSON().toString());

    province = this.client.getGeoObjectTypes(new String[] { TEST_GOT.getCode() }, PermissionContext.READ)[0];
    AttributeTermType attributeTermType3 = (AttributeTermType) province.getAttribute("testTerm").get();

    checkTermsUpdate(attributeTermType3);

    this.client.deleteTerm(rootTerm.getCode(), "termValue2");

    province = this.client.getGeoObjectTypes(new String[] { TEST_GOT.getCode() }, PermissionContext.READ)[0];
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

    GeoObjectType province = MetadataFactory.newGeoObjectType(TEST_GOT.getCode(), GeometryType.POLYGON, new LocalizedValue("Province"), new LocalizedValue(""), true, organizationCode, client.getAdapter());
    province.addAttribute(attributeClassificationType);

    String geoObjectTypeCode = province.getCode();

    String gtJSON = province.toJSON().toString();

    this.client.createGeoObjectType(gtJSON);

    String attributeTypeJSON = attributeClassificationType.toJSON().toString();
    attributeClassificationType = (AttributeClassificationType) this.client.createAttributeType(geoObjectTypeCode, attributeTypeJSON);

    net.geoprism.registry.graph.AttributeType mdAttributeConcreteDAOIF = checkAttribute(TEST_GOT.getCode(), attributeClassificationType.getName());

    Assert.assertNotNull("A GeoObjectType did not define the attribute: " + attributeClassificationType.getName(), mdAttributeConcreteDAOIF);
    Assert.assertTrue("A GeoObjectType did not define the attribute of the correct type: " + mdAttributeConcreteDAOIF.getType(), mdAttributeConcreteDAOIF instanceof net.geoprism.registry.graph.AttributeClassificationType);

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
  private net.geoprism.registry.graph.AttributeType checkAttribute(String geoObjectTypeCode, String attributeName)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(geoObjectTypeCode);
    return type.getAttribute(attributeName).orElseThrow();
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
