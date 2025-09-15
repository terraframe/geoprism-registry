/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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

import com.google.gson.JsonObject;

import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.SynchronizationConfig.Type;
import net.geoprism.registry.graph.JenaExternalSystem;

public class JenaExportConfig extends ExternalSystemSyncConfig
{

  private String publishUid;

  private String namespace;

  private String graph;

  public JenaExportConfig()
  {
    this.namespace = "http://terraframe.com";
    this.graph = this.namespace + "/g1";
  }

  public String getPublishUid()
  {
    return publishUid;
  }

  public void setPublishUid(String publishUid)
  {
    this.publishUid = publishUid;
  }

  public String getNamespace()
  {
    return namespace;
  }

  public void setNamespace(String namespace)
  {
    this.namespace = namespace;
  }

  public String getGraph()
  {
    return graph;
  }

  public void setGraph(String graph)
  {
    this.graph = graph;
  }

  @Override
  public JenaExternalSystem getSystem()
  {
    return (JenaExternalSystem) super.getSystem();
  }

  @Override
  public void populate(SynchronizationConfig config)
  {
    super.populate(config);

    JsonObject json = config.getConfigurationJson();

    if (json != null && json.has("publishUid"))
    {
      this.setPublishUid(json.get("publishUid").getAsString());
    }

    if (json != null && json.has("namespace"))
    {
      this.setNamespace(json.get("namespace").getAsString());
    }

    if (json != null && json.has("graph"))
    {
      this.setGraph(json.get("graph").getAsString());
    }
  }

  @Override
  public String getSynchronizationType()
  {
    return Type.JENA.name();
  }

}
