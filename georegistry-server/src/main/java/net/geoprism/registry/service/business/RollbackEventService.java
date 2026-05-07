package net.geoprism.registry.service.business;

import java.util.LinkedList;
import java.util.List;

import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.eventhandling.GapAwareTrackingToken;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.dataaccess.transaction.Transaction;

import net.geoprism.registry.axon.config.RegistryEventStore;
import net.geoprism.registry.axon.event.repository.AbstractBusinessObjectEdgeEvent;
import net.geoprism.registry.axon.event.repository.AbstractGeoObjectEdgeEvent;
import net.geoprism.registry.axon.event.repository.BusinessObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.EventPhase;
import net.geoprism.registry.axon.event.repository.GeoObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectCreateParentEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectRemoveParentEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectUpdateParentEvent;
import net.geoprism.registry.axon.event.repository.RemoveBusinessObjectEdgeEvent;
import net.geoprism.registry.axon.event.repository.RemoveBusinessObjectEvent;
import net.geoprism.registry.axon.event.repository.RemoveGeoObjectEdgeEvent;
import net.geoprism.registry.axon.event.repository.RemoveGeoObjectEvent;
import net.geoprism.registry.axon.event.repository.RepositoryEvent;
import net.geoprism.registry.axon.event.rollback.RollbackBusinessObjectEdgeEventBuilder;
import net.geoprism.registry.axon.event.rollback.RollbackBusinessObjectEventBuilder;
import net.geoprism.registry.axon.event.rollback.RollbackEventBuilder;
import net.geoprism.registry.axon.event.rollback.RollbackGeoObjectEdgeEventBuilder;
import net.geoprism.registry.axon.event.rollback.RollbackGeoObjectEventBuilder;
import net.geoprism.registry.axon.projection.RepositoryProjection;
import net.geoprism.registry.view.RollbackDTO;

@Service
public class RollbackEventService
{
  @Autowired
  private RegistryEventStore   store;

  @Autowired
  private RepositoryProjection projection;

  @Transaction
  public void rollback(RollbackDTO configuration)
  {
    GapAwareTrackingToken start = GapAwareTrackingToken.newInstance(configuration.getStartIndex(), new LinkedList<>());

    processEventType(start, EventPhase.EDGE);

    processEventType(start, EventPhase.OBJECT);
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

    builder.build().forEach(event -> {
      if (event instanceof GeoObjectApplyEvent)
      {
        this.projection.handleApplyGeoObject((GeoObjectApplyEvent) event);
      }
      else if (event instanceof RemoveGeoObjectEvent)
      {
        this.projection.handleRemoveGeoObjectEvent((RemoveGeoObjectEvent) event);
      }
      else if (event instanceof RemoveGeoObjectEdgeEvent)
      {
        this.projection.handleRemoveGeoObjectEvent((RemoveGeoObjectEdgeEvent) event);
      }
      else if (event instanceof GeoObjectCreateParentEvent)
      {
        this.projection.handleCreateParent((GeoObjectCreateParentEvent) event);
      }
      else if (event instanceof GeoObjectRemoveParentEvent)
      {
        this.projection.handleRemoveParent((GeoObjectRemoveParentEvent) event);
      }
      else if (event instanceof GeoObjectUpdateParentEvent)
      {
        this.projection.handleUpdateParent((GeoObjectUpdateParentEvent) event);
      }
      else if (event instanceof BusinessObjectApplyEvent)
      {
        this.projection.handleApplyBusinessObject((BusinessObjectApplyEvent) event);
      }
      else if (event instanceof RemoveBusinessObjectEvent)
      {
        this.projection.handleRemoveBusinessObjectEvent((RemoveBusinessObjectEvent) event);
      }
      else if (event instanceof RemoveBusinessObjectEdgeEvent)
      {
        this.projection.handleRemoveBusinessObjectEvent((RemoveBusinessObjectEdgeEvent) event);
      }
      else
      {
        throw new UnsupportedOperationException("Events of type [" + event.getClass().getName() + "] do not support being rolledback");
      }
    });
  }

  protected void processEventType(GapAwareTrackingToken start, EventPhase phase)
  {
    long limit = 1000;
    long offset = 0;

    List<String> baseObjectIds = null;

    while ( ( baseObjectIds = this.store.getBaseObjectIds(start, null, phase, limit, offset) ).size() > 0)
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

        offset += limit;
      }
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
    else if (event instanceof AbstractGeoObjectEdgeEvent && phase.equals(EventPhase.EDGE))
    {
      return new RollbackGeoObjectEdgeEventBuilder((AbstractGeoObjectEdgeEvent) event);
    }
    else if (event instanceof AbstractBusinessObjectEdgeEvent && phase.equals(EventPhase.EDGE))
    {
      return new RollbackBusinessObjectEdgeEventBuilder((AbstractBusinessObjectEdgeEvent) event);
    }

    throw new UnsupportedOperationException();
  }

}
