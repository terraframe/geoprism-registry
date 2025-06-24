package net.geoprism.registry.axon.aggregate;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import net.geoprism.registry.axon.command.BusinessObjectCompositeCommand;
import net.geoprism.registry.axon.command.BusinessObjectCompositeCreateCommand;
import net.geoprism.registry.axon.event.BusinessObjectApplyEvent;

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

  @CommandHandler
  public BusinessObjectAggregate(BusinessObjectCompositeCreateCommand command)
  {
    RunwayTransactionWrapper.run(() -> command.getEvents().stream().forEach(AggregateLifecycle::apply));
  }

  @CommandHandler
  public void on(BusinessObjectCompositeCommand command)
  {
    RunwayTransactionWrapper.run(() -> command.getEvents().stream().forEach(AggregateLifecycle::apply));
  }

  @EventSourcingHandler
  public void on(BusinessObjectApplyEvent event)
  {
    this.key = event.getKey();
    this.code = event.getCode();
    this.type = event.getType();
    this.object = event.getObject();
  }

}
