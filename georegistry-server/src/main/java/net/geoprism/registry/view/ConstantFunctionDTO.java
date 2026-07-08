package net.geoprism.registry.view;

public class ConstantFunctionDTO extends ColumnFunctionDTO
{
  private String value;

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
}
