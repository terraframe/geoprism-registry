/**
 *
 */
package net.geoprism.registry.hierarchy;

import java.util.List;
import java.util.Optional;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.GraphTypeDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.metadata.MdEdge;

import net.geoprism.registry.InstanceTestClassExtension;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.graph.UndirectedGraphType;
import net.geoprism.registry.service.business.UndirectedGraphTypeBusinessServiceIF;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@ExtendWith(InstanceTestClassExtension.class)
public class UndirectedGraphTypeTest
{
  @Autowired
  private UndirectedGraphTypeBusinessServiceIF service;

  @Test
  @Request
  public void testCreate()
  {
    String code = "TEST";
    LocalizedValue label = new LocalizedValue("Test Label");
    LocalizedValue description = new LocalizedValue("Test Description");

    UndirectedGraphType type = this.service.create(code, label, description, 0L);

    try
    {
      Assertions.assertNotNull(type);
      Assertions.assertEquals(code, type.getCode());
      Assertions.assertEquals(label.getValue(), type.getLabel().getValue());
      Assertions.assertEquals(description.getValue(), type.getDescriptionLV().getValue());

      MdEdge mdEdge = type.getMdEdge();

      Assertions.assertNotNull(mdEdge);

      MdEdgeDAO mdEdgeDao = (MdEdgeDAO) BusinessFacade.getEntityDAO(mdEdge);

      Assertions.assertNotNull(mdEdgeDao.definesAttribute("startDate"));
      Assertions.assertNotNull(mdEdgeDao.definesAttribute("endDate"));
    }
    finally
    {
      this.service.delete(type);
    }

  }

  @Test
  @Request
  public void testUpdate()
  {
    UndirectedGraphType type = this.service.create("TEST", new LocalizedValue("Test Label"), new LocalizedValue("Test Description"), 0L);

    try
    {
      GraphTypeDTO object = new GraphTypeDTO();
      object.setLabel(new LocalizedValue("Updated Label"));
      object.setDescription(new LocalizedValue("Updated Description"));

      this.service.update(type, object);

      Assertions.assertEquals("Updated Label", type.getLabel().getValue());
      Assertions.assertEquals("Updated Description", type.getDescriptionLV().getValue());
    }
    finally
    {
      this.service.delete(type);
    }

  }

  @Test
  @Request
  public void testGetByCode()
  {
    UndirectedGraphType type = this.service.create("TEST", new LocalizedValue("Test Label"), new LocalizedValue("Test Description"), 0L);

    try
    {
      UndirectedGraphType result = this.service.getByCode(type.getCode()).get();

      Assertions.assertNotNull(result);
      Assertions.assertEquals(type.getCode(), result.getCode());
    }
    finally
    {
      this.service.delete(type);
    }

  }

  @Test
  @Request
  public void testGetByMdEdge()
  {
    UndirectedGraphType type = this.service.create("TEST", new LocalizedValue("Test Label"), new LocalizedValue("Test Description"), 0L);

    try
    {
      Optional<UndirectedGraphType> optional = this.service.getByMdEdge(type.getMdEdge());

      Assertions.assertTrue(optional.isPresent());
      Assertions.assertEquals(type.getCode(), optional.get().getCode());
    }
    finally
    {
      this.service.delete(type);
    }

  }

  @Test
  @Request
  public void testGetByAll()
  {
    UndirectedGraphType type = this.service.create("TEST", new LocalizedValue("Test Label"), new LocalizedValue("Test Description"), 0L);

    try
    {
      List<UndirectedGraphType> results = this.service.getAll();

      Assertions.assertEquals(1, results.size());

      UndirectedGraphType result = results.get(0);

      Assertions.assertEquals(type.getCode(), result.getCode());
    }
    finally
    {
      this.service.delete(type);
    }

  }

}
