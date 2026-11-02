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
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.MetadataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.session.Request;

import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.InstanceTestClassExtension;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectTypeInfo;
import net.geoprism.registry.test.TestRegistryClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@ExtendWith(InstanceTestClassExtension.class)
public class AttributeTypeServiceTest extends FastDatasetTest implements InstanceTestClassListener
{
  public static final TestGeoObjectTypeInfo TEST_GOT  = new TestGeoObjectTypeInfo("GOTTest_TEST1", FastTestDataset.ORG_CGOV);

  protected static String                   TYPE_CODE = null;

  @Autowired
  private TestRegistryClient                client;

  @BeforeEach
  public void setUp() throws Exception
  {
    super.setUp();

    setUpExtras();
  }

  @AfterEach
  public void tearDown() throws Exception
  {
    cleanUpExtras();

    super.tearDown();
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

    Assertions.assertNotNull(mdAttributeConcreteDAOIF, "A GeoObjectType did not define the attribute: " + testChar.getCode());
    Assertions.assertTrue(mdAttributeConcreteDAOIF instanceof net.geoprism.registry.graph.AttributeCharacterType, "A GeoObjectType did not define the attribute of the correct type: " + mdAttributeConcreteDAOIF.getType());

    testChar.setLabel(new LocalizedValue("testCharLocalName-Update"));
    testChar.setDescription(new LocalizedValue("testCharLocalDescrip-Update"));
    attributeTypeJSON = testChar.toJSON().toString();
    testChar = this.client.updateAttributeType(geoObjectTypeCode, attributeTypeJSON);

    Assertions.assertEquals("testCharLocalName-Update", testChar.getLabel().getValue());
    Assertions.assertEquals("testCharLocalDescrip-Update", testChar.getDescription().getValue());
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

    Assertions.assertNotNull(mdAttributeConcreteDAOIF, "A GeoObjectType did not define the attribute: " + testDate.getCode());
    Assertions.assertTrue(mdAttributeConcreteDAOIF instanceof net.geoprism.registry.graph.AttributeDateType, "A GeoObjectType did not define the attribute of the correct type: " + mdAttributeConcreteDAOIF.getType());
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

    Assertions.assertNotNull(mdAttributeConcreteDAOIF, "A GeoObjectType did not define the attribute: " + testInteger.getCode());
    Assertions.assertTrue(mdAttributeConcreteDAOIF instanceof net.geoprism.registry.graph.AttributeLongType, "A GeoObjectType did not define the attribute of the correct type: " + mdAttributeConcreteDAOIF.getType());
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

    Assertions.assertNotNull(mdAttributeConcreteDAOIF, "A GeoObjectType did not define the attribute: " + testBoolean.getCode());
    Assertions.assertTrue(mdAttributeConcreteDAOIF instanceof net.geoprism.registry.graph.AttributeBooleanType, "A GeoObjectType did not define the attribute of the correct type: " + mdAttributeConcreteDAOIF.getType());
  }

  @Test
  public void testCreateGeoObjectTypeClassification()
  {
    String organizationCode = FastTestDataset.ORG_CGOV.getCode();

    AttributeClassificationType attributeClassificationType = (AttributeClassificationType) AttributeType.factory("testClassification", new LocalizedValue("Test Classification Name"), new LocalizedValue("Test Classification Description"), AttributeClassificationType.TYPE, false, false, false);
    attributeClassificationType.setConceptSet(cSet.getCode());
    attributeClassificationType.setRootTerm(rootConcept.getCode());
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

    Assertions.assertNotNull(mdAttributeConcreteDAOIF, "A GeoObjectType did not define the attribute: " + attributeClassificationType.getCode());
    Assertions.assertTrue(mdAttributeConcreteDAOIF instanceof net.geoprism.registry.graph.AttributeClassificationType, "A GeoObjectType did not define the attribute of the correct type: " + mdAttributeConcreteDAOIF.getType());

    String rootTerm = attributeClassificationType.getRootTerm();

    Assertions.assertNotNull(rootTerm, "AttributeClassification root term not set correctly: " + attributeClassificationType.getCode());
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
