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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.commongeoregistry.adapter.metadata.AttributeType;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.geoprism.registry.service.BusinessTypeService;
import net.geoprism.registry.spring.JsonObjectDeserializer;

@RestController
@Validated
public class BusinessTypeController extends RunwaySpringController
{
  public static class RemoveAttributeBody
  {
    @NotEmpty
    String typeCode;

    @NotEmpty
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
    @NotEmpty
    String     typeCode;

    @NotNull
    @JsonDeserialize(using = JsonObjectDeserializer.class)
    JsonObject attributeType;

    public String getTypeCode()
    {
      return typeCode;
    }

    public void setTypeCode(String typeCode)
    {
      this.typeCode = typeCode;
    }

    public JsonObject getAttributeType()
    {
      return attributeType;
    }

    public void setAttributeType(JsonObject attributeType)
    {
      this.attributeType = attributeType;
    }
  }

  public static class OidBody
  {
    @NotEmpty
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

  public static final String API_PATH = "business-type";

  @Autowired
  private BusinessTypeService service;

  @GetMapping(API_PATH + "/get-by-org")
  public ResponseEntity<String> getByOrg()
  {
    JsonArray response = service.listByOrg(this.getSessionId());

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get-all")
  public ResponseEntity<String> getAll()
  {
    JsonArray response = service.getAll(this.getSessionId());
    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get")
  public ResponseEntity<String> get(@NotEmpty
  @RequestParam String oid)
  {
    JsonObject response = service.get(this.getSessionId(), oid);
    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/apply")
  public ResponseEntity<String> apply(@RequestBody String type)
  {
    JsonObject response = this.service.apply(this.getSessionId(), type);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/remove")
  public ResponseEntity<Void> remove(@Valid @RequestBody OidBody body)
  {
    this.service.remove(this.getSessionId(), body.oid);

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  }

  @PostMapping(API_PATH + "/edit")
  public ResponseEntity<String> edit(@Valid @RequestBody OidBody body)
  {
    JsonObject response = this.service.edit(this.getSessionId(), body.oid);
    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/unlock")
  public ResponseEntity<Void> unlock(@Valid @RequestBody OidBody body)
  {
    this.service.unlock(this.getSessionId(), body.oid);

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  }

  @PostMapping(API_PATH + "/add-attribute")
  public ResponseEntity<String> createAttributeType(@Valid @RequestBody AttributeTypeBody body)
  {
    AttributeType attrType = this.service.createAttributeType(this.getSessionId(), body.typeCode, body.attributeType);
    JsonObject response = attrType.toJSON();

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/update-attribute")
  public ResponseEntity<String> updateAttributeType(@Valid @RequestBody AttributeTypeBody body)
  {
    AttributeType attrType = this.service.updateAttributeType(this.getSessionId(), body.typeCode, body.attributeType);
    JsonObject response = attrType.toJSON();

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/remove-attribute")
  public ResponseEntity<Void> removeAttributeType(@Valid @RequestBody RemoveAttributeBody body)
  {
    this.service.removeAttributeType(this.getSessionId(), body.typeCode, body.attributeName);

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  }

  @GetMapping(API_PATH + "/data")
  public ResponseEntity<String> data(@NotEmpty
  @RequestParam String typeCode, @RequestParam(required = false) String criteria)
  {
    JsonObject page = this.service.data(this.getSessionId(), typeCode, criteria);

    return new ResponseEntity<String>(page.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get-edge-types")
  public ResponseEntity<String> getEdgeTypes(@NotEmpty
  @RequestParam String typeCode)
  {
    JsonArray edgeTypes = this.service.getEdgeTypes(this.getSessionId(), typeCode);

    return new ResponseEntity<String>(edgeTypes.toString(), HttpStatus.OK);
  }

}
