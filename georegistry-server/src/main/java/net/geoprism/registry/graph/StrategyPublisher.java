package net.geoprism.registry.graph;

import net.geoprism.registry.LabeledPropertyGraphTypeVersion;

public interface StrategyPublisher
{
  void publish(LabeledPropertyGraphTypeVersion version);
}
