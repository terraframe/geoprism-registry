package net.geoprism.registry.io.view;

public class BasicColumnFunctionDTO extends ColumnFunctionDTO
{
  public static final String TYPE = "basic";

  private String             target;

  public BasicColumnFunctionDTO()
  {
  }

  public BasicColumnFunctionDTO(String target)
  {
    super();
    this.target = target;
  }

  public String getTarget()
  {
    return target;
  }

  public void setTarget(String target)
  {
    this.target = target;
  }

  @Override
  public String getType()
  {
    return TYPE;
  }
}
