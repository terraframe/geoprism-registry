package net.geoprism.registry.axon.event.repository;

public abstract class AbstractBusinessObjectEdgeEvent extends AbstractRepositoryEvent
{
  public AbstractBusinessObjectEdgeEvent()
  {
    super();
  }

  public AbstractBusinessObjectEdgeEvent(String eventId)
  {
    super(eventId);
  }

  public abstract String getSourceType();

  public abstract String getSourceCode();

  public abstract String getTargetType();

  public abstract String getTargetCode();

  public abstract String getEdgeTypeCode();

}
