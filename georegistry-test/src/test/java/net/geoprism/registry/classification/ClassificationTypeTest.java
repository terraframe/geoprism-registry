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
package net.geoprism.registry.classification;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.runwaysdk.constants.graph.MdClassificationInfo;
import com.runwaysdk.dataaccess.cache.DataNotFoundException;
import com.runwaysdk.session.Request;

import net.geoprism.registry.model.ClassificationType;
import net.geoprism.registry.view.Page;

public class ClassificationTypeTest
{
  @Test
  @Request
  public void testCreate()
  {
    String code = "TEST_PROG";
    String label = "Test Prog";
    String description = "Test Description";

    ClassificationType type = ClassificationType.apply(createMock(code, label, description));

    try
    {
      Assert.assertEquals(code, type.getCode());
      Assert.assertEquals(label, type.getDisplayLabel().getValue());
      Assert.assertNotNull(type.getMdVertex());
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
    ClassificationType type = ClassificationType.apply(createMock());

    try
    {
      Assert.assertNotNull(ClassificationType.getByCode(type.getCode()));
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
    ClassificationType type = ClassificationType.apply(createMock());

    try
    {
      String label = "Updated";

      JsonObject json = type.toJSON();
      json.add(MdClassificationInfo.DISPLAY_LABEL, new LocalizedValue(label).toJSON());

      type = ClassificationType.apply(json);

      Assert.assertEquals(label, type.getDisplayLabel().getValue());
    }
    finally
    {
      type.delete();
    }
  }

  @Test
  @Request
  public void testRemove()
  {
    ClassificationType type = ClassificationType.apply(createMock());
    type.delete();

    try
    {
      ClassificationType.getByCode(type.getCode());

      Assert.fail("Able to get type which should have been deleted");
    }
    catch (DataNotFoundException ex)
    {
      // Expected
    }

  }

  @Test
  @Request
  public void testToJson()
  {
    ClassificationType classificationType = ClassificationType.apply(createMock());

    try
    {
      JsonObject json = classificationType.toJSON();

      Assert.assertEquals(classificationType.getCode(), json.get(DefaultAttribute.CODE.getName()).getAsString());
      Assert.assertEquals(classificationType.getDisplayLabel().toJSON(), json.get(MdClassificationInfo.DISPLAY_LABEL).getAsJsonObject());
    }
    finally
    {
      classificationType.delete();
    }

  }

  @Test
  @Request
  public void testPage()
  {
    ClassificationType classificationType = ClassificationType.apply(createMock());

    try
    {
      Page<ClassificationType> page = ClassificationType.page(new JsonObject());

      Assert.assertEquals(new Long(1), page.getCount());

      ClassificationType result = page.getResults().get(0);

      Assert.assertEquals(classificationType.getOid(), result.getOid());
    }
    finally
    {
      classificationType.delete();
    }

  }

  public static JsonObject createMock()
  {
    return createMock("TEST_PROG", "Test Prog", "Test Description");
  }

  public static JsonObject createMock(String code, String label, String description)
  {
    JsonObject object = new JsonObject();
    object.addProperty(DefaultAttribute.CODE.getName(), code);
    object.add(MdClassificationInfo.DISPLAY_LABEL, new LocalizedValue(label).toJSON());
    object.add(MdClassificationInfo.DESCRIPTION, new LocalizedValue(description).toJSON());
    return object;
  }

}
