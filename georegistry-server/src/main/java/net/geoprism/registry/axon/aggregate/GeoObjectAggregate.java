package net.geoprism.registry.axon.aggregate;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateCreationPolicy;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.modelling.command.CreationPolicy;
import org.axonframework.spring.stereotype.Aggregate;

import net.geoprism.registry.axon.command.remote.RemoteGeoObjectCommand;
import net.geoprism.registry.axon.command.remote.RemoteGeoObjectSetParentCommand;
import net.geoprism.registry.axon.command.repository.GeoObjectCompositeCommand;
import net.geoprism.registry.axon.command.repository.GeoObjectCompositeCreateCommand;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectEvent;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectSetParentEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectCreateParentEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectUpdateParentEvent;

@Aggregate
public class GeoObjectAggregate
{
  @AggregateIdentifier
  private String uid;

  // @JsonDeserialize(using = StringDeserializer.class)
  // @JsonSerialize(using = StringSerializer.class)
  private String object;

  public String getUid()
  {
    return uid;
  }

  public void setUid(String uid)
  {
    this.uid = uid;
  }

  public String getObject()
  {
    return object;
  }

  public void setObject(String object)
  {
    this.object = object;
  }

  public GeoObjectAggregate()
  {
  }

  @CommandHandler
  public GeoObjectAggregate(GeoObjectCompositeCreateCommand command)
  {
    RunwayTransactionWrapper.run(() -> command.getEvents().stream().forEach(AggregateLifecycle::apply));
  }

  @CommandHandler
  @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
  public void on(GeoObjectCompositeCommand command)
  {
    RunwayTransactionWrapper.run(() -> command.getEvents().stream().forEach(AggregateLifecycle::apply));
  }
  
  
  @CommandHandler
  @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
  public void on(RemoteGeoObjectCommand command)
  {
    RunwayTransactionWrapper.run(() -> AggregateLifecycle.apply(new RemoteGeoObjectEvent(command.getCommitId(), command.getUid(), command.getIsNew(), command.getObject(), command.getType(), command.getStartDate(), command.getEndDate())));
  }

  @CommandHandler
  public void on(RemoteGeoObjectSetParentCommand command)
  {
    RunwayTransactionWrapper.run(() -> AggregateLifecycle.apply(new RemoteGeoObjectSetParentEvent(command.getCommitId(), command.getUid(), command.getType(), command.getEdgeUid(), command.getEdgeType(), command.getStartDate(), command.getEndDate(), command.getParentCode(), command.getParentType())));
  }

  @EventSourcingHandler
  public void on(GeoObjectApplyEvent event)
  {
    this.uid = event.getUid();
    this.object = event.getObject();
  }

  @EventSourcingHandler
  public void on(GeoObjectUpdateParentEvent event)
  {
    this.uid = event.getUid();
  }

  @EventSourcingHandler
  public void on(GeoObjectCreateParentEvent event)
  {
    this.uid = event.getUid();
  }

  @EventSourcingHandler
  public void on(RemoteGeoObjectEvent event)
  {
    this.uid = event.getUid();
    this.object = event.getObject();
  }

  @EventSourcingHandler
  public void on(RemoteGeoObjectSetParentEvent event)
  {
    this.uid = event.getUid();
  }  
}
