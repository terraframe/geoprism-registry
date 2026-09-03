package net.geoprism.registry;

import java.util.UUID;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.CodeReference;
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

  protected static ConceptObject             childConcept;

  protected abstract TestOrganizationInfo getOrganization();

  @Request
  public void beforeClassSetup() throws Exception
  {
    cClass = this.cClassService.apply(this.mockConceptClass());
    cEdgeType = this.cEdgeTypeService.create(this.mockConceptEdge(cClass));

    cSet = this.cSetService.apply(this.mockConceptSet());

    this.cSetService.addConceptClass(cSet, cClass);
    this.cSetService.addConceptEdgeType(cSet, cEdgeType);

    rootConcept = this.cObjectService.newInstance(cClass);
    rootConcept.setCode("Test Term");
    rootConcept.apply();

    childConcept = this.cObjectService.newInstance(cClass);
    childConcept.setCode("Child Concept");
    childConcept.apply();

    this.cObjectService.addChild(rootConcept, cEdgeType, childConcept, UUID.randomUUID().toString(), TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, null);
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

  public ConceptClassDTO mockConceptClass()
  {
    return mockConceptClass("TEST_C_CLASS", "Test Concept", "Test Concept");
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
    return mockConceptSet("TEST_CONCEPT_SET", "Test Prog", "Test Description");
  }

  public ConceptSetDTO mockConceptSet(String code, String label, String description)
  {
    ConceptSetDTO object = new ConceptSetDTO();
    object.setCode(code);
    object.setDisplayLabel(new LocalizedValue(label));
    object.setDescription(new LocalizedValue(description));
    object.setDiscreteType(DiscreteType.TAXONOMY);

    return object;
  }

  public ConceptEdgeTypeDTO mockConceptEdge(ConceptClass conceptClass)
  {
    return mockConceptEdge(conceptClass, "TEST_CONCEPT_EDGE", "Test Prog", "Test Description");
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
    dto.setRootTerm(CodeReference.build(rootConcept.getCode(), rootConcept.getType().getCode()));
    dto.setStartDate(TestDataSet.DEFAULT_OVER_TIME_DATE);
    dto.setEndDate(TestDataSet.DEFAULT_END_TIME_DATE);

    return dto;
  }

}
