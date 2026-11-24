/**
 *
 */
package net.geoprism.registry.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.session.Request;

import net.geoprism.GenericException;
import net.geoprism.registry.ConceptDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.InstanceTestClassExtension;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.graph.ConceptClass;
import net.geoprism.registry.model.ConceptObject;
import net.geoprism.registry.query.graph.PageCriteria;
import net.geoprism.registry.query.graph.PageCriteria.MatchMode;
import net.geoprism.registry.test.TestOrganizationInfo;
import net.geoprism.registry.test.USATestData;
import net.geoprism.registry.view.ConceptSetDTO;
import net.geoprism.registry.view.DiscreteType;
import net.geoprism.registry.view.JsonSerializable;
import net.geoprism.registry.view.JsonSerializablePage;
import net.geoprism.registry.view.ObjectOverTimeDTO;
import net.geoprism.registry.view.ValueOverTimeEntryDTO;

import tools.jackson.databind.node.StringNode;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@ExtendWith(InstanceTestClassExtension.class)
public class ConceptObjectServiceTest extends ConceptDatasetTest implements InstanceTestClassListener
{
  private static String        TEST_CODE = "TEST_OBJ";

  private static AttributeType attribute;

  private static AttributeType attributeOverTime;

  private static ConceptClass  secondClass;

  public ConceptSetDTO mockConceptSet(String code, String label, String description)
  {
    ConceptSetDTO dto = super.mockConceptSet(code, label, description);
    dto.setDiscreteType(DiscreteType.ENUMERATION);
    dto.setRootTerm(null);

    return dto;
  }

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
      Assertions.assertNotNull(object.getVertex().getRID());
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
      Assertions.assertEquals("Test Text", object.getValue(attribute.getCode()));
      Assertions.assertEquals("Test Text 2", object.getValue(attributeOverTime.getCode(), USATestData.DEFAULT_OVER_TIME_DATE));
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

      Assertions.assertEquals(object.getVertex().getOid(), result.getVertex().getOid());
      Assertions.assertEquals(USATestData.SOURCE.getDataSource().getOid(), (String) result.getValue(DefaultAttribute.DATA_SOURCE.getName()));
      Assertions.assertEquals("Test Text 2", result.getValue(attributeOverTime.getCode(), USATestData.DEFAULT_OVER_TIME_DATE));
      Assertions.assertEquals("Test Text", result.getValue(attribute.getCode()));
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

      Assertions.assertEquals(object.getVertex().getOid(), result.getVertex().getOid());
    }
    finally
    {
      this.cObjectService.delete(object);
    }
  }

  @Test
  @Request
  public void testGetByCode_BAD()
  {
    ConceptObject object = this.cObjectService.newInstance(cClass);
    object.setValue(attribute.getCode(), "Test Text");
    object.setCode(TEST_CODE);
    this.cObjectService.apply(object);

    try
    {
      ConceptObject result = this.cObjectService.getByCode(cClass, "BAD").orElse(null);

      Assertions.assertNull(result);
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

      Assertions.assertNotNull(result);
      Assertions.assertEquals(object.getVertex().getOid(), result.getVertex().getOid());
    }
    finally
    {
      this.cObjectService.delete(object);
    }
  }

  @Test
  @Request
  public void testBadConceptSetGetByCode()
  {
    assertThrows(GenericException.class, () -> {
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
    });
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

      Assertions.assertNotNull(dto);
      Assertions.assertEquals(TEST_CODE, dto.getCode());

      Assertions.assertEquals(USATestData.SOURCE.getCode(), dto.getValue(DefaultAttribute.DATA_SOURCE.getName(), USATestData.DEFAULT_OVER_TIME_DATE).get());
      Assertions.assertEquals(text, dto.getValue(attribute.getCode()));

      List<ValueOverTimeEntryDTO<String>> valuesOverTime = dto.getValuesOverTime(attributeOverTime.getCode());

      Assertions.assertEquals(1, valuesOverTime.size());

      ValueOverTimeEntryDTO<String> entry = valuesOverTime.get(0);

      Assertions.assertEquals(USATestData.DEFAULT_OVER_TIME_DATE, entry.getStartDate());
      Assertions.assertEquals(USATestData.DEFAULT_END_TIME_DATE, entry.getEndDate());
      Assertions.assertEquals(text, entry.getValue());
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

        Assertions.assertEquals(uid, edge.getObjectValue(DefaultAttribute.UID.getName()));
        Assertions.assertNotNull(edge.getObjectValue(DefaultAttribute.DATA_SOURCE.getName()));

        List<ConceptObject> results = this.cObjectService.getChildren(parent, cEdgeType, USATestData.DEFAULT_OVER_TIME_DATE);

        Assertions.assertEquals(1, results.size());

        ConceptObject result = (ConceptObject) results.get(0);

        Assertions.assertEquals(child.getCode(), result.getCode());
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

        Assertions.assertEquals(0, this.cObjectService.getChildren(parent, cEdgeType, USATestData.DEFAULT_OVER_TIME_DATE).size());
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

        Assertions.assertEquals(1, results.size());

        ConceptObject result = results.get(0);

        Assertions.assertEquals(child.getCode(), result.getCode());
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
  public void testAddCycle()
  {
    assertThrows(GenericException.class, () -> {
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
    });
  }

  /*
   * Tests for ConceptObjectBusinessService.data(). Note that ConceptDatasetTest
   * creates three concepts before each test: ROOT_CONCEPT ("Test Term"),
   * PARENT_CONCEPT ("Parent Concept") and CHILD_CONCEPT ("Child Concept").
   */

  @Test
  @Request
  public void testData()
  {
    PageCriteria criteria = new PageCriteria();
    criteria.setRows(10);

    JsonSerializablePage<JsonSerializable> page = this.cObjectService.data(cClass, criteria);

    Assertions.assertEquals(3L, page.getCount());
    Assertions.assertEquals(1, page.getPageNumber());
    Assertions.assertEquals(10, page.getPageSize());
    Assertions.assertEquals(3, page.getResultSet().size());

    List<String> codes = getCodes(page);

    Assertions.assertTrue(codes.contains(ROOT_CONCEPT));
    Assertions.assertTrue(codes.contains(PARENT_CONCEPT));
    Assertions.assertTrue(codes.contains(CHILD_CONCEPT));
  }

  @Test
  @Request
  public void testDataEmpty()
  {
    PageCriteria criteria = new PageCriteria();
    criteria.setRows(10);

    JsonSerializablePage<JsonSerializable> page = this.cObjectService.data(secondClass, criteria);

    Assertions.assertEquals(0L, page.getCount());
    Assertions.assertEquals(0, page.getResultSet().size());
  }

  @Test
  @Request
  public void testDataAttributeValues()
  {
    ConceptObject object = this.cObjectService.newInstance(cClass);
    object.setCode(TEST_CODE);
    object.setValue(attribute.getCode(), "Test Text");
    object.setValue(attributeOverTime.getCode(), "Test Text 2", USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
    this.cObjectService.apply(object);

    try
    {
      PageCriteria criteria = new PageCriteria();
      criteria.setRows(10);
      criteria.setFilters(Map.of(DefaultAttribute.CODE.getName(), new PageCriteria.Filter(MatchMode.EQUALS, new StringNode(TEST_CODE))));

      JsonSerializablePage<JsonSerializable> page = this.cObjectService.data(cClass, criteria);

      Assertions.assertEquals(1L, page.getCount());
      Assertions.assertEquals(1, page.getResultSet().size());

      JsonObject result = page.getResultSet().get(0).toJSON().getAsJsonObject();

      Assertions.assertEquals(TEST_CODE, result.get(DefaultAttribute.CODE.getName()).getAsString());
      Assertions.assertEquals("Test Text", result.get(attribute.getCode()).getAsString());

      // Change over time attributes are not included in the data results
      Assertions.assertFalse(result.has(attributeOverTime.getCode()));
    }
    finally
    {
      this.cObjectService.delete(object);
    }
  }

  @Test
  @Request
  public void testDataSortAscending()
  {
    PageCriteria criteria = new PageCriteria();
    criteria.setRows(10);
    criteria.setSortField(DefaultAttribute.CODE.getName());
    criteria.setSortOrder(1);

    JsonSerializablePage<JsonSerializable> page = this.cObjectService.data(cClass, criteria);

    Assertions.assertEquals(List.of(CHILD_CONCEPT, PARENT_CONCEPT, ROOT_CONCEPT), getCodes(page));
  }

  @Test
  @Request
  public void testDataSortDescending()
  {
    PageCriteria criteria = new PageCriteria();
    criteria.setRows(10);
    criteria.setSortField(DefaultAttribute.CODE.getName());
    criteria.setSortOrder(-1);

    JsonSerializablePage<JsonSerializable> page = this.cObjectService.data(cClass, criteria);

    Assertions.assertEquals(List.of(ROOT_CONCEPT, PARENT_CONCEPT, CHILD_CONCEPT), getCodes(page));
  }

  @Test
  @Request
  public void testDataMultiSort()
  {
    PageCriteria criteria = new PageCriteria();
    criteria.setRows(10);
    criteria.setMultiSortMeta(List.of(new PageCriteria.SortMeta(DefaultAttribute.CODE.getName(), -1)));

    JsonSerializablePage<JsonSerializable> page = this.cObjectService.data(cClass, criteria);

    Assertions.assertEquals(List.of(ROOT_CONCEPT, PARENT_CONCEPT, CHILD_CONCEPT), getCodes(page));
  }

  @Test
  @Request
  public void testDataPagination()
  {
    PageCriteria criteria = new PageCriteria();
    criteria.setFirst(0);
    criteria.setRows(2);
    criteria.setSortField(DefaultAttribute.CODE.getName());
    criteria.setSortOrder(1);

    JsonSerializablePage<JsonSerializable> page = this.cObjectService.data(cClass, criteria);

    Assertions.assertEquals(3L, page.getCount());
    Assertions.assertEquals(1, page.getPageNumber());
    Assertions.assertEquals(2, page.getPageSize());
    Assertions.assertEquals(List.of(CHILD_CONCEPT, PARENT_CONCEPT), getCodes(page));

    criteria.setFirst(2);

    page = this.cObjectService.data(cClass, criteria);

    Assertions.assertEquals(3L, page.getCount());
    Assertions.assertEquals(2, page.getPageNumber());
    Assertions.assertEquals(2, page.getPageSize());
    Assertions.assertEquals(List.of(ROOT_CONCEPT), getCodes(page));
  }

  @Test
  @Request
  public void testDataFilterEquals()
  {
    JsonSerializablePage<JsonSerializable> page = this.cObjectService.data(cClass, codeFilter(MatchMode.EQUALS, PARENT_CONCEPT));

    Assertions.assertEquals(1L, page.getCount());
    Assertions.assertEquals(List.of(PARENT_CONCEPT), getCodes(page));
  }

  @Test
  @Request
  public void testDataFilterStartsWith()
  {
    // Starts with matching is case insensitive
    JsonSerializablePage<JsonSerializable> page = this.cObjectService.data(cClass, codeFilter(MatchMode.STARTS_WITH, "parent"));

    Assertions.assertEquals(1L, page.getCount());
    Assertions.assertEquals(List.of(PARENT_CONCEPT), getCodes(page));
  }

  @Test
  @Request
  public void testDataFilterContains()
  {
    // Contains matching is case insensitive
    PageCriteria criteria = codeFilter(MatchMode.CONTAINS, "CONCEPT");
    criteria.setSortField(DefaultAttribute.CODE.getName());
    criteria.setSortOrder(1);

    JsonSerializablePage<JsonSerializable> page = this.cObjectService.data(cClass, criteria);

    Assertions.assertEquals(2L, page.getCount());
    Assertions.assertEquals(List.of(CHILD_CONCEPT, PARENT_CONCEPT), getCodes(page));
  }

  @Test
  @Request
  public void testDataFilterNoMatch()
  {
    JsonSerializablePage<JsonSerializable> page = this.cObjectService.data(cClass, codeFilter(MatchMode.EQUALS, "BAD"));

    Assertions.assertEquals(0L, page.getCount());
    Assertions.assertEquals(0, page.getResultSet().size());
  }

  @Test
  @Request
  public void testDataFilterUnknownAttribute()
  {
    // Filters on attributes which do not exist on the type are ignored
    PageCriteria criteria = new PageCriteria();
    criteria.setRows(10);
    criteria.setFilters(Map.of("doesNotExist", new PageCriteria.Filter(MatchMode.EQUALS, new StringNode("BAD"))));

    JsonSerializablePage<JsonSerializable> page = this.cObjectService.data(cClass, criteria);

    Assertions.assertEquals(3L, page.getCount());
    Assertions.assertEquals(3, page.getResultSet().size());
  }

  @Test
  @Request
  public void testDataFilterAttribute()
  {
    ConceptObject object = this.cObjectService.newInstance(cClass);
    object.setCode(TEST_CODE);
    object.setValue(attribute.getCode(), "Test Text");
    this.cObjectService.apply(object);

    try
    {
      PageCriteria criteria = new PageCriteria();
      criteria.setRows(10);
      criteria.setFilters(Map.of(attribute.getCode(), new PageCriteria.Filter(MatchMode.CONTAINS, new StringNode("text"))));

      JsonSerializablePage<JsonSerializable> page = this.cObjectService.data(cClass, criteria);

      Assertions.assertEquals(1L, page.getCount());
      Assertions.assertEquals(List.of(TEST_CODE), getCodes(page));
    }
    finally
    {
      this.cObjectService.delete(object);
    }
  }

  private static PageCriteria codeFilter(MatchMode mode, String value)
  {
    PageCriteria criteria = new PageCriteria();
    criteria.setRows(10);
    criteria.setFilters(Map.of(DefaultAttribute.CODE.getName(), new PageCriteria.Filter(mode, new StringNode(value))));

    return criteria;
  }

  private static List<String> getCodes(JsonSerializablePage<JsonSerializable> page)
  {
    return page.getResultSet().stream() //
        .map(result -> result.toJSON().getAsJsonObject().get(DefaultAttribute.CODE.getName()).getAsString()) //
        .collect(Collectors.toList());
  }

}
