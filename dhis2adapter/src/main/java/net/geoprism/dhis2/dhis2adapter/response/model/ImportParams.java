package net.geoprism.dhis2.dhis2adapter.response.model;

public class ImportParams
{
  private String userOverrideMode;
  
  private String importMode;
  
  private String identifier;
  
  private String preheatMode;
  
  private String importStrategy;
  
  private String atomicMode;
  
  private String mergeMode;
  
  private String flushMode;
  
  private Boolean skipSharing;
  
  private Boolean skipTranslation;
  
  private Boolean skipValidation;
  
  private Boolean metadataSyncImport;
  
  private String username;

  public String getUserOverrideMode()
  {
    return userOverrideMode;
  }

  public void setUserOverrideMode(String userOverrideMode)
  {
    this.userOverrideMode = userOverrideMode;
  }

  public String getImportMode()
  {
    return importMode;
  }

  public void setImportMode(String importMode)
  {
    this.importMode = importMode;
  }

  public String getIdentifier()
  {
    return identifier;
  }

  public void setIdentifier(String identifier)
  {
    this.identifier = identifier;
  }

  public String getPreheatMode()
  {
    return preheatMode;
  }

  public void setPreheatMode(String preheatMode)
  {
    this.preheatMode = preheatMode;
  }

  public String getImportStrategy()
  {
    return importStrategy;
  }

  public void setImportStrategy(String importStrategy)
  {
    this.importStrategy = importStrategy;
  }

  public String getAtomicMode()
  {
    return atomicMode;
  }

  public void setAtomicMode(String atomicMode)
  {
    this.atomicMode = atomicMode;
  }

  public String getMergeMode()
  {
    return mergeMode;
  }

  public void setMergeMode(String mergeMode)
  {
    this.mergeMode = mergeMode;
  }

  public String getFlushMode()
  {
    return flushMode;
  }

  public void setFlushMode(String flushMode)
  {
    this.flushMode = flushMode;
  }

  public Boolean getSkipSharing()
  {
    return skipSharing;
  }

  public void setSkipSharing(Boolean skipSharing)
  {
    this.skipSharing = skipSharing;
  }

  public Boolean getSkipTranslation()
  {
    return skipTranslation;
  }

  public void setSkipTranslation(Boolean skipTranslation)
  {
    this.skipTranslation = skipTranslation;
  }

  public Boolean getSkipValidation()
  {
    return skipValidation;
  }

  public void setSkipValidation(Boolean skipValidation)
  {
    this.skipValidation = skipValidation;
  }

  public Boolean getMetadataSyncImport()
  {
    return metadataSyncImport;
  }

  public void setMetadataSyncImport(Boolean metadataSyncImport)
  {
    this.metadataSyncImport = metadataSyncImport;
  }

  public String getUsername()
  {
    return username;
  }

  public void setUsername(String username)
  {
    this.username = username;
  }
  
  
}
