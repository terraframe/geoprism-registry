package com.runwaysdk.build.domain;

import java.util.Arrays;
import java.util.Date;
import java.util.SortedSet;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.SynchronizationConfigQuery;
import net.geoprism.registry.etl.DHIS2AttributeMapping;
import net.geoprism.registry.etl.DHIS2SyncConfig;
import net.geoprism.registry.etl.DHIS2SyncLevel;
import net.geoprism.registry.etl.DHIS2SyncLevelPatchAccessor;
import net.geoprism.registry.etl.ExternalSystemSyncConfig;
import net.geoprism.registry.etl.FhirSyncLevel;
import net.geoprism.registry.etl.export.SeverGeoObjectJsonAdapters;
import net.geoprism.registry.graph.DHIS2ExternalSystem;
import net.geoprism.registry.graph.ExternalSystem;

public class DHIS2SyncMultiSelectPatch
{
  public static void main(String[] args)
  {
    new DHIS2SyncMultiSelectPatch().doIt();
  }
  
  @Request
  public void doIt()
  {
    doItTrans();
  }
  
  @Transaction
  public void doItTrans()
  {
    QueryFactory qf = new QueryFactory();
    
    SynchronizationConfigQuery query = new SynchronizationConfigQuery(qf);
    
    OIterator<? extends SynchronizationConfig> it = query.getIterator();
    
    while (it.hasNext())
    {
      SynchronizationConfig config = it.next();
      
      ExternalSystem es = config.getExternalSystem();
      
      if (es instanceof DHIS2ExternalSystem)
      {
        ExternalSystemSyncConfig esconfig = config.buildConfiguration();
        
        if (esconfig instanceof DHIS2SyncConfig)
        {
          DHIS2SyncConfig dhis2Config = (DHIS2SyncConfig) esconfig;
          
          SortedSet<DHIS2SyncLevel> levels = dhis2Config.getLevels();
          
          levels.forEach((DHIS2SyncLevel level) -> {
            String groupId = DHIS2SyncLevelPatchAccessor.getOrgUnitGroupId(level);
            
            if (StringUtils.isNotEmpty(groupId)) {
              level.setOrgUnitGroupIds(Arrays.asList(new String[] {groupId}));
            }
          });
          
          GsonBuilder builder = new GsonBuilder();
          builder.registerTypeAdapter(Date.class, new SeverGeoObjectJsonAdapters.DateSerializer());
          builder.registerTypeAdapter(FhirSyncLevel.class, new FhirSyncLevel.Serializer());
          builder.registerTypeAdapter(DHIS2AttributeMapping.class, new DHIS2AttributeMapping.DHIS2AttributeMappingSerializer());
          builder.serializeNulls();

          Gson gson = builder.create();
          
          config.appLock();
          config.setConfiguration(gson.toJsonTree(esconfig).toString());
          config.apply();
        }
      }
    }
  }
}
