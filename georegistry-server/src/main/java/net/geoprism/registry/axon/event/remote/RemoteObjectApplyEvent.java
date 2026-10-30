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
package net.geoprism.registry.axon.event.remote;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import net.geoprism.registry.view.ObjectAtTimeDTO;
import net.geoprism.registry.view.PublishDTO;
import net.geoprism.registry.view.TypeInfo;
import net.geoprism.registry.view.serialization.DateDeserializer;
import net.geoprism.registry.view.serialization.DateSerializer;

public class RemoteObjectApplyEvent implements RemoteEvent
{
  private String          commitId;

  private String          key;

  private String          code;

  private TypeInfo        type;

  private ObjectAtTimeDTO object;

  @JsonSerialize(using = DateSerializer.class)
  @JsonDeserialize(using = DateDeserializer.class)
  private Date            startDate;

  @JsonSerialize(using = DateSerializer.class)
  @JsonDeserialize(using = DateDeserializer.class)
  private Date            endDate;

  public RemoteObjectApplyEvent()
  {
  }

  public RemoteObjectApplyEvent(String commitId, String code, TypeInfo type, ObjectAtTimeDTO object, Date startDate, Date endDate)
  {
    this(commitId, code + "#" + type, code, type, object, startDate, endDate);
  }

  public RemoteObjectApplyEvent(String commitId, String key, String code, TypeInfo type, ObjectAtTimeDTO object, Date startDate, Date endDate)
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

  public TypeInfo getType()
  {
    return type;
  }

  public void setType(TypeInfo type)
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

  @Override
  public boolean isValid(PublishDTO dto)
  {
    return !dto.getExclusions().contains(this.getType());
  }

  @Override
  @JsonIgnore
  public String getBaseObjectId()
  {
    return this.getCode() + "#" + this.getType().getTypeCode() + "#" + this.getType().getTypeClass().getShortCode();
  }

}
