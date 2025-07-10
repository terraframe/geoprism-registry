package net.geoprism.registry.service.business;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.eventhandling.GapAwareTrackingToken;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.dataaccess.transaction.Transaction;

import net.geoprism.registry.Commit;
import net.geoprism.registry.Publish;
import net.geoprism.registry.axon.command.remote.RemoteCommand;
import net.geoprism.registry.axon.command.remote.RemoteGeoObjectCommand;
import net.geoprism.registry.axon.command.remote.RemoteGeoObjectSetParentCommand;
import net.geoprism.registry.axon.event.repository.EventType;
import net.geoprism.registry.axon.event.repository.GeoObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectCreateParentEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectRemoveParentEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectUpdateParentEvent;
import net.geoprism.registry.axon.event.repository.InMemoryEventMerger;
import net.geoprism.registry.cache.ClassificationCache;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.view.EventPublishingConfiguration;

@Service
public class PublishEventService
{
  public static final String         DOMAIN_EVENT_ENTRY_TABLE = "domainevententry";

  @Autowired
  private EventStore                 store;

  @Autowired
  private CommandGateway             gateway;

  @Autowired
  private GeoObjectBusinessServiceIF service;

  @Autowired
  private PublishBusinessServiceIF   publishService;

  @Autowired
  private CommitBusinessServiceIF    commitService;

  @Transaction
  public Publish publish(EventPublishingConfiguration configuration) throws InterruptedException
  {
    Publish publish = this.publishService.create(configuration);

    Commit previous = this.publishService.getMostRecentCommit(publish);

    Long lastSequenceNumber = previous != null ? previous.getLastSequenceNumber() : 0;
    Integer versionNumber = previous != null ? previous.getVersionNumber() : 1;

    GapAwareTrackingToken start = new GapAwareTrackingToken(lastSequenceNumber, new LinkedList<>());

    publish(publish, configuration, start, versionNumber);

    return publish;
  }

  protected Commit publish(Publish publish, EventPublishingConfiguration configuration, GapAwareTrackingToken start, Integer versionNumber)
  {
    GapAwareTrackingToken end = (GapAwareTrackingToken) this.store.createHeadToken();

    if (end != null && start.getIndex() < end.getIndex())
    {
      Commit commit = this.commitService.create(publish, configuration, versionNumber, end.getIndex());

      processEventType(start, end, EventType.OBJECT, publish, commit);

      processEventType(start, end, EventType.HIERARCHY, publish, commit);

      return commit;
    }

    throw new ProgrammingErrorException("Unable to publish events because no events exist");
  }

  private RemoteCommand build(Publish publish, Commit commit, GeoObjectEvent event, ClassificationCache cache)
  {
    if (event instanceof GeoObjectApplyEvent)
    {
      String oJson = ( (GeoObjectApplyEvent) event ).getObject();
      String type = ( (GeoObjectApplyEvent) event ).getType();
      Boolean isNew = ( (GeoObjectApplyEvent) event ).getIsNew();
      String uid = ( (GeoObjectApplyEvent) event ).getUid();

      GeoObjectOverTime dtoOvertTime = GeoObjectOverTime.fromJSON(ServiceFactory.getAdapter(), oJson);
      ServerGeoObjectIF geoObject = this.service.fromDTO(dtoOvertTime, isNew);

      GeoObject dto = this.service.toGeoObject(geoObject, publish.getForDate(), false, cache);

      return new RemoteGeoObjectCommand(commit.getUid(), uid, isNew, dto.toJSON().toString(), type, publish.getStartDate(), publish.getEndDate());
    }
    else if (event instanceof GeoObjectCreateParentEvent)
    {
      String uid = ( (GeoObjectCreateParentEvent) event ).getUid();
      String type = ( (GeoObjectCreateParentEvent) event ).getType();
      String edgeUid = ( (GeoObjectCreateParentEvent) event ).getEdgeUid();
      String edgeType = ( (GeoObjectCreateParentEvent) event ).getEdgeType();
      String parentType = ( (GeoObjectCreateParentEvent) event ).getParentType();
      String parentCode = ( (GeoObjectCreateParentEvent) event ).getParentCode();

      return new RemoteGeoObjectSetParentCommand(commit.getUid(), uid, type, edgeUid, edgeType, publish.getStartDate(), publish.getEndDate(), parentCode, parentType);
    }
    else if (event instanceof GeoObjectUpdateParentEvent)
    {
      String uid = ( (GeoObjectUpdateParentEvent) event ).getUid();
      String type = ( (GeoObjectUpdateParentEvent) event ).getType();
      String edgeUid = ( (GeoObjectUpdateParentEvent) event ).getEdgeUid();
      String edgeType = ( (GeoObjectUpdateParentEvent) event ).getEdgeType();
      String parentType = ( (GeoObjectUpdateParentEvent) event ).getParentType();
      String parentCode = ( (GeoObjectUpdateParentEvent) event ).getParentCode();

      return new RemoteGeoObjectSetParentCommand(commit.getUid(), uid, type, edgeUid, edgeType, publish.getStartDate(), publish.getEndDate(), parentCode, parentType);
    }
    else if (event instanceof GeoObjectRemoveParentEvent)
    {
      String uid = ( (GeoObjectRemoveParentEvent) event ).getUid();
      String type = ( (GeoObjectRemoveParentEvent) event ).getType();
      String edgeUid = ( (GeoObjectRemoveParentEvent) event ).getEdgeUid();
      String edgeType = ( (GeoObjectRemoveParentEvent) event ).getEdgeType();

      return new RemoteGeoObjectSetParentCommand(commit.getUid(), uid, type, edgeUid, edgeType, publish.getStartDate(), publish.getEndDate(), null, null);
    }

    throw new UnsupportedOperationException("Events of type [" + event.getClass().getName() + "] do no support being published");
  }

  public List<String> getAggregateIds(GapAwareTrackingToken start, GapAwareTrackingToken end)
  {
    LinkedList<String> aggregateIds = new LinkedList<>();

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT DISTINCT aggregateidentifier");
    statement.append(" FROM " + PublishEventService.DOMAIN_EVENT_ENTRY_TABLE);
    statement.append(" WHERE globalindex < " + end.getIndex());

    if (start != null)
    {
      statement.append(" AND globalindex >= " + start.getIndex());
    }

    try (ResultSet resultSet = Database.query(statement.toString()))
    {
      while (resultSet.next())
      {
        aggregateIds.add(resultSet.getString(1));
      }
    }
    catch (SQLException e)
    {
      throw new ProgrammingErrorException(e);
    }

    return aggregateIds;
  }

  protected void processEventType(GapAwareTrackingToken start, GapAwareTrackingToken end, EventType eventType, Publish publish, Commit commit)
  {
    long firstSequenceNumber = start != null ? start.getIndex() : 0;

    ClassificationCache cache = new ClassificationCache();

    List<String> aggregateIds = this.getAggregateIds(start, end);

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

          if (payload instanceof GeoObjectEvent)
          {
            GeoObjectEvent event = (GeoObjectEvent) payload;

            if (eventType.equals(event.getEventType()) && event.isValidFor(publish.getForDate()))
            {
              merger.add(event);
            }
          }
        }
      }

      /*
       * try (BlockingStream<TrackedEventMessage<?>> stream =
       * this.store.readEvents(aggregateId)) { while (stream.hasNextAvailable())
       * { TrackedEventMessage<?> message = stream.nextAvailable(); Object
       * payload = message.getPayload();
       * 
       * if (payload instanceof GeoObjectEvent) { GeoObjectEvent event =
       * (GeoObjectEvent) payload;
       * 
       * if (eventType.equals(event.getEventType()) &&
       * event.isValidFor(configuration.getDate())) { merger.add(event); } } } }
       */
      merger.merge().stream().map(event -> this.build(publish, commit, event, cache)).forEach(command -> {
        this.gateway.sendAndWait(command);
      });
    }

  }

}
