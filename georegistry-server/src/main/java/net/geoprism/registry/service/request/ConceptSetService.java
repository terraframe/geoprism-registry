/**
 * Copyright (c) 2023 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism(tm).
 *
 * Geoprism(tm) is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Geoprism(tm) is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.service.request;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.graph.ConceptSet;
import net.geoprism.registry.service.business.ConceptSetBusinessServiceIF;
import net.geoprism.registry.view.ConceptSetDTO;

@Service
public class ConceptSetService implements ConceptSetServiceIF
{
  @Autowired
  private ConceptSetBusinessServiceIF service;

  @Override
  @Request(RequestType.SESSION)
  public void delete(String sessionId, String code)
  {
    service.getByCode(code).ifPresent(t -> service.delete(t));
  }

  @Override
  @Request(RequestType.SESSION)
  public List<ConceptSetDTO> getAll(String sessionId)
  {
    return service.getAll().stream().map(t -> service.toDTO(t)).toList();
  }

  @Override
  @Request(RequestType.SESSION)
  public ConceptSetDTO getByCode(String sessionId, String code)
  {
    ConceptSet t = service.getByCodeOrThrow(code);

    return service.toDTO(t);
  }

  @Override
  @Request(RequestType.SESSION)
  public ConceptSetDTO apply(String sessionId, ConceptSetDTO object)
  {
    ConceptSet t = service.apply(object);

    return service.toDTO(t);
  }
}
