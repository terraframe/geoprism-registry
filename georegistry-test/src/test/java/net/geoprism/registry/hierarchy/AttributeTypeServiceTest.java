/**
 *
 */
package net.geoprism.registry.hierarchy;

import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.CodeReference;
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

import com.runwaysdk.session.Request;

import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectTypeInfo;
import net.geoprism.registry.test.TestRegistryClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class AttributeTypeServiceTest extends FastDatasetTest implements InstanceTestClassListener
{
  public static final TestGeoObjectTypeInfo TEST_GOT  = new TestGeoObjectTypeInfo("GOTTest_TEST1", FastTestDataset.ORG_CGOV);

  protected static String                   TYPE_CODE = null;

  @Autowired
  private TestRegistryClient                client;

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
    TEST_GOT.delete();
  }

  private void setUpExtras()
  {
    cleanUpExtras();
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

    net.geoprism.registry.graph.AttributeType mdAttributeConcreteDAOIF = checkAttribute(TEST_GOT.getCode(), testChar.getCode());

    Assert.assertNotNull("A GeoObjectType did not define the attribute: " + testChar.getCode(), mdAttributeConcreteDAOIF);
    Assert.assertTrue("A GeoObjectType did not define the attribute of the correct type: " + mdAttributeConcreteDAOIF.getType(), mdAttributeConcreteDAOIF instanceof net.geoprism.registry.graph.AttributeCharacterType);

    testChar.setLabel(new LocalizedValue("testCharLocalName-Update"));
    testChar.setDescription(new LocalizedValue("testCharLocalDescrip-Update"));
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

    net.geoprism.registry.graph.AttributeType mdAttributeConcreteDAOIF = checkAttribute(TEST_GOT.getCode(), testDate.getCode());

    Assert.assertNotNull("A GeoObjectType did not define the attribute: " + testDate.getCode(), mdAttributeConcreteDAOIF);
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

    net.geoprism.registry.graph.AttributeType mdAttributeConcreteDAOIF = checkAttribute(TEST_GOT.getCode(), testInteger.getCode());

    Assert.assertNotNull("A GeoObjectType did not define the attribute: " + testInteger.getCode(), mdAttributeConcreteDAOIF);
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

    net.geoprism.registry.graph.AttributeType mdAttributeConcreteDAOIF = checkAttribute(TEST_GOT.getCode(), testBoolean.getCode());

    Assert.assertNotNull("A GeoObjectType did not define the attribute: " + testBoolean.getCode(), mdAttributeConcreteDAOIF);
    Assert.assertTrue("A GeoObjectType did not define the attribute of the correct type: " + mdAttributeConcreteDAOIF.getType(), mdAttributeConcreteDAOIF instanceof net.geoprism.registry.graph.AttributeBooleanType);
  }

  @Test
  public void testCreateGeoObjectTypeClassification()
  {
    String organizationCode = FastTestDataset.ORG_CGOV.getCode();

    AttributeClassificationType attributeClassificationType = (AttributeClassificationType) AttributeType.factory("testClassification", new LocalizedValue("Test Classification Name"), new LocalizedValue("Test Classification Description"), AttributeClassificationType.TYPE, false, false, false);
    attributeClassificationType.setConceptSet(cSet.getCode());
    attributeClassificationType.setRootTerm(CodeReference.build(rootConcept.getCode(), rootConcept.getType().getCode()));
    attributeClassificationType.setStartDate(TestDataSet.DEFAULT_OVER_TIME_DATE);
    attributeClassificationType.setEndDate(TestDataSet.DEFAULT_OVER_TIME_DATE);

    GeoObjectType province = MetadataFactory.newGeoObjectType(TEST_GOT.getCode(), GeometryType.POLYGON, new LocalizedValue("Province"), new LocalizedValue(""), true, organizationCode, client.getAdapter());
    province.addAttribute(attributeClassificationType);

    String geoObjectTypeCode = province.getCode();

    String gtJSON = province.toJSON().toString();

    this.client.createGeoObjectType(gtJSON);

    String attributeTypeJSON = attributeClassificationType.toJSON().toString();
    attributeClassificationType = (AttributeClassificationType) this.client.createAttributeType(geoObjectTypeCode, attributeTypeJSON);

    net.geoprism.registry.graph.AttributeType mdAttributeConcreteDAOIF = checkAttribute(TEST_GOT.getCode(), attributeClassificationType.getCode());

    Assert.assertNotNull("A GeoObjectType did not define the attribute: " + attributeClassificationType.getCode(), mdAttributeConcreteDAOIF);
    Assert.assertTrue("A GeoObjectType did not define the attribute of the correct type: " + mdAttributeConcreteDAOIF.getType(), mdAttributeConcreteDAOIF instanceof net.geoprism.registry.graph.AttributeClassificationType);

    CodeReference rootTerm = attributeClassificationType.getRootTerm();

    Assert.assertNotNull("AttributeClassification root term not set correctly: " + attributeClassificationType.getCode(), rootTerm);
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

}
