package net.geoprism.registry.axon.event.repository;

import net.geoprism.registry.view.ObjectOverTimeDTO;
import net.geoprism.registry.view.PublishDTO;

public class BusinessObjectApplyEvent extends ObjectApplyEvent implements BusinessObjectEvent
{
  public BusinessObjectApplyEvent()
  {
  }

  public BusinessObjectApplyEvent(String code, String type, ObjectOverTimeDTO object, Boolean isNew)
  {
    super(code, type, object, isNew);
  }

  @Override
  public String getBaseObjectId()
  {
    return this.getCode() + "#" + this.getType() + "#B";
  }

  @Override
  public Boolean isValidFor(PublishDTO dto)
  {
    return dto.getBusinessTypes().anyMatch(this.getType()::equals);
  }
}
