package net.geoprism.registry.axon.event.remote;

import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import net.geoprism.registry.axon.command.remote.RemoteGeoObjectCommand;
import net.geoprism.registry.spring.DateDeserializer;
import net.geoprism.registry.spring.DateSerializer;

public class RemoteGeoObjectEvent implements RemoteEvent
{
  private String  commitId;

  private String  code;

  private String  type;

  private Boolean isNew;

  // Serialized GeoObject
  private String  object;

  @JsonSerialize(using = DateSerializer.class)
  @JsonDeserialize(using = DateDeserializer.class)
  private Date    startDate;

  @JsonSerialize(using = DateSerializer.class)
  @JsonDeserialize(using = DateDeserializer.class)
  private Date    endDate;

  public RemoteGeoObjectEvent()
  {
  }

  public RemoteGeoObjectEvent(String commitId, String code, Boolean isNew, String object, String type, Date startDate, Date endDate)
  {
    super();
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

  @Override
  public Object toCommand()
  {
    return new RemoteGeoObjectCommand(commitId, code, isNew, object, type, startDate, endDate);
  }
}
