/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.hierarchy;

import java.util.List;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.metadata.MdEdge;

import net.geoprism.registry.DirectedAcyclicGraphType;

public class DirectedAcyclicGraphTypeTest
{

  @Test
  @Request
  public void testCreate()
  {
    String code = "TEST";
    LocalizedValue label = new LocalizedValue("Test Label");
    LocalizedValue description = new LocalizedValue("Test Description");

    DirectedAcyclicGraphType type = DirectedAcyclicGraphType.create(code, label, description);

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
    DirectedAcyclicGraphType type = DirectedAcyclicGraphType.create("TEST", new LocalizedValue("Test Label"), new LocalizedValue("Test Description"));

    try
    {
      JsonObject object = new JsonObject();
      object.add(DirectedAcyclicGraphType.DISPLAYLABEL, new LocalizedValue("Updated Label").toJSON());
      object.add(DirectedAcyclicGraphType.DESCRIPTION, new LocalizedValue("Updated Description").toJSON());

      type.update(object);

      Assert.assertEquals("Updated Label", type.getDisplayLabel().getValue());
      Assert.assertEquals("Updated Description", type.getDescription().getValue());
    }
    finally
    {
      type.delete();
    }

  }

  @Test
  @Request
  public void testGetByCode()
  {
    DirectedAcyclicGraphType type = DirectedAcyclicGraphType.create("TEST", new LocalizedValue("Test Label"), new LocalizedValue("Test Description"));

    try
    {
      DirectedAcyclicGraphType result = DirectedAcyclicGraphType.getByCode(type.getCode());

      Assert.assertNotNull(result);
      Assert.assertEquals(type.getCode(), result.getCode());
    }
    finally
    {
      type.delete();
    }

  }

  @Test
  @Request
  public void testGetByMdEdge()
  {
    DirectedAcyclicGraphType type = DirectedAcyclicGraphType.create("TEST", new LocalizedValue("Test Label"), new LocalizedValue("Test Description"));

    try
    {
      DirectedAcyclicGraphType result = DirectedAcyclicGraphType.getByMdEdge(type.getMdEdge());

      Assert.assertNotNull(result);
      Assert.assertEquals(type.getCode(), result.getCode());
    }
    finally
    {
      type.delete();
    }

  }

  @Test
  @Request
  public void testGetByAll()
  {
    DirectedAcyclicGraphType type = DirectedAcyclicGraphType.create("TEST", new LocalizedValue("Test Label"), new LocalizedValue("Test Description"));

    try
    {
      List<DirectedAcyclicGraphType> results = DirectedAcyclicGraphType.getAll();

      Assert.assertEquals(1, results.size());

      DirectedAcyclicGraphType result = results.get(0);

      Assert.assertEquals(type.getCode(), result.getCode());
    }
    finally
    {
      type.delete();
    }

  }

}
