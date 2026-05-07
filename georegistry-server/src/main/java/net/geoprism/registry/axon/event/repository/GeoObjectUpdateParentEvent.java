package net.geoprism.registry.axon.event.repository;

import java.util.Date;
import java.util.UUID;

public class GeoObjectUpdateParentEvent extends AbstractHierarchyEvent implements GeoObjectEvent
{
  private String code;

  private String type;

  private String edgeUid;

  private String edgeTypeCode;

  private Date   startDate;

  private Date   endDate;

  private String parentType;

  private String parentCode;

  private String dataSource;

  public GeoObjectUpdateParentEvent()
  {
  }

  public GeoObjectUpdateParentEvent(String code, String type, String edgeUid, String edgeType, Date startDate, Date endDate, String parentCode, String parentType, String dataSource)
  {
    super(UUID.randomUUID().toString());

    this.code = code;
    this.type = type;
    this.edgeUid = edgeUid;
    this.edgeTypeCode = edgeType;
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

  public String getEdgeTypeCode()
  {
    return edgeTypeCode;
  }

  public void setEdgeTypeCode(String edgeTypeCode)
  {
    this.edgeTypeCode = edgeTypeCode;
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

  public String getDataSource()
  {
    return dataSource;
  }

  public void setDataSource(String dataSource)
  {
    this.dataSource = dataSource;
  }
}
