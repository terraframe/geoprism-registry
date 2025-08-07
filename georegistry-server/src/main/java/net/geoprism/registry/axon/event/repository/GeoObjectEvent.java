package net.geoprism.registry.axon.event.repository;

public interface GeoObjectEvent extends RepositoryEvent
{
  public Boolean getRefreshWorking();

  public void setRefreshWorking(Boolean refreshWorking);
}
