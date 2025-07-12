package net.geoprism.registry.view;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class BusinessEdgeTypeSnapshotDTO
{
  private String         code;

  private String         orgCode;

  private String         labelAttribute;

  @JsonDeserialize(using = LocalizedValueDeserializer.class)
  @JsonSerialize(using = LocalizedValueSerializer.class)
  private LocalizedValue displayLabel;

  @JsonDeserialize(using = LocalizedValueDeserializer.class)
  @JsonSerialize(using = LocalizedValueSerializer.class)
  private LocalizedValue description;

  private String         parentTypeCode;

  private String         childTypeCode;

  private Boolean        isChildGeoObject;

  private Boolean        isParentGeoObject;

  public String getCode()
  {
    return code;
  }

  public void setCode(String code)
  {
    this.code = code;
  }

  public String getOrgCode()
  {
    return orgCode;
  }

  public void setOrgCode(String orgCode)
  {
    this.orgCode = orgCode;
  }

  public String getLabelAttribute()
  {
    return labelAttribute;
  }

  public void setLabelAttribute(String labelAttribute)
  {
    this.labelAttribute = labelAttribute;
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

  public String getParentTypeCode()
  {
    return parentTypeCode;
  }

  public void setParentTypeCode(String parentTypeCode)
  {
    this.parentTypeCode = parentTypeCode;
  }

  public String getChildTypeCode()
  {
    return childTypeCode;
  }

  public void setChildTypeCode(String childTypeCode)
  {
    this.childTypeCode = childTypeCode;
  }

  public Boolean getIsChildGeoObject()
  {
    return isChildGeoObject;
  }

  public void setIsChildGeoObject(Boolean isChildGeoObject)
  {
    this.isChildGeoObject = isChildGeoObject;
  }

  public Boolean getIsParentGeoObject()
  {
    return isParentGeoObject;
  }

  public void setIsParentGeoObject(Boolean isParentGeoObject)
  {
    this.isParentGeoObject = isParentGeoObject;
  }

}
