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
