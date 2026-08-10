package net.geoprism.registry.io.view;

public class ExternalIdMappingDTO
{
  private String            authority;

  private ColumnFunctionDTO function;

  public String getAuthority()
  {
    return authority;
  }

  public void setAuthority(String authority)
  {
    this.authority = authority;
  }

  public ColumnFunctionDTO getFunction()
  {
    return function;
  }

  public void setFunction(ColumnFunctionDTO function)
  {
    this.function = function;
  }

}
