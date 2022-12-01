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
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.controller.ShapefileController.GetConfigurationInput;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.service.ExcelService;
import net.geoprism.registry.spring.NullableDateDeserializer;

@RestController
@Validated
public class ExcelImportController extends RunwaySpringController
{
  public static final class BusinessConfigurationInput
  {
    @NotEmpty(message = "Import type requires a value")
    private String        type;

    @JsonDeserialize(using = NullableDateDeserializer.class)
    private Date          date;

    @NotNull(message = "Shapefile requires a value")
    private MultipartFile file;

    @NotEmpty(message = "Import Strategy requires a value")
    private String        strategy;

    @NotNull(message = "Import blank cells requires a value")
    private Boolean       copyBlank;

    public String getType()
    {
      return type;
    }

    public void setType(String type)
    {
      this.type = type;
    }

    public Date getDate()
    {
      return date;
    }

    public void setDate(Date date)
    {
      this.date = date;
    }

    public MultipartFile getFile()
    {
      return file;
    }

    public void setFile(MultipartFile file)
    {
      this.file = file;
    }

    public String getStrategy()
    {
      return strategy;
    }

    public void setStrategy(String strategy)
    {
      this.strategy = strategy;
    }

    public Boolean getCopyBlank()
    {
      return copyBlank;
    }

    public void setCopyBlank(Boolean copyBlank)
    {
      this.copyBlank = copyBlank;
    }

  }

  public static final String API_PATH = "excel";

  @Autowired
  private ExcelService       service;

  @PostMapping(API_PATH + "/get-configuration")
  public ResponseEntity<String> getConfiguration(@Valid @ModelAttribute GetConfigurationInput input) throws IOException
  {
    String sessionId = this.getSessionId();

    try (InputStream stream = input.getFile().getInputStream())
    {
      String fileName = input.getFile().getOriginalFilename();

      SimpleDateFormat format = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);
      format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

      ImportStrategy strategy = ImportStrategy.valueOf(input.getStrategy());

      JSONObject configuration = service.getExcelConfiguration(sessionId, input.getType(), input.getStartDate(), input.getEndDate(), fileName, stream, strategy, input.getCopyBlank());

      return new ResponseEntity<String>(configuration.toString(), HttpStatus.OK);
    }
  }

  @PostMapping(API_PATH + "/export-spreadsheet")
  public ResponseEntity<?> exportSpreadsheet(@RequestParam
  @NotEmpty String type,
      @RequestParam
      @NotEmpty String hierarchyType)
  {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=export.xlsx");

    InputStreamResource isr = new InputStreamResource(service.exportSpreadsheet(this.getSessionId(), type, hierarchyType));
    return new ResponseEntity<InputStreamResource>(isr, headers, HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/get-business-config")
  public ResponseEntity<String> getBusinessConfiguration(@Valid @ModelAttribute BusinessConfigurationInput input) throws IOException
  {
    try (InputStream stream = input.file.getInputStream())
    {
      String fileName = input.file.getOriginalFilename();

      SimpleDateFormat format = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);
      format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

      ImportStrategy strategy = ImportStrategy.valueOf(input.getStrategy());

      JSONObject configuration = service.getBusinessTypeConfiguration(this.getSessionId(), input.type, input.date, fileName, stream, strategy, input.copyBlank);

      return new ResponseEntity<String>(configuration.toString(), HttpStatus.OK);
    }
  }

}
