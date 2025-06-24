package net.geoprism.registry.axon.aggregate;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import net.geoprism.registry.axon.command.GeoObjectCompositeCreateCommand;
import net.geoprism.registry.axon.command.GeoObjectCompositeCommand;
import net.geoprism.registry.axon.command.GeoObjectCreateCommand;
import net.geoprism.registry.axon.event.GeoObjectApplyEvent;

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
  public GeoObjectAggregate(GeoObjectCreateCommand command)
  {
    RunwayTransactionWrapper.run(() -> AggregateLifecycle.apply(new GeoObjectApplyEvent(command.getUid(), command.getIsNew(), command.getIsImport(), command.getObject(), command.getParents())));
  }

  @CommandHandler
  public GeoObjectAggregate(GeoObjectCompositeCreateCommand command)    
  {
    RunwayTransactionWrapper.run(() -> command.getEvents().stream().forEach(AggregateLifecycle::apply));
  }

  @CommandHandler
  public void on(GeoObjectCompositeCommand command)
  {
    RunwayTransactionWrapper.run(() -> command.getEvents().stream().forEach(AggregateLifecycle::apply));
  }

  @EventSourcingHandler
  public void on(GeoObjectApplyEvent event)
  {
    this.uid = event.getUid();
    this.object = event.getObject();
  }

}
