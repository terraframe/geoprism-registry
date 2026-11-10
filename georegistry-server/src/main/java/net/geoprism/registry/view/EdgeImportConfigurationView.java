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
package net.geoprism.registry.view;

import java.util.Date;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.view.serialization.DateDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;

public class EdgeImportConfigurationView
{
  @NotEmpty
  private String         graphTypeCode;

  @NotEmpty
  private String         graphTypeClass;

  @JsonDeserialize(using = DateDeserializer.class)
  private Date           startDate;

  @JsonDeserialize(using = DateDeserializer.class)
  private Date           endDate;

  @NotNull(message = "file requires a value")
  private MultipartFile  file;

  @NotNull(message = "Import Strategy requires a value")
  private ImportStrategy strategy;

  private String         dataSource;

  private String         description;

  public String getGraphTypeCode()
  {
    return graphTypeCode;
  }

  public void setGraphTypeCode(String graphTypeCode)
  {
    this.graphTypeCode = graphTypeCode;
  }

  public String getGraphTypeClass()
  {
    return graphTypeClass;
  }

  public void setGraphTypeClass(String graphTypeClass)
  {
    this.graphTypeClass = graphTypeClass;
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

  public ImportStrategy getStrategy()
  {
    return strategy;
  }

  public void setStrategy(ImportStrategy strategy)
  {
    this.strategy = strategy;
  }

  public String getDataSource()
  {
    return dataSource;
  }

  public void setDataSource(String dataSource)
  {
    this.dataSource = dataSource;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public static EdgeImportConfigurationView of(String graphTypeClass, String graphTypeCode, Date startDate, Date endDate, String dataSource, ImportStrategy strategy)
  {
    EdgeImportConfigurationView view = new EdgeImportConfigurationView();
    view.setGraphTypeClass(graphTypeClass);
    view.setGraphTypeCode(graphTypeCode);
    view.setStartDate(startDate);
    view.setEndDate(endDate);
    view.setDataSource(dataSource);
    view.setStrategy(strategy);

    return view;
  }

}
