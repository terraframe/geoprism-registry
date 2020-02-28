package net.geoprism.registry.etl;

import java.util.LinkedList;
import java.util.Map;

import net.geoprism.data.importer.ShapefileFunction;
import net.geoprism.registry.io.GeoObjectImportConfiguration;

import org.json.JSONObject;

abstract public class ImportConfiguration
{
  public static final String FORMAT_TYPE = "formatType";
  
  public static final String OBJECT_TYPE = "objectType";
  
  public static final String HISTORY_ID  = "historyId";
  
  public static final String VAULT_FILE_ID  = "vaultFileId";
  
  protected String formatType;
  
  protected String objectType;
  
  protected String historyId;
  
  protected String vaultFileId;
  
  protected LinkedList<RecordedErrorException> errors = new LinkedList<RecordedErrorException>();
  
  protected Map<String, ShapefileFunction> functions;
  
  public ImportConfiguration()
  {
    
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
    else
    {
      throw new UnsupportedOperationException();
    }
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

  public void fromJSON(String json)
  {
    JSONObject jo = new JSONObject(json);
    
    this.objectType = jo.getString(OBJECT_TYPE);
    this.formatType = jo.getString(FORMAT_TYPE);
    
    if (jo.has(HISTORY_ID))
    {
      this.historyId = jo.getString(HISTORY_ID);
    }
    
    this.vaultFileId = jo.getString(VAULT_FILE_ID);
  }
  
  protected void toJSON(JSONObject jo)
  {
    jo.put(OBJECT_TYPE, this.objectType);
    jo.put(FORMAT_TYPE, this.formatType);
    jo.put(HISTORY_ID, this.historyId);
    jo.put(VAULT_FILE_ID, this.vaultFileId);
  }
  
  abstract public JSONObject toJSON();
  
  public boolean hasExceptions()
  {
    return this.errors.size() > 0;
  }
  
  /**
   * Be careful when using this method because if an import was resumed half-way through then
   * this won't include errors which were created last time the import ran. You probably want
   * to query the database instead.
   * 
   * @return
   */
  public LinkedList<RecordedErrorException> getExceptions()
  {
    return this.errors;
  }
  
  public void addException(RecordedErrorException e)
  {
    this.errors.add(e);
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
  }
  
}
