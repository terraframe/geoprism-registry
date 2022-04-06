/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.business;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;

import net.geoprism.registry.BusinessType;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.test.FastTestDataset;

public class BusinessObjectTest
{
  private static String          TEST_CODE = "TEST_OBJ";

  private static FastTestDataset testData;

  private static BusinessType    type;

  private static AttributeType   attribute;

  @BeforeClass
  public static void setUpClass()
  {
    testData = FastTestDataset.newTestData();
    testData.setUpMetadata();

    setUpClassInRequest();
  }

  @Request
  private static void setUpClassInRequest()
  {
    String code = "TEST_PROG";
    String orgCode = FastTestDataset.ORG_CGOV.getCode();
    String label = "Test Prog";

    JsonObject object = new JsonObject();
    object.addProperty(BusinessType.CODE, code);
    object.addProperty(BusinessType.ORGANIZATION, orgCode);
    object.add(BusinessType.DISPLAYLABEL, new LocalizedValue(label).toJSON());

    type = BusinessType.apply(object);

    attribute = type.createAttributeType(new AttributeCharacterType("testCharacter", new LocalizedValue("Test Character"), new LocalizedValue("Test True"), false, false, false));
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
  private static void cleanUpClassInRequest()
  {
    if (type != null)
    {
      type.delete();
    }
  }

  @Test
  @Request
  public void testBasicCreate()
  {
    BusinessObject object = BusinessObject.newInstance(type);
    object.setCode(TEST_CODE);
    object.apply();

    try
    {
      Assert.assertNotNull(object.getVertex().getRID());
    }
    finally
    {
      object.delete();
    }
  }

  @Test
  @Request
  public void testSetGetValue()
  {
    BusinessObject object = BusinessObject.newInstance(type);
    object.setValue(attribute.getName(), "Test Text");
    object.setCode(TEST_CODE);
    object.apply();

    try
    {
      Assert.assertEquals("Test Text", object.getObjectValue(attribute.getName()));
    }
    finally
    {
      object.delete();
    }

  }

  @Test
  @Request
  public void testGet()
  {
    BusinessObject object = BusinessObject.newInstance(type);
    object.setValue(attribute.getName(), "Test Text");
    object.setCode(TEST_CODE);
    object.apply();

    try
    {
      BusinessObject result = BusinessObject.get(type, attribute.getName(), object.getObjectValue(attribute.getName()));

      Assert.assertEquals((String) object.getObjectValue("oid"), (String) result.getObjectValue("oid"));
    }
    finally
    {
      object.delete();
    }
  }

  @Test
  @Request
  public void testGetByCode()
  {
    BusinessObject object = BusinessObject.newInstance(type);
    object.setValue(attribute.getName(), "Test Text");
    object.setCode(TEST_CODE);
    object.apply();

    try
    {
      BusinessObject result = BusinessObject.getByCode(type, object.getCode());

      Assert.assertEquals((String) object.getObjectValue("oid"), (String) result.getObjectValue("oid"));
    }
    finally
    {
      object.delete();
    }
  }
}
