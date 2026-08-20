package net.geoprism.registry.axon.event.repository;

import java.util.Date;

import net.geoprism.registry.view.TypeInfo;

public abstract class AbstractObjectEdgeEvent extends AbstractRepositoryEvent
{
  public AbstractObjectEdgeEvent()
  {
    super();
  }

  public AbstractObjectEdgeEvent(String eventId)
  {
    super(eventId);
  }

  public abstract TypeInfo getSourceType();

  public abstract String getSourceCode();

  public abstract TypeInfo getTargetType();

  public abstract String getTargetCode();

  public abstract TypeInfo getEdgeType();

  public abstract Date getStartDate();

  public abstract Date getEndDate();
}
