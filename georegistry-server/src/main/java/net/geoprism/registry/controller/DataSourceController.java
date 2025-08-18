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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import net.geoprism.registry.model.DataSourceDTO;
import net.geoprism.registry.service.request.SourceServiceIF;

@RestController
@Validated
public class DataSourceController extends RunwaySpringController
{
  public static class CodeBody
  {
    @NotEmpty
    private String code;

    public String getCode()
    {
      return code;
    }

    public void setCode(String code)
    {
      this.code = code;
    }

  }

  public static final String API_PATH = "source";

  @Autowired
  private SourceServiceIF    service;

  @GetMapping(API_PATH + "/get-all")
  public ResponseEntity<List<DataSourceDTO>> getAll()
  {
    List<DataSourceDTO> sources = this.service.getAll(this.getSessionId());

    return new ResponseEntity<List<DataSourceDTO>>(sources, HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/apply")
  public ResponseEntity<DataSourceDTO> apply(@Valid @RequestBody DataSourceDTO source)
  {
    DataSourceDTO response = this.service.apply(this.getSessionId(), source);

    return new ResponseEntity<DataSourceDTO>(response, HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/remove")
  public ResponseEntity<Void> remove(@Valid @RequestBody CodeBody body)
  {
    this.service.delete(this.getSessionId(), body.getCode());

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  }

  @GetMapping(API_PATH + "/get")
  public ResponseEntity<DataSourceDTO> get(@NotEmpty @RequestParam String code)
  {
    DataSourceDTO source = this.service.getByCode(this.getSessionId(), code);

    return new ResponseEntity<DataSourceDTO>(source, HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/search")
  public ResponseEntity<List<DataSourceDTO>> search(@RequestParam(required = false) String text)
  {
    List<DataSourceDTO> sources = this.service.search(this.getSessionId(), text);

    return new ResponseEntity<List<DataSourceDTO>>(sources, HttpStatus.OK);
  }

}
