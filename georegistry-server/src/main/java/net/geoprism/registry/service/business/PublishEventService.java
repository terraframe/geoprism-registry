package net.geoprism.registry.service.business;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.common.stream.BlockingStream;
import org.axonframework.eventhandling.TrackedEventMessage;
import org.axonframework.eventhandling.TrackingToken;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.geoprism.registry.axon.command.remote.RemoteCommand;
import net.geoprism.registry.axon.command.remote.RemoteGeoObjectCommand;
import net.geoprism.registry.axon.command.remote.RemoteGeoObjectSetParentCommand;
import net.geoprism.registry.axon.event.repository.EventType;
import net.geoprism.registry.axon.event.repository.GeoObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectCreateParentEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectRemoveParentEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectUpdateParentEvent;
import net.geoprism.registry.axon.event.repository.InMemoryGeoObjectEventMerger;
import net.geoprism.registry.cache.ClassificationCache;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.view.EventPublishingConfiguration;

@Service
public class PublishEventService
{
  @Autowired
  private EventStore                 store;

  @Autowired
  private CommandGateway             gateway;

  @Autowired
  private GeoObjectBusinessServiceIF service;

  public void publish(EventPublishingConfiguration configuration, TrackingToken token) throws InterruptedException
  {
    TrackingToken end = this.store.createTailToken();

    processEventType(token, end, EventType.OBJECT, configuration);

    processEventType(token, end, EventType.HIERARCHY, configuration);
  }

  private RemoteCommand build(EventPublishingConfiguration configuration, GeoObjectEvent event, ClassificationCache cache)
  {
    if (event instanceof GeoObjectApplyEvent)
    {
      String oJson = ( (GeoObjectApplyEvent) event ).getObject();
      String type = ( (GeoObjectApplyEvent) event ).getType();
      Boolean isNew = ( (GeoObjectApplyEvent) event ).getIsNew();
      String uid = ( (GeoObjectApplyEvent) event ).getUid();

      GeoObjectOverTime dtoOvertTime = GeoObjectOverTime.fromJSON(ServiceFactory.getAdapter(), oJson);
      ServerGeoObjectIF geoObject = this.service.fromDTO(dtoOvertTime, isNew);

      GeoObject dto = this.service.toGeoObject(geoObject, configuration.getDate(), false, cache);

      return new RemoteGeoObjectCommand(uid, isNew, dto.toJSON().toString(), type, configuration.getStartDate(), configuration.getEndDate());
    }
    else if (event instanceof GeoObjectCreateParentEvent)
    {
      String uid = ( (GeoObjectCreateParentEvent) event ).getUid();
      String type = ( (GeoObjectCreateParentEvent) event ).getType();
      String edgeUid = ( (GeoObjectCreateParentEvent) event ).getEdgeUid();
      String edgeType = ( (GeoObjectCreateParentEvent) event ).getEdgeType();
      String parentType = ( (GeoObjectCreateParentEvent) event ).getParentType();
      String parentCode = ( (GeoObjectCreateParentEvent) event ).getParentCode();

      return new RemoteGeoObjectSetParentCommand(uid, type, edgeUid, edgeType, configuration.getStartDate(), configuration.getEndDate(), parentCode, parentType);
    }
    else if (event instanceof GeoObjectUpdateParentEvent)
    {
      String uid = ( (GeoObjectUpdateParentEvent) event ).getUid();
      String type = ( (GeoObjectUpdateParentEvent) event ).getType();
      String edgeUid = ( (GeoObjectUpdateParentEvent) event ).getEdgeUid();
      String edgeType = ( (GeoObjectUpdateParentEvent) event ).getEdgeType();
      String parentType = ( (GeoObjectUpdateParentEvent) event ).getParentType();
      String parentCode = ( (GeoObjectUpdateParentEvent) event ).getParentCode();

      return new RemoteGeoObjectSetParentCommand(uid, type, edgeUid, edgeType, configuration.getStartDate(), configuration.getEndDate(), parentCode, parentType);
    }
    else if (event instanceof GeoObjectRemoveParentEvent)
    {
      String uid = ( (GeoObjectRemoveParentEvent) event ).getUid();
      String type = ( (GeoObjectRemoveParentEvent) event ).getType();
      String edgeUid = ( (GeoObjectRemoveParentEvent) event ).getEdgeUid();
      String edgeType = ( (GeoObjectRemoveParentEvent) event ).getEdgeType();

      return new RemoteGeoObjectSetParentCommand(uid, type, edgeUid, edgeType, configuration.getStartDate(), configuration.getEndDate(), null, null);
    }

    throw new UnsupportedOperationException("Events of type [" + event.getClass().getName() + "] do no support being published");
  }

  protected void processEventType(TrackingToken start, TrackingToken end, EventType eventType, EventPublishingConfiguration configuration) throws InterruptedException
  {
    ClassificationCache cache = new ClassificationCache();
    InMemoryGeoObjectEventMerger merger = new InMemoryGeoObjectEventMerger();

    try (BlockingStream<TrackedEventMessage<?>> stream = this.store.openStream(start))
    {
      while (stream.hasNextAvailable())
      {
        TrackedEventMessage<?> message = stream.nextAvailable();
        Object payload = message.getPayload();

        if (payload instanceof GeoObjectEvent)
        {
          GeoObjectEvent event = (GeoObjectEvent) payload;

          if (eventType.equals(event.getEventType()) && event.isValidFor(configuration.getDate()))
          {
            merger.add(event);
          }
        }
      }
    }

    merger.merge().stream().map(event -> this.build(configuration, event, cache)).forEach(command -> {
      this.gateway.sendAndWait(command);
    });

  }

}
