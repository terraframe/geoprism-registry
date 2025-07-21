package net.geoprism.registry.service.business;

import java.util.LinkedList;
import java.util.List;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.eventhandling.GapAwareTrackingToken;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;

import net.geoprism.registry.Commit;
import net.geoprism.registry.Publish;
import net.geoprism.registry.axon.command.remote.RemoteBusinessObjectAddGeoObjectCommand;
import net.geoprism.registry.axon.command.remote.RemoteBusinessObjectCommand;
import net.geoprism.registry.axon.command.remote.RemoteCommand;
import net.geoprism.registry.axon.command.remote.RemoteGeoObjectCommand;
import net.geoprism.registry.axon.command.remote.RemoteGeoObjectSetParentCommand;
import net.geoprism.registry.axon.config.RegistryEventStore;
import net.geoprism.registry.axon.event.repository.BusinessObjectAddGeoObjectEvent;
import net.geoprism.registry.axon.event.repository.BusinessObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.EventType;
import net.geoprism.registry.axon.event.repository.GeoObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectCreateParentEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectRemoveParentEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectUpdateParentEvent;
import net.geoprism.registry.axon.event.repository.InMemoryEventMerger;
import net.geoprism.registry.axon.event.repository.RepositoryEvent;
import net.geoprism.registry.cache.ClassificationCache;
import net.geoprism.registry.model.EdgeDirection;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.view.PublishDTO;

@Service
public class PublishEventService
{
  @Autowired
  private RegistryEventStore         store;

  @Autowired
  private CommandGateway             gateway;

  @Autowired
  private GeoObjectBusinessServiceIF service;

  @Autowired
  private PublishBusinessServiceIF   publishService;

  @Autowired
  private CommitBusinessServiceIF    commitService;

  @Transaction
  public Publish publish(PublishDTO configuration) throws InterruptedException
  {
    Publish publish = this.publishService.create(configuration);

    Commit previous = this.commitService.getMostRecentCommit(publish);

    Long lastSequenceNumber = previous != null ? previous.getLastSequenceNumber() : 0;
    Integer versionNumber = previous != null ? previous.getVersionNumber() : 1;

    GapAwareTrackingToken start = new GapAwareTrackingToken(lastSequenceNumber, new LinkedList<>());

    publish(publish, start, versionNumber);

    return publish;
  }

  protected Commit publish(Publish publish, GapAwareTrackingToken start, Integer versionNumber)
  {
    PublishDTO dto = publish.toDTO();

    GapAwareTrackingToken end = (GapAwareTrackingToken) this.store.createHeadToken();

    if (end != null && start.getIndex() < end.getIndex())
    {
      Commit commit = this.commitService.create(publish, versionNumber, end.getIndex());

      processEventType(start, end, EventType.OBJECT, publish, commit, dto);

      processEventType(start, end, EventType.HIERARCHY, publish, commit, dto);

      return commit;
    }

    throw new ProgrammingErrorException("Unable to publish events because no events exist");
  }

  private RemoteCommand build(Publish publish, Commit commit, RepositoryEvent event, ClassificationCache cache)
  {
    if (event instanceof GeoObjectApplyEvent)
    {
      String oJson = ( (GeoObjectApplyEvent) event ).getObject();
      String type = ( (GeoObjectApplyEvent) event ).getType();
      Boolean isNew = ( (GeoObjectApplyEvent) event ).getIsNew();
      String code = ( (GeoObjectApplyEvent) event ).getCode();

      GeoObjectOverTime dtoOvertTime = GeoObjectOverTime.fromJSON(ServiceFactory.getAdapter(), oJson);
      ServerGeoObjectIF geoObject = this.service.fromDTO(dtoOvertTime, isNew);

      GeoObject dto = this.service.toGeoObject(geoObject, publish.getForDate(), false, cache);

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

      return new RemoteGeoObjectSetParentCommand(commit.getUid(), code, type, edgeUid, edgeType, publish.getStartDate(), publish.getEndDate(), parentCode, parentType);
    }
    else if (event instanceof GeoObjectUpdateParentEvent)
    {
      String code = ( (GeoObjectUpdateParentEvent) event ).getCode();
      String type = ( (GeoObjectUpdateParentEvent) event ).getType();
      String edgeUid = ( (GeoObjectUpdateParentEvent) event ).getEdgeUid();
      String edgeType = ( (GeoObjectUpdateParentEvent) event ).getEdgeType();
      String parentType = ( (GeoObjectUpdateParentEvent) event ).getParentType();
      String parentCode = ( (GeoObjectUpdateParentEvent) event ).getParentCode();

      return new RemoteGeoObjectSetParentCommand(commit.getUid(), code, type, edgeUid, edgeType, publish.getStartDate(), publish.getEndDate(), parentCode, parentType);
    }
    else if (event instanceof GeoObjectRemoveParentEvent)
    {
      String code = ( (GeoObjectRemoveParentEvent) event ).getCode();
      String type = ( (GeoObjectRemoveParentEvent) event ).getType();
      String edgeUid = ( (GeoObjectRemoveParentEvent) event ).getEdgeUid();
      String edgeType = ( (GeoObjectRemoveParentEvent) event ).getEdgeType();

      return new RemoteGeoObjectSetParentCommand(commit.getUid(), code, type, edgeUid, edgeType, publish.getStartDate(), publish.getEndDate(), null, null);
    }
    else if (event instanceof BusinessObjectApplyEvent)
    {
      String oJson = ( (BusinessObjectApplyEvent) event ).getObject();
      String code = ( (BusinessObjectApplyEvent) event ).getCode();
      String type = ( (BusinessObjectApplyEvent) event ).getType();

      return new RemoteBusinessObjectCommand(commit.getUid(), code, type, oJson);
    }
    else if (event instanceof BusinessObjectAddGeoObjectEvent)
    {
      String code = ( (BusinessObjectAddGeoObjectEvent) event ).getCode();
      String type = ( (BusinessObjectAddGeoObjectEvent) event ).getType();
      String edgeType = ( (BusinessObjectAddGeoObjectEvent) event ).getEdgeType();
      String geoObjectType = ( (BusinessObjectAddGeoObjectEvent) event ).getGeoObjectType();
      String geoObjectCode = ( (BusinessObjectAddGeoObjectEvent) event ).getGeoObjectCode();
      EdgeDirection direction = ( (BusinessObjectAddGeoObjectEvent) event ).getDirection();

      return new RemoteBusinessObjectAddGeoObjectCommand(commit.getUid(), code, type, edgeType, geoObjectType, geoObjectCode, direction);

    }

    throw new UnsupportedOperationException("Events of type [" + event.getClass().getName() + "] do no support being published");
  }

  protected void processEventType(GapAwareTrackingToken start, GapAwareTrackingToken end, EventType eventType, Publish publish, Commit commit, PublishDTO dto)
  {
    long firstSequenceNumber = start != null ? start.getIndex() : 0;

    ClassificationCache cache = new ClassificationCache();

    List<String> aggregateIds = this.store.getAggregateIds(start, end);

    for (String aggregateId : aggregateIds)
    {
      InMemoryEventMerger merger = new InMemoryEventMerger();
      DomainEventStream stream = this.store.readEvents(aggregateId, firstSequenceNumber);

      while (stream.hasNext())
      {
        DomainEventMessage<?> message = stream.next();

        if (message.getSequenceNumber() < end.getIndex())
        {
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
      }

      merger.buildEvents().stream().map(event -> this.build(publish, commit, event, cache)).forEach(command -> {
        this.gateway.sendAndWait(command);
      });
    }

  }

}
