package net.geoprism.registry.axon.command.remote;

import java.util.Date;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class RemoteGeoObjectCommand implements RemoteCommand
{
  @TargetAggregateIdentifier
  private String  key;

  private String  code;

  private String  type;

  private String  commitId;

  private Boolean isNew;

  // Serialized GeoObject
  private String  object;

  private Date    startDate;

  private Date    endDate;

  public RemoteGeoObjectCommand(String commitId, String code, Boolean isNew, String object, String type, Date startDate, Date endDate)
  {
    super();
    this.key = code + "#" + type;
    this.commitId = commitId;
    this.code = code;
    this.isNew = isNew;
    this.object = object;
    this.type = type;
    this.startDate = startDate;
    this.endDate = endDate;
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

  public Boolean getIsNew()
  {
    return isNew;
  }

  public void setIsNew(Boolean isNew)
  {
    this.isNew = isNew;
  }

  public String getObject()
  {
    return object;
  }

  public void setObject(String object)
  {
    this.object = object;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
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
