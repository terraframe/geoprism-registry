package net.geoprism.registry.view;

import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.serialization.LocalizedValueDeserializer;
import org.commongeoregistry.adapter.serialization.LocalizedValueSerializer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class ImportTypeDTO
{
  private String                code;

  private String                type;

  @JsonSerialize(using = LocalizedValueSerializer.class)
  @JsonDeserialize(using = LocalizedValueDeserializer.class)
  private LocalizedValue        label;

  private List<ImportColumnDTO> attributes;

  public ImportTypeDTO()
  {
    this.attributes = new LinkedList<>();
  }

  public String getCode()
  {
    return code;
  }

  public void setCode(String code)
  {
    this.code = code;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public LocalizedValue getLabel()
  {
    return label;
  }

  public void setLabel(LocalizedValue label)
  {
    this.label = label;
  }

  public List<ImportColumnDTO> getAttributes()
  {
    return attributes;
  }

  public void setAttributes(List<ImportColumnDTO> attributes)
  {
    this.attributes = attributes;
  }

  public void addAttribute(ImportColumnDTO attribute)
  {
    this.attributes.add(attribute);
  }
}
