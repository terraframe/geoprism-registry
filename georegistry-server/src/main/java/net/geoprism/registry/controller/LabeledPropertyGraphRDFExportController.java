package net.geoprism.registry.controller;

import java.io.InputStream;

import jakarta.servlet.http.HttpServletRequest;

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

import net.geoprism.registry.service.business.LabeledPropertyGraphRDFExportBusinessService.GeometryExportType;
import net.geoprism.registry.service.request.LabeledPropertyGraphRDFExportService;

@RestController
@Validated
public class LabeledPropertyGraphRDFExportController extends RunwaySpringController
{
  public static final String API_PATH = "rdf";
  
  @Autowired
  private LabeledPropertyGraphRDFExportService  rdfExportService;
  
  @GetMapping(API_PATH + "/export")
  public ResponseEntity<?> export(HttpServletRequest request, @RequestParam(name = "geomExportType", required = false) String sGeomExportType, @RequestParam(required = true) String versionId)
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
}
