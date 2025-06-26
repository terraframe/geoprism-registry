package net.geoprism.registry.axon.event.repository;

import java.util.Date;

public interface GeoObjectEvent
{
  public EventType getEventType();

  public String getAggregate();

  public Boolean getRefreshWorking();

  public void setRefreshWorking(Boolean refreshWorking);

  public Boolean isValidFor(Date date);
}
