package net.geoprism.registry.axon.command.remote;

import java.util.Date;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class RemoteGeoObjectSetParentCommand implements RemoteCommand
{

  @TargetAggregateIdentifier
  private String key;

  private String code;

  private String commitId;

  private String type;

  private String edgeUid;

  private String edgeType;

  private Date   startDate;

  private Date   endDate;

  private String parentType;

  private String parentCode;

  public RemoteGeoObjectSetParentCommand()
  {
  }

  public RemoteGeoObjectSetParentCommand(String commitId, String code, String type, String edgeUid, String edgeType, Date startDate, Date endDate, String parentCode, String parentType)
  {
    super();
    this.key = code + "#" + type;
    this.commitId = commitId;
    this.code = code;
    this.type = type;
    this.edgeUid = edgeUid;
    this.edgeType = edgeType;
    this.startDate = startDate;
    this.endDate = endDate;
    this.parentType = parentType;
    this.parentCode = parentCode;
  }

  public String getCommitId()
  {
    return commitId;
  }

  public void setCommitId(String commitId)
  {
    this.commitId = commitId;
  }
  
  public String getCode()
  {
    return code;
  }
  
  public void setCode(String code)
  {
    this.code = code;
  }
  
  public String getKey()
  {
    return key;
  }
  
  public void setKey(String key)
  {
    this.key = key;
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
}
