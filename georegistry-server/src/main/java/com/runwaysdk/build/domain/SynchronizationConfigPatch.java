package com.runwaysdk.build.domain;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.metadata.MdTermRelationship;

import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.SynchronizationConfigQuery;
import net.geoprism.registry.etl.DHIS2SyncConfig;
import net.geoprism.registry.etl.ExternalSystemSyncConfig;
import net.geoprism.registry.graph.DHIS2ExternalSystem;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.model.ServerHierarchyType;

public class SynchronizationConfigPatch
{
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

    try (OIterator<? extends SynchronizationConfig> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        SynchronizationConfig config = iterator.next();
        ExternalSystem system = config.getExternalSystem();

        if (system instanceof DHIS2ExternalSystem)
        {
          MdTermRelationship universalRelationship = config.getHierarchy();

          if (universalRelationship != null)
          {
            ServerHierarchyType hierarchy = ServerHierarchyType.get(universalRelationship);

            if (hierarchy != null)
            {
              JsonObject json = config.getConfigurationJson();

              json.addProperty(DHIS2SyncConfig.HIERARCHY, hierarchy.getCode());

              config.appLock();
              config.setConfiguration(json.toString());
              config.apply();
            }
          }
        }
      }
    }
  }

}
