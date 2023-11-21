/**
 *
 */
package net.geoprism.registry;

import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;

import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.graph.SingleLabeledPropertyGraphType;
import net.geoprism.registry.lpg.StrategyConfiguration;
import net.geoprism.registry.service.business.LabeledPropertyGraphTypeBusinessServiceIF;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestHierarchyTypeInfo;

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
    graph.setValidOn(TestDataSet.DEFAULT_OVER_TIME_DATE);
    graph.getDisplayLabel().setValue("Test List");
    graph.setCode(this.code);
    graph.getDescription().setValue("My Abstract");
    graph.setHierarchy(this.ht.getCode());
    graph.setStrategyType(LabeledPropertyGraphType.TREE);
    graph.setStrategyConfiguration(this.configuration);
    graph.setOrganization(this.ht.getOrganization().getServerObject().getOrganization());

    return graph.toJSON();
  }

  @Request
  public LabeledPropertyGraphType build(LabeledPropertyGraphTypeBusinessServiceIF service)
  {
    JsonObject json = this.buildJSON();

    return service.apply(json);
  }
}
