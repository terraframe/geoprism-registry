package net.geoprism.registry.axon.command.remote;

import java.util.Date;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class RemoteGeoObjectCommand implements RemoteCommand
{
  @TargetAggregateIdentifier
  private String  uid;

  private String  commitId;

  private Boolean isNew;

  // Serialized GeoObject
  private String  object;

  private String  type;

  private Date    startDate;

  private Date    endDate;

  public RemoteGeoObjectCommand(String commitId, String uid, Boolean isNew, String object, String type, Date startDate, Date endDate)
  {
    super();
    this.commitId = commitId;
    this.uid = uid;
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

  public String getUid()
  {
    return uid;
  }

  public void setUid(String uid)
  {
    this.uid = uid;
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
