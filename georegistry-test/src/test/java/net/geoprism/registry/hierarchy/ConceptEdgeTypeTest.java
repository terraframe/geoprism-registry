/**
 *
 */
package net.geoprism.registry.hierarchy;

import java.util.List;
import java.util.Optional;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.session.Request;
import com.runwaysdk.system.metadata.MdEdge;

import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.graph.ConceptClass;
import net.geoprism.registry.graph.ConceptEdgeType;
import net.geoprism.registry.service.business.ConceptClassBusinessServiceIF;
import net.geoprism.registry.service.business.ConceptEdgeTypeBusinessServiceIF;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.view.ConceptClassDTO;
import net.geoprism.registry.view.ConceptEdgeTypeDTO;
import net.geoprism.registry.view.DiscreteType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class ConceptEdgeTypeTest extends FastDatasetTest implements InstanceTestClassListener
{
  private static ConceptClass              conceptClass;

  @Autowired
  private ConceptClassBusinessServiceIF    cClassService;

  @Autowired
  private ConceptEdgeTypeBusinessServiceIF cEdgeService;

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
    if (conceptClass != null)
    {
      this.cClassService.delete(conceptClass);
    }
  }

  @Test
  @Request
  public void testCreate()
  {
    String code = "TEST";
    LocalizedValue label = new LocalizedValue("Test Label");
    LocalizedValue description = new LocalizedValue("Test Description");

    ConceptEdgeType type = this.cEdgeService.create(ConceptEdgeTypeDTO.build(FastTestDataset.ORG_CGOV.getCode(), code, label, description, conceptClass.getCode(), conceptClass.getCode(), DiscreteType.TAXONOMY));

    try
    {
      Assert.assertNotNull(type);
      Assert.assertEquals(code, type.getCode());
      Assert.assertEquals(label.getValue(), type.getLabel().getValue());
      Assert.assertEquals(description.getValue(), type.getDescriptionLV().getValue());

      MdEdge mdEdge = type.getMdEdge();

      Assert.assertNotNull(mdEdge);
    }
    finally
    {
      this.cEdgeService.delete(type);
    }

  }

  @Test
  @Request
  public void testUpdate()
  {
    ConceptEdgeType type = createTestRelationship();

    try
    {
      ConceptEdgeTypeDTO view = new ConceptEdgeTypeDTO();
      view.setLabel(new LocalizedValue("Updated Label"));
      view.setDescription(new LocalizedValue("Updated Description"));

      this.cEdgeService.update(type, view);

      Assert.assertEquals("Updated Label", type.getLabel().getValue());
      Assert.assertEquals("Updated Description", type.getDescriptionLV().getValue());
    }
    finally
    {
      this.cEdgeService.delete(type);
    }

  }

  @Test
  @Request
  public void testGetByCode()
  {
    ConceptEdgeType type = createTestRelationship();

    try
    {
      ConceptEdgeType result = this.cEdgeService.getByCodeOrThrow(type.getCode());

      Assert.assertNotNull(result);
      Assert.assertEquals(type.getCode(), result.getCode());
    }
    finally
    {
      this.cEdgeService.delete(type);
    }

  }

  @Test
  @Request
  public void testGetByMdEdge()
  {
    ConceptEdgeType type = createTestRelationship();

    try
    {
      Optional<ConceptEdgeType> result = this.cEdgeService.getByMdEdge(type.getMdEdge());

      Assert.assertTrue(result.isPresent());
      Assert.assertEquals(type.getCode(), result.get().getCode());
    }
    finally
    {
      this.cEdgeService.delete(type);
    }

  }

  @Test
  @Request
  public void testGetByAll()
  {
    ConceptEdgeType type = createTestRelationship();

    try
    {
      List<ConceptEdgeType> results = this.cEdgeService.getAll();

      Assert.assertTrue(results.size() > 0);
    }
    finally
    {
      this.cEdgeService.delete(type);
    }

  }

  public ConceptEdgeType createTestRelationship()
  {
    return this.cEdgeService.create(ConceptEdgeTypeDTO.build(FastTestDataset.ORG_CGOV.getCode(), "TEST", new LocalizedValue("Test Label"), new LocalizedValue("Test Description"), conceptClass.getCode(), conceptClass.getCode(), DiscreteType.TAXONOMY));
  }

}
