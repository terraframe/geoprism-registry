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
package net.geoprism.registry.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.model.SourceAuthorityDTO;
import net.geoprism.registry.service.request.SourceAuthorityServiceIF;

@RestController
@Validated
@RequestMapping(RegistryConstants.CONTROLLER_ROOT + "source-authority")
public class SourceAuthorityController extends AbstractCrudController<SourceAuthorityDTO, SourceAuthorityServiceIF>
{
  public SourceAuthorityController(SourceAuthorityServiceIF service)
  {
    super(service);
  }

  @GetMapping("/search")
  public ResponseEntity<List<SourceAuthorityDTO>> search(@RequestParam(name = "text", required = false) String text)
  {
    List<SourceAuthorityDTO> sources = this.getService().search(this.getSessionId(), text);

    return new ResponseEntity<List<SourceAuthorityDTO>>(sources, HttpStatus.OK);
  }

}
