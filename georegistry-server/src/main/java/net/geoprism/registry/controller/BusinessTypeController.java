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

import org.commongeoregistry.adapter.metadata.AttributeType;
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

import com.google.gson.JsonObject;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.geoprism.registry.service.request.BusinessEdgeTypeServiceIF;
import net.geoprism.registry.service.request.BusinessTypeService;
import net.geoprism.registry.view.BusinessEdgeTypeView;
import net.geoprism.registry.view.BusinessTypeDTO;
import net.geoprism.registry.view.OrganizationGroup;

@RestController
@RequestMapping("api/business-type")
@Validated
public class BusinessTypeController extends RunwaySpringController
{
  public static class RemoveAttributeBody
  {
    @NotBlank
    String typeCode;

    @NotBlank
    String attributeName;

    public String getTypeCode()
    {
      return typeCode;
    }

    public void setTypeCode(String typeCode)
    {
      this.typeCode = typeCode;
    }

    public String getAttributeName()
    {
      return attributeName;
    }

    public void setAttributeName(String attributeName)
    {
      this.attributeName = attributeName;
    }

  }

  public static class AttributeTypeBody
  {
    @NotBlank
    String        typeCode;

    @NotNull
    AttributeType attributeType;

    public String getTypeCode()
    {
      return typeCode;
    }

    public void setTypeCode(String typeCode)
    {
      this.typeCode = typeCode;
    }

    public AttributeType getAttributeType()
    {
      return attributeType;
    }

    public void setAttributeType(AttributeType attributeType)
    {
      this.attributeType = attributeType;
    }
  }

  public static class OidBody
  {
    @NotBlank
    private String oid;

    public String getOid()
    {
      return oid;
    }

    public void setOid(String oid)
    {
      this.oid = oid;
    }
  }

  @Autowired
  private BusinessTypeService       service;

  @Autowired
  private BusinessEdgeTypeServiceIF edgeService;

  @GetMapping("/get-by-org")
  public ResponseEntity<List<OrganizationGroup<BusinessTypeDTO>>> getByOrg()
  {
    List<OrganizationGroup<BusinessTypeDTO>> response = service.listByOrg(this.getSessionId());

    return ResponseEntity.ok(response);
  }

  @GetMapping("/get-all")
  public ResponseEntity<List<BusinessTypeDTO>> getAll()
  {
    List<BusinessTypeDTO> response = service.getAll(this.getSessionId());

    return ResponseEntity.ok(response);
  }

  @GetMapping("/get")
  public ResponseEntity<BusinessTypeDTO> get(@NotBlank @RequestParam(name = "oid") String oid)
  {
    BusinessTypeDTO response = service.get(this.getSessionId(), oid);

    return ResponseEntity.ok(response);
  }

  @PostMapping("/apply")
  public ResponseEntity<BusinessTypeDTO> apply(@RequestBody BusinessTypeDTO type)
  {
    BusinessTypeDTO response = this.service.apply(this.getSessionId(), type);

    return ResponseEntity.ok(response);
  }

  @PostMapping("/remove")
  public ResponseEntity<Void> remove(@Valid @RequestBody OidBody body)
  {
    this.service.remove(this.getSessionId(), body.oid);

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  }

  @PostMapping("/edit")
  public ResponseEntity<BusinessTypeDTO> edit(@Valid @RequestBody OidBody body)
  {
    BusinessTypeDTO response = this.service.edit(this.getSessionId(), body.oid);

    return ResponseEntity.ok(response);
  }

  @PostMapping("/unlock")
  public ResponseEntity<Void> unlock(@Valid @RequestBody OidBody body)
  {
    this.service.unlock(this.getSessionId(), body.oid);

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  }

  @PostMapping("/add-attribute")
  public ResponseEntity<String> createAttributeType(@Valid @RequestBody AttributeTypeBody body)
  {
    AttributeType attrType = this.service.createAttributeType(this.getSessionId(), body.typeCode, body.attributeType);
    JsonObject response = attrType.toJSON();

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping("/update-attribute")
  public ResponseEntity<String> updateAttributeType(@Valid @RequestBody AttributeTypeBody body)
  {
    AttributeType attrType = this.service.updateAttributeType(this.getSessionId(), body.typeCode, body.attributeType);
    JsonObject response = attrType.toJSON();

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping("/remove-attribute")
  public ResponseEntity<Void> removeAttributeType(@Valid @RequestBody RemoveAttributeBody body)
  {
    this.service.removeAttributeType(this.getSessionId(), body.typeCode, body.attributeName);

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  }

  @GetMapping("/data")
  public ResponseEntity<String> data(@NotBlank @RequestParam(name = "typeCode") String typeCode, @RequestParam(required = false, name = "criteria") String criteria)
  {
    JsonObject page = this.service.data(this.getSessionId(), typeCode, criteria);

    return new ResponseEntity<String>(page.toString(), HttpStatus.OK);
  }

  @GetMapping("/get-edge-types")
  public ResponseEntity<List<BusinessEdgeTypeView>> getEdgeTypes(@NotBlank @RequestParam(name = "typeCode") String typeCode)
  {
    List<BusinessEdgeTypeView> edgeTypes = this.service.getEdgeTypes(this.getSessionId(), typeCode);

    return ResponseEntity.ok(edgeTypes);
  }
}
