package net.geoprism.registry.axon.event;

import java.util.Date;

public class UpdateParentEvent implements GeoObjectEvent
{
  private String uid;

  private String type;

  private String edgeUid;

  private String edgeType;

  private Date   stateDate;

  private Date   endDate;

  private String parentType;

  private String parentCode;

  public UpdateParentEvent()
  {
  }

  public UpdateParentEvent(String uid, String type, String edgeUid, String edgeType, Date stateDate, Date endDate, String parentCode, String parentType)
  {
    super();
    this.uid = uid;
    this.type = type;
    this.edgeUid = edgeUid;
    this.edgeType = edgeType;
    this.stateDate = stateDate;
    this.endDate = endDate;
    this.parentType = parentType;
    this.parentCode = parentCode;
  }

  public String getUid()
  {
    return uid;
  }

  public void setUid(String uid)
  {
    this.uid = uid;
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

  public Date getStateDate()
  {
    return stateDate;
  }

  public void setStateDate(Date stateDate)
  {
    this.stateDate = stateDate;
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

}
