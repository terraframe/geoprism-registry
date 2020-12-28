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
package net.geoprism.registry;

import com.runwaysdk.configuration.ConfigurationManager;
import com.runwaysdk.configuration.ConfigurationReaderIF;


public class GeoregistryProperties
{
  private ConfigurationReaderIF props;

  private static class Singleton 
  {
    private static GeoregistryProperties INSTANCE = new GeoregistryProperties();
  }

  public GeoregistryProperties()
  {
    this.props = ConfigurationManager.getReader(GeoregistryConfigGroup.COMMON, "georegistry.properties");
  }

  public static int getRefreshSessionRecordCount()
  {
    return Singleton.INSTANCE.props.getInteger("import.refreshSessionRecordCount", 10000);
  }
  
  public static String getRemoteServerUrl()
  {
    String url = Singleton.INSTANCE.props.getString("cgr.remote.url", "https://localhost:8443/georegistry/");
    
    if (!url.endsWith("/"))
    {
      url = url + "/";
    }
    
    return url;
  }
}
