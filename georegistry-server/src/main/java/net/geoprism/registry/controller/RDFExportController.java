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

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.service.business.LabeledPropertyGraphRDFExportBusinessServiceIF.GeometryExportType;
import net.geoprism.registry.service.request.RDFExportService;
import net.geoprism.registry.view.ImportHistoryView;
import net.geoprism.registry.view.RDFExport;

@RestController
@Validated
public class RDFExportController extends RunwaySpringController
{
  public static class ExportParams
  {
    GeometryExportType geomExportType = GeometryExportType.NO_GEOMETRIES;

    String             versionId;

    public GeometryExportType getGeomExportType()
    {
      return geomExportType;
    }

    public void setGeomExportType(GeometryExportType geomExportType)
    {
      this.geomExportType = geomExportType;
    }

    public String getVersionId()
    {
      return versionId;
    }

    public void setVersionId(String versionId)
    {
      this.versionId = versionId;
    }

  }

  public static final String API_PATH = RegistryConstants.CONTROLLER_ROOT + "rdf";

  @Autowired
  private RDFExportService   service;

  @PostMapping(API_PATH + "/export")
  public ResponseEntity<ImportHistoryView> export(@RequestBody ExportParams params)
  {
    ImportHistoryView history = service.export(getSessionId(), params.getVersionId(), params.getGeomExportType());

    return new ResponseEntity<ImportHistoryView>(history, HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/repo-export-start")
  public ResponseEntity<ImportHistoryView> repoExport(@RequestBody RDFExport export)
  {
    ImportHistoryView history = service.export(getSessionId(), export);

    return new ResponseEntity<ImportHistoryView>(history, HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/repo-export-download")
  public ResponseEntity<InputStreamResource> download(HttpServletRequest request, @RequestParam(name = "historyId", required = false) String historyId)
  {
    InputStream is = service.exportDownload(getSessionId(), historyId);

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Content-Type", "application/zip");
    httpHeaders.set("Content-Disposition", "attachment; filename=\"rdfexport.zip\"");

    return new ResponseEntity<InputStreamResource>(new InputStreamResource(is), httpHeaders, HttpStatus.OK);
  }
}
