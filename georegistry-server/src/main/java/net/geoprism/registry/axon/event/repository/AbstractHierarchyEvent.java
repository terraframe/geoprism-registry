package net.geoprism.registry.axon.event.repository;

import java.util.Date;

import net.geoprism.registry.view.PublishDTO;

public abstract class AbstractHierarchyEvent extends AbstractGeoObjectEvent implements GeoObjectEvent
{

  public AbstractHierarchyEvent()
  {
    super();
  }

  public AbstractHierarchyEvent(String eventId)
  {
    super(eventId);
  }

  public abstract Date getStartDate();

  public abstract Date getEndDate();

  public abstract String getEdgeType();

  @Override
  public Boolean isValidFor(PublishDTO dto)
  {
    Date date = dto.getDate();

    if (dto.getHierarchyTypes().anyMatch(this.getEdgeType()::equals))
    {
      return ( date.after(this.getStartDate()) && date.before(this.getEndDate()) ) || date.equals(this.getStartDate()) || date.equals(this.getEndDate());
    }

    return false;
  }
}
