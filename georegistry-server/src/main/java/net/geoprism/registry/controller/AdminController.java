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

import java.io.IOException;
import java.io.InputStream;

import jakarta.validation.Valid;

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
