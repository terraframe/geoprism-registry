package net.geoprism.registry.axon.event.repository;

import net.geoprism.registry.view.ObjectOverTimeDTO;
import net.geoprism.registry.view.PublishDTO;
import net.geoprism.registry.view.TypeInfo;

public class BusinessObjectApplyEvent extends ObjectApplyEvent implements BaseObjectEvent
{
  public BusinessObjectApplyEvent()
  {
  }

  public BusinessObjectApplyEvent(String code, TypeInfo type, ObjectOverTimeDTO object, Boolean isNew)
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
    return dto.getTypes().stream().anyMatch(this.getType()::equals);
  }
}
