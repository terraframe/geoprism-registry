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
package net.geoprism.registry.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.geoprism.registry.graph.ConceptEdgeType;
import net.geoprism.registry.service.request.ConceptEdgeTypeServiceIF;
import net.geoprism.registry.service.request.EdgeClassServiceIF;
import net.geoprism.registry.view.ConceptEdgeTypeDTO;

@RestController
@RequestMapping("api/concept-edge-type")
@Validated
public class ConceptEdgeTypeController extends EdgeClassController<ConceptEdgeType, ConceptEdgeTypeDTO>
{
  @Autowired
  private ConceptEdgeTypeServiceIF service;

  @Override
  protected EdgeClassServiceIF<ConceptEdgeType, ConceptEdgeTypeDTO> getService()
  {
    return this.service;
  }
}
