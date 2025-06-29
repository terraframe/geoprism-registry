package net.geoprism.registry.axon.event.repository;

public class BusinessObjectApplyEvent implements BusinessObjectEvent
{
  private String  key;

  private String  code;

  private String  type;

  private String  object;

  private Boolean isNew;

  public BusinessObjectApplyEvent()
  {
  }

  public BusinessObjectApplyEvent(String code, String type, String object, Boolean isNew)
  {
    this(code + "#" + type, code, type, object, isNew);
  }

  public BusinessObjectApplyEvent(String key, String code, String type, String object, Boolean isNew)
  {
    super();

    this.key = key;
    this.code = code;
    this.type = type;
    this.object = object;
    this.isNew = isNew;
  }

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

  public Boolean getIsNew()
  {
    return isNew;
  }

  public void setIsNew(Boolean isNew)
  {
    this.isNew = isNew;
  }

}
