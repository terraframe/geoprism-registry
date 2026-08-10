package net.geoprism.registry.io.view;

import net.geoprism.registry.io.ParentMatchStrategy;

public class LocationDTO
{
  private String              label;

  private String              code;

  private String              target;

  private ColumnFunctionDTO   function;

  private String              authority;

  private ParentMatchStrategy matchStrategy;

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  public String getCode()
  {
    return code;
  }

  public void setCode(String code)
  {
    this.code = code;
  }

  public String getTarget()
  {
    return target;
  }

  public void setTarget(String target)
  {
    this.target = target;
  }

  public ParentMatchStrategy getMatchStrategy()
  {
    return matchStrategy;
  }

  public void setMatchStrategy(ParentMatchStrategy matchStrategy)
  {
    this.matchStrategy = matchStrategy;
  }

  public ColumnFunctionDTO getFunction()
  {
    return function;
  }

  public void setFunction(ColumnFunctionDTO function)
  {
    this.function = function;
  }

  public String getAuthority()
  {
    return authority;
  }

  public void setAuthority(String authority)
  {
    this.authority = authority;
  }

}
