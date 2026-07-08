package net.geoprism.registry.view;

public class BasicColumnFunctionDTO extends ColumnFunctionDTO
{
  private String attributeName;

  public BasicColumnFunctionDTO()
  {
  }

  public BasicColumnFunctionDTO(String attributeName)
  {
    super();
    this.attributeName = attributeName;
  }

  public String getAttributeName()
  {
    return attributeName;
  }

  public void setAttributeName(String attributeName)
  {
    this.attributeName = attributeName;
  }
}
