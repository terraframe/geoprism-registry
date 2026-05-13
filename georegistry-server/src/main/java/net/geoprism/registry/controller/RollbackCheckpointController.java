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
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.controller.BusinessTypeController.OidBody;
import net.geoprism.registry.service.request.RollbackCheckpointService;
import net.geoprism.registry.view.BasicPage;
import net.geoprism.registry.view.RollbackCheckpointDTO;

@RestController
@RequestMapping(RegistryConstants.CONTROLLER_ROOT + "rollback")
@Validated
public class RollbackCheckpointController extends RunwaySpringController
{
  @Autowired
  private RollbackCheckpointService service;

  @GetMapping("get-page")
  public ResponseEntity<BasicPage<RollbackCheckpointDTO>> getPage( //
      @RequestParam(name = "pageSize", defaultValue = "20") Integer pageSize, //
      @RequestParam(name = "pageNumber", defaultValue = "1") Integer pageNumber)
  {
    BasicPage<RollbackCheckpointDTO> page = this.service.getPage(getSessionId(), pageSize, pageNumber);

    return ResponseEntity.ok(page);
  }

  @PostMapping("rollback")
  public ResponseEntity<Void> rollback(@Valid @RequestBody OidBody body)
  {
    this.service.rollback(getSessionId(), body.getOid());

    return ResponseEntity.ok(null);
  }
}
