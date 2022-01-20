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

import com.google.gson.JsonObject;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.metadata.MdTermRelationship;

import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.SynchronizationConfigQuery;
import net.geoprism.registry.etl.DHIS2SyncConfig;
import net.geoprism.registry.graph.DHIS2ExternalSystem;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.model.ServerHierarchyType;

public class SynchronizationConfigPatch
{
  private static final Logger logger = LoggerFactory.getLogger(SynchronizationConfigPatch.class);
  
  public static void main(String[] args)
  {
    new SynchronizationConfigPatch().doIt();
  }

  @Request
  private void doIt()
  {
    patchJobs();
  }

  @Transaction
  private void patchJobs()
  {
    SynchronizationConfigQuery query = new SynchronizationConfigQuery(new QueryFactory());

    logger.info("Attempting to patch " + query.getCount() + " synchronization configs.");
    
    long count = 0;
    
    try (OIterator<? extends SynchronizationConfig> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        SynchronizationConfig config = iterator.next();
        ExternalSystem system = config.getExternalSystem();

        if (system instanceof DHIS2ExternalSystem)
        {
          JsonObject json = config.getConfigurationJson();
          ServerHierarchyType hierarchy = null;
          
          MdTermRelationship universalRelationship = config.getHierarchy();

          if (universalRelationship != null)
          {
            hierarchy = ServerHierarchyType.get(universalRelationship);
          }
          else if (json.has("hierarchy"))
          {
            hierarchy = ServerHierarchyType.get(json.get("hierarchy").getAsString());
          }
          else if (json.has("hierarchyCode"))
          {
            hierarchy = ServerHierarchyType.get(json.get("hierarchyCode").getAsString());
          }
          
          if (hierarchy != null)
          {
            json.remove("hierarchy");
            json.addProperty(DHIS2SyncConfig.HIERARCHY, hierarchy.getCode());

            config.appLock();
            config.setConfiguration(json.toString());
            config.apply();
            
            count++;
          }
          else
          {
            logger.error("Skipping " + config.getKey() + " because we couldn't resolve a hierarchy.");
          }
        }
      }
    }
    
    logger.info("Successfully patched " + count + " synchronization configs.");
  }

}
