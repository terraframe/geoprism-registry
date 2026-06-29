/**
 *
 */
package net.geoprism.registry.business;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;

import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.classification.ClassificationTypeTest;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.graph.ConceptClass;
import net.geoprism.registry.model.Classification;
import net.geoprism.registry.model.ClassificationType;
import net.geoprism.registry.model.ConceptObject;
import net.geoprism.registry.service.business.ClassificationBusinessServiceIF;
import net.geoprism.registry.service.business.ClassificationTypeBusinessServiceIF;
import net.geoprism.registry.service.business.ConceptClassBusinessServiceIF;
import net.geoprism.registry.service.business.ConceptObjectBusinessServiceIF;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.view.ConceptClassDTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc

@RunWith(SpringInstanceTestClassRunner.class)
public class ConceptObjectTest extends FastDatasetTest implements InstanceTestClassListener
{
  private static String                       TEST_CODE = "TEST_OBJ";

  private static ConceptClass                 type;

  private static AttributeType                attribute;

  private static AttributeType                attributeOverTime;

  private static AttributeClassificationType  attributeClassification;

  private static ClassificationType           classificationType;

  private static Classification               root;

  @Autowired
  private ClassificationTypeBusinessServiceIF cTypeService;

  @Autowired
  private ClassificationBusinessServiceIF     cService;

  @Autowired
  private ConceptClassBusinessServiceIF       cClassService;

  @Autowired
  private ConceptObjectBusinessServiceIF      cObjectService;

  @Override
  public void beforeClassSetup() throws Exception
  {
    super.beforeClassSetup();

    testData.setUpInstanceData();

    setUpClassInRequest();
  }

  @Request
  private void setUpClassInRequest()
  {
    classificationType = this.cTypeService.apply(ClassificationTypeTest.createMock());

    root = this.cService.newInstance(classificationType);
    root.setCode("ROOT_OBJ");

    this.cService.apply(root, null);

    String code = "TEST_PROG";
    String orgCode = FastTestDataset.ORG_CGOV.getCode();
    String label = "Test Prog";

    ConceptClassDTO object = new ConceptClassDTO();
    object.setCode(code);
    object.setOrganization(orgCode);
    object.setDisplayLabel(new LocalizedValue(label));

    type = this.cClassService.apply(object);

    attribute = this.cClassService.createAttributeType(type, new AttributeCharacterType("testCharacter", new LocalizedValue("Test Character"), new LocalizedValue("Test True"), false, false, false, false));
    attributeOverTime = this.cClassService.createAttributeType(type, new AttributeCharacterType("testCharacter2", new LocalizedValue("Test Character 2"), new LocalizedValue("Test True"), false, false, false, true));

    attributeClassification = new AttributeClassificationType("testClassification", new LocalizedValue("Test Classification"), new LocalizedValue("Test Classification"), false, false, false);
    attributeClassification.setClassificationType(classificationType.getCode());
    attributeClassification.setRootTerm(root.toTerm());
    attributeClassification.setChangeOverTime(false);

    attributeClassification = (AttributeClassificationType) this.cClassService.createAttributeType(type, attributeClassification);
  }

  @Override
  public void afterClassSetup() throws Exception
  {
    cleanUpClassInRequest();

    if (testData != null)
    {
      testData.tearDownInstanceData();
    }

    super.afterClassSetup();
  }

  @Request
  private void cleanUpClassInRequest()
  {
    if (type != null)
    {
      this.cClassService.delete(type);
    }

    if (root != null)
    {
      this.cService.delete(root);
    }

    if (classificationType != null)
    {
      this.cTypeService.delete(classificationType);
    }
  }

  @Test
  @Request
  public void testBasicCreate()
  {
    ConceptObject object = this.cObjectService.newInstance(type);
    object.setCode(TEST_CODE);

    this.cObjectService.apply(object);

    try
    {
      Assert.assertNotNull(object.getVertex().getRID());
    }
    finally
    {
      this.cObjectService.delete(object);
    }
  }

  @Test
  @Request
  public void testSetGetValue()
  {
    ConceptObject object = this.cObjectService.newInstance(type);
    object.setValue(attribute.getCode(), "Test Text");
    object.setValue(attributeOverTime.getCode(), "Test Text 2", FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE);
    object.setValue(attributeClassification.getCode(), root.getVertex());
    object.setValue(DefaultAttribute.DATA_SOURCE.getName(), FastTestDataset.SOURCE.getDataSource());
    object.setCode(TEST_CODE);
    this.cObjectService.apply(object);

    try
    {
      Assert.assertEquals("Test Text", object.getValue(attribute.getCode()));
      Assert.assertEquals("Test Text 2", object.getValue(attributeOverTime.getCode(), FastTestDataset.DEFAULT_OVER_TIME_DATE));
    }
    finally
    {
      this.cObjectService.delete(object);
    }

  }

  @Test
  @Request
  public void testGet()
  {
    ConceptObject object = this.cObjectService.newInstance(type);
    object.setValue(attribute.getCode(), "Test Text");
    object.setCode(TEST_CODE);
    object.setValue(DefaultAttribute.DATA_SOURCE.getName(), FastTestDataset.SOURCE.getDataSource());

    this.cObjectService.apply(object);

    try
    {
      ConceptObject result = this.cObjectService.get(type, attribute.getCode(), object.getValue(attribute.getCode()));

      Assert.assertEquals(object.getVertex().getOid(), result.getVertex().getOid());
      Assert.assertEquals(FastTestDataset.SOURCE.getDataSource().getOid(), (String) result.getValue(DefaultAttribute.DATA_SOURCE.getName()));
    }
    finally
    {
      this.cObjectService.delete(object);
    }
  }

  @Test
  @Request
  public void testGetByCode()
  {
    ConceptObject object = this.cObjectService.newInstance(type);
    object.setValue(attribute.getCode(), "Test Text");
    object.setCode(TEST_CODE);
    this.cObjectService.apply(object);

    try
    {
      ConceptObject result = this.cObjectService.getByCode(type, object.getCode());

      Assert.assertEquals(object.getVertex().getOid(), result.getVertex().getOid());
    }
    finally
    {
      this.cObjectService.delete(object);
    }
  }

  @Test
  @Request
  public void testToFromJson()
  {
    String text = "Test Text";

    ConceptObject object = this.cObjectService.newInstance(type);
    object.setValue(attribute.getCode(), text);
    object.setCode(TEST_CODE);
    object.setValue(attributeClassification.getCode(), root.getVertex());
    object.setValue(attributeOverTime.getCode(), text, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE);
    object.setValue(DefaultAttribute.DATA_SOURCE.getName(), FastTestDataset.SOURCE.getDataSource());

    JsonObject json = this.cObjectService.toJSON(object);

    object = this.cObjectService.newInstance(type);

    this.cObjectService.populate(object, json);

    this.cObjectService.apply(object);

    try
    {
      json = this.cObjectService.toJSON(object);

      Assert.assertNotNull(json);
      Assert.assertEquals(TEST_CODE, json.get("code").getAsString());

      JsonObject data = json.get("data").getAsJsonObject();

      Assert.assertEquals(FastTestDataset.SOURCE.getCode(), data.get(DefaultAttribute.DATA_SOURCE.getName()).getAsString());
      Assert.assertEquals(text, data.get(attribute.getCode()).getAsString());
      Assert.assertEquals(root.getCode(), data.get(attributeClassification.getCode()).getAsString());
    }
    finally
    {
      this.cObjectService.delete(object);
    }
  }
}
