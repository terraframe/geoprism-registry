package net.geoprism.registry.dhis2;

import java.util.HashMap;
import java.util.Map;

public class DHIS2VersionManager
{
  public enum SupportedFeature
  {
    OAUTH, SYNCHRONIZATION, PLUGIN
  }
  
  public class DHIS2Version
  {
    private SupportedFeature[] supportedFeatures;
    
    private Integer apiVersion;
    
    private Boolean supported;
    
    private DHIS2Version(Integer apiVersion, Boolean supported, SupportedFeature[] supportedFeatures)
    {
      this.supported = supported;
      this.supportedFeatures = supportedFeatures;
      this.apiVersion = apiVersion;
    }
    
    public SupportedFeature[] getSupportedFeatures()
    {
      return supportedFeatures;
    }
    
    public void setSupportedFeatures(SupportedFeature[] supportedFeatures)
    {
      this.supportedFeatures = supportedFeatures;
    }
  }
  
  public Map<Integer, DHIS2Version> getSupportedVersions()
  {
    Map<Integer, DHIS2Version> map = new HashMap<Integer, DHIS2Version>();
    
    map.put(31, new DHIS2Version(31, true, new SupportedFeature[] {SupportedFeature.OAUTH, SupportedFeature.SYNCHRONIZATION, SupportedFeature.PLUGIN}));
    
    return map;
  }
}
