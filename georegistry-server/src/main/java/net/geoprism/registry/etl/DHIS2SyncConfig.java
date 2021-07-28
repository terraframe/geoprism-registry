/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.etl;

import java.util.SortedSet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.graph.DHIS2ExternalSystem;
import net.geoprism.registry.model.ServerHierarchyType;

public class DHIS2SyncConfig extends ExternalSystemSyncConfig
{
  public static final String            ATTRIBUTES      = "attributes";

  public static final String            HIERARCHY       = "hierarchy";

  public static final String            LEVELS          = "levels";

  public static final String            GEO_OBJECT_TYPE = "geoObjectType";

  public static final String            TYPE            = "type";

  private SortedSet<DHIS2SyncLevel>     levels;

  private String                        hierarchyCode;

  private transient ServerHierarchyType hierarchy;

  public SortedSet<DHIS2SyncLevel> getLevels()
  {
    return levels;
  }

  public void setLevels(SortedSet<DHIS2SyncLevel> levels)
  {
    this.levels = levels;
  }

  public String getHierarchyCode()
  {
    return hierarchyCode;
  }

  public void setHierarchyCode(String hierarchyCode)
  {
    this.hierarchyCode = hierarchyCode;
  }

  public ServerHierarchyType getHierarchy()
  {
    return hierarchy;
  }

  public void setHierarchy(ServerHierarchyType hierarchy)
  {
    this.hierarchy = hierarchy;
  }

  @Override
  public DHIS2ExternalSystem getSystem()
  {
    return (DHIS2ExternalSystem) super.getSystem();
  }

  @Override
  public void populate(SynchronizationConfig config)
  {
    super.populate(config);

    JsonObject json = config.getConfigurationJson();

    String hierarchyCode = json.get(DHIS2SyncConfig.HIERARCHY).getAsString();

    this.setHierarchyCode(hierarchyCode);
    this.setHierarchy(ServerHierarchyType.get(hierarchyCode));

    JsonArray jaLevels = json.get(LEVELS).getAsJsonArray();
    // this.levels = new GsonBuilder().create().fromJson(jaLevels, new
    // TypeToken<SortedSet<SyncLevel>>() {}.getType());

    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapter(DHIS2AttributeMapping.class, new DHIS2AttributeMapping.DHIS2AttributeMappingDeserializer());
    Gson gson = builder.create();
    this.levels = gson.fromJson(jaLevels, new TypeToken<SortedSet<DHIS2SyncLevel>>()
    {
    }.getType());
  }

}
