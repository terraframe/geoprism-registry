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

import net.geoprism.registry.view.TypeInfo;

public class GeoObjectCreateParentEvent extends AbstractHierarchyEvent implements GeoObjectEvent
{
  private String   code;

  private TypeInfo type;

  private String   edgeUid;

  private TypeInfo edgeType;

  private Date     startDate;

  private Date     endDate;

  private TypeInfo parentType;

  private String   parentCode;

  private String   dataSource;

  private Boolean  validate;

  public GeoObjectCreateParentEvent()
  {
  }

  public GeoObjectCreateParentEvent(String code, TypeInfo type, String edgeUid, TypeInfo edgeType, Date stateDate, Date endDate, String parentCode, TypeInfo parentType, String dataSource, Boolean validate)
  {
    super(UUID.randomUUID().toString());

    this.code = code;
    this.type = type;
    this.edgeUid = edgeUid;
    this.edgeType = edgeType;
    this.startDate = stateDate;
    this.endDate = endDate;
    this.parentType = parentType;
    this.parentCode = parentCode;
    this.dataSource = dataSource;
    this.validate = validate;
  }

  public String getCode()
  {
    return code;
  }

  public void setCode(String code)
  {
    this.code = code;
  }

  public TypeInfo getType()
  {
    return type;
  }

  public void setType(TypeInfo type)
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

  public TypeInfo getEdgeType()
  {
    return edgeType;
  }

  public void setEdgeType(TypeInfo edgeType)
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

  public TypeInfo getParentType()
  {
    return parentType;
  }

  public void setParentType(TypeInfo parentType)
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

  public String getDataSource()
  {
    return dataSource;
  }

  public void setDataSource(String dataSource)
  {
    this.dataSource = dataSource;
  }

  public Boolean getValidate()
  {
    return validate;
  }

  public void setValidate(Boolean validate)
  {
    this.validate = validate;
  }

  @Override
  @JsonIgnore
  public EventPhase getEventPhase()
  {
    return EventPhase.EDGE;
  }
}
