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
package net.geoprism.dhis2.dhis2adapter.response.model;

import net.geoprism.dhis2.dhis2adapter.configuration.ImportStrategy;
import net.geoprism.dhis2.dhis2adapter.configuration.MergeMode;

public class ImportParams
{
  private String userOverrideMode;
  
  private String importMode;
  
  private String identifier;
  
  private String preheatMode;
  
  private ImportStrategy importStrategy;
  
  private String atomicMode;
  
  private MergeMode mergeMode;
  
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

  public ImportStrategy getImportStrategy()
  {
    return importStrategy;
  }

  public void setImportStrategy(ImportStrategy importStrategy)
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

  public MergeMode getMergeMode()
  {
    return mergeMode;
  }

  public void setMergeMode(MergeMode mergeMode)
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
