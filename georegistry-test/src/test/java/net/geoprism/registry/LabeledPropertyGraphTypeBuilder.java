/**
 *
 */
package net.geoprism.registry;

import java.util.Date;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;

import net.geoprism.registry.test.TestGeoObjectTypeInfo;
import net.geoprism.registry.test.TestHierarchyTypeInfo;

public class LabeledPropertyGraphTypeBuilder
{
  private TestHierarchyTypeInfo[] hts;

  private TestGeoObjectTypeInfo[] types;

  private String                code;

  public LabeledPropertyGraphTypeBuilder()
  {
    this.code = "TEST_CODE";
  }

  public LabeledPropertyGraphTypeBuilder setHts(TestHierarchyTypeInfo... hts)
  {
    this.hts = hts;

    return this;
  }

  public LabeledPropertyGraphTypeBuilder setTypes(TestGeoObjectTypeInfo... types)
  {
    this.types = types;

    return this;
  }

  public LabeledPropertyGraphTypeBuilder setCode(String code)
  {
    this.code = code;

    return this;
  }

  @Request
  public JsonObject buildJSON()
  {
    JsonArray hArray = new JsonArray();
    JsonArray array = new JsonArray();

    for (TestHierarchyTypeInfo ht : hts)
    {
      array.add(ht.getCode());
    }
    
    JsonArray tArray = new JsonArray();
    
    for (TestGeoObjectTypeInfo ht : types)
    {
      tArray.add(ht.getCode());
    }

    SingleLabeledPropertyGraphType list = new SingleLabeledPropertyGraphType();
    list.setValidOn(new Date());
    list.getDisplayLabel().setValue("Test List");
    list.setCode(this.code);
    list.getDescription().setValue("My Abstract");
    list.setHierarchies(array.toString());
    list.setTypes(tArray.toString());

    if (hArray.size() > 0)
    {
      list.setSubtypeHierarchies(hArray.toString());
    }

    return list.toJSON();
  }

  @Request
  public LabeledPropertyGraphType build()
  {
    JsonObject json = this.buildJSON();

    return LabeledPropertyGraphType.apply(json);
  }
}
