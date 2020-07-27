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
package net.geoprism.registry.etl.export;

import net.geoprism.dhis2.dhis2adapter.HTTPConnector;
import net.geoprism.registry.etl.export.dhis2.DHIS2Service;
import net.geoprism.registry.etl.export.dhis2.DHIS2ServiceIF;

public class DataExportServiceFactory
{
  private static DataExportServiceFactory instance;
  
  private DHIS2ServiceIF dhis2;
  
  private void initialize()
  {
  }
  
  public static synchronized DataExportServiceFactory getInstance()
  {
    if (instance == null)
    {
      instance = new DataExportServiceFactory();
      instance.initialize();
    }
    
    return instance;
  }
  
  public synchronized DHIS2ServiceIF instanceGetDhis2Service(HTTPConnector connector, String version)
  {
    if (this.dhis2 == null)
    {
      this.dhis2 = new DHIS2Service(connector, version);
    }
    
    return this.dhis2;
  }
  
  public static DHIS2ServiceIF getDhis2Service(HTTPConnector connector, String version)
  {
    return getInstance().instanceGetDhis2Service(connector, version);
  }
  
  public static void setDhis2Service(DHIS2ServiceIF dhis2)
  {
    getInstance().dhis2 = dhis2;
  }
}
