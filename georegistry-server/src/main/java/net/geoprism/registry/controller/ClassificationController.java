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

import net.geoprism.registry.service.ClassificationService;
import net.geoprism.registry.spring.JsonObjectDeserializer;

@RestController
@Validated
public class ClassificationController extends RunwaySpringController
{
  public static class ClassificationBody
  {
    @NotEmpty
    String classificationCode;

    @NotEmpty
    String code;

    public String getClassificationCode()
    {
      return classificationCode;
    }

    public void setClassificationCode(String classificationCode)
    {
      this.classificationCode = classificationCode;
    }

    public String getCode()
    {
      return code;
    }

    public void setCode(String code)
    {
      this.code = code;
    }
  }

  public static class MoveBody
  {
    @NotEmpty
    String classificationCode;

    @NotEmpty
    String code;

    @NotEmpty
    String parentCode;

    public String getClassificationCode()
    {
      return classificationCode;
    }

    public void setClassificationCode(String classificationCode)
    {
      this.classificationCode = classificationCode;
    }

    public String getCode()
    {
      return code;
    }

    public void setCode(String code)
    {
      this.code = code;
    }

    public String getParentCode()
    {
      return parentCode;
    }

    public void setParentCode(String parentCode)
    {
      this.parentCode = parentCode;
    }
  }

  public static class ApplyBody
  {
    @NotEmpty
    private String     classificationCode;

    @NotNull
    @JsonDeserialize(using = JsonObjectDeserializer.class)
    private JsonObject classification;

    @NotNull
    private Boolean    isNew;

    private String     parentCode;

    public String getClassificationCode()
    {
      return classificationCode;
    }

    public void setClassificationCode(String classificationCode)
    {
      this.classificationCode = classificationCode;
    }

    public String getParentCode()
    {
      return parentCode;
    }

    public void setParentCode(String parentCode)
    {
      this.parentCode = parentCode;
    }

    public JsonObject getClassification()
    {
      return classification;
    }

    public void setClassification(JsonObject classification)
    {
      this.classification = classification;
    }

    public Boolean getIsNew()
    {
      return isNew;
    }

    public void setIsNew(Boolean isNew)
    {
      this.isNew = isNew;
    }
  }

  public static final String    API_PATH = "classification";

  @Autowired
  private ClassificationService service;

  @PostMapping(API_PATH + "/apply")
  public ResponseEntity<String> apply(@Valid
  @RequestBody ApplyBody body)
  {
    JsonObject response = this.service.apply(this.getSessionId(), body.classificationCode, body.parentCode, body.classification, body.isNew);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/remove")
  public ResponseEntity<Void> remove(@Valid
  @RequestBody ClassificationBody body)
  {
    this.service.remove(this.getSessionId(), body.classificationCode, body.code);

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  }

  @GetMapping(API_PATH + "/get")
  public ResponseEntity<String> get(@NotEmpty
  @RequestParam String classificationCode,
      @NotEmpty
      @RequestParam String code)
  {
    JsonObject response = this.service.get(this.getSessionId(), classificationCode, code);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/move")
  public ResponseEntity<Void> move(@Valid
  @RequestBody MoveBody body)
  {
    this.service.move(this.getSessionId(), body.classificationCode, body.code, body.parentCode);

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  }

  @GetMapping(API_PATH + "/get-children")
  public ResponseEntity<String> getChildren(@NotEmpty
  @RequestParam String classificationCode, @RequestParam(required = false) String code, @RequestParam(required = false) Integer pageSize, @RequestParam(required = false) Integer pageNumber)
  {
    JsonObject page = this.service.getChildren(this.getSessionId(), classificationCode, code, pageSize, pageNumber);

    return new ResponseEntity<String>(page.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get-ancestor-tree")
  public ResponseEntity<String> getAncestorTree(@NotEmpty
  @RequestParam String classificationCode, @RequestParam(required = false) String rootCode,
      @NotEmpty
      @RequestParam String code, @RequestParam(required = false) Integer pageSize)
  {
    JsonObject page = this.service.getAncestorTree(this.getSessionId(), classificationCode, rootCode, code, pageSize);

    return new ResponseEntity<String>(page.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/search")
  public ResponseEntity<String> search(@NotEmpty
  @RequestParam String classificationCode, @RequestParam(required = false) String rootCode, @RequestParam(required = false) String text)
  {
    JsonArray results = this.service.search(this.getSessionId(), classificationCode, rootCode, text);

    return new ResponseEntity<String>(results.toString(), HttpStatus.OK);
  }
}
