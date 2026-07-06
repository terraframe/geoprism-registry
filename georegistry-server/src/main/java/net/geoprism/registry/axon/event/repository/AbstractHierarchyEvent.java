package net.geoprism.registry.axon.event.repository;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.geoprism.registry.view.PublishDTO;
import net.geoprism.registry.view.TypeClass;

public abstract class AbstractHierarchyEvent extends AbstractGeoObjectEdgeEvent implements GeoObjectEvent
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

  public abstract String getCode();

  public abstract String getType();

  @Override
  public String getEdgeClassType()
  {
    return TypeClass.HIERARCHY.getCode();
  }

  @Override
  public Boolean isValidFor(PublishDTO dto)
  {
    Date date = dto.getDate();

    if (dto.getHierarchyTypes().anyMatch(this.getEdgeTypeCode()::equals))
    {
      return ( date.after(this.getStartDate()) && date.before(this.getEndDate()) ) || date.equals(this.getStartDate()) || date.equals(this.getEndDate());
    }

    return false;
  }

  @Override
  public String getSourceCode()
  {
    return null;
  }

  @Override
  public String getSourceType()
  {
    return null;
  }

  @Override
  public String getTargetCode()
  {
    return this.getCode();
  }

  @Override
  public String getTargetType()
  {
    return this.getType();
  }

  @Override
  @JsonIgnore
  public String getBaseObjectId()
  {
    return this.getCode() + "#" + this.getType() + "_H_" + this.getEdgeTypeCode();
  }

  @Override
  @JsonIgnore
  public EventPhase getEventPhase()
  {
    return EventPhase.EDGE;
  }
}
