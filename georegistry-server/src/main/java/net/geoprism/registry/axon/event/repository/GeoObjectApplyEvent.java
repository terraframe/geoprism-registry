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

import net.geoprism.registry.view.PublishDTO;
import net.geoprism.registry.view.TypeInfo;

public class GeoObjectApplyEvent extends AbstractGeoObjectEvent implements GeoObjectEvent, ImportHistoryEvent
{
  private String   code;

  private TypeInfo type;

  private Boolean  isNew;

  private String   object;

  private Boolean  isImport;

  // Optional ID of the import
  private String   historyId;

  // Optional start date of the import
  private Date     startDate;

  // Optional start date of the import
  private Date     endDate;

  public GeoObjectApplyEvent()
  {
  }

  public GeoObjectApplyEvent(String code, TypeInfo type, Boolean isNew, Boolean isImport, String object)
  {
    super(UUID.randomUUID().toString());

    this.code = code;
    this.type = type;
    this.isNew = isNew;
    this.isImport = isImport;
    this.object = object;
  }

  public GeoObjectApplyEvent(String code, TypeInfo type, Boolean isNew, Boolean isImport, String object, String historyId, Date startDate, Date endDate)
  {
    super(UUID.randomUUID().toString());

    this.code = code;
    this.type = type;
    this.isNew = isNew;
    this.isImport = isImport;
    this.object = object;
    this.historyId = historyId;
    this.startDate = startDate;
    this.endDate = endDate;
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

  public Boolean getIsImport()
  {
    return isImport;
  }

  public void setIsImport(Boolean isImport)
  {
    this.isImport = isImport;
  }

  public String getObject()
  {
    return object;
  }

  public void setObject(String object)
  {
    this.object = object;
  }

  public TypeInfo getType()
  {
    return type;
  }

  public void setType(TypeInfo type)
  {
    this.type = type;
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
  public String getBaseObjectId()
  {
    return this.code + "#" + this.type.getTypeCode() + "_O_";
  }

  @Override
  @JsonIgnore
  public EventPhase getEventPhase()
  {
    return EventPhase.OBJECT;
  }

  @Override
  public Boolean isValidFor(PublishDTO dto)
  {
    return dto.getTypes().stream().anyMatch(this.getType()::equals);
  }
}
