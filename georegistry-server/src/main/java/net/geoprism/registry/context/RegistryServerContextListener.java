/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.context;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

@Component
public class RegistryServerContextListener implements ServletContextListener
{

  public static class StartupThread implements Runnable
  {
    private static final Log log = LogFactory.getLog(StartupThread.class);

    public StartupThread()
    {
      super();
    }

    @Override
    public void run()
    {
      // No longer does anything
      // Left in case we need to rebuild something on boot
    }
  }

  @Override
  public void contextInitialized(ServletContextEvent sce)
  {
    Thread t = new Thread(new StartupThread());
    t.setDaemon(true);
    t.start();
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce)
  {
  }
}
