package net.geoprism.registry.service.business;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.eventhandling.GapAwareTrackingToken;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;

import net.geoprism.registry.Commit;
import net.geoprism.registry.Publish;
import net.geoprism.registry.axon.command.remote.RemoteBusinessObjectAddGeoObjectCommand;
import net.geoprism.registry.axon.command.remote.RemoteBusinessObjectCommand;
import net.geoprism.registry.axon.command.remote.RemoteBusinessObjectCreateEdgeCommand;
import net.geoprism.registry.axon.command.remote.RemoteCommand;
import net.geoprism.registry.axon.command.remote.RemoteGeoObjectCommand;
import net.geoprism.registry.axon.command.remote.RemoteGeoObjectCreateEdgeCommand;
import net.geoprism.registry.axon.command.remote.RemoteGeoObjectSetParentCommand;
import net.geoprism.registry.axon.config.RegistryEventStore;
import net.geoprism.registry.axon.event.repository.BusinessObjectAddGeoObjectEvent;
import net.geoprism.registry.axon.event.repository.BusinessObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.BusinessObjectCreateEdgeEvent;
import net.geoprism.registry.axon.event.repository.EventType;
import net.geoprism.registry.axon.event.repository.GeoObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectCreateEdgeEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectCreateParentEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectRemoveParentEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectUpdateParentEvent;
import net.geoprism.registry.axon.event.repository.InMemoryEventMerger;
import net.geoprism.registry.axon.event.repository.RepositoryEvent;
import net.geoprism.registry.model.EdgeDirection;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.view.PublishDTO;

@Service
public class PublishEventService
{
  @Autowired
  private RegistryEventStore          store;

  @Autowired
  private CommandGateway              gateway;

  @Autowired
  private GeoObjectBusinessServiceIF  service;

  @Autowired
  private PublishBusinessServiceIF    publishService;

  @Autowired
  private DataSourceBusinessServiceIF sourceService;

  @Autowired
  private CommitBusinessServiceIF     commitService;

  @Transaction
  public Publish publish(PublishDTO configuration) throws InterruptedException
  {
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
    PublishDTO dto = publish.toDTO();

    GapAwareTrackingToken end = (GapAwareTrackingToken) this.store.createHeadToken();

    if (end != null && start.getIndex() < end.getIndex())
    {
      Commit commit = this.commitService.create(publish, versionNumber, end.getIndex());

      Set<String> sources = new TreeSet<String>();

      processEventType(start, end, EventType.OBJECT, publish, commit, dto, sources);

      processEventType(start, end, EventType.HIERARCHY, publish, commit, dto, sources);

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

    throw new ProgrammingErrorException("Unable to publish events because no events exist");
  }

  private RemoteCommand build(Publish publish, Commit commit, RepositoryEvent event, Set<String> sources)
  {
    if (event instanceof GeoObjectApplyEvent)
    {
      String oJson = ( (GeoObjectApplyEvent) event ).getObject();
      String type = ( (GeoObjectApplyEvent) event ).getType();
      Boolean isNew = ( (GeoObjectApplyEvent) event ).getIsNew();
      String code = ( (GeoObjectApplyEvent) event ).getCode();

      ServerGeoObjectIF object = this.service.getGeoObjectByCode(code, type);

      this.service.populate(object, GeoObjectOverTime.fromJSON(ServiceFactory.getAdapter(), oJson));

      GeoObject dto = this.service.toGeoObject(object, publish.getForDate(), false);

      String dataSource = (String) dto.getValue(DefaultAttribute.DATA_SOURCE.getName());

      if (!StringUtils.isBlank(dataSource))
      {
        sources.add(dataSource);
      }

      return new RemoteGeoObjectCommand(commit.getUid(), code, isNew, dto.toJSON().toString(), type, publish.getStartDate(), publish.getEndDate());
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

      return new RemoteGeoObjectSetParentCommand(commit.getUid(), code, type, edgeUid, edgeType, publish.getStartDate(), publish.getEndDate(), parentCode, parentType, dataSource);
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

      return new RemoteGeoObjectSetParentCommand(commit.getUid(), code, type, edgeUid, edgeType, publish.getStartDate(), publish.getEndDate(), parentCode, parentType, dataSource);
    }
    else if (event instanceof GeoObjectRemoveParentEvent)
    {
      String code = ( (GeoObjectRemoveParentEvent) event ).getCode();
      String type = ( (GeoObjectRemoveParentEvent) event ).getType();
      String edgeUid = ( (GeoObjectRemoveParentEvent) event ).getEdgeUid();
      String edgeType = ( (GeoObjectRemoveParentEvent) event ).getEdgeType();

      return new RemoteGeoObjectSetParentCommand(commit.getUid(), code, type, edgeUid, edgeType, publish.getStartDate(), publish.getEndDate(), null, null, null);
    }
    else if (event instanceof GeoObjectCreateEdgeEvent)
    {
      String sourceCode = ( (GeoObjectCreateEdgeEvent) event ).getSourceCode();
      String sourceType = ( (GeoObjectCreateEdgeEvent) event ).getSourceType();
      String edgeUid = ( (GeoObjectCreateEdgeEvent) event ).getEdgeUid();
      String edgeType = ( (GeoObjectCreateEdgeEvent) event ).getEdgeType();
      String edgeTypeCode = ( (GeoObjectCreateEdgeEvent) event ).getEdgeTypeCode();
      String targetCode = ( (GeoObjectCreateEdgeEvent) event ).getTargetCode();
      String targetType = ( (GeoObjectCreateEdgeEvent) event ).getTargetType();
      Date startDate = ( (GeoObjectCreateEdgeEvent) event ).getStartDate();
      Date endDate = ( (GeoObjectCreateEdgeEvent) event ).getEndDate();
      String dataSource = ( (GeoObjectCreateEdgeEvent) event ).getDataSource();

      if (!StringUtils.isBlank(dataSource))
      {
        sources.add(dataSource);
      }

      return new RemoteGeoObjectCreateEdgeCommand(commit.getUid(), sourceCode, sourceType, edgeUid, edgeType, edgeTypeCode, startDate, endDate, targetCode, targetType, dataSource);
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

      return new RemoteBusinessObjectCommand(commit.getUid(), code, type, oJson);
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

      return new RemoteBusinessObjectAddGeoObjectCommand(commit.getUid(), code, type, edgeUid, edgeType, geoObjectType, geoObjectCode, direction, dataSource);

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

      return new RemoteBusinessObjectCreateEdgeCommand(commit.getUid(), sourceCode, sourceType, edgeUid, edgeType, targetCode, targetType, dataSource);
    }

    throw new UnsupportedOperationException("Events of type [" + event.getClass().getName() + "] do not support being published");
  }

  protected void processEventType(GapAwareTrackingToken start, GapAwareTrackingToken end, EventType eventType, Publish publish, Commit commit, PublishDTO dto, Set<String> source)
  {
    List<String> aggregateIds = this.store.getAggregateIds(start, end);

    for (String aggregateId : aggregateIds)
    {
      InMemoryEventMerger merger = new InMemoryEventMerger();
      DomainEventStream stream = this.store.readEvents(aggregateId, start, end);

      while (stream.hasNext())
      {
        DomainEventMessage<?> message = stream.next();

        Object payload = message.getPayload();

        if (payload instanceof RepositoryEvent)
        {
          RepositoryEvent event = (RepositoryEvent) payload;

          if (eventType.equals(event.getEventType()) && event.isValidFor(dto))
          {
            merger.add(event);
          }
        }
      }

      merger.buildEvents().stream().map(event -> this.build(publish, commit, event, source)).forEach(command -> {
        this.gateway.sendAndWait(command);
      });
    }

  }

}
