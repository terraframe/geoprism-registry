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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.service.business.PublishBusinessServiceIF;
import net.geoprism.registry.view.PublishDTO;

@Service
public class PublishService
{
  @Autowired
  private PublishBusinessServiceIF service;

  @Request(RequestType.SESSION)
  public PublishDTO create(String sessionId, PublishDTO dto)
  {
    return this.service.create(dto).toDTO();
  }

  @Request(RequestType.SESSION)
  public PublishDTO get(String sessionId, String uid)
  {
    return this.service.getByUid(uid).orElseThrow(() -> {
      throw new ProgrammingErrorException("Unable to find a publish with the uid [" + uid + "]");
    }).toDTO();
  }

  @Request(RequestType.SESSION)
  public List<PublishDTO> getAll(String sessionId)
  {
    return this.service.getAll().stream().map(p -> p.toDTO()).toList();
  }

}