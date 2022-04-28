/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Common Geo Registry Adapter(tm).
 *
 * Common Geo Registry Adapter(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Common Geo Registry Adapter(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Common Geo Registry Adapter(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package org.commongeoregistry.adapter.constants;

import java.io.IOException;
import java.util.Properties;

public class CGRAdapterProperties
{
  private static CGRAdapterProperties instance;
  
  private Properties props;
  
  public CGRAdapterProperties()
  {
    props = new Properties();
    
    try
    {
      props.load(CGRAdapterProperties.class.getClassLoader().getResourceAsStream("cgradapter.properties"));
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }
  
  public static synchronized CGRAdapterProperties getInstance()
  {
    if (instance == null)
    {
      instance = new CGRAdapterProperties();
    }
    
    return instance;
  }
  
  public static String getApiVersion()
  {
    return CGRAdapterProperties.getInstance().props.getProperty("apiVersion");
  }
}
