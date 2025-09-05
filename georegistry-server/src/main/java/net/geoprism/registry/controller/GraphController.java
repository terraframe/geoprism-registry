package net.geoprism.registry.controller;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonArray;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.service.request.EdgeImportService;
import net.geoprism.registry.service.request.GraphTypeService;
import net.geoprism.registry.spring.NullableDateDeserializer;

@RestController
@Validated
public class GraphController extends RunwaySpringController
{
  public static final String API_PATH = RegistryConstants.CONTROLLER_ROOT + "graph/";
  
  @Autowired
  private GraphTypeService graphTypeService;
  
  @Autowired EdgeImportService myGraphService;
  
  public static final class GetConfigurationBody
  {
    @NotEmpty private String graphTypeCode;
    @NotEmpty private String graphTypeClass;

    @JsonDeserialize(using = NullableDateDeserializer.class)
    private Date          startDate;

    @JsonDeserialize(using = NullableDateDeserializer.class)
    private Date          endDate;

    @NotNull(message = "file requires a value")
    private MultipartFile file;

    @NotEmpty(message = "Import Strategy requires a value")
    private String        strategy;

    private String        dataSource;

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

    public String getStrategy()
    {
      return strategy;
    }

    public void setStrategy(String strategy)
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

  }
  
  @GetMapping(API_PATH + "get")
  public ResponseEntity<String> get(@RequestParam(required = false) String[] codes)
  {
    final JsonArray graphTypes = new JsonArray();
    
    graphTypeService.getGraphTypes(this.getSessionId(), codes).stream()
      .sorted((a,b) -> a.getLabel().getValue().compareTo(b.getLabel().getValue()))
      .forEach(t -> graphTypes.add(t.toJSON()));
    
    return new ResponseEntity<String>(graphTypes.toString(), HttpStatus.OK);
  }
  
  @PostMapping(API_PATH + "get-json-import-config")
  public ResponseEntity<String> getJsonImportConfig(@Valid @ModelAttribute GetConfigurationBody body) throws IOException
  {
    String sessionId = this.getSessionId();

    try (InputStream stream = body.getFile().getInputStream())
    {
      String fileName = body.getFile().getOriginalFilename();

      SimpleDateFormat format = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);
      format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

      ImportStrategy strategy = ImportStrategy.valueOf(body.getStrategy());

      ObjectNode configuration = myGraphService.getJsonImportConfiguration(sessionId, body.getGraphTypeClass(), body.getGraphTypeCode(), body.getStartDate(), body.getEndDate(), body.dataSource, fileName, stream, strategy);

      return new ResponseEntity<String>(configuration.toString(), HttpStatus.OK);
    }
  }
}
