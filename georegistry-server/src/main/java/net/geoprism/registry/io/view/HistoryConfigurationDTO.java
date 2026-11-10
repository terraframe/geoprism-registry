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
package net.geoprism.registry.io.view;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.runwaysdk.dataaccess.ProgrammingErrorException;

import net.geoprism.registry.etl.ObjectImporterFactory.JobHistoryType;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

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
      JsonMapper mapper = JsonMapper.shared();
      return (T) mapper.readValue(json, HistoryConfigurationDTO.class);
    }
    catch (JacksonException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public static String toJson(HistoryConfigurationDTO dto)
  {
    try
    {
      JsonMapper mapper = JsonMapper.shared();
      return mapper.writeValueAsString(dto);
    }
    catch (JacksonException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

}
