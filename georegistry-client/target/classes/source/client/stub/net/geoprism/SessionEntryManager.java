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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.runwaysdk.generation.loader.LoaderDecorator;

public class SessionEntryManager
{

  private static final Log log = LogFactory.getLog(SessionEntryManager.class);
  
  /**
   * Initializes the SessionEntry objects and whatever those components require.
   */
//  @Request
  public static void initialize()
  {
    // use reflection to avoid Reloadable being infectious
    try
    {
      LoaderDecorator.load("net.geoprism.SessionEntry").getMethod("deleteAll").invoke(null);
    }
    catch(Throwable t)
    {
      log.error("Unable to call SessionEntry.initialize()", t);
      throw new RuntimeException(t);
    }
  }
  
  /**
   * Destroy the SessionEntry objects and whatever those components require.
   */
//  @Request
  public static void destroy()
  {
    // use reflection to avoid Reloadable being infectious
    try
    {
      LoaderDecorator.load("net.geoprism.SessionEntry").getMethod("deleteAll").invoke(null);
    }
    catch(Throwable t)
    {
      log.error("Unable to call SessionEntry.destroy()", t);
      throw new RuntimeException(t);
    }
  }
  
}
