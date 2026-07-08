package net.geoprism.registry.view;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import net.geoprism.registry.etl.ObjectImporterFactory.JobHistoryType;

@JsonTypeInfo( //
    use = JsonTypeInfo.Id.NAME, // use logical type name
    include = JsonTypeInfo.As.PROPERTY, property = "objectType")

@JsonSubTypes({ //
    @JsonSubTypes.Type(value = BusinessObjectImportConfigurationDTO.class, name = "BUSINESS_OBJECT"), //
    @JsonSubTypes.Type(value = ConceptObjectImportConfigurationDTO.class, name = "CONCEPT_OBJECT"), //
    @JsonSubTypes.Type(value = GeoObjectImportConfigurationDTO.class, name = "GEO_OBJECT"), //
    @JsonSubTypes.Type(value = EdgeObjectImportConfigurationDTO.class, name = "EDGE_OBJECT"), //
    @JsonSubTypes.Type(value = ExportConfigurationDTO.class, names = { "LPG, RDF_LPG, RDF_REPO" }), //
})
public abstract class HistoryConfigurationDTO
{

  private JobHistoryType objectType;

  public JobHistoryType getObjectType()
  {
    return objectType;
  }

  public void setObjectType(JobHistoryType objectType)
  {
    this.objectType = objectType;
  }
}
