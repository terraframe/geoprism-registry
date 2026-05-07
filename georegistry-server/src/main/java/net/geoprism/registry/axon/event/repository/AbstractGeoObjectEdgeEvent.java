package net.geoprism.registry.axon.event.repository;

public abstract class AbstractGeoObjectEdgeEvent extends AbstractGeoObjectEvent
{
  public AbstractGeoObjectEdgeEvent()
  {
    super();
  }

  public AbstractGeoObjectEdgeEvent(String eventId)
  {
    super(eventId);
  }

  public abstract String getSourceType();

  public abstract String getSourceCode();

  public abstract String getTargetType();

  public abstract String getTargetCode();

  public abstract String getEdgeTypeCode();

  public abstract String getEdgeClassType();

}
