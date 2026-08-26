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

import java.util.List;

import org.commongeoregistry.adapter.metadata.GraphTypeDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import net.geoprism.registry.graph.EdgeClass;
import net.geoprism.registry.service.request.EdgeClassServiceIF;

public abstract class EdgeClassController<T extends EdgeClass, D extends GraphTypeDTO> extends RunwaySpringController
{
  public static class CodeBody
  {
    @NotBlank
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

  protected abstract EdgeClassServiceIF<T, D> getService();

  @GetMapping("/get-all")
  public ResponseEntity<List<D>> getAll()
  {
    List<D> all = this.getService().getAll(this.getSessionId());

    return ResponseEntity.ok(all);
  }

  @PostMapping("/apply")
  public ResponseEntity<D> apply(@RequestBody D type)
  {
    D response = this.getService().apply(this.getSessionId(), type);

    return ResponseEntity.ok(response);
  }

  @PostMapping("/remove")
  public ResponseEntity<Void> remove(@Valid @RequestBody CodeBody body)
  {
    this.getService().delete(this.getSessionId(), body.code);

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  }

  @GetMapping("/get")
  public ResponseEntity<D> get(@NotBlank @RequestParam(name = "code") String code)
  {
    D response = this.getService().getByCode(this.getSessionId(), code);

    return ResponseEntity.ok(response);
  }
}
