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
package net.geoprism.registry.service;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.runwaysdk.constants.graph.MdClassificationInfo;
import com.runwaysdk.session.Request;

import net.geoprism.registry.model.ClassificationType;
import net.geoprism.registry.view.Page;

public class ClassificationTypeTest
{

  @Test
  @Request
  public void testApply()
  {
    String code = "TestClassification";
    String displayLabel = "Test Classification";

    ClassificationType classificationType = create(code, displayLabel);

    try
    {
      Assert.assertEquals(code, classificationType.getCode());
      Assert.assertEquals(displayLabel, classificationType.getDisplayLabel().getValue());
    }
    finally
    {
      classificationType.delete();
    }

  }

  @Test
  @Request
  public void testToJson()
  {
    ClassificationType classificationType = create("TestClassification", "Test Classification");

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
    ClassificationType classificationType = create("TestClassification", "Test Classification");

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

  private ClassificationType create(String code, String displayLabel)
  {
    JsonObject object = new JsonObject();
    object.addProperty(DefaultAttribute.CODE.getName(), code);
    object.add(MdClassificationInfo.DISPLAY_LABEL, new LocalizedValue(displayLabel).toJSON());

    ClassificationType classificationType = ClassificationType.apply(object);
    return classificationType;
  }
}
