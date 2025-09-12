package net.geoprism.registry.axon.config;

import org.axonframework.eventhandling.DomainEventData;

public interface IndexEventData<T> extends DomainEventData<T>
{
  public Long getGlobalIndex();
}
