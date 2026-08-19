/**
 *
 */
package net.geoprism.registry.business;

import java.util.List;
import java.util.UUID;

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

import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.session.Request;

import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.classification.ClassificationTypeTest;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.graph.ConceptClass;
import net.geoprism.registry.graph.ConceptEdgeType;
import net.geoprism.registry.model.Classification;
import net.geoprism.registry.model.ClassificationType;
import net.geoprism.registry.model.ConceptObject;
import net.geoprism.registry.service.business.ClassificationBusinessServiceIF;
import net.geoprism.registry.service.business.ClassificationTypeBusinessServiceIF;
import net.geoprism.registry.service.business.ConceptClassBusinessServiceIF;
import net.geoprism.registry.service.business.ConceptEdgeTypeBusinessServiceIF;
import net.geoprism.registry.service.business.ConceptObjectBusinessServiceIF;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.view.ConceptClassDTO;
import net.geoprism.registry.view.ConceptEdgeTypeDTO;
import net.geoprism.registry.view.ObjectOverTimeDTO;
import net.geoprism.registry.view.ValueOverTimeEntryDTO;

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

  private static ConceptEdgeType              relationshipType;

  @Autowired
  private ClassificationTypeBusinessServiceIF cTypeService;

  @Autowired
  private ClassificationBusinessServiceIF     cService;

  @Autowired
  private ConceptClassBusinessServiceIF       cClassService;

  @Autowired
  private ConceptObjectBusinessServiceIF      cObjectService;

  @Autowired
  private ConceptEdgeTypeBusinessServiceIF    cEdgeService;

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

    relationshipType = this.cEdgeService.create(ConceptEdgeTypeDTO.build(FastTestDataset.ORG_CGOV.getCode(), "TEST_REL", new LocalizedValue("Test Rel"), new LocalizedValue("Test Rel"), type.getCode(), type.getCode()));

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
    if (relationshipType != null)
    {
      this.cEdgeService.delete(relationshipType);
    }

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
    object.setValue(DefaultAttribute.DATA_SOURCE.getName(), FastTestDataset.SOURCE.getDataSource(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE);
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
    object.setValue(DefaultAttribute.DATA_SOURCE.getName(), FastTestDataset.SOURCE.getDataSource(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE);
    object.setValue(attributeOverTime.getCode(), "Test Text 2", FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE);

    this.cObjectService.apply(object);

    try
    {
      ConceptObject result = this.cObjectService.get(type, attribute.getCode(), object.getValue(attribute.getCode())).orElse(null);

      Assert.assertEquals(object.getVertex().getOid(), result.getVertex().getOid());
      Assert.assertEquals(FastTestDataset.SOURCE.getDataSource().getOid(), (String) result.getValue(DefaultAttribute.DATA_SOURCE.getName()));
      Assert.assertEquals("Test Text 2", result.getValue(attributeOverTime.getCode(), FastTestDataset.DEFAULT_OVER_TIME_DATE));
      Assert.assertEquals("Test Text", result.getValue(attribute.getCode()));
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
      ConceptObject result = this.cObjectService.getByCode(type, object.getCode()).orElse(null);

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
    // object.setValue(attributeClassification.getCode(), root.getVertex());
    object.setValue(attributeOverTime.getCode(), text, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE);
    object.setValue(DefaultAttribute.DATA_SOURCE.getName(), FastTestDataset.SOURCE.getDataSource(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE);

    ObjectOverTimeDTO dto = this.cObjectService.toDTO(object);

    object = this.cObjectService.newInstance(type);

    this.cObjectService.populate(object, dto);

    this.cObjectService.apply(object);

    try
    {
      dto = this.cObjectService.toDTO(object);

      Assert.assertNotNull(dto);
      Assert.assertEquals(TEST_CODE, dto.getCode());

      Assert.assertEquals(FastTestDataset.SOURCE.getCode(), dto.getValue(DefaultAttribute.DATA_SOURCE.getName(), FastTestDataset.DEFAULT_OVER_TIME_DATE).get());
      Assert.assertEquals(text, dto.getValue(attribute.getCode()));

      List<ValueOverTimeEntryDTO<String>> valuesOverTime = dto.getValuesOverTime(attributeOverTime.getCode());

      Assert.assertEquals(1, valuesOverTime.size());

      ValueOverTimeEntryDTO<String> entry = valuesOverTime.get(0);

      Assert.assertEquals(FastTestDataset.DEFAULT_OVER_TIME_DATE, entry.getStartDate());
      Assert.assertEquals(FastTestDataset.DEFAULT_END_TIME_DATE, entry.getEndDate());
      Assert.assertEquals(text, entry.getValue());
    }
    finally
    {
      this.cObjectService.delete(object);
    }
  }

  @Test
  @Request
  public void testAddChildren()
  {
    ConceptObject parent = this.cObjectService.newInstance(type);
    parent.setValue(attribute.getCode(), "Test Parent");
    parent.setCode("TEST_PARENT");
    this.cObjectService.apply(parent);

    try
    {
      ConceptObject child = this.cObjectService.newInstance(type);
      child.setValue(attribute.getCode(), "Test Child");
      child.setCode("TEST_CHILD");
      this.cObjectService.apply(child);

      try
      {
        String uid = UUID.randomUUID().toString();

        EdgeObject edge = this.cObjectService.addChild(parent, relationshipType, child, uid, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, FastTestDataset.SOURCE.getDataSource()).get();

        Assert.assertEquals(uid, edge.getObjectValue(DefaultAttribute.UID.getName()));
        Assert.assertNotNull(edge.getObjectValue(DefaultAttribute.DATA_SOURCE.getName()));

        List<ConceptObject> results = this.cObjectService.getChildren(parent, relationshipType, FastTestDataset.DEFAULT_OVER_TIME_DATE);

        Assert.assertEquals(1, results.size());

        ConceptObject result = (ConceptObject) results.get(0);

        Assert.assertEquals(child.getCode(), result.getCode());
      }
      finally
      {
        this.cObjectService.delete(child);
      }
    }
    finally
    {
      this.cObjectService.delete(parent);
      ;
    }
  }

  @Test
  @Request
  public void testRemoveChildren()
  {
    ConceptObject parent = this.cObjectService.newInstance(type);
    parent.setValue(attribute.getCode(), "Test Parent");
    parent.setCode("TEST_PARENT");
    this.cObjectService.apply(parent);

    try
    {
      ConceptObject child = this.cObjectService.newInstance(type);
      child.setValue(attribute.getCode(), "Test Child");
      child.setCode("TEST_CHILD");
      this.cObjectService.apply(child);

      try
      {
        this.cObjectService.addChild(parent, relationshipType, child, UUID.randomUUID().toString(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, FastTestDataset.SOURCE.getDataSource());
        this.cObjectService.removeChild(parent, relationshipType, child, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE);

        Assert.assertEquals(0, this.cObjectService.getChildren(parent, relationshipType, FastTestDataset.DEFAULT_OVER_TIME_DATE).size());
      }
      finally
      {
        this.cObjectService.delete(child);
      }
    }
    finally
    {
      this.cObjectService.delete(parent);
    }
  }

  @Test
  @Request
  public void testDuplicateChildren()
  {
    ConceptObject parent = this.cObjectService.newInstance(type);
    parent.setValue(attribute.getCode(), "Test Parent");
    parent.setCode("TEST_PARENT");
    this.cObjectService.apply(parent);

    try
    {
      ConceptObject child = this.cObjectService.newInstance(type);
      child.setValue(attribute.getCode(), "Test Child");
      child.setCode("TEST_CHILD");
      this.cObjectService.apply(child);

      try
      {
        this.cObjectService.addChild(parent, relationshipType, child, UUID.randomUUID().toString(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, FastTestDataset.SOURCE.getDataSource());
        this.cObjectService.addChild(parent, relationshipType, child, UUID.randomUUID().toString(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, FastTestDataset.SOURCE.getDataSource());
        this.cObjectService.addChild(parent, relationshipType, child, UUID.randomUUID().toString(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, FastTestDataset.SOURCE.getDataSource());
        this.cObjectService.addChild(parent, relationshipType, child, UUID.randomUUID().toString(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, FastTestDataset.SOURCE.getDataSource());
        this.cObjectService.addChild(parent, relationshipType, child, UUID.randomUUID().toString(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, FastTestDataset.SOURCE.getDataSource());

        List<ConceptObject> results = this.cObjectService.getChildren(parent, relationshipType, FastTestDataset.DEFAULT_OVER_TIME_DATE);

        Assert.assertEquals(1, results.size());

        ConceptObject result = results.get(0);

        Assert.assertEquals(child.getCode(), result.getCode());
      }
      finally
      {
        this.cObjectService.delete(child);
      }
    }
    finally
    {
      this.cObjectService.delete(parent);
      ;
    }
  }

  @Test(expected = UnsupportedOperationException.class)
  @Request
  public void testAddCycle()
  {
    ConceptObject parent = this.cObjectService.newInstance(type);
    parent.setValue(attribute.getCode(), "Test Parent");
    parent.setCode("TEST_PARENT");
    this.cObjectService.apply(parent);

    try
    {
      ConceptObject child = this.cObjectService.newInstance(type);
      child.setValue(attribute.getCode(), "Test Child");
      child.setCode("TEST_CHILD");
      this.cObjectService.apply(child);

      try
      {
        this.cObjectService.addChild(parent, relationshipType, child, UUID.randomUUID().toString(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, FastTestDataset.SOURCE.getDataSource()).get();
        this.cObjectService.addChild(child, relationshipType, parent, UUID.randomUUID().toString(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, FastTestDataset.SOURCE.getDataSource()).get();
      }
      finally
      {
        this.cObjectService.delete(child);
      }
    }
    finally
    {
      this.cObjectService.delete(parent);
      ;
    }
  }

}
