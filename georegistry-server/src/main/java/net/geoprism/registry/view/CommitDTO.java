package net.geoprism.registry.view;

import java.util.UUID;

public class CommitDTO
{
  private String  uid;

  private String  publishId;

  private Integer versionNumber;

  private Long    lastOriginGlobalIndex;

  public CommitDTO()
  {
    this.uid = UUID.randomUUID().toString();
  }

  public CommitDTO(String uid, String publishId, Integer versionNumber, Long lastOriginGlobalIndex)
  {
    this.uid = uid;
    this.publishId = publishId;
    this.versionNumber = versionNumber;
    this.lastOriginGlobalIndex = lastOriginGlobalIndex;
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
}
