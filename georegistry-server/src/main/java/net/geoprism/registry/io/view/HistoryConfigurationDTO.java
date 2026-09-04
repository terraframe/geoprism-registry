package net.geoprism.registry.io.view;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runwaysdk.dataaccess.ProgrammingErrorException;

import net.geoprism.registry.etl.ObjectImporterFactory.JobHistoryType;

@JsonTypeInfo( //
    use = JsonTypeInfo.Id.NAME, // use logical type name
    include = JsonTypeInfo.As.PROPERTY, //
    property = "objectType", //
    visible = true)

@JsonSubTypes({ //
    @JsonSubTypes.Type(value = BusinessObjectImportConfigurationDTO.class, name = "BUSINESS_OBJECT"), //
    @JsonSubTypes.Type(value = ConceptObjectImportConfigurationDTO.class, name = "CONCEPT_OBJECT"), //
    @JsonSubTypes.Type(value = GeoObjectImportConfigurationDTO.class, name = "GEO_OBJECT"), //
    @JsonSubTypes.Type(value = EdgeObjectImportConfigurationDTO.class, name = "EDGE_OBJECT"), //
    @JsonSubTypes.Type(value = ExportConfigurationDTO.class, names = { "LPG", "RDF_LPG", "RDF_REPO" }) //
})
public abstract class HistoryConfigurationDTO
{

  private JobHistoryType objectType;

  private String         fileName;

  public JobHistoryType getObjectType()
  {
    return objectType;
  }

  public void setObjectType(JobHistoryType objectType)
  {
    this.objectType = objectType;
  }

  public String getFileName()
  {
    return fileName;
  }

  public void setFileName(String fileName)
  {
    this.fileName = fileName;
  }

  @SuppressWarnings("unchecked")
  public static <T extends HistoryConfigurationDTO> T parseJson(String json)
  {
    try
    {
      ObjectMapper mapper = new ObjectMapper();
      return (T) mapper.readValue(json, HistoryConfigurationDTO.class);
    }
    catch (JsonProcessingException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public static String toJson(HistoryConfigurationDTO dto)
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
