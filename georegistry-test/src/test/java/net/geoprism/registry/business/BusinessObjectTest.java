/**
 *
 */
package net.geoprism.registry.business;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTimeCollection;
import com.runwaysdk.session.Request;

import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.InstanceTestClassExtension;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.graph.BusinessEdgeType;
import net.geoprism.registry.graph.BusinessType;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.EdgeDirection;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.graph.VertexComponent;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.query.graph.PageCriteria;
import net.geoprism.registry.query.graph.PageCriteria.MatchMode;
import net.geoprism.registry.service.business.BusinessEdgeTypeBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessObjectBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessTypeBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.view.BusinessEdgeTypeDTO;
import net.geoprism.registry.view.BusinessTypeDTO;
import net.geoprism.registry.view.JsonSerializable;
import net.geoprism.registry.view.JsonSerializablePage;
import net.geoprism.registry.view.ObjectAtTimeDTO;
import net.geoprism.registry.view.ObjectOverTimeDTO;
import net.geoprism.registry.view.ValueOverTimeEntryDTO;
import tools.jackson.databind.node.StringNode;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@ExtendWith(InstanceTestClassExtension.class)
public class BusinessObjectTest extends FastDatasetTest implements InstanceTestClassListener
{
  private static String                      TEST_CODE = "TEST_OBJ";

  private static BusinessType                type;

  private static AttributeType               attribute;

  private static AttributeType               attributeOverTime;

  private static AttributeLocalType          attributeLocal;

  private static AttributeClassificationType attributeClassification;

  private static AttributeClassificationType attributeClassificationOverTime;

  private static BusinessEdgeType            relationshipType;

  private static BusinessEdgeType            bGeoEdgeType;

  @Autowired
  private BusinessTypeBusinessServiceIF      bTypeService;

  @Autowired
  private BusinessEdgeTypeBusinessServiceIF  bEdgeService;

  @Autowired
  private BusinessObjectBusinessServiceIF    bObjectService;

  @Autowired
  private GeoObjectBusinessServiceIF         objectService;

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
    String code = "TEST_PROG";
    String orgCode = FastTestDataset.ORG_CGOV.getCode();
    String label = "Test Prog";

    BusinessTypeDTO object = new BusinessTypeDTO();
    object.setCode(code);
    object.setOrganization(orgCode);
    object.setDisplayLabel(new LocalizedValue(label));

    type = this.bTypeService.apply(object);

    attribute = this.bTypeService.createAttributeType(type, new AttributeCharacterType("testCharacter", new LocalizedValue("Test Character"), new LocalizedValue("Test True"), false, false, false, false));
    attributeLocal = this.bTypeService.createAttributeType(type, new AttributeLocalType("testLocal", new LocalizedValue("Test Local"), new LocalizedValue("Test Local"), false, false, false, false));
    attributeOverTime = this.bTypeService.createAttributeType(type, new AttributeLocalType("testCharacter2", new LocalizedValue("Test Character 2"), new LocalizedValue("Test True"), false, false, false, true));

    attributeClassification = this.createAttributeClassificationType();
    attributeClassification.setChangeOverTime(false);
    attributeClassification = (AttributeClassificationType) this.bTypeService.createAttributeType(type, attributeClassification);

    attributeClassificationOverTime = this.createDefaultClassificationType();
    attributeClassificationOverTime = (AttributeClassificationType) this.bTypeService.createAttributeType(type, attributeClassificationOverTime);

    relationshipType = this.bEdgeService.create(BusinessEdgeTypeDTO.build(FastTestDataset.ORG_CGOV.getCode(), "TEST_REL", new LocalizedValue("Test Rel"), new LocalizedValue("Test Rel"), type.getCode(), type.getCode()));

    bGeoEdgeType = this.bEdgeService.create(BusinessEdgeTypeDTO.build(FastTestDataset.ORG_CGOV.getCode(), "GEO_EDGE", new LocalizedValue("Geo Edge"), new LocalizedValue("Geo Edge"), BusinessEdgeTypeDTO.GEO_OBJECT_TYPE, type.getCode()));
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
      Assertions.assertNotNull(object.getVertex().getRID());
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
    object.setValue(attributeClassification.getCode(), rootConcept.getVertex());
    object.setValue(attributeClassificationOverTime.getCode(), parentConcept.getVertex(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE);
    object.setValue(DefaultAttribute.DATA_SOURCE.getName(), FastTestDataset.SOURCE.getDataSource(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE);
    object.setCode(TEST_CODE);

    this.bObjectService.apply(object);

    try
    {
      Assertions.assertEquals("Test Text", object.getValue(attribute.getCode()));
      Assertions.assertEquals("Test Text 2", ( (LocalizedValue) object.getValue(attributeOverTime.getCode(), FastTestDataset.DEFAULT_OVER_TIME_DATE) ).getLocalizedValue());
      Assertions.assertEquals(rootConcept.getOid(), object.getValue(attributeClassification.getCode()));
      Assertions.assertEquals(parentConcept.getOid(), object.getValue(attributeClassificationOverTime.getCode(), FastTestDataset.DEFAULT_OVER_TIME_DATE));

      ValueOverTimeCollection votc = object.getValuesOverTime(attributeClassificationOverTime.getCode());

      Assertions.assertEquals(1, votc.size());
      Assertions.assertEquals(FastTestDataset.DEFAULT_OVER_TIME_DATE, votc.get(0).getStartDate());
      Assertions.assertEquals(FastTestDataset.DEFAULT_END_TIME_DATE, votc.get(0).getEndDate());
      Assertions.assertEquals(parentConcept.getOid(), votc.get(0).getValue());
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
      BusinessObject result = this.bObjectService.get(type, attribute.getCode(), object.getValue(attribute.getCode())).orElse(null);

      Assertions.assertEquals(object.getVertex().getOid(), result.getVertex().getOid());
      Assertions.assertEquals(FastTestDataset.SOURCE.getDataSource().getOid(), (String) result.getValue(DefaultAttribute.DATA_SOURCE.getName()));

      Assertions.assertEquals("Test Text 2", ( (LocalizedValue) result.getValue(attributeOverTime.getCode(), FastTestDataset.DEFAULT_OVER_TIME_DATE) ).getLocalizedValue());
      Assertions.assertEquals("Test Text", result.getValue(attribute.getCode()));
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
      BusinessObject result = this.bObjectService.getByCode(type, object.getCode()).orElse(null);

      Assertions.assertEquals(object.getVertex().getOid(), result.getVertex().getOid());
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

      Assertions.assertEquals(1, results.size());

      VertexComponent result = (VertexComponent) results.get(0);

      Assertions.assertEquals(serverObject.getCode(), result.getCode());
    }
    finally
    {
      this.bObjectService.delete(object);
    }
  }

  @Test
  @Request
  public void testToFromObjectOverTimeDTO()
  {
    String text = "Test Text";

    BusinessObject object = this.bObjectService.newInstance(type);
    object.setValue(attribute.getCode(), text);
    object.setCode(TEST_CODE);
    object.setValue(attributeClassification.getCode(), parentConcept.getVertex());
    object.setValue(attributeClassificationOverTime.getCode(), childConcept.getVertex(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE);
    object.setValue(attributeOverTime.getCode(), new LocalizedValue("Test Text"), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE);
    object.setValue(DefaultAttribute.DATA_SOURCE.getName(), FastTestDataset.SOURCE.getDataSource(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE);

    ObjectOverTimeDTO dto = this.bObjectService.toDTO(object);

    Assertions.assertEquals(parentConcept.getCode(), dto.getValue(attributeClassification.getCode()));
    this.assertClassificationOverTime(dto, childConcept.getCode());

    object = this.bObjectService.newInstance(type);

    this.bObjectService.populate(object, dto);

    this.bObjectService.apply(object);

    try
    {
      // Validate the classification value was populated from the DTO
      Assertions.assertEquals(parentConcept.getOid(), object.getValue(attributeClassification.getCode()));
      Assertions.assertEquals(childConcept.getOid(), object.getValue(attributeClassificationOverTime.getCode(), FastTestDataset.DEFAULT_OVER_TIME_DATE));

      ValueOverTimeCollection votc = object.getValuesOverTime(attributeClassificationOverTime.getCode());

      Assertions.assertEquals(1, votc.size());
      Assertions.assertEquals(FastTestDataset.DEFAULT_OVER_TIME_DATE, votc.get(0).getStartDate());
      Assertions.assertEquals(FastTestDataset.DEFAULT_END_TIME_DATE, votc.get(0).getEndDate());

      dto = this.bObjectService.toDTO(object);

      dto = ObjectOverTimeDTO.parseJson(ObjectOverTimeDTO.toJson(dto));

      Assertions.assertNotNull(dto);
      Assertions.assertEquals(TEST_CODE, dto.getCode());
      Assertions.assertEquals(FastTestDataset.SOURCE.getCode(), dto.getValue(DefaultAttribute.DATA_SOURCE.getName(), FastTestDataset.DEFAULT_OVER_TIME_DATE).orElseThrow());
      Assertions.assertEquals(text, dto.getValue(attribute.getCode()));
      Assertions.assertEquals(parentConcept.getCode(), dto.getValue(attributeClassification.getCode()));
      this.assertClassificationOverTime(dto, childConcept.getCode());

      List<ValueOverTimeEntryDTO<LocalizedValue>> valuesOverTime = dto.getValuesOverTime(attributeOverTime.getCode());

      Assertions.assertEquals(1, valuesOverTime.size());

      ValueOverTimeEntryDTO<LocalizedValue> entry = valuesOverTime.get(0);

      Assertions.assertEquals(FastTestDataset.DEFAULT_OVER_TIME_DATE, entry.getStartDate());
      Assertions.assertEquals(FastTestDataset.DEFAULT_END_TIME_DATE, entry.getEndDate());
      Assertions.assertEquals(text, entry.getValue().getLocalizedValue());
    }
    finally
    {
      this.bObjectService.delete(object);
    }
  }

  @Test
  @Request
  public void testToFromObjectAtTimeDTO()
  {
    String text = "Test Text";

    BusinessObject object = this.bObjectService.newInstance(type);
    object.setValue(attribute.getCode(), text);
    object.setCode(TEST_CODE);
    object.setValue(attributeClassification.getCode(), parentConcept.getVertex());
    object.setValue(attributeClassificationOverTime.getCode(), childConcept.getVertex(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE);
    object.setValue(attributeOverTime.getCode(), new LocalizedValue("Test Text"), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE);
    object.setValue(DefaultAttribute.DATA_SOURCE.getName(), FastTestDataset.SOURCE.getDataSource(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE);

    ObjectAtTimeDTO dto = this.bObjectService.toDTO(object).toDate(FastTestDataset.DEFAULT_OVER_TIME_DATE);

    Assertions.assertEquals(parentConcept.getCode(), dto.getValue(attributeClassification.getCode()));
    Assertions.assertEquals(childConcept.getCode(), dto.getValue(attributeClassificationOverTime.getCode()));

    object = this.bObjectService.newInstance(type);

    this.bObjectService.populate(object, dto, FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE);

    this.bObjectService.apply(object);

    try
    {
      // Validate the classification value was populated from the DTO
      Assertions.assertEquals(parentConcept.getOid(), object.getValue(attributeClassification.getCode()));
      Assertions.assertEquals(childConcept.getOid(), object.getValue(attributeClassificationOverTime.getCode(), FastTestDataset.DEFAULT_OVER_TIME_DATE));

      ValueOverTimeCollection votc = object.getValuesOverTime(attributeClassificationOverTime.getCode());

      Assertions.assertEquals(1, votc.size());
      Assertions.assertEquals(FastTestDataset.DEFAULT_OVER_TIME_DATE, votc.get(0).getStartDate());
      Assertions.assertEquals(FastTestDataset.DEFAULT_END_TIME_DATE, votc.get(0).getEndDate());

      dto = this.bObjectService.toDTO(object).toDate(FastTestDataset.DEFAULT_OVER_TIME_DATE);

      dto = ObjectAtTimeDTO.parseJson(ObjectAtTimeDTO.toJson(dto));

      Assertions.assertNotNull(dto);
      Assertions.assertEquals(TEST_CODE, dto.getCode());
      Assertions.assertEquals(FastTestDataset.SOURCE.getCode(), dto.getValue(DefaultAttribute.DATA_SOURCE.getName()));
      Assertions.assertEquals(text, dto.getValue(attribute.getCode()));
      Assertions.assertEquals(parentConcept.getCode(), dto.getValue(attributeClassification.getCode()));
      Assertions.assertEquals(childConcept.getCode(), dto.getValue(attributeClassificationOverTime.getCode()));

      Map<String, String> localizedValue = dto.getValue(attributeOverTime.getCode());

      Assertions.assertEquals(text, localizedValue.get(LocalizedValue.LOCALIZED_VALUE));
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

      Assertions.assertEquals(0, this.bObjectService.getParents(object, bGeoEdgeType, FastTestDataset.DEFAULT_OVER_TIME_DATE).size());
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

      Assertions.assertEquals(1, results.size());

      VertexComponent result = results.get(0);

      Assertions.assertEquals(serverObject.getCode(), result.getCode());
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

      List<BusinessObject> results = this.objectService.getBusinessObjects((VertexServerGeoObject) serverObject, bGeoEdgeType, EdgeDirection.CHILD, null);

      Assertions.assertEquals(1, results.size());

      BusinessObject result = results.get(0);

      Assertions.assertEquals(object.getCode(), result.getCode());
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

      List<BusinessObject> results = this.objectService.getBusinessObjects((VertexServerGeoObject) serverObject, bGeoEdgeType, EdgeDirection.CHILD, null);

      Assertions.assertEquals(1, results.size());

      BusinessObject result = results.get(0);

      Assertions.assertEquals(object.getCode(), result.getCode());
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

        Assertions.assertEquals(uid, edge.getObjectValue(DefaultAttribute.UID.getName()));
        Assertions.assertNotNull(edge.getObjectValue(DefaultAttribute.DATA_SOURCE.getName()));

        List<VertexComponent> results = this.bObjectService.getParents(child, relationshipType, FastTestDataset.DEFAULT_OVER_TIME_DATE);

        Assertions.assertEquals(1, results.size());

        VertexComponent result = results.get(0);

        Assertions.assertEquals(parent.getCode(), result.getCode());
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

        Assertions.assertEquals(0, this.bObjectService.getParents(child, relationshipType, FastTestDataset.DEFAULT_OVER_TIME_DATE).size());
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

        Assertions.assertEquals(1, results.size());

        VertexComponent result = results.get(0);

        Assertions.assertEquals(parent.getCode(), result.getCode());
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

        Assertions.assertEquals(uid, edge.getObjectValue(DefaultAttribute.UID.getName()));
        Assertions.assertNotNull(edge.getObjectValue(DefaultAttribute.DATA_SOURCE.getName()));

        List<VertexComponent> results = this.bObjectService.getChildren(parent, relationshipType, FastTestDataset.DEFAULT_OVER_TIME_DATE);

        Assertions.assertEquals(1, results.size());

        BusinessObject result = (BusinessObject) results.get(0);

        Assertions.assertEquals(child.getCode(), result.getCode());
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

        Assertions.assertEquals(0, this.bObjectService.getChildren(parent, relationshipType, FastTestDataset.DEFAULT_OVER_TIME_DATE).size());
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

        Assertions.assertEquals(1, results.size());

        VertexComponent result = results.get(0);

        Assertions.assertEquals(child.getCode(), result.getCode());
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

  private void assertClassificationOverTime(ObjectOverTimeDTO dto, String expectedCode)
  {
    List<ValueOverTimeEntryDTO<String>> entries = dto.getValuesOverTime(attributeClassificationOverTime.getCode());

    Assertions.assertEquals(1, entries.size());

    ValueOverTimeEntryDTO<String> entry = entries.get(0);

    Assertions.assertEquals(FastTestDataset.DEFAULT_OVER_TIME_DATE, entry.getStartDate());
    Assertions.assertEquals(FastTestDataset.DEFAULT_END_TIME_DATE, entry.getEndDate());
    Assertions.assertEquals(expectedCode, entry.getValue());
    Assertions.assertEquals(expectedCode, dto.getValue(attributeClassificationOverTime.getCode(), FastTestDataset.DEFAULT_OVER_TIME_DATE).orElseThrow());
  }

  /*
   * Tests for BusinessObjectBusinessService.data()
   */

  private static final String DATA_ALPHA = "Alpha Object";

  private static final String DATA_BETA  = "Beta Object";

  private static final String DATA_GAMMA = "Gamma Item";

  private List<BusinessObject> createDataObjects()
  {
    List<BusinessObject> objects = new LinkedList<>();

    try
    {
      for (String code : List.of(DATA_ALPHA, DATA_BETA, DATA_GAMMA))
      {
        BusinessObject object = this.bObjectService.newInstance(type);
        object.setCode(code);
        object.setValue(attribute.getCode(), code + " Text");
        object.setValue(attributeLocal.getCode(), new LocalizedValue(code + " Localized"));

        this.bObjectService.apply(object);

        objects.add(object);
      }
    }
    catch (RuntimeException e)
    {
      deleteDataObjects(objects);

      throw e;
    }

    return objects;
  }

  private void deleteDataObjects(List<BusinessObject> objects)
  {
    objects.forEach(object -> this.bObjectService.delete(object));
  }

  @Test
  @Request
  public void testData()
  {
    List<BusinessObject> objects = createDataObjects();

    try
    {
      PageCriteria criteria = new PageCriteria();
      criteria.setRows(10);

      JsonSerializablePage<JsonSerializable> page = this.bObjectService.data(type, criteria);

      Assertions.assertEquals(3L, page.getCount());
      Assertions.assertEquals(1, page.getPageNumber());
      Assertions.assertEquals(10, page.getPageSize());
      Assertions.assertEquals(3, page.getResultSet().size());

      List<String> codes = getCodes(page);

      Assertions.assertTrue(codes.contains(DATA_ALPHA));
      Assertions.assertTrue(codes.contains(DATA_BETA));
      Assertions.assertTrue(codes.contains(DATA_GAMMA));
    }
    finally
    {
      deleteDataObjects(objects);
    }
  }

  @Test
  @Request
  public void testDataEmpty()
  {
    PageCriteria criteria = new PageCriteria();
    criteria.setRows(10);

    JsonSerializablePage<JsonSerializable> page = this.bObjectService.data(type, criteria);

    Assertions.assertEquals(0L, page.getCount());
    Assertions.assertEquals(0, page.getResultSet().size());
  }

  @Test
  @Request
  public void testDataAttributeValues()
  {
    BusinessObject object = this.bObjectService.newInstance(type);
    object.setCode(TEST_CODE);
    object.setValue(attribute.getCode(), "Test Text");
    object.setValue(attributeOverTime.getCode(), new LocalizedValue("Test Text 2"), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE);
    object.setValue(attributeClassification.getCode(), rootConcept.getVertex());
    object.setValue(attributeClassificationOverTime.getCode(), parentConcept.getVertex(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_END_TIME_DATE);
    this.bObjectService.apply(object);

    try
    {
      PageCriteria criteria = new PageCriteria();
      criteria.setRows(10);

      JsonSerializablePage<JsonSerializable> page = this.bObjectService.data(type, criteria);

      Assertions.assertEquals(1L, page.getCount());
      Assertions.assertEquals(1, page.getResultSet().size());

      JsonObject result = page.getResultSet().get(0).toJSON().getAsJsonObject();

      Assertions.assertEquals(TEST_CODE, result.get(DefaultAttribute.CODE.getName()).getAsString());
      Assertions.assertEquals("Test Text", result.get(attribute.getCode()).getAsString());
      Assertions.assertEquals(rootConcept.getCode(), result.get(attributeClassification.getCode()).getAsString());

      // Change over time attributes are not included in the data results
      Assertions.assertFalse(result.has(attributeOverTime.getCode()));
      Assertions.assertFalse(result.has(attributeClassificationOverTime.getCode()));
    }
    finally
    {
      this.bObjectService.delete(object);
    }
  }

  @Test
  @Request
  public void testDataSortAscending()
  {
    List<BusinessObject> objects = createDataObjects();

    try
    {
      PageCriteria criteria = new PageCriteria();
      criteria.setRows(10);
      criteria.setSortField(DefaultAttribute.CODE.getName());
      criteria.setSortOrder(1);

      JsonSerializablePage<JsonSerializable> page = this.bObjectService.data(type, criteria);

      Assertions.assertEquals(List.of(DATA_ALPHA, DATA_BETA, DATA_GAMMA), getCodes(page));
    }
    finally
    {
      deleteDataObjects(objects);
    }
  }

  @Test
  @Request
  public void testDataSortDescending()
  {
    List<BusinessObject> objects = createDataObjects();

    try
    {
      PageCriteria criteria = new PageCriteria();
      criteria.setRows(10);
      criteria.setSortField(DefaultAttribute.CODE.getName());
      criteria.setSortOrder(-1);

      JsonSerializablePage<JsonSerializable> page = this.bObjectService.data(type, criteria);

      Assertions.assertEquals(List.of(DATA_GAMMA, DATA_BETA, DATA_ALPHA), getCodes(page));
    }
    finally
    {
      deleteDataObjects(objects);
    }
  }

  @Test
  @Request
  public void testDataMultiSort()
  {
    List<BusinessObject> objects = createDataObjects();

    try
    {
      PageCriteria criteria = new PageCriteria();
      criteria.setRows(10);
      criteria.setMultiSortMeta(List.of(new PageCriteria.SortMeta(attribute.getCode(), -1)));

      JsonSerializablePage<JsonSerializable> page = this.bObjectService.data(type, criteria);

      Assertions.assertEquals(List.of(DATA_GAMMA, DATA_BETA, DATA_ALPHA), getCodes(page));
    }
    finally
    {
      deleteDataObjects(objects);
    }
  }

  @Test
  @Request
  public void testDataPagination()
  {
    List<BusinessObject> objects = createDataObjects();

    try
    {
      PageCriteria criteria = new PageCriteria();
      criteria.setFirst(0);
      criteria.setRows(2);
      criteria.setSortField(DefaultAttribute.CODE.getName());
      criteria.setSortOrder(1);

      JsonSerializablePage<JsonSerializable> page = this.bObjectService.data(type, criteria);

      Assertions.assertEquals(3L, page.getCount());
      Assertions.assertEquals(1, page.getPageNumber());
      Assertions.assertEquals(2, page.getPageSize());
      Assertions.assertEquals(List.of(DATA_ALPHA, DATA_BETA), getCodes(page));

      criteria.setFirst(2);

      page = this.bObjectService.data(type, criteria);

      Assertions.assertEquals(3L, page.getCount());
      Assertions.assertEquals(2, page.getPageNumber());
      Assertions.assertEquals(2, page.getPageSize());
      Assertions.assertEquals(List.of(DATA_GAMMA), getCodes(page));
    }
    finally
    {
      deleteDataObjects(objects);
    }
  }

  @Test
  @Request
  public void testDataFilterEquals()
  {
    List<BusinessObject> objects = createDataObjects();

    try
    {
      JsonSerializablePage<JsonSerializable> page = this.bObjectService.data(type, dataFilter(DefaultAttribute.CODE.getName(), MatchMode.EQUALS, DATA_BETA));

      Assertions.assertEquals(1L, page.getCount());
      Assertions.assertEquals(List.of(DATA_BETA), getCodes(page));
    }
    finally
    {
      deleteDataObjects(objects);
    }
  }

  @Test
  @Request
  public void testDataFilterStartsWith()
  {
    List<BusinessObject> objects = createDataObjects();

    try
    {
      // Starts with matching is case insensitive
      JsonSerializablePage<JsonSerializable> page = this.bObjectService.data(type, dataFilter(DefaultAttribute.CODE.getName(), MatchMode.STARTS_WITH, "gam"));

      Assertions.assertEquals(1L, page.getCount());
      Assertions.assertEquals(List.of(DATA_GAMMA), getCodes(page));
    }
    finally
    {
      deleteDataObjects(objects);
    }
  }

  @Test
  @Request
  public void testDataFilterContains()
  {
    List<BusinessObject> objects = createDataObjects();

    try
    {
      // Contains matching is case insensitive
      PageCriteria criteria = dataFilter(DefaultAttribute.CODE.getName(), MatchMode.CONTAINS, "OBJECT");
      criteria.setSortField(DefaultAttribute.CODE.getName());
      criteria.setSortOrder(1);

      JsonSerializablePage<JsonSerializable> page = this.bObjectService.data(type, criteria);

      Assertions.assertEquals(2L, page.getCount());
      Assertions.assertEquals(List.of(DATA_ALPHA, DATA_BETA), getCodes(page));
    }
    finally
    {
      deleteDataObjects(objects);
    }
  }

  @Test
  @Request
  public void testDataFilterNoMatch()
  {
    List<BusinessObject> objects = createDataObjects();

    try
    {
      JsonSerializablePage<JsonSerializable> page = this.bObjectService.data(type, dataFilter(DefaultAttribute.CODE.getName(), MatchMode.EQUALS, "BAD"));

      Assertions.assertEquals(0L, page.getCount());
      Assertions.assertEquals(0, page.getResultSet().size());
    }
    finally
    {
      deleteDataObjects(objects);
    }
  }

  @Test
  @Request
  public void testDataFilterUnknownAttribute()
  {
    List<BusinessObject> objects = createDataObjects();

    try
    {
      // Filters on attributes which do not exist on the type are ignored
      JsonSerializablePage<JsonSerializable> page = this.bObjectService.data(type, dataFilter("doesNotExist", MatchMode.EQUALS, "BAD"));

      Assertions.assertEquals(3L, page.getCount());
      Assertions.assertEquals(3, page.getResultSet().size());
    }
    finally
    {
      deleteDataObjects(objects);
    }
  }

  @Test
  @Request
  public void testDataFilterAttribute()
  {
    List<BusinessObject> objects = createDataObjects();

    try
    {
      JsonSerializablePage<JsonSerializable> page = this.bObjectService.data(type, dataFilter(attribute.getCode(), MatchMode.CONTAINS, "alpha object text"));

      Assertions.assertEquals(1L, page.getCount());
      Assertions.assertEquals(List.of(DATA_ALPHA), getCodes(page));
    }
    finally
    {
      deleteDataObjects(objects);
    }
  }

  @Test
  @Request
  public void testDataFilterClassification()
  {
    List<BusinessObject> objects = createDataObjects();

    try
    {
      objects.get(0).setValue(attributeClassification.getCode(), rootConcept.getVertex());
      this.bObjectService.apply(objects.get(0));

      objects.get(1).setValue(attributeClassification.getCode(), parentConcept.getVertex());
      this.bObjectService.apply(objects.get(1));

      JsonSerializablePage<JsonSerializable> page = this.bObjectService.data(type, dataFilter(attributeClassification.getCode(), MatchMode.EQUALS, rootConcept.getCode()));

      Assertions.assertEquals(1L, page.getCount());
      Assertions.assertEquals(List.of(DATA_ALPHA), getCodes(page));
    }
    finally
    {
      deleteDataObjects(objects);
    }
  }

  @Test
  @Request
  public void testDataSortClassification()
  {
    List<BusinessObject> objects = createDataObjects();

    try
    {
      // Classification attributes are sorted by the code of the concept
      setClassifications(objects);

      PageCriteria criteria = new PageCriteria();
      criteria.setRows(10);
      criteria.setSortField(attributeClassification.getCode());
      criteria.setSortOrder(1);

      JsonSerializablePage<JsonSerializable> page = this.bObjectService.data(type, criteria);

      Assertions.assertEquals(List.of(DATA_BETA, DATA_GAMMA, DATA_ALPHA), getCodes(page));
      Assertions.assertEquals(List.of(CHILD_CONCEPT, PARENT_CONCEPT, ROOT_CONCEPT), getValues(page, attributeClassification.getCode()));

      criteria.setSortOrder(-1);

      page = this.bObjectService.data(type, criteria);

      Assertions.assertEquals(List.of(DATA_ALPHA, DATA_GAMMA, DATA_BETA), getCodes(page));
      Assertions.assertEquals(List.of(ROOT_CONCEPT, PARENT_CONCEPT, CHILD_CONCEPT), getValues(page, attributeClassification.getCode()));
    }
    finally
    {
      deleteDataObjects(objects);
    }
  }

  @Test
  @Request
  public void testDataMultiSortClassification()
  {
    List<BusinessObject> objects = createDataObjects();

    try
    {
      setClassifications(objects);

      PageCriteria criteria = new PageCriteria();
      criteria.setRows(10);
      criteria.setMultiSortMeta(List.of(new PageCriteria.SortMeta(attributeClassification.getCode(), -1)));

      JsonSerializablePage<JsonSerializable> page = this.bObjectService.data(type, criteria);

      Assertions.assertEquals(List.of(DATA_ALPHA, DATA_GAMMA, DATA_BETA), getCodes(page));
    }
    finally
    {
      deleteDataObjects(objects);
    }
  }

  /**
   * Assigns classifications so that the classification order differs from the
   * code order: Alpha -> ROOT_CONCEPT ("Test Term"), Beta -> CHILD_CONCEPT
   * ("Child Concept"), Gamma -> PARENT_CONCEPT ("Parent Concept")
   */
  private void setClassifications(List<BusinessObject> objects)
  {
    objects.get(0).setValue(attributeClassification.getCode(), rootConcept.getVertex());
    this.bObjectService.apply(objects.get(0));

    objects.get(1).setValue(attributeClassification.getCode(), childConcept.getVertex());
    this.bObjectService.apply(objects.get(1));

    objects.get(2).setValue(attributeClassification.getCode(), parentConcept.getVertex());
    this.bObjectService.apply(objects.get(2));
  }

  private static List<String> getValues(JsonSerializablePage<JsonSerializable> page, String attributeName)
  {
    return page.getResultSet().stream() //
        .map(result -> result.toJSON().getAsJsonObject().get(attributeName).getAsString()) //
        .collect(Collectors.toList());
  }

  @Test
  @Request
  public void testDataLocalValues()
  {
    List<BusinessObject> objects = createDataObjects();

    try
    {
      setLocals(objects);

      PageCriteria criteria = new PageCriteria();
      criteria.setRows(10);
      criteria.setSortField(DefaultAttribute.CODE.getName());
      criteria.setSortOrder(1);

      JsonSerializablePage<JsonSerializable> page = this.bObjectService.data(type, criteria);

      Assertions.assertEquals(List.of(DATA_ALPHA, DATA_BETA, DATA_GAMMA), getCodes(page));
      Assertions.assertEquals(List.of(LOCAL_ZEBRA, LOCAL_APPLE, LOCAL_MANGO), getValues(page, attributeLocal.getCode()));
    }
    finally
    {
      deleteDataObjects(objects);
    }
  }

  @Test
  @Request
  public void testDataFilterLocalEquals()
  {
    List<BusinessObject> objects = createDataObjects();

    try
    {
      setLocals(objects);

      JsonSerializablePage<JsonSerializable> page = this.bObjectService.data(type, dataFilter(attributeLocal.getCode(), MatchMode.EQUALS, LOCAL_MANGO));

      Assertions.assertEquals(1L, page.getCount());
      Assertions.assertEquals(List.of(DATA_GAMMA), getCodes(page));
      Assertions.assertEquals(List.of(LOCAL_MANGO), getValues(page, attributeLocal.getCode()));
    }
    finally
    {
      deleteDataObjects(objects);
    }
  }

  @Test
  @Request
  public void testDataFilterLocalStartsWith()
  {
    List<BusinessObject> objects = createDataObjects();

    try
    {
      setLocals(objects);

      // Starts with matching is case insensitive
      JsonSerializablePage<JsonSerializable> page = this.bObjectService.data(type, dataFilter(attributeLocal.getCode(), MatchMode.STARTS_WITH, "zeb"));

      Assertions.assertEquals(1L, page.getCount());
      Assertions.assertEquals(List.of(DATA_ALPHA), getCodes(page));
    }
    finally
    {
      deleteDataObjects(objects);
    }
  }

  @Test
  @Request
  public void testDataFilterLocalContains()
  {
    List<BusinessObject> objects = createDataObjects();

    try
    {
      setLocals(objects);

      // Contains matching is case insensitive
      JsonSerializablePage<JsonSerializable> page = this.bObjectService.data(type, dataFilter(attributeLocal.getCode(), MatchMode.CONTAINS, "PPL"));

      Assertions.assertEquals(1L, page.getCount());
      Assertions.assertEquals(List.of(DATA_BETA), getCodes(page));
    }
    finally
    {
      deleteDataObjects(objects);
    }
  }

  @Test
  @Request
  public void testDataSortLocal()
  {
    List<BusinessObject> objects = createDataObjects();

    try
    {
      setLocals(objects);

      PageCriteria criteria = new PageCriteria();
      criteria.setRows(10);
      criteria.setSortField(attributeLocal.getCode());
      criteria.setSortOrder(1);

      JsonSerializablePage<JsonSerializable> page = this.bObjectService.data(type, criteria);

      Assertions.assertEquals(List.of(DATA_BETA, DATA_GAMMA, DATA_ALPHA), getCodes(page));
      Assertions.assertEquals(List.of(LOCAL_APPLE, LOCAL_MANGO, LOCAL_ZEBRA), getValues(page, attributeLocal.getCode()));

      criteria.setSortOrder(-1);

      page = this.bObjectService.data(type, criteria);

      Assertions.assertEquals(List.of(DATA_ALPHA, DATA_GAMMA, DATA_BETA), getCodes(page));
      Assertions.assertEquals(List.of(LOCAL_ZEBRA, LOCAL_MANGO, LOCAL_APPLE), getValues(page, attributeLocal.getCode()));
    }
    finally
    {
      deleteDataObjects(objects);
    }
  }

  @Test
  @Request
  public void testDataMultiSortLocal()
  {
    List<BusinessObject> objects = createDataObjects();

    try
    {
      setLocals(objects);

      PageCriteria criteria = new PageCriteria();
      criteria.setRows(10);
      criteria.setMultiSortMeta(List.of(new PageCriteria.SortMeta(attributeLocal.getCode(), -1)));

      JsonSerializablePage<JsonSerializable> page = this.bObjectService.data(type, criteria);

      Assertions.assertEquals(List.of(DATA_ALPHA, DATA_GAMMA, DATA_BETA), getCodes(page));
    }
    finally
    {
      deleteDataObjects(objects);
    }
  }

  private static final String LOCAL_ZEBRA = "Zebra Label";

  private static final String LOCAL_APPLE = "Apple Label";

  private static final String LOCAL_MANGO = "Mango Label";

  /**
   * Assigns local values so that the local value order differs from the code
   * order: Alpha -> "Zebra Label", Beta -> "Apple Label", Gamma -> "Mango
   * Label"
   */
  private void setLocals(List<BusinessObject> objects)
  {
    List<String> values = List.of(LOCAL_ZEBRA, LOCAL_APPLE, LOCAL_MANGO);

    for (int i = 0; i < objects.size(); i++)
    {
      objects.get(i).setValue(attributeLocal.getCode(), new LocalizedValue(values.get(i)));
      this.bObjectService.apply(objects.get(i));
    }
  }

  private static PageCriteria dataFilter(String attributeName, MatchMode mode, String value)
  {
    PageCriteria criteria = new PageCriteria();
    criteria.setRows(10);
    criteria.setFilters(Map.of(attributeName, new PageCriteria.Filter(mode, new StringNode(value))));

    return criteria;
  }

  private static List<String> getCodes(JsonSerializablePage<JsonSerializable> page)
  {
    return page.getResultSet().stream() //
        .map(result -> result.toJSON().getAsJsonObject().get(DefaultAttribute.CODE.getName()).getAsString()) //
        .collect(Collectors.toList());
  }

}
