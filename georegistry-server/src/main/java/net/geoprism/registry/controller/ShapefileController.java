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
import java.util.Date;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.service.ShapefileService;

@RestController
public class ShapefileController extends RunwaySpringController
{
  public static final class GetConfigurationInput
  {
    @NotEmpty(message = "Import type requires a value")
    private String        type;

    @JsonDeserialize(using = NullableDateDeserializer.class)
    private Date          startDate;

    @JsonDeserialize(using = NullableDateDeserializer.class)
    private Date          endDate;

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

    public Date getStartDate()
    {
      return startDate;
    }

    public void setStartDate(Date startDate)
    {
      this.startDate = startDate;
    }

    public Date getEndDate()
    {
      return endDate;
    }

    public void setEndDate(Date endDate)
    {
      this.endDate = endDate;
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

  public static final String API_PATH = "shapefile";

  @Autowired
  private ShapefileService   service;

  @PostMapping(API_PATH + "/get-shapefile-configuration")
  public ResponseEntity<String> getShapefileConfiguration(@Valid @ModelAttribute GetConfigurationInput input) throws IOException
  {
    String sessionId = this.getSessionId();

    try (InputStream stream = input.file.getInputStream())
    {
      String fileName = input.file.getOriginalFilename();

      ImportStrategy strategy = ImportStrategy.valueOf(input.strategy);

      JSONObject configuration = service.getShapefileConfiguration(sessionId, input.type, input.startDate, input.endDate, fileName, stream, strategy, input.copyBlank);

      return new ResponseEntity<String>(configuration.toString(), HttpStatus.OK);
    }
  }
}
