package net.geoprism.registry.axon.event.rollback;

import java.util.List;

import net.geoprism.registry.axon.event.repository.RepositoryEvent;

public abstract class RollbackEventBuilder
{
  public abstract List<RepositoryEvent> build();

  public abstract void addEvent(RepositoryEvent event);

}
