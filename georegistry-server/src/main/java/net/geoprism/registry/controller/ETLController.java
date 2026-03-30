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

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;
import com.runwaysdk.resource.StreamResource;
import com.runwaysdk.system.scheduler.JobHistory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import net.geoprism.registry.service.request.ETLService;
import net.geoprism.registry.spring.JsonObjectDeserializer;
import net.geoprism.registry.spring.NullableDateDeserializer;
import net.geoprism.registry.view.ImportHistoryView;

@RestController
@RequestMapping("api/etl")
@Validated
public class ETLController extends RunwaySpringController
{
  public static class HistoryIdBody
  {
    @NotEmpty
    private String historyId;

    public String getHistoryId()
    {
      return historyId;
    }

    public void setHistoryId(String historyId)
    {
      this.historyId = historyId;
    }
  }

  public static class ConfigBody
  {
    @NotNull
    @JsonDeserialize(using = JsonObjectDeserializer.class)
    JsonObject config;

    public JsonObject getConfig()
    {
      return config;
    }

    public void setConfig(JsonObject config)
    {
      this.config = config;
    }
  }

  public static class ReImportConfigBody
  {
    @NotEmpty
    String                config;

    private MultipartFile file;

    public MultipartFile getFile()
    {
      return file;
    }

    public void setFile(MultipartFile file)
    {
      this.file = file;
    }

    public String getConfig()
    {
      return config;
    }

    public void setConfig(String config)
    {
      this.config = config;
    }
  }

  public static class EdgeImportBody
  {
    @JsonDeserialize(using = NullableDateDeserializer.class)
    private Date          startDate;

    @JsonDeserialize(using = NullableDateDeserializer.class)
    private Date          endDate;

    private String        dataSource;

    private MultipartFile file;

    public MultipartFile getFile()
    {
      return file;
    }

    public void setFile(MultipartFile file)
    {
      this.file = file;
    }
  }

  @Autowired
  protected ETLService service;

  @PostMapping("/reimport")
  public ResponseEntity<String> doReImport(@Valid @ModelAttribute ReImportConfigBody body)
  {
    JsonObject config = this.service.reImport(this.getSessionId(), body.file, body.config);

    return new ResponseEntity<String>(config.toString(), HttpStatus.OK);
  }

  @PostMapping("/import")
  public ResponseEntity<String> doImport(@Valid @RequestBody ConfigBody body)
  {
    JsonObject config = this.service.doImport(this.getSessionId(), body.config.toString());

    return new ResponseEntity<String>(config.toString(), HttpStatus.OK);
  }

  @PostMapping("/validation-resolve")
  public ResponseEntity<Void> submitValidationProblemResolution(@Valid @RequestBody ConfigBody body)
  {
    this.service.submitValidationProblemResolution(this.getSessionId(), body.config.toString());

    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  @PostMapping("/error-resolve")
  public ResponseEntity<Void> submitImportErrorResolution(@Valid @RequestBody ConfigBody body)
  {
    this.service.submitImportErrorResolution(this.getSessionId(), body.config.toString());

    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  @PostMapping("/import-resolve")
  public ResponseEntity<Void> resolveImport(@Valid @RequestBody HistoryIdBody body)
  {
    this.service.resolveImport(this.getSessionId(), body.getHistoryId());

    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  @GetMapping("/get-active")
  public ResponseEntity<String> getActiveImports( //
      @RequestParam(required = false, name = "pageSize") Integer pageSize, //
      @RequestParam(required = false, name = "pageNumber") Integer pageNumber, //
      @RequestParam(required = false, name = "sortAttr") String sortAttr, //
      @RequestParam(required = false, name = "isAscending") Boolean isAscending)
  {
    if (sortAttr == null || sortAttr.equals(""))
    {
      sortAttr = JobHistory.CREATEDATE;
    }

    if (isAscending == null)
    {
      isAscending = true;
    }

    JsonObject config = this.service.getActiveImports(this.getSessionId(), pageSize, pageNumber, sortAttr, isAscending);

    return new ResponseEntity<String>(config.toString(), HttpStatus.OK);
  }

  @GetMapping("/get-completed")
  public ResponseEntity<String> getCompletedImports( //
      @RequestParam(required = false, name = "pageSize") Integer pageSize, //
      @RequestParam(required = false, name = "pageNumber") Integer pageNumber, //
      @RequestParam(required = false, name = "sortAttr") String sortAttr, //
      @RequestParam(required = false, name = "isAscending") Boolean isAscending)
  {
    if (sortAttr == null || sortAttr.equals(""))
    {
      sortAttr = JobHistory.CREATEDATE;
    }

    if (isAscending == null)
    {
      isAscending = true;
    }

    JsonObject json = this.service.getCompletedImports(this.getSessionId(), pageSize, pageNumber, sortAttr, isAscending);

    return new ResponseEntity<String>(json.toString(), HttpStatus.OK);
  }

  @GetMapping("/get-errors")
  public ResponseEntity<String> getImportErrors( //
      @RequestParam(required = false, name = "historyId") String historyId, //
      @RequestParam(required = false, name = "onlyUnresolved") Boolean onlyUnresolved, //
      @RequestParam(required = false, name = "pageSize") Integer pageSize, //
      @RequestParam(required = false, name = "pageNumber") Integer pageNumber)
  {
    JsonObject json = this.service.getImportErrors(this.getSessionId(), historyId, onlyUnresolved, pageSize, pageNumber);

    return new ResponseEntity<String>(json.toString(), HttpStatus.OK);
  }

  @GetMapping("/get-import-details")
  public ResponseEntity<String> getImportDetails( //
      @RequestParam(required = false, name = "historyId") String historyId, //
      @RequestParam(required = false, name = "onlyUnresolved") Boolean onlyUnresolved, //
      @RequestParam(required = false, name = "pageSize") Integer pageSize, //
      @RequestParam(required = false, name = "pageNumber") Integer pageNumber)
  {
    JsonObject details = this.service.getImportDetails(this.getSessionId(), historyId, onlyUnresolved, pageSize, pageNumber);

    return new ResponseEntity<String>(details.toString(), HttpStatus.OK);
  }

  @PostMapping("/cancel-import")
  public ResponseEntity<Void> cancelImport(@Valid @RequestBody ConfigBody body)
  {
    this.service.cancelImport(this.getSessionId(), body.config.toString());

    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  @GetMapping("/get-export-details")
  public ResponseEntity<String> getExportDetails( //
      @RequestParam(required = false, name = "historyId") String historyId, //
      @RequestParam(required = false, name = "pageSize") Integer pageSize, //
      @RequestParam(required = false, name = "pageNumber") Integer pageNumber)
  {
    JsonObject details = this.service.getExportDetails(this.getSessionId(), historyId, pageSize, pageNumber);

    return new ResponseEntity<String>(details.toString(), HttpStatus.OK);
  }

  @PostMapping("/import-edge-json")
  public ResponseEntity<Void> importEdgeJson(@Valid @ModelAttribute EdgeImportBody body) throws IOException, JSONException, ParseException
  {
    try (InputStream stream = body.file.getInputStream())
    {
      service.importEdgeJson(this.getSessionId(), body.startDate, body.endDate, body.dataSource, new StreamResource(stream, body.file.getOriginalFilename()));

      return new ResponseEntity<Void>(HttpStatus.OK);
    }
  }

  @GetMapping("/get-import-history")
  public ResponseEntity<List<ImportHistoryView>> getImportHistory( //
      @NotEmpty @RequestParam(name = "classType") String classType, //
      @NotEmpty @RequestParam(name = "typeCode") String typeCode //
  )
  {
    List<ImportHistoryView> response = this.service.getHistory(this.getSessionId(), classType, typeCode);

    return ResponseEntity.ok(response);
  }

}
