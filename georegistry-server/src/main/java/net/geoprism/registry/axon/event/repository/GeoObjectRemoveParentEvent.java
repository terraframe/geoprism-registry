package net.geoprism.registry.axon.event.repository;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GeoObjectRemoveParentEvent extends AbstractHierarchyEvent implements GeoObjectEvent
{
  private String type;

  private String uid;

  private String edgeType;

  private String edgeUid;

  private Date   startDate;

  private Date   endDate;

  public GeoObjectRemoveParentEvent()
  {
  }

  public GeoObjectRemoveParentEvent(String uid, String type, String edgeUid, String edgeType, Date startDate, Date endDate)
  {
    super();

    this.uid = uid;
    this.type = type;
    this.edgeUid = edgeUid;
    this.edgeType = edgeType;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getUid()
  {
    return uid;
  }

  public void setUid(String uid)
  {
    this.uid = uid;
  }

  public String getEdgeType()
  {
    return edgeType;
  }

  public void setEdgeType(String edgeType)
  {
    this.edgeType = edgeType;
  }

  public String getEdgeUid()
  {
    return edgeUid;
  }

  public void setEdgeUid(String edgeUid)
  {
    this.edgeUid = edgeUid;
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

  @Override
  @JsonIgnore
  public String getAggregate()
  {
    return this.uid + "_H_" + this.edgeType;
  }

  @Override
  @JsonIgnore
  public EventType getEventType()
  {
    return EventType.HIERARCHY;
  }
}
