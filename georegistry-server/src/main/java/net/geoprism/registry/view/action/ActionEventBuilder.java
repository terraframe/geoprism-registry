package net.geoprism.registry.view.action;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;

import com.runwaysdk.dataaccess.ProgrammingErrorException;

import net.geoprism.registry.axon.command.CompositeGeoObjectCommand;
import net.geoprism.registry.axon.event.ApplyGeoObjectEvent;
import net.geoprism.registry.axon.event.GeoObjectEvent;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;

public class ActionEventBuilder
{
  private boolean               hasAttributeUpdate;

  private VertexServerGeoObject object;

  private String                parentJson;

  private Boolean               isNew;

  private List<GeoObjectEvent>  events;

  public ActionEventBuilder()
  {
    this.hasAttributeUpdate = false;
    this.isNew = false;
    this.events = new LinkedList<>();
  }

  public Optional<VertexServerGeoObject> getObject()
  {
    return this.getObject(false);
  }

  public Optional<VertexServerGeoObject> getObject(boolean hasAttributeUpdate)
  {
    this.hasAttributeUpdate = this.hasAttributeUpdate || hasAttributeUpdate;

    return Optional.ofNullable(this.object);
  }

  public VertexServerGeoObject getOrThrow()
  {
    return this.getOrThrow(false);
  }

  public VertexServerGeoObject getOrThrow(boolean hasAttributeUpdate)
  {
    return this.getObject(hasAttributeUpdate).orElseThrow(() -> {
      throw new ProgrammingErrorException("Geo Object is request to perform action");
    });
  }

  public void setObject(VertexServerGeoObject object)
  {
    this.object = object;
  }

  public void setObject(VertexServerGeoObject object, Boolean isNew, String parentJson)
  {
    this.object = object;
    this.isNew = isNew;
    this.parentJson = parentJson;
  }

  public List<GeoObjectEvent> getEvents()
  {
    return events;
  }

  public void setEvents(List<GeoObjectEvent> events)
  {
    this.events = events;
  }

  public void addEvent(GeoObjectEvent event)
  {
    this.events.add(event);
  }

  public void addEvent(Optional<GeoObjectEvent> event)
  {
    event.ifPresent(events::add);
  }

  public String getUid()
  {
    return this.getOrThrow().getUid();
  }

  public String getParentJson()
  {
    return parentJson;
  }

  public void setParentJson(String parentJson)
  {
    this.parentJson = parentJson;
  }

  public CompositeGeoObjectCommand build(GeoObjectBusinessServiceIF service)
  {
    List<GeoObjectEvent> list = new LinkedList<>();

    if (this.hasAttributeUpdate || this.isNew)
    {
      GeoObjectOverTime dto = service.toGeoObjectOverTime(object);

      list.add(new ApplyGeoObjectEvent(dto.getUid(), this.isNew, false, dto.toJSON().toString(), this.parentJson));
    }

    list.addAll(events);

    return new CompositeGeoObjectCommand(getUid(), list);
  }

}
