package net.geoprism.registry.axon.event.repository;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.geoprism.registry.view.PublishDTO;
import net.geoprism.registry.view.TypeInfo;

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

  public abstract TypeInfo getType();

  @Override
  public Boolean isValidFor(PublishDTO dto)
  {
    Date date = dto.getDate();

    if (dto.getTypes().stream().anyMatch(this.getEdgeType()::equals))
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
  public TypeInfo getSourceType()
  {
    return null;
  }

  @Override
  public String getTargetCode()
  {
    return this.getCode();
  }

  @Override
  public TypeInfo getTargetType()
  {
    return this.getType();
  }

  @Override
  @JsonIgnore
  public String getBaseObjectId()
  {
    String delimeter = "_" + this.getEdgeType().getTypeClass().getCode() + "_";

    return this.getCode() + "#" + this.getType().getTypeCode() + delimeter + this.getEdgeType().getTypeCode();
  }

  @Override
  @JsonIgnore
  public EventPhase getEventPhase()
  {
    return EventPhase.EDGE;
  }
}
