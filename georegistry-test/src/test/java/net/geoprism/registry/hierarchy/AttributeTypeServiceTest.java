/**
 *
 */
package net.geoprism.registry.hierarchy;

import java.util.Optional;

import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeDataSourceType;
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

import com.runwaysdk.business.SmartExceptionDTO;
import com.runwaysdk.session.Request;

import net.geoprism.registry.DataNotFoundException;
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

  @Test
  public void testCreateDataSourceAttribute_AndUpdate()
  {
    String geoObjectTypeCode = this.createProvinceType();

    AttributeType dto = AttributeType.factory("testSource", new LocalizedValue("testSourceLocalName"), new LocalizedValue("testSourceLocalDescrip"), AttributeDataSourceType.TYPE, false, false, false);

    AttributeType result = this.client.createAttributeType(geoObjectTypeCode, dto.toJSON().toString());

    // Validate the returned DTO
    Assertions.assertTrue(result instanceof AttributeDataSourceType);
    Assertions.assertEquals(dto.getCode(), result.getCode());
    Assertions.assertEquals(AttributeDataSourceType.TYPE, result.getType());
    Assertions.assertEquals("testSourceLocalName", result.getLabel().getValue());
    Assertions.assertEquals("testSourceLocalDescrip", result.getDescription().getValue());

    // Validate the persisted attribute
    net.geoprism.registry.graph.AttributeType attributeType = checkAttribute(TEST_GOT.getCode(), dto.getCode());

    Assertions.assertTrue(attributeType instanceof net.geoprism.registry.graph.AttributeDataSourceType, "A GeoObjectType did not define the attribute of the correct type: " + attributeType.getType());

    // Update the attribute
    result.setLabel(new LocalizedValue("testSourceLocalName-Update"));
    result.setDescription(new LocalizedValue("testSourceLocalDescrip-Update"));

    AttributeType updated = this.client.updateAttributeType(geoObjectTypeCode, result.toJSON().toString());

    Assertions.assertTrue(updated instanceof AttributeDataSourceType);
    Assertions.assertEquals(dto.getCode(), updated.getCode());
    Assertions.assertEquals("testSourceLocalName-Update", updated.getLabel().getValue());
    Assertions.assertEquals("testSourceLocalDescrip-Update", updated.getDescription().getValue());

    attributeType = checkAttribute(TEST_GOT.getCode(), dto.getCode());

    Assertions.assertTrue(attributeType instanceof net.geoprism.registry.graph.AttributeDataSourceType);
  }

  @Test
  public void testCreateClassificationAttribute()
  {
    String geoObjectTypeCode = this.createProvinceType();

    AttributeClassificationType dto = this.newClassificationAttribute("testClassification", ROOT_CONCEPT);

    AttributeClassificationType result = (AttributeClassificationType) this.client.createAttributeType(geoObjectTypeCode, dto.toJSON().toString());

    // Validate the returned DTO
    Assertions.assertEquals(dto.getCode(), result.getCode());
    Assertions.assertEquals(AttributeClassificationType.TYPE, result.getType());
    Assertions.assertEquals("testClassificationLocalName", result.getLabel().getValue());
    Assertions.assertEquals("testClassificationLocalDescrip", result.getDescription().getValue());
    Assertions.assertEquals(cSet.getCode(), result.getConceptSet());
    Assertions.assertEquals(ROOT_CONCEPT, result.getRootTerm());
    Assertions.assertEquals(TestDataSet.DEFAULT_OVER_TIME_DATE, result.getStartDate());
    Assertions.assertEquals(TestDataSet.DEFAULT_END_TIME_DATE, result.getEndDate());

    // Validate the persisted attribute
    net.geoprism.registry.graph.AttributeType attributeType = checkAttribute(TEST_GOT.getCode(), dto.getCode());

    Assertions.assertTrue(attributeType instanceof net.geoprism.registry.graph.AttributeClassificationType, "A GeoObjectType did not define the attribute of the correct type: " + attributeType.getType());

    net.geoprism.registry.graph.AttributeClassificationType classification = (net.geoprism.registry.graph.AttributeClassificationType) attributeType;

    Assertions.assertEquals(cSet.getCode(), classification.getConceptSet());
    Assertions.assertEquals(ROOT_CONCEPT, classification.getRootTerm());
    Assertions.assertEquals(TestDataSet.DEFAULT_OVER_TIME_DATE, classification.getStartDate());
    Assertions.assertEquals(TestDataSet.DEFAULT_END_TIME_DATE, classification.getEndDate());
  }

  @Test
  public void testCreateClassificationAttribute_AndUpdate()
  {
    String geoObjectTypeCode = this.createProvinceType();

    AttributeClassificationType dto = this.newClassificationAttribute("testClassification", ROOT_CONCEPT);

    AttributeClassificationType result = (AttributeClassificationType) this.client.createAttributeType(geoObjectTypeCode, dto.toJSON().toString());

    // The concept values can only be set on creation, so they should be
    // ignored on update
    result.setLabel(new LocalizedValue("testClassificationLocalName-Update"));
    result.setDescription(new LocalizedValue("testClassificationLocalDescrip-Update"));
    result.setRootTerm(PARENT_CONCEPT);

    AttributeClassificationType updated = (AttributeClassificationType) this.client.updateAttributeType(geoObjectTypeCode, result.toJSON().toString());

    Assertions.assertEquals("testClassificationLocalName-Update", updated.getLabel().getValue());
    Assertions.assertEquals("testClassificationLocalDescrip-Update", updated.getDescription().getValue());
    Assertions.assertEquals(cSet.getCode(), updated.getConceptSet());
    Assertions.assertEquals(ROOT_CONCEPT, updated.getRootTerm());

    net.geoprism.registry.graph.AttributeClassificationType classification = (net.geoprism.registry.graph.AttributeClassificationType) checkAttribute(TEST_GOT.getCode(), dto.getCode());

    Assertions.assertEquals(ROOT_CONCEPT, classification.getRootTerm());
  }

  @Test
  public void testCreateMultipleClassificationAttributes()
  {
    String geoObjectTypeCode = this.createProvinceType();

    AttributeClassificationType first = this.newClassificationAttribute("testClassification1", ROOT_CONCEPT);
    AttributeClassificationType second = this.newClassificationAttribute("testClassification2", PARENT_CONCEPT);

    this.client.createAttributeType(geoObjectTypeCode, first.toJSON().toString());
    this.client.createAttributeType(geoObjectTypeCode, second.toJSON().toString());

    net.geoprism.registry.graph.AttributeClassificationType firstAttribute = (net.geoprism.registry.graph.AttributeClassificationType) checkAttribute(TEST_GOT.getCode(), first.getCode());
    net.geoprism.registry.graph.AttributeClassificationType secondAttribute = (net.geoprism.registry.graph.AttributeClassificationType) checkAttribute(TEST_GOT.getCode(), second.getCode());

    Assertions.assertEquals(cSet.getCode(), firstAttribute.getConceptSet());
    Assertions.assertEquals(ROOT_CONCEPT, firstAttribute.getRootTerm());

    Assertions.assertEquals(cSet.getCode(), secondAttribute.getConceptSet());
    Assertions.assertEquals(PARENT_CONCEPT, secondAttribute.getRootTerm());
  }

  @Test
  public void testCreateClassificationAttribute_UnknownConceptSet()
  {
    String geoObjectTypeCode = this.createProvinceType();

    AttributeClassificationType dto = this.newClassificationAttribute("testClassification", ROOT_CONCEPT);
    dto.setConceptSet("DOES_NOT_EXIST");

    String attributeTypeJSON = dto.toJSON().toString();

    Assertions.assertThrows(SmartExceptionDTO.class, () -> {
      this.client.createAttributeType(geoObjectTypeCode, attributeTypeJSON);
    });

    Assertions.assertTrue(this.findAttribute(TEST_GOT.getCode(), dto.getCode()).isEmpty(), "The attribute should not have been created");
  }

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
  private Optional<net.geoprism.registry.graph.AttributeType> findAttribute(String geoObjectTypeCode, String attributeName)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(geoObjectTypeCode);
    return type.getAttribute(attributeName);
  }

  private String createProvinceType()
  {
    String organizationCode = FastTestDataset.ORG_CGOV.getCode();

    GeoObjectType province = MetadataFactory.newGeoObjectType(TEST_GOT.getCode(), GeometryType.POLYGON, new LocalizedValue("Province"), new LocalizedValue(""), true, organizationCode, client.getAdapter());

    this.client.createGeoObjectType(province.toJSON().toString());

    return province.getCode();
  }

  private AttributeClassificationType newClassificationAttribute(String code, String rootTerm)
  {
    AttributeClassificationType dto = (AttributeClassificationType) AttributeType.factory(code, new LocalizedValue("testClassificationLocalName"), new LocalizedValue("testClassificationLocalDescrip"), AttributeClassificationType.TYPE, false, false, false);
    dto.setConceptSet(cSet.getCode());
    dto.setRootTerm(rootTerm);
    dto.setStartDate(TestDataSet.DEFAULT_OVER_TIME_DATE);
    dto.setEndDate(TestDataSet.DEFAULT_END_TIME_DATE);

    return dto;
  }

}
