package net.geoprism.registry.axon.event.repository;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.google.gson.JsonObject;
import com.runwaysdk.dataaccess.ProgrammingErrorException;

import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.graph.DataSource;
import net.geoprism.registry.graph.SourceAuthority;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.model.EdgeType;
import net.geoprism.registry.model.GraphType;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerParentTreeNode;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.view.ServerParentTreeNodeOverTime;

public abstract class AbstractGeoObjectEventBuilder<K>
{
  private boolean                      attributeUpdate;

  private K                            object;

  private Boolean                      isNew;

  private Boolean                      isImport;

  private List<GeoObjectEvent>         events;

  private Boolean                      refreshWorking;

  private GeoObjectImportConfiguration configuration;

  protected GeoObjectBusinessServiceIF service;

  public AbstractGeoObjectEventBuilder(GeoObjectBusinessServiceIF service)
  {
    this.service = service;
    this.attributeUpdate = false;
    this.isNew = false;
    this.isImport = false;
    this.events = new LinkedList<>();
    this.refreshWorking = false;
  }

  public GeoObjectImportConfiguration getConfiguration()
  {
    return configuration;
  }

  public void setConfiguration(GeoObjectImportConfiguration configuration)
  {
    this.configuration = configuration;
  }

  public Optional<K> getObject()
  {
    return this.getObject(false);
  }

  public Optional<K> getObject(boolean hasAttributeUpdate)
  {
    this.attributeUpdate = this.attributeUpdate || hasAttributeUpdate;

    return Optional.ofNullable(this.object);
  }

  @SuppressWarnings("unchecked")
  public <T extends K> T getOrThrow()
  {
    return (T) this.getOrThrow(false);
  }

  @SuppressWarnings("unchecked")
  public <T extends K> T getOrThrow(boolean hasAttributeUpdate)
  {
    return (T) this.getObject(hasAttributeUpdate).orElseThrow(() -> {

      throw new ProgrammingErrorException("Geo Object is request to perform action");
    });
  }

  public void setObject(K object)
  {
    if (this.object != null)
    {
      throw new UnsupportedOperationException("Cannot override an object which is already set");
    }

    this.object = object;
  }

  public void setObject(K object, Boolean isNew)
  {
    this.setObject(object);

    this.isNew = isNew;
  }

  public void setObject(K object, Boolean isNew, Boolean isImport)
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

  public abstract String getCode();

  public abstract String getType();

  protected abstract JsonObject toJSON();

  protected abstract void removeAllEdges(ServerHierarchyType hierarchyType);

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

  public void createExternalId(String authority, String externalId, ImportStrategy importStrategy)
  {
    this.events.add(new GeoObjectApplyExternalIdEvent(this.getCode(), this.getType(), authority, externalId, importStrategy));
  }

  public void addParent(ServerGeoObjectIF parent, ServerHierarchyType hierarchy, Date startDate, Date endDate, String edgeUuid, DataSource dataSource, Boolean validate)
  {
    String code = dataSource != null ? dataSource.getCode() : null;

    this.events.add(new GeoObjectCreateParentEvent(this.getCode(), this.getType(), edgeUuid, hierarchy.getCode(), startDate, endDate, parent.getCode(), parent.getType().getCode(), code, validate));
  }

  public void removeParent(ServerGeoObjectIF parent, ServerHierarchyType hierarchy, Date startDate, Date endDate, String edgeUuid)
  {
    this.events.add(new GeoObjectRemoveParentEvent(this.getCode(), this.getType(), edgeUuid, hierarchy.getCode(), startDate, endDate));
  }

  public void addExternalId(SourceAuthority authority, String id, ImportStrategy strategy)
  {
    this.events.add(new GeoObjectApplyExternalIdEvent(this.getCode(), this.getType(), authority.getCode(), id, strategy));
  }

  public void removeExternalId(SourceAuthority authority)
  {
    this.events.add(new GeoObjectRemoveExternalIdEvent(this.getCode(), this.getType(), authority.getCode()));
  }

  public void addEdge(ServerGeoObjectIF target, GraphType graphType, Date startDate, Date endDate, String edgeUuid, DataSource source, ImportStrategy strategy, Boolean validate)
  {
    String typeCode = EdgeType.getTypeCode(graphType);
    String code = source != null ? source.getCode() : null;

    this.events.add(new GeoObjectApplyEdgeEvent(this.getCode(), this.getType(), typeCode, graphType.getCode(), target.getCode(), target.getType().getCode(), startDate, endDate, code, strategy, validate));
  }

  public void setParents(ServerParentTreeNodeOverTime parentsOverTime, DataSource dataSource)
  {
    final Collection<ServerHierarchyType> hierarchyTypes = parentsOverTime.getHierarchies();

    for (ServerHierarchyType hierarchyType : hierarchyTypes)
    {
      final List<ServerParentTreeNode> entries = parentsOverTime.getEntries(hierarchyType);

      if (!this.getIsNew())
      {
        this.removeAllEdges(hierarchyType);
      }

      for (ServerParentTreeNode entry : entries)
      {
        ServerGeoObjectIF parent = entry.getGeoObject();

        this.addParent(parent, hierarchyType, entry.getStartDate(), entry.getEndDate(), entry.getUid(), dataSource, false);
      }
    }
  }

  public List<RepositoryEvent> build()
  {
    LinkedList<RepositoryEvent> list = new LinkedList<>();

    if (this.attributeUpdate || this.isNew)
    {
      GeoObjectApplyEvent event = new GeoObjectApplyEvent(this.getCode(), this.getType(), this.isNew, this.isImport, this.toJSON().toString());

      if (this.configuration != null)
      {
        event.setStartDate(this.configuration.getStartDate());
        event.setEndDate(this.configuration.getEndDate());
        event.setHistoryId(this.configuration.getHistoryId());
      }

      list.add(event);
    }

    list.addAll(events.stream().sorted((a, b) -> {
      // Update events should always be first
      if (a instanceof GeoObjectUpdateParentEvent)
      {
        return -1;
      }

      return 1;
    }).toList());

    if (this.refreshWorking && list.size() > 0)
    {
      GeoObjectEvent event = (GeoObjectEvent) list.getLast();
      event.setRefreshWorking(true);
    }

    return list;
  }

}
