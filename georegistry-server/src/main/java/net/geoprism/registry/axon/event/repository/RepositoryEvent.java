package net.geoprism.registry.axon.event.repository;

import java.util.Date;

public interface RepositoryEvent
{
  public EventType getEventType();

  public Boolean isValidFor(Date date);

  public String getAggregate();

}
