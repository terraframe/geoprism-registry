package net.geoprism.registry.axon.aggregate;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateCreationPolicy;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.modelling.command.CreationPolicy;
import org.axonframework.spring.stereotype.Aggregate;

import net.geoprism.registry.axon.command.remote.RemoteBusinessObjectAddGeoObjectCommand;
import net.geoprism.registry.axon.command.remote.RemoteBusinessObjectCommand;
import net.geoprism.registry.axon.command.remote.RemoteBusinessObjectCreateEdgeCommand;
import net.geoprism.registry.axon.command.repository.BusinessObjectCompositeCommand;
import net.geoprism.registry.axon.command.repository.BusinessObjectCompositeCreateCommand;
import net.geoprism.registry.axon.event.remote.RemoteBusinessObjectAddGeoObjectEvent;
import net.geoprism.registry.axon.event.remote.RemoteBusinessObjectCreateEdgeEvent;
import net.geoprism.registry.axon.event.remote.RemoteBusinessObjectEvent;
import net.geoprism.registry.axon.event.repository.BusinessObjectApplyEvent;

@Aggregate
public class BusinessObjectAggregate
{
  @AggregateIdentifier
  private String key;

  private String type;

  private String code;

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

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getCode()
  {
    return code;
  }

  public void setCode(String code)
  {
    this.code = code;
  }

  public String getObject()
  {
    return object;
  }

  public void setObject(String object)
  {
    this.object = object;
  }

  public BusinessObjectAggregate()
  {
  }

  @CommandHandler
  public BusinessObjectAggregate(BusinessObjectCompositeCreateCommand command)
  {
    RunwayTransactionWrapper.run(() -> command.getEvents().stream().forEach(AggregateLifecycle::apply));
  }

  @CommandHandler
  @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
  public void on(BusinessObjectCompositeCommand command)
  {
    RunwayTransactionWrapper.run(() -> command.getEvents().stream().forEach(AggregateLifecycle::apply));
  }

  @CommandHandler
  @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
  public void on(RemoteBusinessObjectCommand command)
  {
    RunwayTransactionWrapper.run(() -> AggregateLifecycle.apply(new RemoteBusinessObjectEvent(command.getCommitId(), command.getKey(), command.getCode(), command.getType(), command.getObject())));
  }

  @CommandHandler
  public void on(RemoteBusinessObjectAddGeoObjectCommand command)
  {
    RunwayTransactionWrapper.run(() -> AggregateLifecycle.apply(new RemoteBusinessObjectAddGeoObjectEvent(command.getCommitId(), command.getKey(), command.getCode(), command.getType(), command.getEdgeType(), command.getGeoObjectType(), command.getGeoObjectCode(), command.getDirection(), command.getDataSource())));
  }

  @CommandHandler
  public void on(RemoteBusinessObjectCreateEdgeCommand command)
  {
    RunwayTransactionWrapper.run(() -> AggregateLifecycle.apply(new RemoteBusinessObjectCreateEdgeEvent(command.getCommitId(), command.getSourceCode(), command.getSourceType(), command.getEdgeUid(), command.getEdgeType(), command.getTargetCode(), command.getTargetType(), command.getDataSource())));
  }

  @EventSourcingHandler
  public void on(BusinessObjectApplyEvent event)
  {
    this.key = event.getKey();
    this.code = event.getCode();
    this.type = event.getType();
    this.object = event.getObject();
  }

  @EventSourcingHandler
  public void on(RemoteBusinessObjectEvent event)
  {
    this.key = event.getKey();
    this.code = event.getCode();
    this.type = event.getType();
    this.object = event.getObject();
  }

  @EventSourcingHandler
  public void on(RemoteBusinessObjectAddGeoObjectEvent event)
  {
    this.key = event.getKey();
    this.code = event.getCode();
    this.type = event.getType();
  }

  @EventSourcingHandler
  public void on(RemoteBusinessObjectCreateEdgeEvent event)
  {
    this.key = event.getKey();
    this.code = event.getSourceCode();
    this.type = event.getSourceType();
  }

}
