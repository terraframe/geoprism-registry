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

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.NotBlank;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.graph.ConceptClass;
import net.geoprism.registry.model.ConceptObject;
import net.geoprism.registry.service.request.ConceptObjectService;
import net.geoprism.registry.view.ConceptClassDTO;
import net.geoprism.registry.view.NodeDTO;
import net.geoprism.registry.view.ObjectOverTimeDTO;
import net.geoprism.registry.view.Page;

@RestController
@Validated
@RequestMapping(RegistryConstants.CONTROLLER_ROOT + "concept-object")
public class ConceptObjectController extends ObjectController<ConceptObject, ConceptClass, ConceptClassDTO>
{
  public ConceptObjectController(ConceptObjectService service)
  {
    super(service);
  }

  @Override
  protected ConceptObjectService getService()
  {
    return (ConceptObjectService) super.getService();
  }

  @GetMapping("/search")
  public ResponseEntity<List<ObjectOverTimeDTO>> search( //
      @NotBlank @RequestParam(name = "typeCode") String typeCode, //
      @NotBlank @RequestParam(name = "attribute") String attribute, //
      @RequestParam(name = "text") String text)
  {
    List<ObjectOverTimeDTO> response = this.getService().search(getSessionId(), typeCode, attribute, text);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/search-class")
  public ResponseEntity<List<ObjectOverTimeDTO>> search( //
      @NotBlank @RequestParam(name = "conceptClass") String conceptClass, //
      @RequestParam(name = "text") String text)
  {
    List<ObjectOverTimeDTO> response = this.getService().search(getSessionId(), conceptClass, text);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/get-children")
  public ResponseEntity<Page<ObjectOverTimeDTO>> getChildren( //
      @NotBlank @RequestParam(name = "concept") String concept, //
      @NotBlank @RequestParam(name = "typeCode") String typeCode, //
      @NotBlank @RequestParam(name = "attribute") String attribute, //
      @RequestParam(name = "pageSize", required = false, defaultValue = "20") Integer pageSize, //
      @RequestParam(name = "pageNumber", required = false, defaultValue = "1") Integer pageNumber)
  {
    Page<ObjectOverTimeDTO> response = this.getService().getChildren(getSessionId(), concept, typeCode, attribute, pageSize, pageNumber);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/get-ancestor-tree")
  public ResponseEntity<NodeDTO<ObjectOverTimeDTO>> getAncestorTree( //
      @NotBlank @RequestParam(name = "concept") String concept, //
      @NotBlank @RequestParam(name = "typeCode") String typeCode, //
      @NotBlank @RequestParam(name = "attribute") String attribute, //
      @RequestParam(name = "pageSize", required = false, defaultValue = "20") Integer pageSize)
  {
    NodeDTO<ObjectOverTimeDTO> response = this.getService().getAncestorTree(getSessionId(), concept, typeCode, attribute, pageSize);

    return ResponseEntity.ok(response);
  }

}
