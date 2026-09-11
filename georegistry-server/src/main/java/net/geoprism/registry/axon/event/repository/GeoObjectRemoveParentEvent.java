package net.geoprism.registry.axon.event.repository;

import java.util.Date;
import java.util.UUID;

import net.geoprism.registry.view.TypeInfo;

public class GeoObjectRemoveParentEvent extends AbstractHierarchyEvent implements GeoObjectEvent
{
  private String   code;

  private TypeInfo type;

  private TypeInfo edgeType;

  private String   edgeUid;

  private Date     startDate;

  private Date     endDate;

  public GeoObjectRemoveParentEvent()
  {
  }

  public GeoObjectRemoveParentEvent(String code, TypeInfo type, String edgeUid, TypeInfo edgeType, Date startDate, Date endDate)
  {
    super(UUID.randomUUID().toString());

    this.code = code;
    this.type = type;
    this.edgeUid = edgeUid;
    this.edgeType = edgeType;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  public TypeInfo getType()
  {
    return type;
  }

  public void setType(TypeInfo type)
  {
    this.type = type;
  }

  public String getCode()
  {
    return code;
  }

  public void setCode(String code)
  {
    this.code = code;
  }

  public TypeInfo getEdgeType()
  {
    return edgeType;
  }

  public void setEdgeType(TypeInfo edgeType)
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

}
