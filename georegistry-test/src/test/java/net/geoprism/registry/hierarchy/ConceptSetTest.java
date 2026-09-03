/**
 *
 */
package net.geoprism.registry.hierarchy;

import java.util.List;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.session.Request;

import net.geoprism.registry.DatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.graph.ConceptClass;
import net.geoprism.registry.graph.ConceptEdgeType;
import net.geoprism.registry.graph.ConceptSet;
import net.geoprism.registry.model.ConceptObject;
import net.geoprism.registry.service.business.ConceptClassBusinessServiceIF;
import net.geoprism.registry.service.business.ConceptEdgeTypeBusinessServiceIF;
import net.geoprism.registry.service.business.ConceptObjectBusinessServiceIF;
import net.geoprism.registry.service.business.ConceptSetBusinessServiceIF;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.view.ConceptClassDTO;
import net.geoprism.registry.view.ConceptEdgeTypeDTO;
import net.geoprism.registry.view.ConceptSetDTO;
import net.geoprism.registry.view.DiscreteType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class ConceptSetTest extends DatasetTest implements InstanceTestClassListener
{
  private static ConceptObject             conceptObject;

  private static ConceptClass              conceptClass;

  private static ConceptEdgeType           conceptEdgeType;

  @Autowired
  private ConceptClassBusinessServiceIF    cClassService;

  @Autowired
  private ConceptEdgeTypeBusinessServiceIF cEdgeService;

  @Autowired
  private ConceptObjectBusinessServiceIF   cObjectService;

  @Autowired
  private ConceptSetBusinessServiceIF      service;

  @Override
  @Request
  public void beforeClassSetup() throws Exception
  {
    FastTestDataset.ORG_CGOV.apply();

    ConceptClassDTO dto = new ConceptClassDTO();
    dto.setCode("TEST_CONCEPT_TYPE");
    dto.setOrganization(FastTestDataset.ORG_CGOV.getCode());
    dto.setDisplayLabel(new LocalizedValue("TEST_CONCEPT_TYPE"));

    conceptClass = this.cClassService.apply(dto);

    conceptEdgeType = this.cEdgeService.create(ConceptEdgeTypeDTO.build(FastTestDataset.ORG_CGOV.getCode(), "TestConceptEdge", conceptClass.getCode(), conceptClass.getCode(), DiscreteType.TAXONOMY));

    conceptObject = this.cObjectService.newInstance(conceptClass);
    conceptObject.setCode("Concept Object 1");
    conceptObject.apply();
  }

  @Override
  @Request
  public void afterClassSetup() throws Exception
  {
    if (conceptObject != null)
    {
      this.cObjectService.delete(conceptObject);
    }

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
    ConceptSetDTO dto = createDTO();

    ConceptSet set = this.service.apply(dto);

    try
    {
      Assert.assertNotNull(set);
      Assert.assertEquals(dto.getCode(), set.getCode());
      Assert.assertEquals(dto.getDisplayLabel().getValue(), set.getLabel().getValue());
      Assert.assertEquals(dto.getDescription().getValue(), set.getDescriptionLV().getValue());
    }
    finally
    {
      this.service.delete(set);
    }

  }

  @Test
  @Request
  public void testToDTO()
  {
    ConceptSetDTO dto = createDTO();

    ConceptSet set = this.service.apply(dto);

    try
    {
      ConceptSetDTO result = this.service.toDTO(set);

      Assert.assertNotNull(result);
      Assert.assertEquals(dto.getCode(), result.getCode());
      Assert.assertEquals(dto.getDisplayLabel().getValue(), result.getDisplayLabel().getValue());
      Assert.assertEquals(dto.getDescription().getValue(), result.getDescription().getValue());
    }
    finally
    {
      this.service.delete(set);
    }

  }

  @Test
  @Request
  public void testUpdate()
  {
    ConceptSetDTO dto = createDTO();

    ConceptSet set = this.service.apply(dto);

    try
    {
      dto = this.service.toDTO(set);
      dto.setDisplayLabel(new LocalizedValue("Updated Label"));
      dto.setDescription(new LocalizedValue("Updated Description"));

      set = this.service.apply(dto);

      Assert.assertNotNull(set);
      Assert.assertEquals(dto.getCode(), set.getCode());
      Assert.assertEquals(dto.getDisplayLabel().getValue(), set.getLabel().getValue());
      Assert.assertEquals(dto.getDescription().getValue(), set.getDescriptionLV().getValue());
    }
    finally
    {
      this.service.delete(set);
    }

  }

  @Test
  @Request
  public void testGetByCode()
  {
    ConceptSetDTO dto = createDTO();

    ConceptSet set = this.service.apply(dto);

    try
    {
      set = this.service.getByCodeOrThrow(dto.getCode());

      Assert.assertNotNull(set);
      Assert.assertEquals(dto.getCode(), set.getCode());
    }
    finally
    {
      this.service.delete(set);
    }
  }

  @Test
  @Request
  public void testGetByAll()
  {
    ConceptSetDTO dto = createDTO();

    ConceptSet set = this.service.apply(dto);

    try
    {
      List<ConceptSet> results = this.service.getAll();

      Assert.assertNotNull(results);
      Assert.assertEquals(1, results.size());
      Assert.assertEquals(dto.getCode(), results.get(0).getCode());
    }
    finally
    {
      this.service.delete(set);
    }
  }

  @Test
  @Request
  public void testAddConceptClass()
  {
    ConceptSetDTO dto = createDTO();

    ConceptSet set = this.service.apply(dto);

    try
    {
      EdgeObject edge = this.service.addConceptClass(set, conceptClass);

      Assert.assertNotNull(edge);
      Assert.assertEquals(1, this.service.getConceptClasses(set).size());
    }
    finally
    {
      this.service.delete(set);
    }
  }

  @Test(expected = UnsupportedOperationException.class)
  @Request
  public void testAddDuplicateConceptClass()
  {
    ConceptSetDTO dto = createDTO();

    ConceptSet set = this.service.apply(dto);

    try
    {
      this.service.addConceptClass(set, conceptClass);
      this.service.addConceptClass(set, conceptClass);
    }
    finally
    {
      this.service.delete(set);
    }
  }

  @Test
  @Request
  public void testAddConceptEdgeType()
  {
    ConceptSetDTO dto = createDTO();

    ConceptSet set = this.service.apply(dto);

    try
    {
      EdgeObject edge = this.service.addConceptEdgeType(set, conceptEdgeType);

      Assert.assertNotNull(edge);
      Assert.assertEquals(1, this.service.getConceptEdgeTypeEdges(set).size());
    }
    finally
    {
      this.service.delete(set);
    }
  }

  @Test(expected = UnsupportedOperationException.class)
  @Request
  public void testAddDuplicateEdgeType()
  {
    ConceptSetDTO dto = createDTO();

    ConceptSet set = this.service.apply(dto);

    try
    {
      this.service.addConceptEdgeType(set, conceptEdgeType);
      this.service.addConceptEdgeType(set, conceptEdgeType);
    }
    finally
    {
      this.service.delete(set);
    }
  }

  @Test
  @Request
  public void testCreateWithClassAndType()
  {
    ConceptSetDTO dto = createDTO();
    dto.getConceptClasses().add(conceptClass.getCode());
    dto.getConceptEdgeTypes().add(conceptEdgeType.getCode());
    dto.setRootTerm(conceptObject.getCode());

    ConceptSet set = this.service.apply(dto);

    try
    {
      Assert.assertNotNull(set);
      Assert.assertNotNull(set.getRootTerm());
      Assert.assertEquals(1, this.service.getConceptEdgeTypeEdges(set).size());
      Assert.assertEquals(1, this.service.getConceptClasses(set).size());
    }
    finally
    {
      this.service.delete(set);
    }

  }

  public ConceptSetDTO createDTO()
  {
    ConceptSetDTO dto = new ConceptSetDTO();
    dto.setCode("TEST");
    dto.setDisplayLabel(new LocalizedValue("Test Label"));
    dto.setDescription(new LocalizedValue("Test Description"));
    dto.setDiscreteType(DiscreteType.TAXONOMY);

    return dto;
  }

}
