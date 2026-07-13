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
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
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
import net.geoprism.registry.graph.BusinessEdgeType;
import net.geoprism.registry.graph.BusinessType;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.Classification;
import net.geoprism.registry.model.ClassificationType;
import net.geoprism.registry.model.EdgeDirection;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.graph.VertexComponent;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.service.business.BusinessEdgeTypeBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessObjectBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessTypeBusinessServiceIF;
import net.geoprism.registry.service.business.ClassificationBusinessServiceIF;
import net.geoprism.registry.service.business.ClassificationTypeBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.view.BusinessEdgeTypeView;
import net.geoprism.registry.view.BusinessGeoEdgeTypeView;
import net.geoprism.registry.view.BusinessTypeDTO;
import net.geoprism.registry.view.ObjectOverTimeDTO;
import net.geoprism.registry.view.ValueOverTimeEntryDTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc

@RunWith(SpringInstanceTestClassRunner.class)
public class BusinessObjectTest extends FastDatasetTest implements InstanceTestClassListener
{
  private static String                       TEST_CODE = "TEST_OBJ";

  private static BusinessType                 type;

  private static AttributeType                attribute;

  private static AttributeType                attributeOverTime;

  private static AttributeClassificationType  attributeClassification;

  private static ClassificationType           classificationType;

  private static Classification               root;

  private static BusinessEdgeType             relationshipType;

  private static BusinessEdgeType             bGeoEdgeType;

  @Autowired
  private ClassificationTypeBusinessServiceIF cTypeService;

  @Autowired
  private ClassificationBusinessServiceIF     cService;

  @Autowired
  private BusinessTypeBusinessServiceIF       bTypeService;

  @Autowired
  private BusinessEdgeTypeBusinessServiceIF   bEdgeService;

  @Autowired
  private BusinessObjectBusinessServiceIF     bObjectService;

  @Autowired
  private GeoObjectBusinessServiceIF          objectService;

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

    BusinessTypeDTO object = new BusinessTypeDTO();
    object.setCode(code);
    object.setOrganization(orgCode);
    object.setDisplayLabel(new LocalizedValue(label));

    type = this.bTypeService.apply(object);

    attribute = this.bTypeService.createAttributeType(type, new AttributeCharacterType("testCharacter", new LocalizedValue("Test Character"), new LocalizedValue("Test True"), false, false, false, false));
    attributeOverTime = this.bTypeService.createAttributeType(type, new AttributeLocalType("testCharacter2", new LocalizedValue("Test Character 2"), new LocalizedValue("Test True"), false, false, false, true));

    attributeClassification = new AttributeClassificationType("testClassification", new LocalizedValue("Test Classification"), new LocalizedValue("Test Classification"), false, false, false);
    attributeClassification.setClassificationType(classificationType.getCode());
    attributeClassification.setRootTerm(root.toTerm());
    attributeClassification.setChangeOverTime(false);

    attributeClassification = (AttributeClassificationType) this.bTypeService.createAttributeType(type, attributeClassification);

    relationshipType = this.bEdgeService.create(BusinessEdgeTypeView.build(FastTestDataset.ORG_CGOV.getCode(), "TEST_REL", new LocalizedValue("Test Rel"), new LocalizedValue("Test Rel"), type.getCode(), type.getCode()));

    bGeoEdgeType = this.bEdgeService.create(BusinessGeoEdgeTypeView.build(FastTestDataset.ORG_CGOV.getCode(), "GEO_EDGE", new LocalizedValue("Geo Edge"), new LocalizedValue("Geo Edge"), type.getCode(), EdgeDirection.PARENT));

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
      this.bEdgeService.delete(relationshipType);
    }

    if (bGeoEdgeType != null)
    {
      this.bEdgeService.delete(this.bEdgeService.getByCodeOrThrow(bGeoEdgeType.getCode()));
    }

    if (type != null)
    {
      this.bTypeService.delete(type);
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
    BusinessObject object = this.bObjectService.newInstance(type);
    object.setCode(TEST_CODE);

    this.bObjectService.apply(object);

    try
    {
      Assert.assertNotNull(object.getVertex().getRID());
    }
    finally
    {
      this.bObjectService.delete(object);
    }
  }

  @Test
  @Request
  public void testSetGetValue()
  {
    BusinessObject object = this.bObjectService.newInstance(type);
    object.setValue(attribute.getCode(), "Test Text");
    object.setValue(attributeOverTime.getCode(), new LocalizedValue("Test Text 2"), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE);
    object.setValue(attributeClassification.getCode(), root.getVertex());
    object.setValue(DefaultAttribute.DATA_SOURCE.getName(), FastTestDataset.SOURCE.getDataSource(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE);
    object.setCode(TEST_CODE);

    this.bObjectService.apply(object);

    try
    {
      Assert.assertEquals("Test Text", object.getValue(attribute.getCode()));
      Assert.assertEquals("Test Text 2", ( (LocalizedValue) object.getValue(attributeOverTime.getCode(), FastTestDataset.DEFAULT_OVER_TIME_DATE) ).getLocalizedValue());
    }
    finally
    {
      this.bObjectService.delete(object);
    }

  }

  @Test
  @Request
  public void testGet()
  {
    BusinessObject object = this.bObjectService.newInstance(type);
    object.setValue(attribute.getCode(), "Test Text");
    object.setValue(attributeOverTime.getCode(), new LocalizedValue("Test Text 2"), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE);
    object.setCode(TEST_CODE);
    object.setValue(DefaultAttribute.DATA_SOURCE.getName(), FastTestDataset.SOURCE.getDataSource(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE);

    this.bObjectService.apply(object);

    try
    {
      BusinessObject result = this.bObjectService.get(type, attribute.getCode(), object.getValue(attribute.getCode()));

      Assert.assertEquals(object.getVertex().getOid(), result.getVertex().getOid());
      Assert.assertEquals(FastTestDataset.SOURCE.getDataSource().getOid(), (String) result.getValue(DefaultAttribute.DATA_SOURCE.getName()));

      Assert.assertEquals("Test Text 2", ( (LocalizedValue) result.getValue(attributeOverTime.getCode(), FastTestDataset.DEFAULT_OVER_TIME_DATE) ).getLocalizedValue());
      Assert.assertEquals("Test Text", result.getValue(attribute.getCode()));
    }
    finally
    {
      this.bObjectService.delete(object);
    }
  }

  @Test
  @Request
  public void testGetByCode()
  {
    BusinessObject object = this.bObjectService.newInstance(type);
    object.setValue(attribute.getCode(), "Test Text");
    object.setCode(TEST_CODE);
    this.bObjectService.apply(object);

    try
    {
      BusinessObject result = this.bObjectService.getByCode(type, object.getCode());

      Assert.assertEquals(object.getVertex().getOid(), result.getVertex().getOid());
    }
    finally
    {
      this.bObjectService.delete(object);
    }
  }

  @Test
  @Request
  public void testAddGetGeoObjects()
  {
    ServerGeoObjectIF serverObject = FastTestDataset.CENTRAL_HOSPITAL.getServerObject();

    BusinessObject object = this.bObjectService.newInstance(type);
    object.setValue(attribute.getCode(), "Test Text");
    object.setCode(TEST_CODE);
    object.setValue(DefaultAttribute.DATA_SOURCE.getName(), FastTestDataset.SOURCE.getDataSource(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE);

    this.bObjectService.apply(object);

    try
    {
      this.bObjectService.addParent(object, bGeoEdgeType, serverObject, UUID.randomUUID().toString(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, FastTestDataset.SOURCE.getDataSource(), false);

      List<VertexComponent> results = this.bObjectService.getParents(object, bGeoEdgeType, FastTestDataset.DEFAULT_OVER_TIME_DATE);

      Assert.assertEquals(1, results.size());

      VertexComponent result = (VertexComponent) results.get(0);

      Assert.assertEquals(serverObject.getCode(), result.getCode());
    }
    finally
    {
      this.bObjectService.delete(object);
    }
  }

  @Test
  @Request
  public void testToFromJson()
  {
    String text = "Test Text";

    BusinessObject object = this.bObjectService.newInstance(type);
    object.setValue(attribute.getCode(), text);
    object.setCode(TEST_CODE);
    // object.setValue(attributeClassification.getCode(), root.getVertex());
    object.setValue(attributeOverTime.getCode(), new LocalizedValue("Test Text"), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE);
    object.setValue(DefaultAttribute.DATA_SOURCE.getName(), FastTestDataset.SOURCE.getDataSource(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE);

    ObjectOverTimeDTO dto = this.bObjectService.toDTO(object);

    object = this.bObjectService.newInstance(type);

    this.bObjectService.populate(object, dto);

    this.bObjectService.apply(object);

    try
    {
      dto = this.bObjectService.toDTO(object);

      dto = ObjectOverTimeDTO.parseJson(ObjectOverTimeDTO.toJson(dto));

      Assert.assertNotNull(dto);
      Assert.assertEquals(TEST_CODE, dto.getCode());
      Assert.assertEquals(FastTestDataset.SOURCE.getCode(), dto.getValue(DefaultAttribute.DATA_SOURCE.getName(), FastTestDataset.DEFAULT_OVER_TIME_DATE).orElseThrow());
      Assert.assertEquals(text, dto.getValue(attribute.getCode()));

      List<ValueOverTimeEntryDTO<LocalizedValue>> valuesOverTime = dto.getValuesOverTime(attributeOverTime.getCode());

      Assert.assertEquals(1, valuesOverTime.size());

      ValueOverTimeEntryDTO<LocalizedValue> entry = valuesOverTime.get(0);

      Assert.assertEquals(FastTestDataset.DEFAULT_OVER_TIME_DATE, entry.getStartDate());
      Assert.assertEquals(FastTestDataset.DEFAULT_END_TIME_DATE, entry.getEndDate());
      Assert.assertEquals(text, entry.getValue().getLocalizedValue());
    }
    finally
    {
      this.bObjectService.delete(object);
    }
  }

  @Test
  @Request
  public void testRemoveGeoObjects()
  {
    ServerGeoObjectIF serverObject = FastTestDataset.CENTRAL_HOSPITAL.getServerObject();

    BusinessObject object = this.bObjectService.newInstance(type);
    object.setValue(attribute.getCode(), "Test Text");
    object.setCode(TEST_CODE);
    this.bObjectService.apply(object);

    try
    {
      this.bObjectService.addParent(object, bGeoEdgeType, serverObject, UUID.randomUUID().toString(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, FastTestDataset.SOURCE.getDataSource(), false);
      this.bObjectService.removeParent(object, bGeoEdgeType, serverObject, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, false);

      Assert.assertEquals(0, this.bObjectService.getParents(object, bGeoEdgeType, FastTestDataset.DEFAULT_OVER_TIME_DATE).size());
    }
    finally
    {
      this.bObjectService.delete(object);
    }
  }

  @Test
  @Request
  public void testDuplicateGeoObjects()
  {
    ServerGeoObjectIF serverObject = FastTestDataset.CENTRAL_HOSPITAL.getServerObject();

    BusinessObject object = this.bObjectService.newInstance(type);
    object.setValue(attribute.getCode(), "Test Text");
    object.setCode(TEST_CODE);
    this.bObjectService.apply(object);

    try
    {
      this.bObjectService.addParent(object, bGeoEdgeType, serverObject, UUID.randomUUID().toString(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, FastTestDataset.SOURCE.getDataSource(), false);
      this.bObjectService.addParent(object, bGeoEdgeType, serverObject, UUID.randomUUID().toString(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, FastTestDataset.SOURCE.getDataSource(), false);
      this.bObjectService.addParent(object, bGeoEdgeType, serverObject, UUID.randomUUID().toString(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, FastTestDataset.SOURCE.getDataSource(), false);
      this.bObjectService.addParent(object, bGeoEdgeType, serverObject, UUID.randomUUID().toString(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, FastTestDataset.SOURCE.getDataSource(), false);

      List<VertexComponent> results = this.bObjectService.getParents(object, bGeoEdgeType, FastTestDataset.DEFAULT_OVER_TIME_DATE);

      Assert.assertEquals(1, results.size());

      VertexComponent result = results.get(0);

      Assert.assertEquals(serverObject.getCode(), result.getCode());
    }
    finally
    {
      this.bObjectService.delete(object);
    }
  }

  @Test
  @Request
  public void testGetBusinessObjects()
  {
    ServerGeoObjectIF serverObject = FastTestDataset.CENTRAL_HOSPITAL.getServerObject();

    BusinessObject object = this.bObjectService.newInstance(type);
    object.setValue(attribute.getCode(), "Test Text");
    object.setCode(TEST_CODE);
    this.bObjectService.apply(object);

    try
    {
      this.bObjectService.addParent(object, bGeoEdgeType, serverObject, UUID.randomUUID().toString(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, FastTestDataset.SOURCE.getDataSource(), false);

      List<BusinessObject> results = this.objectService.getBusinessObjects((VertexServerGeoObject) serverObject, bGeoEdgeType, EdgeDirection.CHILD);

      Assert.assertEquals(1, results.size());

      BusinessObject result = results.get(0);

      Assert.assertEquals(object.getCode(), result.getCode());
    }
    finally
    {
      this.bObjectService.delete(object);
    }
  }

  @Test
  @Request
  public void testGetBusinessObjects_NoType()
  {
    ServerGeoObjectIF serverObject = FastTestDataset.CENTRAL_HOSPITAL.getServerObject();

    BusinessObject object = this.bObjectService.newInstance(type);
    object.setValue(attribute.getCode(), "Test Text");
    object.setCode(TEST_CODE);
    this.bObjectService.apply(object);

    try
    {
      this.bObjectService.addParent(object, bGeoEdgeType, serverObject, UUID.randomUUID().toString(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, FastTestDataset.SOURCE.getDataSource(), false);

      List<BusinessObject> results = this.objectService.getBusinessObjects((VertexServerGeoObject) serverObject, bGeoEdgeType, EdgeDirection.CHILD);

      Assert.assertEquals(1, results.size());

      BusinessObject result = results.get(0);

      Assert.assertEquals(object.getCode(), result.getCode());
    }
    finally
    {
      this.bObjectService.delete(object);
    }
  }

  @Test
  @Request
  public void testAddParent()
  {
    BusinessObject parent = this.bObjectService.newInstance(type);
    parent.setValue(attribute.getCode(), "Test Parnet");
    parent.setCode("TEST_PARENT");
    this.bObjectService.apply(parent);

    try
    {
      BusinessObject child = this.bObjectService.newInstance(type);
      child.setValue(attribute.getCode(), "Test Child");
      child.setCode("TEST_CHILD");
      this.bObjectService.apply(child);

      try
      {
        String uid = UUID.randomUUID().toString();

        EdgeObject edge = this.bObjectService.addParent(child, relationshipType, parent, uid, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, FastTestDataset.SOURCE.getDataSource()).get();

        Assert.assertEquals(uid, edge.getObjectValue(DefaultAttribute.UID.getName()));
        Assert.assertNotNull(edge.getObjectValue(DefaultAttribute.DATA_SOURCE.getName()));

        List<VertexComponent> results = this.bObjectService.getParents(child, relationshipType, FastTestDataset.DEFAULT_OVER_TIME_DATE);

        Assert.assertEquals(1, results.size());

        VertexComponent result = results.get(0);

        Assert.assertEquals(parent.getCode(), result.getCode());
      }
      finally
      {
        this.bObjectService.delete(child);
      }
    }
    finally
    {
      this.bObjectService.delete(parent);
      ;
    }
  }

  @Test
  @Request
  public void testRemoveParent()
  {
    BusinessObject parent = this.bObjectService.newInstance(type);
    parent.setValue(attribute.getCode(), "Test Parnet");
    parent.setCode("TEST_PARENT");
    this.bObjectService.apply(parent);

    try
    {
      BusinessObject child = this.bObjectService.newInstance(type);
      child.setValue(attribute.getCode(), "Test Child");
      child.setCode("TEST_CHILD");
      this.bObjectService.apply(child);

      try
      {
        this.bObjectService.addParent(child, relationshipType, parent, UUID.randomUUID().toString(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, FastTestDataset.SOURCE.getDataSource());
        this.bObjectService.removeParent(child, relationshipType, parent, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE);

        Assert.assertEquals(0, this.bObjectService.getParents(child, relationshipType, FastTestDataset.DEFAULT_OVER_TIME_DATE).size());
      }
      finally
      {
        this.bObjectService.delete(child);
      }
    }
    finally
    {
      this.bObjectService.delete(parent);
      ;
    }
  }

  @Test
  @Request
  public void testDuplicateParent()
  {
    BusinessObject parent = this.bObjectService.newInstance(type);
    parent.setValue(attribute.getCode(), "Test Parnet");
    parent.setCode("TEST_PARENT");
    this.bObjectService.apply(parent);

    try
    {
      BusinessObject child = this.bObjectService.newInstance(type);
      child.setValue(attribute.getCode(), "Test Child");
      child.setCode("TEST_CHILD");
      this.bObjectService.apply(child);

      try
      {
        this.bObjectService.addParent(child, relationshipType, parent, UUID.randomUUID().toString(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, FastTestDataset.SOURCE.getDataSource());
        this.bObjectService.addParent(child, relationshipType, parent, UUID.randomUUID().toString(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, FastTestDataset.SOURCE.getDataSource());
        this.bObjectService.addParent(child, relationshipType, parent, UUID.randomUUID().toString(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, FastTestDataset.SOURCE.getDataSource());
        this.bObjectService.addParent(child, relationshipType, parent, UUID.randomUUID().toString(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, FastTestDataset.SOURCE.getDataSource());
        this.bObjectService.addParent(child, relationshipType, parent, UUID.randomUUID().toString(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, FastTestDataset.SOURCE.getDataSource());

        List<VertexComponent> results = this.bObjectService.getParents(child, relationshipType, FastTestDataset.DEFAULT_OVER_TIME_DATE);

        Assert.assertEquals(1, results.size());

        VertexComponent result = results.get(0);

        Assert.assertEquals(parent.getCode(), result.getCode());
      }
      finally
      {
        this.bObjectService.delete(child);
      }
    }
    finally
    {
      this.bObjectService.delete(parent);
    }
  }

  @Test
  @Request
  public void testAddChildren()
  {
    BusinessObject parent = this.bObjectService.newInstance(type);
    parent.setValue(attribute.getCode(), "Test Parnet");
    parent.setCode("TEST_PARENT");
    this.bObjectService.apply(parent);

    try
    {
      BusinessObject child = this.bObjectService.newInstance(type);
      child.setValue(attribute.getCode(), "Test Child");
      child.setCode("TEST_CHILD");
      this.bObjectService.apply(child);

      try
      {
        String uid = UUID.randomUUID().toString();

        EdgeObject edge = this.bObjectService.addChild(parent, relationshipType, child, uid, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, FastTestDataset.SOURCE.getDataSource()).get();

        Assert.assertEquals(uid, edge.getObjectValue(DefaultAttribute.UID.getName()));
        Assert.assertNotNull(edge.getObjectValue(DefaultAttribute.DATA_SOURCE.getName()));

        List<VertexComponent> results = this.bObjectService.getChildren(parent, relationshipType, FastTestDataset.DEFAULT_OVER_TIME_DATE);

        Assert.assertEquals(1, results.size());

        BusinessObject result = (BusinessObject) results.get(0);

        Assert.assertEquals(child.getCode(), result.getCode());
      }
      finally
      {
        this.bObjectService.delete(child);
      }
    }
    finally
    {
      this.bObjectService.delete(parent);
      ;
    }
  }

  @Test
  @Request
  public void testRemoveChildren()
  {
    BusinessObject parent = this.bObjectService.newInstance(type);
    parent.setValue(attribute.getCode(), "Test Parnet");
    parent.setCode("TEST_PARENT");
    this.bObjectService.apply(parent);

    try
    {
      BusinessObject child = this.bObjectService.newInstance(type);
      child.setValue(attribute.getCode(), "Test Child");
      child.setCode("TEST_CHILD");
      this.bObjectService.apply(child);

      try
      {
        this.bObjectService.addChild(parent, relationshipType, child, UUID.randomUUID().toString(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, FastTestDataset.SOURCE.getDataSource());
        this.bObjectService.removeChild(parent, relationshipType, child, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE);

        Assert.assertEquals(0, this.bObjectService.getChildren(parent, relationshipType, FastTestDataset.DEFAULT_OVER_TIME_DATE).size());
      }
      finally
      {
        this.bObjectService.delete(child);
      }
    }
    finally
    {
      this.bObjectService.delete(parent);
      ;
    }
  }

  @Test
  @Request
  public void testDuplicateChildren()
  {
    BusinessObject parent = this.bObjectService.newInstance(type);
    parent.setValue(attribute.getCode(), "Test Parnet");
    parent.setCode("TEST_PARENT");
    this.bObjectService.apply(parent);

    try
    {
      BusinessObject child = this.bObjectService.newInstance(type);
      child.setValue(attribute.getCode(), "Test Child");
      child.setCode("TEST_CHILD");
      this.bObjectService.apply(child);

      try
      {
        this.bObjectService.addChild(parent, relationshipType, child, UUID.randomUUID().toString(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, FastTestDataset.SOURCE.getDataSource());
        this.bObjectService.addChild(parent, relationshipType, child, UUID.randomUUID().toString(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, FastTestDataset.SOURCE.getDataSource());
        this.bObjectService.addChild(parent, relationshipType, child, UUID.randomUUID().toString(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, FastTestDataset.SOURCE.getDataSource());
        this.bObjectService.addChild(parent, relationshipType, child, UUID.randomUUID().toString(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, FastTestDataset.SOURCE.getDataSource());
        this.bObjectService.addChild(parent, relationshipType, child, UUID.randomUUID().toString(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE, FastTestDataset.SOURCE.getDataSource());

        List<VertexComponent> results = this.bObjectService.getChildren(parent, relationshipType, FastTestDataset.DEFAULT_OVER_TIME_DATE);

        Assert.assertEquals(1, results.size());

        VertexComponent result = results.get(0);

        Assert.assertEquals(child.getCode(), result.getCode());
      }
      finally
      {
        this.bObjectService.delete(child);
      }
    }
    finally
    {
      this.bObjectService.delete(parent);
      ;
    }
  }

}
