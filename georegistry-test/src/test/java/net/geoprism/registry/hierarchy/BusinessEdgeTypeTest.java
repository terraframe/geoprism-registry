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
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.metadata.MdEdge;

import net.geoprism.registry.BusinessEdgeType;
import net.geoprism.registry.BusinessType;
import net.geoprism.registry.test.FastTestDataset;

public class BusinessEdgeTypeTest
{
  private static FastTestDataset testData;

  private static BusinessType    parentType;

  private static BusinessType    childType;

  @BeforeClass
  public static void setUpClass()
  {
    testData = FastTestDataset.newTestData();
    testData.setUpMetadata();

    setUpClassInRequest();
  }

  @AfterClass
  public static void cleanUpClass()
  {
    cleanUpClassInRequest();
    
    if (testData != null)
    {
      testData.tearDownMetadata();
    }
  }

  @Request
  public static void setUpClassInRequest()
  {
    String orgCode = FastTestDataset.ORG_CGOV.getCode();

    JsonObject parentObject = new JsonObject();
    parentObject.addProperty(BusinessType.CODE, "TEST_PARENT");
    parentObject.addProperty(BusinessType.ORGANIZATION, orgCode);
    parentObject.add(BusinessType.DISPLAYLABEL, new LocalizedValue("TEST_PARENT").toJSON());

    JsonObject childObject = new JsonObject();
    childObject.addProperty(BusinessType.CODE, "TEST_CHILD");
    childObject.addProperty(BusinessType.ORGANIZATION, orgCode);
    childObject.add(BusinessType.DISPLAYLABEL, new LocalizedValue("TEST_CHILD").toJSON());

    parentType = BusinessType.apply(parentObject);
    childType = BusinessType.apply(childObject);
  }

  @Request
  public static void cleanUpClassInRequest()
  {
    if (parentType != null)
    {
      parentType.delete();
    }
    
    if (childType != null)
    {
      childType.delete();
    }
  }

  @Test
  @Request
  public void testCreate()
  {
    String code = "TEST";
    LocalizedValue label = new LocalizedValue("Test Label");
    LocalizedValue description = new LocalizedValue("Test Description");

    BusinessEdgeType type = BusinessEdgeType.create(FastTestDataset.ORG_CGOV.getCode(), code, label, description, parentType.getCode(), childType.getCode());

    try
    {
      Assert.assertNotNull(type);
      Assert.assertEquals(code, type.getCode());
      Assert.assertEquals(label.getValue(), type.getDisplayLabel().getValue());
      Assert.assertEquals(description.getValue(), type.getDescription().getValue());

      MdEdge mdEdge = type.getMdEdge();

      Assert.assertNotNull(mdEdge);
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
    BusinessEdgeType type = createTestRelationship();

    try
    {
      JsonObject object = new JsonObject();
      object.add(BusinessEdgeType.DISPLAYLABEL, new LocalizedValue("Updated Label").toJSON());
      object.add(BusinessEdgeType.DESCRIPTION, new LocalizedValue("Updated Description").toJSON());

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
    BusinessEdgeType type = createTestRelationship();

    try
    {
      BusinessEdgeType result = BusinessEdgeType.getByCode(type.getCode());

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
    BusinessEdgeType type = createTestRelationship();

    try
    {
      BusinessEdgeType result = BusinessEdgeType.getByMdEdge(type.getMdEdge());

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
    BusinessEdgeType type = createTestRelationship();

    try
    {
      List<BusinessEdgeType> results = BusinessEdgeType.getAll();

      Assert.assertEquals(1, results.size());

      BusinessEdgeType result = results.get(0);

      Assert.assertEquals(type.getCode(), result.getCode());
    }
    finally
    {
      type.delete();
    }

  }

  public BusinessEdgeType createTestRelationship()
  {
    return BusinessEdgeType.create(FastTestDataset.ORG_CGOV.getCode(), "TEST", new LocalizedValue("Test Label"), new LocalizedValue("Test Description"), parentType.getCode(), childType.getCode());
  }

}
