package net.geoprism.registry.axon.event.repository;

import net.geoprism.registry.view.ObjectOverTimeDTO;
import net.geoprism.registry.view.PublishDTO;

public class ConceptObjectApplyEvent extends ObjectApplyEvent implements ConceptObjectEvent
{
  public ConceptObjectApplyEvent()
  {
  }

  public ConceptObjectApplyEvent(String code, String type, ObjectOverTimeDTO object, Boolean isNew)
  {
    super(code, type, object, isNew);
  }

  @Override
  public String getBaseObjectId()
  {
    return this.getCode() + "#" + this.getType() + "#CC";
  }

  @Override
  public Boolean isValidFor(PublishDTO dto)
  {
    return dto.getConceptClasses().anyMatch(this.getType()::equals);
  }
}
