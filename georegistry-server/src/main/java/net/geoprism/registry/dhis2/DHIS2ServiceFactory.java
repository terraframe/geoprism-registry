/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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

import com.runwaysdk.dataaccess.ProgrammingErrorException;

import net.geoprism.dhis2.dhis2adapter.HTTPConnector;
import net.geoprism.dhis2.dhis2adapter.exception.BadServerUriException;
import net.geoprism.dhis2.dhis2adapter.exception.HTTPException;
import net.geoprism.dhis2.dhis2adapter.exception.IncompatibleServerVersionException;
import net.geoprism.dhis2.dhis2adapter.exception.InvalidLoginException;
import net.geoprism.dhis2.dhis2adapter.exception.UnexpectedResponseException;
import net.geoprism.registry.etl.export.dhis2.DHIS2TransportService;
import net.geoprism.registry.etl.export.dhis2.DHIS2TransportServiceIF;
import net.geoprism.registry.graph.DHIS2ExternalSystem;

public class DHIS2ServiceFactory
{
  private static DHIS2ServiceFactory instance;
  
  private DHIS2TransportServiceIF dhis2 = null;
  
  private void initialize()
  {
  }
  
  public static synchronized DHIS2ServiceFactory getInstance()
  {
    if (instance == null)
    {
      instance = new DHIS2ServiceFactory();
      instance.initialize();
    }
    
    return instance;
  }
  
  public synchronized DHIS2TransportServiceIF instanceGetDhis2Service(DHIS2ExternalSystem system) throws UnexpectedResponseException, InvalidLoginException, HTTPException, BadServerUriException
  {
    if (this.dhis2 == null)
    {
      HTTPConnector connector = new HTTPConnector();
      connector.setServerUrl(system.getUrl());
      connector.setCredentials(system.getUsername(), system.getPassword());
      
      DHIS2TransportService dhis2 = new DHIS2TransportService(connector);
      
      try
      {
        dhis2.initialize();
        
        if (dhis2.getVersionRemoteServerApi() > DHIS2FeatureService.LAST_TESTED_DHIS2_API_VERSION)
        {
          Integer compatLayerVersion = dhis2.getVersionRemoteServerApi() - 2;
          
          if (compatLayerVersion < DHIS2FeatureService.LAST_TESTED_DHIS2_API_VERSION)
          {
            compatLayerVersion = DHIS2FeatureService.LAST_TESTED_DHIS2_API_VERSION;
          }
          
          dhis2.setVersionApiCompat(compatLayerVersion);
        }
      }
      catch (IncompatibleServerVersionException e)
      {
        throw new ProgrammingErrorException(e);
      }
      
      return dhis2;
    }
    
    return this.dhis2;
  }
  
  public static DHIS2TransportServiceIF buildDhis2TransportService(DHIS2ExternalSystem system) throws UnexpectedResponseException, InvalidLoginException, HTTPException, BadServerUriException
  {
    return getInstance().instanceGetDhis2Service(system);
  }
  
  /**
   * This should only be used for testing, since the CGR may have multiple DHIS2 external systems (which may require different transport services).
   * 
   * @param dhis2
   */
  public static void setDhis2TransportService(DHIS2TransportServiceIF dhis2)
  {
    getInstance().dhis2 = dhis2;
  }
}
