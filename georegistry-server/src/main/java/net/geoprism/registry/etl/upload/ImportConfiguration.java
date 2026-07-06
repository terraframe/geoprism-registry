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

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import net.geoprism.data.importer.BasicColumnFunction;
import net.geoprism.data.importer.ShapefileFunction;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.etl.FormatSpecificImporterFactory.FormatImporterType;
import net.geoprism.registry.etl.ObjectImporterFactory;
import net.geoprism.registry.graph.DataSource;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.jobs.ImportHistory;
import net.geoprism.registry.service.business.DataSourceBusinessServiceIF;
import net.geoprism.registry.service.business.ServiceFactory;
import net.geoprism.registry.view.TypeInfo;

public abstract class ImportConfiguration
{
  public static enum ImportStrategy {
    NEW_AND_UPDATE, NEW_ONLY, UPDATE_ONLY
    // DELETE
  }

  public static final String               FORMAT_TYPE                  = "formatType";

  public static final String               OBJECT_TYPE                  = "objectType";

  public static final String               HISTORY_ID                   = "historyId";

  public static final String               JOB_ID                       = "jobId";

  public static final String               VAULT_FILE_ID                = "vaultFileId";

  public static final String               FILE_NAME                    = "fileName";

  public static final String               IMPORT_STRATEGY              = "importStrategy";

  public static final String               EXTERNAL_SYSTEM_ID           = "externalSystemId";

  private static final String              IS_EXTERNAL                  = "isExternal";

  public static final String               EXTERNAL_ID_ATTRIBUTE_TARGET = "externalIdAttributeTarget";

  public static final String               COPY_BLANK                   = "copyBlank";

  public static final String               IGNORE_PROJECTION            = "ignoreProjection";

  public static final String               DESCRIPTION                  = "description";

  public static final String               START_DATE                   = "startDate";

  public static final String               END_DATE                     = "endDate";

  public static final String               DATA_SOURCE                  = "dataSource";

  protected String                         formatType;

  protected String                         objectType;

  protected String                         historyId;

  protected String                         jobId;

  protected String                         vaultFileId;

  protected String                         fileName;

  protected Boolean                        isExternal                   = false;

  protected Boolean                        copyBlank                    = true;

  protected Boolean                        ignoreProjection             = false;

  protected String                         externalSystemId             = null;

  protected String                         description                  = null;

  protected ExternalSystem                 externalSystem               = null;

  protected ShapefileFunction              externalIdFunction           = null;

  protected Map<String, ShapefileFunction> functions;

  protected ImportStrategy                 importStrategy;

  private DataSource                       dataSource;

  private Date                             startDate;

  private Date                             endDate;

  private DataSourceBusinessServiceIF      sourceService;

  public ImportConfiguration()
  {
    this.sourceService = ServiceFactory.getBean(DataSourceBusinessServiceIF.class);
  }

  public abstract void enforceCreatePermissions();

  public abstract void enforceExecutePermissions();

  public abstract void populate(ImportHistory history);

  public abstract List<TypeInfo> getTypes();

  public static ImportConfiguration build(String json)
  {
    JSONObject jo = new JSONObject(json);

    boolean includeCoordinates = false;

    if (jo.has(FORMAT_TYPE) && jo.get(FORMAT_TYPE).equals(FormatImporterType.EXCEL.name()))
    {
      includeCoordinates = true;
    }

    return ImportConfiguration.build(json, includeCoordinates);
  }

  public static ImportConfiguration build(String json, boolean includeCoordinates)
  {
    JSONObject jo = new JSONObject(json);

    String objectType = jo.getString(OBJECT_TYPE);

    if (objectType.equals(ObjectImporterFactory.ObjectImportType.GEO_OBJECT.name()))
    {
      GeoObjectImportConfiguration config = new GeoObjectImportConfiguration();
      config.fromJSON(json, includeCoordinates);
      return config;
    }
    else if (objectType.equals(ObjectImporterFactory.ObjectImportType.BUSINESS_OBJECT.name()))
    {
      BusinessObjectImportConfiguration config = new BusinessObjectImportConfiguration();
      config.fromJSON(json, false);
      return config;
    }
    else if (objectType.equals(ObjectImporterFactory.ObjectImportType.EDGE_OBJECT.name()))
    {
      EdgeObjectImportConfiguration config = new EdgeObjectImportConfiguration();
      config.fromJSON(json, false);
      return config;
    }
    else
    {
      throw new UnsupportedOperationException();
    }
  }

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

  public void fromJSON(String json)
  {
    JSONObject config = new JSONObject(json);

    this.objectType = config.getString(OBJECT_TYPE);
    this.formatType = config.getString(FORMAT_TYPE);

    if (config.has(DESCRIPTION))
    {
      this.description = config.getString(DESCRIPTION);
    }

    if (config.has(HISTORY_ID))
    {
      this.historyId = config.getString(HISTORY_ID);
    }

    if (config.has(JOB_ID))
    {
      this.jobId = config.getString(JOB_ID);
    }

    this.vaultFileId = config.getString(VAULT_FILE_ID);

    if (config.has(IMPORT_STRATEGY))
    {
      this.importStrategy = ImportStrategy.valueOf(config.getString(IMPORT_STRATEGY));
    }

    this.fileName = config.getString(FILE_NAME);

    if (config.has(EXTERNAL_SYSTEM_ID))
    {
      this.externalSystemId = config.getString(EXTERNAL_SYSTEM_ID);
    }

    if (config.has(IS_EXTERNAL))
    {
      this.isExternal = config.getBoolean(IS_EXTERNAL);
    }

    if (config.has(COPY_BLANK))
    {
      this.copyBlank = config.getBoolean(COPY_BLANK);
    }

    if (config.has(IGNORE_PROJECTION))
    {
      this.ignoreProjection = config.getBoolean(IGNORE_PROJECTION);
    }

    if (config.has(EXTERNAL_ID_ATTRIBUTE_TARGET))
    {
      this.externalIdFunction = new BasicColumnFunction(config.getString(EXTERNAL_ID_ATTRIBUTE_TARGET));
    }

    if (config.has(GeoObjectImportConfiguration.START_DATE))
    {
      this.setStartDate(GeoRegistryUtil.parseDate(config.getString(GeoObjectImportConfiguration.START_DATE)));
    }

    if (config.has(GeoObjectImportConfiguration.END_DATE))
    {
      this.setEndDate(GeoRegistryUtil.parseDate(config.getString(GeoObjectImportConfiguration.END_DATE)));
    }

    if (config.has(DATA_SOURCE))
    {
      this.sourceService.getByCode(config.getString(DATA_SOURCE)).ifPresent(source -> {
        this.setDataSource(source);
      });
    }
  }

  protected void toJSON(JSONObject config)
  {
    config.put(OBJECT_TYPE, this.objectType);
    config.put(FORMAT_TYPE, this.formatType);
    config.put(HISTORY_ID, this.historyId);
    config.put(JOB_ID, this.jobId);
    config.put(VAULT_FILE_ID, this.vaultFileId);
    config.put(FILE_NAME, this.fileName);
    config.put(EXTERNAL_SYSTEM_ID, this.externalSystemId);
    config.put(IS_EXTERNAL, this.isExternal);
    config.put(COPY_BLANK, this.copyBlank);
    config.put(IGNORE_PROJECTION, this.ignoreProjection);

    if (this.importStrategy != null)
    {
      config.put(IMPORT_STRATEGY, this.importStrategy.name());
    }

    if (this.description != null)
    {
      config.put(DESCRIPTION, this.description);
    }

    if (this.externalIdFunction != null)
    {
      config.put(EXTERNAL_ID_ATTRIBUTE_TARGET, this.externalIdFunction.toJson());
    }

    if (this.getStartDate() != null)
    {
      config.put(EdgeObjectImportConfiguration.START_DATE, GeoRegistryUtil.formatDate(this.getStartDate(), false));
    }
    if (this.getEndDate() != null)
    {
      config.put(EdgeObjectImportConfiguration.END_DATE, GeoRegistryUtil.formatDate(this.getEndDate(), false));
    }

    if (this.getDataSource() != null)
    {
      config.put(GeoObjectImportConfiguration.DATA_SOURCE, dataSource.getCode());
    }

  }

  abstract public JSONObject toJSON();

  public abstract boolean hasExceptions();

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

}
