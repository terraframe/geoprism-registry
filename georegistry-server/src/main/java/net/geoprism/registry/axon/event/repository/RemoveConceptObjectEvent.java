package net.geoprism.registry.axon.event.repository;

import net.geoprism.registry.view.PublishDTO;

public class RemoveConceptObjectEvent extends RemoveObjectEvent implements ConceptObjectEvent
{
  public RemoveConceptObjectEvent()
  {
  }

  public RemoveConceptObjectEvent(String code, String type)
  {
    super(code, type);
  }

  @Override
  public Boolean isValidFor(PublishDTO dto)
  {
    return dto.getConceptClasses().anyMatch(this.getType()::equals);
  }

}
