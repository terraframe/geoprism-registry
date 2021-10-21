/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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

import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import net.geoprism.data.importer.BasicColumnFunction;
import net.geoprism.data.importer.ShapefileFunction;
import net.geoprism.registry.etl.FormatSpecificImporterFactory.FormatImporterType;
import net.geoprism.registry.etl.ImportHistory;
import net.geoprism.registry.etl.ObjectImporterFactory;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.io.Location;

public abstract class ImportConfiguration
{
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

  protected String                         formatType;

  protected String                         objectType;

  protected String                         historyId;

  protected String                         jobId;

  protected String                         vaultFileId;

  protected String                         fileName;

  protected Boolean                        isExternal                   = false;

  protected Boolean                        copyBlank                    = true;

  protected String                         externalSystemId             = null;

  protected ExternalSystem                 externalSystem               = null;

  protected ShapefileFunction              externalIdFunction           = null;

  protected Map<String, ShapefileFunction> functions;

  protected ImportStrategy                 importStrategy;

  public static enum ImportStrategy {
    NEW_AND_UPDATE, NEW_ONLY, UPDATE_ONLY
    // DELETE
  }

  public ImportConfiguration()
  {

  }

  public abstract void enforceCreatePermissions();

  public abstract void enforceExecutePermissions();
  
  public abstract void populate(ImportHistory history);

  public abstract List<Location> getLocations();

  public static ImportConfiguration build(String json)
  {
    JSONObject jo = new JSONObject(json);

    boolean includeCoordinates = false;

    if (jo.get(FORMAT_TYPE).equals(FormatImporterType.EXCEL.name()))
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
    else if (objectType.equals(ObjectImporterFactory.ObjectImportType.PROGRAMMATIC_OBJECT.name()))
    {
      ProgrammaticObjectImportConfiguration config = new ProgrammaticObjectImportConfiguration();
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

  public void fromJSON(String json)
  {
    JSONObject jo = new JSONObject(json);

    this.objectType = jo.getString(OBJECT_TYPE);
    this.formatType = jo.getString(FORMAT_TYPE);

    if (jo.has(HISTORY_ID))
    {
      this.historyId = jo.getString(HISTORY_ID);
    }

    if (jo.has(JOB_ID))
    {
      this.jobId = jo.getString(JOB_ID);
    }

    this.vaultFileId = jo.getString(VAULT_FILE_ID);

    this.importStrategy = ImportStrategy.valueOf(jo.getString(IMPORT_STRATEGY));

    this.fileName = jo.getString(FILE_NAME);

    if (jo.has(EXTERNAL_SYSTEM_ID))
    {
      this.externalSystemId = jo.getString(EXTERNAL_SYSTEM_ID);
    }

    if (jo.has(IS_EXTERNAL))
    {
      this.isExternal = jo.getBoolean(IS_EXTERNAL);
    }

    if (jo.has(COPY_BLANK))
    {
      this.copyBlank = jo.getBoolean(COPY_BLANK);
    }

    if (jo.has(EXTERNAL_ID_ATTRIBUTE_TARGET))
    {
      this.externalIdFunction = new BasicColumnFunction(jo.getString(EXTERNAL_ID_ATTRIBUTE_TARGET));
    }
  }

  protected void toJSON(JSONObject jo)
  {
    jo.put(OBJECT_TYPE, this.objectType);
    jo.put(FORMAT_TYPE, this.formatType);
    jo.put(HISTORY_ID, this.historyId);
    jo.put(JOB_ID, this.jobId);
    jo.put(VAULT_FILE_ID, this.vaultFileId);
    jo.put(IMPORT_STRATEGY, this.importStrategy.name());
    jo.put(FILE_NAME, this.fileName);
    jo.put(EXTERNAL_SYSTEM_ID, this.externalSystemId);
    jo.put(IS_EXTERNAL, this.isExternal);
    jo.put(COPY_BLANK, this.copyBlank);

    if (this.externalIdFunction != null)
    {
      jo.put(EXTERNAL_ID_ATTRIBUTE_TARGET, this.externalIdFunction.toJson());
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
