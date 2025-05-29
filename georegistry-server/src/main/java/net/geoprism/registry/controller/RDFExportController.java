/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.controller;

import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import net.geoprism.graph.GraphTypeReference;
import net.geoprism.graph.RepoRDFExportJob;
import net.geoprism.registry.service.business.LabeledPropertyGraphRDFExportBusinessServiceIF.GeometryExportType;
import net.geoprism.registry.service.request.LabeledPropertyGraphRDFExportService;
import net.geoprism.registry.service.request.RepoRDFExportService;

@RestController
@Validated
public class RDFExportController extends RunwaySpringController
{
  public static final String API_PATH = "rdf";
  
  @Autowired
  private LabeledPropertyGraphRDFExportService  rdfExportService;
  
  @Autowired
  private RepoRDFExportService  repoRdfExportService;
  
  @GetMapping(API_PATH + "/export")
  public ResponseEntity<InputStreamResource> export(HttpServletRequest request, @RequestParam(name = "geomExportType", required = false) String sGeomExportType, @RequestParam(required = true) String versionId)
  {
    GeometryExportType geomExportType = GeometryExportType.NO_GEOMETRIES;
    if (StringUtils.isBlank(sGeomExportType)) geomExportType = GeometryExportType.NO_GEOMETRIES;
    else geomExportType = GeometryExportType.valueOf(sGeomExportType);
    
    InputStream is = rdfExportService.export(getSessionId(), versionId, geomExportType);
    
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Content-Type", "application/zip");
    httpHeaders.set("Content-Disposition", "attachment; filename=\"rdfexport.zip\"");
    
    return new ResponseEntity<InputStreamResource>(new InputStreamResource(is), httpHeaders, HttpStatus.OK);
  }
  
  @GetMapping(API_PATH + "/repo-export")
  public ResponseEntity<String> export(HttpServletRequest request, @RequestParam(name = "geomExportType", required = false) String sGeomExportType, @RequestParam(required = true) String[] sGraphTypeRefs, @RequestParam(required = true) String[] gotCodes)
  {
    GeometryExportType geomExportType = GeometryExportType.NO_GEOMETRIES;
    if (StringUtils.isBlank(sGeomExportType)) geomExportType = GeometryExportType.NO_GEOMETRIES;
    else geomExportType = GeometryExportType.valueOf(sGeomExportType);
    
    var graphTypeRefs = Arrays.asList(sGraphTypeRefs).stream().map(s -> new GraphTypeReference(s.split(RepoRDFExportJob.ARRAY_STORAGE_CONCAT_TOKEN)[0], s.split(RepoRDFExportJob.ARRAY_STORAGE_CONCAT_TOKEN)[1])).toArray(GraphTypeReference[]::new);
    
    String progressId = repoRdfExportService.export(getSessionId(), graphTypeRefs, gotCodes, geomExportType);
    
    return new ResponseEntity<String>(progressId, HttpStatus.OK);
  }
}
