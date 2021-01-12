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

import net.geoprism.dhis2.dhis2adapter.HTTPConnector;
import net.geoprism.registry.etl.export.dhis2.DHIS2TransportService;
import net.geoprism.registry.etl.export.dhis2.DHIS2TransportServiceIF;
import net.geoprism.registry.graph.DHIS2ExternalSystem;

public class DHIS2ServiceFactory
{
  
  
  private static DHIS2ServiceFactory instance;
  
  private DHIS2TransportServiceIF dhis2;
  
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
  
  public synchronized DHIS2TransportServiceIF instanceGetDhis2Service(DHIS2ExternalSystem system)
  {
    if (this.dhis2 == null)
    {
      HTTPConnector connector = new HTTPConnector();
      connector.setServerUrl(system.getUrl());
      connector.setCredentials(system.getUsername(), system.getPassword());
      
      this.dhis2 = new DHIS2TransportService(connector, getAPIVersion(system));
    }
    
    return this.dhis2;
  }
  
  public Integer getAPIVersion(DHIS2ExternalSystem system)
  {
//    String in = system.getVersion();
//
//    if (in.startsWith("2.31"))
//    {
//      return "26";
//    }
//
//    return "26"; // We currently only support API version 26 right now anyway
    
    return null;
  }
  
  public static DHIS2TransportServiceIF getDhis2TransportService(DHIS2ExternalSystem system)
  {
    return getInstance().instanceGetDhis2Service(system);
  }
  
  public static void setDhis2TransportService(DHIS2TransportServiceIF dhis2)
  {
    getInstance().dhis2 = dhis2;
  }
}
