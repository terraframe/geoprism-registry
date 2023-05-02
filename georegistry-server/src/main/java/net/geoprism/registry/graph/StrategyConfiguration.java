package net.geoprism.registry.graph;

import com.google.gson.JsonElement;

public interface StrategyConfiguration
{
  public JsonElement toJson();
  
  public StrategyPublisher getPublisher();
}
