package net.geoprism.registry.view;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.serialization.LocalizedValueDeserializer;
import org.commongeoregistry.adapter.serialization.LocalizedValueSerializer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class ImportColumnDTO
{
  private String            code;

  private String            locale;

  private String            baseType;

  private String            target;

  private ColumnFunctionDTO function;

  private Boolean           required;

  @JsonSerialize(using = LocalizedValueSerializer.class)
  @JsonDeserialize(using = LocalizedValueDeserializer.class)
  private LocalizedValue    label;

  public String getCode()
  {
    return code;
  }

  public void setCode(String code)
  {
    this.code = code;
  }

  public String getLocale()
  {
    return locale;
  }

  public void setLocale(String locale)
  {
    this.locale = locale;
  }

  public String getBaseType()
  {
    return baseType;
  }

  public void setBaseType(String baseType)
  {
    this.baseType = baseType;
  }

  public String getTarget()
  {
    return target;
  }

  public void setTarget(String target)
  {
    this.target = target;
  }

  public ColumnFunctionDTO getFunction()
  {
    return function;
  }

  public void setFunction(ColumnFunctionDTO function)
  {
    this.function = function;
  }

  public Boolean getRequired()
  {
    return required;
  }

  public void setRequired(Boolean required)
  {
    this.required = required;
  }

  public LocalizedValue getLabel()
  {
    return label;
  }

  public void setLabel(LocalizedValue label)
  {
    this.label = label;
  }
}
