package net.geoprism.registry.view;

import java.util.Date;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.spring.NullableDateDeserializer;

public class EdgeImportConfigurationView
{
  @NotEmpty
  private String         graphTypeCode;

  @NotEmpty
  private String         graphTypeClass;

  @JsonDeserialize(using = NullableDateDeserializer.class)
  private Date           startDate;

  @JsonDeserialize(using = NullableDateDeserializer.class)
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
