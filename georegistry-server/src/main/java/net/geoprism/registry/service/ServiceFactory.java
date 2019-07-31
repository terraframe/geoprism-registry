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
package net.geoprism.registry.service;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.RegistryAdapterServer;

import net.geoprism.registry.AdapterUtilities;

public class ServiceFactory
{
  private static ServiceFactory instance;
  
  private ConversionService cs;
  
  private AdapterUtilities util;
  
  private RegistryIdService idService;
  
  private RegistryService registryService;
  
  private RegistryAdapter adapter;
  
  private void initialize()
  {
    this.registryService = new RegistryService();
    this.cs = new ConversionService();
    this.util = new AdapterUtilities();
    this.idService = new RegistryIdService();
    
    this.adapter = new RegistryAdapterServer(this.idService);
    
    this.registryService.initialize(this.adapter);
  }
  
  public static synchronized ServiceFactory getInstance()
  {
    if (instance == null)
    {
      instance = new ServiceFactory();
      instance.initialize();
    }
    
    return instance;
  }

  public static RegistryAdapter getAdapter()
  {
    return ServiceFactory.getInstance().adapter;
  }

  public static RegistryService getRegistryService()
  {
    return ServiceFactory.getInstance().registryService;
  }
  
  public static ConversionService getConversionService()
  {
    return ServiceFactory.getInstance().cs;
  }
  
  public static AdapterUtilities getUtilities()
  {
    return ServiceFactory.getInstance().util;
  }
  
  public static RegistryIdService getIdService()
  {
    return ServiceFactory.getInstance().idService;
  }
}
