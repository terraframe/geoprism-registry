/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.runwaysdk.session.Request;

import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import net.geoprism.context.ServerContextListener;
import net.geoprism.gis.geoserver.GeoserverFacade;
import net.geoprism.gis.geoserver.GeoserverProperties;
import net.geoprism.registry.service.WMSService;

public class RegistryServerContextListener implements ServerContextListener
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
      GeoServerRESTReader reader = GeoserverProperties.getReader();

      while (true)
      {
        try
        {
          if (reader.existGeoserver() && GeoserverFacade.workspaceExists())
          {
            new WMSService().createAllWMSLayers(false);

            return; // we are done here
          }
          else
          {
            log.debug("Waiting for geoserver");

            try
            {
              Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
            }
          }
        }
        catch (Throwable t)
        {
          // we couldn't hit the application correctly, so log the error
          // and quit the loop to avoid excessive logging
          log.error("Unable to start the application.", t);

          return;
        }
      }
    }
  }

  @Override
  public void initialize()
  {
  }

  @Override
  @Request
  public void startup()
  {
    Thread t = new Thread(new StartupThread());
    t.setDaemon(true);
    t.start();
  }

  @Override
  public void shutdown()
  {
  }
}
