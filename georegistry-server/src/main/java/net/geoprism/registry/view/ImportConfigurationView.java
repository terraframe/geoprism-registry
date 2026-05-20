package net.geoprism.registry.view;

import java.util.Date;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.spring.NullableDateDeserializer;

public class ImportConfigurationView
{
  @NotEmpty(message = "Import type requires a value")
  private String         type;

  @JsonDeserialize(using = NullableDateDeserializer.class)
  private Date           startDate;

  @JsonDeserialize(using = NullableDateDeserializer.class)
  private Date           endDate;

  @NotNull(message = "Shapefile requires a value")
  private MultipartFile  file;

  @NotNull(message = "Import Strategy requires a value")
  private ImportStrategy strategy;

  @NotNull(message = "Import blank cells requires a value")
  private Boolean        copyBlank;

  private String         dataSource;

  private String         description;

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

  public ImportStrategy getStrategy()
  {
    return strategy;
  }

  public void setStrategy(ImportStrategy strategy)
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

  public static ImportConfigurationView of(String type, Date startDate, Date endDate, String dataSource, ImportStrategy strategy, Boolean copyBlank)
  {
    ImportConfigurationView view = new ImportConfigurationView();
    view.setType(type);
    view.setStartDate(startDate);
    view.setEndDate(endDate);
    view.setDataSource(dataSource);
    view.setStrategy(strategy);
    view.setCopyBlank(copyBlank);

    return view;
  }
}
