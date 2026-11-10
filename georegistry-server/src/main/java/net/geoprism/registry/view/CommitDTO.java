/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.view;

import java.util.Date;
import java.util.UUID;

import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import net.geoprism.registry.view.serialization.DateTimeDeserializer;
import net.geoprism.registry.view.serialization.DateTimeSerializer;

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

  public CommitDTO(String uid, String publishId, Integer versionNumber, Long lastOriginGlobalIndex)
  {
    this(uid, publishId, versionNumber, lastOriginGlobalIndex, null);
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
