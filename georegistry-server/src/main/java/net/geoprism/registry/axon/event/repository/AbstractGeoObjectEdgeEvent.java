package net.geoprism.registry.axon.event.repository;

import net.geoprism.registry.view.TypeInfo;

public abstract class AbstractGeoObjectEdgeEvent extends AbstractGeoObjectEvent implements ObjectEdgeEventIF
{
  public AbstractGeoObjectEdgeEvent()
  {
    super();
  }

  public AbstractGeoObjectEdgeEvent(String eventId)
  {
    super(eventId);
  }

  public abstract TypeInfo getSourceType();

  public abstract String getSourceCode();

  public abstract TypeInfo getTargetType();

  public abstract String getTargetCode();

  public abstract TypeInfo getEdgeType();

}
