package net.geoprism.registry.service.business;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.eventhandling.GapAwareTrackingToken;
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.dataaccess.transaction.Transaction;

import net.geoprism.registry.Commit;
import net.geoprism.registry.Publish;
import net.geoprism.registry.axon.config.RegistryEventStore;
import net.geoprism.registry.axon.event.remote.RemoteBusinessObjectAddGeoObjectEvent;
import net.geoprism.registry.axon.event.remote.RemoteBusinessObjectCreateEdgeEvent;
import net.geoprism.registry.axon.event.remote.RemoteBusinessObjectEvent;
import net.geoprism.registry.axon.event.remote.RemoteEvent;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectCreateEdgeEvent;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectEvent;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectSetParentEvent;
import net.geoprism.registry.axon.event.repository.BusinessObjectAddGeoObjectEvent;
import net.geoprism.registry.axon.event.repository.BusinessObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.BusinessObjectCreateEdgeEvent;
import net.geoprism.registry.axon.event.repository.EventPhase;
import net.geoprism.registry.axon.event.repository.GeoObjectApplyEdgeEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectCreateParentEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectRemoveParentEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectUpdateParentEvent;
import net.geoprism.registry.axon.event.repository.InMemoryEventMerger;
import net.geoprism.registry.axon.event.repository.RepositoryEvent;
import net.geoprism.registry.event.EmptyPublishException;
import net.geoprism.registry.model.EdgeDirection;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.view.PublishDTO;

@Service
public class PublishEventService
{
  @Autowired
  private RegistryEventStore             store;

  @Autowired
  private EventGateway                   gateway;

  @Autowired
  private PublishBusinessServiceIF       publishService;

  @Autowired
  private DataSourceBusinessServiceIF    sourceService;

  @Autowired
  private GeoObjectBusinessServiceIF     service;

  @Autowired
  private CommitBusinessServiceIF        commitService;

  @Autowired
  private HierarchyTypeBusinessServiceIF hiearchyService;

  @Transaction
  public Publish publish(PublishDTO configuration) throws InterruptedException
  {
    // Add all types in a hierarchy to the exported geo object types
    List<String> codes = configuration.getHierarchyTypes() //
        .map(code -> this.hiearchyService.get(code)) //
        .map(hierarchy -> this.hiearchyService.getAllTypes(hierarchy)) //
        .flatMap(List::stream) //
        .map(type -> type.getCode()) //
        .toList();

    codes.stream().forEach(configuration::addGeoObjectType);

    Publish publish = this.publishService.create(configuration);

    createNewCommit(publish);

    return publish;
  }

  @Transaction
  public Commit createNewCommit(Publish publish)
  {
    Optional<Commit> previous = this.commitService.getLatest(publish);

    Long lastGlobalIndex = previous.map(p -> p.getLastOriginGlobalIndex()).orElse(Long.valueOf(0));
    Integer versionNumber = previous.map(p -> p.getVersionNumber() + 1).orElse(Integer.valueOf(1));

    GapAwareTrackingToken start = new GapAwareTrackingToken(lastGlobalIndex, new LinkedList<>());

    Commit commit = publish(publish, start, versionNumber);

    // Determine all of the dependent commits
    previous.ifPresent(p -> commit.addDependency(p).apply());

    List<Publish> dependencies = this.publishService.getRemoteFor(publish.toDTO());

    for (Publish dependency : dependencies)
    {
      // We only need to add the latest commit as a dependency because the
      // latest commit will have a dependency on its previous version if one
      // exists
      this.commitService.getLatest(dependency).ifPresent(latest -> commit.addDependency(latest).apply());
    }

    return commit;
  }

  protected Commit publish(Publish publish, GapAwareTrackingToken start, Integer versionNumber)
  {
    long total = 0;
    PublishDTO dto = publish.toDTO();

    GapAwareTrackingToken end = (GapAwareTrackingToken) this.store.createHeadToken();

    if (end != null && start.getIndex() < end.getIndex())
    {
      Commit commit = this.commitService.create(publish, versionNumber, end.getIndex());

      Set<String> sources = new TreeSet<String>();

      total += processEventType(start, end, EventPhase.OBJECT, publish, commit, dto, sources);

      total += processEventType(start, end, EventPhase.EDGE, publish, commit, dto, sources);

      if (total == 0)
      {
        throw new EmptyPublishException();
      }

      // Add the sources as a dependency to the commit
      sources.stream() //
          .map(code -> this.sourceService.getByCode(code)) //
          .filter(Optional::isPresent) //
          .map(Optional::get) //
          .forEach(source -> {
            this.commitService.addSource(commit, source);
          });

      return commit;
    }

    throw new EmptyPublishException();
  }

  private RemoteEvent build(Publish publish, Commit commit, RepositoryEvent event, Set<String> sources)
  {
    if (event instanceof GeoObjectApplyEvent)
    {
      String oJson = ( (GeoObjectApplyEvent) event ).getObject();
      String type = ( (GeoObjectApplyEvent) event ).getType();
      Boolean isNew = ( (GeoObjectApplyEvent) event ).getIsNew();
      String code = ( (GeoObjectApplyEvent) event ).getCode();

      // Possible optimization - Directly convert from GeoObjectOverTime to
      // GeoObject for a given time without using a ServerGeoObject

      ServerGeoObjectIF object = this.service.getGeoObjectByCode(code, type);

      this.service.populate(object, GeoObjectOverTime.fromJSON(ServiceFactory.getAdapter(), oJson));

      GeoObject dto = this.service.toGeoObject(object, publish.getForDate(), false);

      String dataSource = (String) dto.getValue(DefaultAttribute.DATA_SOURCE.getName());

      if (!StringUtils.isBlank(dataSource))
      {
        sources.add(dataSource);
      }

      return new RemoteGeoObjectEvent(commit.getUid(), code, isNew, dto.toJSON().toString(), type, publish.getStartDate(), publish.getEndDate());
    }
    else if (event instanceof GeoObjectCreateParentEvent)
    {
      String code = ( (GeoObjectCreateParentEvent) event ).getCode();
      String type = ( (GeoObjectCreateParentEvent) event ).getType();
      String edgeUid = ( (GeoObjectCreateParentEvent) event ).getEdgeUid();
      String edgeType = ( (GeoObjectCreateParentEvent) event ).getEdgeType();
      String parentType = ( (GeoObjectCreateParentEvent) event ).getParentType();
      String parentCode = ( (GeoObjectCreateParentEvent) event ).getParentCode();
      String dataSource = ( (GeoObjectCreateParentEvent) event ).getDataSource();

      if (!StringUtils.isBlank(dataSource))
      {
        sources.add(dataSource);
      }

      return new RemoteGeoObjectSetParentEvent(commit.getUid(), code, type, edgeUid, edgeType, publish.getStartDate(), publish.getEndDate(), parentCode, parentType, dataSource);
    }
    else if (event instanceof GeoObjectUpdateParentEvent)
    {
      String code = ( (GeoObjectUpdateParentEvent) event ).getCode();
      String type = ( (GeoObjectUpdateParentEvent) event ).getType();
      String edgeUid = ( (GeoObjectUpdateParentEvent) event ).getEdgeUid();
      String edgeType = ( (GeoObjectUpdateParentEvent) event ).getEdgeType();
      String parentType = ( (GeoObjectUpdateParentEvent) event ).getParentType();
      String parentCode = ( (GeoObjectUpdateParentEvent) event ).getParentCode();
      String dataSource = ( (GeoObjectUpdateParentEvent) event ).getDataSource();

      if (!StringUtils.isBlank(dataSource))
      {
        sources.add(dataSource);
      }

      return new RemoteGeoObjectSetParentEvent(commit.getUid(), code, type, edgeUid, edgeType, publish.getStartDate(), publish.getEndDate(), parentCode, parentType, dataSource);
    }
    else if (event instanceof GeoObjectRemoveParentEvent)
    {
      String code = ( (GeoObjectRemoveParentEvent) event ).getCode();
      String type = ( (GeoObjectRemoveParentEvent) event ).getType();
      String edgeUid = ( (GeoObjectRemoveParentEvent) event ).getEdgeUid();
      String edgeType = ( (GeoObjectRemoveParentEvent) event ).getEdgeType();

      return new RemoteGeoObjectSetParentEvent(commit.getUid(), code, type, edgeUid, edgeType, publish.getStartDate(), publish.getEndDate(), null, null, null);
    }
    else if (event instanceof GeoObjectApplyEdgeEvent)
    {
      String sourceCode = ( (GeoObjectApplyEdgeEvent) event ).getSourceCode();
      String sourceType = ( (GeoObjectApplyEdgeEvent) event ).getSourceType();
      String edgeUid = ( (GeoObjectApplyEdgeEvent) event ).getEdgeUid();
      String edgeType = ( (GeoObjectApplyEdgeEvent) event ).getEdgeType();
      String edgeTypeCode = ( (GeoObjectApplyEdgeEvent) event ).getEdgeTypeCode();
      String targetCode = ( (GeoObjectApplyEdgeEvent) event ).getTargetCode();
      String targetType = ( (GeoObjectApplyEdgeEvent) event ).getTargetType();
      Date startDate = ( (GeoObjectApplyEdgeEvent) event ).getStartDate();
      Date endDate = ( (GeoObjectApplyEdgeEvent) event ).getEndDate();
      String dataSource = ( (GeoObjectApplyEdgeEvent) event ).getDataSource();

      if (!StringUtils.isBlank(dataSource))
      {
        sources.add(dataSource);
      }

      return new RemoteGeoObjectCreateEdgeEvent(commit.getUid(), sourceCode, sourceType, edgeUid, edgeType, edgeTypeCode, startDate, endDate, targetCode, targetType, dataSource);
    }
    else if (event instanceof BusinessObjectApplyEvent)
    {
      String oJson = ( (BusinessObjectApplyEvent) event ).getObject();
      String code = ( (BusinessObjectApplyEvent) event ).getCode();
      String type = ( (BusinessObjectApplyEvent) event ).getType();

      // TODO: Use business object service to get the data source??
      JsonObject object = JsonParser.parseString(oJson).getAsJsonObject();
      JsonObject data = object.get("data").getAsJsonObject();

      String dataSource = data.has(DefaultAttribute.DATA_SOURCE.getName()) ? data.get(DefaultAttribute.DATA_SOURCE.getName()).getAsString() : null;

      if (!StringUtils.isBlank(dataSource))
      {
        sources.add(dataSource);
      }

      return new RemoteBusinessObjectEvent(commit.getUid(), code, type, oJson);
    }
    else if (event instanceof BusinessObjectAddGeoObjectEvent)
    {
      String code = ( (BusinessObjectAddGeoObjectEvent) event ).getCode();
      String type = ( (BusinessObjectAddGeoObjectEvent) event ).getType();
      String edgeUid = ( (BusinessObjectAddGeoObjectEvent) event ).getEdgeUid();
      String edgeType = ( (BusinessObjectAddGeoObjectEvent) event ).getEdgeType();
      String geoObjectType = ( (BusinessObjectAddGeoObjectEvent) event ).getGeoObjectType();
      String geoObjectCode = ( (BusinessObjectAddGeoObjectEvent) event ).getGeoObjectCode();
      EdgeDirection direction = ( (BusinessObjectAddGeoObjectEvent) event ).getDirection();
      String dataSource = ( (BusinessObjectAddGeoObjectEvent) event ).getDataSource();

      if (!StringUtils.isBlank(dataSource))
      {
        sources.add(dataSource);
      }

      return new RemoteBusinessObjectAddGeoObjectEvent(commit.getUid(), code, type, edgeUid, edgeType, geoObjectType, geoObjectCode, direction, dataSource);

    }
    else if (event instanceof BusinessObjectCreateEdgeEvent)
    {
      String sourceCode = ( (BusinessObjectCreateEdgeEvent) event ).getSourceCode();
      String sourceType = ( (BusinessObjectCreateEdgeEvent) event ).getSourceType();
      String edgeUid = ( (BusinessObjectCreateEdgeEvent) event ).getEdgeUid();
      String edgeType = ( (BusinessObjectCreateEdgeEvent) event ).getEdgeType();
      String targetCode = ( (BusinessObjectCreateEdgeEvent) event ).getTargetCode();
      String targetType = ( (BusinessObjectCreateEdgeEvent) event ).getTargetType();
      String dataSource = ( (BusinessObjectCreateEdgeEvent) event ).getDataSource();

      if (!StringUtils.isBlank(dataSource))
      {
        sources.add(dataSource);
      }

      return new RemoteBusinessObjectCreateEdgeEvent(commit.getUid(), sourceCode, sourceType, edgeUid, edgeType, targetCode, targetType, dataSource);
    }

    throw new UnsupportedOperationException("Events of type [" + event.getClass().getName() + "] do not support being published");
  }

  protected long processEventType(GapAwareTrackingToken start, GapAwareTrackingToken end, EventPhase phase, Publish publish, Commit commit, PublishDTO dto, Set<String> source)
  {
    long limit = 1000;
    long offset = 0;

    long total = 0;

    List<String> baseObjectIds = null;

    while ( ( baseObjectIds = this.store.getBaseObjectIds(start, end, phase, limit, offset) ).size() > 0)
    {
      for (String baseObjectId : baseObjectIds)
      {
        InMemoryEventMerger merger = new InMemoryEventMerger();
        DomainEventStream stream = this.store.readEvents(baseObjectId, start, end);

        while (stream.hasNext())
        {
          DomainEventMessage<?> message = stream.next();

          Object payload = message.getPayload();

          if (payload instanceof RepositoryEvent)
          {
            RepositoryEvent event = (RepositoryEvent) payload;

            if (phase.equals(event.getEventPhase()) && event.isValidFor(dto))
            {
              merger.add(event);

              total++;
            }
          }
        }

        merger.buildEvents().stream() //
            .map(event -> this.build(publish, commit, event, source)) //
            .map(GenericEventMessage::asEventMessage) //
            .forEach(this.gateway::publish);

      }
      
      
      offset += limit;
    }

    return total;
  }

}
