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
import org.springframework.test.context.ContextConfiguration;

import com.google.gson.JsonObject;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.metadata.MdEdge;

import net.geoprism.registry.DirectedAcyclicGraphType;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.business.DirectedAcyclicGraphTypeBusinessServiceIF;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class DirectedAcyclicGraphTypeTest
{
  @Autowired
  private DirectedAcyclicGraphTypeBusinessServiceIF service;

  @Test
  @Request
  public void testCreate()
  {
    String code = "TEST";
    LocalizedValue label = new LocalizedValue("Test Label");
    LocalizedValue description = new LocalizedValue("Test Description");

    DirectedAcyclicGraphType type = this.service.create(code, label, description);

    try
    {
      Assert.assertNotNull(type);
      Assert.assertEquals(code, type.getCode());
      Assert.assertEquals(label.getValue(), type.getDisplayLabel().getValue());
      Assert.assertEquals(description.getValue(), type.getDescription().getValue());

      MdEdge mdEdge = type.getMdEdge();

      Assert.assertNotNull(mdEdge);

      MdEdgeDAO mdEdgeDao = (MdEdgeDAO) BusinessFacade.getEntityDAO(mdEdge);

      Assert.assertNotNull(mdEdgeDao.definesAttribute("startDate"));
      Assert.assertNotNull(mdEdgeDao.definesAttribute("endDate"));
    }
    finally
    {
      type.delete();
    }

  }

  @Test
  @Request
  public void testUpdate()
  {
    DirectedAcyclicGraphType type = this.service.create("TEST", new LocalizedValue("Test Label"), new LocalizedValue("Test Description"));

    try
    {
      JsonObject object = new JsonObject();
      object.add(DirectedAcyclicGraphType.DISPLAYLABEL, new LocalizedValue("Updated Label").toJSON());
      object.add(DirectedAcyclicGraphType.DESCRIPTION, new LocalizedValue("Updated Description").toJSON());

      this.service.update(type, object);

      Assert.assertEquals("Updated Label", type.getDisplayLabel().getValue());
      Assert.assertEquals("Updated Description", type.getDescription().getValue());
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
    DirectedAcyclicGraphType type = this.service.create("TEST", new LocalizedValue("Test Label"), new LocalizedValue("Test Description"));

    try
    {
      DirectedAcyclicGraphType result = DirectedAcyclicGraphType.getByCode(type.getCode());

      Assert.assertNotNull(result);
      Assert.assertEquals(type.getCode(), result.getCode());
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
    DirectedAcyclicGraphType type = this.service.create("TEST", new LocalizedValue("Test Label"), new LocalizedValue("Test Description"));

    try
    {
      DirectedAcyclicGraphType result = DirectedAcyclicGraphType.getByMdEdge(type.getMdEdge());

      Assert.assertNotNull(result);
      Assert.assertEquals(type.getCode(), result.getCode());
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
    DirectedAcyclicGraphType type = this.service.create("TEST", new LocalizedValue("Test Label"), new LocalizedValue("Test Description"));

    try
    {
      List<DirectedAcyclicGraphType> results = DirectedAcyclicGraphType.getAll();

      Assert.assertEquals(1, results.size());

      DirectedAcyclicGraphType result = results.get(0);

      Assert.assertEquals(type.getCode(), result.getCode());
    }
    finally
    {
      this.service.delete(type);
    }

  }

}
