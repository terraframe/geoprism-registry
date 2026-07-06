package net.geoprism.registry.axon.event.remote;

import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import net.geoprism.registry.view.ObjectAtTimeDTO;
import net.geoprism.registry.view.serialization.DateDeserializer;
import net.geoprism.registry.view.serialization.DateSerializer;

public abstract class RemoteObjectEvent implements RemoteEvent
{
  private String          commitId;

  private String          key;

  private String          code;

  private String          type;

  private ObjectAtTimeDTO object;

  @JsonSerialize(using = DateSerializer.class)
  @JsonDeserialize(using = DateDeserializer.class)
  private Date            startDate;

  @JsonSerialize(using = DateSerializer.class)
  @JsonDeserialize(using = DateDeserializer.class)
  private Date            endDate;

  public RemoteObjectEvent()
  {
  }

  public RemoteObjectEvent(String commitId, String code, String type, ObjectAtTimeDTO object, Date startDate, Date endDate)
  {
    this(commitId, code + "#" + type, code, type, object, startDate, endDate);
  }

  public RemoteObjectEvent(String commitId, String key, String code, String type, ObjectAtTimeDTO object, Date startDate, Date endDate)
  {
    super();

    this.key = key;
    this.commitId = commitId;
    this.code = code;
    this.type = type;
    this.object = object;
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

  public String getCode()
  {
    return code;
  }

  public void setCode(String code)
  {
    this.code = code;
  }

  public ObjectAtTimeDTO getObject()
  {
    return object;
  }

  public void setObject(ObjectAtTimeDTO object)
  {
    this.object = object;
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
