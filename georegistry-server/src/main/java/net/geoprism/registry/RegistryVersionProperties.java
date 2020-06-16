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

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistryVersionProperties
{
  private Logger logger = LoggerFactory.getLogger(RegistryVersionProperties.class);
  
  private Properties props;
  
  private static RegistryVersionProperties instance;
  
  public RegistryVersionProperties()
  {
    this.props = new Properties();
    try
    {
      this.props.load(RegistryVersionProperties.class.getResourceAsStream("/georegistry-version.properties"));
    }
    catch (IOException e)
    {
      logger.error("Error occurred while reading georegistry-version.properties", e);
    }
  }
  
  public static synchronized RegistryVersionProperties getInstance()
  {
    if (instance == null)
    {
      instance = new RegistryVersionProperties();
    }
    
    return instance;
  }
  
  public String getVersion()
  {
    return this.props.getProperty("version");
  }
}
