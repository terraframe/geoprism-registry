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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import net.geoprism.registry.service.request.UndirectedGraphTypeService;
import net.geoprism.registry.view.ImportHistoryView;

@RestController
@RequestMapping("api/undirected-graph-type")
@Validated
public class UndirectedGraphTypeController extends RunwaySpringController
{
  public static final class CodeBody
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

  @Autowired
  private UndirectedGraphTypeService service;

  @GetMapping("/get-all")
  public ResponseEntity<List<GraphTypeDTO>> getAll()
  {
    List<GraphTypeDTO> response = this.service.getAll(this.getSessionId());

    return ResponseEntity.ok(response);
  }

  @PostMapping("/apply")
  public ResponseEntity<GraphTypeDTO> apply(@RequestBody GraphTypeDTO type)
  {
    GraphTypeDTO response = this.service.apply(this.getSessionId(), type);

    return ResponseEntity.ok(response);
  }

  @PostMapping("/remove")
  public ResponseEntity<Void> remove(@Valid @RequestBody CodeBody body)
  {
    this.service.remove(this.getSessionId(), body.code);

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  }

  @GetMapping("/get")
  public ResponseEntity<GraphTypeDTO> get(@NotEmpty @RequestParam(name = "code") String code)
  {
    GraphTypeDTO response = this.service.get(this.getSessionId(), code);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/get-import-history")
  public ResponseEntity<List<ImportHistoryView>> getImportHistory(@NotEmpty @RequestParam(name = "code") String code)
  {
    List<ImportHistoryView> response = this.service.getHistory(this.getSessionId(), code);

    return ResponseEntity.ok(response);
  }
}
