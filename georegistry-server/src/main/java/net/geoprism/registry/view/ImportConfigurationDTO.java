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
package net.geoprism.registry.view;

import java.util.Date;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.runwaysdk.dataaccess.ProgrammingErrorException;

import net.geoprism.registry.etl.FormatSpecificImporterFactory.FormatImporterType;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.excel.SheetDTO;
import net.geoprism.registry.view.serialization.DateDeserializer;
import net.geoprism.registry.view.serialization.DateSerializer;

public abstract class ImportConfigurationDTO extends HistoryConfigurationDTO
{
  private FormatImporterType formatType;

  private String             historyId;

  private String             jobId;

  private String             vaultFileId;

  private String             fileName;

  private ImportStrategy     importStrategy;

  private String             externalSystemId;

  private Boolean            isExternal;

  private ColumnFunctionDTO  externalIdAttributeTarget;

  private Boolean            copyBlank;

  private Boolean            ignoreProjection;

  private String             description;

  @JsonSerialize(using = DateSerializer.class)
  @JsonDeserialize(using = DateDeserializer.class)
  private Date               startDate;

  @JsonSerialize(using = DateSerializer.class)
  @JsonDeserialize(using = DateDeserializer.class)
  private Date               endDate;

  private String             dataSource;

  private SheetDTO           sheet;

  public ImportConfigurationDTO()
  {
    this.copyBlank = false;
    this.ignoreProjection = false;
    this.isExternal = false;
  }

  public FormatImporterType getFormatType()
  {
    return formatType;
  }

  public void setFormatType(FormatImporterType formatType)
  {
    this.formatType = formatType;
  }

  public String getHistoryId()
  {
    return historyId;
  }

  public void setHistoryId(String historyId)
  {
    this.historyId = historyId;
  }

  public String getJobId()
  {
    return jobId;
  }

  public void setJobId(String jobId)
  {
    this.jobId = jobId;
  }

  public String getVaultFileId()
  {
    return vaultFileId;
  }

  public void setVaultFileId(String vaultFileId)
  {
    this.vaultFileId = vaultFileId;
  }

  public String getFileName()
  {
    return fileName;
  }

  public void setFileName(String fileName)
  {
    this.fileName = fileName;
  }

  public ImportStrategy getImportStrategy()
  {
    return importStrategy;
  }

  public void setImportStrategy(ImportStrategy importStrategy)
  {
    this.importStrategy = importStrategy;
  }

  public String getExternalSystemId()
  {
    return externalSystemId;
  }

  public void setExternalSystemId(String externalSystemId)
  {
    this.externalSystemId = externalSystemId;
  }

  public Boolean getIsExternal()
  {
    return isExternal;
  }

  public void setIsExternal(Boolean isExternal)
  {
    this.isExternal = isExternal;
  }

  public ColumnFunctionDTO getExternalIdAttributeTarget()
  {
    return externalIdAttributeTarget;
  }

  public void setExternalIdAttributeTarget(ColumnFunctionDTO externalIdAttributeTarget)
  {
    this.externalIdAttributeTarget = externalIdAttributeTarget;
  }

  public Boolean getCopyBlank()
  {
    return copyBlank;
  }

  public void setCopyBlank(Boolean copyBlank)
  {
    this.copyBlank = copyBlank;
  }

  public Boolean getIgnoreProjection()
  {
    return ignoreProjection;
  }

  public void setIgnoreProjection(Boolean ignoreProjection)
  {
    this.ignoreProjection = ignoreProjection;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
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

  public String getDataSource()
  {
    return dataSource;
  }

  public void setDataSource(String dataSource)
  {
    this.dataSource = dataSource;
  }

  public SheetDTO getSheet()
  {
    return sheet;
  }

  public void setSheet(SheetDTO sheet)
  {
    this.sheet = sheet;
  }

  public static ImportConfigurationDTO parseJson(String json)
  {
    try
    {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.readValue(json, ImportConfigurationDTO.class);
    }
    catch (JsonProcessingException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public static String toJson(ImportConfigurationDTO dto)
  {
    try
    {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.writeValueAsString(dto);
    }
    catch (JsonProcessingException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

}
