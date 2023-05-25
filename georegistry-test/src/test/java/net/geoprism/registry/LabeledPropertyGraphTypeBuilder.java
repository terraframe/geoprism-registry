/**
 *
 */
package net.geoprism.registry;

import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;

import net.geoprism.registry.graph.StrategyConfiguration;
import net.geoprism.registry.test.TestHierarchyTypeInfo;
import net.geoprism.registry.test.USATestData;

public class LabeledPropertyGraphTypeBuilder
{
  private TestHierarchyTypeInfo ht;

  private String                code;

  private StrategyConfiguration configuration;

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

  public LabeledPropertyGraphTypeBuilder setHt(TestHierarchyTypeInfo ht)
  {
    this.ht = ht;

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
    SingleLabeledPropertyGraphType graph = new SingleLabeledPropertyGraphType();
    graph.setValidOn(USATestData.DEFAULT_OVER_TIME_DATE);
    graph.getDisplayLabel().setValue("Test List");
    graph.setCode(this.code);
    graph.getDescription().setValue("My Abstract");
    graph.setHierarchy(this.ht.getCode());
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
