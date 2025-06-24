package net.geoprism.registry.axon.event;

public class BusinessObjectApplyEvent implements BusinessObjectEvent
{
  private String key;

  private String type;

  private String code;

  private String object;

  public BusinessObjectApplyEvent()
  {
  }

  public BusinessObjectApplyEvent(String type, String code, String object)
  {
    this(code + "#" + type, type, code, object);
  }

  public BusinessObjectApplyEvent(String key, String type, String code, String object)
  {
    super();

    this.key = key;
    this.type = type;
    this.code = code;
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

}
