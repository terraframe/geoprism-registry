/**
 *
 */
package net.geoprism.registry.service;

import java.util.List;
import java.util.UUID;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.session.Request;

import net.geoprism.registry.ConceptDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.graph.ConceptClass;
import net.geoprism.registry.model.ConceptObject;
import net.geoprism.registry.test.TestOrganizationInfo;
import net.geoprism.registry.test.USATestData;
import net.geoprism.registry.view.ObjectOverTimeDTO;
import net.geoprism.registry.view.ValueOverTimeEntryDTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class ConceptObjectServiceTest extends ConceptDatasetTest implements InstanceTestClassListener
{
  private static String        TEST_CODE = "TEST_OBJ";

  private static AttributeType attribute;

  private static AttributeType attributeOverTime;

  private static ConceptClass  secondClass;

  @Override
  protected TestOrganizationInfo getOrganization()
  {
    return USATestData.ORG_NPS;
  }

  @Override
  @Request
  public void beforeClassSetup() throws Exception
  {
    USATestData.ORG_NPS.apply();
    USATestData.AUTHORITY.apply();
    USATestData.SOURCE.apply();

    super.beforeClassSetup();

    attribute = this.cClassService.createAttributeType(cClass, new AttributeCharacterType("testCharacter", new LocalizedValue("Test Character"), new LocalizedValue("Test True"), false, false, false, false));
    attributeOverTime = this.cClassService.createAttributeType(cClass, new AttributeCharacterType("testCharacter2", new LocalizedValue("Test Character 2"), new LocalizedValue("Test True"), false, false, false, true));

    secondClass = this.cClassService.apply(this.mockConceptClass("SECOND_C_CLASS", "SECOND Concept", "SECOND Concept"));
  }

  @Override
  @Request
  public void afterClassSetup() throws Exception
  {
    this.cClassService.delete(secondClass);

    super.afterClassSetup();

    USATestData.SOURCE.delete();
    USATestData.AUTHORITY.delete();
    USATestData.ORG_NPS.delete();
  }

  @Test
  @Request
  public void testBasicCreate()
  {
    ConceptObject object = this.cObjectService.newInstance(cClass);
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
    ConceptObject object = this.cObjectService.newInstance(cClass);
    object.setValue(attribute.getCode(), "Test Text");
    object.setValue(attributeOverTime.getCode(), "Test Text 2", USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
    object.setValue(DefaultAttribute.DATA_SOURCE.getName(), USATestData.SOURCE.getDataSource(), USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
    object.setCode(TEST_CODE);
    this.cObjectService.apply(object);

    try
    {
      Assert.assertEquals("Test Text", object.getValue(attribute.getCode()));
      Assert.assertEquals("Test Text 2", object.getValue(attributeOverTime.getCode(), USATestData.DEFAULT_OVER_TIME_DATE));
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
    ConceptObject object = this.cObjectService.newInstance(cClass);
    object.setValue(attribute.getCode(), "Test Text");
    object.setCode(TEST_CODE);
    object.setValue(DefaultAttribute.DATA_SOURCE.getName(), USATestData.SOURCE.getDataSource(), USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
    object.setValue(attributeOverTime.getCode(), "Test Text 2", USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);

    this.cObjectService.apply(object);

    try
    {
      ConceptObject result = this.cObjectService.get(cClass, attribute.getCode(), object.getValue(attribute.getCode())).orElse(null);

      Assert.assertEquals(object.getVertex().getOid(), result.getVertex().getOid());
      Assert.assertEquals(USATestData.SOURCE.getDataSource().getOid(), (String) result.getValue(DefaultAttribute.DATA_SOURCE.getName()));
      Assert.assertEquals("Test Text 2", result.getValue(attributeOverTime.getCode(), USATestData.DEFAULT_OVER_TIME_DATE));
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
    ConceptObject object = this.cObjectService.newInstance(cClass);
    object.setValue(attribute.getCode(), "Test Text");
    object.setCode(TEST_CODE);
    this.cObjectService.apply(object);

    try
    {
      ConceptObject result = this.cObjectService.getByCode(cClass, object.getCode()).orElse(null);

      Assert.assertEquals(object.getVertex().getOid(), result.getVertex().getOid());
    }
    finally
    {
      this.cObjectService.delete(object);
    }
  }

  @Test
  @Request
  public void testConceptSetGetByCode()
  {
    ConceptObject object = this.cObjectService.newInstance(cClass);
    object.setValue(attribute.getCode(), "Test Text");
    object.setCode(TEST_CODE);
    this.cObjectService.apply(object);

    try
    {
      ConceptObject result = this.cObjectService.getByCode(cSet, object.getCode()).orElse(null);

      Assert.assertEquals(object.getVertex().getOid(), result.getVertex().getOid());
    }
    finally
    {
      this.cObjectService.delete(object);
    }
  }

  @Test(expected = UnsupportedOperationException.class)
  @Request
  public void testBadConceptSetGetByCode()
  {
    ConceptObject object = this.cObjectService.newInstance(secondClass);
    object.setCode(TEST_CODE);

    this.cObjectService.apply(object);

    try
    {
      this.cObjectService.getByCode(cSet, object.getCode()).orElse(null);
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

    ConceptObject object = this.cObjectService.newInstance(cClass);
    object.setValue(attribute.getCode(), text);
    object.setCode(TEST_CODE);
    // object.setValue(attributeClassification.getCode(), root.getVertex());
    object.setValue(attributeOverTime.getCode(), text, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
    object.setValue(DefaultAttribute.DATA_SOURCE.getName(), USATestData.SOURCE.getDataSource(), USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);

    ObjectOverTimeDTO dto = this.cObjectService.toDTO(object);

    object = this.cObjectService.newInstance(cClass);

    this.cObjectService.populate(object, dto);

    this.cObjectService.apply(object);

    try
    {
      dto = this.cObjectService.toDTO(object);

      Assert.assertNotNull(dto);
      Assert.assertEquals(TEST_CODE, dto.getCode());

      Assert.assertEquals(USATestData.SOURCE.getCode(), dto.getValue(DefaultAttribute.DATA_SOURCE.getName(), USATestData.DEFAULT_OVER_TIME_DATE).get());
      Assert.assertEquals(text, dto.getValue(attribute.getCode()));

      List<ValueOverTimeEntryDTO<String>> valuesOverTime = dto.getValuesOverTime(attributeOverTime.getCode());

      Assert.assertEquals(1, valuesOverTime.size());

      ValueOverTimeEntryDTO<String> entry = valuesOverTime.get(0);

      Assert.assertEquals(USATestData.DEFAULT_OVER_TIME_DATE, entry.getStartDate());
      Assert.assertEquals(USATestData.DEFAULT_END_TIME_DATE, entry.getEndDate());
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
    ConceptObject parent = this.cObjectService.newInstance(cClass);
    parent.setValue(attribute.getCode(), "Test Parent");
    parent.setCode("TEST_PARENT");
    this.cObjectService.apply(parent);

    try
    {
      ConceptObject child = this.cObjectService.newInstance(cClass);
      child.setValue(attribute.getCode(), "Test Child");
      child.setCode("TEST_CHILD");
      this.cObjectService.apply(child);

      try
      {
        String uid = UUID.randomUUID().toString();

        EdgeObject edge = this.cObjectService.addChild(parent, cEdgeType, child, uid, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE, USATestData.SOURCE.getDataSource()).get();

        Assert.assertEquals(uid, edge.getObjectValue(DefaultAttribute.UID.getName()));
        Assert.assertNotNull(edge.getObjectValue(DefaultAttribute.DATA_SOURCE.getName()));

        List<ConceptObject> results = this.cObjectService.getChildren(parent, cEdgeType, USATestData.DEFAULT_OVER_TIME_DATE);

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
    ConceptObject parent = this.cObjectService.newInstance(cClass);
    parent.setValue(attribute.getCode(), "Test Parent");
    parent.setCode("TEST_PARENT");
    this.cObjectService.apply(parent);

    try
    {
      ConceptObject child = this.cObjectService.newInstance(cClass);
      child.setValue(attribute.getCode(), "Test Child");
      child.setCode("TEST_CHILD");
      this.cObjectService.apply(child);

      try
      {
        this.cObjectService.addChild(parent, cEdgeType, child, UUID.randomUUID().toString(), USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE, USATestData.SOURCE.getDataSource());
        this.cObjectService.removeChild(parent, cEdgeType, child, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);

        Assert.assertEquals(0, this.cObjectService.getChildren(parent, cEdgeType, USATestData.DEFAULT_OVER_TIME_DATE).size());
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
    ConceptObject parent = this.cObjectService.newInstance(cClass);
    parent.setValue(attribute.getCode(), "Test Parent");
    parent.setCode("TEST_PARENT");
    this.cObjectService.apply(parent);

    try
    {
      ConceptObject child = this.cObjectService.newInstance(cClass);
      child.setValue(attribute.getCode(), "Test Child");
      child.setCode("TEST_CHILD");
      this.cObjectService.apply(child);

      try
      {
        this.cObjectService.addChild(parent, cEdgeType, child, UUID.randomUUID().toString(), USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE, USATestData.SOURCE.getDataSource());
        this.cObjectService.addChild(parent, cEdgeType, child, UUID.randomUUID().toString(), USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE, USATestData.SOURCE.getDataSource());
        this.cObjectService.addChild(parent, cEdgeType, child, UUID.randomUUID().toString(), USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE, USATestData.SOURCE.getDataSource());
        this.cObjectService.addChild(parent, cEdgeType, child, UUID.randomUUID().toString(), USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE, USATestData.SOURCE.getDataSource());
        this.cObjectService.addChild(parent, cEdgeType, child, UUID.randomUUID().toString(), USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE, USATestData.SOURCE.getDataSource());

        List<ConceptObject> results = this.cObjectService.getChildren(parent, cEdgeType, USATestData.DEFAULT_OVER_TIME_DATE);

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
    ConceptObject parent = this.cObjectService.newInstance(cClass);
    parent.setValue(attribute.getCode(), "Test Parent");
    parent.setCode("TEST_PARENT");
    this.cObjectService.apply(parent);

    try
    {
      ConceptObject child = this.cObjectService.newInstance(cClass);
      child.setValue(attribute.getCode(), "Test Child");
      child.setCode("TEST_CHILD");
      this.cObjectService.apply(child);

      try
      {
        this.cObjectService.addChild(parent, cEdgeType, child, UUID.randomUUID().toString(), USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE, USATestData.SOURCE.getDataSource()).get();
        this.cObjectService.addChild(child, cEdgeType, parent, UUID.randomUUID().toString(), USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE, USATestData.SOURCE.getDataSource()).get();
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

}
