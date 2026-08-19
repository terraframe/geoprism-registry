/**
 *
 */
package net.geoprism.registry.hierarchy;

import java.util.List;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.session.Request;

import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.graph.ConceptClass;
import net.geoprism.registry.graph.ConceptEdgeType;
import net.geoprism.registry.graph.ConceptSet;
import net.geoprism.registry.service.business.ConceptClassBusinessServiceIF;
import net.geoprism.registry.service.business.ConceptEdgeTypeBusinessServiceIF;
import net.geoprism.registry.service.business.ConceptSetBusinessServiceIF;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.view.ConceptClassDTO;
import net.geoprism.registry.view.ConceptEdgeTypeDTO;
import net.geoprism.registry.view.ConceptSetDTO;

@SuppressWarnings("unchecked")
public abstract class ConceptSetTest<T extends ConceptSet, D extends ConceptSetDTO> extends FastDatasetTest implements InstanceTestClassListener
{
  private static ConceptClass              conceptClass;

  private static ConceptEdgeType           conceptEdgeType;

  @Autowired
  private ConceptClassBusinessServiceIF    cClassService;

  @Autowired
  private ConceptEdgeTypeBusinessServiceIF cEdgeService;

  protected abstract ConceptSetBusinessServiceIF<T, D> getService();

  @Override
  public void beforeClassSetup() throws Exception
  {
    super.beforeClassSetup();

    setUpClassInRequest();
  }

  @Request
  public void setUpClassInRequest()
  {
    String orgCode = FastTestDataset.ORG_CGOV.getCode();

    ConceptClassDTO dto = new ConceptClassDTO();
    dto.setCode("TEST_CONCEPT_TYPE");
    dto.setOrganization(orgCode);
    dto.setDisplayLabel(new LocalizedValue("TEST_CONCEPT_TYPE"));

    conceptClass = this.cClassService.apply(dto);

    conceptEdgeType = this.cEdgeService.create(ConceptEdgeTypeDTO.build(FastTestDataset.ORG_CGOV.getCode(), "TestConceptEdge", conceptClass.getCode(), conceptClass.getCode()));

  }

  @Override
  public void afterClassSetup() throws Exception
  {
    cleanUpClassInRequest();

    super.afterClassSetup();
  }

  @Request
  public void cleanUpClassInRequest()
  {
    if (conceptEdgeType != null)
    {
      this.cEdgeService.delete(conceptEdgeType);
    }

    if (conceptClass != null)
    {
      this.cClassService.delete(conceptClass);
    }
  }

  @Test
  @Request
  public void testCreate()
  {
    D dto = createDTO();

    T set = this.getService().apply(dto);

    try
    {
      Assert.assertNotNull(set);
      Assert.assertEquals(dto.getCode(), set.getCode());
      Assert.assertEquals(dto.getDisplayLabel().getValue(), set.getLabel().getValue());
      Assert.assertEquals(dto.getDescription().getValue(), set.getDescriptionLV().getValue());
    }
    finally
    {
      this.getService().delete(set);
    }

  }

  @Test
  @Request
  public void testUpdate()
  {
    D dto = createDTO();

    T set = this.getService().apply(dto);

    try
    {
      dto = this.getService().toDTO(set);
      dto.setDisplayLabel(new LocalizedValue("Updated Label"));
      dto.setDescription(new LocalizedValue("Updated Description"));

      set = this.getService().apply(dto);

      Assert.assertNotNull(set);
      Assert.assertEquals(dto.getCode(), set.getCode());
      Assert.assertEquals(dto.getDisplayLabel().getValue(), set.getLabel().getValue());
      Assert.assertEquals(dto.getDescription().getValue(), set.getDescriptionLV().getValue());
    }
    finally
    {
      this.getService().delete(set);
    }

  }

  @Test
  @Request
  public void testGetByCode()
  {
    D dto = createDTO();

    T set = this.getService().apply(dto);

    try
    {
      set = this.getService().getByCodeOrThrow(dto.getCode());

      Assert.assertNotNull(set);
      Assert.assertEquals(dto.getCode(), set.getCode());
    }
    finally
    {
      this.getService().delete(set);
    }
  }

  @Test
  @Request
  public void testGetByAll()
  {
    D dto = createDTO();

    T set = this.getService().apply(dto);

    try
    {
      List<T> results = this.getService().getAll();

      Assert.assertNotNull(results);
      Assert.assertEquals(1, results.size());
      Assert.assertEquals(dto.getCode(), results.get(0).getCode());
    }
    finally
    {
      this.getService().delete(set);
    }
  }

  @Test
  @Request
  public void testAddConceptClass()
  {
    D dto = createDTO();

    T set = this.getService().apply(dto);

    try
    {
      EdgeObject edge = this.getService().addConceptClass(set, conceptClass);

      Assert.assertNotNull(edge);
      Assert.assertEquals(1, this.getService().getConceptClasses(set).size());
    }
    finally
    {
      this.getService().delete(set);
    }
  }

  @Test(expected = UnsupportedOperationException.class)
  @Request
  public void testAddDuplicateConceptClass()
  {
    D dto = createDTO();

    T set = this.getService().apply(dto);

    try
    {
      this.getService().addConceptClass(set, conceptClass);
      this.getService().addConceptClass(set, conceptClass);
    }
    finally
    {
      this.getService().delete(set);
    }
  }

  @Test
  @Request
  public void testAddConceptEdgeType()
  {
    D dto = createDTO();

    T set = this.getService().apply(dto);

    try
    {
      EdgeObject edge = this.getService().addConceptEdgeType(set, conceptEdgeType);

      Assert.assertNotNull(edge);
      Assert.assertEquals(1, this.getService().getConceptEdgeTypeEdges(set).size());
    }
    finally
    {
      this.getService().delete(set);
    }
  }

  @Test(expected = UnsupportedOperationException.class)
  @Request
  public void testAddDuplicateEdgeType()
  {
    D dto = createDTO();

    T set = this.getService().apply(dto);

    try
    {
      this.getService().addConceptEdgeType(set, conceptEdgeType);
      this.getService().addConceptEdgeType(set, conceptEdgeType);
    }
    finally
    {
      this.getService().delete(set);
    }
  }

  public D createDTO()
  {
    ConceptSetDTO dto = new ConceptSetDTO();
    dto.setCode("TEST");
    dto.setDisplayLabel(new LocalizedValue("Test Label"));
    dto.setDescription(new LocalizedValue("Test Description"));
    return (D) dto;
  }

}
