package net.geoprism.registry.axon.event.repository;

import net.geoprism.registry.view.PublishDTO;

public class RemoveBusinessObjectEvent extends RemoveObjectEvent implements BusinessObjectEvent
{
  public RemoveBusinessObjectEvent()
  {
  }

  public RemoveBusinessObjectEvent(String code, String type)
  {
    super(code, type);
  }

  @Override
  public Boolean isValidFor(PublishDTO dto)
  {
    return dto.getBusinessTypes().anyMatch(this.getType()::equals);
  }

}
