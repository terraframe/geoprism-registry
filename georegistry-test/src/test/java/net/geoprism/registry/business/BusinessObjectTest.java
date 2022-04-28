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
package net.geoprism.registry.business;

import java.util.List;

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
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
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
    testData.setUpInstanceData();

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
      testData.tearDownInstanceData();
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

  @Test
  @Request
  public void testAddGetGeoObjects()
  {
    ServerGeoObjectIF serverObject = FastTestDataset.CENTRAL_HOSPITAL.getServerObject();

    BusinessObject object = BusinessObject.newInstance(type);
    object.setValue(attribute.getName(), "Test Text");
    object.setCode(TEST_CODE);
    object.apply();

    try
    {
      object.addGeoObject(serverObject);

      List<VertexServerGeoObject> results = object.getGeoObjects();

      Assert.assertEquals(1, results.size());

      VertexServerGeoObject result = results.get(0);

      Assert.assertEquals(serverObject.getCode(), result.getCode());
    }
    finally
    {
      object.delete();
    }
  }

  @Test
  @Request
  public void testRemoveGeoObjects()
  {
    ServerGeoObjectIF serverObject = FastTestDataset.CENTRAL_HOSPITAL.getServerObject();

    BusinessObject object = BusinessObject.newInstance(type);
    object.setValue(attribute.getName(), "Test Text");
    object.setCode(TEST_CODE);
    object.apply();

    try
    {
      object.addGeoObject(serverObject);
      object.removeGeoObject(serverObject);

      Assert.assertEquals(0, object.getGeoObjects().size());
    }
    finally
    {
      object.delete();
    }
  }

  @Test
  @Request
  public void testDuplicateGeoObjects()
  {
    ServerGeoObjectIF serverObject = FastTestDataset.CENTRAL_HOSPITAL.getServerObject();

    BusinessObject object = BusinessObject.newInstance(type);
    object.setValue(attribute.getName(), "Test Text");
    object.setCode(TEST_CODE);
    object.apply();

    try
    {
      object.addGeoObject(serverObject);
      object.addGeoObject(serverObject);
      object.addGeoObject(serverObject);
      object.addGeoObject(serverObject);

      List<VertexServerGeoObject> results = object.getGeoObjects();

      Assert.assertEquals(1, results.size());

      VertexServerGeoObject result = results.get(0);

      Assert.assertEquals(serverObject.getCode(), result.getCode());
    }
    finally
    {
      object.delete();
    }
  }

  @Test
  @Request
  public void testGetBusinessObjects()
  {
    ServerGeoObjectIF serverObject = FastTestDataset.CENTRAL_HOSPITAL.getServerObject();
    
    BusinessObject object = BusinessObject.newInstance(type);
    object.setValue(attribute.getName(), "Test Text");
    object.setCode(TEST_CODE);
    object.apply();
    
    try
    {
      object.addGeoObject(serverObject);
      
      List<BusinessObject> results = serverObject.getBusinessObjects(type);
      
      
      Assert.assertEquals(1, results.size());
      
      BusinessObject result = results.get(0);
      
      Assert.assertEquals(object.getCode(), result.getCode());
    }
    finally
    {
      object.delete();
    }
  }
  
  @Test
  @Request
  public void testGetBusinessObjects_NoType()
  {
    ServerGeoObjectIF serverObject = FastTestDataset.CENTRAL_HOSPITAL.getServerObject();
    
    BusinessObject object = BusinessObject.newInstance(type);
    object.setValue(attribute.getName(), "Test Text");
    object.setCode(TEST_CODE);
    object.apply();
    
    try
    {
      object.addGeoObject(serverObject);
      
      List<BusinessObject> results = serverObject.getBusinessObjects();
      
      
      Assert.assertEquals(1, results.size());
      
      BusinessObject result = results.get(0);
      
      Assert.assertEquals(object.getCode(), result.getCode());
    }
    finally
    {
      object.delete();
    }
  }
  
}
