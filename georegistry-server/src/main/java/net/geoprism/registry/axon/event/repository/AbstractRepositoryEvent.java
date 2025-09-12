package net.geoprism.registry.axon.event.repository;

public abstract class AbstractRepositoryEvent implements RepositoryEvent
{
  private String eventId;

  public AbstractRepositoryEvent()
  {
  }

  public AbstractRepositoryEvent(String eventId)
  {
    this.eventId = eventId;
  }

  public String getEventId()
  {
    return eventId;
  }

  public void setEventId(String eventId)
  {
    this.eventId = eventId;
  }
}
