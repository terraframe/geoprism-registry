package net.geoprism.registry.view;

import java.util.Set;

public class ExclusionDTO
{
  private String      code;

  private Set<String> value;

  public ExclusionDTO()
  {
  }

  public ExclusionDTO(String code, Set<String> value)
  {
    super();
    this.code = code;
    this.value = value;
  }

  public String getCode()
  {
    return code;
  }

  public void setCode(String code)
  {
    this.code = code;
  }

  public Set<String> getValue()
  {
    return value;
  }

  public void setValue(Set<String> value)
  {
    this.value = value;
  }
}
