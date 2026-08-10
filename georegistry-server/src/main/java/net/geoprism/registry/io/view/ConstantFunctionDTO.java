package net.geoprism.registry.io.view;

public class ConstantFunctionDTO extends ColumnFunctionDTO
{
  public static final String TYPE = "constant";

  private String             value;

  public ConstantFunctionDTO()
  {
  }

  public ConstantFunctionDTO(String value)
  {
    super();
    this.value = value;
  }

  public String getValue()
  {
    return value;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  @Override
  public String getType()
  {
    return TYPE;
  }
}
