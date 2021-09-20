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

  public void setHts(Hierarchy... hts)
  {
    this.hts = hts;
  }

  public void setOrg(Organization org)
  {
    this.org = org;
  }

  public void setInfo(TestGeoObjectTypeInfo info)
  {
    this.info = info;
  }

  public void setVisibility(String visibility)
  {
    this.visibility = visibility;
  }

  public void setMaster(boolean isMaster)
  {
    this.isMaster = isMaster;
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
    list.setCode("TEST_CODE");
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
