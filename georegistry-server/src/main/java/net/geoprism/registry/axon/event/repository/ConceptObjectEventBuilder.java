package net.geoprism.registry.axon.event.repository;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.runwaysdk.dataaccess.ProgrammingErrorException;

import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.graph.ConceptEdgeType;
import net.geoprism.registry.graph.DataSource;
import net.geoprism.registry.model.ConceptObject;
import net.geoprism.registry.service.business.ConceptObjectBusinessServiceIF;
import net.geoprism.registry.view.ObjectOverTimeDTO;

public class ConceptObjectEventBuilder
{
  private boolean                        attributeUpdate;

  private ConceptObject                  object;

  private Boolean                        isNew;

  private List<RepositoryEvent>          events;

  private ImportConfiguration            configuration;

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

  public List<RepositoryEvent> getEvents()
  {
    return events;
  }

  public void setEvents(List<RepositoryEvent> events)
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

  public void addParent(ConceptObject parent, ConceptEdgeType edgeType, Date startDate, Date endDate, DataSource source, Boolean validate)
  {
    ConceptObject object = this.getOrThrow();
    String code = source != null ? source.getCode() : null;

    this.events.add(new ObjectApplyEdgeEvent(parent.getCode(), parent.getType().getTypeInfo(), edgeType.getTypeInfo(), object.getCode(), object.getType().getTypeInfo(), startDate, endDate, code, ImportStrategy.NEW_AND_UPDATE, validate));
  }

  public void addChild(ConceptObject child, ConceptEdgeType edgeType, Date startDate, Date endDate, DataSource source, Boolean validate)
  {
    ConceptObject object = this.getOrThrow();
    String code = source != null ? source.getCode() : null;

    this.events.add(new ObjectApplyEdgeEvent(object.getCode(), object.getType().getTypeInfo(), edgeType.getTypeInfo(), child.getCode(), child.getType().getTypeInfo(), startDate, endDate, code, ImportStrategy.NEW_AND_UPDATE, validate));
  }

  public ImportConfiguration getConfiguration()
  {
    return configuration;
  }

  public void setConfiguration(ImportConfiguration configuration)
  {
    this.configuration = configuration;
  }

  public List<RepositoryEvent> build()
  {
    LinkedList<RepositoryEvent> list = new LinkedList<>();

    ConceptObject object = this.getOrThrow();

    if (this.attributeUpdate || this.isNew)
    {
      ObjectOverTimeDTO dto = service.toDTO(object);

      ConceptObjectApplyEvent event = new ConceptObjectApplyEvent(object.getCode(), object.getType().getCode(), dto, isNew);

      if (this.configuration != null)
      {
        event.setStartDate(this.configuration.getStartDate());
        event.setEndDate(this.configuration.getEndDate());
        event.setHistoryId(this.configuration.getHistoryId());
      }

      list.add(event);
    }

    list.addAll(events);

    return list;
  }

}
