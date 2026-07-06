package net.geoprism.registry.axon.event.repository;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.runwaysdk.dataaccess.ProgrammingErrorException;

import net.geoprism.registry.model.ConceptObject;
import net.geoprism.registry.service.business.ConceptObjectBusinessServiceIF;
import net.geoprism.registry.view.ObjectOverTimeDTO;

public class ConceptObjectEventBuilder
{
  private boolean                        attributeUpdate;

  private ConceptObject                  object;

  private Boolean                        isNew;

  private List<ConceptObjectEvent>       events;

  private ConceptObjectBusinessServiceIF service;

  public ConceptObjectEventBuilder(ConceptObjectBusinessServiceIF service)
  {
    this.service = service;
    this.attributeUpdate = false;
    this.isNew = false;
    this.events = new LinkedList<>();
  }

  public Optional<ConceptObject> getObject()
  {
    return this.getObject(false);
  }

  public Optional<ConceptObject> getObject(boolean hasAttributeUpdate)
  {
    this.attributeUpdate = this.attributeUpdate || hasAttributeUpdate;

    return Optional.ofNullable(this.object);
  }

  @SuppressWarnings("unchecked")
  public <T extends ConceptObject> T getOrThrow()
  {
    return (T) this.getOrThrow(false);
  }

  @SuppressWarnings("unchecked")
  public <T extends ConceptObject> T getOrThrow(boolean hasAttributeUpdate)
  {
    return (T) this.getObject(hasAttributeUpdate).orElseThrow(() -> {
      throw new ProgrammingErrorException("Business object is required to perform action");
    });
  }

  public void setObject(ConceptObject object)
  {
    if (this.object != null)
    {
      throw new UnsupportedOperationException("Cannot override an object which is already set");
    }

    this.object = object;
  }

  public void setObject(ConceptObject object, Boolean isNew)
  {
    this.setObject(object);

    this.isNew = isNew;
  }

  public List<ConceptObjectEvent> getEvents()
  {
    return events;
  }

  public void setEvents(List<ConceptObjectEvent> events)
  {
    this.events = events;
  }

  public void addEvent(ConceptObjectEvent event)
  {
    this.events.add(event);
  }

  public void addEvent(Optional<ConceptObjectEvent> event)
  {
    event.ifPresent(events::add);
  }

  public String getCode()
  {
    return this.getOrThrow().getCode();
  }

  public String getType()
  {
    return this.getOrThrow().getType().getCode();
  }

  public boolean isAttributeUpdate()
  {
    return attributeUpdate;
  }

  public void setAttributeUpdate(boolean attributeUpdate)
  {
    this.attributeUpdate = attributeUpdate;
  }

  public List<RepositoryEvent> build()
  {
    LinkedList<RepositoryEvent> list = new LinkedList<>();

    ConceptObject object = this.getOrThrow();

    if (this.attributeUpdate || this.isNew)
    {
      ObjectOverTimeDTO dto = service.toDTO(object);

      list.add(new ConceptObjectApplyEvent(object.getCode(), object.getType().getCode(), dto, isNew));
    }

    list.addAll(events);

    return list;
  }

}
