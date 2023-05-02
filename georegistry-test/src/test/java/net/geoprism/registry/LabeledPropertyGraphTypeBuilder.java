/**
 *
 */
package net.geoprism.registry;

import java.util.Date;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;

import net.geoprism.registry.graph.StrategyConfiguration;
import net.geoprism.registry.test.TestGeoObjectTypeInfo;
import net.geoprism.registry.test.TestHierarchyTypeInfo;

public class LabeledPropertyGraphTypeBuilder
{
  private TestHierarchyTypeInfo[] hts;

  private TestGeoObjectTypeInfo[] types;

  private String                  code;

  private StrategyConfiguration   configuration;

  public LabeledPropertyGraphTypeBuilder()
  {
    this.code = "TEST_CODE";
  }

  public StrategyConfiguration getConfiguration()
  {
    return configuration;
  }

  public void setConfiguration(StrategyConfiguration configuration)
  {
    this.configuration = configuration;
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

    SingleLabeledPropertyGraphType graph = new SingleLabeledPropertyGraphType();
    graph.setValidOn(new Date());
    graph.getDisplayLabel().setValue("Test List");
    graph.setCode(this.code);
    graph.getDescription().setValue("My Abstract");
    graph.setHierarchies(array.toString());
    graph.setTypes(tArray.toString());
    graph.setStrategyType(LabeledPropertyGraphType.TREE);
    graph.setStrategyConfiguration(this.configuration);

    return graph.toJSON();
  }

  @Request
  public LabeledPropertyGraphType build()
  {
    JsonObject json = this.buildJSON();

    return LabeledPropertyGraphType.apply(json);
  }
}
