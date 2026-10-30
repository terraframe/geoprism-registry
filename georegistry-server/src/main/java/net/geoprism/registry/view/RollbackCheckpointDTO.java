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

import tools.jackson.databind.annotation.JsonSerialize;

import net.geoprism.registry.view.serialization.DateTimeSerializer;

public class RollbackCheckpointDTO
{
  private String oid;

  private String historyId;

  private String filename;

  @JsonSerialize(using = DateTimeSerializer.class)
  private Date   importDate;

  @JsonSerialize(using = DateTimeSerializer.class)
  private Date   checkpointDate;

  private String description;

  private Long   globalIndex;

  public String getOid()
  {
    return oid;
  }

  public void setOid(String oid)
  {
    this.oid = oid;
  }

  public String getHistoryId()
  {
    return historyId;
  }

  public void setHistoryId(String historyId)
  {
    this.historyId = historyId;
  }

  public String getFilename()
  {
    return filename;
  }

  public void setFilename(String filename)
  {
    this.filename = filename;
  }

  public Date getImportDate()
  {
    return importDate;
  }

  public void setImportDate(Date importDate)
  {
    this.importDate = importDate;
  }

  public Date getCheckpointDate()
  {
    return checkpointDate;
  }

  public void setCheckpointDate(Date checkpointDate)
  {
    this.checkpointDate = checkpointDate;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public Long getGlobalIndex()
  {
    return globalIndex;
  }

  public void setGlobalIndex(Long globalIndex)
  {
    this.globalIndex = globalIndex;
  }

}
