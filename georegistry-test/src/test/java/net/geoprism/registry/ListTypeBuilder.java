/**
 *
 */
package net.geoprism.registry;

import java.util.Date;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;

import net.geoprism.registry.model.ServerOrganization;
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

  private ServerOrganization    org;

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

  public ListTypeBuilder setOrg(ServerOrganization org)
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
    list.setGeoObjectType(this.info.getServerObject().getType());
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
