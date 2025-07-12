package net.geoprism.registry.view;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class GeoObjectTypeSnapshotDTO
{
  private String         code;

  @JsonDeserialize(using = LocalizedValueDeserializer.class)
  @JsonSerialize(using = LocalizedValueSerializer.class)
  private LocalizedValue displayLabel;

  @JsonDeserialize(using = LocalizedValueDeserializer.class)
  @JsonSerialize(using = LocalizedValueSerializer.class)
  private LocalizedValue description;

  private String         geometryType;

  private Boolean        isPrivate;

  private Boolean        isRoot;

  private Boolean        isAbstract;

  private Boolean        isGeometryEditable;

  private String         orgCode;

  private String         parentTypeCode;

  public String getCode()
  {
    return code;
  }

  public void setCode(String code)
  {
    this.code = code;
  }

  public LocalizedValue getDisplayLabel()
  {
    return displayLabel;
  }

  public void setDisplayLabel(LocalizedValue displayLabel)
  {
    this.displayLabel = displayLabel;
  }

  public LocalizedValue getDescription()
  {
    return description;
  }

  public void setDescription(LocalizedValue description)
  {
    this.description = description;
  }

  public String getGeometryType()
  {
    return geometryType;
  }

  public void setGeometryType(String geometryType)
  {
    this.geometryType = geometryType;
  }

  public Boolean getIsPrivate()
  {
    return isPrivate;
  }

  public void setIsPrivate(Boolean isPrivate)
  {
    this.isPrivate = isPrivate;
  }

  public Boolean getIsRoot()
  {
    return isRoot;
  }

  public void setIsRoot(Boolean isRoot)
  {
    this.isRoot = isRoot;
  }

  public Boolean getIsAbstract()
  {
    return isAbstract;
  }

  public void setIsAbstract(Boolean isAbstract)
  {
    this.isAbstract = isAbstract;
  }

  public Boolean getIsGeometryEditable()
  {
    return isGeometryEditable;
  }

  public void setIsGeometryEditable(Boolean isGeometryEditable)
  {
    this.isGeometryEditable = isGeometryEditable;
  }

  public String getOrgCode()
  {
    return orgCode;
  }

  public void setOrgCode(String orgCode)
  {
    this.orgCode = orgCode;
  }

  public String getParentTypeCode()
  {
    return parentTypeCode;
  }

  public void setParentTypeCode(String parentTypeCode)
  {
    this.parentTypeCode = parentTypeCode;
  }
}
