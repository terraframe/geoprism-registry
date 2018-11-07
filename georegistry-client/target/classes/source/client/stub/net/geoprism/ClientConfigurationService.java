/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeSet;

import com.runwaysdk.constants.ClientRequestIF;


public class ClientConfigurationService 
{
  /**
   * Retrieve all implementations of ConfigurationIF.
   */
  public static List<ClientConfigurationIF> getConfigurations()
  {
    List<ClientConfigurationIF> configurations = new ArrayList<ClientConfigurationIF>();

    ServiceLoader<ClientConfigurationIF> loader = ServiceLoader.load(ClientConfigurationIF.class, Thread.currentThread().getContextClassLoader());

    try
    {
      Iterator<ClientConfigurationIF> it = loader.iterator();

      while (it.hasNext())
      {
        configurations.add(it.next());
      }

    }
    catch (ServiceConfigurationError serviceError)
    {
      throw new RuntimeException(serviceError);
    }

    configurations.add(new DefaultClientConfiguration());

    return configurations;
  }

  public static List<GeoprismApplication> getApplications(ClientRequestIF request)
  {
    List<GeoprismApplication> applications = new LinkedList<GeoprismApplication>();

    List<ClientConfigurationIF> configurations = ClientConfigurationService.getConfigurations();

    for (ClientConfigurationIF configuration : configurations)
    {
      applications.addAll(configuration.getApplications(request));
    }

    return applications;
  }

  public static Set<String> getPublicEndpoints()
  {
    Set<String> applications = new TreeSet<String>();

    List<ClientConfigurationIF> configurations = ClientConfigurationService.getConfigurations();

    for (ClientConfigurationIF configuration : configurations)
    {
      applications.addAll(configuration.getPublicEndpoints());
    }

    return applications;
  }
}
