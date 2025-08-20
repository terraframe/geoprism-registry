package net.geoprism.registry.axon.event.repository;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.google.gson.JsonObject;
import com.runwaysdk.dataaccess.ProgrammingErrorException;

import net.geoprism.registry.BusinessEdgeType;
import net.geoprism.registry.axon.command.repository.BusinessObjectCompositeCommand;
import net.geoprism.registry.graph.DataSource;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.EdgeDirection;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.service.business.BusinessObjectBusinessServiceIF;

public class BusinessObjectEventBuilder
{
  private boolean                         attributeUpdate;

  private BusinessObject                  object;

  private Boolean                         isNew;

  private List<BusinessObjectEvent>       events;

  private BusinessObjectBusinessServiceIF service;
  
  public BusinessObjectEventBuilder(BusinessObjectBusinessServiceIF service)
  {
    this.service = service;
    this.attributeUpdate = false;
    this.isNew = false;
    this.events = new LinkedList<>();
  }

  public Optional<BusinessObject> getObject()
  {
    return this.getObject(false);
  }

  public Optional<BusinessObject> getObject(boolean hasAttributeUpdate)
  {
    this.attributeUpdate = this.attributeUpdate || hasAttributeUpdate;

    return Optional.ofNullable(this.object);
  }

  @SuppressWarnings("unchecked")
  public <T extends BusinessObject> T getOrThrow()
  {
    return (T) this.getOrThrow(false);
  }

  @SuppressWarnings("unchecked")
  public <T extends BusinessObject> T getOrThrow(boolean hasAttributeUpdate)
  {
    return (T) this.getObject(hasAttributeUpdate).orElseThrow(() -> {
      throw new ProgrammingErrorException("Business object is required to perform action");
    });
  }

  public void setObject(BusinessObject object)
  {
    if (this.object != null)
    {
      throw new UnsupportedOperationException("Cannot override an object which is already set");
    }

    this.object = object;
  }

  public void setObject(BusinessObject object, Boolean isNew)
  {
    this.setObject(object);

    this.isNew = isNew;
  }

  public List<BusinessObjectEvent> getEvents()
  {
    return events;
  }

  public void setEvents(List<BusinessObjectEvent> events)
  {
    this.events = events;
  }

  public void addEvent(BusinessObjectEvent event)
  {
    this.events.add(event);
  }

  public void addEvent(Optional<BusinessObjectEvent> event)
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

  public void addParent(BusinessObject parent, BusinessEdgeType edgeType, DataSource source, Boolean validate)
  {
    BusinessObject object = this.getOrThrow();
    String code = source != null ? source.getCode() : null;

    this.events.add(new BusinessObjectCreateEdgeEvent(parent.getCode(), parent.getType().getCode(), edgeType.getCode(), object.getCode(), object.getType().getCode(), code, validate));
  }

  public void addGeoObject(BusinessEdgeType edgeType, ServerGeoObjectIF geoObject, EdgeDirection direction, DataSource source)
  {
    BusinessObject object = this.getOrThrow();
    String code = source != null ? source.getCode() : null;

    this.events.add(new BusinessObjectAddGeoObjectEvent(object.getCode(), object.getType().getCode(), edgeType.getCode(), geoObject.getType().getCode(), geoObject.getCode(), direction, code));
  }

  public Object build()
  {
    BusinessObject object = this.getOrThrow();

    LinkedList<BusinessObjectEvent> list = new LinkedList<>();

    if (this.attributeUpdate || isNew)
    {
      JsonObject dto = service.toJSON(object);

      list.add(new BusinessObjectApplyEvent(object.getCode(), object.getType().getCode(), dto.toString(), isNew));
    }

    list.addAll(events);

    // if (this.isNew)
    // {
    // return new BusinessObjectCompositeCreateCommand(object.getCode(),
    // object.getType().getCode(), list);
    // }

    return new BusinessObjectCompositeCommand(object.getCode(), object.getType().getCode(), list);
  }

}
