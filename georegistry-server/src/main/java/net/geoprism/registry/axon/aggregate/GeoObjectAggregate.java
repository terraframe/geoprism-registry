package net.geoprism.registry.axon.aggregate;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import net.geoprism.registry.axon.command.CreateGeoObjectCommand;
import net.geoprism.registry.axon.command.UpdateGeoObjectCommand;
import net.geoprism.registry.axon.event.ApplyGeoObjectEvent;
import net.geoprism.registry.axon.event.CreateGeoObjectEvent;
import net.geoprism.registry.axon.event.UpdateGeoObjectEvent;

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
  public GeoObjectAggregate(CreateGeoObjectCommand command)
  {
    RunwayTransactionWrapper.run(() -> AggregateLifecycle.apply(new ApplyGeoObjectEvent(command.getUid(), command.getIsNew(), command.getIsImport(), command.getObject(), command.getParents())));
  }

  @CommandHandler
  public void on(UpdateGeoObjectCommand command)
  {
    RunwayTransactionWrapper.run(() -> AggregateLifecycle.apply(new ApplyGeoObjectEvent(command.getUid(), command.getIsNew(), command.getIsImport(), command.getObject(), command.getParents())));
  }

  @EventSourcingHandler
  public void on(ApplyGeoObjectEvent event)
  {
    this.uid = event.getUid();
    this.object = event.getObject();
  }

}
