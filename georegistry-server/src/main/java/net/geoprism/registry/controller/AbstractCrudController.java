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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import net.geoprism.registry.service.request.AbstractCrudServiceIF;

public abstract class AbstractCrudController<T, S extends AbstractCrudServiceIF<T>> extends RunwaySpringController
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

  private S service;

  public AbstractCrudController(S service)
  {
    this.service = service;
  }

  protected S getService()
  {
    return service;
  }

  @GetMapping("/get-all")
  public ResponseEntity<List<T>> getAll()
  {
    List<T> sources = this.service.getAll(this.getSessionId());

    return new ResponseEntity<List<T>>(sources, HttpStatus.OK);
  }

  @PostMapping("/apply")
  public ResponseEntity<T> apply(@Valid @RequestBody T source)
  {
    T response = this.service.apply(this.getSessionId(), source);

    return new ResponseEntity<T>(response, HttpStatus.OK);
  }

  @PostMapping("/remove")
  public ResponseEntity<Void> remove(@Valid @RequestBody CodeBody body)
  {
    this.service.delete(this.getSessionId(), body.getCode());

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  }

  @GetMapping("/get")
  public ResponseEntity<T> get(@NotEmpty @RequestParam(name = "code") String code)
  {
    T source = this.service.getByCode(this.getSessionId(), code);

    return new ResponseEntity<T>(source, HttpStatus.OK);
  }

}
