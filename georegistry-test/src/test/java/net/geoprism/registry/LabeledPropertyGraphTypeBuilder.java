/**
 *
 */
package net.geoprism.registry;

import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;

import net.geoprism.graph.GraphTypeReference;
import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.graph.SingleLabeledPropertyGraphType;
import net.geoprism.registry.lpg.StrategyConfiguration;
import net.geoprism.registry.lpg.TreeStrategyConfiguration;
import net.geoprism.registry.service.business.LabeledPropertyGraphTypeBusinessServiceIF;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestHierarchyTypeInfo;

public class LabeledPropertyGraphTypeBuilder
{
  private TestHierarchyTypeInfo ht;
  
  private GraphTypeReference[] graphTypeReferences;

  private String                code;

  private StrategyConfiguration configuration;
  
  private String strategyType;
  
  private Organization organization;
  
  private String[] geoObjectTypeCodes;

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
    
    if (configuration instanceof TreeStrategyConfiguration)
    {
      this.strategyType = LabeledPropertyGraphType.TREE;
    }
    else
    {
      throw new UnsupportedOperationException();
    }
  }
  
  public void setGeoObjectTypeCodes(String[] geoObjectTypeCodes)
  {
    this.geoObjectTypeCodes = geoObjectTypeCodes;
  }
  
  public Organization getOrganization()
  {
    return organization;
  }

  public void setOrganization(Organization organization)
  {
    this.organization = organization;
  }

  public void setGraphTypes(GraphTypeReference[] graphTypeReferences)
  {
    this.graphTypeReferences = graphTypeReferences;
  }
  
  public void setStrategyType(String strategyType)
  {
    this.strategyType = strategyType;
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
    graph.setHierarchy(this.ht == null ? null : this.ht.getCode());
    graph.setGraphTypeReferences(this.graphTypeReferences);
    graph.setStrategyType(strategyType);
    graph.setStrategyConfiguration(this.configuration);
    graph.setGeoObjectTypeCodesList(this.geoObjectTypeCodes);
    
    if (organization != null)
    {
      graph.setOrganization(organization);
    }
    else
    {
      graph.setOrganization(this.ht == null ? null : ht.getOrganization().getServerObject().getOrganization());
    }

    return graph.toJSON();
  }

  @Request
  public LabeledPropertyGraphType build(LabeledPropertyGraphTypeBusinessServiceIF service)
  {
    JsonObject json = this.buildJSON();

    return service.apply(json);
  }
}
