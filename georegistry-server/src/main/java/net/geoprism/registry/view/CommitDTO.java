package net.geoprism.registry.view;

import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import net.geoprism.registry.spring.DateTimeDeserializer;
import net.geoprism.registry.spring.DateTimeSerializer;

public class CommitDTO
{
  private String  uid;

  private String  publishId;

  private Integer versionNumber;

  private Long    lastOriginGlobalIndex;

  @JsonSerialize(using = DateTimeSerializer.class)
  @JsonDeserialize(using = DateTimeDeserializer.class)
  private Date    createDate;

  public CommitDTO()
  {
    this.uid = UUID.randomUUID().toString();
  }

  public CommitDTO(String uid, String publishId, Integer versionNumber, Long lastOriginGlobalIndex, Date createDate)
  {
    this.uid = uid;
    this.publishId = publishId;
    this.versionNumber = versionNumber;
    this.lastOriginGlobalIndex = lastOriginGlobalIndex;
    this.createDate = createDate;
  }

  public String getUid()
  {
    return uid;
  }

  public void setUid(String uid)
  {
    this.uid = uid;
  }

  public String getPublishId()
  {
    return publishId;
  }

  public void setPublishId(String publishId)
  {
    this.publishId = publishId;
  }

  public Integer getVersionNumber()
  {
    return versionNumber;
  }

  public void setVersionNumber(Integer versionNumber)
  {
    this.versionNumber = versionNumber;
  }

  public Long getLastOriginGlobalIndex()
  {
    return lastOriginGlobalIndex;
  }

  public void setLastOriginGlobalIndex(Long lastOriginGlobalIndex)
  {
    this.lastOriginGlobalIndex = lastOriginGlobalIndex;
  }

  public Date getCreateDate()
  {
    return createDate;
  }

  public void setCreateDate(Date createDate)
  {
    this.createDate = createDate;
  }
}
