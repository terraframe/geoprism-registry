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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

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

import net.geoprism.registry.controller.UndirectedGraphTypeController.TypeBody;
import net.geoprism.registry.service.DirectedAcyclicGraphTypeService;
import net.geoprism.registry.spring.JsonObjectDeserializer;

@RestController
@Validated
public class DirectedAcyclicGraphTypeController extends RunwaySpringController
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

  public static final class TypeBody
  {
    @NotNull
    @JsonDeserialize(using = JsonObjectDeserializer.class)
    private JsonObject type;

    public JsonObject getType()
    {
      return type;
    }

    public void setType(JsonObject type)
    {
      this.type = type;
    }
  }

  public static final String API_PATH = "directed-graph-type";

  @Autowired
  private DirectedAcyclicGraphTypeService service;

  @GetMapping(API_PATH + "/get-all")
  public ResponseEntity<String> getAll()
  {
    JsonArray response = this.service.getAll(this.getSessionId());

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/create")
  public ResponseEntity<String> create(@RequestBody TypeBody body)
  {
    JsonObject response = this.service.create(this.getSessionId(), body.type);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/update")
  public ResponseEntity<String> update(@RequestBody TypeBody body)
  {
    JsonObject response = this.service.update(this.getSessionId(), body.type);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/remove")
  public ResponseEntity<Void> remove(@Valid
  @RequestBody CodeBody body)
  {
    this.service.remove(this.getSessionId(), body.code);

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  }

  @GetMapping(API_PATH + "/get")
  public ResponseEntity<String> get(@NotEmpty
  @RequestParam String code)
  {
    JsonObject response = this.service.get(this.getSessionId(), code);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }
}
