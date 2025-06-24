package net.geoprism.registry.axon.command;

import java.util.List;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import net.geoprism.registry.axon.event.BusinessObjectEvent;

public class BusinessObjectCompositeCommand
{
  @TargetAggregateIdentifier
  private String                    key;

  private String                    code;

  private String                    type;

  private List<BusinessObjectEvent> events;

  public BusinessObjectCompositeCommand()
  {
    super();
  }

  public BusinessObjectCompositeCommand(String code, String type, List<BusinessObjectEvent> events)
  {
    super();
    this.key = code + "#" + type;

    this.code = code;
    this.type = type;
    this.events = events;
  }

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

  public List<BusinessObjectEvent> getEvents()
  {
    return events;
  }

  public void setEvents(List<BusinessObjectEvent> events)
  {
    this.events = events;
  }

}
