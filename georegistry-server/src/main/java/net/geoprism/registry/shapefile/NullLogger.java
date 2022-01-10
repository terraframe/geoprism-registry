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
package net.geoprism.registry.shapefile;

import net.geoprism.data.importer.GISImportLoggerIF;

public class NullLogger implements GISImportLoggerIF
{

  @Override
  public void log(String featureId, Throwable t)
  {
  }

  @Override
  public void log(String featureId, String message)
  {
  }

  @Override
  public boolean hasLogged()
  {
    return true;
  }

  @Override
  public void close()
  {
  }

}
