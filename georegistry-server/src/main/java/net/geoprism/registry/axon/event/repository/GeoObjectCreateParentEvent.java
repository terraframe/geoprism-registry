package net.geoprism.registry.axon.event.repository;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GeoObjectCreateParentEvent extends AbstractHierarchyEvent implements GeoObjectEvent
{
  private String  code;

  private String  type;

  private String  edgeUid;

  private String  edgeType;

  private Date    startDate;

  private Date    endDate;

  private String  parentType;

  private String  parentCode;

  private Boolean validate;

  public GeoObjectCreateParentEvent()
  {
  }

  public GeoObjectCreateParentEvent(String code, String type, String edgeUid, String edgeType, Date stateDate, Date endDate, String parentCode, String parentType, Boolean validate)
  {
    super();

    this.code = code;
    this.type = type;
    this.edgeUid = edgeUid;
    this.edgeType = edgeType;
    this.startDate = stateDate;
    this.endDate = endDate;
    this.parentType = parentType;
    this.parentCode = parentCode;
    this.validate = validate;
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

  public String getEdgeUid()
  {
    return edgeUid;
  }

  public void setEdgeUid(String edgeUid)
  {
    this.edgeUid = edgeUid;
  }

  public String getEdgeType()
  {
    return edgeType;
  }

  public void setEdgeType(String edgeType)
  {
    this.edgeType = edgeType;
  }

  public Date getStartDate()
  {
    return startDate;
  }

  public void setStartDate(Date startDate)
  {
    this.startDate = startDate;
  }

  public Date getEndDate()
  {
    return endDate;
  }

  public void setEndDate(Date endDate)
  {
    this.endDate = endDate;
  }

  public String getParentType()
  {
    return parentType;
  }

  public void setParentType(String parentType)
  {
    this.parentType = parentType;
  }

  public String getParentCode()
  {
    return parentCode;
  }

  public void setParentCode(String parentCode)
  {
    this.parentCode = parentCode;
  }

  public Boolean getValidate()
  {
    return validate;
  }

  public void setValidate(Boolean validate)
  {
    this.validate = validate;
  }

  @Override
  @JsonIgnore
  public String getAggregate()
  {
    return this.code + "#" + this.type + "_H_" + this.edgeType;
  }

  @Override
  @JsonIgnore
  public EventType getEventType()
  {
    return EventType.HIERARCHY;
  }
}
