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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.NotBlank;
import net.geoprism.registry.graph.BusinessType;
import net.geoprism.registry.service.request.BusinessTypeService;
import net.geoprism.registry.service.request.ObjectClassServiceIF;
import net.geoprism.registry.view.BusinessEdgeTypeView;
import net.geoprism.registry.view.BusinessTypeDTO;

@RestController
@RequestMapping("api/business-type")
@Validated
public class BusinessTypeController extends ObjectClassController<BusinessType, BusinessTypeDTO>
{

  @Autowired
  private BusinessTypeService service;

  @Override
  protected ObjectClassServiceIF<BusinessType, BusinessTypeDTO> getService()
  {
    return this.service;
  }

  @GetMapping("/get-edge-types")
  public ResponseEntity<List<BusinessEdgeTypeView>> getEdgeTypes(@NotBlank @RequestParam(name = "typeCode") String typeCode)
  {
    List<BusinessEdgeTypeView> edgeTypes = this.service.getEdgeTypes(this.getSessionId(), typeCode);

    return ResponseEntity.ok(edgeTypes);
  }
}
