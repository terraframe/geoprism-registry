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

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.service.request.ShapefileService;
import net.geoprism.registry.view.ImportConfigurationView;

@RestController
public class ShapefileController extends RunwaySpringController
{

  public static final String API_PATH = RegistryConstants.CONTROLLER_ROOT + "shapefile";

  @Autowired
  private ShapefileService   service;

  @PostMapping(API_PATH + "/get-shapefile-configuration")
  public ResponseEntity<String> getShapefileConfiguration(@Valid @ModelAttribute ImportConfigurationView input) throws IOException
  {
    String sessionId = this.getSessionId();

    try (InputStream stream = input.getFile().getInputStream())
    {
      String fileName = input.getFile().getOriginalFilename();

      JSONObject configuration = service.getShapefileConfiguration(sessionId, fileName, stream, input);

      return new ResponseEntity<String>(configuration.toString(), HttpStatus.OK);
    }
  }
}
