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
import java.text.SimpleDateFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.service.request.ExcelService;
import net.geoprism.registry.view.BusinessObjectImportConfigurationDTO;
import net.geoprism.registry.view.ConceptObjectImportConfigurationDTO;
import net.geoprism.registry.view.GeoObjectImportConfigurationDTO;
import net.geoprism.registry.view.ImportConfigurationView;

@RestController
@Validated
@RequestMapping(RegistryConstants.CONTROLLER_ROOT + "excel")
public class ExcelImportController extends RunwaySpringController
{
  @Autowired
  private ExcelService service;

  @PostMapping("/get-configuration")
  public ResponseEntity<GeoObjectImportConfigurationDTO> getConfiguration(@Valid @ModelAttribute ImportConfigurationView body) throws IOException
  {
    String sessionId = this.getSessionId();

    try (InputStream stream = body.getFile().getInputStream())
    {
      String fileName = body.getFile().getOriginalFilename();

      GeoObjectImportConfigurationDTO configuration = service.getExcelConfiguration(sessionId, fileName, stream, body);

      return ResponseEntity.ok(configuration);
    }
  }

  @PostMapping("/export-spreadsheet")
  public ResponseEntity<?> exportSpreadsheet(@RequestParam(name = "type") @NotEmpty String type, @RequestParam(name = "hierarchyType") @NotEmpty String hierarchyType)
  {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=export.xlsx");

    InputStreamResource isr = new InputStreamResource(service.exportSpreadsheet(this.getSessionId(), type, hierarchyType));
    return new ResponseEntity<InputStreamResource>(isr, headers, HttpStatus.OK);
  }

  @PostMapping("/get-business-config")
  public ResponseEntity<BusinessObjectImportConfigurationDTO> getBusinessConfiguration(@Valid @ModelAttribute ImportConfigurationView body) throws IOException
  {
    try (InputStream stream = body.getFile().getInputStream())
    {
      String fileName = body.getFile().getOriginalFilename();

      SimpleDateFormat format = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);
      format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

      BusinessObjectImportConfigurationDTO configuration = service.getBusinessTypeConfiguration(this.getSessionId(), fileName, stream, body);

      return ResponseEntity.ok(configuration);
    }
  }

  @PostMapping("/get-concept-config")
  public ResponseEntity<ConceptObjectImportConfigurationDTO> getConceptConfiguration(@Valid @ModelAttribute ImportConfigurationView body) throws IOException
  {
    try (InputStream stream = body.getFile().getInputStream())
    {
      String fileName = body.getFile().getOriginalFilename();

      SimpleDateFormat format = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);
      format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

      ConceptObjectImportConfigurationDTO configuration = service.getConceptClassConfiguration(this.getSessionId(), fileName, stream, body);

      return ResponseEntity.ok(configuration);
    }
  }

}
