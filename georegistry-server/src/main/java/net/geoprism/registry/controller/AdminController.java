package net.geoprism.registry.controller;

import java.io.IOException;
import java.io.InputStream;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import net.geoprism.registry.service.request.BackupAndRestoreServiceIF;

@RestController
@Validated
public class AdminController extends RunwaySpringController
{
  public static final String          API_PATH = "admin";

  @Autowired
  protected BackupAndRestoreServiceIF service;

  @PostMapping(API_PATH + "/restore")
  public ResponseEntity<Void> restore(@Valid @ModelAttribute MultipartFile file) throws IOException
  {
    try (InputStream istream = file.getInputStream())
    {
      this.service.restore(this.getSessionId(), istream);

      return new ResponseEntity<Void>(HttpStatus.OK);
    }
  }

  @PostMapping(API_PATH + "/delete-data")
  public ResponseEntity<Void> deleteData()
  {
    this.service.deleteData(this.getSessionId());

    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/backup")
  public ResponseEntity<InputStreamResource> backup()
  {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=backup-" + System.currentTimeMillis() + ".zip");

    InputStreamResource isr = new InputStreamResource(service.backup(this.getSessionId()));
    return new ResponseEntity<InputStreamResource>(isr, headers, HttpStatus.OK);
  }

}
