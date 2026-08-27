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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.geoprism.registry.controller.EdgeClassController.CodeBody;
import net.geoprism.registry.service.request.ConceptSetServiceIF;
import net.geoprism.registry.view.ConceptSetDTO;
import net.geoprism.registry.view.DiscreteType;

@RestController
@Validated
@RequestMapping("concept-set")
public class ConceptSetController extends RunwaySpringController
{
  public static class DiscreteTypeBody extends CodeBody
  {
    @NotNull
    private DiscreteType discreteType;

    public DiscreteType getDiscreteType()
    {
      return discreteType;
    }

    public void setDiscreteType(DiscreteType discreteType)
    {
      this.discreteType = discreteType;
    }
  }

  @Autowired
  private ConceptSetServiceIF service;

  @GetMapping("/get-all")
  public ResponseEntity<List<ConceptSetDTO>> getAll(@NotNull @RequestParam(name = "discreteType") DiscreteType discreteType)
  {
    List<ConceptSetDTO> response = service.getAll(this.getSessionId());

    return ResponseEntity.ok(response);
  }

  @GetMapping("/get")
  public ResponseEntity<ConceptSetDTO> get(@NotNull @RequestParam(name = "discreteType") DiscreteType discreteType, @NotBlank @RequestParam(name = "code") String code)
  {
    ConceptSetDTO response = service.getByCode(this.getSessionId(), code);

    return ResponseEntity.ok(response);
  }

  @PostMapping("/apply")
  public ResponseEntity<ConceptSetDTO> apply(@RequestBody ConceptSetDTO type)
  {
    ConceptSetDTO response = this.service.apply(this.getSessionId(), type);

    return ResponseEntity.ok(response);
  }

  @PostMapping("/remove")
  public ResponseEntity<Void> remove(@Valid @RequestBody DiscreteTypeBody body)
  {
    this.service.delete(this.getSessionId(), body.getCode());

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  }
}
