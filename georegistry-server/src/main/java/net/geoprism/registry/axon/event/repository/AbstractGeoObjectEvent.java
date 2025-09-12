package net.geoprism.registry.axon.event.repository;

public abstract class AbstractGeoObjectEvent extends AbstractRepositoryEvent implements GeoObjectEvent
{
  private Boolean refreshWorking;

  public AbstractGeoObjectEvent()
  {
    this.refreshWorking = false;
  }

  public AbstractGeoObjectEvent(String eventId)
  {
    super(eventId);

    this.refreshWorking = false;
  }

  @Override
  public Boolean getRefreshWorking()
  {
    return this.refreshWorking;
  }

  public void setRefreshWorking(Boolean refreshWorking)
  {
    this.refreshWorking = refreshWorking;
  }

}
