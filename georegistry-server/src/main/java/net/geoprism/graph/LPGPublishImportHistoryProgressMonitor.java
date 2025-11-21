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
package net.geoprism.graph;

import net.geoprism.registry.etl.ImportStage;
import net.geoprism.registry.jobs.ImportHistory;
import net.geoprism.registry.lpg.LPGPublishProgressMonitorIF;

public class LPGPublishImportHistoryProgressMonitor implements LPGPublishProgressMonitorIF
{
  
  private ImportHistory history;
  
  public LPGPublishImportHistoryProgressMonitor(ImportHistory history)
  {
    this.history = history;
  }
  
  @Override
  public void appLock()
  {
    this.history.appLock();
  }

  @Override
  public void apply()
  {
    this.history.apply();
  }

  @Override
  public void setWorkProgress(Long num)
  {
    this.history.setWorkProgress(num);
    this.history.setImportedRecords(num);
  }

  @Override
  public void setWorkTotal(Long total)
  {
    this.history.setWorkTotal(total);
  }

  @Override
  public void clearStage()
  {
    this.history.clearStage();
  }

  @Override
  public void addStage(Object importStage)
  {
    this.history.addStage((ImportStage) importStage);
  }

}
