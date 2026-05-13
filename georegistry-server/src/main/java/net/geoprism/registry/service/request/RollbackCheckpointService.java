/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.service.request;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.RollbackCheckpoint;
import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.jobs.GPRJobHistory;
import net.geoprism.registry.service.business.RollbackCheckpointBusinessService;
import net.geoprism.registry.view.BasicPage;
import net.geoprism.registry.view.RollbackCheckpointDTO;

@Service
public class RollbackCheckpointService
{
  @Autowired
  private RollbackCheckpointBusinessService service;

  @Request(RequestType.SESSION)
  public BasicPage<RollbackCheckpointDTO> getPage(String sessionId, Integer pageSize, Integer pageNumber)
  {
    List<RollbackCheckpointDTO> results = this.service.getAll(pageSize, pageNumber).stream().map(this::toDTO).toList();

    long count = this.service.getCount();

    BasicPage<RollbackCheckpointDTO> page = new BasicPage<RollbackCheckpointDTO>();
    page.setResultSet(results);
    page.setPageNumber(pageNumber);
    page.setPageSize(pageSize);
    page.setCount(count);

    return page;
  }

  @Request(RequestType.SESSION)
  public void rollback(String sessionId, String oid)
  {
    RollbackCheckpoint checkpoint = this.service.get(oid);

    this.service.rollback(checkpoint);
  }

  public RollbackCheckpointDTO toDTO(RollbackCheckpoint checkpoint)
  {
    GPRJobHistory history = checkpoint.getHistory();

    RollbackCheckpointDTO dto = new RollbackCheckpointDTO();
    dto.setOid(checkpoint.getOid());
    dto.setCheckpointDate(checkpoint.getCreateDate());
    dto.setHistoryId(history.getOid());
    dto.setImportDate(history.getCreateDate());

    if (StringUtils.isNotBlank(history.getConfigJson()) && history.getConfigJson().startsWith("{"))
    {
      JsonObject config = JsonParser.parseString(history.getConfigJson()).getAsJsonObject();

      dto.setFilename(config.get(ImportConfiguration.FILE_NAME).getAsString());

    }

    return dto;
  }

}
