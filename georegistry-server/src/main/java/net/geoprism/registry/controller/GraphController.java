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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.commongeoregistry.adapter.metadata.GraphTypeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.service.request.EdgeImportService;
import net.geoprism.registry.service.request.GraphTypeService;
import net.geoprism.registry.view.EdgeImportConfigurationView;
import net.geoprism.registry.view.EdgeObjectImportConfigurationDTO;

@RestController
@Validated
@RequestMapping(RegistryConstants.CONTROLLER_ROOT + "graph/")
public class GraphController extends RunwaySpringController
{
  @Autowired
  private GraphTypeService  graphTypeService;

  @Autowired
  private EdgeImportService importService;

  @GetMapping("get")
  public ResponseEntity<List<GraphTypeDTO>> get(@RequestParam(name = "codes", required = false) String[] codes)
  {
    List<GraphTypeDTO> types = graphTypeService.getGraphTypes(this.getSessionId(), codes).stream() //
        .sorted((a, b) -> a.getLabel().getValue().compareTo(b.getLabel().getValue())) //
        .toList();

    return ResponseEntity.ok(types);
  }

  @PostMapping("get-json-import-config")
  public ResponseEntity<EdgeObjectImportConfigurationDTO> getJsonImportConfig(@Valid @ModelAttribute EdgeImportConfigurationView body) throws IOException
  {
    String sessionId = this.getSessionId();

    try (InputStream stream = body.getFile().getInputStream())
    {
      String fileName = body.getFile().getOriginalFilename();

      EdgeObjectImportConfigurationDTO configuration = importService.getJsonImportConfiguration(sessionId, fileName, stream, body);

      return ResponseEntity.ok(configuration);
    }
  }
}
