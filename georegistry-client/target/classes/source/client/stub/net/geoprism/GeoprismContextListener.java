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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.runwaysdk.generation.loader.LoaderDecorator;

public class GeoprismContextListener implements ServletContextListener
{
  @Override
  public void contextInitialized(ServletContextEvent arg0) {
    try
    {
      LoaderDecorator.load("net.geoprism.context.ServerInitializer").getMethod("initialize").invoke(null);
    }
    catch(Throwable t)
    {
      throw new RuntimeException(t);
    }
  }
  
  @Override
  public void contextDestroyed(ServletContextEvent arg0) {
    try
    {
      LoaderDecorator.load("net.geoprism.context.ServerInitializer").getMethod("destroy").invoke(null);
    }
    catch(Throwable t)
    {
      throw new RuntimeException(t);
    }
  }
}
