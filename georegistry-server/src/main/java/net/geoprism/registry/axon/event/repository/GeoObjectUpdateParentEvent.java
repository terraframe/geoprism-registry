package net.geoprism.registry.axon.event.repository;

import java.util.Date;
import java.util.UUID;

import net.geoprism.registry.view.TypeInfo;

public class GeoObjectUpdateParentEvent extends AbstractHierarchyEvent implements GeoObjectEvent
{
  private String   code;

  private TypeInfo type;

  private String   edgeUid;

  private TypeInfo edgeType;

  private Date     startDate;

  private Date     endDate;

  private TypeInfo parentType;

  private String   parentCode;

  private String   dataSource;

  public GeoObjectUpdateParentEvent()
  {
  }

  public GeoObjectUpdateParentEvent(String code, TypeInfo type, String edgeUid, TypeInfo edgeType, Date startDate, Date endDate, String parentCode, TypeInfo parentType, String dataSource)
  {
    super(UUID.randomUUID().toString());

    this.code = code;
    this.type = type;
    this.edgeUid = edgeUid;
    this.edgeType = edgeType;
    this.startDate = startDate;
    this.endDate = endDate;
    this.parentType = parentType;
    this.parentCode = parentCode;
    this.dataSource = dataSource;
  }

  public String getCode()
  {
    return code;
  }

  public TypeInfo getType()
  {
    return type;
  }

  public void setType(TypeInfo type)
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

  public TypeInfo getEdgeType()
  {
    return edgeType;
  }

  public void setEdgeType(TypeInfo edgeType)
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

  public TypeInfo getParentType()
  {
    return parentType;
  }

  public void setParentType(TypeInfo parentType)
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

  public String getDataSource()
  {
    return dataSource;
  }

  public void setDataSource(String dataSource)
  {
    this.dataSource = dataSource;
  }

  public void setCode(String code)
  {
    this.code = code;
  }

}
