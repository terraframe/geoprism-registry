package net.geoprism.registry;

import java.util.Date;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;

import net.geoprism.registry.test.TestGeoObjectTypeInfo;
import net.geoprism.registry.test.TestHierarchyTypeInfo;

public class MasterListBuilder
{
  private Organization            org;

  private TestHierarchyTypeInfo   ht;

  private TestGeoObjectTypeInfo   info;

  private String                  visibility;

  private boolean                 isMaster;

  private TestGeoObjectTypeInfo[] parents;

  private TestHierarchyTypeInfo[] subtypeHierarchies;

  public void setOrg(Organization org)
  {
    this.org = org;
  }

  public void setHt(TestHierarchyTypeInfo ht)
  {
    this.ht = ht;
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

  public void setParents(TestGeoObjectTypeInfo... parents)
  {
    this.parents = parents;
  }

  public void setSubtypeHierarchies(TestHierarchyTypeInfo... subtypeHierarchies)
  {
    this.subtypeHierarchies = subtypeHierarchies;
  }

  @Request
  public JsonObject buildJSON()
  {
    JsonArray pArray = new JsonArray();
    for (TestGeoObjectTypeInfo parent : this.parents)
    {
      JsonObject object = new JsonObject();
      object.addProperty("code", parent.getCode());
      object.addProperty("selected", true);

      pArray.add(object);
    }

    JsonObject hierarchy = new JsonObject();
    hierarchy.addProperty("code", this.ht.getCode());
    hierarchy.add("parents", pArray);

    JsonArray array = new JsonArray();
    array.add(hierarchy);

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

    if (this.subtypeHierarchies != null)
    {
      JsonArray hArray = new JsonArray();
      for (TestHierarchyTypeInfo ht : this.subtypeHierarchies)
      {
        JsonObject object = new JsonObject();
        object.addProperty("code", ht.getCode());
        object.addProperty("selected", true);

        hArray.add(object);

      }
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
