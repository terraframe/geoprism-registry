package net.geoprism.registry.axon.event.repository;

import net.geoprism.registry.view.PublishDTO;

public interface RepositoryEvent
{
  public String getEventId();

  public EventPhase getEventPhase();

  public Boolean isValidFor(PublishDTO dto);

  public String getBaseObjectId();

}
