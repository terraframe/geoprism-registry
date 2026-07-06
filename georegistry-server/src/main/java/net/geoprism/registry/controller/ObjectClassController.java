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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.gson.JsonObject;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.geoprism.registry.graph.ObjectClass;
import net.geoprism.registry.service.request.ObjectClassServiceIF;
import net.geoprism.registry.view.OrganizationGroup;

public abstract class ObjectClassController<T extends ObjectClass, D> extends RunwaySpringController
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

  protected abstract ObjectClassServiceIF<T, D> getService();

  @GetMapping("/get-by-org")
  public ResponseEntity<List<OrganizationGroup<D>>> getByOrg()
  {
    List<OrganizationGroup<D>> response = getService().listByOrg(this.getSessionId());

    return ResponseEntity.ok(response);
  }

  @GetMapping("/get-all")
  public ResponseEntity<List<D>> getAll()
  {
    List<D> response = getService().getAll(this.getSessionId());

    return ResponseEntity.ok(response);
  }

  @GetMapping("/get")
  public ResponseEntity<D> get(@NotBlank @RequestParam(name = "oid") String oid)
  {
    D response = getService().get(this.getSessionId(), oid);

    return ResponseEntity.ok(response);
  }

  @PostMapping("/apply")
  public ResponseEntity<D> apply(@RequestBody D type)
  {
    D response = this.getService().apply(this.getSessionId(), type);

    return ResponseEntity.ok(response);
  }

  @PostMapping("/remove")
  public ResponseEntity<Void> remove(@Valid @RequestBody OidBody body)
  {
    this.getService().remove(this.getSessionId(), body.oid);

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  }

  @PostMapping("/edit")
  public ResponseEntity<D> edit(@Valid @RequestBody OidBody body)
  {
    D response = this.getService().edit(this.getSessionId(), body.oid);

    return ResponseEntity.ok(response);
  }

  @PostMapping("/unlock")
  public ResponseEntity<Void> unlock(@Valid @RequestBody OidBody body)
  {
    this.getService().unlock(this.getSessionId(), body.oid);

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  }

  @PostMapping("/add-attribute")
  public ResponseEntity<String> createAttributeType(@Valid @RequestBody AttributeTypeBody body)
  {
    AttributeType attrType = this.getService().createAttributeType(this.getSessionId(), body.typeCode, body.attributeType);
    JsonObject response = attrType.toJSON();

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping("/update-attribute")
  public ResponseEntity<String> updateAttributeType(@Valid @RequestBody AttributeTypeBody body)
  {
    AttributeType attrType = this.getService().updateAttributeType(this.getSessionId(), body.typeCode, body.attributeType);
    JsonObject response = attrType.toJSON();

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping("/remove-attribute")
  public ResponseEntity<Void> removeAttributeType(@Valid @RequestBody RemoveAttributeBody body)
  {
    this.getService().removeAttributeType(this.getSessionId(), body.typeCode, body.attributeName);

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  }
}
