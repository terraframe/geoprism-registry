package net.geoprism.registry.axon.event;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;

import com.runwaysdk.dataaccess.ProgrammingErrorException;

import net.geoprism.registry.axon.command.GeoObjectCompositeCreateCommand;
import net.geoprism.registry.axon.command.GeoObjectCompositeCommand;
import net.geoprism.registry.etl.upload.ClassifierVertexCache;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;

public class GeoObjectEventBuilder
{
  private boolean               attributeUpdate;

  private ServerGeoObjectIF     object;

  private String                parentJson;

  private Boolean               isNew;

  private Boolean               isImport;

  private List<GeoObjectEvent>  events;

  private ClassifierVertexCache classifierCache;

  private Boolean               refreshWorking;

  public GeoObjectEventBuilder()
  {
    this(null);
  }

  public GeoObjectEventBuilder(ClassifierVertexCache classifierCache)
  {
    this.classifierCache = classifierCache;
    this.attributeUpdate = false;
    this.isNew = false;
    this.isImport = false;
    this.events = new LinkedList<>();
    this.refreshWorking = false;
  }

  public Optional<ServerGeoObjectIF> getObject()
  {
    return this.getObject(false);
  }

  public Optional<ServerGeoObjectIF> getObject(boolean hasAttributeUpdate)
  {
    this.attributeUpdate = this.attributeUpdate || hasAttributeUpdate;

    return Optional.ofNullable(this.object);
  }

  @SuppressWarnings("unchecked")
  public <T extends ServerGeoObjectIF> T getOrThrow()
  {
    return (T) this.getOrThrow(false);
  }

  @SuppressWarnings("unchecked")
  public <T extends ServerGeoObjectIF> T getOrThrow(boolean hasAttributeUpdate)
  {
    return (T) this.getObject(hasAttributeUpdate).orElseThrow(() -> {
      throw new ProgrammingErrorException("Geo Object is request to perform action");
    });
  }

  public void setObject(ServerGeoObjectIF object)
  {
    if (this.object != null)
    {
      throw new UnsupportedOperationException("Cannot override an object which is already set");
    }

    this.object = object;
  }

  public void setObject(ServerGeoObjectIF object, Boolean isNew)
  {
    this.setObject(object);

    this.isNew = isNew;
  }

  public void setObject(ServerGeoObjectIF object, Boolean isNew, String parentJson)
  {
    this.setObject(object);

    this.isNew = isNew;
    this.parentJson = parentJson;
  }

  public void setObject(ServerGeoObjectIF object, Boolean isNew, Boolean isImport)
  {
    this.setObject(object);

    this.isNew = isNew;
    this.isImport = isImport;
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

  public String getType()
  {
    return this.getOrThrow().getType().getCode();
  }

  public String getParentJson()
  {
    return parentJson;
  }

  public void setParentJson(String parentJson)
  {
    this.parentJson = parentJson;
  }

  public Boolean getIsNew()
  {
    return isNew;
  }

  public void setIsNew(Boolean isNew)
  {
    this.isNew = isNew;
  }

  public Boolean getIsImport()
  {
    return isImport;
  }

  public void setIsImport(Boolean isImport)
  {
    this.isImport = isImport;
  }

  public boolean isAttributeUpdate()
  {
    return attributeUpdate;
  }

  public void setAttributeUpdate(boolean attributeUpdate)
  {
    this.attributeUpdate = attributeUpdate;
  }

  public Boolean getRefreshWorking()
  {
    return refreshWorking;
  }

  public void setRefreshWorking(Boolean refreshWorking)
  {
    this.refreshWorking = refreshWorking;
  }

  public void createExternalId(ExternalSystem system, String externalId, ImportStrategy importStrategy)
  {
    this.events.add(new GeoObjectSetExternalIdEvent(this.getUid(), this.getType(), system.getId(), externalId, importStrategy));
  }

  public void addParent(ServerGeoObjectIF parent, ServerHierarchyType hierarchy, Date startDate, Date endDate, String edgeUuid, Boolean validate)
  {
    this.events.add(new GeoObjectCreateParentEvent(this.getUid(), this.getType(), edgeUuid, hierarchy.getCode(), startDate, endDate, parent.getCode(), parent.getType().getCode(), validate));
  }

  public Object build(GeoObjectBusinessServiceIF service)
  {
    LinkedList<GeoObjectEvent> list = new LinkedList<>();

    if (this.attributeUpdate || this.isNew)
    {
      GeoObjectOverTime dto = service.toGeoObjectOverTime(object, false, this.classifierCache);

      list.add(new GeoObjectApplyEvent(dto.getUid(), this.isNew, this.isImport, dto.toJSON().toString(), this.parentJson));
    }

    list.addAll(events);

    if (this.refreshWorking && list.size() > 0)
    {
      list.getLast().setRefreshWorking(true);
    }

    if (this.isNew)
    {
      return new GeoObjectCompositeCreateCommand(getUid(), list);
    }

    return new GeoObjectCompositeCommand(getUid(), list);
  }

}
