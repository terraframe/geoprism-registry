/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.Command;

import net.geoprism.registry.TableEntity;
import net.geoprism.registry.service.request.WMSService;

public class GeoserverCreateWMSCommand implements Command
{
  private Logger logger = LoggerFactory.getLogger(GeoserverCreateWMSCommand.class);

  private TableEntity version;

  public GeoserverCreateWMSCommand(TableEntity version)
  {
    this.version = version;
  }

  /**
   * Executes the statement in this Command.
   */
  public void doIt()
  {
    try
    {
      logger.info("Creating WMS for TableEntity [" + this.version.getOid() + "]");

      new WMSService().createGeoServerLayer(version, false);
    }
    catch (Throwable t)
    {
      logger.error("Unexpected error while creating geoserver WMS service.", t);
    }
  }

  /**
   * Executes the undo in this Command, and closes the connection.
   */
  public void undoIt()
  {
  }

  /**
   * Returns a human readable string describing what this command is trying to
   * do.
   * 
   * @return human readable string describing what this command is trying to do.
   */
  public String doItString()
  {
    return null;
  }

  public String undoItString()
  {
    return null;
  }

  /*
   * Indicates if this Command deletes something.
   * 
   * @return <code><b>true</b></code> if this Command deletes something.
   */
  public boolean isUndoable()
  {
    return false;
  }

  @Override
  public void doFinally()
  {
  }

}
