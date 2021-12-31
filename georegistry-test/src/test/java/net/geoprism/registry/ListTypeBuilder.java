/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry;

import java.util.Date;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;

import net.geoprism.registry.test.TestGeoObjectTypeInfo;
import net.geoprism.registry.test.TestHierarchyTypeInfo;

public class ListTypeBuilder
{
  public static class Hierarchy
  {
    private TestHierarchyTypeInfo   type;

    private TestGeoObjectTypeInfo[] parents;

    private TestHierarchyTypeInfo[] subtypeHierarchies;

    public void setType(TestHierarchyTypeInfo type)
    {
      this.type = type;
    }

    public void setParents(TestGeoObjectTypeInfo... parents)
    {
      this.parents = parents;
    }

    public void setSubtypeHierarchies(TestHierarchyTypeInfo... subtypeHierarchies)
    {
      this.subtypeHierarchies = subtypeHierarchies;
    }

  }

  private Organization          org;

  private Hierarchy[]           hts;

  private TestGeoObjectTypeInfo info;

  private String                code;

  public ListTypeBuilder()
  {
    this.code = "TEST_CODE";
  }

  public ListTypeBuilder setHts(Hierarchy... hts)
  {
    this.hts = hts;

    return this;
  }

  public ListTypeBuilder setOrg(Organization org)
  {
    this.org = org;

    return this;
  }

  public ListTypeBuilder setInfo(TestGeoObjectTypeInfo info)
  {
    this.info = info;

    return this;
  }

  public ListTypeBuilder setCode(String code)
  {
    this.code = code;

    return this;
  }

  @Request
  public JsonObject buildJSON()
  {
    JsonArray array = new JsonArray();
    JsonArray hArray = new JsonArray();

    for (Hierarchy ht : hts)
    {
      JsonArray pArray = new JsonArray();
      for (TestGeoObjectTypeInfo parent : ht.parents)
      {
        JsonObject object = new JsonObject();
        object.addProperty("code", parent.getCode());
        object.addProperty("selected", true);

        pArray.add(object);
      }

      JsonObject hierarchy = new JsonObject();
      hierarchy.addProperty("code", ht.type.getCode());
      hierarchy.add("parents", pArray);

      array.add(hierarchy);

      if (ht.subtypeHierarchies != null)
      {
        for (TestHierarchyTypeInfo subtype : ht.subtypeHierarchies)
        {
          JsonObject object = new JsonObject();
          object.addProperty("code", subtype.getCode());
          object.addProperty("selected", true);

          hArray.add(object);

        }
      }
    }

    SingleListType list = new SingleListType();
    list.setValidOn(new Date());
    list.setUniversal(this.info.getUniversal());
    list.getDisplayLabel().setValue("Test List");
    list.setCode(this.code);
    list.setOrganization(this.org);
    list.getDescription().setValue("My Abstract");
    list.setHierarchies(array.toString());

    if (hArray.size() > 0)
    {
      list.setSubtypeHierarchies(hArray.toString());
    }

    list.getListProcess().setValue("Process");
    list.getListProgress().setValue("Progress");
    list.getListAccessConstraints().setValue("Access Contraints");
    list.getListUseConstraints().setValue("User Constraints");
    list.getListAcknowledgements().setValue("Acknowledgements");
    list.getListDisclaimer().setValue("Disclaimer");
    list.setListContactName("Contact Name");
    list.setListTelephoneNumber("Telephone Number");
    list.setListEmail("Email");

    return list.toJSON();
  }

  @Request
  public ListType build()
  {
    JsonObject json = this.buildJSON();

    return ListType.apply(json);
  }
}
