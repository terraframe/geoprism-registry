package net.geoprism.registry.axon.event.remote;

import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import net.geoprism.registry.axon.command.remote.RemoteGeoObjectSetParentCommand;
import net.geoprism.registry.spring.DateDeserializer;
import net.geoprism.registry.spring.DateSerializer;

public class RemoteGeoObjectSetParentEvent implements RemoteEvent
{
  private String commitId;

  private String uid;

  private String type;

  private String edgeUid;

  private String edgeType;

  @JsonSerialize(using = DateSerializer.class)
  @JsonDeserialize(using = DateDeserializer.class)
  private Date   startDate;

  @JsonSerialize(using = DateSerializer.class)
  @JsonDeserialize(using = DateDeserializer.class)
  private Date   endDate;

  private String parentType;

  private String parentCode;

  public RemoteGeoObjectSetParentEvent()
  {
  }

  public RemoteGeoObjectSetParentEvent(String commitId, String uid, String type, String edgeUid, String edgeType, Date startDate, Date endDate, String parentCode, String parentType)
  {
    super();
    this.commitId = commitId;
    this.uid = uid;
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
  
  @Override
  public Object toCommand()
  {
    return new RemoteGeoObjectSetParentCommand(commitId, uid, type, edgeUid, edgeType, startDate, endDate, parentCode, parentType);
  }
}
