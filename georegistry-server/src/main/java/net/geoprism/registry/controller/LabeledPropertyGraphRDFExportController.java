package net.geoprism.registry.controller;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.geoprism.registry.service.request.LabeledPropertyGraphRDFExportService;

@RestController
@Validated
public class LabeledPropertyGraphRDFExportController extends RunwaySpringController
{
  public static final String API_PATH = "rdf";
  
  @Autowired
  private LabeledPropertyGraphRDFExportService  rdfExportService;
  
  @GetMapping(API_PATH + "/export")
  public ResponseEntity<?> export(HttpServletRequest request, @RequestParam(required = true) String versionId)
  {
    InputStream is = rdfExportService.export(getSessionId(), versionId);
    
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Content-Type", "application/zip");
    httpHeaders.set("Content-Disposition", "attachment; filename=\"rdfexport.zip\"");
    
    return new ResponseEntity<InputStreamResource>(new InputStreamResource(is), httpHeaders, HttpStatus.OK);
  }
}
