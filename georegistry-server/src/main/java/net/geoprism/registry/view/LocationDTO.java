package net.geoprism.registry.view;

import com.fasterxml.jackson.annotation.JsonProperty;

import net.geoprism.registry.io.ParentMatchStrategy;

public class LocationDTO
{
  private String              label;

  private String              code;

  private String              target;

  @JsonProperty("class")
  private String              className;

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

  public String getClassName()
  {
    return className;
  }

  public void setClassName(String className)
  {
    this.className = className;
  }

  public ParentMatchStrategy getMatchStrategy()
  {
    return matchStrategy;
  }

  public void setMatchStrategy(ParentMatchStrategy matchStrategy)
  {
    this.matchStrategy = matchStrategy;
  }
}
