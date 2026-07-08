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
package net.geoprism.registry.etl.upload;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;

import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.localization.SupportedLocaleIF;

import net.geoprism.data.importer.BasicColumnFunction;
import net.geoprism.data.importer.ShapefileFunction;
import net.geoprism.registry.etl.FormatSpecificImporterFactory.FormatImporterType;
import net.geoprism.registry.etl.ObjectImporterFactory;
import net.geoprism.registry.etl.ObjectImporterFactory.JobHistoryType;
import net.geoprism.registry.graph.AttributeDataSourceType;
import net.geoprism.registry.graph.AttributeGeometryType;
import net.geoprism.registry.graph.DataSource;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.io.ConstantShapefileFunction;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.io.LocalizedValueFunction;
import net.geoprism.registry.jobs.ImportHistory;
import net.geoprism.registry.model.graph.ObjectClassIF;
import net.geoprism.registry.model.localization.DefaultLocaleView;
import net.geoprism.registry.service.business.DataSourceBusinessServiceIF;
import net.geoprism.registry.service.business.ServiceFactory;
import net.geoprism.registry.view.BasicColumnFunctionDTO;
import net.geoprism.registry.view.BusinessObjectImportConfigurationDTO;
import net.geoprism.registry.view.ColumnFunctionDTO;
import net.geoprism.registry.view.ConceptObjectImportConfigurationDTO;
import net.geoprism.registry.view.ConstantFunctionDTO;
import net.geoprism.registry.view.EdgeObjectImportConfigurationDTO;
import net.geoprism.registry.view.GeoObjectImportConfigurationDTO;
import net.geoprism.registry.view.ImportColumnDTO;
import net.geoprism.registry.view.ImportConfigurationDTO;
import net.geoprism.registry.view.ImportTypeDTO;
import net.geoprism.registry.view.LocalizedValueFunctionDTO;
import net.geoprism.registry.view.TypeInfo;

public abstract class ImportConfiguration
{
  public static enum ImportStrategy {
    NEW_AND_UPDATE, NEW_ONLY, UPDATE_ONLY
    // DELETE
  }

  public static final String               TEXT               = "text";

  public static final String               NUMERIC            = "numeric";

  protected String                         formatType;

  protected String                         objectType;

  protected String                         historyId;

  protected String                         jobId;

  protected String                         vaultFileId;

  protected String                         fileName;

  protected Boolean                        isExternal         = false;

  protected Boolean                        copyBlank          = true;

  protected Boolean                        ignoreProjection   = false;

  protected String                         externalSystemId   = null;

  protected String                         description        = null;

  protected ExternalSystem                 externalSystem     = null;

  protected ShapefileFunction              externalIdFunction = null;

  protected Map<String, ShapefileFunction> functions;

  protected ImportStrategy                 importStrategy;

  private DataSource                       dataSource;

  private Date                             startDate;

  private Date                             endDate;

  private DataSourceBusinessServiceIF      sourceService;

  public ImportConfiguration()
  {
    this.functions = new HashMap<String, ShapefileFunction>();

    this.sourceService = ServiceFactory.getBean(DataSourceBusinessServiceIF.class);
  }

  public abstract void enforceCreatePermissions();

  public abstract void enforceExecutePermissions();

  public abstract void populate(ImportHistory history);

  public abstract List<TypeInfo> getTypes();

  abstract public ImportConfigurationDTO toDTO();

  public abstract boolean hasExceptions();

  public ImportStrategy getImportStrategy()
  {
    return importStrategy;
  }

  public void setImportStrategy(ImportStrategy importStrategy)
  {
    this.importStrategy = importStrategy;
  }

  public String getVaultFileId()
  {
    return vaultFileId;
  }

  public void setVaultFileId(String vaultFileId)
  {
    this.vaultFileId = vaultFileId;
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

  public String getFormatType()
  {
    return formatType;
  }

  public void setFormatType(String formatType)
  {
    this.formatType = formatType;
  }

  public String getObjectType()
  {
    return objectType;
  }

  public void setObjectType(String objectType)
  {
    this.objectType = objectType;
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

  public Boolean isExternalImport()
  {
    return this.isExternal;
  }

  public ExternalSystem getExternalSystem()
  {
    if (this.externalSystem == null)
    {
      this.externalSystem = ExternalSystem.get(externalSystemId);
    }

    return this.externalSystem;
  }

  public String getExternalSystemId()
  {
    return externalSystemId;
  }

  public void setExternalSystemId(String externalSystemId)
  {
    this.externalSystemId = externalSystemId;
    this.externalSystem = null;
  }

  public ShapefileFunction getExternalIdFunction()
  {
    return this.externalIdFunction;
  }

  public void setExternalIdFunction(ShapefileFunction externalIdFunction)
  {
    this.externalIdFunction = externalIdFunction;
  }

  public Boolean getIsExternal()
  {
    return isExternal;
  }

  public void setIsExternal(Boolean isExternal)
  {
    this.isExternal = isExternal;
  }

  public DataSource getDataSource()
  {
    return dataSource;
  }

  public void setDataSource(DataSource dataSource)
  {
    this.dataSource = dataSource;
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

  public ImportConfiguration fromDTO(ImportConfigurationDTO dto)
  {
    this.setObjectType(dto.getObjectType().name());
    this.setFormatType(dto.getFormatType().name());
    this.setDescription(dto.getDescription());
    this.setHistoryId(dto.getHistoryId());
    this.setJobId(dto.getJobId());
    this.setVaultFileId(dto.getVaultFileId());
    this.setImportStrategy(dto.getImportStrategy());
    this.setFileName(dto.getFileName());
    this.setExternalSystemId(dto.getExternalSystemId());
    this.setIsExternal(dto.getIsExternal());
    this.setCopyBlank(dto.getCopyBlank());
    this.setIgnoreProjection(dto.getIgnoreProjection());
    this.setExternalIdFunction(this.fromDTO(dto.getExternalIdAttributeTarget()));
    this.setStartDate(dto.getStartDate());
    this.setEndDate(dto.getEndDate());

    if (!StringUtils.isBlank(dto.getDataSource()))
    {
      this.sourceService.getByCode(dto.getDataSource()).ifPresent(source -> {
        this.setDataSource(source);
      });
    }

    return this;
  }

  protected ShapefileFunction fromDTO(ColumnFunctionDTO dto)
  {
    if (dto != null)
    {

      if (dto instanceof BasicColumnFunctionDTO)
      {
        return new BasicColumnFunction( ( (BasicColumnFunctionDTO) dto ).getAttributeName());
      }
      else if (dto instanceof ConstantFunctionDTO)
      {
        return new ConstantShapefileFunction( ( (ConstantFunctionDTO) dto ).getValue());
      }
      else if (dto instanceof LocalizedValueFunctionDTO)
      {
        LocalizedValueFunction function = new LocalizedValueFunction();

        ( (LocalizedValueFunctionDTO) dto ).getMap().forEach((k, f) -> function.add(k, this.fromDTO(f)));

        return function;
      }

      throw new UnsupportedOperationException();
    }

    return null;
  }

  protected void toDTO(ImportConfigurationDTO dto)
  {
    dto.setObjectType(JobHistoryType.valueOf(this.objectType));
    dto.setFormatType(FormatImporterType.valueOf(this.formatType));
    dto.setHistoryId(this.historyId);
    dto.setJobId(this.jobId);
    dto.setVaultFileId(this.vaultFileId);
    dto.setFileName(this.fileName);
    dto.setExternalSystemId(this.externalSystemId);
    dto.setIsExternal(this.isExternal);
    dto.setCopyBlank(this.copyBlank);
    dto.setIgnoreProjection(this.ignoreProjection);
    dto.setStartDate(this.getStartDate());
    dto.setEndDate(this.getEndDate());

    if (this.importStrategy != null)
    {
      dto.setImportStrategy(this.importStrategy);
    }

    if (this.description != null)
    {
      dto.setDescription(this.description);
    }

    if (this.externalIdFunction != null)
    {
      dto.setExternalIdAttributeTarget(toDTO(this.externalIdFunction));
    }

    if (this.getDataSource() != null)
    {
      dto.setDataSource(dataSource.getCode());
    }
  }

  public String getFileName()
  {
    return fileName;
  }

  public void setFileName(String fileName)
  {
    this.fileName = fileName;
  }

  public ShapefileFunction getFunction(String attributeName)
  {
    return this.functions.get(attributeName);
  }

  public Map<String, ShapefileFunction> getFunctions()
  {
    return functions;
  }

  public void setFunction(String attributeName, ShapefileFunction function)
  {
    this.functions.put(attributeName, function);
  }

  public void validate()
  {
    if (this.historyId == null || this.historyId.length() == 0)
    {
      throw new RuntimeException("History Id is required");
    }

    if (this.vaultFileId == null || this.vaultFileId.length() == 0)
    {
      throw new RuntimeException("Vault File Id is required");
    }

    if (this.importStrategy == null)
    {
      throw new RuntimeException("Import strategy is required");
    }
  }

  // public static ImportConfiguration build(String json)
  // {
  // JSONObject jo = new JSONObject(json);
  //
  // boolean includeCoordinates = false;
  //
  // if (jo.has(FORMAT_TYPE) &&
  // jo.get(FORMAT_TYPE).equals(FormatImporterType.EXCEL.name()))
  // {
  // includeCoordinates = true;
  // }
  //
  // return ImportConfiguration.build(json, includeCoordinates);
  // }
  //
  // public static ImportConfiguration build(String json, boolean
  // includeCoordinates)
  // {
  // JSONObject jo = new JSONObject(json);
  //
  // String objectType = jo.getString(OBJECT_TYPE);
  //
  // if
  // (objectType.equals(ObjectImporterFactory.JobHistoryType.GEO_OBJECT.name()))
  // {
  // GeoObjectImportConfiguration config = new GeoObjectImportConfiguration();
  // config.fromJSON(json, includeCoordinates);
  // return config;
  // }
  // else if
  // (objectType.equals(ObjectImporterFactory.JobHistoryType.BUSINESS_OBJECT.name()))
  // {
  // BusinessObjectImportConfiguration config = new
  // BusinessObjectImportConfiguration();
  // config.fromJSON(json, false);
  // return config;
  // }
  // else if
  // (objectType.equals(ObjectImporterFactory.JobHistoryType.EDGE_OBJECT.name()))
  // {
  // EdgeObjectImportConfiguration config = new EdgeObjectImportConfiguration();
  // config.fromJSON(json, false);
  // return config;
  // }
  // else
  // {
  // throw new UnsupportedOperationException();
  // }
  // }

  public static String getBaseType(net.geoprism.registry.graph.AttributeType type)
  {
    if (type instanceof net.geoprism.registry.graph.AttributeBooleanType)
    {
      return AttributeBooleanType.TYPE;
    }
    else if (type instanceof net.geoprism.registry.graph.AttributeClassificationType || type instanceof net.geoprism.registry.graph.AttributeCharacterType || type instanceof net.geoprism.registry.graph.AttributeLocalType)
    {
      return BusinessObjectImportConfiguration.TEXT;
    }
    else if (type instanceof net.geoprism.registry.graph.AttributeDoubleType || type instanceof net.geoprism.registry.graph.AttributeLongType)
    {
      return BusinessObjectImportConfiguration.NUMERIC;
    }

    return AttributeDateType.TYPE;
  }

  public static String getBaseType(org.opengis.feature.type.AttributeType type)
  {
    Class<?> clazz = type.getBinding();

    if (Boolean.class.isAssignableFrom(clazz))
    {
      return AttributeBooleanType.TYPE;
    }
    else if (String.class.isAssignableFrom(clazz))
    {
      return ImportConfiguration.TEXT;
    }
    else if (Number.class.isAssignableFrom(clazz))
    {
      return ImportConfiguration.NUMERIC;
    }
    else if (Date.class.isAssignableFrom(clazz))
    {
      return AttributeDateType.TYPE;
    }

    throw new UnsupportedOperationException("Unsupported type [" + type.getBinding().getName() + "]");
  }

  public static ImportConfiguration build(ImportConfigurationDTO dto)
  {
    return ImportConfiguration.build(dto, dto.getFormatType().equals(FormatImporterType.EXCEL));
  }

  public static ImportConfiguration build(ImportConfigurationDTO dto, boolean includeCoordinates)
  {

    if (dto.getObjectType().equals(ObjectImporterFactory.JobHistoryType.BUSINESS_OBJECT))
    {
      BusinessObjectImportConfiguration config = new BusinessObjectImportConfiguration();
      config.fromDTO((BusinessObjectImportConfigurationDTO) dto, false);
      return config;
    }
    else if (dto.getObjectType().equals(ObjectImporterFactory.JobHistoryType.CONCEPT_OBJECT))
    {
      ConceptObjectImportConfiguration config = new ConceptObjectImportConfiguration();
      config.fromDTO((ConceptObjectImportConfigurationDTO) dto, false);
      return config;
    }
    else if (dto.getObjectType().equals(ObjectImporterFactory.JobHistoryType.GEO_OBJECT))
    {
      GeoObjectImportConfiguration config = new GeoObjectImportConfiguration();
      config.fromDTO((GeoObjectImportConfigurationDTO) dto, includeCoordinates);
      return config;
    }
    else if (dto.getObjectType().equals(ObjectImporterFactory.JobHistoryType.EDGE_OBJECT))
    {
      EdgeObjectImportConfiguration config = new EdgeObjectImportConfiguration();
      config.fromDTO((EdgeObjectImportConfigurationDTO) dto, false);
      return config;
    }
    throw new UnsupportedOperationException();
  }

  public static ColumnFunctionDTO toDTO(ShapefileFunction function)
  {
    if (function instanceof BasicColumnFunction)
    {
      return new BasicColumnFunctionDTO( ( (BasicColumnFunction) function ).getAttributeName());
    }
    else if (function instanceof ConstantShapefileFunction)
    {
      return new ConstantFunctionDTO( ( (ConstantShapefileFunction) function ).getConstant());
    }
    else if (function instanceof LocalizedValueFunction)
    {
      LocalizedValueFunctionDTO dto = new LocalizedValueFunctionDTO();

      LocalizedValueFunction func = (LocalizedValueFunction) function;

      func.getEntries().forEach(entry -> dto.put(entry.getKey(), toDTO(entry.getValue())));

      return dto;
    }

    throw new UnsupportedOperationException();
  }

  public static ImportTypeDTO toTypeDTO(ObjectClassIF type, Map<String, ShapefileFunction> functions)
  {
    TypeInfo typeInfo = type.getTypeInfo();

    ImportTypeDTO dto = new ImportTypeDTO();
    dto.setCode(typeInfo.getTypeCode());
    dto.setType(typeInfo.getTypeClass().getCode());
    dto.setLabel(type.getLabel());

    type.getAttributes().stream() //
        .filter(a -> ! ( a instanceof AttributeGeometryType )) //
        .filter(a -> ! ( a instanceof AttributeDataSourceType )) //
        .filter(a -> !a.getCode().equals(DefaultAttribute.UID.getName())) //
        .filter(a -> !a.getCode().equals(DefaultAttribute.INVALID.getName())) //
        .filter(a -> !a.getCode().equals(DefaultAttribute.EXISTS.getName())) //
        .forEach(attribute -> {

          if (attribute instanceof net.geoprism.registry.graph.AttributeLocalType)
          {
            LocalizedValueFunction function = (LocalizedValueFunction) functions.getOrDefault(attribute.getCode(), new LocalizedValueFunction());

            ImportColumnDTO base = new ImportColumnDTO();
            base.setCode(attribute.getCode());
            base.setBaseType(getBaseType(attribute));
            base.setLocale(LocalizedValue.DEFAULT_LOCALE);
            base.setLabel(attribute.getLocalizedLabel().appendLocalizedValue(" (" + LocalizationFacade.localize(DefaultLocaleView.LABEL) + ")"));
            base.setRequired(attribute.getRequired());

            if (function.has(LocalizedValue.DEFAULT_LOCALE))
            {
              base.setTarget(function.getFunction(LocalizedValue.DEFAULT_LOCALE).toJson());
            }

            dto.addAttribute(base);

            for (SupportedLocaleIF locale : LocalizationFacade.getSupportedLocales())
            {
              String localeCode = locale.getLocale().toString();

              ImportColumnDTO aDto = new ImportColumnDTO();
              aDto.setCode(attribute.getCode());
              aDto.setBaseType(getBaseType(attribute));
              aDto.setLocale(localeCode);
              aDto.setLabel(attribute.getLocalizedLabel().appendLocalizedValue(" (" + locale.getDisplayLabel().getValue() + ")"));

              if (function.has(localeCode))
              {
                aDto.setTarget(function.getFunction(localeCode).toJson());
              }

              dto.addAttribute(aDto);
            }
          }
          else
          {
            String attributeName = attribute.getCode();

            ImportColumnDTO column = new ImportColumnDTO();
            column.setCode(attributeName);
            column.setBaseType(getBaseType(attribute));
            column.setLabel(attribute.getLocalizedLabel());
            column.setRequired(attribute.getRequired());

            if (functions.containsKey(attributeName))
            {
              ShapefileFunction function = functions.get(attributeName);

              if (function instanceof BasicColumnFunction)
              {
                column.setTarget( ( (BasicColumnFunction) function ).getAttributeName());
              }
              else
              {
                column.setFunction(toDTO(function));
              }
            }

            dto.addAttribute(column);
          }
        });

    // Add attributes for all functions that are not part of the type attributes
    functions.entrySet().stream() //
        .filter(e -> dto.getAttributes().stream().filter(t -> t.getCode().equals(e.getKey())).findAny().isEmpty()) //
        .forEach(e -> {
          String code = e.getKey();
          String baseType = GeoObjectImportConfiguration.TEXT;
          LocalizedValue label = new LocalizedValue(code);

          // Hard-coded entries for coordinate columns
          if (code.equals(GeoObjectImportConfiguration.LATITUDE_KEY))
          {
            baseType = GeoObjectImportConfiguration.NUMERIC;
            label = new LocalizedValue(LocalizationFacade.localize(GeoObjectImportConfiguration.LATITUDE_KEY));
          }
          else if (code.equals(GeoObjectImportConfiguration.LONGITUDE_KEY))
          {
            baseType = GeoObjectImportConfiguration.NUMERIC;
            label = new LocalizedValue(LocalizationFacade.localize(GeoObjectImportConfiguration.LONGITUDE_KEY));
          }

          ImportColumnDTO column = new ImportColumnDTO();
          column.setCode(code);
          column.setBaseType(baseType);
          column.setLabel(label);

          ShapefileFunction function = e.getValue();

          if (function instanceof BasicColumnFunction)
          {
            column.setTarget( ( (BasicColumnFunction) function ).getAttributeName());
          }
          else
          {
            column.setFunction(toDTO(function));
          }

          dto.getAttributes().add(column);

        });
    
    Collections.sort(dto.getAttributes(), (a, b) -> a.getCode().compareTo(b.getCode()));

    return dto;
  }

}
