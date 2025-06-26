package net.geoprism.registry.axon.command.repository;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public abstract class BusinessObjectApplyCommand
{

  @TargetAggregateIdentifier
  private String  key;

  private String  code;

  private String  type;

  private Boolean isNew;

  private Boolean isImport;

  // private JsonObject object;
  private String  object;

  public BusinessObjectApplyCommand()
  {
  }

  public BusinessObjectApplyCommand(String code, String type, Boolean isNew, Boolean isImport, String object)
  {
    super();
    this.key = code + "#" + type;
    this.code = code;
    this.type = type;
    this.isNew = isNew;
    this.isImport = isImport;
    this.object = object;
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

  public Boolean getIsNew()
  {
    return isNew;
  }

  public void setIsNew(Boolean isNew)
  {
    this.isNew = isNew;
  }

  public Boolean getIsImport()
  {
    return isImport;
  }

  public void setIsImport(Boolean isImport)
  {
    this.isImport = isImport;
  }

  public String getObject()
  {
    return object;
  }

  public void setObject(String object)
  {
    this.object = object;
  }

}