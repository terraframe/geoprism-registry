package net.geoprism.registry.axon.aggregate;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateCreationPolicy;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.modelling.command.CreationPolicy;
import org.axonframework.spring.stereotype.Aggregate;

import net.geoprism.registry.axon.command.remote.RemoteGeoObjectCommand;
import net.geoprism.registry.axon.command.remote.RemoteGeoObjectCreateEdgeCommand;
import net.geoprism.registry.axon.command.remote.RemoteGeoObjectSetParentCommand;
import net.geoprism.registry.axon.command.repository.GeoObjectCompositeCommand;
import net.geoprism.registry.axon.command.repository.GeoObjectCompositeCreateCommand;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectCreateEdgeEvent;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectEvent;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectSetParentEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectCreateEdgeEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectCreateParentEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectUpdateParentEvent;

@Aggregate
public class GeoObjectAggregate
{
  @AggregateIdentifier
  private String key;

  private String code;

  private String type;

  // @JsonDeserialize(using = StringDeserializer.class)
  // @JsonSerialize(using = StringSerializer.class)
  private String object;

  public String getKey()
  {
    return key;
  }

  public void setKey(String key)
  {
    this.key = key;
  }

  public String getCode()
  {
    return code;
  }

  public void setCode(String code)
  {
    this.code = code;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
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
    RunwayTransactionWrapper.run(() -> AggregateLifecycle.apply(new RemoteGeoObjectEvent(command.getCommitId(), command.getCode(), command.getIsNew(), command.getObject(), command.getType(), command.getStartDate(), command.getEndDate())));
  }

  @CommandHandler
  public void on(RemoteGeoObjectSetParentCommand command)
  {
    RunwayTransactionWrapper.run(() -> AggregateLifecycle.apply(new RemoteGeoObjectSetParentEvent(command.getCommitId(), command.getCode(), command.getType(), command.getEdgeUid(), command.getEdgeType(), command.getStartDate(), command.getEndDate(), command.getParentCode(), command.getParentType())));
  }

  @CommandHandler
  public void on(RemoteGeoObjectCreateEdgeCommand command)
  {
    RunwayTransactionWrapper.run(() -> AggregateLifecycle.apply(new RemoteGeoObjectCreateEdgeEvent(command.getCommitId(), command.getSourceCode(), command.getSourceType(), command.getEdgeUid(), command.getEdgeType(), command.getEdgeTypeCode(), command.getStartDate(), command.getEndDate(), command.getTargetCode(), command.getTargetType())));
  }

  @EventSourcingHandler
  public void on(GeoObjectApplyEvent event)
  {
    this.key = event.getCode() + "#" + event.getType();
    this.code = event.getCode();
    this.type = event.getType();
    this.object = event.getObject();
  }

  @EventSourcingHandler
  public void on(GeoObjectUpdateParentEvent event)
  {
    this.key = event.getCode() + "#" + event.getType();
    this.code = event.getCode();
    this.type = event.getType();
  }

  @EventSourcingHandler
  public void on(GeoObjectCreateParentEvent event)
  {
    this.key = event.getCode() + "#" + event.getType();
    this.code = event.getCode();
    this.type = event.getType();
  }

  @EventSourcingHandler
  public void on(GeoObjectCreateEdgeEvent event)
  {
    this.key = event.getSourceCode() + "#" + event.getSourceType();
    this.code = event.getSourceCode();
    this.type = event.getSourceType();
  }

  @EventSourcingHandler
  public void on(RemoteGeoObjectEvent event)
  {
    this.key = event.getCode() + "#" + event.getType();
    this.code = event.getCode();
    this.type = event.getType();
    this.object = event.getObject();
  }

  @EventSourcingHandler
  public void on(RemoteGeoObjectSetParentEvent event)
  {
    this.key = event.getCode() + "#" + event.getType();
    this.code = event.getCode();
    this.type = event.getType();
  }

  @EventSourcingHandler
  public void on(RemoteGeoObjectCreateEdgeEvent event)
  {
    this.key = event.getSourceCode() + "#" + event.getSourceType();
    this.code = event.getSourceCode();
    this.type = event.getSourceType();
  }
}
