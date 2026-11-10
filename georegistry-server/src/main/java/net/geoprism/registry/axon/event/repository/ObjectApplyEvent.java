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
package net.geoprism.registry.axon.event.repository;

import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.geoprism.registry.view.ObjectOverTimeDTO;
import net.geoprism.registry.view.TypeInfo;

public abstract class ObjectApplyEvent extends AbstractRepositoryEvent implements ImportHistoryEvent
{
  private String            code;

  private TypeInfo          type;

  private ObjectOverTimeDTO object;

  private Boolean           isNew;

  // Optional ID of the import
  private String            historyId;

  // Optional start date of the import
  private Date              startDate;

  // Optional start date of the import
  private Date              endDate;

  public ObjectApplyEvent()
  {
  }

  public ObjectApplyEvent(String code, TypeInfo type, ObjectOverTimeDTO object, Boolean isNew)
  {
    super(UUID.randomUUID().toString());

    this.code = code;
    this.type = type;
    this.object = object;
    this.isNew = isNew;
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

  public ObjectOverTimeDTO getObject()
  {
    return object;
  }

  public void setObject(ObjectOverTimeDTO object)
  {
    this.object = object;
  }

  public Boolean getIsNew()
  {
    return isNew;
  }

  public void setIsNew(Boolean isNew)
  {
    this.isNew = isNew;
  }

  public String getHistoryId()
  {
    return historyId;
  }

  public void setHistoryId(String historyId)
  {
    this.historyId = historyId;
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
  @JsonIgnore
  public EventPhase getEventPhase()
  {
    return EventPhase.OBJECT;
  }
}
