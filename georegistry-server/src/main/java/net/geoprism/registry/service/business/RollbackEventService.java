package net.geoprism.registry.service.business;

import java.util.LinkedList;
import java.util.List;

import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.eventhandling.GapAwareTrackingToken;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import net.geoprism.registry.RollbackCheckpoint;
import net.geoprism.registry.axon.config.RegistryEventStore;
import net.geoprism.registry.axon.event.repository.AbstractGeoObjectEdgeEvent;
import net.geoprism.registry.axon.event.repository.BusinessObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.ConceptObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.EventPhase;
import net.geoprism.registry.axon.event.repository.GeoObjectApplyEdgeEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectCreateParentEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectRemoveParentEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectUpdateParentEvent;
import net.geoprism.registry.axon.event.repository.ObjectApplyEdgeEvent;
import net.geoprism.registry.axon.event.repository.ObjectEdgeEventIF;
import net.geoprism.registry.axon.event.repository.ObjectRemoveEdgeEvent;
import net.geoprism.registry.axon.event.repository.RemoveGeoObjectEdgeEvent;
import net.geoprism.registry.axon.event.repository.RemoveGeoObjectEvent;
import net.geoprism.registry.axon.event.repository.RemoveObjectEvent;
import net.geoprism.registry.axon.event.repository.RepositoryEvent;
import net.geoprism.registry.axon.event.rollback.RollbackBusinessObjectEventBuilder;
import net.geoprism.registry.axon.event.rollback.RollbackConceptObjectEventBuilder;
import net.geoprism.registry.axon.event.rollback.RollbackEventBuilder;
import net.geoprism.registry.axon.event.rollback.RollbackGeoObjectEdgeEventBuilder;
import net.geoprism.registry.axon.event.rollback.RollbackGeoObjectEventBuilder;
import net.geoprism.registry.axon.event.rollback.RollbackObjectEdgeEventBuilder;
import net.geoprism.registry.axon.projection.RepositoryProjection;
import net.geoprism.registry.cache.ClearObjectCacheEvent;

@Service
public class RollbackEventService
{
  private static Logger             logger         = LoggerFactory.getLogger(RollbackEventService.class);

  public static final int           ROLLBACK_CHUNK = 1000;

  @Autowired
  private ApplicationEventPublisher publisher;

  @Autowired
  private RegistryEventStore        store;

  @Autowired
  private RepositoryProjection      projection;

  public void rollback(RollbackCheckpoint checkpoint)
  {
    try
    {
      logger.info("Rolling back checkpoint [" + checkpoint.getOid() + "] to event index [" + checkpoint.getGlobalIndex() + "] ");

      store.setLock(true);

      GapAwareTrackingToken start = GapAwareTrackingToken.newInstance(checkpoint.getGlobalIndex(), new LinkedList<>());

      processEventType(start, EventPhase.EDGE);

      publisher.publishEvent(new ClearObjectCacheEvent(this));

      processEventType(start, EventPhase.OBJECT);

      publisher.publishEvent(new ClearObjectCacheEvent(this));

      // Delete the events
      store.delete(checkpoint.getGlobalIndex());

      logger.info("Finished rolling back checkpoint: " + checkpoint.getOid());
    }
    finally
    {
      store.setLock(false);
    }

  }

  private void rollback(RepositoryEvent original, GapAwareTrackingToken start, EventPhase phase)
  {
    RollbackEventBuilder builder = this.get(original, phase);
    DomainEventStream stream = this.store.readEvents(original.getBaseObjectId(), (GapAwareTrackingToken) this.store.createTailToken(), start);

    while (stream.hasNext())
    {
      DomainEventMessage<?> message = stream.next();

      Object payload = message.getPayload();

      if (payload instanceof RepositoryEvent)
      {
        RepositoryEvent previous = (RepositoryEvent) payload;

        if (phase.equals(previous.getEventPhase()))
        {
          builder.addEvent(previous);
        }
      }
    }

    builder.build().forEach(this::replay);
  }

  public void replay(RepositoryEvent event)
  {
    if (event instanceof GeoObjectApplyEvent e)
    {
      this.projection.handleApplyGeoObject(e);
    }
    else if (event instanceof RemoveGeoObjectEvent e)
    {
      this.projection.handleRemoveGeoObjectEvent(e);
    }
    else if (event instanceof RemoveGeoObjectEdgeEvent e)
    {
      this.projection.handleRemoveGeoObjectEdgeEvent(e);
    }
    else if (event instanceof GeoObjectCreateParentEvent e)
    {
      this.projection.handleCreateParent(e);
    }
    else if (event instanceof GeoObjectRemoveParentEvent e)
    {
      this.projection.handleRemoveParent(e);
    }
    else if (event instanceof GeoObjectUpdateParentEvent e)
    {
      this.projection.handleUpdateParent(e);
    }
    else if (event instanceof BusinessObjectApplyEvent e)
    {
      this.projection.handleApplyBusinessObject(e);
    }
    else if (event instanceof ConceptObjectApplyEvent e)
    {
      this.projection.handleApplyConceptObject(e);
    }
    else if (event instanceof RemoveObjectEvent e)
    {
      this.projection.handleRemoveObjectEvent(e);
    }
    else if (event instanceof ObjectRemoveEdgeEvent e)
    {
      this.projection.handleObjectRemoveEdge(e);
    }
    else if (event instanceof ObjectApplyEdgeEvent e)
    {
      this.projection.handleObjectApplyEdge(e);
    }
    else if (event instanceof GeoObjectApplyEdgeEvent e)
    {
      this.projection.handleGeoObjectApplyEdge(e);
    }
    else
    {
      throw new UnsupportedOperationException("Events of type [" + event.getClass().getName() + "] do not support being replayed");
    }
  }

  protected void processEventType(GapAwareTrackingToken start, EventPhase phase)
  {
    long offset = 0;

    List<String> baseObjectIds = null;

    while ( ( baseObjectIds = this.store.getBaseObjectIds(start, null, phase, ROLLBACK_CHUNK, offset) ).size() > 0)
    {
      for (String baseObjectId : baseObjectIds)
      {
        LinkedList<RepositoryEvent> events = new LinkedList<>();

        DomainEventStream stream = this.store.readEvents(baseObjectId, start, null);

        while (stream.hasNext())
        {
          DomainEventMessage<?> message = stream.next();

          Object payload = message.getPayload();

          if (payload instanceof RepositoryEvent)
          {
            RepositoryEvent event = (RepositoryEvent) payload;

            if (phase.equals(event.getEventPhase()))
            {
              events.add(event);
            }
          }

          events.stream() //
              .findFirst() // TODO: merge the events?
              .ifPresent(event -> this.rollback(event, start, phase));
        }
      }

      offset += ROLLBACK_CHUNK;
    }

  }

  public RollbackEventBuilder get(RepositoryEvent event, EventPhase phase)
  {
    if (event instanceof GeoObjectApplyEvent && phase.equals(EventPhase.OBJECT))
    {
      return new RollbackGeoObjectEventBuilder((GeoObjectApplyEvent) event);
    }
    else if (event instanceof BusinessObjectApplyEvent && phase.equals(EventPhase.OBJECT))
    {
      return new RollbackBusinessObjectEventBuilder((BusinessObjectApplyEvent) event);
    }
    else if (event instanceof ConceptObjectApplyEvent && phase.equals(EventPhase.OBJECT))
    {
      return new RollbackConceptObjectEventBuilder((ConceptObjectApplyEvent) event);
    }
    else if (event instanceof AbstractGeoObjectEdgeEvent && phase.equals(EventPhase.EDGE))
    {
      return new RollbackGeoObjectEdgeEventBuilder((AbstractGeoObjectEdgeEvent) event);
    }
    else if (event instanceof ObjectEdgeEventIF && phase.equals(EventPhase.EDGE))
    {
      return new RollbackObjectEdgeEventBuilder((ObjectEdgeEventIF) event);
    }

    throw new UnsupportedOperationException("Event type cannot be rolled back [" + event.getClass().getTypeName() + "]");
  }

}
