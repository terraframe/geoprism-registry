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
package com.runwaysdk.build.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.registry.jobs.ImportHistoryQuery;
import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.jobs.ImportHistory;
import net.geoprism.registry.model.ServerGeoObjectType;

public class PatchGotIntoImportHistory
{
  private static final Logger logger = LoggerFactory.getLogger(PatchGotIntoImportHistory.class);
  
  public static void main(String[] args)
  {
    new PatchGotIntoImportHistory().doIt();
  }

  @Transaction
  private void doIt()
  {
    ImportHistoryQuery ihq = new ImportHistoryQuery(new QueryFactory());
    
    OIterator<? extends ImportHistory> it = ihq.getIterator();
    try
    {
      for (ImportHistory hist : it)
      {
        try
        {
          ImportConfiguration config = hist.getConfig();
          
          if (config instanceof GeoObjectImportConfiguration)
          {
            GeoObjectImportConfiguration goConfig = (GeoObjectImportConfiguration) config;
            
            ServerGeoObjectType type = goConfig.getType();
            
            hist.appLock();
            hist.setOrganization(type.getOrganization().getOrganization());
            hist.setGeoObjectTypeCode(type.getCode());
            hist.apply();
          }
        }
        catch (net.geoprism.registry.DataNotFoundException e)
        {
          logger.error("ImportHistory references object which does not exist", e);
        }
      }
    }
    finally
    {
      it.close();
    }
  }
}
