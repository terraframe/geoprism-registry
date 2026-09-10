package net.geoprism.registry;

import java.util.UUID;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import com.runwaysdk.session.Request;

import net.geoprism.registry.graph.ConceptClass;
import net.geoprism.registry.graph.ConceptEdgeType;
import net.geoprism.registry.graph.ConceptSet;
import net.geoprism.registry.model.ConceptObject;
import net.geoprism.registry.service.business.ConceptClassBusinessServiceIF;
import net.geoprism.registry.service.business.ConceptEdgeTypeBusinessServiceIF;
import net.geoprism.registry.service.business.ConceptObjectBusinessServiceIF;
import net.geoprism.registry.service.business.ConceptSetBusinessServiceIF;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestOrganizationInfo;
import net.geoprism.registry.view.ConceptClassDTO;
import net.geoprism.registry.view.ConceptEdgeTypeDTO;
import net.geoprism.registry.view.ConceptSetDTO;
import net.geoprism.registry.view.DiscreteType;

public abstract class ConceptDatasetTest extends DatasetTest
{
  public static final String                 ROOT_CONCEPT       = "Test Term";

  public static final String                 PARENT_CONCEPT     = "Parent Concept";

  public static final String                 CHILD_CONCEPT      = "Child Concept";

  public static final String                 CONCEPT_CLASS_CODE = "TEST_C_CLASS";

  public static final String                 CONCEPT_EDGE_CODE  = "TEST_CONCEPT_EDGE";

  public static final String                 CONCEPT_SET_CODE   = "TEST_CONCEPT_SET";

  @Autowired
  protected ConceptSetBusinessServiceIF      cSetService;

  @Autowired
  protected ConceptEdgeTypeBusinessServiceIF cEdgeTypeService;

  @Autowired
  protected ConceptClassBusinessServiceIF    cClassService;

  @Autowired
  protected ConceptObjectBusinessServiceIF   cService;

  protected static ConceptClass              cClass;

  protected static ConceptEdgeType           cEdgeType;

  protected static ConceptSet                cSet;

  protected static ConceptObject             rootConcept;

  protected static ConceptObject             parentConcept;

  protected static ConceptObject             childConcept;

  protected abstract TestOrganizationInfo getOrganization();

  @Request
  public void beforeClassSetup() throws Exception
  {
    cClass = this.cClassService.apply(this.mockConceptClass());
    cEdgeType = this.cEdgeTypeService.create(this.mockConceptEdge(cClass));

    cSet = this.cSetService.apply(this.mockConceptSet());

    this.cSetService.addConceptClass(cSet, cClass);

    if (!cSet.getDiscreteType().equals(DiscreteType.ENUMERATION.name()))
    {
      this.cSetService.addConceptEdgeType(cSet, cEdgeType);
    }
  }

  @Request
  public void afterClassSetup() throws Exception
  {
    if (cSet != null)
    {
      this.cSetService.delete(cSet);

      cSet = null;
    }

    if (cEdgeType != null)
    {
      this.cEdgeTypeService.delete(cEdgeType);

      cEdgeType = null;
    }

    if (cClass != null)
    {
      this.cClassService.delete(cClass);

      cClass = null;
    }
  }

  @Before
  @Request
  public void setUp() throws Exception
  {
    rootConcept = createConceptObject(ROOT_CONCEPT, "Food");
    parentConcept = createConceptObject(PARENT_CONCEPT, "Flatbread");
    childConcept = createConceptObject(CHILD_CONCEPT, "Pizza");

    addConceptEdge(rootConcept, cEdgeType, parentConcept);
    addConceptEdge(parentConcept, cEdgeType, childConcept);
  }

  @After
  @Request
  public void tearDown() throws Exception
  {
    if (childConcept != null)
    {
      this.cObjectService.delete(childConcept);

      childConcept = null;
    }

    if (parentConcept != null)
    {
      this.cObjectService.delete(parentConcept);

      parentConcept = null;
    }

    if (rootConcept != null)
    {
      this.cObjectService.delete(rootConcept);

      rootConcept = null;
    }
  }

  protected final ConceptObject createConceptObject(String code)
  {
    return this.createConceptObject(code, code);
  }

  protected ConceptObject createConceptObject(String code, String label)
  {
    ConceptObject concept = this.cObjectService.newInstance(cClass);
    concept.setCode(code);
    concept.setValue(DefaultAttribute.DISPLAY_LABEL.getName(), new LocalizedValue(label), TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE);

    this.cObjectService.apply(concept, false);

    return concept;
  }

  protected void addConceptEdge(ConceptObject parent, ConceptEdgeType edge, ConceptObject child)
  {
    this.cObjectService.addChild(parent, edge, child, UUID.randomUUID().toString(), TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, null);
  }

  public ConceptClassDTO mockConceptClass()
  {
    return mockConceptClass(CONCEPT_CLASS_CODE, "Test Concept", "Test Concept");
  }

  public ConceptClassDTO mockConceptClass(String code, String label, String description)
  {
    ConceptClassDTO object = new ConceptClassDTO();
    object.setCode(code);
    object.setDisplayLabel(new LocalizedValue(label));
    object.setOrganization(this.getOrganization().getCode());

    return object;
  }

  public ConceptSetDTO mockConceptSet()
  {
    return mockConceptSet(CONCEPT_SET_CODE, "Test Prog", "Test Description");
  }

  public ConceptSetDTO mockConceptSet(String code, String label, String description)
  {
    ConceptSetDTO object = new ConceptSetDTO();
    object.setCode(code);
    object.setDisplayLabel(new LocalizedValue(label));
    object.setDescription(new LocalizedValue(description));
    object.setDiscreteType(DiscreteType.TAXONOMY);
    object.setRootTerm(ROOT_CONCEPT);

    return object;
  }

  public ConceptEdgeTypeDTO mockConceptEdge(ConceptClass conceptClass)
  {
    return mockConceptEdge(conceptClass, CONCEPT_EDGE_CODE, "Test Prog", "Test Description");
  }

  public ConceptEdgeTypeDTO mockConceptEdge(ConceptClass conceptClass, String code, String label, String description)
  {
    ConceptEdgeTypeDTO object = new ConceptEdgeTypeDTO();
    object.setCode(code);
    object.setLabel(new LocalizedValue(label));
    object.setDescription(new LocalizedValue(description));
    object.setParentType(conceptClass.getCode());
    object.setChildType(conceptClass.getCode());
    object.setOrganizationCode(this.getOrganization().getCode());
    object.setDiscreteType(DiscreteType.TAXONOMY);

    return object;
  }

  public AttributeClassificationType createAttributeClassificationType()
  {
    AttributeClassificationType dto = (AttributeClassificationType) AttributeType.factory("testClassification", new LocalizedValue("testClassificationLocalName"), new LocalizedValue("testClassificationLocalDescrip"), AttributeClassificationType.TYPE, false, false, true);
    dto.setConceptSet(cSet.getCode());
    dto.setStartDate(TestDataSet.DEFAULT_OVER_TIME_DATE);
    dto.setEndDate(TestDataSet.DEFAULT_END_TIME_DATE);

    if (!cSet.getDiscreteType().equals(DiscreteType.ENUMERATION.name()))
    {
      dto.setRootTerm(ROOT_CONCEPT);
    }

    return dto;
  }

  public AttributeClassificationType createDefaultClassificationType()
  {
    AttributeClassificationType dto = (AttributeClassificationType) AttributeType.factory(DefaultAttribute.CLASSIFICATION.getName(), new LocalizedValue("Classification"), new LocalizedValue("Classification"), AttributeClassificationType.TYPE, false, false, true);
    dto.setConceptSet(cSet.getCode());
    dto.setStartDate(TestDataSet.DEFAULT_OVER_TIME_DATE);
    dto.setEndDate(TestDataSet.DEFAULT_END_TIME_DATE);

    if (!cSet.getDiscreteType().equals(DiscreteType.ENUMERATION.name()))
    {
      dto.setRootTerm(ROOT_CONCEPT);
    }

    return dto;
  }

}
