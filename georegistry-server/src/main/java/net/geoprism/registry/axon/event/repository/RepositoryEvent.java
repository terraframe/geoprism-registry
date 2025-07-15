package net.geoprism.registry.axon.event.repository;

import net.geoprism.registry.view.PublishDTO;

public interface RepositoryEvent
{
  public EventType getEventType();

  public Boolean isValidFor(PublishDTO dto);

  public String getAggregate();

}
