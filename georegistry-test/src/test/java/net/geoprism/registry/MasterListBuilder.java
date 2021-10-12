/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry;

import java.util.Date;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;

import net.geoprism.registry.test.TestGeoObjectTypeInfo;
import net.geoprism.registry.test.TestHierarchyTypeInfo;

public class MasterListBuilder
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

  private String                visibility;

  private boolean               isMaster;

  private String                code;

  public MasterListBuilder()
  {
    this.code = "TEST_CODE";
  }

  public MasterListBuilder setHts(Hierarchy... hts)
  {
    this.hts = hts;

    return this;
  }

  public MasterListBuilder setOrg(Organization org)
  {
    this.org = org;

    return this;
  }

  public MasterListBuilder setInfo(TestGeoObjectTypeInfo info)
  {
    this.info = info;

    return this;
  }

  public MasterListBuilder setVisibility(String visibility)
  {
    this.visibility = visibility;

    return this;
  }

  public MasterListBuilder setMaster(boolean isMaster)
  {
    this.isMaster = isMaster;

    return this;
  }

  public MasterListBuilder setCode(String code)
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

    MasterList list = new MasterList();
    list.setUniversal(this.info.getUniversal());
    list.getDisplayLabel().setValue("Test List");
    list.setCode(this.code);
    list.setRepresentativityDate(new Date());
    list.setPublishDate(new Date());
    list.setListAbstract("My Abstract");
    list.setProcess("Process");
    list.setProgress("Progress");
    list.setAccessConstraints("Access Contraints");
    list.setUseConstraints("User Constraints");
    list.setAcknowledgements("Acknowledgements");
    list.setDisclaimer("Disclaimer");
    list.setContactName("Contact Name");
    list.setOrganization(this.org);
    list.setTelephoneNumber("Telephone Number");
    list.setEmail("Email");
    list.setHierarchies(array.toString());
    list.addFrequency(ChangeFrequency.ANNUAL);
    list.setIsMaster(this.isMaster);
    list.setVisibility(this.visibility);

    if (hArray.size() > 0)
    {
      list.setSubtypeHierarchies(hArray.toString());
    }

    return list.toJSON();
  }

  @Request
  public MasterList build()
  {
    JsonObject json = this.buildJSON();

    return MasterList.create(json);
  }
}
